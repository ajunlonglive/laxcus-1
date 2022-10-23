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
 * 部署云端快捷组件应用包调用器
 * 
 * @author scott.liang
 * @version 1.0 3/22/2020
 * @since laxcus 1.0
 */
public class GateDeployContactPackageInvoker extends GateDeployCloudPackageInvoker {

	/**
	 * 构造部署云端快捷组件应用包调用器，指定命令
	 * @param cmd 部署云端快捷组件应用包
	 */
	public GateDeployContactPackageInvoker(DeployContactPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployContactPackage getCommand() {
		return (DeployContactPackage) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.gate.invoker.GateDeployCloudPackageInvoker#checkPermission()
	 */
	@Override
	boolean checkPermission() {
		// 判断有发布快捷组件的权限
		boolean success = canPublishTask();
		if (success) {
			DeployContactPackage cmd = getCommand();
			if (cmd.hasLibrary()) {
				success = canPublishTaskLibrary();
			}
		}
		return success;
	}

}