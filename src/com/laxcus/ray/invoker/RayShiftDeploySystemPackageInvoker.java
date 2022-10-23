/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.cloud.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 转发发布单个分布任务组件调用器
 * 有生成的组件包，发给ACCOUNT站点
 * 
 * @author scott.liang
 * @version 1.0 9/2/2020
 * @since laxcus 1.0
 */
public class RayShiftDeploySystemPackageInvoker extends RayInvoker {

	/** 执行步骤，在ENDING阶段 **/
	private int step;

	/**
	 * 构造转发发布单个分布任务组件，指定命令
	 * @param shift 转发发布单个分布任务组件
	 */
	public RayShiftDeploySystemPackageInvoker(ShiftDeploySystemPackage shift) {
		super(shift);
		// 不要设置签名，以系统身份向ACCOUNT发送命令
		shift.setIssuer(null);
		// 从1开始
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftDeploySystemPackage getCommand() {
		return (ShiftDeploySystemPackage) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftDeploySystemPackage shift = getCommand();
		Node remote = shift.getRemote();
		DeploySystemPackage sub = shift.getCommand();

		// 发送命令给ACCOUNT站点
		boolean success = launchTo(remote, sub);
		if (!success) {
			reply(false, 0);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		boolean success = false;
		switch(step) {
		case 1 :
			success = send();
			step++; // 继续下一步
			break;
		case 2:
			success = receive();
			break;
		}

		Logger.debug(this, "ending", success, "step is %d", step);

		return success;
	}

	/**
	 * 读取反馈结果
	 * @return 返回实例
	 */
	private MailSystemPackageProduct readProduct() {
		// 反馈结果
		int index = findEchoKey(0);
		try {
			if (isSuccessCompleted(index)) {
				return getObject(MailSystemPackageProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 接收ACCOUNT站点反馈命令，在确认成功后，上传云应用包到ACCOUNT指定站点。
	 * @return 成功返回真，否则假
	 */
	private boolean send() {	
		// 读结果
		MailSystemPackageProduct product = readProduct();
		// 判断成功，不成功退出
		boolean success = (product != null && product.isSuccessful());
		if (!success) {
			reply(false, 0); // 唤醒
			return false;
		}

		ShiftDeploySystemPackage shift = getCommand();
		CloudPackageComponent component = shift.getComponent();

		// 根据ACCOUNT的提供的地址，上传整个组件文件，退出等待反馈结果
		Cabin hub = product.getSource();
		ReplyItem item = new ReplyItem(hub, component);
		success = replyTo(item);

		// 不成功，通知退出
		if (!success) {
			reply(false, 0);
		}

		Logger.debug(this, "send", success, "reply to %s", hub);

		return success;
	}

	/**
	 * 上传之后，接收ACCOUNT站点的处理，返回给调用器
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		// 取结果
		MailSystemPackageProduct product = readProduct();
		// 判断成功，保存处理结果
		boolean success = (product != null && product.isSuccessful());
		int elements = (product != null ? product.getElements(): 0);
		// 设置结果
		reply(success, elements);

		Logger.debug(this, "receive", success, "result is");

		return useful(success);
	}

	/**
	 * 反馈结果
	 * @param success 成功或者否
	 */
	private void reply(boolean success, int elements) {
		ShiftDeploySystemPackage shift = getCommand();
		DeploySystemPackageHook hook = shift.getHook();

		// 返回结果...
		DeploySystemPackageProduct product = new DeploySystemPackageProduct(success);
		product.setRemote(shift.getRemote());
		product.setElements(elements);
		
		hook.setResult(product);
	}

}