/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.bank.pool.*;
import com.laxcus.command.task.*;
import com.laxcus.site.*;

/**
 * 获取保存系统组件的ACCOUNT站点调用器。<br>
 * 每个ACCOUNT站点都保存有系统组件，所以顺序取一个即可！
 * 
 * @author scott.liang
 * @version 1.0 10/11/2019
 * @since laxcus 1.0
 */
public class BankTakeSystemTaskSiteInvoker extends BankInvoker {

	/**
	 * 构造获取保存系统组件的ACCOUNT站点调用器，指定命令
	 * @param cmd 获取保存系统组件的ACCOUNT站点
	 */
	public BankTakeSystemTaskSiteInvoker(TakeSystemTaskSite cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 顺序取一个
		Node site = AccountOnBankPool.getInstance().next();
		TakeSystemTaskSiteProduct product = new TakeSystemTaskSiteProduct(site);
		replyProduct(product);
		// 退出！
		return useful();
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
