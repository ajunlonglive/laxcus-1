/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import com.laxcus.echo.*;
import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * 异步数据交换器。<br>
 * 采取批量的乱序发送/接收的模式。一个批量中有N个子包，每组子包有一个共同包编号（packetId），每个子包有一个唯一的子包编号（index）。
 * 通过包编号和子包编号，接收端对数据进行顺序重组。发送/接收端根本配置判断超时，达到超时后，向对方重新发送数据包。
 * 
 * 发送的每组数据包，以包编号（packetId）为唯一标识。
 * 每个子包，有一个唯一性的子包编号（index）。<br><br>
 * 
 * 乱序传输的FIXP子包数据域长度设计：<br>
 * 1. 以INTERNET的IP MTU的576为基准参考标准，一个FIXP子包长度不能超过这个标准，避免中继过程拆包。<br>
 * 2. 考虑FIXP的包头有60字节以上，FIXP数据域的长度限制在500字节左右，所以子包数据定义为500字节。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/22/2018
 * @since laxcus 1.0
 */
public class ReplyWatcher extends MutexHandler {
	
	/** 数据包编号，双方协定，从1开始 **/
	protected int packetId = 1;

	/** 运行中的标识 **/
	protected ReplyFlag replyFlag;

	/** 异步通信令牌，是必须的基础参数。**/
	protected CastToken token;

	/** 接收/发送包时间 **/
	private long packetTime;
	
	/** 重试发生时间 **/
	private long retryTime;

	/** 发送数据流统计 **/
	private long receiveFlowSize;

	/** 接收数据流统计值 **/
	private long sendFlowSize;

	/** 接收超时 **/
	private int receiveTimeout;

	/** 启用时间，构造时定义。运行时不再改变！ **/
	private long launchTime;
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		destroy();
	}

	/**
	 * 销毁
	 */
	protected void destroy() {
		token = null;
		replyFlag = null;
	}

	/**
	 * 构造默认的异步数据交换器
	 */
	protected ReplyWatcher() {
		super();
		packetId = 1;
		// 初始化
		receiveFlowSize = sendFlowSize = 0L;
		// 设置接收超时时间。
		setReceiveTimeout(60000);
		// 刷新时间
		refreshPacketTime();
		refreshRetryTime();
		// 启动时间
		launchTime = System.currentTimeMillis();
	}
	
	/**
	 * 运行时间
	 * @return
	 */
	public long getRunTime() {
		return System.currentTimeMillis() - launchTime;
	}

	/**
	 * 设置异步通信令牌。<br><br>
	 * REPLY MONITOR反馈过来，保存通信的基础参数：<br>
	 * 1. REPLY MONITOR的地址 <br>
	 * 2. 异步通信标识 <br>
	 * 3. 密钥 <br>
	 * @param e 异步通信令牌
	 */
	public void setToken(CastToken e) {
		Laxkit.nullabled(e);
		token = e;
	}

	/**
	 * 返回异步通信令牌
	 * @return 异步通信令牌
	 */
	public CastToken getToken(){
		return token;
	}

	/**
	 * 返回异步通信标识
	 * @return 异步通信标识
	 */
	public CastFlag getCastFlag() {
		return token.getFlag();
	}

	/**
	 * 返回异步通信码
	 * @return 异步通信码
	 */
	public CastCode getCastCode() {
		return token.getFlag().getCode();
	}

	/**
	 * 返回对称密钥
	 * @return 对称密钥
	 */
	public Cipher getCipher() {
		return token.getCipher();
	}

	/**
	 * 判断有对称密钥
	 * @return 返回真或者假
	 */
	public boolean hasCipher() {
		return token.getCipher() != null;
	}

	/**
	 * 设置异步通信标识
	 * @param e
	 */
	public void setReplyFlag(ReplyFlag e) {
		Laxkit.nullabled(e);
		replyFlag = e;
	}

	/**
	 * 刷新包发送/接收时间
	 */
	public void refreshPacketTime() {
		packetTime = System.currentTimeMillis();
	}

	/**
	 * 达到超时时间
	 * @param timeout 超时时间
	 * @return 返回真或者假
	 */
	public boolean isPacketTimeout(long timeout) {
		return System.currentTimeMillis() - packetTime >= timeout;
	}

	/**
	 * 刷新重试时间
	 */
	public void refreshRetryTime() {
		retryTime = System.currentTimeMillis();
	}

	/**
	 * 达到重试超时时间
	 * @param timeout 超时时间
	 * @return 返回真或者假
	 */
	public boolean isRetryTimeout(long timeout) {
		return System.currentTimeMillis() - retryTime >= timeout;
	}
	
	/**
	 * 增加接收的数据流量
	 * @param size 接收的数据流量尺寸
	 */
	public void addReceiveFlowSize(long size) {
		if (size < 0) {
			throw new IllegalValueException("illegal flow value:%d", size);
		}
		receiveFlowSize += size;
	}

	/**
	 * 返回接收的数据流量
	 * @return 接收的数据流量的整形值
	 */
	public long getReceiveFlowSize() {
		return receiveFlowSize;
	}

	/**
	 * 增加发送的数据流量
	 * @param size 发送的数据流量尺寸
	 */
	public void addSendFlowSize(long size) {
		if (size < 0) {
			throw new IllegalValueException("illegal flow value:%d", size);
		}
		sendFlowSize += size;
	}

	/**
	 * 返回发送的数据流量
	 * @return 发送的数据流量的整型值
	 */
	public long getSendFlowSize() {
		return sendFlowSize;
	}

	/**
	 * 设置接收超时。单位：毫秒
	 * @param ms 接收超时
	 */
	public void setReceiveTimeout(int ms) {
		receiveTimeout = ms;
	}

	/**
	 * 返回接收超时。单位：毫秒
	 * @return 接收超时
	 */
	public int getReceiveTimeout() {
		return receiveTimeout;
	}

	/**
	 * 从数据包中取出异步通信码
	 * @param packet FIXP数据包
	 * @return 异步通信码
	 */
	protected CastCode readCode(Packet packet) {
		try {
			byte[] b = packet.findRaw(MessageKey.CAST_CODE);
			if (b != null) {
				return new CastCode(b);
			}
		} catch (Throwable e) {
			Logger.error(e);
		}
		return null;
	}
	


}
