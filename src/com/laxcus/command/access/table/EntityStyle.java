/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.util.*;

/**
 * 使用“EXPORT ENTITY”命令导出到磁盘的文件样式
 * 
 * @author scott.liang
 * @version 1.0 5/1/2019
 * @since laxcus 1.0
 */
public class EntityStyle {

	/** 无定义 **/
	public final static int NONE = 0;

	/** CSV格式 **/
	public final static int CSV = 1; 

	/** TXT格式 ，以制表符为分割**/
	public final static int TXT = 2; 
	
	/**
	 * 输出全部数字描述
	 * @return 类型的数字描述
	 */
	public static int[] getSymbols() {
		return new int[] { EntityStyle.CSV, EntityStyle.TXT };
	}
	
	/**
	 * 输出全部字符串描述
	 * @return 字符集的字符串描述
	 */
	public static String[] getStrings() {
		ArrayList<String> a = new ArrayList<String>();

		int[] symbols = EntityStyle.getSymbols();
		for (int i = 0; i < symbols.length; i++) {
			String s = EntityStyle.translate(symbols[i]);
			a.add(s);
		}

		String[] s = new String[a.size()];
		return a.toArray(s);
	}
	
	/**
	 * 判断是CSV文件格式
	 * @param who 文件格式
	 * @return 匹配返回真，否则假
	 */
	public static boolean isCSV(int who) {
		return who == EntityStyle.CSV;
	}
	
	/**
	 * 判断是TXT文件格式
	 * @param who 文件格式
	 * @return 匹配返回真，否则假
	 */
	public static boolean isTXT(int who) {
		return who == EntityStyle.TXT;
	}

	/**
	 * 判断是合法的文件类型
	 * @param who 文件类型符
	 * @return 返回真或者假
	 */
	public static boolean isType(int who) {
		switch(who){
		case EntityStyle.CSV:
		case EntityStyle.TXT:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 转义描述字
	 * @param who
	 * @return
	 */
	public static String translate(int who) {
		switch(who){
		case EntityStyle.CSV:
			return "CSV";
		case EntityStyle.TXT:
			return "TXT";
		default:
			return "NONE";
		}
	}
	
	/**
	 * 转义描述字
	 * @param who 字符串描述
	 * @return 支持的类型
	 */
	public static int translate(String who) {
		// 解析参数
		if (who.matches("^\\s*(?i)(?:CSV)\\s*$")) {
			return EntityStyle.CSV; // CSV格式
		}
		if (who.matches("^\\s*(?i)(?:TXT)\\s*$")) {
			return EntityStyle.TXT; // TXT格式
		}
		// 输出了！
		return EntityStyle.NONE;
	}

}
