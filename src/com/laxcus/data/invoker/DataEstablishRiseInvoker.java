/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.io.*;

import com.laxcus.data.pool.*;
import com.laxcus.distribute.establish.command.*;
import com.laxcus.distribute.establish.session.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.rise.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * ESTABLISH.RISE命令调用器 <br><br>
 * 
 * 在DATA站点上，RISE阶段负责从指定的BUILD站点下载数据，在本地更新，生成RISE处理报告返回
 * 
 * @author scott.liang
 * @version 1.2 9/12/2015
 * @since laxcus 1.0
 */
public class DataEstablishRiseInvoker extends DataInvoker {

	/** 磁盘文件名 **/
	private File file;

	/** 分布任务组件实例 **/
	private RiseTask riseTask;
	
	/**
	 * 构造RISE命令调用器，指定命令
	 * @param cmd RISE命令
	 */
	public DataEstablishRiseInvoker(RiseStep cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RiseStep getCommand() {
		return (RiseStep) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RiseStep cmd = getCommand();

		// 根据命名，分配一个SCAN阶段任务实例
		RiseSession session = cmd.getSession();
		Phase phase = session.getPhase();
		// 获得SCAN阶段任务实例
		riseTask = RiseTaskPool.getInstance().create(phase);
		boolean success = (riseTask != null);
		// 不成功，返回一个错误报告
		if (!success) {
			Logger.error(this, "launch", "cannot find %s", phase);
			super.replyFault(Major.FAULTED, Minor.NOTFOUND);
			return false;
		}

		// 设置命令
		riseTask.setCommand(cmd);
		// 设置调用器编号
		riseTask.setInvokerId(getInvokerId());
		
		// 执行操作
		success = convert();
		
		// 更新索引和缓存映像数据块
		if (success) {
			StaffOnDataPool.getInstance().reloadIndex();
			StaffOnDataPool.getInstance().reloadCacheReflexStub();
		}

		Logger.debug(this, "launch", success, "replace is");

		// 退出
		return useful(success);
	}
	
	/**
	 * 建立一个本地文件，输出的数据可以选择写到磁盘上
	 */
	private File createDiskFile() {
		String name = String.format("%d.rise", getInvokerId());
		File root = RiseManager.getInstance().getRoot();
		return new File(root, name);
	}
	
	/**
	 * 更新数据
	 * @return
	 */
	private boolean convert( ) {
		DefaultEchoHelp help = null;
		
		boolean ondisk = isDisk();
		file = (ondisk ? createDiskFile() : null);
		boolean success = false;
		try {
			if (ondisk) {
				riseTask.convertTo(file);
//				success = doLargeTransfer(file);
				success = replyFile(file);
			} else {
				byte[] b = riseTask.convert();
//				success = doLargeTransfer(b);
				success = replyPrimitive(b);
			}
		} catch (TaskException e) {
			Logger.error(e);
			help = new DefaultEchoHelp (Laxkit.printThrowable(e));
		} catch (Throwable e) {
			Logger.fatal(e);
			help = new DefaultEchoHelp(Laxkit.printThrowable(e));
		}
		// 如果失败
		if (!success) {
			EchoCode code = new EchoCode(Major.FAULTED, Minor.IMPLEMENT_FAILED);
			super.replyFault(code, help);
		}
		return success;
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
	 * 执行远程协调操作
	 * @param quest 协商请求指令
	 * @return 返回协调结果，没有定义是空指针
	 */
	public TalkReply ask(TalkQuest quest) {
		if (riseTask != null) {
			return riseTask.talk(quest);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		// 释放组件实例
		if (riseTask != null) {
			riseTask.destroy();
			riseTask = null;
		}
		// 调用上级
		super.destroy();
	}
}