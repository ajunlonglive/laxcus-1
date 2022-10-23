/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.cloud.*;

/**
 * 删除系统级的分布计算应用调用器
 * 
 * @author scott.liang
 * @version 1.0 6/22/2020
 * @since laxcus 1.0
 */
public class WatchDropConductPackageInvoker extends WatchDropCloudPackageInvoker {

	/**
	 * 构造删除系统级的分布计算应用调用器，指定命令
	 * @param cmd 删除系统级的分布计算应用
	 */
	public WatchDropConductPackageInvoker(DropConductPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropConductPackage getCommand() {
		return (DropConductPackage) super.getCommand();
	}

}