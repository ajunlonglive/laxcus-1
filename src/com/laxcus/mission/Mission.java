/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.mission;

import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * 分布计算的前端任务，在FRONT节点上运行。
 * 子类包括TubMission、DriverMission、EdgeMission。
 * 与MeetInvoker的区别是，命令发送出去后，等待返回结果，或者故障出错，才会返回。
 * 
 * @author scott.liang
 * @version 1.2 8/3/2019
 * @since laxcus 10
 */
public abstract class Mission {
	
	/** FRONT节点 **/
	protected static FrontLauncher launcher;

	/**
	 * 设置FRONT节点实例 
	 * @param e FRONT节点
	 */
	public static void setFrontLauncher(FrontLauncher e) {
		Laxkit.nullabled(e);
		Mission.launcher = e;
	}

	/** 单向锁 **/
	private SingleLock lock;

	/** 被投递的命令 **/
	protected Command command;

	/** 处理结果 **/
	protected MissionResult result;
	
	/** 错误实例 **/
	protected MissionException exception;
	
	/** 调用器编号 **/
	protected long invokerId;

	/** 退出标记 **/
	private boolean exit;
	
	/**
	 * 销毁参数，系统回收垃圾时执行
	 */
	private void destroy() {
		command = null;
		result = null;
		exception = null;
		lock = null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		destroy();
	}

	/**
	 * 构造默认的FRONT任务
	 */
	protected Mission() {
		super();
		lock = new SingleLock();
		// 初始化为无效状态
		invokerId = InvokerIdentity.INVALID;
		// 退出为假。收到处理结果时为真。
		exit = false;
	}

	/**
	 * 构造FRONT任务，指定命令
	 * @param cmd 命令
	 */
	public Mission(Command cmd) {
		this();
		setCommand(cmd);
	}

	/**
	 * 设置命令
	 * @param e Command子类实例
	 */
	public void setCommand(Command e) {
		Laxkit.nullabled(e);

		command = e;
	}

	/**
	 * 返回命令
	 * @return Command子类实例
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * 线程延时。单位：毫秒。
	 * @param ms 超时时间
	 */
	protected synchronized void delay(long ms) {
		try {
			if (ms > 0L) {
				wait(ms);
			}
		} catch (InterruptedException e) {
			Logger.error(e);
		}
	}

	/**
	 * 唤醒线程
	 */
	protected synchronized void wakeup() {
		try {
			notify();
		} catch (IllegalMonitorStateException e) {
			Logger.error(e);
		}
	}
	
	/**
	 * 设置为退出
	 */
	protected void exit() {
		exit = true;
		wakeup();
	}
	
	/**
	 * 判断执行退出
	 * @return 返回真或者假
	 */
	public boolean isExit() {
		return exit;
	}

	/**
	 * 设置结果，并且唤醒等待
	 * @param e FrontResult实例
	 */
	public void setResult(MissionResult e) {
		lock.lock();
		try {
			if (result == null && exception == null) {
				result = e;
			}
		} finally {
			lock.unlock();
		}
		// 要求退出
		exit();
	}
	
	/**
	 * 输出结果
	 * @return 结果
	 */
	public MissionResult getResult() {
		return result;
	}
	
	/**
	 * 返回异常
	 * @return 异常实例 
	 */
	public MissionException getException() {
		return exception;
	}

	/**
	 * 设置错误
	 * @param e MissionException实例
	 */
	public void setException(MissionException e) {
		lock.lock();
		try {
			if (result == null && exception == null) {
				exception = e;
			}
		} finally {
			lock.unlock();
		}
		// 要求退出
		exit();
	}
	
	/**
	 * 设置错误
	 * @param message 消息
	 */
	public void setException(String message) {
		setException(new MissionException(message));
	}

	/**
	 * 判断定义了数据处理结果
	 * @return 返回真或者假
	 */
	public boolean hasResult() {
		return result != null;
	}

	/**
	 * 判断定义了错误
	 * @return 返回真或者假
	 */
	public boolean hasFault() {
		return exception != null;
	}

	/**
	 * 判断定义了错误
	 * @return 返回真或者假
	 */
	public boolean hasException() {
		return exception != null;
	}

	/**
	 * 中断异步调用器处理工作
	 * @throws EchoException - 如果调用器没有找到，或者处在运行状态时，弹出异常
	 */
	public void interrupt() throws EchoException {
		if (InvokerIdentity.isInvalid(invokerId)) {
			throw new EchoException("illegal invoker identity:%d", invokerId);
		}

		// 释放任务调用器
		boolean success = getInvokerPool().releaseMissionInvoker(invokerId);
		// 成功，设置驱动器异常
		if (success) {
			setException(new MissionInterruptException("user interrupted!"));
		} else {
			throw new EchoException("interrupt failed!");
		}
	}
	
	/**
	 * 返回FRONT节点的调用器管理池
	 * @return 返回实例
	 */
	public FrontInvokerPool getInvokerPool() {
		return (FrontInvokerPool) launcher.getInvokerPool();
	}

	/**
	 * 提交命令到服务器，返回处理结果
	 * @return 返回处理结果
	 * 
	 * @throws MissionException 如果处理过程中发生异常
	 */
	public MissionResult commit() throws MissionException {
		boolean memory = launcher.isMemory();
		long timeout = launcher.getCommandTimeout();
		return commit(timeout, memory);
	}

	/**
	 * 提交命令到服务器，返回处理结果
	 * 
	 * @param timeout 命令超时时间，单位：毫秒
	 * @param memory 采用内存处理模式
	 * @return 返回处理结果
	 * 
	 * @throws MissionException 如果处理过程中发生异常
	 */
	public abstract MissionResult commit(long timeout, boolean memory)
			throws MissionException;

}