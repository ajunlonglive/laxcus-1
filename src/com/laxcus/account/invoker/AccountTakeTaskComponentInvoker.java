/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.account.pool.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.task.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;
import com.laxcus.site.*;

/**
 * 获得分布任务组件包调用器 <BR><BR>
 * 
 * 本处执行三个处理：<BR>
 * 1. 向目标地址发布DTG格式的全部应用引导文件 <BR>
 * 2. 发布与节点/签名关联的全部JAR附件 <BR>
 * 3. 发布与节点/签名关联的全部动态链接库 <BR><BR>
 * 
 * 
 * 实际上，就是发布全部组件
 * 
 * @author scott.liang
 * @version 1.0 3/11/2013
 * @since laxcus 1.0
 */
public class AccountTakeTaskComponentInvoker extends AccountInvoker {

	/**
	 * 构造获得分布任务组件包调用器，指定命令
	 * @param cmd TakeTaskComponent命令
	 */
	public AccountTakeTaskComponentInvoker(TakeTaskComponent cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeTaskComponent getCommand() {
		return (TakeTaskComponent) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeTaskComponent cmd = getCommand();
		TaskTag tag = cmd.getTag();

		// 查找分布任务组件
		TaskComponent component = TaskOnAccountPool.getInstance().doComponent(tag);
		// 判断分布任务组件存在
		boolean success = (component != null);
		// 发送结果
		if (success) {
			TaskComponentProduct product = new TaskComponentProduct(component);
			success = replyProduct(product);
		} else {
			replyFault(Major.FAULTED, Minor.NOTFOUND);
		}
		
		// 分发JAR附件和动态链接库
		if (success) {
			distribute();
		}

		// 发布这个节点上，与相关阶段有关的全部应用!
		Logger.debug(this, "launch", success, "publish application '%s'", tag.getPart());

		return useful(success);
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
	 * 转发
	 * @param root
	 */
	private void distribute() {
		TakeTaskComponent cmd = getCommand();
		TaskTag tag = cmd.getTag();
		Node remote = getCommandSite();

		// 读取关联用户签名和阶段的JAR附件包和动态链接库
		ReadPackageTuple tuple = WareOnAccountPool.getInstance().read(tag.getPart());
		if (tuple == null) {
			return;
		}

		// 分发到指定节点
		for( CloudPackageItem item : tuple.getJars() ) {
			distributeTaskAssist(remote, tag.getPart(), item);
		}
		for(CloudPackageItem item : tuple.getLibraries()) {
			distributeTaskLibrary(remote, tag.getPart(), item);
		}
	}

	/**
	 * 发布分布任务组件的附件包
	 * @param remote 目标地址
	 * @param part 执行部件
	 * @param item 包单元
	 * @return 成功返回真，否则假
	 */
	protected boolean distributeTaskAssist(Node remote, TaskPart part, CloudPackageItem item) {
		// 软件名称、附件名，内容的MD5签名
		Naming ware = item.getWare();
		String name = item.getSimpleName();
		byte[] content = item.getContent();
		// 生成内容签名
		MD5Hash sign = Laxkit.doMD5Hash(content);
		
		// 定义组件包
		TaskAssistComponent component = new TaskAssistComponent(part, sign, ware, name, content);
		PublishSingleTaskAssistComponent sub = new PublishSingleTaskAssistComponent(component);
		PublishSingleTaskAssistComponentHook hook = new PublishSingleTaskAssistComponentHook();
		ShiftPublishSingleTaskAssistComponent shift = new ShiftPublishSingleTaskAssistComponent(remote, sub, hook);
		// 设置签名，这个很重要
		shift.setIssuer(part.getIssuer());

		// 发送并且等待
		boolean success = getCommandPool().press(shift);
		if (success) {
			// 等待
			hook.await();
			// 取出结果！
			PublishTaskAssistComponentProduct result = hook.getProduct();
			success = (result != null && result.isSuccessful());
		}
		
		Logger.debug(this, "distributeTaskAssist", success, "publish %s#%s to %s", ware, name, remote);
		return success;
	}

	/**
	 * 发布分布任务组件的动态链接库
	 * @param remote 目标地址
	 * @param part 执行部件
	 * @param item 包单元
	 * @return 成功返回真，否则假
	 */
	protected boolean distributeTaskLibrary(Node remote, TaskPart part, CloudPackageItem item) {
		// 软件名称、动态链接库名，内容的MD5签名
		Naming ware = item.getWare();
		String name = item.getSimpleName();
		byte[] content = item.getContent();
		// 生成内容签名
		MD5Hash sign = Laxkit.doMD5Hash(content);

		// 定义动态链接库组件包
		TaskLibraryComponent component = new TaskLibraryComponent(part, sign, ware, name, content);
		PublishSingleTaskLibraryComponent sub = new PublishSingleTaskLibraryComponent(component);
		PublishSingleTaskLibraryComponentHook hook = new PublishSingleTaskLibraryComponentHook();
		ShiftPublishSingleTaskLibraryComponent shift = new ShiftPublishSingleTaskLibraryComponent(remote, sub, hook);
		// 设置签名，这个很重要
		shift.setIssuer(part.getIssuer());

		// 发送并且等待
		boolean success = getCommandPool().press(shift);
		if (success) {
			// 等待
			hook.await();
			// 取出结果！
			PublishTaskLibraryComponentProduct result = hook.getProduct();
			success = (result != null && result.isSuccessful());
		}
		
		Logger.debug(this, "distributeTaskLibrary", success, "publish %s#%s to %s", ware, name, remote);
		
		return success;
	}

}