/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;
import java.util.*;

import com.laxcus.command.reload.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 设置应答包尺寸解析器 <BR><BR>
 * 
 * 格式：SET REPLY PACKET SIZE FIXP包尺寸 子包尺寸 TO [节点地址|ALL|LOCAL]
 * 
 * @author scott.liang
 * @version 1.0 1/11/2019
 * @since laxcus 1.0
 */
public class ReplyPacketSizeParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+REPLY\\s+PACKET\\s+SIZE)\\s+(?i)([0-9]+\\s*[M|K]*)\\s+(?i)([0-9]+\\s*[K]*)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)(?i)(\\s*|\\s+ON\\s+WIDE\\s*)$";
	
	/** 公网 **/
	private final static String WIDE = "^\\s*(?i)(?:ON\\s+WIDE)\\s*$";

	/**
	 * 构造默认的设置应答包尺寸解析器
	 */
	public ReplyPacketSizeParser() {
		super();
	}
	
	/**
	 * 判断匹配“SET REPLY PACKET SIZE”语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET REPLY PACKET SIZE", input);
		}
		Pattern pattern = Pattern.compile(ReplyPacketSizeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“SET REPLY PACKET SIZE”语句
	 * @param input 输入语句
	 * @return 返回ReplyPacketSize命令
	 */
	public ReplyPacketSize split(String input) {
		Pattern pattern = Pattern.compile(ReplyPacketSizeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ReplyPacketSize cmd = new ReplyPacketSize();
		
		// FIXP包尺寸
		String value = matcher.group(1);
		long mtu = ConfigParser.splitLongCapacity(value, 0);
		if (mtu < 1) {
			return null;
		}
		cmd.setPacketSize((int) mtu);
		
		// FIXP子包尺寸
		value = matcher.group(2);
		mtu = ConfigParser.splitLongCapacity(value, 0);
		if (mtu < 1) {
			return null;
		}
		cmd.setSubPacketSize((int) mtu);

		// 目标地址
		String suffix = matcher.group(3);
		if (suffix.matches("^\\s*(?i)(LOCAL)\\s*$")) {
			cmd.setLocal(true);
		} else if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}
		
		// 判断是公网/内网
		String wide = matcher.group(4);
		cmd.setWide(wide.matches(ReplyPacketSizeParser.WIDE));

		// 原语
		cmd.setPrimitive(input);

		return cmd;
	}

}