/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

/**
 * HUB节点定义的子节点注册间隔时间
 * 
 * @author scott.liang
 * @version 1.0 12/4/2018
 * @since laxcus 1.0
 */
class HubTimer {

	/** 最大延迟注册时间，5分钟 **/
	private long maxRegisterInterval = 300000;

	/** 间隔时间，以毫秒为单位 **/
	private long registerInterval = 60000;

	/**
	 * 构造默认的HUB节点时间记录器
	 */
	public HubTimer() {
		super();
	}

	/**
	 * 最大注册间隔时间
	 * @param ms 毫秒
	 * @return 返回最大注册间隔时间
	 */
	public long setMaxRegisterInterval(long ms) {
		return maxRegisterInterval = ms;
	}

	/**
	 * 最大注册间隔时间
	 * @return 最大注册间隔时间
	 */
	public long getMaxRegisterInterval() {
		return maxRegisterInterval;
	}

	/**
	 * 设置触发间隔
	 * @param ms 毫秒
	 * @return 返回触发时间时间
	 */
	public long setRegisterInterval(long ms) {
		return registerInterval = ms;
	}

	/**
	 * 返回触发间隔
	 * @return
	 */
	public long getRegisterInterval() {
		return registerInterval;
	}
}
