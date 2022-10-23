/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

/**
 * 内网检测计时器。<br>
 * 默认是1秒。
 * 
 * @author scott.liang
 * @version 1.0 11/9/2019
 * @since laxcus 1.0
 */
public class PockTimer {
	
	/** 超时时间 **/
	private long timeout;
	
	/** 记录最后的刷新时间 **/
	private long lastTime;

	/**
	 * 构造内网检测计时器，指定超时时间
	 * @param timeout 超时时间
	 */
	public PockTimer(long timeout) {
		super();
		refreshTime();
		// 定义超时时间
		setTimeout(timeout < 1000L ? 1000L : timeout);
	}

	/**
	 * 构造默认的内网检测计时器，默认是1秒
	 */
	public PockTimer() {
		this(1000L);
	}

	/**
	 * 设置超时时间
	 * @param ms 超时时间
	 * @return 更新后的超时时间
	 */
	public long setTimeout(long ms) {
		if (ms >= 1000L) {
			timeout = ms;
		}
		return timeout;
	}

	/**
	 * 返回超时时间
	 * @return 以毫秒为单位的时间
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * 刷新时间
	 */
	public void refreshTime() {
		lastTime = System.currentTimeMillis();
	}

	/**
	 * 判断达到超时时间
	 * @return 返回真或者假
	 */
	public boolean isTimeout() {
		return System.currentTimeMillis() - lastTime >= timeout;
	}

}