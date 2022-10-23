/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.bank.pool.*;
import com.laxcus.command.task.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;

/**
 * 诊断分布任务组件所在的ACCOUNT站点地址调用器
 * 
 * @author scott.liang
 * @version 1.0 7/19/2018
 * @since laxcus 1.0
 */
public class BankAssertTaskHubInvoker extends BankSeekAccountSiteInvoker {
	
	/** ACCOUNT站点报告 **/
	private AssertTaskHubProduct product = new AssertTaskHubProduct();

	/**
	 * 构造诊断分布任务组件所在的ACCOUNT站点地址，指定命令
	 * @param cmd 诊断分布任务组件所在的ACCOUNT站点地址
	 */
	public BankAssertTaskHubInvoker(AssertTaskHub cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AssertTaskHub getCommand() {
		return (AssertTaskHub) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AssertTaskHub cmd = getCommand();
		
		Logger.debug(this, "launch", "element size:%d", cmd.size());

		// 取出用户签名
		TreeSet<Siger> array = new TreeSet<Siger>();
		for (TaskPart part : cmd.list()) {
			// 如果是系统组件，取全部ACCOUNT站点；否则只找一个
			if (part.isSystemLevel()) {
				List<Node> sites = AccountOnBankPool.getInstance().detail();
				product.addAll(part, sites);
			} else if (part.isUserLevel()) {
				array.add(part.getIssuer());
			}
		}

		// 找ACCOUNT站点
		boolean success = seekSites(array, false);
		// 不成功，反馈保存的结果
		if (!success) {
			replyProduct(product);
		}
		
		Logger.debug(this, "launch", success, "to hub");

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 接收反馈的ACCOUNT站点
		List<Seat> seats = replySites();
		
		Logger.debug(this, "ending", "result size:%d", seats.size());

		// 处理结果
		// 查找用户签名
		AssertTaskHub cmd = getCommand();
		for (TaskPart part : cmd.list()) {
			// 如果是系统组件，忽略
			if (part.isSystemLevel()) {
				continue;
			}

			// 保存ACCOUNT站点地址
			for (Seat seat : seats) {
				if (Laxkit.compareTo(part.getIssuer(), seat.getSiger()) == 0) {
					product.add(part, seat.getSite());
				}
			}
		}
		
		Logger.debug(this, "ending", "product size:%d", product.size());
		
		// 发送处理结果
		replyProduct(product);
		// 结束
		return useful();
	}

}
