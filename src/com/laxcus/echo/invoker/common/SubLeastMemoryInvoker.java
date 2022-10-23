/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.io.*;

import com.laxcus.command.mix.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 子站点节点最小内存限制调用器
 * 
 * @author scott.liang
 * @version 1.0 8/8/2019
 * @since laxcus 1.0
 */
public class SubLeastMemoryInvoker extends CommonInvoker {

	/**
	 * 构造子站点节点最小内存限制调用器，指定命令
	 * @param cmd 节点最小内存限制命令
	 */
	public SubLeastMemoryInvoker(LeastMemory cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public LeastMemory getCommand() {
		return (LeastMemory) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		LeastMemory cmd = getCommand();
		Node from = cmd.getSourceSite();
		Node hub = getHub();

		// 命令必须来自管理站点
		boolean success = (Laxkit.compareTo(from, hub) == 0);
		// 根据命令，选择设置节点最小内存限制
		if (success) {
			success = reset();
		}

		// 要求反馈结果时...
		if (cmd.isReply()) {
			LeastMemoryProduct product = new LeastMemoryProduct();
			product.add(getLocal(), success);
			replyProduct(product);
		}

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
	 * 重置节点最小内存限制
	 */
	private boolean reset() {
		LeastMemory cmd = getCommand();

		boolean success = true;
		if (isLinux()) {
			if (cmd.isUnlimit()) {
				LinuxDevice.getInstance().setMemoryUnlimit();
			} else {
				LinuxDevice.getInstance().setMemoryLeast(cmd.getCapacity());
				LinuxDevice.getInstance().setMemoryLeastRate(cmd.getRate());
			}
			// 重新检测
			try {
				LinuxDevice.getInstance().checkMemory();
			} catch (IOException e) {
				Logger.error(e);
			}
		} else if (isWindows()) {
			if (cmd.isUnlimit()) {
				WindowsDevice.getInstance().setMemoryUnlimit();
			} else {
				WindowsDevice.getInstance().setMemoryLeast(cmd.getCapacity());
				WindowsDevice.getInstance().setMemoryLeastRate(cmd.getRate());
			}
			// 重新检测
			WindowsDevice.getInstance().checkMemory();
		} else {
			success = false; // 无确定！
		}
		
		// 重新注册到管理节点
		if (success) {
			getLauncher().checkin(false);
		}

		return success;
	}
}
