/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.remote.client.echo.*;

/**
 * 命令转义管理池 <br>
 * 
 * 这个管理池接受来自沙箱组件中的命令，借助线程，跳出沙箱检查，以系统身份，把命令发送给本地命令管理池，并且等待结果。
 * 
 * 设置为保护类型，只在系统接口中可见。
 * 
 * @author scott.liang
 * @version 1.0 4/28/2018
 * @since laxcus 1.0
 */
public class SwitchPool extends VirtualPool {
	
	/** 命令管理池  **/
	private CommandPool commandPool;
	
	/** 调用器管理池 **/
	private InvokerPool invokerPool;

	/** ADMIT命令数组 **/
	private ArrayList<SwitchTuple> admits = new ArrayList<SwitchTuple>(100);

	/** PRESS命令数组 **/
	private ArrayList<SwitchTuple> presses = new ArrayList<SwitchTuple>(100);

	/**
	 * 构造命令转义管理池
	 */
	protected SwitchPool() {
		super();
		setSleepTimeMillis(60000);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		commandPool = getLauncher().getCommandPool();
		invokerPool = getLauncher().getInvokerPool();
		return (commandPool != null && invokerPool != null);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.debug(this, "process", "into...");

		while (!isInterrupted()) {
			check();
			sleep();
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		admits.clear();
		presses.clear();
	}

	/**
	 * 检查业务
	 */
	private void check() {
		super.lockSingle();
		try {
			while (admits.size() > 0) {
				SwitchTuple tuple = admits.remove(0);
				admit(tuple);
			}
			while (presses.size() > 0) {
				SwitchTuple tuple = presses.remove(0);
				press(tuple);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 对命令分布基本参数，保持前后一致
	 * @param cmd 异步命令
	 */
	private void grant(Command cmd) {
		// 没用定义来源调用器编号，忽略
		if (!cmd.hasRelateId()) {
			return;
		}

		// 拿到关联调用器编号
		long invokerId = cmd.getRelateId();
		// 查找调用器
		EchoInvoker invoker = invokerPool.findInvoker(invokerId);
		if (invoker == null) {
			throw new EchoException("cannot be find invoker, by %d", invokerId);
		}

		// 设置为内存/硬盘模式
		cmd.setMemory(invoker.isMemory());
		// 设置用户签名
		cmd.setIssuer(invoker.getIssuer());
		// 命令优先级
		cmd.setPriority(invoker.getPriority());

		Logger.debug(this, "grant", "Memory:%s, Issuer:%s", cmd.isMemory(), cmd.getIssuer());
	}

	/**
	 * 投递命令切换元组
	 * @param tuple 命令切换元组
	 */
	private void admit(SwitchTuple tuple) {
		Command cmd = tuple.getCommand();
		grant(cmd);
		boolean success = false;
		try {
			success = commandPool.admit(cmd);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		tuple.setSuccessful(success);
	}

	/**
	 * 启动命令切换元组
	 * @param tuple 命令切换元组
	 */
	private void press(SwitchTuple tuple) {
		Command cmd = tuple.getCommand();
		grant(cmd);
		boolean success = false;
		try {
			success = commandPool.press(cmd);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		tuple.setSuccessful(success);
	}

	/**
	 * 保存命令。<br>
	 * 这个过程，是将命令从沙箱状态，切换到系统状态。命令将跳出沙箱的监视，以系统状态运行。
	 * 
	 * @param cmd 异步命令
	 * @param press 立即触发启动
	 * @return 保存并等待接受成功返回真，否则假
	 */
	private boolean add(Command cmd, boolean press) {
		// 生成命令元组
		SwitchTuple tuple = new SwitchTuple(cmd);

		// 保存命令
		boolean success = false;
		super.lockSingle();
		try {
			if (press) {
				success = presses.add(tuple);
			} else {
				success = admits.add(tuple);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 成功，唤醒线程和等待结果；失败，设置不成功
		if (success) {
			wakeup();
			tuple.await();
		} else {
			tuple.setSuccessful(false);
		}
		// 返回成功或者否
		return tuple.isSuccessful();
	}

	/**
	 * 命令从沙箱状态，切换到系统环境中
	 * @param cmd 命令实例
	 * @return 接受返回真，否则假
	 */
	public boolean admit(Command cmd) {
		return add(cmd, false);
	}

	/**
	 * 命令从沙箱状态，切换到系统环境状态。这个过程将跳出沙箱的监视
	 * @param cmd 命令实例
	 * @return 接受返回真，否则假
	 */
	public boolean press(Command cmd) {
		return add(cmd, true);
	}

}