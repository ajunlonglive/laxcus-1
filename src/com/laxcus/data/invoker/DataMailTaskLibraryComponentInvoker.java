/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.command.task.*;
import com.laxcus.data.pool.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.conduct.from.*;
import com.laxcus.task.establish.rise.*;
import com.laxcus.task.establish.scan.*;
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
public class DataMailTaskLibraryComponentInvoker extends DataInvoker {

	/**
	 * 构造发布分布任务组件动态链接库包，指定命令
	 * @param cmd 发布分布任务组件动态链接库包
	 */
	public DataMailTaskLibraryComponentInvoker(MailTaskLibraryComponent cmd) {
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
		// CONDUCT组件
		if (PhaseTag.isFrom(family)) {
			success = FromTaskPool.getInstance().hasTask(issuer);
			if (!success) {
				success = StaffOnDataPool.getInstance().allow(issuer);
			}
		}
		// ESTABLISH组件
		else if (PhaseTag.isScan(family)) {
			success = ScanTaskPool.getInstance().hasTask(issuer);
			if (!success) {
				success = StaffOnDataPool.getInstance().allow(issuer);
			}
		} else if (PhaseTag.isRise(family)) {
			success = RiseTaskPool.getInstance().hasTask(issuer);
			if (!success) {
				success = StaffOnDataPool.getInstance().allow(issuer);
			}
		}

		Logger.note(this, "launch", success, "check %s is %s",
				(issuer != null ? issuer.toString() : "system"),
				PhaseTag.translate(family));

		// 来源端异步监听地址
		Cabin source = cmd.getSource();

		// 向CALL节点异步投递地址
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
		// 发布组件链接库
		if (success) {
			success = deploy(component);
		}

		// 反馈结果
		reply(success);

		return useful(success);
	}

	/**
	 * 执行发布
	 * @param component 组件链接库
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
		if (PhaseTag.isFrom(family)) {
			boolean exists = FromTaskPool.getInstance().hasTaskElement(component.getSection());
			if (exists) {
				success = FromTaskPool.getInstance().deploy(component);
			} else {
				success = FromTaskPool.getInstance().direct(component);
			}
		}
		// ESTABLISH 阶段
		else if (PhaseTag.isScan(family)) {
			boolean exists = ScanTaskPool.getInstance().hasTaskElement(component.getSection());
			if (exists) {
				success = ScanTaskPool.getInstance().deploy(component);
			} else {
				success = ScanTaskPool.getInstance().direct(component);
			}
		} else if (PhaseTag.isRise(family)) {
			boolean exists = RiseTaskPool.getInstance().hasTaskElement(component.getSection());
			if (exists) {
				success = RiseTaskPool.getInstance().deploy(component);
			} else {
				success = RiseTaskPool.getInstance().direct(component);
			}
		}

		return success;
	}
	
	/**
	 * 反馈结果给来源站点
	 * @param success 成功或者否
	 * @return 发送反馈结果成功返回真，否则假
	 */
	private boolean reply(boolean success) {
		MailTaskLibraryComponent cmd = getCommand();
		MailTaskLibraryComponentProduct product = new MailTaskLibraryComponentProduct(success, cmd.getFamily());
		product.setIssuer(cmd.getIssuer());

		return replyProduct(product);
	}

}