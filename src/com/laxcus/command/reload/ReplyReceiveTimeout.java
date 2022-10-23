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
 * 异步接收器超时时间 <br>
 * 
 * 对应到ReplySucker
 *
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class ReplyReceiveTimeout extends ReplyTimeout {
	
	private static final long serialVersionUID = 1025505428610729695L;

	/** 接收FIXP包失效时间 **/
	private long disableTimeout;
	
	/** 接收FIXP子包超时时间 **/
	private long subPacketTimeout;
	
	/**
	 * 构造默认的设置异步接收超时
	 */
	public ReplyReceiveTimeout() {
		super();
	}

	/**
	 * 生成设置异步接收超时副本
	 * @param that
	 */
	private ReplyReceiveTimeout(ReplyReceiveTimeout that) {
		super(that);
		disableTimeout = that.disableTimeout;
		subPacketTimeout = that.subPacketTimeout;
	}

	/**
	 * 从可类化读取器解析设置异步接收超时
	 * @param reader 可类化读取器
	 */
	public ReplyReceiveTimeout(ClassReader reader) {
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

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ReplyReceiveTimeout duplicate() {
		return new ReplyReceiveTimeout(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(disableTimeout);
		writer.writeLong(subPacketTimeout);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		disableTimeout = reader.readLong();
		subPacketTimeout = reader.readLong();
	}

}