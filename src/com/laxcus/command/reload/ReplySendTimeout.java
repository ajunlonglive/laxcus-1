/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.reload;

import com.laxcus.util.classable.*;

/**
 * 设置异步发送超时。<BR>
 * 对应到ReplyDispatcher
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class ReplySendTimeout extends ReplyTimeout {
	
	private static final long serialVersionUID = 1025505428610729695L;

	/** 发送FIXP包失效时间 **/
	private long disableTimeout;
	
	/** 发送FIXP子包超时时间 **/
	private long subPacketTimeout;
	
	/** 子包发送时间时间 **/
	private long interval;
	
	/**
	 * 构造默认的设置异步发送超时
	 */
	public ReplySendTimeout() {
		super();
	}

	/**
	 * 生成设置异步发送超时副本
	 * @param that
	 */
	private ReplySendTimeout(ReplySendTimeout that) {
		super(that);
		disableTimeout = that.disableTimeout;
		subPacketTimeout = that.subPacketTimeout;
		interval = that.interval;
	}

	/**
	 * 从可类化读取器解析设置异步发送超时
	 * @param reader 可类化读取器
	 */
	public ReplySendTimeout(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置FIXP包失效时间
	 * @param ms 毫秒
	 */
	public void setDisableTimeout(long ms) {
		disableTimeout = ms;
	}

	/**
	 * 返回FIXP失效时间
	 * @return 毫秒
	 */
	public long getDisableTimeout() {
		return disableTimeout;
	}

	/**
	 * 设置FIXP子包超时时间
	 * @param ms 毫秒
	 */
	public void setSubPacketTimeout(long ms) {
		subPacketTimeout = ms;
	}

	/**
	 * 返回FIXP子包超时时间
	 * @return 毫秒
	 */
	public long getSubPacketTimeout() {
		return subPacketTimeout;
	}

	/**
	 * 设置FIXP子包发送间隔时间
	 * @param ms 毫秒
	 */
	public void setInterval(long ms) {
		interval = ms;
	}

	/**
	 * 返回FIXP子包发送间隔时间
	 * @return 毫秒
	 */
	public long getInterval() {
		return interval;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ReplySendTimeout duplicate() {
		return new ReplySendTimeout(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(disableTimeout);
		writer.writeLong(subPacketTimeout);
		writer.writeLong(interval);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		disableTimeout = reader.readLong();
		subPacketTimeout = reader.readLong();
		interval = reader.readLong();
	}

}
