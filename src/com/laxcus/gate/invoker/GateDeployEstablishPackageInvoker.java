/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.cloud.*;

/**
 * 部署云端分布数据构建应用包调用器
 * 
 * @author scott.liang
 * @version 1.0 3/22/2020
 * @since laxcus 1.0
 */
public class GateDeployEstablishPackageInvoker extends GateDeployCloudPackageInvoker {

	/**
	 * 构造部署云端分布数据构建应用包调用器，指定命令
	 * @param cmd 部署云端分布数据构建应用包
	 */
	public GateDeployEstablishPackageInvoker(DeployEstablishPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployEstablishPackage getCommand() {
		return (DeployEstablishPackage) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.gate.invoker.GateDeployCloudPackageInvoker#checkPermission()
	 */
	@Override
	boolean checkPermission() {
		// 判断有发布分布数据构建的权限
		boolean success = canPublishTask();
		if (success) {
			DeployEstablishPackage cmd = getCommand();
			if (cmd.hasLibrary()) {
				success = canPublishTaskLibrary();
			}
		}
		return success;
	}

}