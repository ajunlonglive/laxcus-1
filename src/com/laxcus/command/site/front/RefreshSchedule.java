/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * FRONT定时刷新本地资源参数。<br>
 * 由资源管理池定时触发，去GATE/ENTRANCE节点获取新的参数，更新到本地和显示，同时修正与CALL节点/授权人GATE节点的连接。
 * 
 * @author scott.liang
 * @version 1.0 6/3/2020
 * @since laxcus 1.0
 */
public class RefreshSchedule extends Command {

	private static final long serialVersionUID = -3443393960636212755L;

	/** 转发命令本地等待超时时间 **/
	private long shiftTimeout;

	/**
	 * 构造FRONT定时刷新，指定转发命令本地等待超时时间
	 * @param shiftTimeout 转发命令本地等待超时时间
	 */
	public RefreshSchedule(long shiftTimeout) {
		super();
		// 转发命令时间
		setShiftTimeout(shiftTimeout);
	}

	/**
	 * 构造默认的FRONT定时刷新
	 */
	public RefreshSchedule() {
		this(120 * 1000); 	// 转发命令时间
	}

	/**
	 * 生成FRONT定时刷新的副本
	 * @param that FRONT定时刷新
	 */
	private RefreshSchedule(RefreshSchedule that) {
		super(that);
		shiftTimeout = that.shiftTimeout;
	}

	/**
	 * 设置转发命令本地等待超时时间，不能少于2分钟
	 * @param ms 超时时间
	 */
	public void setShiftTimeout(long ms) {
		if (ms >= 120 * 1000) {
			shiftTimeout = ms;
		}
	}

	/**
	 * 返回转发命令本地等待超时时间
	 * @return 毫秒
	 */
	public long getShiftTimeout() {
		return shiftTimeout;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RefreshSchedule duplicate() {
		return new RefreshSchedule(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(shiftTimeout);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		shiftTimeout = reader.readLong();
	}

}