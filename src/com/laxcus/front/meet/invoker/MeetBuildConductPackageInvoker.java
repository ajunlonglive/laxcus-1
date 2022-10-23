/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.cloud.*;

/**
 * 生成CONDUCT分布计算应用软件包调用器
 * 
 * @author scott.liang
 * @version 1.0 2/13/2020
 * @since laxcus 1.0
 */
public class MeetBuildConductPackageInvoker extends MeetBuildCloudPackageInvoker {

	/**
	 * 构造生成CONDUCT分布计算应用软件包调用器，指定命令
	 * @param cmd 生成CONDUCT分布计算应用软件包
	 */
	public MeetBuildConductPackageInvoker(BuildConductPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public BuildConductPackage getCommand() {
		return (BuildConductPackage) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}