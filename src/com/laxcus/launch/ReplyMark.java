/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

/**
 * 异步通信标签
 * 
 * @author scott.liang
 * @version 1.0 7/24/2018
 * @since laxcus 1.0
 */
public class ReplyMark {
	
	/** 异步数据接收器 **/
	public static final String MK_REPLY_SUCKER = "reply-sucker";

	/** 异步数据发送器 **/
	public static final String MK_REPLY_DISPATCHER = "reply-dispatcher";
	
	/** 批量包尺寸，一个批量包包含N个FIXP子包 **/
	public static final String PACKET_SIZE = "packet-size";
	
	/** 子包尺寸，节点唯一 **/
	public static final String SUBPACKET_SIZE = "subpacket-size";
	
	/** 公网批量包尺寸，一个批量包包含N个FIXP子包 **/
	public static final String WIDE_PACKET_SIZE = "wide-packet-size";
	
	/** 公网子包尺寸，节点唯一 **/
	public static final String WIDE_SUBPACKET_SIZE = "wide-subpacket-size";
	
	/** 流量块，估算每个节点的SOCKET接收可能被多少个线程同时使用 **/
	public static final String FLOW_BLOCKS = "flow-block";
	
	/** 流量时间片，即每个UDP SOCKET接收每个包的调用时间 **/
	public static final String TIME_SLICE = "time-slice";
	
	/** 监听端口 **/
	public static final String PORT = "port";

	/** 被单元在管理池中的失效时间，分为接收/发送两种，分别属于：ReplyHelper / ReplyWorker **/
	public static final String DISABLE_TIMEOUT = "disable-timeout";

	/** 子包超时，分为接收/发送两种状态，分别属于：ReplyHelper / ReplyWorker **/
	public static final String SUBPACKET_RECEIVE_TIMEOUT = "subpacket-timeout";
	
	/** FIXP子包之间的发送间隔，延时发送可以减少丢包概率 **/
	public static final String SEND_INTERVAL = "send-interval";

	/** 接收/发送器的UDP发送缓存尺寸 **/
	public static final String SEND_BUFFER_SIZE = "send-buffer-size";

	/** 接收/发送器的UDP接收缓存尺寸 **/
	public static final String RECEIVE_BUFFER_SIZE = "receive-buffer-size";
	
	/** 线程堆栈尺寸 **/
	public static final String STACK_SIZE = "stack-size";

	/** 多入接口成员数目 **/
	public static final String MI = "mi";

	/** 多出接口成员数目 **/
	public static final String MO = "mo";
}