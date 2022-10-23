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
 * 转发发布分布任务组件应用附件调用器。<br><br>
 * 
 * 根据目标地址，分发给DATA/WORK/BUILD/CALL节点。
 * 
 * @author scott.liang
 * @version 1.0 3/18/2020
 * @since laxcus 1.0
 */
public class AccountShiftPublishSingleTaskAssistComponentInvoker extends AccountShiftPublishComponentInvoker {

	/** 执行步骤 **/
	private int step;

	/**
	 * 构造转发发布分布任务组件应用附件，指定命令
	 * @param shift 转发发布分布任务组件应用附件
	 */
	public AccountShiftPublishSingleTaskAssistComponentInvoker(ShiftPublishSingleTaskAssistComponent shift) {
		super(shift);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftPublishSingleTaskAssistComponent getCommand() {
		return (ShiftPublishSingleTaskAssistComponent) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftPublishSingleTaskAssistComponent shift = getCommand();
		PublishSingleTaskAssistComponent cmd = shift.getCommand();

		// 如果阶段命名位于CALL或者/DATA/BUILD/WORK站点，产生不同的命令
		int family = cmd.getFamily();
		MailTaskAssistComponent sub = new MailTaskAssistComponent(family);

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
	 * 反馈结果
	 * @param rights 成功数目
	 * @param faults 失败数目
	 */
	private void reply(int rights, int faults) {
		ShiftPublishSingleTaskAssistComponent shift = getCommand();
		PublishSingleTaskAssistComponentHook hook = shift.getHook();
		PublishSingleTaskAssistComponent cmd = shift.getCommand();

		PublishTaskAssistComponentProduct product = 
			new PublishTaskAssistComponentProduct(cmd.getFile(), cmd.getFamily());
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

	/**
	 * 读取反馈结果
	 * @return
	 */
	private MailTaskAssistComponentProduct readProduct() {
		// 反馈结果
		int index = findEchoKey(0);
		try {
			if (isSuccessCompleted(index)) {
				return getObject(MailTaskAssistComponentProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 第1步骤，以异步方式，向目标地址发布数据
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		// 反馈结果
		MailTaskAssistComponentProduct product = readProduct();
		boolean success = (product != null && product.isSuccessful());
		if (!success) {
			reply(false);
			return false;
		}

		// 生成数据包，发送出去
		ShiftPublishSingleTaskAssistComponent shift = getCommand();
		PublishSingleTaskAssistComponent cmd = shift.getCommand();

		// CALL节点的异步监听地址
		Cabin hub = product.getSource();
		// 异步提交字节数组
		TaskAssistComponent component = cmd.getComponent();
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
		MailTaskAssistComponentProduct product = readProduct();

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

}
