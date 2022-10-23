/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.net.*;
import java.util.regex.*;

import com.laxcus.command.traffic.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 数据传输速率测试解析器。与SWARM的区别是，命令从WATCH站点发出，测试集群内的两个节点之间的传输流量。 <br><br>
 * 
 * 语句格式：GUST 发送的数据长度[M] FIXP数据包长度[K] FIXP数据子包长度（字节）发送间隔[MS] FROM 发起站点 TO 目标站点。<br><br>
 * 
 * 在集群网络内，数据传输流量是一个非常关键的要素，它在很大程度上，决定着大规模数据存储和计算的速度，同时还必须不能影响集群稳定可靠运行。这个命令就是用来检测集群两个节点之间的一个最佳传输速率。<br><br>
 * 
 * 实现最佳传输速率的核心关键：<br>
 * 1. 减少TCP/IP堆栈处理UDP包的频率，这个特别耗时！！！<br>
 * 2. 将FIXP包做在UDP最大允许范围，避免把FIXP数据域切割得过小，一次连续发送多个。<br>
 * 3. 批量发送和批量接收。<br>
 * 4. 规定时延，超过重传。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 8/15/2018
 * @since laxcus 1.0
 */
public class GustParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:GUST)\\s+(?i)([\\w\\W]+)\\s*$";

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:GUST)\\s+(?i)([1-9][0-9]*\\s*[MB|KB|M|K]+)\\s+(?i)([1-9][0-9]*\\s*[MB|KB|M|K]+)\\s+(?i)([1-9][0-9]*|[1-9][0-9]*\\s*[K|KB]+)\\s+(?i)([0-9]+\\s*[MS|毫秒]+)\\s+(?i)(?:FROM)\\s+([\\w\\W]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造数据传输速率测试解析器
	 */
	public GustParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("GUST", input);
		}
		Pattern pattern = Pattern.compile(GustParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析数据传输速率测试命令
	 * @param input 输入语句
	 * @return 返回CheckSwarm命令
	 */
	public Gust split(String input) {
		Pattern pattern = Pattern.compile(GustParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		Swarm swarm = new Swarm();
		// 数据长度
		int value = (int) ConfigParser.splitLongCapacity(matcher.group(1), -1);
		if (value == -1 || value > Laxkit.GB) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(1));
		}
		swarm.setLength(value);
		// FIXP包长度（包含N个FIXP子包）
		value = (int) ConfigParser.splitLongCapacity(matcher.group(2), -1);
		if (value == -1 || value > Laxkit.mb * 10) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(2));
		}
		swarm.setPacketSize(value);

		// FIXP子包数据域长度
		value = (int) ConfigParser.splitLongCapacity(matcher.group(3), -1);
		if (!ReplyTransfer.isSubPacketContentSize(value)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(3));
		}
		swarm.setSubPacketSize(value);

		// FIXP UDP包发送间隔时间
		int sendInterval = (int) ConfigParser.splitTime(matcher.group(4), -1);
		if (sendInterval == -1) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(4));
		}
		swarm.setSendInterval(sendInterval);

		// 目标站点地址
		String to = matcher.group(6);
		// 判断语法正确
		if (!Node.validate(to)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, to);
		}

		try {
			swarm.setSite(new Node(to));
		} catch (UnknownHostException e) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, to);
		}

		// 检测命令
		Gust cmd = new Gust();
		String from = matcher.group(5);
		// 判断语法正确
		if (!Node.validate(from)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, from);
		}

		try {
			cmd.setFrom(new Node(from));
		} catch (UnknownHostException e) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, from);
		}

		// 不允许地址一样
		if (Laxkit.compareTo(cmd.getFrom(), swarm.getSite()) == 0) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, from + " | " + to);
		}

		cmd.setSwarm(swarm);
		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}