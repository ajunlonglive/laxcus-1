/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.cloud.*;
import com.laxcus.echo.*;
import com.laxcus.gate.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 部署云计算应用包调用器。
 * GATE节点在此做为中继节点，接收FRONT/ACCOUNT的通信，向双方发送！
 * 
 * @author scott.liang
 * @version 1.0 3/22/2020
 * @since laxcus 1.0
 */
public abstract class GateDeployCloudPackageInvoker extends GateSeekAccountSiteInvoker {

	/** 阶段步骤，从1开始 **/
	private int step;

	/** ACCOUNT站点 **/
	private Node account;

	/**
	 * 构造部署云计算应用包调用器，指定命令
	 * @param cmd 部署云计算应用包
	 */
	protected GateDeployCloudPackageInvoker(DeployCloudPackage cmd) {
		super(cmd);
		// 初始化1！
		step = 1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployCloudPackage getCommand() {
		return (DeployCloudPackage) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 判断允许发布，如果不允许直接拒绝
		boolean success = checkPermission();
		Logger.debug(this, "launch", success, "check permission");
		
		if (!success) {
			replyFault(Major.FAULTED, Minor.CANNOT_PUBLISH);
			return false;
		}
		// 执行上传操作
		return todo();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 执行上传操作
		return todo();
	}
	
	/**
	 * 执行上传云端应用软件包操作
	 * @return 成功返回真，失败返回假
	 */
	private boolean todo() {
		boolean success = false;
		switch (step) {
		case 1:
			success = doFirst();
			break;
		case 2:
			success = doSecond();
			break;
		case 3:
			success = doThird();
			break;
		case 4:
			success = doFourly();
			break;
		case 5:
			success = doFifthly();
			break;
		}
		step++;

		// 判断结束
		if (!success || step > 5) {
			if (!success) {
				refuse();
			}
			setQuit(true);
		}
		return success;
	}
	
	/**
	 * 第一步：通过HASH站点，找ACCOUNT站点
	 * @return 命令发送成功返回真，否则假
	 */
	private boolean doFirst() {
		Siger siger = getIssuer();
		return seekSite(siger);
	}
	
	/**
	 * 第二阶段处理：接收ACCOUNT节点地址，向ACCOUNT节点发送命令
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		// 从HASH站点收到ACCOUNT站点地址
		account = replySite();
		// 判断地址有效
		boolean success = (account != null);
		// 发送命令给ACCOUNT节点
		if (success) {
			success = launchTo(account, getCommand());
		}
		return success;
	}
	
	private Cabin accountSource;
	
	/**
	 * 接受来自ACCOUNT的反馈，判断传给FRONT节点
	 * @return 成功返回真，否则假 
	 */
	private boolean doThird() {
		MailCloudPackageProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(MailCloudPackageProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断有效，以异步方式，向FRONT节点发送下一步操作
		boolean success = (product != null && product.isSuccessful());
		if (success) {
			accountSource = product.getSource();

			MailCloudPackageProduct sub = new MailCloudPackageProduct(true);
			// 异步发送
			DeployCloudPackage cmd = getCommand();
			Cabin cabin = cmd.getSource();
			success = replyTo(cabin, sub);
		}

		return success;
	}
	
	/**
	 * 第四步，接收来自FRONT节点的数据包，发送给ACCOUNT节点
	 * @return
	 */
	private boolean doFourly() {
		// 1. 读取组件
		CloudPackageComponent component = readComponent();
		boolean success = (component != null);
		// 2. 转发给ACCOUNT节点
		if (success) {
			success = replyTo(accountSource, component);
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
	 * 第五步，接收ACCOUNT的处理结果，反馈给FRONT节点。原样转发，不做处理！
	 * @return 成功返回真，否则假
	 */
	private boolean doFifthly() {
		// 判断返回结果是成功时，重置超时时间，让FRONT尽快更新
		MailCloudPackageProduct product = readProduct();
		boolean success = (product != null && product.isSuccessful());
		if (success) {
			success = CallOnGatePool.getInstance().resetTimeout(getIssuer());
		}

		return reflect();
	}
	
	/**
	 * 从硬盘或者内存读取云计算应用包
	 * @return CloudPackageComponent实例
	 */
	private CloudPackageComponent readComponent() {
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				return getObject(CloudPackageComponent.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 检查用户发布权限
	 * @return
	 */
	abstract boolean checkPermission();
}
