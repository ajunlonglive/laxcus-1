/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

import com.laxcus.util.*;

/**
 * 站点前置器。<br>
 * 提供与站点管理相关的参数。
 * 
 * @author scott.liang
 * @version 1.3 12/7/2013
 * @since laxcus 1.0
 */
public abstract class SitePrecursor extends SiteLauncherThread { // MutexThread {

	/** 站点切换操作选项 **/
	public final static int SWITCH_NONE = 0;

	public final static int SWITCH_ACTIVE = 1;

	public final static int SWITCH_LAUNCH = 2;

	/** 切换站点时状态 **/
	private volatile int switchState;

	/** 重新注册标识 **/
	private volatile boolean kiss;

	/** 握手超时通知。超时通知来自注册站点，当这个参数为“真”时，立即发出“HELLO”激活命令。 **/
	private volatile boolean hurry;

	/** 登录状态标记 **/
	private volatile boolean logined;
	
	/** 强制线程停止循环。此操作是在节点某些情况下控制，默认是假。=true时，主线程将延时，停止循环检测 **/
	private volatile boolean roundSuspend;

	/** 站点超时时间 **/
	private long siteTimeout;

	/** 上级站点回应“HELLO”激活的时间 **/
	private long refreshTime;

	/** 站点线程循环单次延时时间 **/
	private long silentTime;

	/**
	 * 构造默认的站点前置器。
	 */
	protected SitePrecursor() {
		super();
		kiss = false;
		hurry = false;
		logined = false;
		roundSuspend = false;
		// 无切换
		switchState = SitePrecursor.SWITCH_NONE;
		refreshEndTime();
		// 站点默认20秒超时
		setSiteTimeout(20);
		// 线程循环单次延时时间，默认2秒
		setSilentTime(2000);
	}

	/**
	 * 设置重新注册标识。
	 * @param b 真或者否
	 */
	protected void setKiss(boolean b) {
		kiss = b;
	}

	/**
	 * 通知站点线程重新注册。 <br>
	 * 重新注册必须满足注册标记和时间超时后才能注册。
	 * 
	 * @param immediately 立即执行或者否
	 */
	public void kiss(boolean immediately) {
		setKiss(true);
		// 如果立即执行，就唤醒线程
		if (immediately) {
			wakeup();
		}
	}

	/**
	 * 通知站点线程立即重新注册。<br>
	 * 这个操作发生时，将立即唤醒站点线程。
	 */
	public void kiss() {
		kiss(true);
	}

	/**
	 * 判断触发重新注册。
	 * 重新注册，不代表达到超时
	 * @return 返回真或者假
	 */
	public boolean isKiss() {
		return kiss; // kiss.isKiss();
	}

	/**
	 * 设置标识。当这个参数为“真”时，将向站点管理连续发送3个数据包
	 * @param b 标识
	 */
	private void setHurry(boolean b) {
		hurry = b;
	}

	/**
	 * 上级站点通知马上发出握手通知
	 */
	public void hurry() {
		setHurry(true);
		// 唤醒线程
		wakeup();
	}

	/**
	 * 撤销握手通知
	 */
	public void unhurry() {
		setHurry(false);
	}

	/**
	 * 判断有握手催促
	 * @return 返回真或者假
	 */
	public boolean isHurry() {
		return hurry;
	}

	/**
	 * 判断已经注册。注册是指节点进入登录状态。
	 * @return 返回真或者假
	 */
	public boolean isLogined() {
		return logined;
	}

	/**
	 * 判断已经注销
	 * @return 返回真或者假
	 */
	public boolean isLogout() {
		return !logined;
	}

	/**
	 * 设置已经登录状态标记
	 * @param b 已经登录状态标记
	 */
	protected void setLogined(boolean b) {
		logined = b;
	}

	/**
	 * 设置强制停止线程循环。此操作由用户手动控制，发生在FRONT/WATCH节点上。<br>
	 * 主线程检测到这个标记，线程内的所有循环处理工作将自动忽略，只保持重复的延时。
	 * 
	 * @param b 是或者否
	 */
	public void setRoundSuspend(boolean b) {
		roundSuspend = b;
	}

	/**
	 * 判断已经强制停止循环
	 * @return 返回真或者假
	 */
	public boolean isRoundSuspend() {
		return roundSuspend;
	}

	/**
	 * 设置站点超时时间。单位：秒
	 * @param second 以秒为单位的超时时间
	 */
	public void setSiteTimeout(int second) {
		if (second > 0) {
			siteTimeout = second * 1000;
		}
	}

	/**
	 * 返回站点超时时间。单位：秒
	 * @return 以秒为单位的超时时间
	 */
	public int getSiteTimeout() {
		return (int) (siteTimeout / 1000);
	}

	/**
	 * 设置站点超时时间。单位：毫秒
	 * @param ms 为毫秒为单位的超时时间
	 */
	public void setSiteTimeoutMillis(long ms) {
		if (ms > 0) {
			siteTimeout = ms;
		}
	}

	/**
	 * 返回站点的毫秒超时时间。
	 * @return 站点的毫秒超时时间
	 */
	public long getSiteTimeoutMillis() {
		return siteTimeout;
	}

	/**
	 * 获得下一次的触发时间。是指定时间和站点超时时间之和
	 * @param currentTime 当前时间
	 * @return 返回触发时间。
	 */
	protected long nextTouchTime(long currentTime) {
		return currentTime + siteTimeout;
	}

	/**
	 * 运行站点下一次触发时间，发送心跳包的时间
	 * @return 下一次触发时间
	 */
	protected long nextTouchTime() {
		return nextTouchTime(System.currentTimeMillis());
	}

	/**
	 * 判断触发超时
	 * @param endTime 目标时间
	 * @return 超时返回“真”，否则“假”。
	 */
	protected boolean isTouchTimeout(long endTime) {
		return System.currentTimeMillis() >= endTime;
	}
	
	/**
	 * 返回最后的刷新时间。<br>
	 * 保护类型，只允许子类调用。
	 * 
	 * @return 长整数
	 */
	protected long getRefreshEndTime() {
		return refreshTime;
	}

	/**
	 * 刷新激活时间。<br>
	 * 当收到来自服务器的激活应答，或者站点进入线程时，调用这个方法。
	 * @return 返回刷新时间
	 */
	public long refreshEndTime() {
		return refreshTime = System.currentTimeMillis();
	}

	/**
	 * 返回失效超时时间。是站点超时时间的5倍。
	 * @return 失效超时时间
	 */
	public long getDisableTimeout() {
		return siteTimeout * 5;
	}

	/**
	 * 判断达到无效超时时间（是站点超时时间的5倍）
	 * @return 返回真或者假
	 */
	public boolean isDisableTimeout() {
		return System.currentTimeMillis() - refreshTime >= getDisableTimeout();
	}

	/**
	 * 返回站点最大超时时间。是标准超时时间的3倍。
	 * @return 最大超时时间
	 */
	public long getMaxTimeout() {
		return siteTimeout * 3;
	}

	/**
	 * 判断达到最大超时（是标准超时时间的3倍）
	 * @return 超时返回“真”，否则“假”。
	 */
	protected boolean isMaxTimeout() {
		return System.currentTimeMillis() - refreshTime >= getMaxTimeout();
	}

	/**
	 * 返回站点最小超时时间。是标准超时时间的2倍
	 * @return 最小超时时间
	 */
	public long getMinTimeout() {
		return siteTimeout * 2;
	}

	/**
	 * 判断站点达到少量超时（是站点超时的2倍）
	 * @return 返回真或者假
	 */
	protected boolean isMinTimeout() {
		return System.currentTimeMillis() - refreshTime >= getMinTimeout();
	}

	/**
	 * 判断站点超时
	 * @return 返回真或者假
	 */
	protected boolean isSiteTimeout() {
		return System.currentTimeMillis() - refreshTime >= siteTimeout;
	}

	/**
	 * 线程循环单次时间，单位：毫秒。
	 * @param ms 毫秒
	 */
	public void setSilentTime(long ms) {
		if (ms >= 1000) silentTime = ms;
	}

	/**
	 * 线程循环单次延时时间。
	 * @return 以毫秒为单位的延时时间。
	 */
	public long getSilentTime() {
		return silentTime;
	}

	/**
	 * 线程定时休眠。<br>
	 * 休眠时间根据传入的截止时间和当前时间的差值确定，
	 * 最大休眠时间由系统循环单次延时为准，超过循环单次延时时间，以循环单次延时时间为准。
	 * 
	 * @param endtime 截止时间
	 */
	protected void resting(long endtime) {
		long left = endtime - System.currentTimeMillis();
		// if (left > 1000L) {
		// left = 1000;
		// }

		// 超过单次最大延时，以单次最大延时为准。
		if (left > silentTime) {
			left = silentTime;
		}

		delay(left);
	}

	/**
	 * 设置切换状态
	 * @param who 切换状态
	 */
	public void setSwitchState(int who) {
		switch (who) {
		case SitePrecursor.SWITCH_NONE:
		case SitePrecursor.SWITCH_ACTIVE:
		case SitePrecursor.SWITCH_LAUNCH:
			switchState = who;
			break;
		default:
			throw new IllegalValueException("illegal %d", who);
		}
	}

	/**
	 * 返回切换状态
	 * @return 切换状态
	 */
	public int getSwitchState() {
		return switchState;
	}

	/**
	 * 是切换状态
	 * @return 返回真或者假
	 */
	public boolean isSwitchHub() {
		return switchState > 0;
	}

	/**
	 * 判断是切换的激活状态。由子类的doSwitchHub方法执行
	 * @return 返回真或者假
	 */
	public boolean isSwitchActive() {
		return switchState == SitePrecursor.SWITCH_ACTIVE;
	}

	/**
	 * 判断是切换的执行状态。由线程施加。
	 * @return 返回真或者假
	 */
	public boolean isSwitchLaunch() {
		return switchState == SitePrecursor.SWITCH_LAUNCH;
	}

}