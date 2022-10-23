/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import com.laxcus.command.task.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.contact.distant.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.visit.*;
import com.laxcus.work.pool.*;

/**
 * 发布分布任务组件应用附件包调用器
 * 
 * @author scott.liang
 * @version 1.0 10/8/2019
 * @since laxcus 1.0
 */
public class WorkMailTaskAssistComponentInvoker extends WorkInvoker {

	/**
	 * 构造发布分布任务组件应用附件包，指定命令
	 * @param cmd 发布分布任务组件应用附件包
	 */
	public WorkMailTaskAssistComponentInvoker(MailTaskAssistComponent cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MailTaskAssistComponent getCommand() {
		return (MailTaskAssistComponent) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MailTaskAssistComponent cmd = getCommand();
		int family = cmd.getFamily();
		Siger issuer = cmd.getIssuer();

		// 判断匹配的阶段，有账号存在！
		boolean success = false;
		// CONDUCT.TO / CONTACT.DISTANT组件
		if (PhaseTag.isTo(family)) {
			success = ToTaskPool.getInstance().hasTask(issuer);
			// 如果不存在，判断是不是有签名
			if (!success) {
				success = StaffOnWorkPool.getInstance().allow(issuer);
			}
		} else if (PhaseTag.isDistant(family)) {
			success = DistantTaskPool.getInstance().hasTask(issuer);
			if (!success) {
				success = StaffOnWorkPool.getInstance().allow(issuer);
			}
		}
		
		// 来源端的异步监听地址
		Cabin source = cmd.getSource();

		// 异步反馈结果
		MailTaskAssistComponentProduct product = new MailTaskAssistComponentProduct(success, family);
		boolean posted = replyTo(source, product);

		// 返回结果
		return success && posted;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		TaskAssistComponent component = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				component = getObject(TaskAssistComponent.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断有效
		boolean success = (component != null);
		// 发布组件附件
		if (success) {
			success = deploy(component);
		}

		// 反馈结果
		reply(success);
		
		Logger.debug(this, "ending", success, "result is");

		return useful(success);
	}

	/**
	 * 反馈结果给来源地址
	 * @param success 成功或者否
	 * @return 发送反馈结果成功返回真，否则假
	 */
	private boolean reply(boolean success) {
		MailTaskAssistComponent cmd = getCommand();
		MailTaskAssistComponentProduct product = new MailTaskAssistComponentProduct(
				success, cmd.getFamily());
		product.setIssuer(cmd.getIssuer());

		return replyProduct(product);
	}
	
	/**
	 * 执行发布
	 * @param component 组件附件
	 * @return 成功返回真，否则假
	 */
	private boolean deploy(TaskAssistComponent component) {
		MailTaskAssistComponent cmd = getCommand();
		int family = cmd.getFamily();
		Siger issuer = cmd.getIssuer();

		// 判断签名一致！
		boolean match = (Laxkit.compareTo(issuer, component.getIssuer()) == 0);
		if (!match) {
			return false;
		}
		
		boolean success = false;
		// CONDUCT阶段
		if (PhaseTag.isTo(family)) {
			// 判断存在
			boolean exists = ToTaskPool.getInstance().hasTaskElement(component.getSection());
			if (exists) {
				success = ToTaskPool.getInstance().deploy(component);
			} else {
				success = ToTaskPool.getInstance().direct(component);
			}
		}
		// CONTACT.DISTANT 阶段
		else if (PhaseTag.isDistant(family)) {
			boolean exists = DistantTaskPool.getInstance().hasTaskElement(component.getSection());
			if (exists) {
				success = DistantTaskPool.getInstance().deploy(component);
			} else {
				success = DistantTaskPool.getInstance().direct(component);
			}
		} 

		return success;
	}

}