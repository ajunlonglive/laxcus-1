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
 * 连续数据传输速率测试解析器 <br><br>
 * 
 * 语句格式：MULTI SWARM 发送的数据长度[M] FIXP数据包长度[K] FIXP数据子包长度（字节） 发送间隔[MS] TO 目标节点 持续次数。<br><br>
 * 
 * 命令在WATCH/FRONT节点和另一个节点之间进行。<br><br>
 * 
 * 在集群网络内，数据传输流量是一个非常关键的要素，它在很大程度上，决定着大规模数据存储和计算的速度，同时还必须不能影响集群稳定可靠运行。这个命令就是用来检测集群两个节点之间的一个最佳传输速率。<br><br>
 * 
 * 实现最佳传输速率的核心关键：<br>
 * 1. 减少TCP/IP堆栈处理UDP包的频率，这个特别耗时！！！<br>
 * 2. 将FIXP包做在UDP最大允许范围，避免把FIXP数据域切割得过小，一次连续发送多个。<br>
 * 3. 批量发送和批量接收。<br>
 * 4. 规定时延，超过重传。<br><br>
 * 
 * 被管理员/用户使用，从WATCH站/FRONT点发出，WATCH节点可以发送到任意节点，FRONT节点发送到GATE节点。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/26/2018
 * @since laxcus 1.0
 */
public class MultiSwarmParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:MULTI\\s+SWARM)\\s+(?i)([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:MULTI\\s+SWARM)\\s+(?i)([1-9][0-9]*\\s*[MB|KB|M|K]+)\\s+(?i)([1-9][0-9]*\\s*[MB|KB|M|K]+)\\s+(?i)([1-9][0-9]*|[1-9][0-9]*\\s*[K|KB]+)\\s+(?i)([0-9]+\\s*[MS|毫秒]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s+([1-9][0-9]*)\\s*$";

	/**
	 * 构造数据传输速率测试解析器
	 */
	public MultiSwarmParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("MULTI SWARM", input);
		} 
		Pattern pattern = Pattern.compile(MultiSwarmParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析数据传输速率测试命令
	 * @param input 输入语句
	 * @return 返回MultiSwarm命令
	 */
	public MultiSwarm split(String input) {
		Pattern pattern = Pattern.compile(MultiSwarmParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		Swarm sub = new Swarm();
		// 数据长度
		int value = (int) ConfigParser.splitLongCapacity(matcher.group(1), -1);
		if (value == -1 || value > Laxkit.GB) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(1));
		}
		sub.setLength(value);
		// FIXP包长度（包含N个FIXP子包）
		value = (int) ConfigParser.splitLongCapacity(matcher.group(2), -1);
		if (value == -1 || value > Laxkit.mb * 10) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(2));
		}
		sub.setPacketSize(value);
		
		// FIXP包长度（包含N个FIXP子包）
		value = (int) ConfigParser.splitLongCapacity(matcher.group(3), -1);
		if (!ReplyTransfer.isSubPacketContentSize(value)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(3));
		}
		sub.setSubPacketSize(value);

		// FIXP UDP包发送间隔时间
		int sendInterval = (int) ConfigParser.splitTime(matcher.group(4), -1);
		if (sendInterval == -1) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(4));
		}
		sub.setSendInterval(sendInterval);
		
		// 目标节点地址
		String remote = matcher.group(5);

		// 如果不是HUB关键字
		if (!remote.matches("^\\s*(?i)(?:HUB)\\s*$")) {
			// 判断语法正确
			if (!Node.validate(remote)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, remote);
			}
			try {
				sub.setSite(new Node(remote));
			} catch (UnknownHostException e) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, remote);
			}
		}

		// 发送统计数目
		int count = Integer.parseInt(matcher.group(6));

		// 生成命令
		MultiSwarm cmd = new MultiSwarm(count, sub);
		// 保存命令原语
		cmd.setPrimitive(input);
		return cmd;
	}

}