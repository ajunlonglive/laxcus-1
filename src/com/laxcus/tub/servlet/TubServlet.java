/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.servlet;

import com.laxcus.log.client.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.net.*;

/**
 * 边缘计算服务组件。<br>
 * 这是基础接口，在这个基础，派生任务实例。<br>
 * 
 * 边缘计算服务组件做为“云端、边缘计算、终端”三位一体的中间存介质，以驻留进程方式存在，可以向连接的双方发送命令。
 * 
 * @author scott.liang
 * @version 1.0 6/19/2012
 * @since laxcus 1.0
 */
public abstract class TubServlet {

	/** 边缘容器信道 **/
	private static TubChannel channel;

	/** 边缘容器显示接口。显示命令操作过程中产生信息 **/
	private static TubDisplay display;

	/**
	 * 设置边缘容器信道 。通过信道，容器向云端发送命令。在启动时设置
	 * @param e 边缘容器信道 
	 */
	public static void setChannel(TubChannel e) {
		TubServlet.channel = e;
	}

	/**
	 * 返回边缘容器信道 。
	 * @return 边缘容器信道 实例
	 */
	protected static TubChannel getChannel() {
		return TubServlet.channel;
	}

	/**
	 * 设置边缘容器显示接口。在进程启动时设置，信息通过这个接口显示在界面
	 * @param e TubDisplay实例
	 */
	public static void setDisplay(TubDisplay e) {
		TubServlet.display = e;
	}

	/**
	 * 向窗口投递消息
	 * @param text 消息文本
	 * @param focus 获得焦点
	 */
	public void message(String text, boolean focus) {
		if (TubServlet.display != null) {
			TubServlet.display.message(text, focus);
		}
	}

	/**
	 * 向窗口投递警告
	 * @param text 警告文本
	 * @param focus 获得焦点
	 */
	public void warning(String text, boolean focus) {
		if (TubServlet.display != null) {
			TubServlet.display.warning(text, focus);
		}
	}

	/**
	 * 向窗口投递错误
	 * @param text 错误文本
	 * @param focus 获得焦点
	 */
	public void fault(String text, boolean focus) {
		if (TubServlet.display != null) {
			TubServlet.display.fault(text, focus);
		}
	}

	/**
	 * 向窗口投递消息
	 * @param text 消息文本
	 */
	public void message(String text) {
		if (TubServlet.display != null) {
			TubServlet.display.message(text, true);
		}
	}

	/**
	 * 向窗口投递警告
	 * @param text 警告文本
	 */
	public void warning(String text) {
		if (TubServlet.display != null) {
			TubServlet.display.warning(text, true);
		}
	}

	/**
	 * 向窗口投递错误
	 * @param text 错误文本
	 */
	public void fault(String text) {
		if (TubServlet.display != null) {
			TubServlet.display.fault(text, true);
		}
	}

	/** 边缘计算代理 **/
	private TubTrustor trustor;

	/** 通证 **/
	private TubToken token;

	/**
	 * 释放资源
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		destroy();
	}

	/**
	 * 销毁资源。
	 * 外部不应该调用这个方法
	 */
	protected void destroy() {
		Logger.debug(this, "destroy", "release %d", getId());

		// 销毁资源
		if (trustor != null) {
			trustor.detach(this);
		}
		trustor = null;
		token = null;
	}

	/**
	 * 构造默认的边缘计算服务组件
	 */
	protected TubServlet() {
		super();
		token = new TubToken();
	}

	/**
	 * 返回通证实例 
	 * @return TubToken实例
	 */
	public TubToken getToken() {
		return token;
	}

	/**
	 * 返回当前绑定的主机地址，如果它以驻留方式绑定SOCKET接受远程通信时。<br>
	 * 
	 * @return 返回SocketHost实例，没有是空指针。
	 */
	public SocketHost getHost() {
		return token.getHost();
	}

	/**
	 * 设置主机地址
	 * @param e
	 */
	public void setHost(SocketHost e) {
		token.setHost(e);
	}

	/**
	 * 设置任务ID。运行状态下的任务编号。
	 * @param who 编号
	 */
	public void setId(long who) {
		token.setId(who);
	}

	/**
	 * 设置进程ID。运行状态下的任务编号。
	 * @return 返回编号
	 */
	public long getId() {
		return token.getId();
	}

	/**
	 * 设置线程编号，来自系统线程
	 * @param who 线程编号
	 */
	public void setThreadId(long who) {
		token.setThreadId(who);
	}

	/**
	 * 返回线程编号
	 * @return 线程编号
	 */
	public long getThreadId() {
		return token.getThreadId();
	}

	/**
	 * 工作命名
	 * @param e
	 */
	public void setNaming(Naming e) {
		token.setNaming(e);
	}

	/**
	 * 返回工作命名
	 * @return
	 */
	public Naming getNaming() {
		return token.getNaming();
	}

	/**
	 * 返回开始时间，单位：毫秒
	 * @return 系统时间
	 */
	public long getLaunchTime() {
		return token.getLaunchTime();
	}

	/**
	 * 返回任务运行时间
	 * @return 调用器运行时间
	 */
	public long getRunTime() {
		return token.getRunTime();
	}

	/**
	 * 设置边缘计算代理
	 * @param e TubTrustor实例
	 */
	public void setTrustor(TubTrustor e) {
		trustor = e;
	}

	/**
	 * 返回边缘计算代理
	 * @return TubTrustor实例
	 */
	public TubTrustor getTrustor() {
		return trustor;
	}

	/**
	 * 线程延时等待。单位：毫秒。
	 * @param timeout 超时时间
	 */
	protected synchronized void delay(long timeout) {
		try {
			if (timeout > 0L) {
				wait(timeout);
			}
		} catch (InterruptedException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}

	/**
	 * 进入默认的延时
	 */
	protected void sleep() {
		delay(1000);
	}

	/**
	 * 唤醒线程
	 */
	protected synchronized void wakeup() {
		try {
			notify();
		}catch(IllegalMonitorStateException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}

	/**
	 * 唤醒全部
	 */
	protected synchronized void wakeupAll() {
		try {
			notifyAll();
		}catch(IllegalMonitorStateException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}

	/**
	 * 判断处于活跃状态
	 * @return 返回真或者假
	 */
	public abstract boolean isAlive();

	/**
	 * 启动容器组件。类似主进程的“main”函数。
	 * @param args 输入的参数
	 * @return 返回启动状态结果
	 */
	public abstract TubStartResult launch(String args) throws TubException;

	/**
	 * 停止容器组件运行。
	 * @param args 输入的参数
	 * @return 返回停止结果
	 */
	public abstract TubStopResult stop(String args) throws TubException;

}