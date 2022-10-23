/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front;

/**
 * FRONT节点计时器 <br>
 * 检查超时时间。
 * 
 * @author scott.liang
 * @version 1.0 8/2/2018
 * @since laxcus 1.0
 */
public final class FrontTimer {

	/** 超时间隔时间 **/
	private long interval;

	/** 刻度时间 **/
	private long scaleTime;

	/**
	 * 构造默认的FRONT节点计时器
	 */
	public FrontTimer(long interval) {
		super();
		setInterval(interval);
		reset();
	}
	
	/**
	 * 更新时间
	 */
	private void refreshTime() {
		scaleTime = System.currentTimeMillis();
	}

	/**
	 * 重置参数
	 */
	public void reset() {
		refreshTime();
	}

	/**
	 * 判断达到超时标准。<br><br>
	 * 
	 * @return 返回真或者假。
	 */
	public boolean isTimeout() {
		return (System.currentTimeMillis() - scaleTime >= interval);
	}

	/**
	 * 设置超时间隔时间。单位：毫秒。
	 * 
	 * @param ms 毫秒
	 */
	public void setInterval(long ms) {
		if (ms >= 0) interval = ms;
	}

	/**
	 * 返回超时间隔时间。单位：毫秒。
	 * 
	 * @return 间隔时间
	 */
	public long getInterval() {
		return interval;
	}

}