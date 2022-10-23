/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import com.laxcus.command.access.user.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 检索用户资源解析器
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
class SeekUserResourceParser extends SyntaxParser {

	/**
	 * 构造默认的检索用户资源解析器
	 */
	protected SeekUserResourceParser() {
		super();
	}

	/**
	 * 保存用户签名
	 * @param input 输入语句
	 * @param cmd 多用户签名命令
	 */
	protected void splitUser(String input, MultiUser cmd) {
		// 判断要求显示全部
		if (input.matches("^\\s*(?i)(ALL)\\s*$")) {
			return;
		}

		String[] users = splitCommaSymbol(input);
		for (String username : users) {
			Siger siger = splitSiger(username);
			// 保存用户签名
			cmd.addUser(siger);
			// 不是SHA256签名，是一个普通明文，保存它
			if (!username.matches(SyntaxParser.SIGER_SHA256)) {
				cmd.addPlainText(username);
			}
		}
	}

	/**
	 * 根据字符关键字，建立一个查询站点标识
	 * @param type 字符关键字
	 * @return SeekSiteTag实例
	 */
	private SeekSiteTag create(String type) {
		// 特殊关键字
		if (type.matches("^\\s*(?i)(?:PRIME\\s+DATA)\\s*$")) {
			return new SeekSiteTag(SiteTag.DATA_SITE, RankTag.MASTER);
		} else if (type.matches("^\\s*(?i)(?:SLAVE\\s+DATA)\\s*$")) {
			return new SeekSiteTag(SiteTag.DATA_SITE, RankTag.SLAVE);
		}
		// 一般关键字，去解析
		byte who = SiteTag.translate(type);
		if (SiteTag.isSite(who)) {
			return new SeekSiteTag(who);
		}
		return null;
	}

	/**
	 * 解析查询站点标识
	 * @param input 输入语句
	 * @param cmd 检索用户分布资源命令
	 */
	private void splitSite(String input, SeekUserResource cmd) {
		String[] types = splitCommaSymbol(input);

		for (String item : types) {
			// 全部参数时...
			if (item.matches("^\\s*(?i)(ALL)\\s*$")) {
				String[] all = { "HOME", "CALL", "DATA", "WORK", "BUILD" };
				for (int i = 0; i < all.length; i++) {
					cmd.addTag(create(all[i]));
				}
				continue;
			}
			// 其它定义
			SeekSiteTag tag = create(item);
			if (tag == null) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, item);
			}
			cmd.addTag(tag);
		}
	}

	/**
	 * 解析参数
	 * @param users 用户名集合
	 * @param types 站点类型
	 * @param cmd 检索用户分布资源命令
	 */
	protected void split(String users, String types, SeekUserResource cmd) {
		splitUser(users, cmd);
		splitSite(types, cmd);
	}

}