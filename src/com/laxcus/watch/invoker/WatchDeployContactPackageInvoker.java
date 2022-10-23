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
 * 发布系统级的分布迭代应用调用器
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class WatchDeployContactPackageInvoker extends WatchDeployCloudPackageInvoker {

	/**
	 * 构造发布系统级的分布迭代应用调用器，指定命令
	 * @param cmd 发布系统级的分布迭代应用
	 */
	public WatchDeployContactPackageInvoker(DeployContactPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployContactPackage getCommand() {
		return (DeployContactPackage) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.watch.invoker.WatchDeployCloudPackageInvoker#createSub()
	 */
	@Override
	protected DeploySystemPackage createSub() {
		return new DeployContactSystemPackage();
	}
}