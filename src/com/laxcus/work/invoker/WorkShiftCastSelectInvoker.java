/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import java.io.*;

import com.laxcus.command.access.cast.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;

/**
 * CastSelect转发命令调用器
 * 
 * @author scott.liang
 * @version 1.0 06/22/2013
 * @since laxcus 1.0
 */
public class WorkShiftCastSelectInvoker extends WorkInvoker {

	/**
	 * 构造CastSelect转发命令调用器
	 * @param cmd ShiftCastSelect命令
	 */
	public WorkShiftCastSelectInvoker(ShiftCastSelect cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftCastSelect getCommand() {
		return (ShiftCastSelect) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftCastSelect shift = getCommand();
		Node hub = shift.getHub();
		CastSelect cmd = shift.getCommand();

		//		CommandItem item = new CommandItem(hub, cmd);
		// 发送到DATA站点，应答数据选择保存到内存/磁盘
		boolean success = completeTo(hub, cmd); // completeTo(item);
		if (!success) {
			CastSelectHook hook = shift.getHook();
			hook.setFault(new EchoException("send failed! to %s", hub));
		}

		Logger.debug(this, "launch", success, "send to %s", hub);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftCastSelect shift = getCommand();
		CastSelectHook hook = shift.getHook();
		// 处理失败
		if (isFaultCompleted()) {
			hook.setFault(new EchoException("receive failed"));
			return false;
		}

		EchoBuffer buf = findBuffer(0);

		Logger.debug(this, "ending", "%d echo data at '%s'",  super.getInvokerId(), (buf.isDisk() ? "DISK" : "MEMORY"));

		boolean success = false;
		// 判断文件在磁盘或者内存，选择相应的处理方式
		try {
			byte[] b = null;
			if (buf.isDisk()) {
				b = readFile(buf.getFile());
			} else {
				b = buf.readFullMemory();
			}
			hook.setResult(b);
			success = true;
		} catch (IOException e) {
			hook.setFault(e);
		} catch (Throwable e) {
			hook.setFault(e);
		}

		Logger.debug(this, "ending", success, "%d result is", getInvokerId());

		return useful(success);
	}

	/**
	 * 从磁盘文件读数据
	 * @param file - 磁盘文件名
	 * @return - 返回文件中的字节数组
	 * @throws IOException
	 */
	private byte[] readFile(File file) throws IOException {
		// 文件长度
		long length = file.length();
		// 文件零长度，没有找到数据，属于正常情况。
		if (length == 0) {
			return null;
		} else {
			// 从磁盘读文件
			byte[] b = new byte[(int) length];
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
			return b;
		}
	}

}