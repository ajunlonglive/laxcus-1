/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.cloud.*;
import com.laxcus.log.client.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 删除云端应用软件包调用器。<br>
 * 
 * 只能删除系统级的应用！即"DROP XXX PACKAGE SYSTEM"这样的命令
 * 
 * @author scott.liang
 * @version 1.0 6/22/2020
 * @since laxcus 1.0
 */
class WatchDropCloudPackageInvoker extends WatchInvoker {

	/**
	 * 构造删除云端应用软件包调用器
	 * @param cmd 删除云端应用软件包
	 */
	public WatchDropCloudPackageInvoker(DropCloudPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropCloudPackage getCommand() {
		return (DropCloudPackage) super.getCommand();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 必须登录到BANK节点执行
		if (!isBankHub()) {
			faultX(FaultTip.BANK_RETRY);
			return false;
		}
		// 必须是系统组件
		DropCloudPackage cmd = getCommand();
		if(!cmd.isSystemWare()) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		}
		// 投递到BANK节点
		return launchToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		DropCloudPackageProduct product = readProduct();
		boolean success = (product != null && product.hasSuccessful());
		// 删除云端本地
		if (success) {
			print(success, product.getRights());
		} else {
			print(false, 0);
		}
		return useful(success);
	}

	/**
	 * 从硬盘或者内存读取结果
	 * @return DropCloudPackageProduct实例
	 */
	private DropCloudPackageProduct readProduct() {
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				return getObject(DropCloudPackageProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		return null;
	}
	
	/**
	 * 打印结果
	 * @param success 成功
	 * @param elements 删除云端单元数目
	 */
	private void print(boolean success, int elements) {
		DropCloudPackage cmd = getCommand();
		Naming software = cmd.getWare();

		// 显示处理时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "DROP-CLOUD-PACKAGE/STATUS", "DROP-CLOUD-PACKAGE/COUNT", "DROP-CLOUD-PACKAGE/SOFTWARE" });
		// 显示结果
		Object[] a = new Object[] { success, elements, software };
		printRow(a);

		// 输出全部记录
		flushTable();
	}

}