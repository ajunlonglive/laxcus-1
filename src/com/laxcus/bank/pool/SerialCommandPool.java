/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.pool;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.echo.*;

/**
 * BANK站点串行命令管理池。<br><br>
 * 
 * LAXCUS集群的部分账号数据具有全网唯一性，需要串行执行才能保证。这些命令，通过串行命令池逐一处理。<br>
 * 
 * 这些命令包括建立账号和建立数据库。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/26/2018
 * @since laxcus 1.0
 */
public final class SerialCommandPool extends VirtualPool {

	/** BANK站点串行命令管理池句柄 **/
	private static SerialCommandPool selfHandle = new SerialCommandPool();

	/** 串行工作管理器 **/
	private ArrayList<Command> array = new ArrayList<Command>();

	/**
	 * 构造BANK站点串行命令管理池
	 */
	private SerialCommandPool() {
		super();
	}

	/**
	 * 返回BANK站点串行命令管理池句柄
	 * @return 管理池实例
	 */
	public static SerialCommandPool getInstance() {
		return SerialCommandPool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		// 串行处理命令
		while (!isInterrupted()) {
			// 队列空，继续自时
			if (array.isEmpty()) {
				sleep();
				continue;
			}
			// 锁定取出每一个命令
			Command cmd = null;
			super.lockSingle();
			try {
				if (array.size() > 0) {
					cmd = array.remove(0);
				}
			} catch (Throwable e) {
				Logger.fatal(e);
			} finally {
				super.unlockSingle();
			}
			// 处理命令
			if (cmd != null) {
				press(cmd);
			}
		}

		Logger.info(this, "process", "exit...");
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		array.clear();
	}

	/**
	 * 保存一个串行命令。串行命令判断在BankCommandPool。
	 * @param cmd 串行命令
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Command cmd) {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			if (cmd != null) {
				success = array.add(cmd);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 唤醒线程
		if (success) {
			wakeup();
		}
		return success;
	}

	/**
	 * 生成本地转发命令，逐一处理。
	 * @param cmd 异步命令
	 */
	private void press(Command cmd) {
		ShiftCommand shift = null;
		CommandHook hook = null;
		
		// 数据库名，具有全网唯一性。为了保证这个唯一性，采用串行处理。（会降低效率，但是保证了质量）
		if (cmd.getClass() == CreateSchema.class) {
			hook = new CreateSchemaHook();
			shift = new ShiftCreateSchema((CreateSchema) cmd, (CreateSchemaHook) hook);
		}

		// 没有匹配的转发命令
		if (shift == null) {
			fault(cmd, Minor.UNSUPPORT);
			return;
		}

		// 保存到本地
		boolean success = getLauncher().getCommandPool().admit(shift);
		// 不成功，通知请求方
		if (!success) {
			fault(cmd, Minor.IMPLEMENT_FAILED);
			return;
		}

		// 继续等待直到反馈结果
		hook.await();
		
	}

	/**
	 * 向命令来源发送处理错误通知
	 * @param cmd 来源命令
	 * @param minor 次级错误码
	 */
	private boolean fault(Command cmd, short minor) {
		// 如果不需要反馈，直接退出
		if (cmd.isDirect()) {
			Logger.error(this, "unsupport", "cannot be support '%s'", cmd);
			return false;
		}
		Cabin endpoint = cmd.getSource();
		// 没有定义回显地址，这是一个本地命令，不需要处理
		if (endpoint == null) {
			return false;
		}
		// 发送错误提示到目标站点
		EchoClient client = ClientCreator.createEchoClient(endpoint);
		if (client == null) {
			return false;
		}
		// 生成报头，发送给请求端
		EchoHead head = new EchoHead(Major.FAULTED, minor, 0);
		boolean success = client.shoot(head);
		// 关闭连接
		client.destroy();
		return success;
	}
}
