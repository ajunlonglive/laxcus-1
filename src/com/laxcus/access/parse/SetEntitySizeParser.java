/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 修改数据块尺寸命令解析器 <BR>
 * 
 * 语法格式：SET ENTITY SIZE [DATABASE.TABLE] [DIGIT] M
 * 
 * @author scott.liang
 * @version 1.0 8/12/2012
 * @since laxcus 1.0
 */
public class SetEntitySizeParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:SET\\s+ENTITY\\s+SIZE)\\s+([\\w\\W]+?)\\s*$";

	/** 设置数据块的尺寸，单位：兆，语法格式: SET ENTITYSIZE schema.table digitM */
	private final static String SET_ENTITYSIZE = "^\\s*(?i)(?:SET\\s+ENTITY\\s+SIZE)\\s+(\\w+)\\.(\\w+)\\s+([0-9]{1,})(?i)M\\s*$";

	/**
	 * 构造修改数据块尺寸命令解析器
	 */
	public SetEntitySizeParser() {
		super();
	}

	/**
	 * 检查匹配“修改数据块尺寸”语法："SET ENTITY SIZE ..."
	 * @param input - 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET ENTITY SIZE", input);
		}
		Pattern pattern = Pattern.compile(SetEntitySizeParser.SET_ENTITYSIZE);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析数据块尺寸定义
	 * @param input 语法
	 * @param online 在线状态
	 * @return 返回SetEntitySize命令
	 */
	public SetEntitySize split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(SetEntitySizeParser.SET_ENTITYSIZE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		Space space = new Space(matcher.group(1), matcher.group(2));
		if (online) {
			if (!hasTable(space)) {
				throwableNo(FaultTip.NOTFOUND_X, space);
			}
		}

		String digit = matcher.group(3);
		int size = Integer.parseInt(digit) * Laxkit.mb;
		SetEntitySize cmd = new SetEntitySize(space, size);
		cmd.setPrimitive(input);
		return cmd;
	}

}
