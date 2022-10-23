/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.command.cloud.*;

/**
 * 删除云端数据构建应用调用器
 * 
 * @author scott.liang
 * @version 1.0 6/22/2020
 * @since laxcus 1.0
 */
public class BankDropEstablishPackageInvoker extends BankDropCloudPackageInvoker {

	/**
	 * 构造删除云端数据构建应用调用器，指定命令
	 * @param cmd 删除云端数据构建应用
	 */
	public BankDropEstablishPackageInvoker(DropEstablishPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropEstablishPackage getCommand() {
		return (DropEstablishPackage) super.getCommand();
	}

}