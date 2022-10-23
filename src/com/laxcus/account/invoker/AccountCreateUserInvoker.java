/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.account.dict.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 建立用户账号调用器。<br>
 * 建立账号，只能由管理员，或者拥有建立账号权限的用户才能操作。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2018
 * @since laxcus 1.0
 */
public class AccountCreateUserInvoker extends AccountInvoker {

	/**
	 * 建立用户账号调用器，设置异步命令
	 * @param cmd 建立用户账号
	 */
	public AccountCreateUserInvoker(CreateUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateUser getCommand() {
		return (CreateUser) super.getCommand();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CreateUser cmd = getCommand();
		User user = cmd.getUser();
		
		// 调整成员数目
		int members = AccountConfig.getPreferredMembers(user.getMembers());
		user.setMembers(members);
		
		// 并行任务数目
		int jobs = AccountConfig.getPreferredJobs(user.getJobs());
		user.setJobs(jobs);

		// 建立用户账号结果
		boolean success = false;

		// 判断没有达到最大用户数目
		final boolean allow = (!AccountConfig.isMaxUsers(StaffOnAccountPool.getInstance().size()));

		// 账号不存在，才允许建立
		if (allow) {
			if (!StaffOnAccountPool.getInstance().hasAccount(user.getUsername())) {
				success = StaffOnAccountPool.getInstance().createAccount(user);
			}
		}

		// 设置状态结果
		CreateUserProduct product = new CreateUserProduct(user.getUsername(), success);

		// 成功
		if (success) {
			// 投递给WATCH节点
			castToWatch(user);
			replyProduct(product);
		} else {
			failed();
		}

		Logger.note(this, "launch", success, "create %s, allow %s", user, allow);

		// 退出
		return useful(success);
	}
	
	/**
	 * 投递给BANK节点，转发给BANK.WATCH/ TOP.WATCH节点
	 * @param object 用户签名
	 */
	private void castToWatch(User user) {
		Siger siger = user.getUsername();
		Seat seat = new Seat(siger, getLocal());
		// 用户名称的明文
		seat.setPlainText(user.getPlainText()); 
		
		PushRegisterMember sub = new PushRegisterMember(seat);
		directToHub(sub);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}