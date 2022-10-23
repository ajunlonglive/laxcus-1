/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import java.util.*;

import com.laxcus.command.limit.*;
import com.laxcus.law.limit.*;
import com.laxcus.log.client.*;
import com.laxcus.account.dict.*;
import com.laxcus.util.*;

/**
 * 建立限制操作命令调用器 <br>
 * 根据用户签名找到对应的配置，加入参数
 * 
 * @author scott.liang
 * @version 3/23/2017
 * @since laxcus 1.0
 */
public class AccountCreateLimitInvoker extends AccountInvoker {

	/**
	 * 构造建立限制操作命令调用器，指定命令
	 * @param cmd 建立限制操作命令
	 */
	public AccountCreateLimitInvoker(CreateLimit cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateLimit getCommand() {
		return (CreateLimit) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CreateLimit cmd = getCommand();
		Siger siger = cmd.getIssuer();
		
		CreateLimitProduct product = new CreateLimitProduct();

		// 取出账号方位
		AccountSphere sphere = StaffOnAccountPool.getInstance().readAccountSphere(siger);
		// 判断有效，逐一处理
		boolean success = (sphere != null);
		if (success) {
			List<LimitItem> results = sphere.getAccount().createLimitItems(cmd.list());
			success = (results != null && results.size() > 0);
			if (success) {
				success = StaffOnAccountPool.getInstance().updateAccountSphere(sphere);
			}
			if (success) {
				product.addAll(results);
			}
		}

		// 发送处理结果
		success = replyProduct(product);

		Logger.debug(this, "launch", success, "product size is %d",
				product.size());

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

}
