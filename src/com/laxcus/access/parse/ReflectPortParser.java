/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.util.tip.*;

/**
 * 设置映射端口解析器 <BR>
 * 只在WATCH节点，由管理员设置。<br><br>
 * 
 * 语法格式：SET REFLECT PORT [ 映射端口号1, 映射端口号2, ...] ON [STREAM SERVER,PACKET SERVER,SUCKER SERVER,DISPATCHER SERVER] TO 节点
 * 
 * @author scott.liang
 * @version 1.0 10/22/2020
 * @since laxcus 1.0
 */
public class ReflectPortParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+REFLECT\\s+PORT)\\s+([\\w\\W]+?)\\s+(?i)(?:ON)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的设置映射端口解析器
	 */
	public ReflectPortParser() {
		super();
	}

	/**
	 * 判断匹配设置映射端口语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET REFLECT PORT", input);
		}
		Pattern pattern = Pattern.compile(ReflectPortParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析类型
	 * @param input
	 * @return
	 */
	private int splitFamily(String input) {
		if (input.matches("^\\s*(?i)(STREAM\\s+SERVER)\\s*$")) {
			return ReflectPortItem.STREAM_SERVER;
		} else if (input.matches("^\\s*(?i)(PACKET\\s+SERVER)\\s*$")) {
			return ReflectPortItem.PACKET_SERVER;
		} else if (input.matches("^\\s*(?i)(SUCKER\\s+SERVER)\\s*$")) {
			return ReflectPortItem.SUCKER_SERVER;
		} else if (input.matches("^\\s*(?i)(DISPATCHER\\s+SERVER)\\s*$")) {
			return ReflectPortItem.DISPATCHER_SERVER;
		}
		return -1;
	}

	/**
	 * 解析设置映射端口语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回ReflectPort命令
	 */
	public ReflectPort split(String input) {
		Pattern pattern = Pattern.compile(ReflectPortParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		
		// 解析参数
		String[] ports = splitCommaSymbol(matcher.group(1));
		String[] servers = splitCommaSymbol(matcher.group(2));
		String site = matcher.group(3);

		if (ports.length != servers.length) {
			throwableNo(FaultTip.PARAM_MISSING_X, input);
		}
		// 不是节点
		if (!Node.validate(site)) {
			throwableNo(FaultTip.ILLEGAL_SITE_X, site);
		}
		
		// 命令
		ReflectPort cmd = new ReflectPort();
		try {
			cmd.setSite(new Node(site));
		} catch (java.net.UnknownHostException e) {
			throwableNo(FaultTip.ILLEGAL_SITE_X, site);
		}
		
		// 判断，必须是网关
		Node node = cmd.getSite();
		if (!SiteTag.isGateway(node.getFamily())) {
			throwableNo(FaultTip.ILLEGAL_SITE_X, site);
		}
		// 解析参数
		for (int i = 0; i < ports.length; i++) {
			if (!ConfigParser.isInteger(ports[i])) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, ports[i]);
			}
			int family = splitFamily(servers[i]);
			if (family == -1) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, servers[i]);
			}
			// 判断端口号范围
			int port = Integer.parseInt(ports[i]);
			if (!SocketTag.isPort(port)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, ports[i]);
			}
			// 保存参数
			ReflectPortItem item = new ReflectPortItem(family, port);
			boolean success = cmd.add(item);
			// 如果重复
			if (!success) {
				throwableNo(FaultTip.EXISTED_X, servers[i]);
			}
		}
		
		// 命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}