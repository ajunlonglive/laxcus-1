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

import com.laxcus.command.contact.*;
import com.laxcus.distribute.contact.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * CONTACT命令语法解析器。<br><br>
 * 
 * CONTACT命令用于客户机/服务器模型，例如EJB，或者小规模以及简单快速的计算工作，如挖矿，区别于CONDUCT这种大规模的数据处理。<br><br>
 * 
 * 说明：<BR>
 * 1. 命令由“CONTACT 基础字 ”开始，按照FORK、DISTANT、MERGE、NEAR顺序解析，用户输入的文本命令应该与此保持一致。<br>
 * 2. “FORK、MERGE、NEAR”是可选阶段，在语句可以省略，“DISTANT”是必选阶段。<br>
 * 3. FORK负责参数的初始化、规则定义、资源总体分配工作。<br>
 * 4. DISTANT是迭代过程，执行最少一次，或者任意多个，即一次DISTANT处理，或者“DISTANT->SUBDISTANT->SUBDISTANT ...”的处理。它们的区别是DISTANT不需要子命名，SUBDISTANT必须有子命名。<br>
 * 5. DISTANT阶段的数据处理模式为“产生数据和计算数据”两种可能。格式：“MODE:[GENERATE|EVALUATE]”。<br>
 * 6. MERGE接收上个阶段的元数据，执行数据平衡分析和计算后，分配给下个阶段去处理。<br>
 * 7. NEAR位于FRONT站点，负责最后数据的显示和保存工作，如可视化。<br>
 * 8. CONTACT支持事务，事务语句在DISTANT阶段最后，以“ATTACH  [ALL|DATABASE|TABLE] ... BE [SHARE|NOT SHARE] AND ATTACH  ...”格式出现。它是可选语句。<br>
 * 9. CONTACT所有参数和命名，都忽略大小写。<br>
 * 10. 如果语句的字符与与阶段关键字冲突，即有“FORK、MERGE、DISTANT、SUBDISTANT、NEAR”这几种字符，需要做转义处理。转义字符的特点：左右两侧是空格，字符前面有一个“\”符号。<br>
 * 
 * <br><br>
 * 
 * CONTACT文本命令格式：<br>
 * CONTACT 基础字 <br>
 * FORK [系统参数和自定义参数 ...] <br> 
 * DISTANT [系统参数和自定义参数 ...] SUBDISTANT 子命名 [系统参数和自定义参数 ...] SUBDISTANT 子命名 [系统参数和自定义参数 ...] <br>
 * MERGE [系统参数和自定义参数 ...] <br> 
 * NEAR [系统参数和自定义参数 ...] <br>
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
 * SITES:3; MODE:GENERATE; WRITEDISTANT:"/echo/work/origin.bin"; <br><br> 
 * 
 * 自定义参数示例：<br>
 * OKAY(BOOL)=TRUE; HELO(INT)=123; ID(RAW)=0x23900; DISTANTDAY(DATE)='2030-12-9';  EMAIL(STRING)='laxcus@laxcus.org' ; QUERY(COMMAND)="SELECT * \FROM DEMO.SHOW WHERE ID>0"; <br><br>
 * 
 * @author scott.liang
 * @version 1.3 8/23/2013
 * @since laxcus 1.0
 */
public class ContactParser extends DistributedParser {
	
	/** CONTACT语法 **/
	private final static String PREFIX = "^\\s*(?i)(?:CONTACT)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\.[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+([\\p{ASCII}\\W]+)\\s*$";

	/** 各阶段前缀符号 **/
	private final static String FORK_PREFIX = "^\\s*(?i)(FORK[\\p{ASCII}\\W]*?)(?i)(\\s+(?:DISTANT)\\s+[\\p{ASCII}\\W]*)$";
	private final static String DISTANT_PREFIX = "^\\s*(?i)(DISTANT[\\p{ASCII}\\W]*?)(?i)(\\s*|\\s+(?:MERGE|NEAR)\\s+[\\p{ASCII}\\W]*)$";
	private final static String MERGE_PREFIX = "^\\s*(?i)(MERGE[\\p{ASCII}\\W]*?)(?i)(\\s*|\\s+(?:NEAR)\\s+[\\p{ASCII}\\W]*)$";

	/** 公共子任务命名 **/
	private final static String FORK_SYNTAX = "^\\s*(?i)(?:FORK)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String MERGE_SYNTAX = "^\\s*(?i)(?:MERGE)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String NEAR_SYNTAX = "^\\s*(?i)(?:NEAR)(\\s*|\\s+[\\p{ASCII}\\W]+)$";

	/** DISTANT/SUBDISTANT阶段语句 **/
	private final static String DISTANT_ALL_STYLE = "^\\s*(?i)(DISTANT\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+SUBDISTANT\\s+[\\p{ASCII}\\W]+)$";
	private final static String SUBDISTANT_STYLE = "^\\s*(?i)(SUBDISTANT\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+SUBDISTANT\\s+[\\p{ASCII}\\W]+)$";
	private final static String DISTANT_SEGMENT = "^^\\s*(?i)(?:DISTANT)(\\s*?|\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+ATTACH\\s+[\\p{ASCII}\\W]+)$";
	private final static String SUBDISTANT_SEGMENT = "^\\s*(?i)(?:SUBDISTANT)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)(\\s*?|\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+ATTACH\\s+[\\p{ASCII}\\W]+)$";

	/** 系统参数 **/
	private final static String SITES = "^\\s*(?i)SITES\\s*:\\s*(\\d+)\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";
	private final static String DISTANT_MODE = "^\\s*(?i)(?:MODE\\s*:\\s*)(?i)(GENERATE|EVALUATE)\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";
	private final static String GENERATE_TAG = "^\\s*(?i)GENERATE\\s*$";
	private final static String WRITEDISTANT = "^\\s*(?i)(?:WRITEDISTANT)\\s*\\:\\s*\\\"([\\p{ASCII}\\W]+?)\\\"\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";

	/**
	 * 构造CONTACT语句解析器
	 */
	public ContactParser() {
		super();
	}

	/**
	 * 将与阶段关键字冲突的字符做转义处理。被转义字符左右两侧有空格，且有一个前缀符号：“\”。
	 * 转义发生在阶段关键字被过滤之后！！！
	 * @param input 原文
	 * @return 转义后的字符串语句
	 */
	private String replace(String input) {
		input = replace(input, "FORK");
		input = replace(input, "MERGE");
		input = replace(input, "DISTANT");
		input = replace(input, "SUBDISTANT");
		input = replace(input, "NEAR");

		return input;
	}

	/**
	 * 检查传入的参数是否匹配"contact"语句
	 * @param input  输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CONTACT", input);
		}
		Pattern pattern = Pattern.compile(ContactParser.PREFIX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“FORK”阶段语句
	 * @param sock
	 * @param input
	 * @param online 在线模式
	 * @return
	 */
	private ForkObject splitFork(Sock sock, String input, boolean online) {
		Pattern pattern = Pattern.compile(ContactParser.FORK_SYNTAX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		// 解析
		String segment = matcher.group(1);
		// 替换与CONTACT阶段关键字冲突的转义字符
		segment = replace(segment);

		// 初始化对象
		Phase phase = new Phase(PhaseTag.FORK, sock);
		ForkObject object = new ForkObject(phase);

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
	 * 解析DISTANT/SUBDISTANT阶段参数，保存到DISTANT阶段输入器
	 * @param phase 阶段命名
	 * @param segment 参数段
	 * @param trans 事务
	 * @return DistantInputter
	 */
	private DistantInputter splitDistantInputter(Phase phase, String segment, String trans, boolean online) {
		// 生成DISTANT阶段数据输入器，默认是EVALUATE(数据计算)状态
		DistantInputter inputter = new DistantInputter(DistantMode.EVALUATE, phase);

		// 解析"DISTANT/SUBDISTANT"参数
		while (segment.trim().length() > 0) {
			// 任务类型定义，默认是EVALUATE类型
			Pattern pattern = Pattern.compile(ContactParser.DISTANT_MODE);
			Matcher matcher = pattern.matcher(segment);
			if (matcher.matches()) {
				String task = matcher.group(1);
				segment = matcher.group(2);
				if (task.matches(ContactParser.GENERATE_TAG)) {
					inputter.setMode(DistantMode.GENERATE);
				}
				continue;
			}
			// 指定主机数
			pattern = Pattern.compile(ContactParser.SITES);
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
	 * 解析DISTANT根阶段语句
	 * @param sock 基础字
	 * @param input DISTANT语句
	 * @param online 在线模式
	 * @return DistantObject
	 */
	private DistantObject splitDistantElement(Sock sock, String input, boolean online) {
		Pattern pattern = Pattern.compile(ContactParser.DISTANT_SEGMENT);
		Matcher matcher = pattern.matcher(input);
		// 不成功是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// DISTANT阶段命名
		Phase phase = new Phase(PhaseTag.DISTANT, sock);

		// 参数域。与阶段关键字冲突的字符转义
		String segment = matcher.group(1);
		segment = replace(segment);
		// 事务规则
		String trans = matcher.group(2);

		// 生成DISTANT阶段输入器
		DistantInputter inputter = splitDistantInputter(phase, segment, trans, online);

		DistantObject object = new DistantObject(inputter.getMode(), phase);
		object.setInputter(inputter);

		return object;
	}

	/**
	 * 解析SUBDISTANT语句
	 * @param sock 基础字
	 * @param input SUBDISTANT语句
	 * @param online 在线模式
	 * @return DistantObject
	 */
	private DistantObject splitSubDistantElement(Sock sock, String input, boolean online) {
		// 解析SUBDISTANT阶段语句
		Pattern pattern = Pattern.compile(ContactParser.SUBDISTANT_SEGMENT);
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

		// SUBDISTANT命名
		Phase phase = new Phase(PhaseTag.DISTANT, sock, sub);
		DistantInputter inputter = splitDistantInputter(phase, segment, trans, online);
		// 生成DISTANT子级对象
		DistantObject slave = new DistantObject(inputter.getMode(), phase);
		slave.setInputter(inputter);
		// 返回SUBDISTANT对象
		return slave;
	}

	/**
	 * 检查DISTANT/SUBDISTANT关系链条
	 * @param object
	 */
	private void checkDistantLink(DistantObject object) {
		// 检查迭代链接，最后一步必须是数据计算状态（EVALUATE状态）
		DistantObject last = object;
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
	 * 解析DISTANT阶段对象参数，格式：<br>
	 * DISTANT [参数] [ATTACH ...] SUBDISTANT 子命名 [参数] [ATTACH ...] SUBDISTANT 子命名 [参数] [ATTACH ...]
	 * 
	 * @param sock 基础字
	 * @param input DISTANT阶段语句
	 * @param online 在线模式
	 * @return DistantObject对象
	 */
	private DistantObject splitDistant(Sock sock, String input, boolean online) {
		Pattern pattern = Pattern.compile(ContactParser.DISTANT_ALL_STYLE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String to = matcher.group(1);
		String subto = matcher.group(2);

		// 解析DISTANT语句
		DistantObject object = splitDistantElement(sock, to, online);

		// 拆开SUBDISTANT语句，逐一解析每个SUBDISTANT语句
		while (subto.trim().length() > 0) {
			pattern = Pattern.compile(ContactParser.SUBDISTANT_STYLE);
			matcher = pattern.matcher(subto);
			if (!matcher.matches()) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, subto);
			}

			String prefix = matcher.group(1);
			subto = matcher.group(2);
			DistantObject slave = splitSubDistantElement(sock, prefix, online);
			// 以链表迭代形式绑定SUBDISTANT对象
			object.attach(slave);
		}

		// 检查DISTANT/SUBDISTANT阶段的关系链
		checkDistantLink(object);

		// 返回DISTANT对象
		return object;
	}

	/**
	 * 解析“NEAR“语句
	 * @param sock 基础字
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return NEAR对象
	 */
	private NearObject splitNear(Sock sock, String input, boolean online) {
		Pattern pattern = Pattern.compile(ContactParser.NEAR_SYNTAX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 解析，转换与阶段关键字冲突的转义字符
		String segment = matcher.group(1);
		segment = replace(segment);

		Phase phase = new Phase(PhaseTag.NEAR, sock);
		NearObject object = new NearObject(phase);
		while (segment.trim().length() > 0) {
			// 数据写入的本地文件名
			pattern = Pattern.compile(ContactParser.WRITEDISTANT);
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
	 * 解析平衡分布语句: "merge ......"
	 * @param sock 基础字
	 * @param input 输入语句
	 * @return MegerObject对象
	 */
	private MergeObject splitMerge(Sock sock, String input, boolean online) {
		Pattern pattern = Pattern.compile(ContactParser.MERGE_SYNTAX);
		Matcher matcher = pattern.matcher(input);
		if(!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 解析，替换与系统关键字冲突的转义字符
		String segment = matcher.group(1);
		segment = replace(segment);

		Phase phase = new Phase(PhaseTag.MERGE, sock);
		MergeObject object = new MergeObject(phase);

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
	 * 解析CONTACT命令
	 * @param input CONTACT命令的文本语句
	 * @return 返回CONTACT实例
	 */
	public Contact split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(ContactParser.PREFIX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，弹出异常
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		
		Sock sock = splitSock(matcher.group(1)); // 基础字
		String suffix = matcher.group(2); 	// 命名之后的其它参数
		Contact contact = new Contact(sock);

		// 1. 判断FORK阶段语句（可选）
		pattern = Pattern.compile(ContactParser.FORK_PREFIX);
		matcher = pattern.matcher(suffix);
		if (matcher.matches()) {
			String segment = matcher.group(1);
			suffix = matcher.group(2);
			contact.setForkObject(splitFork(sock, segment, online));
		}

		// 2. 判断DISTANT阶段语句（必有！）
		pattern = Pattern.compile(ContactParser.DISTANT_PREFIX);
		matcher = pattern.matcher(suffix);
		if (!matcher.matches()) {
			throwableNo(FaultTip.PARAM_MISSING_X, "DISTANT");
		}
		String segment = matcher.group(1);
		suffix = matcher.group(2);
		DistantObject toObject = splitDistant(sock, segment, online);
		contact.setDistantObject(toObject);

		// 3. 判断MERGE阶段语句（可选）
		pattern = Pattern.compile(ContactParser.MERGE_PREFIX);
		matcher = pattern.matcher(suffix);
		if (matcher.matches()) {
			segment = matcher.group(1);
			suffix = matcher.group(2);
			contact.setMergeObject(splitMerge(sock, segment, online));
		}
		// 4. 判断有NEAR阶段语句（可选）
		if (suffix.trim().length() > 0) {
			contact.setNearObject(splitNear(sock, suffix, online));
		}

		// 生成一个默认的初始化阶段命名
		if (contact.getForkObject() == null) {
			Phase phase = new Phase(PhaseTag.FORK, sock);
			contact.setForkObject(new ForkObject(phase));
		}
		
		// 如果是在线模式，检查本地阶段命名和操作权限！
		if (online) {
			Siger issuer = getResourceChooser().getOwner();
			Phase phase = new Phase(issuer, PhaseTag.NEAR, sock);
			// 判断存在
			boolean success = hasLocalTask(phase);
			if (!success) {
				throwableNo(FaultTip.NOTFOUND_X, phase.toString());
			}
			// 判断有执行快速计算权限
			success = canContact();
			if (!success) {
				throwable(FaultTip.PERMISSION_MISSING);
			}
		}

		// 保存CONTACT原语
		contact.setPrimitive(input);
		// 返回结果
		return contact;
	}

	//	private final static String SITES = "^\\s*(?i)SITES\\s*:\\s*(\\d+)(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";
	//	private final static String DISTANT_MODE = "^\\s*(?i)(?:MODE\\s*:\\s*)(?i)(GENERATE|EVALUATE)(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";
	//	private final static String GENERATE_TAG = "^\\s*(?i)GENERATE\\s*$";
	//	private final static String WRITEDISTANT = "^\\s*(?i)(?:WRITEDISTANT)\\s*\\:\\s*\\\"([\\p{ASCII}\\W]+?)\\\"(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";

	public static void main(String[] args) {
		String input = "CONTACT MINING FROM SITES : 12; PREFIX(STRING)='MINING' ; BEGIN(LONG)=12 ; END(LONG)=122; ZEROS(INT)=3;  GPU(BOOL)=yes ; DISTANT sites:23;  NEAR NODE(STRING)='MINE SITE'; SHA256(STRING)='CODE'; WRITEDISTANT:\"/disks/random.bin\"; ";
		ContactParser parser = new ContactParser();
		parser.split(input, false);
	}
}

///**
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// * 
// * Copyright 2009 laxcus.com. All rights reserved
// * 
// * @license Laxcus Public License (LPL)
// */
//package com.laxcus.access.parse;
//
//import java.util.regex.*;
//
//import com.laxcus.command.contact.*;
//import com.laxcus.distribute.contact.*;
//import com.laxcus.util.naming.*;
//import com.laxcus.util.tip.*;
//
///**
// * 快捷计算命令解析器。<br><br>
// * 
// * 快捷组件参数样式：<br>
// * CONTACT 命名 DISTANT [参数名称(类型)=值; 参数2; ... ATTACH ...]  NEAR [名称(类型)=数值; 名称(类型)=数值;]
// * 
// * @author scott.liang
// * @version 1.0 05/04/2020
// * @since laxcus 1.0
// */
//public class ContactParser extends DistributeParser {
//
//	/** CONTACT语法 **/
//	private final static String PREFIX = "^\\s*(?i)CONTACT\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+([\\p{ASCII}\\W]+)\\s*$";
//
//	/** DISTANT/NEAR阶段语句 **/
//	private final static String DISTANT_PREFIX = "^\\s*(?i)(DISTANT\\s+[\\w\\W]*?)(?i)(\\s*|\\s+(?:NEAR)\\s+[\\w\\W]*)$";
//
//	private final static String NEAR_PREFIX = "^\\s*(?i)(NEAR\\s+[\\w\\W]*?)(?i)(\\s*|\\s+(?:DISTANT)\\s+[\\w\\W]*)$";
//
//	/** 参数语句，过滤关键字 **/
//	private final static String DISTANT_SYNTAX = "^\\s*(?i)(?:DISTANT)(\\s+[\\w\\W]+?)\\s*$";
//
//	private final static String NEAR_SYNTAX = "^\\s*(?i)(?:NEAR)(\\s*|\\s+[\\w\\W]+)$";
//
//	// 写入磁盘
//	private final static String WRITETO = "^\\s*(?i)(?:WRITETO)\\s*\\:\\s*\\\"([\\p{ASCII}\\W]+?)\\\"\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";
//
//	/**
//	 * 构造快捷计算命令解析器
//	 */
//	public ContactParser() {
//		super();
//	}
//
//	/**
//	 * 判断是快捷计算语句
//	 * @param input 输入语句
//	 * @return 匹配返回“真”，否则“假”。
//	 */
//	public boolean matches(String input) {
//		Pattern pattern = Pattern.compile(ContactParser.PREFIX);
//		Matcher matcher = pattern.matcher(input);
//		return matcher.matches();
//	}
//
//	/**
//	 * 将与阶段关键字冲突的字符做转义处理。被转义字符左右两侧有空格，且有一个前缀符号：“\”。
//	 * 转义发生在阶段关键字被过滤之后！！！
//	 * @param input 原文
//	 * @return 转义后的字符串语句
//	 */
//	private String replace(String input) {
//		input = replace(input, "DISTANT");
//		input = replace(input, "NEAR");
//
//		return input;
//	}
//
//	/**
//	 * 解析“DISTANT”阶段语句
//	 * @param root
//	 * @param input
//	 * @param online 在线模式
//	 * @return
//	 */
//	private DistantObject splitDistant(String root, String input, boolean online) {
//		Pattern pattern = Pattern.compile(ContactParser.DISTANT_SYNTAX);
//		Matcher matcher = pattern.matcher(input);
//		if (!matcher.matches()) {
//			throwable(FaultTip.INCORRECT_SYNTAX_X, input);
//		}
//		// 解析
//		String segment = matcher.group(1);
//		// 替换与CONTACT阶段关键字冲突的转义字符
//		segment = replace(segment);
//
//		// 初始化对象
//		Phase phase = new Phase(PhaseTag.DISTANT, root);
//		DistantObject object = new DistantObject(phase);
//
//		// 解析参数
//		while (segment.trim().length() > 0) {
//			// 如果是自定义参数
//			if (isTaskParameter(segment)) {
//				segment = splitTaskParameter(object, segment, online);
//				continue;
//			}
//			throwable(FaultTip.INCORRECT_SYNTAX_X, segment);
//		}
//		return object;
//	}
//
//	/**
//	 * 解析“NEAR”阶段语句
//	 * @param root
//	 * @param input
//	 * @param online 在线模式
//	 * @return
//	 */
//	private NearObject splitNear(String root, String input, boolean online) {
//		Pattern pattern = Pattern.compile(ContactParser.NEAR_SYNTAX);
//		Matcher matcher = pattern.matcher(input);
//		if (!matcher.matches()) {
//			throwable(FaultTip.INCORRECT_SYNTAX_X, input);
//		}
//		// 解析
//		String segment = matcher.group(1);
//		// 替换与CONTACT阶段关键字冲突的转义字符
//		segment = replace(segment);
//
//		// 初始化对象
//		Phase phase = new Phase(PhaseTag.NEAR, root);
//		NearObject object = new NearObject(phase);
//
//		while (segment.trim().length() > 0) {
//			// 数据写入的本地文件名
//			pattern = Pattern.compile(ContactParser.WRITETO);
//			matcher = pattern.matcher(segment);
//			if (matcher.matches()) {
//				String writeto = matcher.group(1);
//				object.setWriteTo(writeto);
//				segment = matcher.group(2);
//				continue;
//			}
//			// 如果是自定义参数
//			if (isTaskParameter(segment)) {
//				segment = splitTaskParameter(object, segment, online);
//				continue;
//			}
//			throwable(FaultTip.INCORRECT_SYNTAX_X, segment);
//		}
//
//		return object;
//	}
//
//	/**
//	 * 解析快捷计算语句
//	 * @param input 输入语句
//	 * @param online 在线模式
//	 * @return 返回Swift命令
//	 */
//	public Contact split(String input, boolean online) {
//		Pattern pattern = Pattern.compile(ContactParser.PREFIX);
//		Matcher matcher = pattern.matcher(input);
//		// 不匹配，弹出异常
//		if (!matcher.matches()) {
//			throwable(FaultTip.INCORRECT_SYNTAX_X, input);
//		}
//
//		// 快捷组件名称
//		String root = matcher.group(1);
//		String suffix = matcher.group(2);
//
//		// 解析参数
//		Contact cmd = new Contact(root);
//
//		while (suffix.trim().length() > 0) {
//			// 1. 判断DISTANT阶段语句
//			pattern = Pattern.compile(ContactParser.DISTANT_PREFIX);
//			matcher = pattern.matcher(suffix);
//			if (matcher.matches()) {
//				String segment = matcher.group(1);
//				suffix = matcher.group(2);
//				cmd.setDistantObject(splitDistant(root, segment, online));
//				continue;
//			}
//
//			// 2. 判断NEAR阶段语句
//			pattern = Pattern.compile(ContactParser.NEAR_PREFIX);
//			matcher = pattern.matcher(suffix);
//			if (matcher.matches()) {
//				String segment = matcher.group(1);
//				suffix = matcher.group(2);
//				cmd.setNearObject(splitNear(root, segment, online));
//				continue;
//			}
//			// 弹出异常
//			throwable(FaultTip.INCORRECT_SYNTAX_X, suffix);
//		}
//
//		// 命令原语
//		cmd.setPrimitive(input);
//		return cmd;
//	}
//
//
//	//	public static void main(String[] args) {
//	//		String input = "CONTACT demo @KEY \"UNXI\", @VALUE \"中国\" ATTACH ALL be NOT SHARE and ATTACH ROW Media.Music/Video 122 be not share";
//	//		SwiftParser parser = new SwiftParser();
//	//		Swift cmd = parser.split(input, false);
//	//	}
//
//	public static void main(String[] args) {
//		String input = "CONTACT  FIRE DISTANT  PREFIX(STRING)='MINING' ; BEGIN(LONG)=12 ; END(LONG)=122; ZEROS(INT)=3;  GPU(BOOL)=yes ;   NEAR NODE(STRING)='MINE SITE'; SHA256(STRING)='CODE'; WRITETO:\"/disks/random.bin\"; ";
//		ContactParser parser = new ContactParser();
//		Contact cmd = parser.split(input, false);
//		System.out.println(cmd.getPrimitive());
//		System.out.println(cmd.getRootText());
//	}	
//
//}