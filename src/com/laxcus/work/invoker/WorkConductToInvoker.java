/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.distribute.calculate.command.*;
import com.laxcus.distribute.calculate.cyber.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.command.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.flux.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.naming.*;

/**
 * CONDUCT/TO命令调用器 <br>
 * 
 * 按照CONDUCT规范，TO阶段执行DIFFUSE/CONVERGE分布计算中最重要的数据计算工作。这是DIFFUSE/CONVERGE分布计算的核心。
 * 
 * @author scott.liang
 * @version 1.1 5/23/2014
 * @since laxcus 1.0
 */
public class WorkConductToInvoker extends WorkInvoker {

	/** 分配的任务实例 **/
	private ToTask toTask;

	/** 数据被写入的磁盘文件 **/
	private File disk;

	/**
	 * 构造CONDUCT/TO命令调用器，指定它的命令
	 * @param cmd 异步操作命令
	 */
	public WorkConductToInvoker(ToStep cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ToStep getCommand() {
		return (ToStep) super.getCommand();
	}

	/**
	 * 判断长度超出限制
	 * @param size 指定长度
	 * @return 返回真或者假
	 */
	private boolean isMaxSize(long size) {
		return size >= FluxTrustorPool.getInstance().getMemberMemorySize();
	}

	/**
	 * 建立一个本地文件，输出的数据可以选择写到磁盘上。
	 */
	private void createDiskFile() {
		String name = String.format("%d.middle", super.getInvokerId());
		File root = FluxTrustorPool.getInstance().getRoot();
		disk = new File(root, name);
	}

	/**
	 * 返回TO阶段会话
	 * @return ToSession
	 */
	private ToSession getSession() {
		ToStep cmd = getCommand();
		return cmd.getSession();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ToStep cmd = getCommand();
		ToSession session = cmd.getSession();
		Phase phase = session.getPhase();

		Logger.debug(this, "launch", "this is %s, thread id : %d", phase, getThreadId());

		// 建立一个本地文件，以待以后使用
		createDiskFile();

		// 1. 根据阶段命名，查找TO阶段任务实例
		toTask = ToTaskPool.getInstance().create(phase);
		boolean success = (toTask != null);
		if (!success) {
			Logger.error(this, "launch", "cannot find '%s'", phase);
			// 发送错误的异步应答，然后退出
			replyFault();
			return false;
		}
		// 设置命令
		toTask.setCommand(cmd);
		// 设置调用器编号
		toTask.setInvokerId(getInvokerId());

		/**
		 * 启动数据业务。
		 * 如果是“产生数据”的业务，分两种：<1>有SELECT，向其它站点请求，然后进入等待。<2>直接产生，反馈异步应答。<br>
		 * 如果是“计算数据”的业务，需要向其它站点发送命令请求后退出，然后等待返回数据，进入“ending”方法处理。<br>
		 */
		success = false;
		try {
			if (toTask.isGenerate()) {
				success = generate();
			} else if (toTask.isEvaluate()) {
				success = download();
			} else {
				throw new TaskException("illegal task by %s", toTask.getClass().getName());
			}
		} catch (TaskException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		if (!success) {
			super.replyFault();
		}

		Logger.debug(this, "launch", success, "thread id : %d, result is", getThreadId());

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		Logger.debug(this, "edning", "current thread id : %d", getThreadId());

		boolean success = false;
		// 在收到产生的数据后，计算数据，返回结果
		try {
			success = evaluate();		
		} catch (TaskException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 发送错误通知
		if (!success) {
			replyFault();
		}

		Logger.debug(this, "ending", success, "current thread id: %d, result is", getThreadId());

		return useful(success);
	}

	/**
	 * 在WORK站点产生数据（在WORK站点向DATA站点检索数据或者其它）
	 * @return 成功返回真，否则假
	 * @throws TaskException
	 */
	private boolean generate() throws TaskException {
		// 根据配置中的要求“产生/分割”数据，返回元数据字节数组长度
		long length = ((ToGenerateTask) toTask).divide();
		// 出错，返回假
		if (length < 1) {
			return false;
		}

		boolean success = false;
		if (isMaxSize(length) || getCommand().isDisk()) {
			// 指定一个文件目录
			toTask.flushTo(disk);
			success = replyFile(disk);
		} else {
			// 生成结果
			byte[] data = toTask.effuse();
			// 发送到请求端
			success = replyPrimitive(data);
		}
		// 工作完成，退出。返回处理结果
		return useful(success);
	}

	/**
	 * 以异步方式，从目标站点下载数据。
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean download() {
		Logger.debug(this, "download", "todo");

		ToSession session = getSession();
		CyberSphere sphere = session.getSphere();
		if (sphere == null || sphere.isEmpty()) {
			Logger.error(this, "download", "illegal CyberSphere by %s", session.getPhase());
			return false;
		}

		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for(CyberArea area : sphere.list()) {
			for(CyberField field : area.list()) {

				Logger.debug(this, "download", "this is '%s'", field);

				// 建立命令
				TakeFluxData cmd = new TakeFluxData(field.getTaskId(), field.getField());
				// 建立异步传输单元
				Node hub = field.getNode();
				CommandItem item = new CommandItem(hub, cmd);
				array.add(item);
			}
		}
		int size = array.size();

		Logger.debug(this, "download", "field size is:%d", size);

		// 发送到目标站点。下载数据写入磁盘
		boolean success = completeTo(array);

		Logger.debug(this, "download", success, "field size is:%d", size);

		return success;
	}

	/**
	 * 在下载完成后，通知服务端释放中间数据
	 */
	private void release() {
		ToSession session = getSession();
		CyberSphere sphere = session.getSphere();
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for(CyberArea area : sphere.list()) {
			for(CyberField field : area.list()) {
				Node hub = field.getNode();
				long taskId = field.getTaskId();
				long mod = field.getMod();

				// 建立释放数据命令，服务器不必发送应答
				ReleaseFluxField cmd = new ReleaseFluxField(taskId, mod);
				cmd.setDirect(true);
				// 生成发送单元
				CommandItem item = new CommandItem(hub, cmd);
				array.add(item);
			}
		}
		int size = array.size();

		Logger.debug(this, "release", "command size is:%d", size );

		// 以“容错”模式，投递命令给服务器
		size = directTo(array, false);

		Logger.debug(this, "release", "send successful size is:%d", size );
	}

	/**
	 * 在获取数据后，执行数据计算操作。<br>
	 * 这是每个“计算”模式的TO任务最后一步。此方法完成后，TO阶段工作将退出运行线程。
	 * 
	 * @return 成功返回“真”，否则“假”。
	 * @throws TaskException
	 * @throws IOException
	 */
	private boolean evaluate() throws TaskException, IOException {
		Logger.debug(this, "evaluate", "into...");

		// 如果要求服务端释放数据时，通知服务端，释放数据
		ToSession session = getSession();
		if (session.isAutoRelease()) {
			release();
		}

		// 不成功时...
		if (isFaultCompleted()) {
			Logger.error(this, "evaluate", "receive failed");
			return false;
		}

		List<Integer> keys = getEchoKeys();

		Logger.debug(this, "evaluate", "index size is %d", keys.size());

		for(int index : keys) {
			EchoBuffer ef = findBuffer(index);
			TakeFluxData cmd = (TakeFluxData) ef.getCommand();
			FluxField field = cmd.getField();

			boolean success = false;
			if (ef.isDisk()) {
				File file = ef.getFile();
				success = ((ToEvaluateTask) toTask).evaluate(field, file);
			} else {
				byte[] b = ef.getMemory();
				success = ((ToEvaluateTask) toTask).evaluate(field, b, 0, b.length);
			}

			Logger.debug(this, "evaluate", success, "index:%d, ondisk is:'%s', flux field:'%s'",
					index, ef.isDisk(), field);
		}

		// 汇总数据
		long length = ((ToEvaluateTask) toTask).assemble();

		Logger.debug(this, "evaluate", "assemble size:%d", length);

		boolean success = false;
		// 超过长度限制，数据写入磁盘；否则，在内存中交换
		if (isMaxSize(length) || getCommand().isDisk()) {
			toTask.flushTo(disk);
			success = replyFile(disk);
		} else {
			byte[] data = toTask.effuse();
			success = replyPrimitive(data);
		}

		Logger.debug(this, "evaluate", success, "send to %s", super.getCommandSource());

		// 全过程处理完成
		return useful(success);
	}

	/**
	 * 执行远程协调操作
	 * @param quest 协商请求指令
	 * @return 返回协调结果，没有定义是空指针
	 */
	public TalkReply ask(TalkQuest quest) {
		if (toTask != null) {
			return toTask.talk(quest);
		}
		return null;
	}

	/**
	 * 如果是最后一次，删除缓存数据
	 */
	private void deleteFluxBuffer() {
		ToStep step = getCommand();
		if (!step.isLast()) {
			return;
		}
		long taskId = toTask.getTaskId();
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

		// 释放组件
		if (toTask != null) {
			deleteFluxBuffer();
			toTask.destroy();
			toTask = null;
		}
		// 磁盘内容存在，删除它
		if (disk != null) {
			if (disk.exists()) {
				disk.delete();
			}
			disk = null;
		}
		
		// 销毁上级实例
		super.destroy();
	}

}