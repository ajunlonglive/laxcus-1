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
 * 数据库共享解析器
 * 
 * @author scott.liang
 * @version 1.0 6/30/2017
 * @since laxcus 1.0
 */
class ShareSchemaParser extends ActiveShareCrossParser {

	/**
	 * 构造数据库共享解析器
	 */
	protected ShareSchemaParser() {
		super();
	}

	/**
	 * 解析共享数据库
	 * @param cmd 共享数据库命令
	 * @param online 在线模式
	 * @param databases 数据库名
	 * @param operator 操作符
	 * @param names 被授权用户名
	 */
	protected void split(ShareSchema cmd, boolean online, String databases, String operator, String names) {
		// 开放/关闭全部数据库
		if (databases.matches("^\\s*(?i)(?:ALL)\\s*$")) {
//			cmd.setAll(true);
		} else {
			String[] params = super.splitCommaSymbol(databases);
			for(String param : params) {
				// 不匹配
				if(!Fame.validate(param)) {
					throwableNo(FaultTip.INCORRECT_SYNTAX_X, param);
				}
				Fame fame = new Fame(param);
				// 在线检查数据库为当前用户私有
				if (online) {
					if (!isPrivate(fame)) {
						throwableNo(FaultTip.NOTFOUND_X, param);
					}
				}
				// 保存命令
				cmd.addFame(fame);
			}
		}

		// 解析操作符
		splitOperators(cmd, operator);

		// 解析用户签名
		splitSigers(cmd, names);
	}
}
