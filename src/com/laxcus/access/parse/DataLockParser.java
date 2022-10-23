/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import com.laxcus.access.schema.*;
import com.laxcus.command.limit.*;
import com.laxcus.law.limit.*;
import com.laxcus.util.tip.*;

/**
 * 数据资源锁定解析器
 * 
 * @author scott.liang
 * @version 1.0 3/26/2017
 * @since laxcus
 */
class DataLockParser extends SyntaxParser {

	/**
	 * 构造默认的数据资源锁定解析器
	 */
	protected DataLockParser() {
		super();
	}

	/**
	 * 解析数据库锁定操作语句
	 * @param cmd 命令
	 * @param input 表名字符串
	 * @param online 在线状态
	 */
	protected void splitSchemas(PostFault cmd, String input, boolean online) {
		String[] params = splitCommaSymbol(input);
		for (String param : params) {
			Fame fame = new Fame(param);
			// 如果是在线，检索这个数据库有效
			if (online) {
				if (!hasSchema(fame)) {
					throwableNo(FaultTip.NOTFOUND_X, fame);
				}
			}
			// 保存
			SchemaFaultItem item = new SchemaFaultItem(fame);
			cmd.add(item);
		}
	}
	
	/**
	 * 解析表级锁定操作语句
	 * @param cmd 命令
	 * @param input 表名字符串
	 * @param online 在线状态
	 */
	protected void splitTables(PostFault cmd, String input, boolean online) {
		String[] params = splitCommaSymbol(input);
		for (String param : params) {
			Space space = new Space(param);
			// 如果在线，检索这个表名有效
			if (online) {
				if (!hasTable(space)) {
					throwableNo(FaultTip.NOTFOUND_X, space);
				}
			}
			// 保存参数
			TableFaultItem item = new TableFaultItem(space);
			cmd.add(item);
		}
	}

}