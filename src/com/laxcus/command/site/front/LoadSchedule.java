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
 * FRONT加载资源数据。<br>
 * 在FRONT节点登录成功后执行，加载的资源分布在GATE/CALL节点上。
 * 
 * @author scott.liang
 * @version 1.0 6/11/2020
 * @since laxcus 1.0
 */
public class LoadSchedule extends Command {

	private static final long serialVersionUID = -8791754822484705807L;

	/** 锁定标记，全局有效！ **/
	private static volatile boolean lockin;
	
	/**
	 * 锁定
	 * @return
	 */
	public static boolean lock() {
		// 没有锁定时，锁定有效
		if (!LoadSchedule.lockin) {
			LoadSchedule.lockin = true;
			return true;
		}
		return false;
	}

	/**
	 * 解析锁定!
	 * @return
	 */
	public static boolean unlock() {
		// 锁定时，解决锁定有效
		if (LoadSchedule.lockin) {
			LoadSchedule.lockin = false;
			return true;
		}
		return false;
	}

	/** 转发命令本地等待超时时间 **/
	private long shiftTimeout;

	/**
	 * 构造FRONT定时刷新，指定转发命令本地等待超时时间
	 * @param shiftTimeout 转发命令本地等待超时时间
	 */
	public LoadSchedule(long shiftTimeout) {
		super();
		// 转发命令时间
		setShiftTimeout(shiftTimeout);
	}
	
	/**
	 * 构造默认的FRONT定时刷新
	 */
	public LoadSchedule() {
		this(120 * 1000); 	// 转发命令时间
	}

	/**
	 * 生成FRONT定时刷新的副本
	 * @param that FRONT定时刷新
	 */
	private LoadSchedule(LoadSchedule that) {
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
	public LoadSchedule duplicate() {
		return new LoadSchedule(this);
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