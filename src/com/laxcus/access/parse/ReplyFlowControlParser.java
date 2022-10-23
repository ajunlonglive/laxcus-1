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
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 设置应答数据流接收队列成员解析器 <BR><BR>
 * 
 * 格式：SET REPLY FLOW CONTROL -BLOCK  队列成员数 -TIMESLICE 时间片 TO [节点地址|ALL|LOCAL]
 * 
 * @author scott.liang
 * @version 1.0 9/9/2020
 * @since laxcus 1.0
 */
public class ReplyFlowControlParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+REPLY\\s+FLOW\\s+CONTROL)\\s+(?i)([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";
	
	/** 流量块 **/
	private final static String BLOCK = "^\\s*(?i)(?:-BLOCK|-B)\\s+([1-9][0-9]*?)(\\s*|\\s+[\\w\\W]+)$";
	
	/** 时间片 **/
	private final static String TIMESLICE = "^\\s*(?i)(?:-TIMESLICE|-TS|-T)\\s+([1-9][0-9]*[\\w\\W]+?)(\\s*|\\s+[\\w\\W]+)$";

	/** 包单元 **/
	private final static String UNIT = "^\\s*(?i)(?:-UNIT|-U)\\s+(?i)([1-9][0-9]*\\s*[KB|K]*)(\\s*|\\s+[\\w\\W]+)$";

	/**
	 * 构造默认的设置应答数据流接收队列成员解析器
	 */
	public ReplyFlowControlParser() {
		super();
	}
	
	/**
	 * 判断匹配“SET REPLY PACKET SIZE”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SET REPLY FLOW CONTROL", input);
		}
		Pattern pattern = Pattern.compile(ReplyFlowControlParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析参数
	 * @param input 输入语句
	 * @param cmd 命令
	 */
	private void splitParams(String input, ReplyFlowControl cmd) {
		// 判断参数
		while (input.trim().length() > 0) {
			// 流量块
			Pattern pattern = Pattern.compile(ReplyFlowControlParser.BLOCK);
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String sub = matcher.group(1);
				input = matcher.group(2);
				cmd.setBlock(Integer.parseInt(sub));
				continue;
			}
			// 时间片
			pattern = Pattern.compile(ReplyFlowControlParser.TIMESLICE);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String sub = matcher.group(1);
				input = matcher.group(2);
				
				// 解析微秒时间
				int ns = ConfigParser.splitMicroTime(sub, -1);
				if (ns < 0) {
					throwableNo(FaultTip.INCORRECT_SYNTAX_X, sub);
				}
				cmd.setTimeslice(ns);
				continue;
			}
			
			// 包单元
			pattern = Pattern.compile(ReplyFlowControlParser.UNIT);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				// 内容长度
				String sub = matcher.group(1);
				input = matcher.group(2);
				
				long len = ConfigParser.splitLongCapacity(sub, 0);
				if (!ReplyTransfer.isSubPacketContentSize((int) len)) { // 不在范围内弹出异常
					throwableNo(FaultTip.ILLEGAL_VALUE_X, sub);
				}
				
				// 子包尺寸
				cmd.setSubPacketContentSize((int)(len));
				continue;
			}
			
			// 弹出异常
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
	}

	/**
	 * 解析“SET REPLY FLOW BLOCK”语句
	 * @param input 输入语句
	 * @return 返回ReplyFlowControl命令
	 */
	public ReplyFlowControl split(String input) {
		Pattern pattern = Pattern.compile(ReplyFlowControlParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ReplyFlowControl cmd = new ReplyFlowControl();
	
		// 解析参数
		String params = matcher.group(1);
		splitParams(params, cmd);

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