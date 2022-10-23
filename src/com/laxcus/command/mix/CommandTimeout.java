/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 命令超时命令。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2016
 * @since laxcus 1.0
 */
public final class CommandTimeout extends Command {

	private static final long serialVersionUID = -8321601214565490112L;

	/** 命令超时时间 **/
	private long interval;

	/**
	 * 构造命令处理模式
	 */
	public CommandTimeout() {
		super();
	}

	/**
	 * 构造命令处理模式，指定时间间隔
	 * @param interval 时间间隔
	 */
	public CommandTimeout(long interval) {
		this();
		setInterval(interval);
	}
	
	/**
	 * 生成命令副本
	 * @param that CommandTimeout实例
	 */
	private CommandTimeout(CommandTimeout that) {
		super(that);
		interval = that.interval;
	}

	/**
	 * 设置命令最长操作时间
	 * @param ms 最长操作时间
	 */
	public void setInterval(long ms) {
		interval = ms;
	}

	/**
	 * 返回命令最长操作时间
	 * @return 最长操作时间
	 */
	public long getInterval() {
		return interval;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CommandTimeout duplicate() {
		return new CommandTimeout(this);
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