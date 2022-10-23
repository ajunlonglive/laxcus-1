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

import com.laxcus.access.schema.*;
import com.laxcus.access.util.*;
import com.laxcus.distribute.*;
import com.laxcus.distribute.parameter.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 分布式计算语法解析器。适用于CONDUCT/ESTABLISH命令解析。<br><br>
 * 
 * 关于基础字的名称解释：<br>
 * 1. 软件名称具有唯一性 <br>
 * 2. 组件名称在一个软件包中存在任意多个 <br><br>
 * 
 * 事务语句格式：<br>
 * 1. ATTACH [ALL|DATABASE|TABLE] [数据库名或者表名] BE [SHARE|NOT SHARE] <br>
 * 2. ATTACH [ALL|DATABASE|TABLE] [数据库名或者表名] BE [SHARE|NOT SHARE] AND ATTAH ... <br>
 * 
 * @author scott.liang
 * @version 1.1 11/9/2013
 * @since laxcus 1.0
 */
class DistributedParser extends RuleParser {
	
	/** 基础字， 只有软件包名称和根名称，软件名称限制16个字符 **/
	private final static String SOCK_PREFIX = "^\\s*(?i)([\\w\\W]{1,16}?)\\.([\\w\\W]+?)\\s*$";
	
	/** 基础字，判断是长度溢出 **/
	private final static String SOCK_UNLIMIT = "^\\s*(?i)([\\w\\W]+?)\\.([\\w\\W]+?)\\s*$";

	/** 自定义参数 **/
	private final static String CHECK_PARAM = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(RAW|BINARY|BOOLEAN|BOOL|CHAR|STRING|DATE|TIME|DATETIME|TIMESTAMP|SMALLINT|SHORT|INT|LONG|BIGINT|FLOAT|REAL|DOUBLE|COMMAND)\\s*\\)\\s*=([\\p{ASCII}\\W]+)$";

	/** 参数格式 **/
	private final static String PARAM_BOOLEAN = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(?:BOOLEAN|BOOL)\\s*\\)\\s*=\\s*(?i)(TRUE|FALSE|YES|NO)\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";
	private final static String PARAM_CONSTS = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(SMALLINT|SHORT|INT|BIGINT|LONG)\\s*\\)\\s*=\\s*([-]{0,1}[0-9]+)\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";
	private final static String PARAM_FLOAT = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(FLOAT|REAL|DOUBLE)\\s*\\)\\s*=\\s*([-]{0,1}[0-9]{1,}[.]{0,1}[0-9]{1,})\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";
	private final static String PARAM_RAW = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(?:RAW|BINARY)\\s*\\)\\s*=\\s*(?i)0X([0-9a-fA-F]+)\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";
	private final static String PARAM_STRING = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(CHAR|STRING|DATE|TIME|DATETIME|TIMESTAMP)\\s*\\)\\s*=\\s*\\'([\\p{ASCII}\\W]+?)\\'\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";
	private final static String PARAM_COMMAND = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(?:COMMAND)\\s*\\)\\s*=\\s*\\\"([\\p{ASCII}\\W]+?)\\\"\\s*(?:\\;)([\\p{ASCII}\\W]+|\\s*)$";

	/**
	 * 构造分布式计算法语解析器
	 */
	protected DistributedParser() {
		super();
	}
	
	/**
	 * 解析任务应用名称，包括软件名和组件名
	 * @param input 输入语句
	 * @return 返回Sock
	 * @throws SyntaxException 如果语句错误时...
	 */
	protected Sock splitSock(String input) {
		Pattern pattern = Pattern.compile(DistributedParser.SOCK_PREFIX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			pattern = Pattern.compile(DistributedParser.SOCK_UNLIMIT);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				throwableNo(FaultTip.NOTSUPPORT_X, input);
			} else {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
			}
		}
		// 软件名称和组件名称
		String ware = matcher.group(1);
		String root = matcher.group(2);
		return new Sock(ware, root);
	}

	/**
	 * 拆解表名
	 * @param input 输入参数
	 * @param online 在线模式
	 * @return 返回数据表集合
	 */
	protected List<Space> splitSpaces(String input, boolean online) {
		String[] items = input.split("\\s*,\\s*");
		ArrayList<Space> array = new ArrayList<Space>();
		for (String item : items) {
			// 判断合法
			if (!Space.validate(item)) {
				throwable(message(FaultTip.INCORRECT_SYNTAX_X, item));
			}

			// 表名
			Space space = new Space(item);
			if (online) {
				if (!hasTable(space)) {
					throwableNo(FaultTip.NOTFOUND_X, space);
				}
			}

			// 保存表名
			array.add(space);
		}
		return array;
	}

	/**
	 * 判断是自定义参数
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	protected boolean isTaskParameter(String input) {
		Pattern pattern = Pattern.compile(DistributedParser.CHECK_PARAM);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析用户自定义参数
	 * 参数格式：“键名(数据类型)=值”，如：“K1(float)=129.33, K2(char)='helo\'key' , K2(RAW)=0x234444”
	 * "title(type)=value1, title(type)='value2', title(type)=0xvalue3"
	 * 
	 * @param object 存取对象实例
	 * @param input
	 * @param online 在线模式
	 * @return 返回剩余的字节串
	 */
	protected String splitTaskParameter(AccessObject object, String input, boolean online) {
		// 解析布尔参数
		Pattern pattern = Pattern.compile(DistributedParser.PARAM_BOOLEAN);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String title = matcher.group(1);
			String value = matcher.group(2);
			object.addParameter(new TaskBoolean(title, value.matches("^\\s*(?i)(TRUE|YES)\\s*$")));
			return matcher.group(3);
		}
		// 解析整型值
		pattern = Pattern.compile(DistributedParser.PARAM_CONSTS);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String title = matcher.group(1);
			String family = matcher.group(2);
			String value = matcher.group(3);
			if (family.matches("^\\s*(?i)(SHORT|SMALLINT)\\s*$")) {
				object.addParameter(new TaskShort(title, java.lang.Short.parseShort(value)));
			} else if (family.matches("^\\s*(?i)(INT)\\s*$")) {
				object.addParameter(new TaskInteger(title, java.lang.Integer.parseInt(value)));
			} else if (family.matches("^\\s*(?i)(BIGINT|LONG)\\s*$")) {
				object.addParameter(new TaskLong(title, java.lang.Long.parseLong(value)));
			}
			return matcher.group(4);
		}
		// 解析浮点值
		pattern = Pattern.compile(DistributedParser.PARAM_FLOAT);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String title = matcher.group(1);
			String family = matcher.group(2);
			String value = matcher.group(3);
			if (family.matches("^\\s*(?i)(FLOAT|REAL)\\s*$")) {
				object.addParameter(new TaskFloat(title, java.lang.Float.parseFloat(value)));
			} else if (family.matches("^\\s*(?i)(DOUBLE)\\s*$")) {
				object.addParameter(new TaskDouble(title, java.lang.Double.parseDouble(value)));
			}
			return matcher.group(4);
		}
		// 解析二进制变量值
		pattern = Pattern.compile(DistributedParser.PARAM_RAW);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String title = matcher.group(1);
			String value = matcher.group(2);
			byte[] b = VariableGenerator.htob(value);
			object.addParameter(new TaskRaw(title, b));	
			return matcher.group(3);
		}
		// 解析字符串值(字符，日期，时间，时间戳)
		pattern = Pattern.compile(DistributedParser.PARAM_STRING);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String title = matcher.group(1);
			String family = matcher.group(2);
			String value = matcher.group(3);

			if (family.matches("^\\s*(?i)(CHAR|STRING)\\s*$")) {
				value = value.replace("\\'", "\'"); // 替换转义字符
				object.addParameter(new TaskString(title, value));
			} else if (family.matches("^\\s*(?i)(DATE)\\s*$")) {
				int date = CalendarGenerator.splitDate(value);
				object.addParameter(new TaskDate(title, date));
			} else if (family.matches("^\\s*(?i)(TIME)\\s*$")) {
				int time = CalendarGenerator.splitTime(value);
				object.addParameter(new TaskTime(title, time));
			} else if (family.matches("^\\s*(?i)(DATETIME|TIMESTAMP)\\s*$")) {
				long timestamp = CalendarGenerator.splitTimestamp(value);
				object.addParameter(new TaskTimestamp(title, timestamp));
			}
			return matcher.group(4);
		}
		// 解析命令
		pattern = Pattern.compile(DistributedParser.PARAM_COMMAND);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String title = matcher.group(1);
			String value = matcher.group(2);

			// 将存在转义的单引号、双引号、逗号、分号还原
			value = value.replace("\\\"", "\"");
			value = value.replace("\\'", "'");
			value = value.replace("\\,", ",");
			value = value.replace("\\;", ";");

			// 解析命令
			TaskCommandParser parser = new TaskCommandParser();
			TaskCommand cmd = parser.split(value, online);
			cmd.setName(title);

			// 保存参数
			object.addParameter(cmd);

			// 返回剩余参数
			return matcher.group(3);
		}

//		// 语法错误
//		throwable(FaultTip.INCORRECT_SYNTAX_X, input);

		 // 语法错误
		 String msg = fault(FaultTip.INCORRECT_SYNTAX_X, input);
		 throw new SyntaxException(msg);
	}

	//	private final static String SEPARATOR = "^\\s*(?:\\;)\\s*$";
	//	private final static String FILTE_SEPARATOR = "^\\s*(?:\\;)\\s*([\\p{ASCII}\\W]+)$";

	//	private final static String PARAM_BOOLEAN = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(?:BOOLEAN|BOOL)\\s*\\)\\s*=\\s*(?i)(TRUE|FALSE|YES|NO)(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";
	//	private final static String PARAM_CONSTS = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(SMALLINT|SHORT|INT|BIGINT|LONG)\\s*\\)\\s*=\\s*([-]{0,1}[0-9]+)(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";
	//	private final static String PARAM_FLOAT = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(FLOAT|REAL|DOUBLE)\\s*\\)\\s*=\\s*([-]{0,1}[0-9]{1,}[.]{0,1}[0-9]{1,})(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";
	//	private final static String PARAM_RAW = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(?:RAW|BINARY)\\s*\\)\\s*=\\s*(?i)0X([0-9a-fA-F]+)(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";
	//	private final static String PARAM_STRING = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(CHAR|STRING|DATE|TIME|DATETIME|TIMESTAMP)\\s*\\)\\s*=\\s*\\'([\\p{ASCII}\\W]+?)\\'(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";
	//	private final static String PARAM_COMMAND = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(?:COMMAND)\\s*\\)\\s*=\\s*\\\"([\\p{ASCII}\\W]+?)\\\"(\\s*\\;[\\p{ASCII}\\W]+|\\s*\\;)\\s*$";

	//	private final static String PARAM_BOOLEAN = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(?:BOOLEAN|BOOL)\\s*\\)\\s*=\\s*(?i)(TRUE|FALSE|YES|NO)(\\s*\\;[\\p{ASCII}\\W]+|\\s*)$";
	//	private final static String PARAM_CONSTS = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(SMALLINT|SHORT|INT|BIGINT|LONG)\\s*\\)\\s*=\\s*([-]{0,1}[0-9]+)(\\s*\\;[\\p{ASCII}\\W]+|\\s*)$";
	//	private final static String PARAM_FLOAT = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(FLOAT|REAL|DOUBLE)\\s*\\)\\s*=\\s*([-]{0,1}[0-9]{1,}[.]{0,1}[0-9]{1,})(\\s*\\;[\\p{ASCII}\\W]+|\\s*)$";
	//	private final static String PARAM_RAW = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(?:RAW|BINARY)\\s*\\)\\s*=\\s*(?i)0X([0-9a-fA-F]+)(\\s*\\;[\\p{ASCII}\\W]+|\\s*)$";
	//	private final static String PARAM_STRING = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(CHAR|STRING|DATE|TIME|DATETIME|TIMESTAMP)\\s*\\)\\s*=\\s*\\'([\\p{ASCII}\\W]+?)\\'(\\s*\\;[\\p{ASCII}\\W]+|\\s*)$";
	//	private final static String PARAM_COMMAND = "^\\s*([\\w\\-]+)\\s*\\(\\s*(?i)(?:COMMAND)\\s*\\)\\s*=\\s*\\\"([\\p{ASCII}\\W]+?)\\\"(\\s*|\\s*\\;[\\p{ASCII}\\W]+)$";

	//	/** 系统的事务规则 **/
	//	private final static String RULE_PREFIX = "^\\s*(?i)(?:AND)\\s+(?i)(ATTACH\\s+[\\p{ASCII}\\W]+)$";
	//	private final static String RULE_ALL = "^\\s*(?i)(?:ATTACH\\s+ALL\\s+BE)\\s+(?i)(SHARE|NOT\\s+SHARE)(?i)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	//	private final static String RULE_DATABASE = "^\\s*(?i)(?:ATTACH\\s+DATABASE)\\s+([\\p{ASCII}\\W]+?)\\s+(?i)(?:BE)\\s+(?i)(SHARE|NOT\\s+SHARE)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	//	private final static String RULE_TABLE = "^\\s*(?i)(?:ATTACH\\s+TABLE)\\s+([\\p{ASCII}\\W]+?)\\s+(?i)(?:BE)\\s+(?i)(SHARE|NOT\\s+SHARE)(\\s*|\\s+[\\p{ASCII}\\W]+)$";

	//	/**
	//	 * 过滤“分号”分隔符这个前缀
	//	 * @param input 输入语句
	//	 * @return 处理结果
	//	 */
	//	protected String filteSeparator(String input) {
	//		Pattern pattern = Pattern.compile(DistributeParser.FILTE_SEPARATOR);
	//		Matcher matcher = pattern.matcher(input);
	//		if (matcher.matches()) {
	//			return matcher.group(1);
	//		}
	//		return input;
	//	}
	//
	//	/**
	//	 * 判断是“分号”分隔符
	//	 * @param input 输入语句
	//	 * @return 返回真或者假
	//	 */
	//	protected boolean isSeparator(String input) {
	//		Pattern pattern = Pattern.compile(DistributeParser.SEPARATOR);
	//		Matcher matcher = pattern.matcher(input);
	//		return matcher.matches();
	//	}

	//	/**
	//	 * 拆解数据库名称
	//	 * @param input
	//	 * @return
	//	 */
	//	private List<Fame> splitRuleSchemas(String input, boolean online) {
	//		String[] items = input.split("\\s*,\\s*");
	//		ArrayList<Fame> array = new ArrayList<Fame>();
	//		for (int i = 0; i < items.length; i++) {
	//			Fame fame = new Fame(items[i].trim());
	//			if (online) {
	//				if (!hasSchema(fame)) {
	//					throwable(FaultTip.NOTFOUND_X, fame);
	//				}
	//			}
	//			array.add(fame);
	//		}
	//		return array;
	//	}


	//	/**
	//	 * 拆解事务操作符
	//	 * @param input
	//	 * @return
	//	 */
	//	private byte splitRuleOperator(String input) {
	//		byte operator = -1;
	//		if (input.matches("^\\s*(?i)(SHARE)\\s*$")) {
	//			operator = RuleOperator.SHARE_READ;
	//		} else if (input.matches("^\\s*(?i)(NOT\\s+SHARE)\\s*$")) {
	//			operator = RuleOperator.EXCLUSIVE_WRITE;
	//		}
	//		if (!RuleOperator.isOperator(operator)) {
	//			throwable(message(FaultTip.INCORRECT_SYNTAX_X, input));
	//		}
	//		return operator;
	//	}

	//	/**
	//	 * 解析事务规则
	//	 * @param input 输入的事务规则语句
	//	 * @param online 在线模式
	//	 * @return 返回解析的事务列表
	//	 */
	//	protected List<RuleItem> splitRules(String input, boolean online) {
	//		ArrayList<RuleItem> array = new ArrayList<RuleItem>();
	//		for (int i = 0; input.trim().length() > 0; i++) {
	//			// 过滤前缀“AND”字符
	//			if (i > 0) {
	//				Pattern pattern = Pattern.compile(DistributeParser.RULE_PREFIX);
	//				Matcher matcher = pattern.matcher(input);
	//				if (!matcher.matches()) {
	//					throwable(message(FaultTip.INCORRECT_SYNTAX_X, input));
	//				}
	//				input = matcher.group(1);
	//			}
	//
	//			// 事务判断
	//			Pattern pattern = Pattern.compile(DistributeParser.RULE_ALL);
	//			Matcher matcher = pattern.matcher(input);
	//			// 用户级事务
	//			if (matcher.matches()) {
	//				byte operator = splitRuleOperator(matcher.group(1));
	//				// 生成事务规则
	//				UserRuleItem rule = new UserRuleItem(operator);
	//				array.add(rule); // 保存
	//				input = matcher.group(2); // 后续参数
	//				continue;
	//			}
	//			// 数据库级事务
	//			pattern = Pattern.compile(DistributeParser.RULE_DATABASE);
	//			matcher = pattern.matcher(input);
	//			if (matcher.matches()) {
	//				// 数据库名
	//				List<Fame> fames = splitRuleSchemas(matcher.group(1), online);
	//				// 操作符
	//				byte operator = splitRuleOperator(matcher.group(2));
	//				// 数据库事务规则
	//				for (Fame fame : fames) {
	//					SchemaRuleItem rule = new SchemaRuleItem(operator, fame);
	//					array.add(rule);
	//				}
	//				// 其它参数
	//				input = matcher.group(3);
	//				continue;
	//			}
	//			// 表级事务
	//			pattern = Pattern.compile(DistributeParser.RULE_TABLE);
	//			matcher = pattern.matcher(input);
	//			if (matcher.matches()) {
	//				// 表名和操作符
	//				List<Space> spaces = splitSpaces(matcher.group(1), online);
	//				byte operator = splitRuleOperator(matcher.group(2));
	//				// 表事务
	//				for(Space space : spaces) {
	//					TableRuleItem rule = new TableRuleItem(operator, space);
	//					array.add(rule);
	//				}
	//				// 其它参数
	//				input = matcher.group(3);
	//				continue;
	//			}
	//
	//			// 弹出错误
	//			throwable(message(FaultTip.INCORRECT_SYNTAX_X, input));
	//		}
	//
	//		return array;
	//	}

	//	public static void main(String[] args) {
	//		String input = " QUERY(command) = \"SELECT * from \\\"\\, media.music \\\" WHERE id>0 \", ATTACH ALL TO SHARE ";
	//		input = " SELECT * \\from MEDIA.MUSIC WHERE ID>0";
	//		String regex = "^([\\p{ASCII}\\W]*?)(?i)(\\s+\\\\FROM\\s+)([\\p{ASCII}\\W]*)$";
	//		
	//		System.out.println(input);
	//		System.out.println(input.replaceAll(regex, "From"));
	//		
	//	}

	//	public static void main(String[] args) {
	////		String input = " QUERY(command) = \"SELECT * from \\\"\\, media.music \\\" WHERE id>0 \", ATTACH ALL TO SHARE ";
	////		String input = " QUERY(command) = \"SELECT * from  MEDIA.MUSIC  WHERE id>0 \", ATTACH ALL TO SHARE ";
	//		String input = " begin(int)=0,end(int)=9999, total(int)=100, SCANS(COMMAND) = \"SELECT WORD FROM MEDIA.MUSIC WHERE ID>0\"";
	//		
	//		FromInputter inputter = new FromInputter();
	//		DistributeParser e= new DistributeParser();
	//		while (input.trim().length() > 0) {
	//			System.out.println(input);
	//			input = e.splitTaskParameter(inputter, input, false);
	//			
	//			input = e.filtePrefix(input);
	//		}
	////		String s = e.splitTaskParameter(inputter, input, false);
	////		System.out.println(s);
	//	}

	//	public static void main(String[] args ) {
	//		String input = "ATTACH ALL BE NOT SHARE AND ATTACH \n\n DATABASE MEDIA BE \n\n NOT SHARE AND ATTACH TABLE TECH.HELO BE SHARE  AND ATTACH DATABASE HELOKEY BE NOT SHARE";
	//		input = " ATTACH TABLE MEDIA.MUSIC BE SHARE";
	//		DistributeParser e= new DistributeParser();
	//		List<Rule> rules =	e.splitRules(input, false);
	//		System.out.printf("rule size is %d\n", rules.size());
	//	}
}