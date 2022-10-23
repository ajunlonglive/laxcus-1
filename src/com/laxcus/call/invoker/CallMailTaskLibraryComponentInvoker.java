/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.call.pool.*;
import com.laxcus.command.task.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.conduct.balance.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.task.contact.fork.*;
import com.laxcus.task.contact.merge.*;
import com.laxcus.task.establish.assign.*;
import com.laxcus.task.establish.issue.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.visit.*;

/**
 * 发布分布任务组件动态链接库包调用器
 * 
 * @author scott.liang
 * @version 1.0 10/18/2019
 * @since laxcus 1.0
 */
public class CallMailTaskLibraryComponentInvoker extends CallInvoker {

	/**
	 * 构造发布分布任务组件动态链接库包
	 * @param cmd 发布分布任务组件应用
	 */
	public CallMailTaskLibraryComponentInvoker(MailTaskLibraryComponent cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MailTaskLibraryComponent getCommand() {
		return (MailTaskLibraryComponent) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MailTaskLibraryComponent cmd = getCommand();
		int family = cmd.getFamily();
		Siger issuer = cmd.getIssuer();

		// 判断匹配的阶段，有账号存在！
		boolean success = false;
		// CONDUCE 阶段
		if (PhaseTag.isInit(family)) {
			success = InitTaskPool.getInstance().hasTask(issuer);
			if (!success) {
				success = StaffOnCallPool.getInstance().allow(issuer);
			}
		} else if (PhaseTag.isBalance(family)) {
			success = BalanceTaskPool.getInstance().hasTask(issuer);
			if (!success) {
				success = StaffOnCallPool.getInstance().allow(issuer);
			}
		}
		// ESTABLISH 阶段
		else if(PhaseTag.isIssue(family)) {
			success = IssueTaskPool.getInstance().hasTask(issuer);
			if (!success) {
				success = StaffOnCallPool.getInstance().allow(issuer);
			}
		} else if(PhaseTag.isAssign(family)) {
			success = AssignTaskPool.getInstance().hasTask(issuer);
			if (!success) {
				success = StaffOnCallPool.getInstance().allow(issuer);
			}
		}
		// CONTACT阶段
		else if (PhaseTag.isFork(family)) {
			success = ForkTaskPool.getInstance().hasTask(issuer);
			if (!success) {
				success = StaffOnCallPool.getInstance().allow(issuer);
			}
		} else if (PhaseTag.isMerge(family)) {
			success = MergeTaskPool.getInstance().hasTask(issuer);
			if (!success) {
				success = StaffOnCallPool.getInstance().allow(issuer);
			}
		}
		
		// 来源端异步监听地址
		Cabin source = cmd.getSource();

		// 异步投递反馈
		MailTaskLibraryComponentProduct product = new MailTaskLibraryComponentProduct(success, family);
		boolean posted = replyTo(source, product);

		// 返回结果
		return success && posted;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		TaskLibraryComponent component = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				component = getObject(TaskLibraryComponent.class, index);
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

		return useful(success);
	}

	/**
	 * 执行发布
	 * @param component 组件附件
	 * @return 成功返回真，否则假
	 */
	private boolean deploy(TaskLibraryComponent component) {
		MailTaskLibraryComponent cmd = getCommand();
		int family = cmd.getFamily();
		Siger issuer = cmd.getIssuer();

		// 判断签名一致！
		boolean match = (Laxkit.compareTo(issuer, component.getIssuer()) == 0);
		if (!match) {
			return false;
		}
		
		boolean success = false;
		// CONDUCT阶段
		if (PhaseTag.isInit(family)) {
			boolean exists = InitTaskPool.getInstance().hasTaskElement(component.getSection());
			if (exists) {
				success = InitTaskPool.getInstance().deploy(component);
			} else {
				success = InitTaskPool.getInstance().direct(component);
			}
		} else if (PhaseTag.isBalance(family)) {
			boolean exists = BalanceTaskPool.getInstance().hasTaskElement(component.getSection());
			if (exists) {
				success = BalanceTaskPool.getInstance().deploy(component);
			} else {
				success = BalanceTaskPool.getInstance().direct(component);
			}
		}
		// ESTABLISH 阶段
		else if (PhaseTag.isIssue(family)) {
			boolean exists = IssueTaskPool.getInstance().hasTaskElement(component.getSection());
			if (exists) {
				success = IssueTaskPool.getInstance().deploy(component);
			} else {
				success = IssueTaskPool.getInstance().direct(component);
			}
		} else if (PhaseTag.isAssign(family)) {
			boolean exists = AssignTaskPool.getInstance().hasTaskElement(component.getSection());
			if (exists) {
				success = AssignTaskPool.getInstance().deploy(component);
			} else {
				success = AssignTaskPool.getInstance().direct(component);
			}
		}
		// CONTACT阶段
		else if (PhaseTag.isFork(family)) {
			boolean exists = ForkTaskPool.getInstance().hasTaskElement(component.getSection());
			if (exists) {
				success = ForkTaskPool.getInstance().deploy(component);
			} else {
				success = ForkTaskPool.getInstance().direct(component);
			}
		} else if (PhaseTag.isMerge(family)) {
			boolean exists = MergeTaskPool.getInstance().hasTaskElement(component.getSection());
			if (exists) {
				success = MergeTaskPool.getInstance().deploy(component);
			} else {
				success = MergeTaskPool.getInstance().direct(component);
			}
		}

		return success;
	}

	/**
	 * 反馈结果
	 * @param success
	 * @return 
	 */
	private boolean reply(boolean success) {
		MailTaskLibraryComponent cmd = getCommand();
		MailTaskLibraryComponentProduct product = 
			new MailTaskLibraryComponentProduct(success, cmd.getFamily());
		product.setIssuer(cmd.getIssuer());

		return replyProduct(product);
	}
}
