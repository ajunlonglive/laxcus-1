/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.mix.*;
import com.laxcus.util.tip.*;

/**
 * 半截符解析器
 * 
 * @author scott.liang
 * @version 1.0 11/26/2017
 * @since laxcus 1.0
 */
public class BuildHalfParser extends SyntaxParser {

	/** 解码格式 **/
	private final static String DECODE = "^\\s*(?i)(?:DECODE\\s+HALF)\\s+([a-p]{2,})\\s*$";

	/** 编码格式1 **/
	private final static String ENCODE = "^\\s*(?i)(?:ENCODE\\s+HALF)(?i)(\\s+NOT\\s+CASE\\s+|\\s+CASE\\s+|\\s+)([\\w\\W]+?)\\s*$";

	//	/** 编码格式1 **/
	//	private final static String ENCODE1 = "^\\s*(?i)(?:ENCODE\\s+HALF)\\s+(?i)(NOT\\s+CASE|CASE)\\s+([\\w\\W]+)\\s*$";
	//
	//	/** 编码格式2 **/
	//	private final static String ENCODE2 = "^\\s*(?i)(?:ENCODE\\s+HALF)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的半截符解析器
	 */
	public BuildHalfParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			boolean b = isCommand("DECODE HALF", input);
			if (!b) {
				b = isCommand("ENCODE HALF", input);
			}
			return b;
		}
		Pattern pattern = Pattern.compile(BuildHalfParser.DECODE);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		// 不匹配，下一个
		if (!match) {
			pattern = Pattern.compile(BuildHalfParser.ENCODE);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}

		//		// 不匹配，下一个
		//		if (!match) {
		//			pattern = Pattern.compile(BuildHalfParser.ENCODE1);
		//			matcher = pattern.matcher(input);
		//			match = matcher.matches();
		//		}
		//		// 不匹配，下一个
		//		if (!match) {
		//			pattern = Pattern.compile(BuildHalfParser.ENCODE2);
		//			matcher = pattern.matcher(input);
		//			match = matcher.matches();
		//		}
		return match;
	}

	/**
	 * 解析半截符语句
	 * @param input 输入语句
	 * @return 返回散列命令
	 */
	public BuildHalf split(String input) {
		boolean encode = true;
		String ignore = "";
		String text = "";

		// 判断是解码格式
		Pattern pattern = Pattern.compile(BuildHalfParser.DECODE);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			encode = false;
			text = matcher.group(1);
		} 
		// 判断是编码格式
		if (!match) {
			pattern = Pattern.compile(BuildHalfParser.ENCODE);
			matcher = pattern.matcher(input);
			match = matcher.matches();
			if (match) {
				ignore = matcher.group(1);
				text = matcher.group(2);
			}
		}

		//		// 编码格式1
		//		if (!match) {
		//			pattern = Pattern.compile(BuildHalfParser.ENCODE1);
		//			matcher = pattern.matcher(input);
		//			match = matcher.matches();
		//			if (match) {
		//				ignore = matcher.group(1);
		//				text = matcher.group(2);
		//			}
		//		}
		//		// 编码格式2
		//		if (!match) {
		//			pattern = Pattern.compile(BuildHalfParser.ENCODE2);
		//			matcher = pattern.matcher(input);
		//			match = matcher.matches();
		//			if (match) {
		//				text = matcher.group(1);
		//			}
		//		}

		// 以上不匹配，是错误
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		BuildHalf cmd = new BuildHalf();
		// 编码模式
		cmd.setEncode(encode);
		// 编码时大小写忽略
		if (encode) {
			if (ignore.matches("^\\s*(?i)(CASE)\\s*$")) {
				cmd.setIgnore(false);
			} else if (ignore.matches("^\\s*(?i)(NOT\\s+CASE)\\s*$")) {
				cmd.setIgnore(true);
			} else {
				cmd.setIgnore(true);
			}
		}

		cmd.setText(text);
		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}