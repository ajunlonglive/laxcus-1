/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.command.cloud.*;
import com.laxcus.command.cloud.task.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 部署云计算应用软件包调用器
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public abstract class DesktopSystemDeployCloudPackageInvoker extends DesktopInvoker {
	
	class PlayFruit {
		boolean success = false;
		int count = 0;
		
		public PlayFruit(boolean b, int i) {
			success = b;
			count = i;
		}
	}

	/**
	 * 构造部署云计算应用软件包调用器，指定命令
	 * @param cmd 部署云计算应用软件包
	 */
	protected DesktopSystemDeployCloudPackageInvoker(DeployCloudPackage cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployCloudPackage getCommand() {
		return (DeployCloudPackage) super.getCommand();
	}

	/**
	 * 检查发布权限
	 * @return 允许返回真，否则假
	 */
	private boolean checkPermission() {
		//		// 1. 检测有发布分布式应用软件的权限
		//		boolean success = getStaffPool().canPublishTask();
		//		// 2. 如果软件包中有动态链接库，判断有发布动态链接库的权限！
		//		if (success) {
		//			DeployCloudPackage cmd = getCommand();
		//			if (cmd.hasLibrary()) {
		//				success = getStaffPool().canPublishTaskLibrary();
		//			}
		//		}
		//		return success;

		return true;
	}

	/**
	 * 检查许可证
	 * @return
	 */
	private boolean checkLicence() {
		// 判断有动态链接库
		DeployCloudPackage cmd = getCommand();
		File file = cmd.getFile();
		
		final String licence = "^\\s*(?i)(LICENCE)([\\w\\W]+)\\s*$";

		// 从“readme”取出许可文件
		String content = null;
		try {
			CloudPackageReader reader = new CloudPackageReader(file);
			List<CloudPackageItem> items = reader.readReadmeItems();
			for (CloudPackageItem e : items) {
				String name = e.getSimpleName();
				// 名字匹配
				if (name.matches(licence)) {
					content = new UTF8().decode(e.getContent());
					break;
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		}

		// 没有内容，忽略！返回真！
		if (content == null) {
			return true;
		}
		// 显示许可协议
		return approveLicence(content);
	}

	/**
	 * 检查磁盘里包含动态链接库
	 */
	private void checkLibrary() {
		boolean exists = true;
		// 判断有动态链接库
		DeployCloudPackage cmd = getCommand();
		File file = cmd.getFile();
		try {
			CloudPackageReader reader = new CloudPackageReader(file);
			exists = reader.hasLibrary();
		} catch (IOException e) {
			Logger.error(e);
		}
		cmd.setLibrary(exists);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 1. 从磁盘磁盘中判断有动态链接库
		checkLibrary();

		// 2. 判断允许发布云计算组件！
		boolean allow = checkPermission();
		if (!allow) {
			faultX(FaultTip.PERMISSION_MISSING);
			return useful(false);
		}
		// 检查许可证
		allow = checkLicence();
		if (!allow) {
			return useful(false);
		}
		
//		// 执行本地的发布
//		PlayFruit second = new PlayFruit(false, 0);
//		PlayFruit first = deployGuide();
//		if (first.success) {
//			second = deployLocal();
//		}
		
		// 在本地部署
		PlayFruit fruit = deployLocal();
		print(fruit.success,  fruit.count );
		

//		// 执行本地的发布
//		PlayFruit second = new PlayFruit(false, 0);
//		PlayFruit first = deployGuide();
//		if (first.success) {
//			second = deployLocal();
//		}
//		boolean success = (first.success  && second.success);
//		print(success,  first.count + second.count);
		
		// 无论在本地部署成功或者失败，都返回true，表示可以退出！
		return useful(fruit.success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
	
	/**
	 * 打印结果
	 * @param success 成功
	 * @param elements 部署单元数目
	 */
	private void print(boolean success, int elements) {
		DeployCloudPackage cmd = getCommand();
		String path = Laxkit.canonical(cmd.getFile());

		// 显示处理时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "DEPLOY-CLOUD-PACKAGE/STATUS", "DEPLOY-CLOUD-PACKAGE/COUNT", "DEPLOY-CLOUD-PACKAGE/FILE" });
		// 显示结果
		Object[] a = new Object[] { success, elements, path };
		printRow(a);

		// 输出全部记录
		flushTable();
	}
	
	/**
	 * 从组件包(*.dtc)内容中读取软件名称
	 * @param content 组件包内容
	 * @return 返回软件名称，没有是空指针！
	 */
	protected Naming readWareName(byte[] content) {
		TaskComponentReader sub = new TaskComponentReader(content);
		WareTag tag = sub.readWareTag();
		// 从内容中读取软件名称
		if (tag != null) {
			Logger.info(this, "readWareName", "this is \"%s\"", tag);
			return tag.getNaming();
		}
		// 不成功，返回空指针
		return null;
	}
	
//	/**
//	 * 部署向导组件
//	 * @return 返回加载单元数目
//	 */
//	private PlayFruit deployGuide() {
//		DeployCloudPackage cmd = getCommand();
//		File file = cmd.getFile();
//
//		int count = 0;
//		
//		try {
//			CloudPackageReader reader = new CloudPackageReader(file);
//			// 取GTC组件类
//			CloudPackageItem item = reader.readGTC();
//			if (item == null) {
//				Logger.error(this, "deployGuide", "cannot be find guide component!");
//				return new PlayFruit(false, 0);
//			}
//			
//			byte[] content = item.getContent();
//			Logger.debug(this, "deployGuide", "guide content length:%d", (content == null ? -1 : content.length));
//			
//			// 读取第三方软件包的软件名称
//			GuideComponentReader sub = new GuideComponentReader(content);
//			WareTag tag = sub.readWareTag();
//			if (tag == null) {
//				Logger.error(this, "deployGuide", "cannot be find ware-tag!");
//				return new PlayFruit(false, 0);
//			}
//			
//			// 向导组件！
//			BootComponent guide = new BootComponent(tag.getNaming(), content);
//			boolean success = GuideTaskPool.getInstance().deploy(guide);
//			if (!success) {
//				return new PlayFruit(false, 0);
//			}
//			count += 1;
//			
//			// 读取JAR附件
//			List<CloudPackageItem> items = reader.readGTCAssists();
//			for (CloudPackageItem e : items) {
//				// 定义组件包
//				BootAssistComponent component = new BootAssistComponent(tag.getNaming(), e.getSimpleName(), e.getContent());
//				success = GuideTaskPool.getInstance().deploy(component);
//				if (!success) return new PlayFruit(false, count);
//				count += 1;
//			}
//			// 读取动态链接库
//			items = reader.readGTCLibraries();
//			for (CloudPackageItem e : items) {
//				BootLibraryComponent component = new BootLibraryComponent(tag.getNaming(), e.getSimpleName(), e.getContent());
//				success = GuideTaskPool.getInstance().deploy(component);
//				if (!success) return new PlayFruit(false, count);
//				count += 1;
//			}
//		} catch (IOException e) {
//			Logger.error(e);
//			return new PlayFruit(false, count);
//		}
//		
//		// 唤醒线程，实时更新
//		if (count > 0) {
//			GuideTaskPool.getInstance().refresh();
//		}
//
//		return new PlayFruit(true, count);
//	}

	/**
	 * 部署本地位置
	 * @param file 磁盘文件
	 * @return 返回部署数目
	 */
	protected abstract PlayFruit deployLocal();
}