/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.command.cloud.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;
import com.laxcus.watch.pool.*;

/**
 * 发布系统软件包调用器
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public abstract class WatchDeployCloudPackageInvoker extends WatchInvoker {

	/**
	 * 构造发布系统软件包调用器
	 * @param cmd 应用包
	 */
	protected WatchDeployCloudPackageInvoker(DeployCloudPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployCloudPackage getCommand() {
		return (DeployCloudPackage)super.getCommand();
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
		// 必须是系统应用
		DeployCloudPackage cmd = getCommand();
		if (!cmd.isSystemWare()) {
			String filename = Laxkit.canonical(cmd.getFile());
			faultX(FaultTip.NOTSUPPORT_X, filename);
			return false;
		}
		
		// 找到全部ACCOUNT站点
		List<Node> sites = SiteOnWatchPool.getInstance().list(SiteTag.ACCOUNT_SITE);
		// 空集合，弹出错误
		if (sites.isEmpty()) {
			faultX(FaultTip.SITE_MISSING);
			return false;
		}
		
		// 读组件
		CloudPackageComponent component = readComponent();
		if (component == null) {
			return false;
		}
		
		// 显示提示信息
		messageX(MessageTip.COMMAND_EXECUTE);
		
		ArrayList<DeploySystemPackageProduct> array = new ArrayList<DeploySystemPackageProduct>();
		// 逐个发送到ACCOUNT
		int size = sites.size();
		for (int index = 0; index < size; index++) {
			Node remote = sites.get(index);
			DeploySystemPackage sub = createSub();
			sub.setPublish(index == 0); // 如果是第一次，执行组件分发

			DeploySystemPackageHook hook = new DeploySystemPackageHook();
			ShiftDeploySystemPackage shift = new ShiftDeploySystemPackage(remote, component, sub, hook);
			
			// 转发处理
			boolean success = getCommandPool().press(shift);
			if (!success) {
				array.add(new DeploySystemPackageProduct(remote, false));
				continue;
			}

			// 等待
			hook.await();
			// 返回结果
			DeploySystemPackageProduct product = hook.getProduct();
			if (product != null) {
				array.add(product);
			} else {
				array.add(new DeploySystemPackageProduct(remote, false));
			}
		}
		
		// 在本地打印
		print(array);

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * 读出内容，生成应用包
	 * @return CloudPackageComponent实例
	 */
	private CloudPackageComponent readComponent() {
		DeployCloudPackage cmd = getCommand();
		// 读磁盘文件
		File file = cmd.getFile();
		byte[] content = readContent(file);
		if (content == null) {
			faultX(FaultTip.SYSTEM_FAULT);
			return null;
		}
		// 返回对象
		return new CloudPackageComponent(file.getName(), content);
	}

	/**
	 * 打印发布结果
	 * @param array
	 */
	private void print(List<DeploySystemPackageProduct> array) {
		// 显示处理时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "DEPLOY-SYSTEM-PACKAGE/STATUS",  "DEPLOY-SYSTEM-PACKAGE/SITE", "DEPLOY-SYSTEM-PACKAGE/ELEMENTS" });

		// 显示结果
		for (DeploySystemPackageProduct e : array) {
			boolean success = e.isSuccessful();
			String remote = e.getRemoteText();
			String value = String.format("%d", e.getElements());
			Object[] a = new Object[] { success, remote, value };
			printRow(a);
		}

		// 输出全部记录
		flushTable();
	}
	
	protected abstract DeploySystemPackage createSub();
}
