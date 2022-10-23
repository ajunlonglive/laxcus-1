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
 * 数据调整命令解析器。<br><br>
 * 
 * MODULATE命令是REGULATE操作在BUILD站点的实现。<br><br>
 * 
 * 语法:<br>
 * <1> MODULATE 数据库名.表名 ORDER BY 列名 <br>
 * <2> MODULATE 数据库名.表名 <br>
 * 
 * @author scott.liang
 * @version 1.1 12/28/2012
 * @since laxcus 1.0
 */
public class ModulateParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:MODULATE)\\s+([\\w\\W]+)\\s*$";

	/** 正则表达式 **/
	private final static String MODULATE1 = "^\\s*(?i)(?:MODULATE)\\s([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(?:ORDER\\s+BY)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";
	private final static String MODULATE2 = "^\\s*(?i)(?:MODULATE)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";

	/**
	 * 构造数据调整命令解析器
	 */
	public ModulateParser() {
		super();
	}

	/**
	 * 检查匹配MODULATE命令
	 * @param input 输入语句
	 * @return 匹配返回真，否则假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("MODULATE", input);
		}
		Pattern pattern = Pattern.compile(ModulateParser.MODULATE1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(ModulateParser.MODULATE2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析MODULATE命令，返回实例
	 * @param input 输入语句
	 * @return 返回Modulate命令实例
	 */
	public Modulate split(String input) {
		Space space = null;
		short columnId = 0;
		String columnName = null;

		Pattern pattern = Pattern.compile(ModulateParser.MODULATE1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			space = new Space(matcher.group(1), matcher.group(2));
			columnName = matcher.group(3);
		}
		if(!match) {
			pattern = Pattern.compile(ModulateParser.MODULATE2);
			matcher = pattern.matcher(input);
			if(match = matcher.matches()) {
				space = new Space(matcher.group(1), matcher.group(2));
			}
		}

		// 查找对应的数据库表配置
		ResourceChooser chooser = SyntaxParser.getResourceChooser();
		Table table = chooser.findTable(space);
		if (table == null) {
			throwableNo(FaultTip.NOTFOUND_X, space);
		}

		// 解析索引
		if (columnName != null) {
			ColumnAttribute attribute = table.find(columnName);
			if (attribute == null) {
				throwableNo(FaultTip.NOTFOUND_X, columnName);
			}
			// 如果是行存储模型，必须是索引键
			if (table.isNSM() && !attribute.isKey()) {
//				throw new SyntaxException("invalid key:%s", columnName);
				throwableNo(FaultTip.SQL_ILLEGAL_KEY_X, columnName);
			}
			columnId = attribute.getColumnId();
		}

		// 建立命令
		Modulate cmd = new Modulate(new Dock(space, columnId));
		cmd.setPrimitive(input);
		return cmd;
	}

	//	public static void main(String[] args) {
	//		String input = "MODULATE 数据库.表名 ORDER BY 列名";
	//		input = "Modulate 千里江山寒色远.表名";
	//		ModulateParser e = new ModulateParser();
	//		boolean match = e.matches(input);
	//		System.out.printf("%s IS %s\n", input, match);
	//	}
}