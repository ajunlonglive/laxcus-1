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
 * 数据传输速率测试解析器 <br><br>
 * 
 * 语句格式：SWARM 发送的数据长度[M] FIXP数据包长度[K] FIXP数据子包长度（字节） 发送间隔[MS] TO 目标节点。<br><br>
 * 
 * 命令在WATCH/FRONT节点和另一个节点之间进行。<br><br>
 * 
 * 在集群网络内，数据传输流量是一个非常关键的要素，它在很大程度上，决定着大规模数据存储和计算的速度，同时还必须不能影响集群稳定可靠运行。这个命令就是用来检测集群两个节点之间的一个最佳传输速率。<br><br>
 * 
 * 实现最佳传输速率的核心关键：<br>
 * 1. 减少TCP/IP堆栈处理UDP包的频率，这个特别耗时！！！<br>
 * 2. 将FIXP包做在UDP最大允许范围，避免把FIXP数据域切割得过小，一次连续发送多个。<br>
 * 3. 批量发送和批量接收。<br>
 * 4. 规定时延，超过重传。<br>
 * 5. 发送数据包时设置发送间隔，避免服务端压力过大，造成丢包。<br><br>
 * 
 * 被管理员/用户使用，从WATCH/FRONT节点发出，WATCH可以发送除FRONT这外的任意节点，FRONT发送到GATE节点。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/10/2018
 * @since laxcus 1.0
 */
public class SwarmParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:SWARM)\\s+(?i)([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SWARM)\\s+(?i)([1-9][0-9]*\\s*[MB|KB|M|K]+)\\s+(?i)([1-9][0-9]*\\s*[M|K|MB|KB]+)\\s+(?i)([1-9][0-9]*|[1-9][0-9]*\\s*[K|KB]+)\\s+(?i)([0-9]+\\s*[MS|毫秒]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造数据传输速率测试解析器
	 */
	public SwarmParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SWARM", input);
		} else {
			Pattern pattern = Pattern.compile(SwarmParser.REGEX);
			Matcher matcher = pattern.matcher(input);
			return matcher.matches();
		}
	}

	/**
	 * 解析数据传输速率测试命令
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回Swarm命令
	 */
	public Swarm split(String input, boolean online) {
		// 诊断在线状态
		if (online) {
			checkOnline();
		}

		Pattern pattern = Pattern.compile(SwarmParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		Swarm cmd = new Swarm();
		// 数据长度
		int value = (int) ConfigParser.splitLongCapacity(matcher.group(1), -1);
		if (value == -1 || value > Laxkit.GB) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(1));
		}
		cmd.setLength(value);

		// FIXP包长度（包含N个FIXP子包）
		value = (int) ConfigParser.splitLongCapacity(matcher.group(2), -1);
		if (value == -1 || value > Laxkit.mb * 10) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(2));
		}
		cmd.setPacketSize(value);
		
		// FIXP包长度（包含N个FIXP子包）
		value = (int) ConfigParser.splitLongCapacity(matcher.group(3), -1);
		if (!ReplyTransfer.isSubPacketContentSize(value)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(3));
		}
		cmd.setSubPacketSize(value);
		
		// FIXP UDP包发送间隔时间
		int sendInterval = (int) ConfigParser.splitTime(matcher.group(4), -1);
		if (sendInterval == -1) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(4));
		}
		cmd.setSendInterval(sendInterval);

		// 目标节点地址
		String remote = matcher.group(5);

		// 如果不是HUB关键字
		if (!remote.matches("^\\s*(?i)(?:HUB)\\s*$")) {
			// 判断语法正确
			if (!Node.validate(remote)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, remote);
			}
			try {
				cmd.setSite(new Node(remote));
			} catch (UnknownHostException e) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, remote);
			}
		}

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}


}