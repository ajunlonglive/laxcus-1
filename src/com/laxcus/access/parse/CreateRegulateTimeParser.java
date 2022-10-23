/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.util.tip.*;

/**
 * 建立数据优化时间解析器。<br>
 * 此命令在FRONT站点产生，经过Gate站点转发，保存到ACCOUNT站点。
 * 触发由Gate站点判断，交给CALL站点，控制DATA主站点执行数据优化工作。
 * 
 * 
 * 语法格式：<br>
 * CREATE REGULATE TIME 数据库名.表名 HOURLY 分钟:秒 [ORDER BY 列名] <br>
 * CREATE REGULATE TIME 数据库名.表名 DAILY 小时:分钟:秒 [ORDER BY 列名] <br>
 * CREATE REGULATE TIME 数据库名.表名 WEEKLY 星期的某天 小时:分钟:秒 [ORDER BY 列名]<br>
 * CREATE REGULATE TIME 数据库名.表名 MONTHLY 月的某天 小时:分钟:秒 [ORDER BY 列名]<br>
 * 
 * @author scott.liang
 * @version 1.2 09/07/2013
 * @since laxcus 1.0
 */
public class CreateRegulateTimeParser extends SyntaxParser {

	/** 数据优化语句格式 */
	private final static String REGULATE_TIME1 = "^\\s*(?i)(?:CREATE\\s+REGULATE\\s+TIME)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(HOURLY|DAILY|WEEKLY|MONTHLY)\\s+([\\p{ASCII}\\W]+?)\\s+(?i)(?:ORDER\\s+BY)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";
	private final static String REGULATE_TIME2 = "^\\s*(?i)(?:CREATE\\s+REGULATE\\s+TIME)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(HOURLY|DAILY|WEEKLY|MONTHLY)\\s+([\\p{ASCII}\\W]+?)\\s*$";

	/** 按小时更新(分:秒) */
	private final static String SQL_HOURLY = "^\\s*([0-9]|[0-5][0-9])\\:([0-9]|[0-5][0-9])\\s*$";

	/** 按天更新(时:分:秒) */
	private final static String SQL_DAILY = "^\\s*([0-9]|[0-1]\\d|20|21|22|23|24)\\:([0-9]|[0-5][0-9])\\:([0-9]|[0-5][0-9])\\s*$";

	/** 按周更新(周第x日<space>时:分:秒) */
	private final static String SQL_WEEKLY = "^\\s*([1-7])\\s+([0-9]|[0-1]\\d|20|21|22|23|24)\\:([0-9]|[0-5][0-9])\\:([0-9]|[0-5][0-9])\\s*$";

	/** 按月更新(月第x日 <space> 时:分:秒) */
	private final static String SQL_MONTHLY = "^\\s*([1-9]|[1-2]\\d|30|31)\\s+([0-9]|[0-1]\\d|20|21|22|23|24)\\:([0-9]|[0-5][0-9])\\:([0-9]|[0-5][0-9])\\s*$";

	/**
	 * 构造建立数据优化时间解析器
	 */
	public CreateRegulateTimeParser() {
		super();
	}

	/**
	 * 判断是建立数据优化时间命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("CREATE  REGULATE TIME", input);
		}
		Pattern pattern = Pattern.compile(CreateRegulateTimeParser.REGULATE_TIME1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(CreateRegulateTimeParser.REGULATE_TIME2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析建立数据优化时间命令
	 * @param input 输入语句
	 * @return 返回RegulateTime命令
	 */
	public CreateRegulateTime split(String input) {
		Pattern pattern = Pattern.compile(CreateRegulateTimeParser.REGULATE_TIME1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(CreateRegulateTimeParser.REGULATE_TIME2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 取数据表名称
		Space space = new Space(matcher.group(1), matcher.group(2));
		ResourceChooser chooser = SyntaxParser.getResourceChooser();
		Table table = chooser.findTable(space);
		if (table == null) {
			throwableNo(FaultTip.NOTFOUND_X, space);
		}

		// 触发类型
		String family = matcher.group(3);
		// 触发时间
		String time = matcher.group(4);

		SwitchTime switchTime = new SwitchTime(space);

		// 取排列名
		if (matcher.groupCount() > 4) {
			String name = matcher.group(5);
			ColumnAttribute attribute = table.find(name);
			if (attribute == null) {
				throwableNo(FaultTip.NOTFOUND_X, name);
			}
			// 如果是行存储模型，必须是索引键
			if (table.isNSM() && !attribute.isKey()) {
				throwableNo(FaultTip.NOTFOUND_X, name);
			}
			switchTime.setColumnId(attribute.getColumnId());
		}

		// 解析触发时间
		if (SwitchTimeTag.isHourly(family)) {
			splitHourly(time, switchTime);
		} else if (SwitchTimeTag.isDaily(family)) {
			splitDaily(time, switchTime);
		} else if (SwitchTimeTag.isWeekly(family)) {
			splitWeekly(time, switchTime);
		} else if (SwitchTimeTag.isMonthly(family)) {
			splitMonthly(time, switchTime);
		}

		CreateRegulateTime cmd = new CreateRegulateTime(switchTime);
		cmd.setPrimitive(input);
		return cmd;
	}

	/**
	 * 解析按小时触发
	 * @param input 时间格式
	 * @param switchTime
	 */
	private void splitHourly(String input, SwitchTime switchTime) {
		Pattern pattern = Pattern.compile(CreateRegulateTimeParser.SQL_HOURLY);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		int minute = Integer.parseInt(matcher.group(1));
		int second = Integer.parseInt(matcher.group(2));
		if (minute > 23 || second > 59) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		// 设置间隔参数
		switchTime.setHourlyInterval(minute, second);
	}

	/**
	 * 解析按天触发
	 * @param input 时间格式
	 * @param switchTime
	 */
	private void splitDaily(String input, SwitchTime switchTime) {
		Pattern pattern = Pattern.compile(CreateRegulateTimeParser.SQL_DAILY);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		int hour = Integer.parseInt(matcher.group(1));
		int minute = Integer.parseInt(matcher.group(2));
		int second = Integer.parseInt(matcher.group(3));
		if (hour > 23 || minute > 59 || second > 59) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		// 设置间隔参数
		switchTime.setDailyInterval(hour, minute, second);
	}

	/**
	 * 解析按星期触发
	 * @param input 时间格式
	 * @param switchTime
	 */
	private void splitWeekly(String input, SwitchTime switchTime) {
		Pattern pattern = Pattern.compile(CreateRegulateTimeParser.SQL_WEEKLY);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		int dayOfWeek = Integer.parseInt(matcher.group(1));
		int hour = Integer.parseInt(matcher.group(2));
		int minute = Integer.parseInt(matcher.group(3));
		int second = Integer.parseInt(matcher.group(4));
		if (dayOfWeek < 1 || dayOfWeek > 7 || hour > 23 || minute > 59 || second > 59) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		// 设置间隔参数
		switchTime.setWeeklyInterval(dayOfWeek, hour, minute, second);
	}

	/**
	 * 解析按月触发
	 * @param input 时间格式
	 * @param switchTime
	 */
	private void splitMonthly(String input, SwitchTime switchTime) {
		Pattern pattern = Pattern.compile(CreateRegulateTimeParser.SQL_MONTHLY);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		int dayOfMonth = Integer.parseInt(matcher.group(1));
		int hour = Integer.parseInt(matcher.group(2));
		int minute = Integer.parseInt(matcher.group(3));
		int second = Integer.parseInt(matcher.group(4));
		if (dayOfMonth > 31 || hour > 23 || minute > 59 || second > 59) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		// 设置间隔参数
		switchTime.setMonthlyInterval(dayOfMonth, hour, minute, second);
	}

	//	public static void main(String[] args) {
	//		String input = "CREATE REGULATE TIME 媒体素材库.字体 HOURLY 12:00 ORDER BY 词条";
	//		RegulateTimeParser e = new RegulateTimeParser();
	//		boolean match = e.matches(input);
	//		System.out.printf("%s IS %s\n", input, match);
	//	}
}