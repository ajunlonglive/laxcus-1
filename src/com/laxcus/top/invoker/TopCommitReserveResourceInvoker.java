/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.io.*;

import com.laxcus.command.reserve.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * CommitReserveResource命令调用器
 * 
 * CommitReserveResource从TOP/HOME的xxxDispatchReserveResourceInvoker发出，被TOP/HOME的监视站点接收，
 * 在接收之后，向TOP/HOME管理站点发出“TakeReserveResource”命令，要求TOP/HOME管理站点传输资源数据（对管理站点上传，本地是下载），
 * 
 * @author scott.liang
 * @version 1.0 7/21/2014
 * @since laxcus 1.0
 */
public class TopCommitReserveResourceInvoker extends TopInvoker {

	/**
	 * 构造CommitReserveResource命令调用器，指定命令
	 * @param cmd CommitReserveResource命令
	 */
	public TopCommitReserveResourceInvoker(CommitReserveResource cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CommitReserveResource getCommand() {
		return (CommitReserveResource) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CommitReserveResource cmd = getCommand();

		// 取得TOP管理站点地址
		Node node = cmd.getSource().getNode();
		TakeReserveResource take = new TakeReserveResource(cmd.getResource());

		// 数据写入硬盘
		boolean success = launchTo(node, take);

		Logger.debug(this, "launch", success, "send %s to %s",
				cmd.getResource(), node);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		// 如果失败，退出
		if (isFaultCompleted(index)) {
			Logger.error(this, "ending", "cannot be take ReserveResource!");
			return false;
		}

		// 判断数据在磁盘或者内存
		boolean ondisk = isEchoFiles();
		// 显示信息
		boolean success = false;
		try {
			if (ondisk) {
				File file = findFile(getFlag());
				success = move(file);
			} else {
				byte[] b = collect();
				if (!Laxkit.isEmpty(b)) {
					success = write(b);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		Logger.note(this, "ending", success, "take ReserveResource!");

		return useful(success);
	}
	
	/**
	 * 生成目标文件
	 * @return
	 */
	private File createTargetFile() {
		CommitReserveResource cmd = getCommand();
		// 原文件路径
		File file = new File(cmd.getResource());
		// 取出名称，换成本地的文件路径
		String name = file.getName();
		// 换成本地文件
		return getLauncher().createResourceFile(name);
	}

	/**
	 * 数据写入磁盘文件
	 * @param b
	 */
	private boolean write(byte[] b) {
		File target = createTargetFile();
		return writeContent(target, b);
	}

	/**
	 * 源文件，移动到指定目标下
	 * @param source 源文件
	 * @return 成功返回真，否则假
	 */
	private boolean move(File source) {
		// 保存在回显目录中的文件
		if (source == null) {
			Logger.error(this, "move", "cannot be find echo file");
			return false;
		}

		// 把文件移到指定目录下
		File target = createTargetFile();
		return moveTo(source, target);
	}
	
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
//	 */
//	@Override
//	public boolean ending() {
//		int index = findEchoKey(0); // getEchoKeys().get(0);
//		// 如果失败，退出
//		if (isFaultCompleted(index)) {
//			Logger.error(this, "ending", "failed");
//			return false;
//		}
//
//		// 保存在回显目录中的文件
//		File source = findFile(index);
//		if (source == null) {
//			Logger.error(this, "ending", "cannot be find echo file, index:%d", index);
//			return false;
//		}
//
//		CommitReserveResource cmd = getCommand();
//		// 原文件路径
//		File target = new File(cmd.getResource());
//
//		
//		// 取出名称，换成本地的文件路径
//		String name = target.getName();
//		target = getLauncher().createResourceFile(name);
//
//		// 把文件移到指定目录下
//		boolean success = (target != null);
//		if (success) {
//			success = move(source, target);
//		}
//
//		Logger.debug(this, "ending", success, "%s move to %s", source, target);
//
//		return useful(success);
//	}
//
//	/**
//	 * 移动磁盘文件
//	 * @param source 源文件
//	 * @param target 临时目标文件
//	 * @return 成功返回真，否则假
//	 */
//	private boolean move(File source, File target) {
//		long seek = 0L;
//		long length = source.length();
//		byte[] b = new byte[10240];
//
//		try {
//			FileInputStream in = new FileInputStream(source);
//			FileOutputStream out = new FileOutputStream(target);
//			while (seek < length) {
//				// 从文件中读数据
//				int size = in.read(b, 0, b.length);
//				if (size < 1) break;
//				// 写数据到目录文件
//				out.write(b, 0, size);
//				seek += size;
//			}
//			in.close();
//			out.close();
//		} catch (IOException e) {
//			Logger.error(e);
//		}
//
//		// 判断成功
//		boolean success = (seek == length);
//		Logger.debug(this, "move", success, "%s move to %s, length compare: %d - %d", 
//				source, target, seek, length);
//
//		return success;
//	}

}