/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.top.pool.*;
import com.laxcus.site.*;

/**
 * 扫描用户/节点关联时间间隔调用器
 * 命令转发给HOME站点
 * 
 * @author scott.liang
 * @version 1.0 6/3/2018
 * @since laxcus 1.0
 */
public class TopScanLinkTimeInvoker extends TopInvoker {

	/**
	 * 构造扫描用户/节点关联时间间隔调用器，指定命令
	 * @param cmd 扫描用户/节点关联时间间隔
	 */
	public TopScanLinkTimeInvoker(ScanLinkTime cmd) {
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
		
		List<Node> slaves = HomeOnTopPool.getInstance().detail();

		boolean success = (slaves.size() > 0);
		if (success) {
			cmd.setDirect(true); // 单向处理不用反馈
			int count = incompleteTo(slaves, cmd);
			success = (count > 0);
		}
		if (success) {
			// 返回结果
			ScanLinkTimeProduct product = new ScanLinkTimeProduct();
			product.setInterval(inverval);
			success = replyProduct(product);
		}

		// 出错，返回通知
		if (!success) {
			replyFault();
		}
	
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
