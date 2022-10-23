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
import com.laxcus.fixp.reply.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 设置应答包尺寸解析器 <BR><BR>
 * 
 * 格式：SET REPLY PACKET MODE SERIAL|PARALLEL TO [节点地址|ALL|LOCAL]
 * 
 * @author scott.liang
 * @version 1.0 1/11/2019
 * @since laxcus 1.0
 */
public class ReplyPacketModeParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+REPLY\\s+PACKET\\s+MODE)\\s+(?i)(SERIAL|PARALLEL)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的设置应答包尺寸解析器
	 */
	public ReplyPacketModeParser() {
		super();
	}

	/**
	 * 判断匹配“SET REPLY PACKET MODE”语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SET REPLY PACKET MODE", input);
		}
		Pattern pattern = Pattern.compile(ReplyPacketModeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“SET REPLY PACKET MODE”语句
	 * @param input 输入语句
	 * @return 返回ReplyPacketMode命令
	 */
	public ReplyPacketMode split(String input) {
		Pattern pattern = Pattern.compile(ReplyPacketModeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ReplyPacketMode cmd = new ReplyPacketMode();
		
		// FIXP包模式
		String value = matcher.group(1);
		if (value.matches("^\\s*(?i)(SERIAL)\\s*$")) {
			cmd.setPacketMode(ReplyTransfer.SERIAL_TRANSFER);
		} else {
			cmd.setPacketMode(ReplyTransfer.PARALLEL_TRANSFER);
		}
		
		// 目标地址
		String suffix = matcher.group(2);
		if (suffix.matches("^\\s*(?i)(LOCAL)\\s*$")) {
			cmd.setLocal(true);
		} else if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}

		cmd.setPrimitive(input);

		return cmd;
	}

}