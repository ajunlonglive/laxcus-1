/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.mix.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 子站点节点最小磁盘空间限制调用器
 * 
 * @author scott.liang
 * @version 1.0 8/18/2019
 * @since laxcus 1.0
 */
public class SubLeastDiskInvoker extends CommonInvoker {

	/**
	 * 构造子站点节点最小磁盘空间限制调用器，指定命令
	 * @param cmd 节点最小磁盘空间限制命令
	 */
	public SubLeastDiskInvoker(LeastDisk cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public LeastDisk getCommand() {
		return (LeastDisk) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		LeastDisk cmd = getCommand();
		Node from = cmd.getSourceSite();
		Node hub = getHub();

		// 命令必须来自管理站点
		boolean success = (Laxkit.compareTo(from, hub) == 0);
		// 根据命令，选择设置节点最小磁盘空间限制
		if (success) {
			success = reset();
		}

		// 要求反馈结果时...
		if (cmd.isReply()) {
			LeastDiskProduct product = new LeastDiskProduct();
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
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 重置节点最小磁盘空间限制
	 */
	private boolean reset() {
		LeastDisk cmd = getCommand();

		boolean success = true;
		if (isLinux()) {
			if (cmd.hasPaths()) {
				success = LinuxDevice.getInstance().updateLeastPaths(cmd.getPaths());
			} else {
				if (cmd.isUnlimit()) {
					LinuxDevice.getInstance().setDiskUnlimit();
				} else {
					LinuxDevice.getInstance().setDiskLeast(cmd.getCapacity());
					LinuxDevice.getInstance().setDiskLeastRate(cmd.getRate());
				}
			}
			// 重新检测
			LinuxDevice.getInstance().checkDisk();
		} else if (isWindows()) {
			if (cmd.hasPaths()) {
				success = WindowsDevice.getInstance().updateLeastPaths(cmd.getPaths());
			} else {
				if (cmd.isUnlimit()) {
					WindowsDevice.getInstance().setDiskUnlimit();
				} else {
					WindowsDevice.getInstance()
							.setDiskLeast(cmd.getCapacity());
					WindowsDevice.getInstance().setDiskLeastRate(cmd.getRate());
				}
			}
			// 重新检测
			WindowsDevice.getInstance().checkDisk();
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
