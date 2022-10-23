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
import com.laxcus.echo.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.guide.archive.*;
import com.laxcus.task.guide.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 部署云计算应用软件包调用器
 * 
 * @author scott.liang
 * @version 1.0 3/21/2020
 * @since laxcus 1.0
 */
public abstract class MeetDeployCloudPackageInvoker extends MeetRuleInvoker {
	
	class PlayFruit {
		boolean success = false;
		int count = 0;
		
		public PlayFruit(boolean b, int i) {
			success = b;
			count = i;
		}
	}
	
	/** 执行步骤 **/
	private int step;

	/** 结果 **/
	private MailCloudPackageProduct mailProduct;
	
	/**
	 * 构造部署云计算应用软件包调用器，指定命令
	 * @param cmd 部署云计算应用软件包
	 */
	protected MeetDeployCloudPackageInvoker(DeployCloudPackage cmd) {
		super(cmd, true); // 要求锁定资源的处理，防止FrontScheduleRefreshInvoker同步
		// 拒绝管理员操作！
		setRefuseAdministrator(true);
		// 不要执行跨过刷新的操作
		setSkipScheduleRefresh(false);
		
		// 建立事务规则！发布过程中，拒绝所有操作！
		createRule();
		// 执行步骤
		step = 1;
	}
	
	/**
	 * 建立锁定规则
	 */
	private void createRule() {
		UserRuleItem item = new UserRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		addRule(item);
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
	 * 判断在本地部署
	 * 
	 * @return 返回真或者假
	 */
	protected boolean isLocal() {
		return getCommand().isLocal();
	}
	
//	private Naming readWareNaming() {
//		// 判断有动态链接库
//		DeployCloudPackage cmd = getCommand();
//		File file = cmd.getFile();
//		// 判断是系统应用
//		try {
//			CloudPackageReader reader = new CloudPackageReader(file);
//			CloudPackageItem item = reader.readGTC();
//			byte[] content = item.getContent();
//
//			// 读取第三方软件包的软件名称
//			GuideComponentReader sub = new GuideComponentReader(content);
//			WareTag tag = sub.readWareTag();
//			return tag.getNaming();
//		} catch (IOException e) {
//			Logger.error(e);
//		}
//		return null;
//	}
//	
//	/**
//	 * 判断是系统应用。只能由管理员
//	 * @return 返回真或者假
//	 */
//	private boolean isSystemApplication() {
//		// 判断是管理员或者拥有管理员权限
//		//	boolean success = (isAdministrator() || getStaffPool().canDBA());
//		
//		// 只要本地发布都支持
//		if (isLocal()) {
//			return false;
//		}
//
//		// 只能管理员发布
//		boolean success = isAdministrator();
//		// 1. 不成立，检测有发布分布式应用软件的权限
//		if (success) {
//			// 判断一致
//			Naming naming = readWareNaming();
//			return (Laxkit.compareTo(Sock.SYSTEM_WARE, naming) == 0);
//		}
//		// 不成立
//		return false;
//	}
//	
//	/**
//	 * 判断是用户应用
//	 * @return 返回真或者假
//	 */
//	private boolean isUserApplication() {
//		// 只要本地发布都支持
//		if (isLocal()) {
//			return false;
//		}
//		
//		// 判断是用户
//		boolean success = isUser();
//		if (success) {
//			Naming naming = readWareNaming();
//			return (Laxkit.compareTo(Sock.SYSTEM_WARE, naming) != 0);
//		}
//		// 不成立
//		return false;
//	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetRuleInvoker#checkSubPermission()
	 */
	@Override
	protected boolean checkSubPermission() {
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
		// 显示许可协议，由用户决定接受它
		return approveLicence(content);
		
//		return getLauncher().showLicence(content);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.front.meet.invoker.MeetRuleInvoker#process()
	 */
	@Override
	protected boolean process() {
		// 1. 从磁盘磁盘中判断有动态链接库
		checkLibrary();

		// 2. 判断允许发送云计算组件！
		boolean allow = checkPermission();
		if (!allow) {
			faultX(FaultTip.PERMISSION_MISSING);
			return true;
		}
		
		// 如果是在本地部署时...
		if (isLocal()) {
			PlayFruit second = new PlayFruit(false, 0);
			PlayFruit first = deployGuide();
			if (first.success) {
				second = deployLocal();
			}
			boolean success = (first.success  && second.success);
			print(success,  first.count + second.count);
			// 无论在本地部署成功或者失败，都返回true，表示可以退出！
			return true;
		}
		
		boolean success = false;
		switch (step) {
		case 1:
			success = attempt();
			break;
		case 2:
			success = send();
			break;
		case 3:
			success = receive();
			break;
		}
		step++;

		// 不成功，或者大于3时，返回“真”退出！
		if (!success || step > 3) {
			// 打印结果
			print();
			return true;
		}

		return false;
	}
	
	/**
	 * 打印结果
	 */
	private void print() {
		boolean success = (mailProduct != null && mailProduct.isSuccessful());
		int elements = 0;
		if (success) {
			elements = mailProduct.getElements();
		}
		print(success, elements);
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
	 * 尝试向GATE节点发出命令
	 * @return 成功返回真，否则假
	 */
	private boolean attempt() {
		// 投递给GATE节点
		return fireToHub();
	}

	/**
	 * 接收GATE节点反馈，向GATE节点发送
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		// 1. 接收GATE节点反馈
		MailCloudPackageProduct product = readProduct();
		boolean success = (product != null && product.isSuccessful());
		// 2. 发送给GATE节点
		if (success) {
			Cabin source = product.getSource();
			CloudPackageComponent component = createComponent();
			success = replyTo(source, component);
		}
		return success;
	}

	/**
	 * 接收GATE节点反馈
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		mailProduct = readProduct();
		boolean success = (mailProduct != null && mailProduct.isSuccessful());
		// 部署本地
		if (success) {
			PlayFruit second = new PlayFruit(false, 0);
			PlayFruit first = deployGuide();
			if (first.success) {
				second = deployLocal();
			}
			success = (first.success && second.success);
			// 重置结果
			mailProduct.setSuccessful(success);
			if (success) mailProduct.addElements(first.count + second.count);
		}
		
		// 通知本地，20秒之后启动更新...
		if (success) {
			DeployCloudPackage cmd = getCommand();
			getStaffPool().forwardScheduleRefresh(cmd.getCheckTime());
		}
		
		return success;
	}

	/**
	 * 从硬盘或者内存读取结果
	 * @return MailCloudPackageProduct实例
	 */
	private MailCloudPackageProduct readProduct() {
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				return getObject(MailCloudPackageProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		return null;
	}
	
	/**
	 * 读磁盘文件，生成云应用组件
	 * @return CloudPackageComponent
	 */
	private CloudPackageComponent createComponent() {
		DeployCloudPackage cmd = getCommand();
		File file = cmd.getFile();
		// 磁盘文件名，不包括路径！
		String name = file.getName();
		// 生成内容
		byte[] content = readContent(file);
		if (content == null) {
			return new CloudPackageComponent(name);
		}

		// 返回结果
		return new CloudPackageComponent(name, content);
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
	
	/**
	 * 部署向导组件
	 * @return 返回加载单元数目
	 */
	private PlayFruit deployGuide() {
		DeployCloudPackage cmd = getCommand();
		File file = cmd.getFile();

		int count = 0;
		
		try {
			CloudPackageReader reader = new CloudPackageReader(file);
			// 取GTC组件类
			CloudPackageItem item = reader.readGTC();
			if (item == null) {
				Logger.error(this, "deployGuide", "cannot be find guide component!");
				return new PlayFruit(false, 0);
			}
			
			byte[] content = item.getContent();
			Logger.debug(this, "deployGuide", "guide content length:%d", (content == null ? -1 : content.length));
			
			// 读取第三方软件包的软件名称
			GuideComponentReader sub = new GuideComponentReader(content);
			WareTag tag = sub.readWareTag();
			if (tag == null) {
				Logger.error(this, "deployGuide", "cannot be find ware-tag!");
				return new PlayFruit(false, 0);
			}
			
			// 向导组件！
			BootComponent guide = new BootComponent(tag.getNaming(), content);
			boolean success = GuideTaskPool.getInstance().deploy(guide);
			if (!success) {
				return new PlayFruit(false, 0);
			}
			count += 1;
			
			// 读取JAR附件
			List<CloudPackageItem> items = reader.readGTCAssists();
			for (CloudPackageItem e : items) {
				// 定义组件包
				BootAssistComponent component = new BootAssistComponent(tag.getNaming(), e.getSimpleName(), e.getContent());
				success = GuideTaskPool.getInstance().deploy(component);
				if (!success) return new PlayFruit(false, count);
				count += 1;
			}
			// 读取动态链接库
			items = reader.readGTCLibraries();
			for (CloudPackageItem e : items) {
				BootLibraryComponent component = new BootLibraryComponent(tag.getNaming(), e.getSimpleName(), e.getContent());
				success = GuideTaskPool.getInstance().deploy(component);
				if (!success) return new PlayFruit(false, count);
				count += 1;
			}
		} catch (IOException e) {
			Logger.error(e);
			return new PlayFruit(false, count);
		}
		
		// 唤醒线程，实时更新
		if (count > 0) {
			GuideTaskPool.getInstance().refresh();
		}

		return new PlayFruit(true, count);
	}

	/**
	 * 检查权限，判断允许发布云计算应用！
	 * @return
	 */
	public abstract boolean checkPermission();
	
	/**
	 * 部署本地位置
	 * @param file 磁盘文件
	 * @return 返回部署数目
	 */
	protected abstract PlayFruit deployLocal();
}