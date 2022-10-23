/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.account.dict.*;
import com.laxcus.account.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 删除账号调用器。
 * 
 * 说明：ACCOUNT接受GATE的操作，删除成功后，只把结果反馈GATE站点即可。相关的转发处理由GATE去执行。
 * 
 * @author scott.liang
 * @version 1.0 7/4/2018
 * @since laxcus 1.0
 */
public class AccountDropUserInvoker extends AccountInvoker {

	/**
	 * 构造删除账号调用器，指定命令
	 * @param cmd 删除账号
	 */
	public AccountDropUserInvoker(DropUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropUser getCommand() {
		return (DropUser) super.getCommand();
	}

	/**
	 * 反馈删除结果
	 * @param successful 删除成功标识
	 */
	public void reply(boolean successful) {
		DropUser cmd = getCommand();
		// 被删除的账号
		Siger siger = cmd.getUsername();		
		// 反馈到FRONT站点
		DropUserProduct product = new DropUserProduct(siger, successful);
		product.setPrimitive(cmd.getPrimitive());
		replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropUser cmd = getCommand();
		Siger siger = cmd.getUsername();
		// 判断账号存在
		boolean success = StaffOnAccountPool.getInstance().hasAccount(siger);
		// 删除账号
		if (success) {
			success = StaffOnAccountPool.getInstance().dropAccount(siger);
		}
		// 删除分布任务组件、码位计算器、快捷组件
		if (success) {
			TaskOnAccountPool.getInstance().drop(siger);
//			ScalerOnAccountPool.getInstance().drop(siger);
//			SwiftOnAccountPool.getInstance().drop(siger);
			
			// 投递给WATCH节点
			castToWatch(siger);
		}

		// 回应
		reply(success);
		// 结束
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
	 * 投递给BANK节点，转发给BANK.WATCH/ TOP.WATCH节点
	 * @param siger 用户签名
	 */
	private void castToWatch(Siger siger) {
		Seat seat = new Seat(siger, getLocal());
		DropRegisterMember sub = new DropRegisterMember(seat);
		directToHub(sub);
	}

}
