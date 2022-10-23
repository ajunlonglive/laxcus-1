/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.pool;

import com.laxcus.pool.*;

/**
 * 分布组件管理池 <br>
 * 这是任务组件（TASK）的基类
 * 
 * @author scott.liang
 * @version 1.0 8/29/2015
 * @since laxcus 1.0
 */
public abstract class ComponentPool extends DiskPool {
	
	/** 检查间隔时间 **/
	private long interval;

	/**
	 * 构造默认的分布组件管理池
	 */
	protected ComponentPool() {
		super();
		// 默认是5分钟
		setInterval(5 * 60 * 1000L);
	}

	/**
	 * 设置组件检查间隔时间
	 * @param ms 单位：毫秒
	 */
	public void setInterval(long ms) {
		interval = ms;
	}

	/**
	 * 返回组件检查间隔时间
	 * @return 以毫秒为单位
	 */
	public long getInterval() {
		return interval;
	}

}