/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.site.watch.*;
import com.laxcus.home.pool.*;

/**
 * 扫描用户/节点关联时间间隔调用器
 * 
 * @author scott.liang
 * @version 1.0 6/3/2018
 * @since laxcus 1.0
 */
public class HomeScanLinkTimeInvoker extends HomeInvoker {

	/**
	 * 构造扫描用户/节点关联时间间隔调用器，指定命令
	 * @param cmd 扫描用户/节点关联时间间隔
	 */
	public HomeScanLinkTimeInvoker(ScanLinkTime cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanLinkTime getCommand() {
		return (ScanLinkTime) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanLinkTime cmd = getCommand();
		long inverval = cmd.getInterval();

		// 重装设置检查间隔时间
		ScanLinkOnHomePool.getInstance().setSleepTimeMillis(inverval);
		// 如果要执行时，唤醒延时
		if (cmd.isImmediate()) {
			ScanLinkOnHomePool.getInstance().wakeup();
		}

		// 要求返回结果
		if (cmd.isReply()) {
			ScanLinkTimeProduct product = new ScanLinkTimeProduct();
			product.setInterval(inverval);
			replyProduct(product);
		}
		// 退出
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