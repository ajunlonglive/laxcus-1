/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.talk.pool;

import com.laxcus.command.*;
import com.laxcus.util.*;

/**
 * 检查操作切换元组 <br>
 * 
 * @author scott.liang
 * @version 1.0 6/13/2018
 * @since laxcus 1.0
 */
final class TalkTuple {

	/** 等待状态，默认是“真” **/
	private boolean awaiting;

	/** 执行完成产生的正常结果对象 **/
	private Command command;

	/** 成功或者失败 **/
	private boolean successful;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Command#finalize()
	 */
	@Override
	public void finalize() {
		command = null;
	}

	/**
	 * 构造默认的检查操作切换元组，指定命令
	 */
	public TalkTuple(Command cmd) {
		super();
		setCommand(cmd);
		awaiting = true;
		successful = false;
	}

	/**
	 * 设置命令
	 * @param e 命令对象
	 */
	private final void setCommand(Command e) {
		Laxkit.nullabled(e);
		command = e;
	}

	/**
	 * 返回命令
	 * @return 命令对象
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * 执行延时
	 * @param timeout 超时时间，单位：毫秒
	 */
	private synchronized void delay(long timeout) {
		try {
			super.wait(timeout);
		} catch (InterruptedException e) {
			
		}
	}

	/**
	 * 唤醒延时
	 */
	private synchronized void wakeup() {
		try {
			notify();
		} catch (IllegalMonitorStateException e) {
			
		}
	}

	/**
	 * 判断处于等待状态
	 * @return 返回真或者假
	 */
	public boolean isAwaiting() {
		return awaiting;
	}

	/**
	 * 触发唤醒对象
	 */
	public void done() {
		awaiting = false;
		wakeup();
	}

	/**
	 * 进入等待状态，直到返回处理结果
	 */
	public void await() {
		while (awaiting) {
			delay(500L);
		}
	}

	/**
	 * 设置执行结果状态（成功或者失败），同时唤醒等待
	 * @param b 成功或者否
	 */
	public void setSuccessful(boolean b) {
		successful = b;
		done();
	}

	/**
	 * 判断执行成功或者失败
	 * @return 故障对象
	 */
	public boolean isSuccessful() {
		return successful;
	}

}