/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

import java.util.*;

import com.laxcus.log.client.*;

/**
 * 内存释放器
 * 
 * @author scott.liang
 * @version 1.0 12/9/2018
 * @since laxcus 1.0
 */
class MemoryReleaser extends TimerTask {

	/** 刻度时间 **/
	private volatile long scaleTime;
	
	/** 默认是不0时，不启动垃圾回收 **/
	private volatile long interval = 0;

	/**
	 * 内存释放器
	 */
	public MemoryReleaser() {
		super();
		// 更新时间
		refresh();
	}
	
	/**
	 * 设置内存释放间隔时间
	 * @param ms
	 */
	public void setInterval(long ms) {
		interval = ms;
	}

	/**
	 * 返回内存释放间隔时间
	 * @return
	 */
	public long getInterval() {
		return interval;
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		if (isTimeout()) {
			refresh();
			gc();
		}
	}
	
	/**
	 * 刷新注册
	 */
	private void refresh() {
		scaleTime = System.currentTimeMillis();
	}

	/**
	 * 判断达到延时时间
	 * @return 返回真或者假
	 */
	private boolean isTimeout() {
		return interval > 0 && System.currentTimeMillis() - scaleTime >= interval;
	}
	
	/**
	 * 垃圾回收
	 */
	private void gc() {
		System.gc();
		Logger.info(this, "gc", "release memory!");
	}

}