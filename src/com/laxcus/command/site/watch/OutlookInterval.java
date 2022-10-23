/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 被WATCH监视节点的定时刷新命令
 * 
 * @author scott.liang
 * @version 1.0 4/13/2018
 * @since laxcus 1.0
 */
public final class OutlookInterval extends Command {

	private static final long serialVersionUID = -9029646649955935260L;
	
	/** 超时间隔时间 **/
	private long interval;

	/**
	 * 构造命令处理模式
	 */
	public OutlookInterval() {
		super();
	}

	/**
	 * 构造命令处理模式
	 * @param interval 间隔时间
	 */
	public OutlookInterval(long interval) {
		this();
		setInterval(interval);
	}

	/**
	 * 构造被WATCH监视节点的定时刷新命令
	 * @param reader 可类化读取器
	 */
	public OutlookInterval(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成命令副本
	 * @param that OutlookInterval实例
	 */
	private OutlookInterval(OutlookInterval that) {
		super(that);
		interval = that.interval;
	}

	/**
	 * 设置超时间隔时间
	 * @param ms 超时间隔时间
	 */
	public void setInterval(long ms) {
		interval = ms;
	}

	/**
	 * 返回超时间隔时间
	 * @return 超时间隔时间
	 */
	public long getInterval() {
		return interval;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public OutlookInterval duplicate() {
		return new OutlookInterval(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(interval);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		interval = reader.readLong();
	}

}