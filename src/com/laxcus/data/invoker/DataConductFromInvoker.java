/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.io.*;

import com.laxcus.distribute.calculate.command.*;
import com.laxcus.distribute.conduct.command.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.from.*;
import com.laxcus.task.flux.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.naming.*;

/**
 * CONDUCT.FROM命令调用器。<br>
 * 
 * 处理DIFFUSE阶段在DATA站点的任务请求。 
 * 
 * @author scott.liang
 * @version 1.2 9/23/2013
 * @since laxcus 1.0
 */
public class DataConductFromInvoker extends DataInvoker {

	/** 数据被写入的磁盘文件 **/
	private File disk;

	/** 分布任务组件 **/
	private FromTask fromTask;

	/**
	 * 构造CONDUCT.FORM命令调用器，指定命令
	 * @param cmd CONDUCT.FROM命令
	 */
	public DataConductFromInvoker(FromStep cmd) {
		super(cmd);
	}

	/**
	 * 判断长度超出限制
	 * @param size
	 * @return 返回真或者假
	 */
	private boolean isMaxSize(long size) {
		return size >= FluxTrustorPool.getInstance().getMemberMemorySize();
	}

	/**
	 * 建立一个本地文件，输出的数据可以选择写到磁盘上
	 */
	private void createDiskFile() {
		String name = String.format("%d.middle", getInvokerId());
		File root = FluxTrustorPool.getInstance().getRoot();
		disk = new File(root, name);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FromStep getCommand() {
		return (FromStep) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 生成本地文件名（不产生磁盘文件），准备后续使用
		createDiskFile();

		FromStep cmd = getCommand();
		FromSession session = cmd.getSession();
		
		Logger.debug(this, "launch", "remote:%s, buddies size:%d", session.getRemote(), session.getBuddies().size());

		// 1. 根据阶段命名，查找FROM阶段任务实例
		Phase phase = session.getPhase();
		fromTask = FromTaskPool.getInstance().create(phase);
		boolean success = (fromTask != null);
		// 如果没有找到，向CALL站点发送错误提示
		if (!success) {
			Logger.error(this, "launch", "cannot be find \"%s\"", phase);
			replyFault(Major.FAULTED, Minor.TASK_NOTFOUND);
			return false;
		}
		// 设置命令
		fromTask.setCommand(cmd);
		// 设置调用器编号
		fromTask.setInvokerId(getInvokerId());

		success = false;
		try {
			success = todo();
		} catch (TaskException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		if (!success) {
			replyFault(Major.FAULTED, Minor.CONDUCT_ERROR);
		}

		Logger.debug(this, "launch", success, "result is");

		return useful(success);
	}

	/**
	 * 执行CONDUCT.FROM阶段数据处理
	 * @return 成功返回“真”，否则“假”。
	 * @throws TaskException, IOException
	 */
	private boolean todo() throws TaskException {
		// 用户按照自己的规则，执行数据分割。分割信息在FROM会话中
		long length = fromTask.divide();
		if (length < 1) {
			return false;
		}

		boolean success = false;
		// 1. 超过指定长度，FluxArea元数据写入磁盘；
		// 2. 以内存模式输出FluxArea
		if (isMaxSize(length)) {
			fromTask.flushTo(disk);
			success = replyFile(disk);
		} else {
			// 输入映像数据的字节数组
			byte[] b = fromTask.effuse();
			// 发送数据流
			success = replyPrimitive(b);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 执行远程协调操作
	 * @param quest 协商请求指令
	 * @return 返回协调结果，没有定义是空指针
	 */
	public TalkReply ask(TalkQuest quest) {
		if (fromTask != null) {
			return fromTask.talk(quest);
		}
		return null;
	}

	/**
	 * 如果是最后一次，删除缓存数据
	 */
	private void deleteFluxBuffer() {
		FromStep step = getCommand();
		if (!step.isLast()) {
			return;
		}
		long taskId = fromTask.getTaskId();
		// 通过异步调用器删除缓存数据，不返回结果
		ReleaseFluxArea sub = new ReleaseFluxArea(taskId);
		sub.setIssuer(getIssuer());
		getCommandPool().admit(sub);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		Logger.debug(this, "destroy", "%d usedtime:%d", getInvokerId(), getRunTime());

		// 释放组件句柄
		if (fromTask != null) {
			deleteFluxBuffer();
			fromTask.destroy();
			fromTask = null;
		}
		// 释放文件
		if (disk != null) {
			if (disk.exists()) {
				disk.delete();
			}
			disk = null;
		}

		// 调用上级类
		super.destroy();
	}

}