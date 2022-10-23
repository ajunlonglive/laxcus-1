/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.io.*;

import com.laxcus.command.reserve.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.schedule.*;

/**
 * TakeReserveResource命令调用器
 * 
 * 这个调用器运行在HOME管理站点，向HOME镜像站点发送资源数据文件。
 * 
 * @author scott.liang
 * @version 1.0 7/21/2014
 * @since laxcus 1.0
 */
public class HomeTakeReserveResourceInvoker extends HomeInvoker implements SerialSchedule {
	
	/** 获得对资源的锁定 **/
	private boolean attached = false;

	/**
	 * 构造TakeReserveResource命令调用器，指定命令
	 * @param cmd - TakeReserveResource命令
	 */
	public HomeTakeReserveResourceInvoker(TakeReserveResource cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeReserveResource getCommand() {
		return (TakeReserveResource) super.getCommand();
	}

	/**
	 * 上传文件
	 * @param file - 磁盘文件
	 * @return - 成功返回真，否则假
	 */
	private boolean upload(File file) {
		boolean success = replyFile(file);

		Logger.debug(this, "upload", success, "upload %s", file);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeReserveResource cmd = getCommand();

		// 检查磁盘上的文件
		String filename = cmd.getResource();
		File file = new File(filename);
		boolean success = (file.exists() && file.isFile());
		// 如果不存在时，发送一个错误
		if (!success) {
			replyFault(Major.FAULTED, Minor.NOTFOUND);
			return false;
		}
		
		// 锁定这个文件
		lock();
		// 上传文件
		success = upload(file);

		// 退出
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 锁定资源，直到获得它
	 */
	private void lock() {
		TakeReserveResource cmd = getCommand();
		String resource = cmd.getResource();
		// 申请串处理工作的权利（每次只有一个线程获得）
		attached = SerialSchedulePool.getInstance().admit(resource, this);
		// 如果没有马上锁定文件，将一直等待，直到串行工作管理池通知得到锁定
		while (!attached) {
			delay(1000);
		}
	}

	/**
	 * 解除对资源的锁定
	 */
	private boolean unlock() {
		TakeReserveResource cmd = getCommand();
		String resource = cmd.getResource();
		return SerialSchedulePool.getInstance().release(resource, this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		// 在调用器生效情况下，解除锁定
		if (isAlive()) {
			unlock();
		}
		// 调用上级释放
		super.destroy();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.schedule.SerialSchedule#attach()
	 */
	@Override
	public void attach() {
		// 串行工作管理池通知，申请的资源已经获得锁定
		attached = true;
		wakeup();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.schedule.SerialSchedule#isAttached()
	 */
	@Override
	public boolean isAttached() {
		return attached;
	}

}