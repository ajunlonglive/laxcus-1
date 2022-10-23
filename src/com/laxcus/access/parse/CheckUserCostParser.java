/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.text.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.access.util.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.tip.*;

/**
 * 诊断用户消耗记录 <BR><BR>
 * 
 * 如：<BR>
 * CHECK USER COST -SITES|-S TOP, CALL, DATA, WORK -USERS|-U AIXIT, NETBAR -COMMAND|-C ESTABLISH, CONTACT -BEGIN|-B 1990-12-2 -END 2020-2-2 <BR> 
 * 
 * @author scott.liang
 * @version 1.0 10/11/2022
 * @since laxcus 1.0
 */
public class CheckUserCostParser extends SyntaxParser {
	
	private final static String REGEX = "^\\s*(?i)(?:CHECK\\s+USER\\s+COST)\\s+([\\w\\W]+)\\s*$";

	/** -SITES 参数 **/
	private final static String SITES = "^\\s*(?i)(?:-SITES|-S)\\s+([\\w\\W]+?)(\\s*|\\s+\\-[\\w\\W]+)\\s*$";

	/** -USERS 参数 **/
	private final static String USER = "^\\s*(?i)(?:-USERS|-U)\\s+([\\w\\W]+?)(\\s*|\\s+\\-[\\w\\W]+)\\s*$";

	/** -COMMANDS 命令参数 **/
	private final static String COMMANDS = "^\\s*(?i)(?:-COMMANDS|-C)\\s+([\\w\\W]+?)(\\s*|\\s+\\-[\\w\\W]+)\\s*$";

	/** 开始时间 **/
	private final static String BEGIN_TIME = "^\\s*(?i)(?:-BEGIN|-B)\\s+([\\w\\W]+?)(\\s*|\\s+\\-[\\w\\W]+)\\s*$";

	/** 结束时间 **/
	private final static String END_TIME = "^\\s*(?i)(?:-END|-E)\\s+([\\w\\W]+?)(\\s*|\\s+\\-[\\w\\W]+)\\s*$";

	/**
	 * 构造诊断用户消耗记录
	 */
	public CheckUserCostParser() {
		super();
	}

	/**
	 * 判断检测站点连通性语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CHECK USER COST", input);
		}
		Pattern pattern = Pattern.compile(CheckUserCostParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析参数
	 * @param cmd 命令
	 * @param input 输入语句
	 */
	private void splitParameters(CheckUserCost cmd, String input) {
		while (input.trim().length() > 0) {
			// -SITES参数
			Pattern pattern = Pattern.compile(CheckUserCostParser.SITES);
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String[] types = splitCommaSymbol(matcher.group(1));
				for (String type : types) {
					byte who = SiteTag.translate(type);
					// 判断有效
					boolean refuse = (SiteTag.isFront(who)
							|| SiteTag.isWatch(who) || SiteTag.isLog(who));
					if (refuse) {
						throwableNo(FaultTip.NOTSUPPORT_X, type);
					}
					cmd.addType(who);
				}
				// 后面的参数
				input = matcher.group(2);
				continue;
			}

			// -USER参数
			pattern = Pattern.compile(CheckUserCostParser.USER);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String[] users = splitCommaSymbol(matcher.group(1));
				for (String user : users) {
					// 判断是16进制符号或者文本
					if (Siger.validate(user)) {
						Siger siger = new Siger(user);
						cmd.addUser(siger);
					} else {
						Siger siger = Laxkit.doSiger(user);
						cmd.addUser(siger);
						cmd.addText(user);
					}
				}
				// 后面的参数
				input = matcher.group(2);
				continue;
			}
			
			// -COMMAND参数
			pattern = Pattern.compile(CheckUserCostParser.COMMANDS);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String[] strs = splitCommaSymbol(matcher.group(1));
				for (String str : strs) {
					// 过滤空格字符
					str = formatCommand(str);
					cmd.addCommand(str);
				}
				// 后面的参数
				input = matcher.group(2);
				continue;
			}
			
			// 开始时间参数
			pattern = Pattern.compile(CheckUserCostParser.BEGIN_TIME);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String text = matcher.group(1);
				// 开始查询时间
				long timestamp = CalendarGenerator.splitTimestamp(text);
				cmd.setBeginTime(timestamp);
				// 后面的参数
				input = matcher.group(2);
				continue;
			}
			
			// 结束时间
			pattern = Pattern.compile(CheckUserCostParser.END_TIME);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String text = matcher.group(1);
				long timestamp = CalendarGenerator.splitTimestamp(text);
				cmd.setEndTime(timestamp);

				// 后面的参数
				input = matcher.group(2);
				continue;
			}
			
			// 错误
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}
	}
	
	/**
	 * 格式化命令，忽略空格
	 * @param cmd 命令
	 * @return 返回格式化结果
	 */
	private String formatCommand(String cmd) {
		StringBuilder bf = new StringBuilder();
		int len = cmd.length();
		for (int i = 0; i < len; i++) {
			char w = cmd.charAt(i);
			if (w != 0x20) {
				bf.append(w);
			}
		}
		return bf.toString();
	}
	
//	private void checkBeginTime(CheckUserCost cmd) {
//		long time = cmd.getBeginTime();
//		java.util.Date date = SimpleTimestamp.format(time);
//		java.util.Calendar dar = java.util.Calendar.getInstance();
//		dar.setTime(date);
//
//		int hour = dar.get(Calendar.HOUR_OF_DAY);
//		if (hour == 0) {
//			dar.set(Calendar.HOUR_OF_DAY, 0);
//		}
//
//		int minute = dar.get(Calendar.MINUTE);
//		if (minute == 0) {
//			dar.set(Calendar.MINUTE, 0);
//		}
//		int second = dar.get(Calendar.SECOND);
//		if (second == 0) {
//			dar.set(Calendar.SECOND, 0);
//		}
//
//		int ms = dar.get(Calendar.MILLISECOND);
//		if (ms == 0) {
//			dar.set(Calendar.MILLISECOND, 0);
//		}
//
//		// 重新格式化时间
//		date = dar.getTime();
//		time = SimpleTimestamp.format(date);
//		cmd.setBeginTime(time);
//	}

	private void checkEndTime(CheckUserCost cmd) {
		long time = cmd.getEndTime();
		java.util.Date date = SimpleTimestamp.format(time);
		java.util.Calendar dar = java.util.Calendar.getInstance();
		dar.setTime(date);

		int hour = dar.get(Calendar.HOUR_OF_DAY);
		int minute = dar.get(Calendar.MINUTE);
		int second = dar.get(Calendar.SECOND);
		int ms = dar.get(Calendar.MILLISECOND);
		
		// 时间
		if (hour <= 0 && minute <= 0 && second <= 0 && ms <= 0) {
			dar.set(Calendar.HOUR_OF_DAY, 23);
			dar.set(Calendar.MINUTE, 59);
			dar.set(Calendar.SECOND, 59);
			dar.set(Calendar.MILLISECOND, 999);
		}

		// 重新格式化时间
		date = dar.getTime();
		time = SimpleTimestamp.format(date);
		cmd.setEndTime(time);
	}

	/**
	 * 解析检测站点连通性命令
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回CheckUserCost命令
	 */
	public CheckUserCost split(String input, boolean online) {
		// 检测在线模式
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(CheckUserCostParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 判断匹配
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		String suffix = matcher.group(1);
		// 解析参数
		CheckUserCost cmd = new CheckUserCost();
		splitParameters(cmd, suffix);

		// 参数不足
		if (cmd.getTypes().isEmpty()) {
			throwable(FaultTip.PARAMETER_MISSING);
		}
		if (cmd.getUsers().isEmpty()) {
			throwable(FaultTip.PARAMETER_MISSING);
		}
		if (cmd.getBeginTime() == 0 || cmd.getEndTime() == 0) {
			throwable(FaultTip.PARAMETER_MISSING);
		}
		// 开始时间不能大于结束时间
		if (cmd.getBeginTime() > cmd.getEndTime()) {
			throwable(FaultTip.ILLEGAL_PARAMETER);
		}
		
		// 结束时间的时分秒如果定义，最大
		checkEndTime(cmd);
		
		// 保存原语
		cmd.setPrimitive(input);

		return cmd;
	}
	
	public static void main(String[] args) {
		String input = "CHECK USER COST -SITES CALL,DATA,WORK,BUILD -USERS TINY,RUBA -COMMANDS CONDUCT -BEGIN 2022-10-2 -END 2022-10-15";
		CheckUserCostParser parser = new CheckUserCostParser();
		CheckUserCost cmd = parser.split(input, false);
		
		boolean allow = cmd.hasType(SiteTag.CALL_SITE);
		System.out.printf("%s\n", (allow ? "YES":"NO"));
		
		long st = CalendarGenerator.splitTimestamp("2022-10-13 4:12:9");
		SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
		System.out.printf("time is %s\n",	style.format( SimpleTimestamp.format(st)) );
	}

}