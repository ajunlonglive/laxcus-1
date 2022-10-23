/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.command.cloud.*;
import com.laxcus.command.cloud.task.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.util.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;

/**
 * 部署分布计算应用软件包调用器
 * 
 * @author scott.liang
 * @version 1.0 3/14/2020
 * @since laxcus 1.0
 */
public class MeetDeployConductPackageInvoker extends MeetDeployCloudPackageInvoker {

	/**
	 * 构造部署分布计算应用软件包，指定命令
	 * @param cmd 部署分布计算应用软件包
	 */
	public MeetDeployConductPackageInvoker(DeployConductPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetDeployCloudPackageInvoker#getCommand()
	 */
	@Override
	public DeployConductPackage getCommand() {
		return (DeployConductPackage) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetDeployCloudPackageInvoker#checkPermission()
	 */
	@Override
	public boolean checkPermission() {
		// 1. 检测有发布分布式应用软件的权限
		boolean success = getStaffPool().canPublishTask();
		// 2. 如果软件包中有动态链接库，判断有发布动态链接库的权限！
		if (success) {
			DeployConductPackage cmd = getCommand();
			if (cmd.hasLibrary()) {
				success = getStaffPool().canPublishTaskLibrary();
			}
		}
		return success;
	}

	/**
	 * 判断许可，条件：必须是PUT阶段
	 * @param part 部件
	 * @return 返回真或者假
	 */
	private boolean allow(TaskPart part) {
		return (part != null && PhaseTag.isPut(part.getFamily()));
	}

	/**
	 * 部署组件！
	 * @param item
	 * @return 成功返回真，否则假
	 */
	private boolean deployTaskBoot(CloudPackageItem item) {
		if (item == null) {
			Logger.error(this, "deployTaskBoot", "cannot be find PUT task component!");
			return false;
		}

		TaskComponentReader reader = new TaskComponentReader(item.getContent());
		TaskPart part = reader.readTaskPart();
		// 两种可能：系统组件，签名相同的组件！
		if (!allow(part)) {
			Logger.error(this, "deployTaskBoot", "illegal task-part! %s", part);
			return false;
		}
		// 修改签名
		part.setIssuer(getIssuer());

		// 生成组件，注意是DTC格式，而非DTG格式
		MD5Hash sign = Laxkit.doMD5Hash(item.getContent());
		TaskTag tag = new TaskTag(part, sign);
		// 组件
		TaskComponent component = new TaskComponent(tag, item.getContent());
//		component.setSelfly(false);
		component.setGroup(false);

		// 在本地部署!
		boolean success = PutTaskPool.getInstance().deploy(component);

		Logger.debug(this, "deployTaskBoot", success, "deploy %s", tag);

		return success;
	}

	/**
	 * 部署JAR附件
	 * @param item
	 * @return 成功返回真，否则假
	 */
	private boolean deployTaskAssist(Naming ware, CloudPackageItem item) {
		// 文件名（去掉路径！）和内容
		String name = item.getSimpleName();
		byte[] content = item.getContent();
		// 生成内容签名
		MD5Hash sign = Laxkit.doMD5Hash(content);
		// 生成工作部件
		TaskPart part = new TaskPart(getIssuer(), PhaseTag.PUT);

		// 定义组件包
		TaskTag tag = new TaskTag(part, sign);
		TaskAssistComponent component = new TaskAssistComponent(tag, ware, name, content);

		// 在本地部署!
		boolean success = PutTaskPool.getInstance().deploy(component);

		Logger.debug(this, "deployTaskAssist", success, "deploy %s", tag);

		return success;
	}

	/**
	 * 部署动态链接库
	 * @param item 单元
	 * @return 成功返回真，否则假
	 */
	protected boolean deployTaskLibrary(Naming ware, CloudPackageItem item) {
		// 文件名（去掉路径！）和内容
		String name = item.getSimpleName();
		byte[] content = item.getContent();
		// 生成内容签名
		MD5Hash sign = Laxkit.doMD5Hash(content);
		// 生成工作部件
		TaskPart part = new TaskPart(getIssuer(), PhaseTag.PUT);

		// 定义组件包
		TaskTag tag = new TaskTag(part, sign);
		TaskLibraryComponent component = new TaskLibraryComponent(tag, ware, name, content);

		// 在本地部署!
		boolean success = PutTaskPool.getInstance().deploy(component);

		Logger.debug(this, "deployTaskLibrary", success, "deploy %s", tag);

		return success;
	}
	
//	/**
//	 * 部署组件！
//	 * @param item
//	 * @return 成功返回真，否则假
//	 */
//	private boolean deployGuideBoot(CloudPackageItem item) {
//		if (item == null) {
//			Logger.error(this, "deployGuideBoot", "cannot be find guide component!");
//			return false;
//		}
//
//		GuideComponentReader reader = new GuideComponentReader(item.getContent());
//		WareTag tag = reader.readWareTag();
//		
//		BootComponent component = new BootComponent(tag.getName(), item.getContent());
//		boolean success = GuidePool.getInstance().deploy(component);
//		
////		TaskPart part = reader.readTaskPart();
////		// 两种可能：系统组件，签名相同的组件！
////		if (!allow(part)) {
////			Logger.error(this, "deployTaskBoot", "illegal task-part! %s", part);
////			return false;
////		}
////		// 修改签名
////		part.setIssuer(getIssuer());
////
////		// 生成组件，注意是DTC格式，而非DTG格式
////		MD5Hash sign = Laxkit.doMD5Hash(item.getContent());
////		TaskTag tag = new TaskTag(part, sign);
////		// 组件
////		TaskComponent component = new TaskComponent(tag, item.getContent());
////		component.setSelfly(false);
////		component.setGroup(false);
////
////		// 在本地部署!
////		boolean success = PutTaskPool.getInstance().deploy(component);
//
//		Logger.debug(this, "deployGuideBoot", success, "deploy %s", tag);
//
//		return success;
//	}
	

	/* (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetDeployCloudPackageInvoker#deployLocal()
	 */
	@Override
	protected PlayFruit deployLocal() {
		DeployCloudPackage cmd = getCommand();
		File file = cmd.getFile();

		int count = 0;

		try {
			CloudPackageReader reader = new CloudPackageReader(file);
			// 取PUT阶段引导类
			CloudPackageItem item = reader.readDTC(PhaseTag.PUT);
			if (item == null) {
				Logger.error(this, "deployLocal", "cannot be find PUT task component!");
				return new PlayFruit(false, 0);
			}
			// 读取第三方软件包的软件名称
			Naming ware = readWareName(item.getContent());
			if (ware == null) {
				Logger.error(this, "deployLocal", "cannot be find ware-name!");
				return new PlayFruit(false, 0);
			}
			// 部署引导包
			boolean success = deployTaskBoot(item);
			if (!success) {
				return new PlayFruit(false, 0);
			}
			count += 1;

			// 读取JAR附件
			List<CloudPackageItem> items = reader.readAssists(PhaseTag.PUT);
			for (CloudPackageItem e : items) {
				success = deployTaskAssist(ware, e);
				if (!success) return new PlayFruit(false, count);
				count += 1;
			}

			// 读取动态链接库
			items = reader.readLibraries(PhaseTag.PUT);
			for (CloudPackageItem e : items) {
				success = deployTaskLibrary(ware, e);
				if (!success) return new PlayFruit(false, count);
				count += 1;
			}
		} catch (IOException e) {
			Logger.error(e);
			return new PlayFruit(false, count);
		}
		
		// 唤醒线程，实时更新
		PutTaskPool.getInstance().update();
		
		return new PlayFruit(true, count);
	}

}