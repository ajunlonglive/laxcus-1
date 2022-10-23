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

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.establish.*;
import com.laxcus.distribute.establish.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * ESTABLISH命令解析器 <br><br>
 * 
 * ESTABLISH命令用于分布环境下的数据构建，通过旧数据的处理，产生新数据。理论上，这些新数据，有助于提供数据计算业务（CONDUCT/CONTACT）的处理速度。<br><br>
 * 
 * 说明：<BR>
 * 1. 命令由“ESTABLISH 应用名称”开始，解析顺序是：ISSUE、SCAN、SIFT（SUBSIFT)、RISE、ASSIGN、END，用户输入的文本命令，应与此保持一致。<br>
 * 2. SCAN、RISE分为单个执行和多个并行执行。多个任务并行格式是：SCAN/RISE BRANCH 子命令。单个执行时，没有“BRANCH 子命名”。<br>
 * 3. SIFT设计为可以迭代执行，有迭代的格式是： SUBSIFT 子命名 [参数....] SUBSIFT 子命名 [参数...]。<br>
 * 4. ASSIGN负责解析SCAN/SIFT、SIFT/SUBSIFT、SUBSIFT/SUBSIFT、SIFT/RISE、SUBSIFT/RISE之间的元数据，和协调、分配数据。<br>
 * 
 * <br><br>
 * 
 * ESTABLISH文本命令格式：<br>
 * ESTABLISH 应用名称 <br>
 * ISSUE [系统参数和自定义参数 ...] <br>
 * SCAN [系统参数和自定义参数 ...] | SCAN BRANCH 子命名 [系统参数和自定义参数 ...] BRANCH 子命名 [系统参数和自定义参数 ...] <br>
 * SIFT [系统参数和自定义参数 ...] SUBSIFT 子命名 [系统参数和自定义参数 ...] SUBSIFT 子命名 [系统参数和自定义参数 ...] <br>
 * RISE [系统参数和自定义参数 ...] | RISE BRANCH 子命名 [系统参数和自定义参数 ...] RISE BRANCH 子命名 [系统参数和自定义参数 ...] <br>
 * ASSIGN [系统参数和自定义参数 ...] <br>
 * END [系统参数和自定义参数 ...] <br>
 * 
 * 
 * <br><br>
 * 
 * 参数分为系统参数和自定义参数，参数用ASCII分号(;)结束，自定义参数允许重复。<br><br>
 * 
 * 系统参数，键和值之间用ASCII冒号(:)分隔。当前系统参数有：<br>
 * <1> SPACE TO : [DATABASE.TABLE][, DATABASE2.TABLE2][, DATABASE3.TABLE3] <br>
 * <2> DOCK TO : [DATABASE.TABLE#COLUMN][, DATABASE.TABLE#COLUMN][, DATABASE.TABLE#COLUMN] <br>
 * <3> WRITETO : "系统路径"<br>
 * 
 * 
 * @author scott.liang
 * @version 1.2 12/9/2014
 * @since laxcus 1.0
 */
public class EstablishParser extends DistributedParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:ESTABLISH)\\s+([\\w\\W]+)\\s*$";

	/** ESTABLISH语法 **/
	private final static String PREFIX = "^\\s*(?i)(?:ESTABLISH)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\.[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+([\\p{ASCII}\\W]+)\\s*$";

	private final static String ISSUE_PREFIX = "^\\s*(?i)(ISSUE[\\p{ASCII}\\W]*?)(?i)(\\s+(?:SCAN)\\s+[\\p{ASCII}\\W]*)$";
	private final static String SCAN_PREFIX = "^\\s*(?i)(SCAN[\\p{ASCII}\\W]*?)(?i)(\\s+(?:SIFT)\\s+[\\p{ASCII}\\W]*)$";
	private final static String SIFT_PREFIX = "^\\s*(?i)(SIFT[\\p{ASCII}\\W]*?)(?i)(\\s+(?:RISE)\\s+[\\p{ASCII}\\W]*)$";
	private final static String RISE_PREFIX = "^\\s*(?i)(RISE[\\p{ASCII}\\W]*?)(?i)(\\s*|\\s+(?:ASSIGN)\\s+[\\p{ASCII}\\W]*)$";
	private final static String ASSIGN_PREFIX = "^\\s*(?i)(ASSIGN[\\p{ASCII}\\W]*?)(?i)(\\s*|\\s+(?:END)\\s+[\\p{ASCII}\\W]*)$";

	/** 多SCAN子句，采用分支处理 **/
	private final static String SCAN_BRANCH_PREFIX = "^\\s*(?i)(?:SCAN)\\s+(?i)(BRANCH\\s+[\\p{ASCII}\\W]+)$";
	private final static String SCAN_BRANCH_SPLIT = "^\\s*((?i)(?:BRANCH)\\s+[\\p{ASCII}\\W]+?)(\\s*|\\s+(?i)(?:BRANCH)\\s+[\\p{ASCII}\\W]+)$";
	private final static String SCAN_BRANCH_SEGMENT = "^\\s*(?i)(?:BRANCH)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)(\\s*?|\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+ATTACH\\s+[\\p{ASCII}\\W]+)$";
	/** 单个SCAN语句 **/
	private final static String SCAN_SINGLE = "^\\s*(?i)(?:SCAN)([\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+ATTACH\\s+[\\p{ASCII}\\W]+\\s+BE\\s+[\\p{ASCII}\\W]+)$";

	/** SIFT/SUBSIFT阶段语句 **/
	private final static String SIFT_ALL_STYLE = "^\\s*(?i)(SIFT\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+SUBSIFT\\s+[\\p{ASCII}\\W]+)$";
	private final static String SUBSIFT_STYLE = "^\\s*(?i)(SUBSIFT\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+SUBSIFT\\s+[\\p{ASCII}\\W]+)$";
	private final static String SIFT_SEGMENT = "^^\\s*(?i)(?:SIFT)(\\s*?|\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+ATTACH\\s+[\\p{ASCII}\\W]+)$";
	private final static String SUBSIFT_SEGMENT = "^\\s*(?i)(?:SUBSIFT)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)(\\s*?|\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+ATTACH\\s+[\\p{ASCII}\\W]+)$";

	/** 多RISE子句，采用分支处理 **/
	private final static String RISE_BRANCH_PREFIX = "^\\s*(?i)(?:RISE)\\s+(?i)(BRANCH\\s+[\\p{ASCII}\\W]+)$";
	private final static String RISE_BRANCH_SPLIT = "^\\s*((?i)(?:BRANCH)\\s+[\\p{ASCII}\\W]+?)(\\s*|\\s+(?i)(?:BRANCH)\\s+[\\p{ASCII}\\W]+)$";
	private final static String RISE_BRANCH_SEGMENT = "^\\s*(?i)(?:BRANCH)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)(\\s*?|\\s+[\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+ATTACH\\s+[\\p{ASCII}\\W]+)$";
	/** 单个RISE语句 **/
	private final static String RISE_SINGLE = "^\\s*(?i)(?:RISE)([\\p{ASCII}\\W]+?)(?i)(\\s*|\\s+ATTACH\\s+[\\p{ASCII}\\W]+\\s+BE\\s+[\\p{ASCII}\\W]+)$";

	/** ISSUE语句 **/
	private final static String ISSUE_SYNTAX = "^\\s*(?i)(?:ISSUE)(\\s*|\\s+[\\p{ASCII}\\W]+)$";

	/** ASSIGN语句 **/
	private final static String ASSIGN_SYNTAX = "^\\s*(?i)(?:ASSIGN)(\\s*|\\s+[\\p{ASCII}\\W]+)$";

	/** END语句 **/
	private final static String END_SYNTAX = "^\\s*(?i)(?:END)(\\s*|\\s+[\\p{ASCII}\\W]+)$";

	/** END对象的数据写入位置 */
	private final static String WRITETO = "^\\s*(?i)(?:WRITETO)\\s*\\:\\s*\\\"([\\p{ASCII}\\W]+?)\\\"\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";

	/** SCAN阶段被关联的表名 **/
	private final static String SPACETO = "^\\s*(?i)(?:SPACE\\s+TO)\\s*\\:\\s*([\\p{ASCII}\\W]+?)\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";
	
	/** SIFT阶段被绑定的列空间 **/
	private final static String DOCKTO = "\\s*(?i)(?:DOCK\\s+TO)\\s*\\:\\s*([\\p{ASCII}\\W]+?)\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";
	
	/** 列空间名称 **/
	private final static String DOCK = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]{1,20}\\.[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]{1,20})(?:\\s+|\\#)([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]{1,20})\\s*$";

	/**
	 * 构造ESTABLISH命令解析器
	 */
	public EstablishParser() {
		super();
	}

	/**
	 * 将阶段语句中，与阶段关键字冲突的转义字符还原
	 * @param input 阶段原语
	 * @return 还原后的语句
	 */
	private String replace(String input) {
		input = replace(input, "ISSUE");
		input = replace(input, "SCAN");
		input = replace(input, "SIFT");
		input = replace(input, "SUBSIFT");
		input = replace(input, "RISE");
		input = replace(input, "ASSIGN");
		input = replace(input, "END");

		return input;
	}

	/**
	 * 检查传入的参数是否匹配"establish"语句
	 * @param input 输入语句
	 * @return 匹配返回真，否则假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("ESTABLISH", input);
		}
		Pattern pattern = Pattern.compile(EstablishParser.PREFIX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“END”语句
	 * @param root ESTABLISH应用名称
	 * @param input END阶段语句
	 * @return
	 */
	private EndObject splitEnd(Sock root, String input, boolean online) {
		Pattern pattern = Pattern.compile(EstablishParser.END_SYNTAX);
		Matcher matcher = pattern.matcher(input);
		if(!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		// 解析
		String segment = matcher.group(1);
		segment = replace(segment);

		Phase phase = new Phase(PhaseTag.END, root);
		EndObject object = new EndObject(phase);
		while (segment.trim().length() > 0) {
			// 数据写入的本地文件名
			pattern = Pattern.compile(EstablishParser.WRITETO);
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
			throwableNo(FaultTip.NOTRESOLVE_X, segment);
		}
		return object;
	}

	/**
	 * 解析单个RISE阶段语句
	 * @param rise RISE对象
	 * @param input RISE单语句
	 * @param online 在线模式
	 * @return 成功返回真，否则假
	 */
	private boolean splitRiseSingle(RiseObject rise, String input, boolean online) {
		// 单行记录
		Pattern pattern = Pattern.compile(EstablishParser.RISE_SINGLE);
		Matcher matcher = pattern.matcher(input);
		// 不匹配返回假
		if (!matcher.matches()) {
			return false;
		}

		// 参数域，过滤还原转义字符
		String segment = matcher.group(1);
		segment = replace(segment);
		// 事务规则
		String transaction = matcher.group(2);

		RiseInputter inputter = new RiseInputter(rise.getPhase());

		while (segment.trim().length() > 0) {
			// 自定义参数
			if (isTaskParameter(segment)) {
				segment = splitTaskParameter(inputter, segment, online);
				continue;
			}

			throwableNo(FaultTip.NOTRESOLVE_X, segment);
		}

		if (transaction.trim().length() > 0) {
			List<RuleItem> rules = splitRules(transaction, online);
			inputter.addRules(rules);
		}

		rise.addInputter(inputter);

		return true;
	}

	/**
	 * 解析“BRANCH 命名 [内容] [ATTACH ...]”语句段
	 * @param root
	 * @param input
	 * @param online 在线模式
	 * @return
	 */
	private RiseInputter splitRiseBranchElement(Sock root, String input, boolean online) {
		Pattern pattern = Pattern.compile(EstablishParser.RISE_BRANCH_SEGMENT);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		// RISE阶段子命名
		String sub = matcher.group(1);
		// 内容
		String segment = matcher.group(2);
		// 事务规则
		String transaction = matcher.group(3);

		Phase phase = new Phase(PhaseTag.RISE, root, sub);
		RiseInputter inputter = new RiseInputter(phase);

		while (segment.trim().length() > 0) {
			// 自定义参数
			if (isTaskParameter(segment)) {
				segment = splitTaskParameter(inputter, segment, online);
				continue;
			}

			throwableNo(FaultTip.NOTRESOLVE_X, segment);
		}

		// 解析事务
		if (transaction.trim().length() > 0) {
			List<RuleItem> rules = splitRules(transaction, online);
			inputter.addRules(rules);
		}

		return inputter;
	}

	/**
	 * 解析RISE分支处理语句
	 * @param rise RISE对象
	 * @param con RISE阶段语句
	 * @param online 在线模式
	 * @return 成功返回真，否则假
	 */
	private boolean splitRiseBranch(RiseObject rise, String input, boolean online) {
		// 多语句平行分布
		Pattern pattern = Pattern.compile(EstablishParser.RISE_BRANCH_PREFIX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return false;
		}

//		String root = rise.getPhase().getRootText();
		Sock root = rise.getPhase().getSock();

		String suffix = matcher.group(1);
		while (suffix.trim().length() > 0) {
			pattern = Pattern.compile(EstablishParser.RISE_BRANCH_SPLIT);
			matcher = pattern.matcher(suffix);
			if (!matcher.matches()) {
				throwableNo(FaultTip.NOTRESOLVE_X, suffix);
			}

			// 替换掉与系统关键字可能存在冲突的转义字符
			String segment = matcher.group(1);
			segment = replace(segment);

			suffix = matcher.group(2);

			// RISE分支输入语句
			RiseInputter inputter = splitRiseBranchElement(root, segment, online);
			rise.addInputter(inputter);
		}

		return true;
	}

	/**
	 * 解析RISE阶段语句，有两种格式：<br>
	 * 1. 多要求分支处理语句：RISE BRANCH 命名 [参数] [ATTACH ... BE ... ]  BRANCH 命名 [参数] [ATTACH ... BE ... ] <br> 
	 * 2. 单要求语句：RISE [参数] [ATTACH ... BE ...] <br>
	 * 
	 * @param root 应用名称 
	 * @param input RISE语句
	 * @param online 在线模式
	 * @return RISE对象
	 */
	private RiseObject splitRise(Sock root, String input, boolean online) {
		Phase phase = new Phase(PhaseTag.RISE, root);
		RiseObject rise = new RiseObject(phase);

		// 判断是“RISE BRANCH”分支语句
		boolean success = splitRiseBranch(rise, input, online);
		// 判断是RISE单一语句
		if (!success) {
			success = splitRiseSingle(rise, input, online);
		}
		// 以上不成功弹出错误
		if (!success) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		return rise;
	}

	/**
	 * 解析“ASSIGN”语句
	 * @param root ESTAB阶段应用名称
	 * @param input ASSIGN阶段语句
	 * @return
	 */
	private AssignObject splitAssign(Sock root, String input, boolean online) {
		Pattern pattern = Pattern.compile(EstablishParser.ASSIGN_SYNTAX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}
		// 解析
		String segment = matcher.group(1);
		segment = replace(segment);

		// 初始化对象
		Phase phase = new Phase(PhaseTag.ASSIGN, root);
		AssignObject object = new AssignObject(phase);
		// 解析参数
		while (segment.trim().length() > 0) {
			// 如果是自定义参数
			if(isTaskParameter(segment)) {
				segment = splitTaskParameter(object, segment, online);
				continue;
			}
			throwableNo(FaultTip.NOTRESOLVE_X, segment);		
		}
		return object;
	}

	/**
	 * 解析"ISSUE"阶段语句
	 * @param root ESTABLSIH应用名称
	 * @param input ISSUE阶段语句
	 * @return
	 */
	private IssueObject splitIssue(Sock root, String input, boolean online) {
		Pattern pattern = Pattern.compile(EstablishParser.ISSUE_SYNTAX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}
		// 解析，还原转义关键字
		String segment = matcher.group(1);
		segment = replace(segment);

		// 初始化对象
		Phase phase = new Phase(PhaseTag.ISSUE, root);
		IssueObject object = new IssueObject(phase);
		// 解析参数
		while (segment.trim().length() > 0) {
			// 如果是自定义参数
			if(isTaskParameter(segment)) {
				segment = splitTaskParameter(object, segment, online);
				continue;
			}
			throwableNo(FaultTip.NOTRESOLVE_X, segment);			
		}
		return object;
	}

	/**
	 * 解析单个SCAN阶段语句
	 * @param scan SCAN对象
	 * @param input SCAN单语句
	 * @param online 在线模式
	 * @return 成功返回真，否则假
	 */
	private boolean splitScanSingle(ScanObject scan, String input, boolean online) {
		// 单行记录
		Pattern pattern = Pattern.compile(EstablishParser.SCAN_SINGLE);
		Matcher matcher = pattern.matcher(input);
		// 不匹配返回假
		if (!matcher.matches()) {
			return false;
		}

		// 参数域，过滤还原转义字符
		String segment = matcher.group(1);
		segment = replace(segment);
		// 事务规则
		String transaction = matcher.group(2);

		ScanInputter inputter = new ScanInputter(scan.getPhase());

		while (segment.trim().length() > 0) {
			// 1. 判断是表空间
			pattern = Pattern.compile(EstablishParser.SPACETO);
			matcher = pattern.matcher(segment);
			if (matcher.matches()) {
				// 取出表名集合
				String line = matcher.group(1);
				// 下一段参数
				segment = matcher.group(2);
				// 解析和保存表名
				inputter.addSpaces(splitSpaces(line, online));

				continue;
			}			
			// 自定义参数
			if (isTaskParameter(segment)) {
				segment = splitTaskParameter(inputter, segment, online);
				continue;
			}

			throwableNo(FaultTip.NOTRESOLVE_X, segment);
		}

		if (transaction.trim().length() > 0) {
			List<RuleItem> rules = splitRules(transaction, online);
			inputter.addRules(rules);
		}

		scan.addInputter(inputter);

		return true;
	}

	/**
	 * 解析“BRANCH 命名 [内容] [ATTACH ...]”语句段
	 * @param root 应用名称
	 * @param input BRANCH阶段语句
	 * @param online 在线模式
	 * @return 返回ScanInputter实例
	 */
	private ScanInputter splitScanBranchElement(Sock root, String input, boolean online) {
		Pattern pattern = Pattern.compile(EstablishParser.SCAN_BRANCH_SEGMENT);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		// SCAN阶段子命名
		String sub = matcher.group(1);
		// 内容
		String segment = matcher.group(2);
		// 事务规则
		String transaction = matcher.group(3);

		Phase phase = new Phase(PhaseTag.SCAN, root, sub);
		ScanInputter inputter = new ScanInputter(phase);

		while (segment.trim().length() > 0) {
			// 判断是锁定表
			pattern = Pattern.compile(EstablishParser.SPACETO);
			matcher = pattern.matcher(segment);
			if (matcher.matches()) {
				String line = matcher.group(1);
				segment = matcher.group(2);
				inputter.addSpaces(splitSpaces(line, online));
				continue;
			}
			// 自定义参数
			if (isTaskParameter(segment)) {
				segment = splitTaskParameter(inputter, segment, online);
				continue;
			}

			throwableNo(FaultTip.NOTRESOLVE_X, segment);
		}

		// 解析事务
		if (transaction.trim().length() > 0) {
			List<RuleItem> rules = splitRules(transaction, online);
			inputter.addRules(rules);
		}

		return inputter;
	}

	/**
	 * 解析SCAN分支处理语句
	 * @param scan SCAN对象
	 * @param con SCAN阶段语句
	 * @param online 在线模式
	 * @return 成功返回真，否则假
	 */
	private boolean splitScanBranch(ScanObject scan, String input, boolean online) {
		// 判断是"SCAN BRANCH"的多分支处理
		Pattern pattern = Pattern.compile(EstablishParser.SCAN_BRANCH_PREFIX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return false;
		}

//		String root = scan.getPhase().getRootText();
		
		Sock root= scan.getPhase().getSock();

		String suffix = matcher.group(1);
				
		while (suffix.trim().length() > 0) {
			pattern = Pattern.compile(EstablishParser.SCAN_BRANCH_SPLIT);
			matcher = pattern.matcher(suffix);
			if (!matcher.matches()) {
				throwableNo(FaultTip.NOTRESOLVE_X, suffix);
			}

			// 替换掉与系统关键字可能存在冲突的转义字符
			String segment = matcher.group(1);
			segment = replace(segment);

			suffix = matcher.group(2);
			
			// SCAN分支输入语句
			ScanInputter inputter = splitScanBranchElement(root, segment, online);
			scan.addInputter(inputter);
		}

		return true;
	}

	/**
	 * 解析SCAN阶段语句，有两种格式：<br>
	 * 1. 多要求分支处理语句：SCAN BRANCH 命名 [参数] [ATTACH ... BE ... ]  BRANCH 命名 [参数] [ATTACH ... BE ... ] <br> 
	 * 2. 单要求语句：SCAN [参数] [ATTACH ... BE ...] <br>
	 * 
	 * @param root 应用名称 
	 * @param input SCAN语句
	 * @param online 在线模式
	 * @return SCAN对象
	 */
	private ScanObject splitScan(Sock root, String input, boolean online) {
		Phase phase = new Phase(PhaseTag.SCAN, root);
		ScanObject object = new ScanObject(phase);
		
		// 判断是“SCAN BRANCH”分支语句
		boolean success = splitScanBranch(object, input, online);
		// 判断是SCAN单一语句
		if (!success) {
			success = splitScanSingle(object, input, online);
		}
		// 以上不成功弹出错误
		if (!success) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		return object;
	}

	/**
	 * 解析SIFT/SUBSIFT阶段参数，保存到SIFT阶段输入器
	 * @param phase 阶段命名
	 * @param segment 参数段
	 * @param transaction 事务
	 * @return SiftInputter
	 */
	private SiftInputter splitSiftInputter(Phase phase, String segment, String transaction, boolean online) {
		// 生成SIFT阶段数据输入器
		SiftInputter inputter = new SiftInputter(phase);

		// 解析"SIFT/SUBSIFT"参数
		while (segment.trim().length() > 0) {
			// 列空间
			Pattern pattern = Pattern.compile(EstablishParser.DOCKTO);
			Matcher matcher = pattern.matcher(segment);
			if (matcher.matches()) {
				String line = matcher.group(1);
				// 解析列空间和保存它
				inputter.addDocks(splitDocks(line));
				// 剩余参数
				segment = matcher.group(2);
				continue;
			}

			// 如果是自定义参数
			if (isTaskParameter(segment)) {
				segment = splitTaskParameter(inputter, segment, online);
				continue;
			}
			throwableNo(FaultTip.NOTRESOLVE_X, segment);
		}

		// 事务
		if (transaction.trim().length() > 0) {
			List<RuleItem> rules = splitRules(transaction, online);
			inputter.addRules(rules);
		}

		return inputter;
	}

	/**
	 * 解析列空间名称
	 * @param input 输入语句
	 * @return Dock列表
	 */
	private List<Dock> splitDocks(String input) {
		ArrayList<Dock> array = new ArrayList<Dock>();

		// 列空间名称用逗号分隔
		String[] items = input.split("\\s*,\\s*");
		for (String item : items) {
			Pattern pattern = Pattern.compile(EstablishParser.DOCK);
			Matcher matcher = pattern.matcher(item);
			// 不匹配是错误
			if (!matcher.matches()) {
				throwableNo(FaultTip.NOTRESOLVE_X, item);
			}
			// 表名
			String spaceName = matcher.group(1);
			// 列名
			String columnaName = matcher.group(2);
			// 判断表名有效
			if (!Space.validate(spaceName)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, item);
			}
			Space space = new Space(spaceName);
			ResourceChooser chooser = SyntaxParser.getResourceChooser();
			Table table = chooser.findTable(space);
			// 如果表不存在弹出异常
			if (table == null) {
				throwableNo(FaultTip.NOTFOUND_X, space);
			}
			ColumnAttribute attribute = table.find(columnaName);
			if (attribute == null) {
				String e = String.format("%s %s", space, columnaName);
				throwableNo(FaultTip.NOTFOUND_X, e);
			}
			Dock dock = new Dock(space, attribute.getColumnId());
			array.add(dock);
		}
		return array;
	}

	/**
	 * 解析SIFT根阶段语句
	 * @param root 应用名称
	 * @param input SIFT语句
	 * @param online 在线模式
	 * @return SiftObject
	 */
	private SiftObject splitSiftElement(Sock root, String input, boolean online) {
		Pattern pattern = Pattern.compile(EstablishParser.SIFT_SEGMENT);
		Matcher matcher = pattern.matcher(input);
		// 不成功是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		// SIFT阶段命名
		Phase phase = new Phase(PhaseTag.SIFT, root);

		// 参数域。与阶段关键字冲突的字符转义
		String segment = matcher.group(1);
		segment = replace(segment);
		// 事务规则
		String transaction = matcher.group(2);

		// 生成SIFT阶段输入器
		SiftInputter inputter = splitSiftInputter(phase, segment, transaction, online);

		SiftObject object = new SiftObject(phase);
		object.setInputter(inputter);

		return object;
	}

	/**
	 * 解析SUBSIFT语句
	 * @param root 应用名称
	 * @param input SUBSIFT语句
	 * @param online 在线模式
	 * @return SiftObject
	 */
	private SiftObject splitSubtoElement(Sock root, String input, boolean online) {
		// 解析SUBSIFT阶段语句
		Pattern pattern = Pattern.compile(EstablishParser.SUBSIFT_SEGMENT);
		Matcher matcher = pattern.matcher(input);
		// 不成功是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		// 子命名
		String sub = matcher.group(1);
		// 参数域
		String segment = matcher.group(2);
		segment = replace(segment);
		// 事务规则
		String transaction = matcher.group(3);

		// SUBSIFT命名
		Phase phase = new Phase(PhaseTag.SIFT, root, sub);
		SiftInputter inputter = splitSiftInputter(phase, segment, transaction, online);
		// 生成SIFT子级对象
		SiftObject slave = new SiftObject(phase);
		slave.setInputter(inputter);
		// 返回SUBSIFT对象
		return slave;
	}

	/**
	 * 解析SIFT阶段对象参数，格式：<br>
	 * SIFT [参数] [ATTACH ...] SUBSIFT 子命名 [参数] [ATTACH ...] SUBSIFT 子命名 [参数] [ATTACH ...]
	 * 
	 * @param root 应用名称
	 * @param input SIFT阶段语句
	 * @param online 在线模式
	 * @return SiftObject对象
	 */
	private SiftObject splitSift(Sock root, String input, boolean online) {
		Pattern pattern = Pattern.compile(EstablishParser.SIFT_ALL_STYLE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		String sift = matcher.group(1);
		String subsift = matcher.group(2);

		// 解析SIFT语句
		SiftObject object = splitSiftElement(root, sift, online);

		// 拆开SUBSIFT语句，逐一解析每个SUBSIFT语句
		while (subsift.trim().length() > 0) {
			pattern = Pattern.compile(EstablishParser.SUBSIFT_STYLE);
			matcher = pattern.matcher(subsift);
			if (!matcher.matches()) {
				throwableNo(FaultTip.NOTRESOLVE_X, subsift);
			}

			String prefix = matcher.group(1);
			subsift = matcher.group(2);
			SiftObject slave = splitSubtoElement(root, prefix, online);
			// 以链表迭代形式绑定SUBSIFT对象
			object.attach(slave);
		}

		// 返回SIFT对象
		return object;
	}

	/**
	 * 解析ESTABLISH命令语句，返回ESTABLISH命令实例。
	 * @param input ESTABLISH字符串命令
	 * @param online 在线模式
	 * @return 返回ESTABLISH命令实例
	 */
	public Establish split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(EstablishParser.PREFIX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		Sock root = splitSock(matcher.group(1)); // 应用名称
		String suffix = matcher.group(2); // 命名之后的其它参数

		// 建立命令实例，解析各阶段参数
		Establish estab = new Establish(root);

		// 1. ISSUE阶段，可选
		pattern = Pattern.compile(EstablishParser.ISSUE_PREFIX);
		matcher = pattern.matcher(suffix);
		if (matcher.matches()) {
			String segment = matcher.group(1);
			suffix = matcher.group(2);
			estab.setIssueObject(splitIssue(root, segment, online));
		}
		// 2. SCAN阶段，必选
		pattern = Pattern.compile(EstablishParser.SCAN_PREFIX);
		matcher = pattern.matcher(suffix);
		if (!matcher.matches()) {
			throwableNo(FaultTip.PARAM_MISSING_X, "SCAN");
		}
		String segment = matcher.group(1);
		suffix = matcher.group(2);
		estab.setScanObject(splitScan(root, segment, online));
		// 3. SIFT阶段，必选
		pattern = Pattern.compile(EstablishParser.SIFT_PREFIX);
		matcher = pattern.matcher(suffix);
		if (!matcher.matches()) {
			throwableNo(FaultTip.PARAM_MISSING_X, "SIFT");
		}
		segment = matcher.group(1);
		suffix = matcher.group(2);
		estab.setSiftObject(splitSift(root, segment, online));
		// 4. RISE阶段，必选
		pattern = Pattern.compile(EstablishParser.RISE_PREFIX);
		matcher = pattern.matcher(suffix);
		if (!matcher.matches()) {
			throwableNo(FaultTip.PARAM_MISSING_X, "RISE");
		}
		segment = matcher.group(1);
		suffix = matcher.group(2);
		estab.setRiseObject(splitRise(root, segment, online));
		// 5. ASSIGN阶段，可选
		pattern = Pattern.compile(EstablishParser.ASSIGN_PREFIX);
		matcher = pattern.matcher(suffix);
		if (matcher.matches()) {
			segment = matcher.group(1);
			suffix = matcher.group(2);
			estab.setAssignObject(splitAssign(root, segment, online));
		}
		// 6. END阶段，可选
		if (suffix.trim().length() > 0) {
			estab.setEndObject(splitEnd(root, suffix, online));
		}

		// 如果是在线模式，检查本地阶段命名和操作权限！
		if (online) {
			Siger issuer = getResourceChooser().getOwner();
			Phase phase = new Phase(issuer, PhaseTag.END, root);
			// 判断存在
			boolean success = hasLocalTask(phase);
			if (!success) {
				throwableNo(FaultTip.NOTFOUND_X, phase.toString());
			}
			// 判断有执行快速计算权限
			success = canEstablish();
			if (!success) {
				throwable(FaultTip.PERMISSION_MISSING);
			}
		}

		// 保留原语
		estab.setPrimitive(input);
		return estab;
	}

//	private final static String WRITETO = "^\\s*(?i)(?:WRITETO)\\s*\\:\\s*\\\"([\\p{ASCII}\\W]+?)\\\"(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";
//	private final static String SPACETO = "^\\s*(?i)(?:SPACE\\s+TO)\\s*\\:\\s*([\\p{ASCII}\\W]+?)(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";
//	private final static String DOCKTO = "\\s*(?i)(?:DOCK\\s+TO)\\s*\\:\\s*([\\p{ASCII}\\W]+?)(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";
//	private final static String DOCK = "^\\s*(\\w{1,32}+\\.\\w{1,32}+)\\s+(\\w{1,32}+)(\\s*|\\s*\\;[\\p{ASCII}\\W]+)$";

//	// 过滤参数分隔符(分号)
//	if (i > 0) {
//		if (isSeparator(segment)) break; // 如果只有一个分号分隔符，将退出！
//		segment = filteSeparator(segment);
//	}
}