/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.*;
import java.util.regex.*;

import com.laxcus.command.traffic.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 数据传输速率测试解析器。与SWARM的区别是，命令从WATCH站点发出，测试集群内的两个节点之间的传输流量。 <br><br>
 * 
 * 语句格式：MULTI GUST 发送的数据长度[M] FIXP数据包长度[K] FIXP数据子包长度（字节）发送间隔[MS] FROM 多个发起站点 TO 多个目标站点。<br><br>
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
public class MultiGustParser extends SyntaxParser {
	
	/** 标题 **/
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:MULTI\\s+GUST)\\s+(?i)([\\w\\W]+)\\s*$";

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:MULTI\\s+GUST)\\s+(?i)([1-9][0-9]*\\s*[MB|KB|M|K]+)\\s+(?i)([1-9][0-9]*\\s*[MB|KB|M|K]+)\\s+(?i)([1-9][0-9]*|[1-9][0-9]*\\s*[K|KB]+)\\s+(?i)([0-9]+\\s*[MS|毫秒]+)\\s+(?i)(?:FROM)\\s+([\\w\\W]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造数据传输速率测试解析器
	 */
	public MultiGustParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("MULTI GUST", input);
		} 
		Pattern pattern = Pattern.compile(MultiGustParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析数据传输速率测试命令
	 * @param input 输入语句
	 * @return 返回MultiGust命令
	 */
	public MultiGust split(String input) {
		Pattern pattern = Pattern.compile(MultiGustParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 数据长度
		int length = (int) ConfigParser.splitLongCapacity(matcher.group(1), -1);
		if (length == -1 || length > Laxkit.GB) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(1));
		}
		// FIXP包长度（包含N个FIXP子包）
		int packetSize = (int) ConfigParser.splitLongCapacity(matcher.group(2), -1);
		if (packetSize == -1 || packetSize > Laxkit.mb * 10) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(2));
		}

		// FIXP子包数据域长度
		int subPacketSize = (int) ConfigParser.splitLongCapacity(matcher.group(3), -1);
		if (!ReplyTransfer.isSubPacketContentSize(subPacketSize)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(3));
		}
		
		// FIXP UDP包发送间隔时间
		int sendInterval = (int) ConfigParser.splitTime(matcher.group(4), -1);
		if (sendInterval == -1) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, matcher.group(4));
		}

		// 发送节点
		List<Node> froms = splitSites(matcher.group(5), SiteTag.NONE);

		// 接收节点
		List<Node> tos = splitSites(matcher.group(6), SiteTag.NONE);

		// 命令
		MultiGust cmd = new MultiGust();

		// 保存命令
		for (Node from : froms) {
			for (Node to : tos) {
				// 不允许地址一样
				if (Laxkit.compareTo(from, to) == 0) {
					continue;
				}

				// 子命令
				Swarm sub = new Swarm();
				sub.setLength(length);
				sub.setPacketSize(packetSize);
				sub.setSubPacketSize(subPacketSize);
				sub.setSendInterval(sendInterval);
				sub.setSite(to);
				// 子命令
				Gust gust = new Gust(from, sub);
				cmd.add(gust);
			}
		}

		// 不允许地址一样
		if (cmd.isEmpty()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}