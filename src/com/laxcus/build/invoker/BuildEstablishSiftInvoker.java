/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.access.stub.index.*;
import com.laxcus.build.pool.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.distribute.calculate.command.*;
import com.laxcus.distribute.establish.command.*;
import com.laxcus.distribute.establish.mid.*;
import com.laxcus.distribute.establish.session.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.sift.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * ESTABLISH.SIFT命令调用器 <br>
 * 
 * 命令来自CALL站点，BUILD站点执行数据构建最重要的SIFT阶段工作。
 * 
 * @author scott.liang
 * @version 1.1 5/23/2014
 * @since laxcus 1.0
 */
public class BuildEstablishSiftInvoker extends EchoInvoker {

	/** SIFT任务组件实例 **/
	private SiftTask siftTask;

	/** 元数据的磁盘文件 **/
	private File file;

	/**
	 * 构造ESTABLISH.SIFT命令调用器，指定它的命令
	 * @param cmd - SIFT命令
	 */
	public BuildEstablishSiftInvoker(SiftStep cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SiftStep getCommand() {
		return (SiftStep) super.getCommand();
	}

	/**
	 * 返回SIFT阶段会话
	 * @return
	 */
	private SiftSession getSession() {
		return getCommand().getSession();
	}

	/**
	 * 建立一个本地文件，输出的数据可以选择写到磁盘上
	 */
	private File createDiskFile() {
		String name = String.format("%d.sift", getInvokerId());
		File root = SiftManager.getInstance().getRoot();
		return new File(root, name);
	}


	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SiftStep cmd = getCommand();

		// 获得SCAN阶段任务实例
		SiftSession session = cmd.getSession();
		Phase phase = session.getPhase();
		siftTask = SiftTaskPool.getInstance().create(phase);
		boolean success = (siftTask != null);
		// 没有找到匹配的任务组件
		if (!success) {
			Logger.error(this, "launch", "cannot find %s", phase);
			super.replyFault(Major.FAULTED, Minor.TASK_NOTFOUND);
			return useful(false);
		}
		// 设置命令
		siftTask.setCommand(cmd);
		// 设置调用器编号
		siftTask.setInvokerId(getInvokerId());

		DefaultEchoHelp help = null;
		// 调用器完成的工作：
		// 1. 根据数据表名，检查数据表名，和在磁盘上建表目录
		try {
			success = this.doCreateSpace();
			// 2. 下载指定节点下的数据块，保存到本地的指定目录下，同时加载它
			if (success) {
				success = this.doDownload();
			}
		} catch (TaskException e) {
			Logger.error(e);
			success = false;
		}

		// 3. 执行用户的SIFT数据操作
		if (success) {
			boolean ondisk = super.isDisk();
			file = (ondisk ? createDiskFile() : null);
			try {
				if (ondisk) {
					siftTask.implementTo(file);
//					success = doLargeTransfer(file); // 超级传输
					success = this.replyFile(file); // 发送文件
				} else {
					byte[] b = siftTask.implement();
//					success = doLargeTransfer(b); // 超级传输
					success = this.replyPrimitive(b); // 发送数据
				}
			} catch (TaskException e) {
				Logger.error(e);
				help = new DefaultEchoHelp(Laxkit.printThrowable(e));
			} catch (Throwable e) {
				Logger.fatal(e);
				help = new DefaultEchoHelp(Laxkit.printThrowable(e));
			}
		}

		// 以上不成功，发送通知
		if(!success) {
			EchoCode code = new EchoCode(Major.FAULTED, Minor.IMPLEMENT_FAILED);
			super.replyFault(code, help);
		}

		//		// 3. 执行用户的SIFT数据操作
		//		SiftArea area = null;
		//		if (success) {
		//			try {
		//				area = task.launch();
		//			} catch (TaskException e) {
		//				Logger.error(e);
		//			} catch (Throwable e) {
		//				Logger.fatal(e);
		//			}
		//		}

		//		// 判断最后成功
		//		success = (area != null);
		//
		//		// 返回处理结果
		//		if (success) {
		//			// 指定 数据源地址
		//			if (area.getSource() == null) {
		//				try {
		//					area.setSource(task.getSiftTrustor().getLocal(super.getInvokerId()));
		//				} catch (TaskException e) {
		//					Logger.error(e);
		//					success = false;
		//				}
		//			}
		//			// 发送对象
		//			if (success) {
		//				success = super.replyObject(area);
		//			}
		//		} else {
		//			super.replyFault();
		//		}

		Logger.debug(this, "launch", success, "result is");

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
	 * 建立表空间
	 * @return
	 */
	private boolean doCreateSpace() throws TaskException {
		SiftSession session = getSession();
		Set<Space> list = session.getSpaces();

		// SIFT业务代理
		SiftTrustor trustor = siftTask.getSiftTrustor();
		long invokerId = getInvokerId();
//		Siger issuer = this.getIssuer();

		// 判断表空间签名有效
		for (Space space : list) {
			// 判断签名许可
			boolean success = trustor.allow(invokerId, space);
			if (!success) {
				Logger.error(this, "doCreateSpace", "system refuse '%s'", space);
				return false;
			}
			// 如果存在是错误
			success = trustor.hasDiskSpace(invokerId, space);
			if (success) {
				Logger.error(this, "doCreateSpace", "cannot find %s", space);
				return false;
			}
		}

		ArrayList<Space> array = new ArrayList<Space>();
		// 建立磁盘空间
		for(Space space : list) {
			boolean success = trustor.createDiskSpace(invokerId, space);
			Logger.note(this, "doCreateSpace", success, "create '%s'", space);
			// 不成功退出，否则保存
			if (!success) {
				break;
			}
			// 保存
			array.add(space);
		}

		boolean success = (array.size() == list.size());
		// 不成功，删除磁盘空间
		if (!success) {
			for (Space space : array) {
				boolean ret = trustor.deleteDiskSpace(invokerId, space);
				Logger.debug(this, "doCreateSpace", ret, "delete '%s'", space);
			}
		}

		return success;
	}

	/**
	 * 下载数据块，保存到本地，同时加载到磁盘上
	 * @return
	 */
	private boolean doDownload() throws TaskException {
		long invokerId = super.getInvokerId();
		SiftSession session = getSession();

		List<SiftHead> list = session.list();
		SiftTrustor trustor = siftTask.getSiftTrustor();

		ArrayList<ShiftDownloadMass> array = new ArrayList<ShiftDownloadMass>();

		// 建立全部转发命令
		for (SiftHead head : list) {
			EstablishFlag flag = head.getFlag();

			Node hub = flag.getSource();
			Space space = flag.getSpace();

			for (StubItem item : head.getStubItems()) {
				long stub = item.getStub();

				// 生成文件存取路径
				final String path = trustor.doChunkFile(invokerId, space, stub);

				Logger.debug(this, "doDownload", "make '%s'", path);

				// 建立传输命令，要求采用流模式（保存到内存）
				StubFlag chunk = new StubFlag(space, stub);
				DownloadMass cmd = new DownloadMass(chunk);
				cmd.setMemory(true);
				// 转发命令
				DownloadMassHook hook = new DownloadMassHook();
				ShiftDownloadMass shift = new ShiftDownloadMass(hub, cmd, hook, path);
				array.add(shift);
			}
		}

		// 为减少轻计算机压力，每次只触发一个下载，直到完成
		for(int i = 0; i < array.size(); i++) {
			ShiftDownloadMass shift = array.get(i);
			boolean success = BuildCommandPool.getInstance().press(shift);
			// 若不成功就退出
			if(!success) {
				Logger.error(this, "doDownload", "press error");
				return false;
			}
			// 等待结果
			DownloadMassHook hook = shift.getHook();
			hook.await();
			// 不成功退出
			if (!hook.isSuccessful()) {
				Logger.error(this, "doDownload", "cannot be download");
				return false;
			}		
		}

		return true;
	}

	/**
	 * 执行远程协调操作
	 * @param quest 协商请求指令
	 * @return 返回协调结果，没有定义是空指针
	 */
	public TalkReply ask(TalkQuest quest) {
		if (siftTask != null) {
			return siftTask.talk(quest);
		}
		return null;
	}

	/**
	 * 如果是最后一次，删除缓存数据
	 */
	private void deleteFluxBuffer() {
		SiftStep step = getCommand();
		if (!step.isLast()) {
			return;
		}
		long taskId = siftTask.getTaskId();
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
		//	Logger.debug(this, "destroy", "%d usedtime:%d", getInvokerId(), getRunTime());

		// 释放组件实例
		if (siftTask != null) {
			deleteFluxBuffer();
			siftTask.destroy();
			siftTask = null;
		}
		// 释放文件
		if (file != null) {
			if (file.exists()) {
				file.delete();
			}
			file = null;
		}

		// 调用上级
		super.destroy();
	}

}