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

/**
 * 打印站点检测目录调用器
 * 
 * @author scott.liang
 * @version 1.0 8/19/2019
 * @since laxcus 1.0
 */
public abstract class CommonCheckSitePathInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造打印站点检测目录调用器，指定命令
	 * @param cmd 打印站点检测目录
	 */
	protected CommonCheckSitePathInvoker(CheckSitePath cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckSitePath getCommand() {
		return (CheckSitePath) super.getCommand();
	}

	/**
	 * 提取当前节点的目录数据
	 * @return CheckSitePathItem实例
	 */
	protected CheckSitePathItem pickup() {
		CheckSitePathItem item = new CheckSitePathItem(getLocal());

		// 判断操作系统，保存参数
		if (isLinux()) {
			// OS
			String name = System.getProperty("os.name");
			String version = LinuxEffector.getInstance().getVersion();
			if (version != null) {
				name = String.format("%s/%s", name, version);
			}
			item.setOS(name);

			// 保存全部目录
			item.addAll(LinuxDevice.getInstance().getPathTabs());
		} else if (isWindows()) {
			// OS
			item.setOS(System.getProperty("os.name") + "/" + System.getProperty("os.version"));

			// 保存全部目录
			item.addAll(WindowsDevice.getInstance().getPathTabs());
		}
		return item;
	}

}