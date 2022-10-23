/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

/**
 * 延迟注册器，用于非重要场景的HUB注册操作。<br><br>
 * 间隔时间以30秒为下限，不得小于这个数字。<br>
 * 
 * 两个条件触发延时注册：<br>
 * 1. 外部操作要求重新注册，且达到默认的延时注册时间。<br>
 * 2. 达到最大延时注册时间，默认是5分钟。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 4/3/2018
 * @since laxcus 1.0
 */
public final class RegisterTimer {

	/** 最大延迟注册时间，5分钟 **/
	private long maxInterval = 5 * 60 * 1000L; // 300000;

	/** 间隔时间，以毫秒为单位 **/
	private long interval;

	/** 触发 **/
	private volatile boolean touched;

	/** 刻度时间 **/
	private volatile long scaleTime;

	/**
	 * 构造默认的延迟注册器
	 */
	public RegisterTimer() {
		super();
		setInterval(maxInterval); // 5分钟触发一次
		refresh();
	}

	/**
	 * 设置触发状态
	 * @param b
	 */
	private void setTouch(boolean b) {
		touched = b;
	}

	/**
	 * 发生触发
	 */
	public void touch() {
		setTouch(true);
	}

	/**
	 * 取消触发
	 */
	private void cancel() {
		setTouch(false);
	}

	/**
	 * 刷新注册
	 */
	public void refresh() {
		scaleTime = System.currentTimeMillis();
		cancel();
	}

	/**
	 * 判断达到延时时间
	 * @param timeout 延时时间
	 * @return 返回真或者假
	 */
	private boolean isTimeout(long timeout) {
		return System.currentTimeMillis() - scaleTime >= timeout;
	}

	/**
	 * 判断发生触发操作
	 * @return 返回真或者假
	 */
	public boolean isTouch() {
		boolean success = false;
		// 定义参数时...
		if (interval > 0 && maxInterval > 0) {
			// 触发注册操作，且在间隔时间内
			success = touched && isTimeout(interval);
			// 达到最大重新注册间隔时间
			if (!success) {
				success = isTimeout(maxInterval);
			}
		} else {
			// 30秒触发
			success = touched && isTimeout(30000);
		}
		return success;
	}

	/**
	 * 最大注册间隔时间
	 * @param ms 毫秒
	 * @return 返回最大注册间隔时间
	 */
	public long setMaxInterval(long ms) {
		return maxInterval = ms;
	}

	/**
	 * 最大注册间隔时间
	 * @return 最大注册间隔时间
	 */
	public long getMaxInterval() {
		return maxInterval;
	}

	/**
	 * 设置触发间隔
	 * @param ms 毫秒
	 * @return 返回触发时间时间
	 */
	public long setInterval(long ms) {
		return interval = ms;
	}

	/**
	 * 返回触发间隔
	 * @return
	 */
	public long getInterval() {
		return interval;
	}

	//	/**
	//	 * 判断发生触发操作
	//	 * @return 返回真或者假
	//	 */
	//	public boolean isTouch() {
	//		// 触发注册操作，且在间隔时间内
	//		boolean success = touched && isTimeout(interval);
	//		// 达到最大重新注册间隔时间
	//		if (!success) {
	//			success = isTimeout(maxInterval);
	//		}
	//		return success;
	//	}

	//	/**
	//	 * 最大注册间隔时间，不得小于最小时间
	//	 * @param ms 毫秒
	//	 * @return 返回最大注册间隔时间
	//	 */
	//	public long setMaxInterval(long ms) {
	//		// 必须大于20秒，否则无效
	//		if (ms >= 30000) {
	//			maxInterval = ms;
	//		}
	//		return maxInterval;
	//	}
	//
	//	/**
	//	 * 最大注册间隔时间
	//	 * @return 最大注册间隔时间
	//	 */
	//	public long getMaxInterval() {
	//		return maxInterval;
	//	}
	//
	//	/**
	//	 * 设置触发间隔
	//	 * @param ms 毫秒
	//	 * @return 返回触发时间时间
	//	 */
	//	public long setInterval(long ms) {
	//		// 必须大于20秒，否则无效
	//		if (ms >= 30000) {
	//			interval = ms;
	//		}
	//		return interval;
	//	}
	//
	//	/**
	//	 * 返回触发间隔
	//	 * @return
	//	 */
	//	public long getInterval() {
	//		return interval;
	//	}

	//	/** 管理节点规定的子节点最大延时注册时间，默认5分钟。**/
	//	private long hubMaxRegisterInterval = 300000;
	//
	//	/** 管理节点规定的子节点标准延时注册时间，默认30秒。 **/
	//	private long hubRegisterInterval = 30000;

	//	/**
	//	 * 设置服务器最大延时注册时间。不得小于30秒。
	//	 * @param ms 以毫秒为单位的时间
	//	 * @return 已定义以毫秒为单位的时间
	//	 */
	//	public long setHubMaxRegisterInterval(long ms) {
	//		if (ms >= 30000) {
	//			hubMaxRegisterInterval = ms;
	//		}
	//		return hubMaxRegisterInterval;
	//	}
	//
	//	/**
	//	 * 返回服务器最大延时注册时间。
	//	 * @return 以毫秒为单位的时间
	//	 */
	//	public long getHubMaxRegisterInterval() {
	//		return hubMaxRegisterInterval;
	//	}
	//
	//	/**
	//	 * 设置服务器延时注册时间。不得小于30秒。
	//	 * @param ms 以毫秒为单位的时间
	//	 * @return 已定义以毫秒为单位的时间
	//	 */
	//	public long setHubRegisterInterval(long ms) {
	//		if (ms >= 30000) {
	//			hubRegisterInterval = ms;
	//		}
	//		return hubRegisterInterval;
	//	}
	//
	//	/**
	//	 * 返回服务器延时注册时间
	//	 * @return 以毫秒为单位的时间
	//	 */
	//	public long getHubRegisterInterval() {
	//		return hubRegisterInterval;
	//	}

}