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
import com.laxcus.task.establish.scan.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * ESTABLISH.SCAN阶段调用器 <br><br>
 * 
 * 命令来自CALL站点，DATA主站点执行数据表索引扫描工作，然后把元数据返回给CALL节点。
 * 
 * @author scott.liang
 * @version 1.2 9/12/2015
 * @since laxcus 1.0
 */
public class DataEstablishScanInvoker extends DataInvoker {

	/** SCAN元数据的写入文件 **/
	private File file;

	/** 实例 **/
	private ScanTask scanTask;
	
	/**
	 * 构造ESTABLISH.SCAN阶段调用器，指定命令
	 * 
	 * @param cmd SCAN阶段命令
	 */
	public DataEstablishScanInvoker(ScanStep cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanStep getCommand() {
		return (ScanStep) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 必须是主节点
		if (!isMaster()) {
			super.replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}

		ScanStep cmd = getCommand();

		// 根据命名，分配一个SCAN阶段任务实例
		ScanSession session = cmd.getSession();
		Phase phase = session.getPhase();
		// 获得SCAN阶段任务实例
		scanTask = ScanTaskPool.getInstance().create(phase);
		boolean success = (scanTask != null);
		// 不成功，返回一个错误报告
		if (!success) {
			Logger.error(this, "launch", "cannot find %s", phase);
			super.replyFault(Major.FAULTED, Minor.NOTFOUND);
			return false;
		}
		// 设置命令
		scanTask.setCommand(cmd);
		// 设置调用器编号
		scanTask.setInvokerId(getInvokerId());

		// 执行操作
		success = scan();

		Logger.debug(this, "launch", success, "scan is");

		// 退出
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 建立一个本地文件，输出的数据可以选择写到磁盘上
	 */
	private File createDiskFile() {
		String name = String.format("%d.scan", getInvokerId());
		File root = ScanManager.getInstance().getRoot();
		return new File(root, name);
	}

	/**
	 * ESTAB.SCAN阶段在DATA节点上的处理。包括扫描磁盘上的表和发送处理结果。
	 */
	private boolean scan( ) {
		boolean ondisk = super.isDisk();
		file = (ondisk ? createDiskFile() : null);

		boolean success = false;
		DefaultEchoHelp help = null;
		try {
			if (ondisk) {
				scanTask.analyseTo(file);
//				success = doLargeTransfer(file);
				success = replyFile(file);
			} else {
				byte[] b = scanTask.analyse();
//				success = doLargeTransfer(b);
				success = replyPrimitive(b);
			}
		} catch (TaskException e) {
			Logger.error(e);
			help = new DefaultEchoHelp(Laxkit.printThrowable(e));
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

		//		// 通过扫描代理委托器，扫描磁盘上的数据块
		//		DefaultEchoHelp help = new DefaultEchoHelp();
		//		ScanArea area = null;
		//		try {
		//			area = task.scan();
		//		} catch (TaskException e) {
		//			Logger.error(e);
		//			help.setMessage(Laxkit.printThrowable(e));
		//		} catch (Throwable e) {
		//			Logger.fatal(e);
		//			help.setMessage(Laxkit.printThrowable(e));
		//		}
		//
		//		// 判断成功
		//		boolean success = (area != null);
		//		// 如果失败
		//		if (!success) {
		//			EchoCode code = new EchoCode(Major.FAULT, Minor.IMPLEMENT_FAILED);
		//			super.replyFault(code, help);
		//			return false;
		//		}
		//
		//		// 反馈参数给CALL站点
		//		success = super.replyObject(area);

		//		// debug code, begin
		//		byte[] b = area.build();
		//		try {
		//			java.io.FileOutputStream out = new java.io.FileOutputStream("/tracking/scan.bin");
		//			out.write(b);
		//			out.close();
		//		} catch (java.io.IOException e) {
		//			Logger.error(e);
		//		}
		//		// debug code, end

		//		return success;
	}
	
	/**
	 * 执行远程协调操作
	 * @param quest 协商请求指令
	 * @return 返回协调结果，没有定义是空指针
	 */
	public TalkReply ask(TalkQuest quest) {
		if (scanTask != null) {
			return scanTask.talk(quest);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		Logger.debug(this, "destroy", "%d usedtime:%d", getInvokerId(), getRunTime());

		// 释放组件实例
		if (scanTask != null) {
			scanTask.destroy();
			scanTask = null;
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