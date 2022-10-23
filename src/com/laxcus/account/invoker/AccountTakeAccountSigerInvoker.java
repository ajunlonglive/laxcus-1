/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.account.dict.*;
import com.laxcus.command.site.bank.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 获得坐标范围内账号调用器。
 * 
 * @author scott.liang
 * @version 1.0 6/30/2018
 * @since laxcus 1.0
 */
public class AccountTakeAccountSigerInvoker extends AccountInvoker {

	/**
	 * 构造获得坐标范围内账号，指定命令
	 * @param cmd 获得坐标范围内账号
	 */
	public AccountTakeAccountSigerInvoker(TakeAccountSiger cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeAccountSiger getCommand() {
		return (TakeAccountSiger) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeAccountSiger cmd = getCommand();
		SiteAxes axes = cmd.getAxes();
		// 根据坐标，筛选账号签名
		List<Siger> all = StaffOnAccountPool.getInstance().choice(axes);

		// 反馈报告
		TakeAccountSigerProduct product = new TakeAccountSigerProduct();
		product.setLocal(getLocal()); // ACCOUNT本地站点
		product.addAll(all); // 保存全部账号

		// 向HASH站点反馈结果
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "%s size:%d", axes, product.size());

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
