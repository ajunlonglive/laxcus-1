/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

/**
 * 站点重新注册标识
 * 
 * @author scott.liang
 * @version 1.0 8/29/2015
 * @since laxcus 1.0
 */
public final class KissKey {

	/** 激活标记 **/
	private boolean kiss;

	/** 重新注册间隔时间 **/
	private long interval;

	/** 最近一次更新时间 **/
	private long refreshTime;

	/**
	 * 构造默认的重新注册
	 */
	public KissKey() {
		super();
		setKiss(false);
		refreshTime = 0;
		setInterval(1000L); // 间隔1秒
	}

	/**
	 * 更新时间
	 */
	private void refreshTime() {
		refreshTime = System.currentTimeMillis();
	}

	/**
	 * 重置参数
	 */
	public void reset() {
		setKiss(false);
		refreshTime();
	}

	/**
	 * 判断触发重新注册操作。<br><br>
	 * 
	 * 触发重新注册条件有两个：<br>
	 * 1. 发生激活操作。<br>
	 * 2. 达到重新注册间隔时间。<br>
	 * 
	 * @return 返回真或者假。
	 */
	public boolean isTouch() {
		return kiss && (System.currentTimeMillis() - refreshTime >= interval);
	}

	/**
	 * 设置重新注册标记
	 * @param b 重新注册
	 */
	public void setKiss(boolean b) {
		kiss = b;
	}

	/**
	 * 判断要求重新注册
	 * @return 返回真或者假
	 */
	public boolean isKiss() {
		return kiss;
	}

	/**
	 * 设置重新注册间隔时间。单位：毫秒。
	 * 
	 * @param ms 毫秒
	 */
	public void setInterval(long ms) {
		if (ms >= 0) interval = ms;
	}

	/**
	 * 返回重新注册间隔时间。单位：毫秒。
	 * 
	 * @return 间隔时间
	 */
	public long getInterval() {
		return interval;
	}

}