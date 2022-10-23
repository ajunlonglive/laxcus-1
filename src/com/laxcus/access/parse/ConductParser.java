/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.*;
import java.util.regex.*;

import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * CONDUCT命令语法解析器。<br><br>
 * 
 * CONDUCT命令是DIFFUSE/CONVERGE分布算法的语句化实现。用于异步、并行、分布式的大规模数据计算处理工作<br><br>
 * 
 * 说明：<BR>
 * 1. 命令由“CONDUCT 应用名称 ”开始，按照INIT、FROM、TO、BALANCE、PUT顺序解析，用户输入的文本命令应该与此保持一致。<br>
 * 2. “INIT、BALANCE、PUT”是可选阶段，在语句可以省略，“FROM、TO”是必选阶段。<br>
 * 3. INIT负责参数的初始化、规则定义、资源总体分配工作。<br>
 * 4. FROM分为单处理（SINGLE）和并行处理（DIFFUSE）两种情况。单处理没有子命名，并行处理在“DIFFUSE”关键字之后是子命名。单处理执行一种需求的数据处理，并行处理执行不同需求的数据处理。<br>
 * 5. TO是迭代过程，执行最少一次，或者任意多个，即一次TO处理，或者“TO->SUBTO->SUBTO ...”的处理。它们的区别是TO不需要子命名，SUBTO必须有子命名。<br>
 * 6. TO阶段的数据处理模式为“产生数据和计算数据”两种可能。格式：“MODE:[GENERATE|EVALUATE]”。<br>
 * 7. BALANCE接收上个阶段的元数据，执行数据平衡分析和计算后，分配给下个阶段去处理。<br>
 * 8. PUT位于FRONT站点，负责最后数据的显示和保存工作，如可视化。<br>
 * 9. CONDUCT支持事务，事务语句在FROM/TO阶段最后，以“ATTACH  [ALL|DATABASE|TABLE] ... BE [SHARE|NOT SHARE] AND ATTACH  ...”格式出现。它是可选语句。<br>
 * 10. CONDUCT所有参数和命名，都忽略大小写。<br>
 * 11. 如果语句的字符与与阶段关键字冲突，即有“INIT、FROM、BALANCE、TO、SUBTO、PUT”这几种字符，需要做转义处理。转义字符的特点：左右两侧是空格，字符前面有一个“\”符号。<br>
 * 
 * <br><br>
 * 
 * CONDUCT文本命令格式：<br>
 * CONDUCT 应用名称 <br>
 * INIT [系统参数和自定义参数 ...] <br>
 * FROM [系统参数和自定义参数 ...] | FROM DIFFUSE 子命名 [系统参数和自定义参数 ...] DIFFUSE 子命名 [系统参数和自定义参数 ...] <br> 
 * TO [系统参数和自定义参数 ...] SUBTO 子命名 [系统参数和自定义参数 ...] SUBTO 子命名 [系统参数和自定义参数 ...] <br>
 * BALANCE [系统参数和自定义参数 ...] <br> 
 * PUT [系统参数和自定义参数 ...] <br>
 *
 * <br><br>
 *
 * 参数分为系统参数和自定义参数两种，参数用ASCII字符的分号(;)结束，自定义参数名称允许重复。。<br>
 * 系统参数格式： 参数命名:[参数值]; <br>
 * 自定义参数格式：参数名称(数据类型)=[参数值]; <br><br>
 * 
 * 自定义参数分为5种情况：<br>
 * 1. 字符串或者日期类型被单引号（' ... '）包裹。<br> 
 * 2. 命令被双引号(" ... ")包裹 。<br>
 * 3. 数值无引号 。<br>
 * 4. 二进制数字以“0x”前缀开始。<br>
 * 5. 布尔值分“TRUE|FALSE|YES|NO”4种。<br>
 * 
 * <br><br>
 * 系统参数示例：<br>
 * SITES:3; MODE:GENERATE; WRITETO:"/echo/work/origin.bin"; <br><br> 
 * 
 * 自定义参数示例：<br>
 * OKAY(BOOL)=TRUE; HELO(INT)=123; ID(RAW)=0x23900; TODAY(DATE)='2030-12-9';  EMAIL(STRING)='laxcus@laxcus.org' ; QUERY(COMMAND)="SELECT * \FROM DEMO.SHOW WHERE ID>0"; <br><br>
 * 
 * @author scott.liang
 * @version 1.3 8/23/2013
 * @since laxcus 1.0
 */
public class ConductParser extends DistributedParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:CONDUCT)\\s+([\\w\\W]+)\\s*$";

	/** CONDUCT语法 **/
	private final static String PREFIX = "^\\s*(?i)(?:CONDUCT)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\.[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+([\\p{ASCII}\\W]+)\\s*$";

	/** 各阶段前缀符号 **/
	private final static String INIT_PREFIX = "^\\s*(?i)(INIT[\\p{ASCII}\\W]*?)(?i)(\\s+(?:FROM)\\s+[\\p{ASCII}\\W]*)$";
	private final static String FROM_PREFIX = "^\\s*(?i)(FROM[\\p{ASCII}\\W]*?)(?i)(\\s+(?:TO)\\s+[\\p{ASCII}\\W]*)$";
	private final static String TO_PREFIX = "^\\s*(?i)(TO[\\p{ASCII}\\W]*?)(?i)(\\s*|\\s+(?:BALANCE|PUT)\\s+[\\p{ASCII}\\W]*)$";
	private final static String BALANCE_PREFIX = "^\\s*(?i)(BALANCE[\\p{ASCII}\\W]*?)(?i)(\\s*|\\s+(?:PUT)\\s+[\\p{ASCII}\\W]*)$";

	/** 公共子任务命名 **/
	private final static String INIT_SYNTAX = "^\\s*(?i)(?:INIT)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String BALANCE_SYNTAX = "^\\s*(?i)(?:BALANCE)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String PUT_SYNTAX = "^\\s*(?i)(?:PUT)(\\s*|\\s+[\\p{ASCII}\\W]+)$";

	/** FROM多要求并行处理语句 **/
	private final static String FROM_PARALLEL_PREFIX = "^\\s*(?i)(?:FROM)\\s+(?i)(DIFFUSE\\s+[\\p{ASCII}\\W]+)$";
	private final static String FROM_PARALLEL_SPLIT = "^\\s*((?i)(?:DIFFUSE)\\s+[\\p{ASCII}\\W]+?)(\\s*|\\s+(?i)(?:DIFFUSE)\\s+[\\p{ASCII}\\W]+)$";
	private final static String FROM_PARALLEL_SEGMENT = "^\\s*(?i)(?:DIFFUSE)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)(\\s*?|\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+ATTACH\\s+[\\p{ASCII}\\W]+)$";
	/** FROM单一要求处理语句 **/
	private final static String FROM_SINGLE = "^\\s*(?i)(?:FROM)([\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+ATTACH\\s+[\\p{ASCII}\\W]+\\s+BE\\s+[\\p{ASCII}\\W]+)$";

	/** TO/SUBTO阶段语句 **/
	private final static String TO_ALL_STYLE = "^\\s*(?i)(TO\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+SUBTO\\s+[\\p{ASCII}\\W]+)$";
	private final static String SUBTO_STYLE = "^\\s*(?i)(SUBTO\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+SUBTO\\s+[\\p{ASCII}\\W]+)$";
	private final static String TO_SEGMENT = "^^\\s*(?i)(?:TO)(\\s*?|\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+ATTACH\\s+[\\p{ASCII}\\W]+)$";
	private final static String SUBTO_SEGMENT = "^\\s*(?i)(?:SUBTO)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)(\\s*?|\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+ATTACH\\s+[\\p{ASCII}\\W]+)$";

	/** 系统参数 **/
	private final static String SITES = "^\\s*(?i)SITES\\s*:\\s*(\\d+)\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";
	private final static String TO_MODE = "^\\s*(?i)(?:MODE\\s*:\\s*)(?i)(GENERATE|EVALUATE)\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";
	private final static String GENERATE_TAG = "^\\s*(?i)GENERATE\\s*$";
	private final static String WRITETO = "^\\s*(?i)(?:WRITETO)\\s*\\:\\s*\\\"([\\p{ASCII}\\W]+?)\\\"\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";

	/**
	 * 构造CONDUCT语句解析器
	 */
	public ConductParser() {
		super();
	}

	/**
	 * 将与阶段关键字冲突的字符做转义处理。被转义字符左右两侧有空格，且有一个前缀符号：“\”。
	 * 转义发生在阶段关键字被过滤之后！！！
	 * @param input 原文
	 * @return 转义后的字符串语句
	 */
	private String replace(String input) {
		input = replace(input, "INIT");
		input = replace(input, "FROM");
		input = replace(input, "BALANCE");
		input = replace(input, "TO");
		input = replace(input, "SUBTO");
		input = replace(input, "PUT");

		return input;
	}

	/**
	 * 检查传入的参数是否匹配"conduct"语句
	 * @param simple 简单判断
	 * @param input  输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CONDUCT", input);
		}
		Pattern pattern = Pattern.compile(ConductParser.PREFIX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“INIT”阶段语句
	 * @param sock
	 * @param input
	 * @param online 在线模式
	 * @return
	 */
	private InitObject splitInit(Sock sock, String input, boolean online) {
		Pattern pattern = Pattern.compile(ConductParser.INIT_SYNTAX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		// 解析
		String segment = matcher.group(1);
		// 替换与CONDUCT阶段关键字冲突的转义字符
		segment = replace(segment);

		// 初始化对象
		Phase phase = new Phase(PhaseTag.INIT, sock);
		InitObject object = new InitObject(phase);

		// 解析参数
		while (segment.trim().length() > 0) {
			// 如果是自定义参数
			if (isTaskParameter(segment)) {
				segment = splitTaskParameter(object, segment, online);
				continue;
			}
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, segment);
		}
		return object;
	}

	/**
	 * 解析单个FROM阶段语句
	 * @param form FROM对象
	 * @param input FROM单语句
	 * @param online 在线模式
	 * @return 成功返回真，否则假
	 */
	private boolean splitFromSingle(FromObject from, String input, boolean online) {
		// 单行记录
		Pattern pattern = Pattern.compile(ConductParser.FROM_SINGLE);
		Matcher matcher = pattern.matcher(input);
		// 不匹配返回假
		if (!matcher.matches()) {
			return false;
		}

		// 参数域，过滤还原转义字符
		String segment = matcher.group(1);
		segment = replace(segment);
		// 事务规则
		String trans = matcher.group(2);

		FromInputter inputter = new FromInputter(from.getPhase());

		// 循环解析参数
		while (segment.trim().length() > 0) {
			// 指定主机数
			pattern = Pattern.compile(ConductParser.SITES);
			matcher = pattern.matcher(segment);
			if (matcher.matches()) {
				int sites = Integer.parseInt(matcher.group(1));
				inputter.setSites(sites);
				segment = matcher.group(2);
				continue;
			}
			// 自定义参数
			if (isTaskParameter(segment)) {
				segment = splitTaskParameter(inputter, segment, online);
				continue;
			}
			// 弹出错误
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, segment);
		}

		if (trans.trim().length() > 0) {
			List<RuleItem> rules = splitRules(trans, online);
			inputter.addRules(rules);
		}

		from.addInputter(inputter);

		return true;
	}

	/**
	 * 解析“DIFFUSE 命名 [内容] [ATTACH ...]”语句段
	 * @param sock
	 * @param input
	 * @param online 在线模式
	 * @return
	 */
	private FromInputter splitFromParallelElement(Sock sock, String input, boolean online) {
		Pattern pattern = Pattern.compile(ConductParser.FROM_PARALLEL_SEGMENT);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// FROM阶段子命名
		String sub = matcher.group(1);
		// 内容
		String segment = matcher.group(2);
		// 事务规则
		String trans = matcher.group(3);

		Phase phase = new Phase(PhaseTag.FROM, sock, sub);
		FromInputter inputter = new FromInputter(phase);

		while (segment.trim().length() > 0) {
			// 指定主机数
			pattern = Pattern.compile(ConductParser.SITES);
			matcher = pattern.matcher(segment);
			if (matcher.matches()) {
				int sites = Integer.parseInt(matcher.group(1));
				inputter.setSites(sites);
				segment = matcher.group(2);
				continue;
			}
			// 自定义参数
			if (isTaskParameter(segment)) {
				segment = splitTaskParameter(inputter, segment, online);
				continue;
			}

			throwableNo(FaultTip.INCORRECT_SYNTAX_X, segment);
		}

		// 解析事务
		if (trans.trim().length() > 0) {
			List<RuleItem> rules = splitRules(trans, online);
			inputter.addRules(rules);
		}

		return inputter;
	}

	/**
	 * 解析FROM并行处理语句
	 * @param from FROM对象
	 * @param con FROM阶段语句
	 * @param online 在线模式
	 * @return 成功返回真，否则假
	 */
	private boolean splitFromParallel(FromObject from, String input, boolean online) {
		// 多语句平行分布
		Pattern pattern = Pattern.compile(ConductParser.FROM_PARALLEL_PREFIX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return false;
		}

		Sock sock = from.getPhase().getSock();

		String suffix = matcher.group(1);
		while (suffix.trim().length() > 0) {
			pattern = Pattern.compile(ConductParser.FROM_PARALLEL_SPLIT);
			matcher = pattern.matcher(suffix);
			if (!matcher.matches()) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, suffix);
			}

			// 替换掉与系统关键字可能存在冲突的转义字符
			String segment = matcher.group(1);
			segment = replace(segment);

			suffix = matcher.group(2);

			// FROM并行输入语句
			FromInputter inputter = splitFromParallelElement(sock, segment, online);
			from.addInputter(inputter);
		}

		return true;
	}

	/**
	 * 解析FROM阶段语句，有两种格式：<br>
	 * 1. 多要求并行处理语句：FROM DIFFUSE 命名 [参数] [ATTACH ... BE ... ]  DIFFUSE 命名 [参数] [ATTACH ... BE ... ] <br> 
	 * 2. 单要求语句：FROM [参数] [ATTACH ... BE ...] <br>
	 * 
	 * @param sock 应用名称 
	 * @param input FROM语句
	 * @param online 在线模式
	 * @return FROM对象
	 */
	private FromObject splitFrom(Sock sock, String input, boolean online) {
		Phase phase = new Phase(PhaseTag.FROM, sock);
		FromObject from = new FromObject(phase);

		// 判断是“FROM DIFFUSE”并行语句
		boolean success = splitFromParallel(from, input, online);
		// 判断是FROM单一语句
		if (!success) {
			success = splitFromSingle(from, input, online);
		}
		// 以上不成功弹出错误
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		return from;
	}

	/**
	 * 解析TO/SUBTO阶段参数，保存到TO阶段输入器
	 * @param phase 阶段命名
	 * @param segment 参数段
	 * @param trans 事务
	 * @return ToInputter
	 */
	private ToInputter splitToInputter(Phase phase, String segment, String trans, boolean online) {
		// 生成TO阶段数据输入器，默认是EVALUATE(数据计算)状态
		ToInputter inputter = new ToInputter(ToMode.EVALUATE, phase);

		// 解析"TO/SUBTO"参数
		while (segment.trim().length() > 0) {
			// 任务类型定义，默认是EVALUATE类型
			Pattern pattern = Pattern.compile(ConductParser.TO_MODE);
			Matcher matcher = pattern.matcher(segment);
			if (matcher.matches()) {
				String task = matcher.group(1);
				segment = matcher.group(2);
				if (task.matches(ConductParser.GENERATE_TAG)) {
					inputter.setMode(ToMode.GENERATE);
				}
				continue;
			}
			// 指定主机数
			pattern = Pattern.compile(ConductParser.SITES);
			matcher = pattern.matcher(segment);
			if (matcher.matches()) {
				int sites = Integer.parseInt(matcher.group(1));
				inputter.setSites(sites);
				segment = matcher.group(2);
				continue;
			}
			// 如果是自定义参数
			if (isTaskParameter(segment)) {
				segment = splitTaskParameter(inputter, segment, online);
				continue;
			}
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, segment);
		}

		// 事务
		if (trans.trim().length() > 0) {
			List<RuleItem> rules = splitRules(trans, online);
			inputter.addRules(rules);
		}

		return inputter;
	}

	/**
	 * 解析TO根阶段语句
	 * @param sock 应用名称
	 * @param input TO语句
	 * @param online 在线模式
	 * @return ToObject
	 */
	private ToObject splitToElement(Sock sock, String input, boolean online) {
		Pattern pattern = Pattern.compile(ConductParser.TO_SEGMENT);
		Matcher matcher = pattern.matcher(input);
		// 不成功是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// TO阶段命名
		Phase phase = new Phase(PhaseTag.TO, sock);

		// 参数域。与阶段关键字冲突的字符转义
		String segment = matcher.group(1);
		segment = replace(segment);
		// 事务规则
		String trans = matcher.group(2);

		// 生成TO阶段输入器
		ToInputter inputter = splitToInputter(phase, segment, trans, online);

		ToObject object = new ToObject(inputter.getMode(), phase);
		object.setInputter(inputter);

		return object;
	}

	/**
	 * 解析SUBTO语句
	 * @param sock 应用名称
	 * @param input SUBTO语句
	 * @param online 在线模式
	 * @return ToObject
	 */
	private ToObject splitSubToElement(Sock sock, String input, boolean online) {
		// 解析SUBTO阶段语句
		Pattern pattern = Pattern.compile(ConductParser.SUBTO_SEGMENT);
		Matcher matcher = pattern.matcher(input);
		// 不成功是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 子命名
		String sub = matcher.group(1);
		// 参数域
		String segment = matcher.group(2);
		segment = replace(segment);
		// 事务规则
		String trans = matcher.group(3);

		// SUBTO命名
		Phase phase = new Phase(PhaseTag.TO, sock, sub);
		ToInputter inputter = splitToInputter(phase, segment, trans, online);
		// 生成TO子级对象
		ToObject slave = new ToObject(inputter.getMode(), phase);
		slave.setInputter(inputter);
		// 返回SUBTO对象
		return slave;
	}

	/**
	 * 检查TO/SUBTO关系链条
	 * @param object
	 */
	private void checkToLink(ToObject object) {
		// 检查迭代链接，最后一步必须是数据计算状态（EVALUATE状态）
		ToObject last = object;
		// 循环到最后一个
		while (last.hasNext()) {
			last = last.next();
		}
		// 如果最后是产生数据，这是错误
		if (last.isGenerate()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, "GENERATE/EVALUATE");
		}
	}

	/**
	 * 解析TO阶段对象参数，格式：<br>
	 * TO [参数] [ATTACH ...] SUBTO 子命名 [参数] [ATTACH ...] SUBTO 子命名 [参数] [ATTACH ...]
	 * 
	 * @param sock 应用名称
	 * @param input TO阶段语句
	 * @param online 在线模式
	 * @return ToObject对象
	 */
	private ToObject splitTo(Sock sock, String input, boolean online) {
		Pattern pattern = Pattern.compile(ConductParser.TO_ALL_STYLE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String to = matcher.group(1);
		String subto = matcher.group(2);

		// 解析TO语句
		ToObject object = splitToElement(sock, to, online);

		// 拆开SUBTO语句，逐一解析每个SUBTO语句
		while (subto.trim().length() > 0) {
			pattern = Pattern.compile(ConductParser.SUBTO_STYLE);
			matcher = pattern.matcher(subto);
			if (!matcher.matches()) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, subto);
			}

			String prefix = matcher.group(1);
			subto = matcher.group(2);
			ToObject slave = splitSubToElement(sock, prefix, online);
			// 以链表迭代形式绑定SUBTO对象
			object.attach(slave);
		}

		// 检查TO/SUBTO阶段的关系链
		checkToLink(object);

		// 返回TO对象
		return object;
	}

	/**
	 * 解析“PUT“语句
	 * @param sock 应用名称
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return PUT对象
	 */
	private PutObject splitPut(Sock sock, String input, boolean online) {
		Pattern pattern = Pattern.compile(ConductParser.PUT_SYNTAX);
		Matcher matcher = pattern.matcher(input);
		if(!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 解析，转换与阶段关键字冲突的转义字符
		String segment = matcher.group(1);
		segment = replace(segment);

		Phase phase = new Phase(PhaseTag.PUT, sock);
		PutObject object = new PutObject(phase);
		while (segment.trim().length() > 0) {
			// 数据写入的本地文件名
			pattern = Pattern.compile(ConductParser.WRITETO);
			matcher = pattern.matcher(segment);
			if (matcher.matches()) {
				String writeto = matcher.group(1);
				object.setWriteTo(writeto);
				segment = matcher.group(2);
				continue;
			}
			// 如果是自定义参数
			if (isTaskParameter(segment)) {
				segment = splitTaskParameter(object, segment, online);
				continue;
			}
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, segment);
		}
		return object;
	}

	/**
	 * 解析平衡分布语句: "balance ......"
	 * @param sock 应用名称
	 * @param input 输入语句
	 * @return Balance对象
	 */
	private BalanceObject splitBalance(Sock sock, String input, boolean online) {
		Pattern pattern = Pattern.compile(ConductParser.BALANCE_SYNTAX);
		Matcher matcher = pattern.matcher(input);
		if(!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 解析，替换与系统关键字冲突的转义字符
		String segment = matcher.group(1);
		segment = replace(segment);

		Phase phase = new Phase(PhaseTag.BALANCE, sock);
		BalanceObject object = new BalanceObject(phase);

		while (segment.trim().length() > 0) {
			// 如果是自定义参数
			if (isTaskParameter(segment)) {
				segment = splitTaskParameter(object, segment, online);
				continue;
			}
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, segment);
		}
		return object;
	}

	/**
	 * 解析CONDUCT命令
	 * @param input CONDUCT命令的文本语句
	 * @return 返回CONDUCT实例
	 */
	public Conduct split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(ConductParser.PREFIX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，弹出异常
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		Sock sock = splitSock(matcher.group(1)); 	// 应用名称
		String suffix = matcher.group(2); 	// 命名之后的其它参数
		Conduct conduct = new Conduct(sock);

		// 1. 判断INIT阶段语句（可选）
		pattern = Pattern.compile(ConductParser.INIT_PREFIX);
		matcher = pattern.matcher(suffix);
		if (matcher.matches()) {
			String segment = matcher.group(1);
			suffix = matcher.group(2);
			conduct.setInitObject(splitInit(sock, segment, online));
		}
		
		// 2. 判断FROM阶段语句（必有！）
		pattern = Pattern.compile(ConductParser.FROM_PREFIX);
		matcher = pattern.matcher(suffix);
		if (!matcher.matches()) {
			throwableNo(FaultTip.PARAM_MISSING_X, "FROM");
		}
		String segment = matcher.group(1);
		suffix = matcher.group(2);
		FromObject fromObject = splitFrom(sock, segment, online);
		conduct.setFromObject(fromObject);
		
		// 3. 判断TO阶段语句（必有！）
		pattern = Pattern.compile(ConductParser.TO_PREFIX);
		matcher = pattern.matcher(suffix);
		if (!matcher.matches()) {
			throwableNo(FaultTip.PARAM_MISSING_X, "TO");
		}
		segment = matcher.group(1);
		suffix = matcher.group(2);
		ToObject toObject = splitTo(sock, segment, online);
		conduct.setToObject(toObject);
		
		// 4. 判断BALANCE阶段语句（可选）
		pattern = Pattern.compile(ConductParser.BALANCE_PREFIX);
		matcher = pattern.matcher(suffix);
		if (matcher.matches()) {
			segment = matcher.group(1);
			suffix = matcher.group(2);
			conduct.setBalanceObject(splitBalance(sock, segment, online));
		}
		// 5. 判断有PUT阶段语句（可选）
		if (suffix.trim().length() > 0) {
			conduct.setPutObject(splitPut(sock, suffix, online));
		}

		// 生成一个默认的初始化阶段命名
		if (conduct.getInitObject() == null) {
			Phase phase = new Phase(PhaseTag.INIT, sock);
			conduct.setInitObject(new InitObject(phase));
		}

		// 如果是在线模式，检查本地阶段命名和操作权限！
		if (online) {
			Siger issuer = getResourceChooser().getOwner();
			Phase phase = new Phase(issuer, PhaseTag.PUT, sock);
			// 判断存在
			boolean success = hasLocalTask(phase);
			if (!success) {
				throwableNo(FaultTip.NOTFOUND_X, phase.toString());
			}
			// 判断有执行快速计算权限
			success = canConduct();
			if (!success) {
				throwable(FaultTip.PERMISSION_MISSING);
			}
		}

		// 保存CONDUCT原语
		conduct.setPrimitive(input);
		// 返回结果
		return conduct;
	}

//	private final static String SITES = "^\\s*(?i)SITES\\s*:\\s*(\\d+)(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";
//	private final static String TO_MODE = "^\\s*(?i)(?:MODE\\s*:\\s*)(?i)(GENERATE|EVALUATE)(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";
//	private final static String GENERATE_TAG = "^\\s*(?i)GENERATE\\s*$";
//	private final static String WRITETO = "^\\s*(?i)(?:WRITETO)\\s*\\:\\s*\\\"([\\p{ASCII}\\W]+?)\\\"(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";

	public static void main(String[] args) {
		String input = "CONDUCT MINING FROM SITES : 12; PREFIX(STRING)='MINING' ; BEGIN(LONG)=12 ; END(LONG)=122; ZEROS(INT)=3;  GPU(BOOL)=yes ; TO sites:23;  PUT NODE(STRING)='MINE SITE'; SHA256(STRING)='CODE'; WRITETO:\"/disks/random.bin\"; ";
		ConductParser parser = new ConductParser();
		parser.split(input, false);
	}
}