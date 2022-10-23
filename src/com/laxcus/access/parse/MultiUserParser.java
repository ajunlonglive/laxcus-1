/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import com.laxcus.command.access.user.*;
import com.laxcus.util.*;

/**
 * 多用户签名解析器
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public class MultiUserParser extends SyntaxParser {

	/**
	 * 构造默认的多用户签名解析器
	 */
	protected MultiUserParser() {
		super();
	}

	/**
	 * 解析多个用户签名。用户签名分为两种，以"SIGN"为前缀的64个字符，或者普通的文本，它们以逗号为分隔。
	 * @param input 签名文本。存在“SIGN [编码]”和“纯文本”两种格式
	 * @param cmd 多用户命令
	 */
	protected void splitSigers(String input, MultiUser cmd) {
		String[] users = splitCommaSymbol(input);
		for (String name : users) {
			// 解析用户签名
			Siger siger = splitSiger(name);
			cmd.addUser(siger);
			// 保存明文（不论是SHA256码或者字符明文）
			cmd.addPlainText(name);
		}
	}

}