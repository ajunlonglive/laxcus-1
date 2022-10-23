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
 * HOME站点定时扫描用户关联的间隔时间。
 * 命令从WATCH站点发出。<br><br>
 * 
 * 语法：SET SCAN LINK TIME [数字]时间单位 [REFRESH]
 * 
 * @author scott.liang
 * @version 1.0 6/3/2018
 * @since laxcus 1.0
 */
public final class ScanLinkTime extends Command {

	private static final long serialVersionUID = -9029646649955935260L;
	
	/** 超时间隔时间 **/
	private long interval;
	
	/** 立即执行这个操作 **/
	private boolean immediate;

	/**
	 * 构造HOME站点定时扫描用户关联的间隔时间
	 */
	public ScanLinkTime() {
		super();
	}
	
	/**
	 * 从可类化数据读取中解析HOME站点定时扫描用户关联的间隔时间
	 * @param reader 可类化数据读取器
	 */
	public ScanLinkTime(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成命令副本
	 * @param that ScanLinkInterval实例
	 */
	private ScanLinkTime(ScanLinkTime that) {
		super(that);
		interval = that.interval;
		immediate = that.immediate;
	}

	/**
	 * 设置超时间隔时间。单位：毫秒。
	 * @param ms 超时间隔时间
	 */
	public void setInterval(long ms) {
		interval = ms;
	}

	/**
	 * 返回超时间隔时间。单位：毫秒。
	 * 
	 * @return 超时间隔时间
	 */
	public long getInterval() {
		return interval;
	}

	/**
	 * 设置执行操作
	 * @param b
	 */
	public void setImmediate(boolean b) {
		immediate = b;
	}

	/**
	 * 判断要执行操作
	 * @return 返回真或者假
	 */
	public boolean isImmediate() {
		return immediate;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ScanLinkTime duplicate() {
		return new ScanLinkTime(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(interval);
		writer.writeBoolean(immediate);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		interval = reader.readLong();
		immediate = reader.readBoolean();
	}

}