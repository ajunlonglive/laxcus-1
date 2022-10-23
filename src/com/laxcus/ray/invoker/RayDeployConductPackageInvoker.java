/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.cloud.*;

/**
 * 发布系统级的分布计算应用调用器
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class RayDeployConductPackageInvoker extends RayDeployCloudPackageInvoker {

	/**
	 * 构造发布系统级的分布计算应用调用器，指定命令
	 * @param cmd 发布系统级的分布计算应用
	 */
	public RayDeployConductPackageInvoker(DeployConductPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployConductPackage getCommand() {
		return (DeployConductPackage) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ray.invoker.RayDeployCloudPackageInvoker#createSub()
	 */
	@Override
	protected DeploySystemPackage createSub() {
		return new DeployConductSystemPackage();
	}
}