/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.command.task.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.archive.*;
import com.laxcus.visit.*;

/**
 * 转发发布分布任务组件动态链接库调用器。<br><br>
 * 
 * 根据目标地址，分发给DATA/WORK/BUILD/CALL节点。
 * 
 * @author scott.liang
 * @version 1.0 3/29/2020
 * @since laxcus 1.0
 */
public class AccountShiftPublishSingleTaskLibraryComponentInvoker extends AccountShiftPublishComponentInvoker {

	/** 执行步骤 **/
	private int step;

	/**
	 * 构造转发发布分布任务组件动态链接库，指定命令
	 * @param shift 转发发布分布任务组件动态链接库
	 */
	public AccountShiftPublishSingleTaskLibraryComponentInvoker(ShiftPublishSingleTaskLibraryComponent shift) {
		super(shift);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftPublishSingleTaskLibraryComponent getCommand() {
		return (ShiftPublishSingleTaskLibraryComponent) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftPublishSingleTaskLibraryComponent shift = getCommand();
		PublishSingleTaskLibraryComponent cmd = shift.getCommand();

		// 如果阶段命名位于CALL或者/DATA/BUILD/WORK站点，产生不同的命令
		int family = cmd.getFamily();
		MailTaskLibraryComponent sub = new MailTaskLibraryComponent(family);

		// 投递到DATA/WORK/BUILD/CALL站点的任意一个
		Node remote = shift.getRemote();
		boolean success = launchTo(remote, sub);
		if (!success) {
			reply(false);
		}

		Logger.debug(this, "launch", success, "send to %s", remote);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		boolean success = false;
		switch (step) {
		case 1:
			success = send();
			break;
		case 2:
			success = receive();
			break;
		}
		step++;

		// 不成功，或者超过2，结束！
		if (!success || step > 2) {
			setQuit(true);
		}
		return success;
	}

	/**
	 * 第1步骤，以异步方式，向目标地址发布数据
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		// 反馈结果
		MailTaskLibraryComponentProduct product = readProduct();

		// 判断有效
		boolean success = (product != null && product.isSuccessful());
		if (!success) {
			reply(false);
			return false;
		}

		// 生成数据包，发送出去
		ShiftPublishSingleTaskLibraryComponent shift = getCommand();
		PublishSingleTaskLibraryComponent cmd = shift.getCommand();

		// 目标节点的异步监听地址
		Cabin hub = product.getSource();
		// 异步提交字节数组
		TaskLibraryComponent component = cmd.getComponent();
		ReplyItem item = new ReplyItem(hub, component);
		success = replyTo(item);
		if (!success) {
			reply(false);
		}
		
		Logger.debug(this, "send", success, "reply to %s", hub);

		return success;
	}

	/**
	 * 第2步，接受反馈
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		// 反馈结果
		MailTaskLibraryComponentProduct product = readProduct();

		// 判断有成功的记录
		boolean success = (product != null && product.hasSuccessful());
		if (success) {
			reply(product.getRights(), product.getFaults());
		} else {
			reply(false);
		}
		
		Logger.debug(this, "receive", success, "finished!");

		return success;
	}

	/**
	 * 读取反馈结果
	 * @return
	 */
	private MailTaskLibraryComponentProduct readProduct() {
		// 反馈结果
		int index = findEchoKey(0);
		try {
			if (isSuccessCompleted(index)) {
				return getObject(MailTaskLibraryComponentProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 反馈结果
	 * @param rights 成功数目
	 * @param faults 失败数目
	 */
	private void reply(int rights, int faults) {
		ShiftPublishSingleTaskLibraryComponent shift = getCommand();
		PublishSingleTaskLibraryComponentHook hook = shift.getHook();
		PublishSingleTaskLibraryComponent cmd = shift.getCommand();

		PublishTaskLibraryComponentProduct product = 
			new PublishTaskLibraryComponentProduct(cmd.getFile(), cmd.getFamily());
		product.addRights(rights);
		product.addFaults(faults);
		
		// 处理结果
		hook.setResult(product);
	}

	/**
	 * 反馈结果
	 * @param successful
	 */
	private void reply(boolean success) {
		int rights = (success ? 1 : 0);
		int faults = (!success ? 1 :0);
		reply(rights, faults);
	}

}