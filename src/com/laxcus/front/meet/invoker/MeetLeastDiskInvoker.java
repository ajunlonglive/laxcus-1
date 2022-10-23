/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 最少磁盘空间限制命令调用器。
 * 
 * 
 * @author scott.liang
 * @version 1.0 8/18/2019
 * @since laxcus 1.0
 */
public class MeetLeastDiskInvoker extends MeetInvoker {

	/**
	 * 构造最少磁盘空间限制命令调用器，指定命令
	 * @param cmd 最少磁盘空间限制命令命令
	 */
	public MeetLeastDiskInvoker(LeastDisk cmd) {
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
		// 本地磁盘空间限制
		if (cmd.isLocal()) {
			reset();
		} else {
			faultX(FaultTip.PERMISSION_MISSING); // 权限不足
		}
		// 投递到HUB站点
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
	
	/**
	 * 最小磁盘空间限制
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
		
		// 打印结果
		print(success);
		
		return success;
	}
	
	/**
	 * 打印本地参数
	 * @param success 成功或者否
	 */
	private void print(boolean success) {
		// 设置标题
		createShowTitle(new String[] { "LEAST-DISK/LOCAL" });

		ShowItem item = new ShowItem();
		String text = (success ? getXMLContent("LEAST-DISK/LOCAL/SUCCESS")
				: getXMLContent("LEAST-DISK/LOCAL/FAILED"));
		// 站点地址
		item.add(new ShowStringCell(0, text));
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}


}