/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import com.laxcus.access.schema.*;
import com.laxcus.command.cross.*;
import com.laxcus.util.tip.*;

/**
 * 数据表共享解析器
 * 
 * @author scott.liang
 * @version 1.0 6/30/2017
 * @since laxcus 1.0
 */
class ShareTableParser extends ActiveShareCrossParser {

	/**
	 * 构造默认的数据表共享解析器
	 */
	protected ShareTableParser() {
		super();
	}

	/**
	 * 解析共享表命令
	 * @param cmd 共享数据表命令
	 * @param online 在线模式
	 * @param tables 表名
	 * @param operator 操作符
	 * @param names 被授权用户名称
	 */
	protected void split(ShareTable cmd, boolean online, String tables, String operator, String names) {
		// 开放/关闭全部数据表
		if (tables.matches("^\\s*(?i)(?:ALL)\\s*$")) {
//			cmd.setAll(true);
		} else {
			String[] params = splitCommaSymbol(tables);
			for (String param : params) {
				// 判断表格式有效
				if (!Space.validate(param)) {
					throwableNo(FaultTip.INCORRECT_SYNTAX_X, param);
				}
				Space space = new Space(param);
				// 在线检查数据表为当前用户私有
				if (online) {
					if (!isPrivate(space)) {
						throwableNo(FaultTip.NOTFOUND_X, param);
					}
				}
				// 保存命令
				cmd.addSpace(space);
			}
		}
		
		// 解析操作符
		splitOperators(cmd, operator);
		// 解析用户签名
		splitSigers(cmd, names);
	}
}