/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.cloud.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 检索云端的分布式应用解析器
 * 
 * @author scott.liang
 * @version 1.0 2/5/2020
 * @since laxcus 1.0
 */
public class SeekCloudWareParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SEEK\\s+CLOUD\\s+WARE)\\s+(ALL|[\\w\\W]+)\\s*$";

	//	private final static String TASKS = "^\\s*(?i)(?:TASKS)\\s+([\\w\\W]+?)(?i)(\\s*|\\s+(?:SCALERS)\\s+[\\w\\W]+)$";

	//	private final static String SWIFTS = "^\\s*(?i)(?:SWIFTS)\\s+([\\w\\W]+?)(?i)(\\s*|\\s+(?:SCALERS|TASKS)\\s+[\\w\\W]+)$";

	//	private final static String SCALERS = "^\\s*(?i)(?:SCALERS)\\s+([\\w\\W]+?)(?i)(\\s*|\\s+(?:TASKS)\\s+[\\w\\W]+)$";
	
	/**
	 * 构造默认的检索云端的分布式应用解析器
	 */
	public SeekCloudWareParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SEEK CLOUD WARE", input);
		}
		Pattern pattern = Pattern.compile(SeekCloudWareParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析命令
	 * @param input 输入语句
	 * @return 返回SeekCloudService命令
	 */
	public SeekCloudWare split(String input) {
		Pattern pattern = Pattern.compile(SeekCloudWareParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String suffix = matcher.group(1);

		SeekCloudWare cmd = new SeekCloudWare();
		// 保存命令原语
		cmd.setPrimitive(input);

		// 全部应用或者部分应用
		if (!suffix.matches("^\\s*(?i)(ALL)\\s*$")) {
			// 不是ALL关键字
			String[] items = splitCommaSymbol(input);
			for (String item : items) {
				// 不匹配，弹出错误!
				if (!Sock.validate(input)) {
					throwableNo(FaultTip.INCORRECT_SYNTAX_X, item);
				}
				cmd.add(new Sock(item));
			}
		}

		//		// 逐个解析
		//		while (!isEmpty(suffix)) {
		//			// 1. 分布计算组件
		//			pattern = Pattern.compile(SeekCloudWareParser.TASKS);
		//			matcher = pattern.matcher(suffix);
		//			boolean success = matcher.matches();
		//			if (success) {
		//				String line = matcher.group(1);
		//				cmd.setTasks(splitTasks(line));
		//				suffix = matcher.group(2);
		//				continue;
		//			}
		//
		////			// 2. 码位计算器
		////			pattern = Pattern.compile(SeekCloudWareParser.SCALERS);
		////			matcher = pattern.matcher(suffix);
		////			// 不匹配是错误
		////			success = matcher.matches();
		////			if (success) {
		////				String line = matcher.group(1);
		////				cmd.setScalers(splitScaler(line));
		////				suffix = matcher.group(2);
		////				continue;
		////			}
		//
		////			// 3. SWIFT组件
		////			pattern = Pattern.compile(SeekCloudWareParser.SWIFTS);
		////			matcher = pattern.matcher(suffix);
		////			// 不匹配是错误
		////			success = matcher.matches();
		////			if (success) {
		////				String line = matcher.group(1);
		////				cmd.setSwifts(splitElement(line));
		////				suffix = matcher.group(2);
		////				continue;
		////			}
		//
		//			// 以上不匹配，弹出错误!
		//			throwable(FaultTip.INCORRECT_SYNTAX_X, suffix);
		//		}

		return cmd;
	}
	
//	/**
//	 * 解析单元，用逗号分隔
//	 * @param input
//	 * @return
//	 */
//	private ScalerWarePart splitScaler(String input) {
//		ScalerWarePart part = new ScalerWarePart();
//
//		// 不是ALL关键字
//		if (!input.matches("^\\s*(?i)(ALL)\\s*$")) {
//			String[] items = splitCommaSymbol(input);
//			for (String item : items) {
//				part.add(new Naming(item));
//			}
//		}
//
//		return part;
//	}

//	/**
//	 * 解析单元，用逗号分隔
//	 * @param input
//	 * @return
//	 */
//	private TaskWarePart splitTasks(String input) {
//		TaskWarePart part = new TaskWarePart();
//
//		// 不是ALL关键字
//		String[] items = splitCommaSymbol(input);
//		for (String item : items) {
//			// 不匹配，弹出错误!
//			if(!Sock.validate(input)) {
//				throwable(FaultTip.INCORRECT_SYNTAX_X, item);
//			}
//			part.add(new Sock(item));
//		}
//
//		return part;
//	}

}