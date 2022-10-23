/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.top.pool.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 发布数据表到指定站点调用器。<br>
 * TOP负责转发给下属的HOME集群，它起中继作用。
 * 
 * @author scott.liang
 * @version 1.0 6/14/2019
 * @since laxcus 1.0
 */
public class TopDeployTableInvoker extends TopInvoker {

	/**
	 * 构造发布数据表到指定站点调用器，指定命令
	 * @param cmd 发布数据表到指定站点命令
	 */
	public TopDeployTableInvoker(DeployTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployTable getCommand() {
		return (DeployTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ArrayList<Node> a = new ArrayList<Node>();
		// 找到全部HOME站点，发给它们，让HOME子集群去判断和处理
		a.addAll(HomeOnTopPool.getInstance().detail());

		boolean success = (a.size() > 0);

		if (success) {
			DeployTable sub = getCommand().duplicate();
			int count = incompleteTo(a, sub);
			success = (count > 0);
		}

		if (!success) {
			DeployTableProduct product = new DeployTableProduct();
			replyProduct(product);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		DeployTableProduct product = new DeployTableProduct();
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					DeployTableProduct e = getObject(DeployTableProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = replyProduct(product);

		Logger.debug(this, "ending", success, "element size:%d", product.size());

		return useful(success);
	}
	
}