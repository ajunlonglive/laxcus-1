/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.mix.*;

/**
 * 检测服务器系统信息调用器
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public abstract class CommonCheckSystemInfoInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造检测服务器系统信息调用器，指定命令
	 * @param cmd 检测服务器系统信息
	 */
	protected CommonCheckSystemInfoInvoker(CheckSystemInfo cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckSystemInfo getCommand() {
		return (CheckSystemInfo) super.getCommand();
	}

	/**
	 * 检测结果
	 * @return 返回CheckSystemInfoItem实例
	 */
	protected CheckSystemInfoItem pickup() {
		if (isLinux()) {
			return readLinux();
		} else if (isWindows()) {
			return readWindows();
		} else {
			return new CheckSystemInfoItem(getLocal(), false);
		}
	}

	/**
	 * 读LINUX系统参数
	 * @return
	 */
	private CheckSystemInfoItem readLinux() {
		CheckSystemInfoItem item = new CheckSystemInfoItem(getLocal(), true);
		item.setVersion(getLauncher().getVersion());
		// CPU信息
		CPULinuxReader cpu = new CPULinuxReader();
		item.setCPUInfo(cpu.read());
		// 内存信息
		MemoryLinuxReader memory = new MemoryLinuxReader();
		item.setMemInfo(memory.read());
		// 磁盘信息
		DiskLinuxReader disk = new DiskLinuxReader();
		item.setDiskInfo(disk.read());
		// JRE信息
		JREReader jre = new JREReader();
		item.setJREInfo(jre.read());
		// 返回结果
		return item;
	}

	/**
	 * 读WINDOWS系统参数
	 * @return
	 */
	private CheckSystemInfoItem readWindows() {
		CheckSystemInfoItem item = new CheckSystemInfoItem(getLocal(), true);
		item.setVersion(getLauncher().getVersion());
		// 内存信息
		MemoryWindowsReader mem = new MemoryWindowsReader();
		item.setMemInfo(mem.read());
		// 磁盘信息
		DiskWindowsReader disk = new DiskWindowsReader();
		item.setDiskInfo(disk.read());
		// JRE信息
		JREReader jre = new JREReader();
		item.setJREInfo(jre.read());
		// 返回结果
		return item;
	}

}