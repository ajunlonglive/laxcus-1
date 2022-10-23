/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.home.pool.*;
import com.laxcus.util.set.*;

/**
 * 查找数据表的CALL站点调用器
 * 
 * @author scott.liang
 * @version 1.0 4/28/2018
 * @since laxcus 1.0
 */
public class HomeFindTableCallSiteInvoker extends HomeInvoker {

	/**
	 * 构造查找数据块DATA从站点调用器，指定命令
	 * @param cmd 查找数据表的CALL站点
	 */
	public HomeFindTableCallSiteInvoker(FindTableCallSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindTableCallSite getCommand() {
		return (FindTableCallSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindTableCallSite cmd = this.getCommand();
		Space space = cmd.getSpace();

		FindTableCallSiteProduct product = new FindTableCallSiteProduct();
		
		// 查找站点
		NodeSet set = CallOnHomePool.getInstance().findSites(space);
		// 保存参数
		if (set != null) {
			product.addAll(set.show());
		}
		// 反馈结果
		boolean success = replyProduct(product);

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
