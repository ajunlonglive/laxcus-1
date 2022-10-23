/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.top.pool.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 发布用户到指定站点调用器。<br>
 * TOP负责转发给下属的HOME集群，它起中继作用。
 * 
 * @author scott.liang
 * @version 1.0 6/1/2019
 * @since laxcus 1.0
 */
public class TopDeployUserInvoker extends TopInvoker {

	/**
	 * 构造发布用户到指定站点调用器，指定命令
	 * @param cmd 发布用户到指定站点命令
	 */
	public TopDeployUserInvoker(DeployUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployUser getCommand() {
		return (DeployUser) super.getCommand();
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
			DeployUser sub = getCommand().duplicate();
			int count = incompleteTo(a, sub);
			success = (count > 0);
		}

		if (!success) {
			DeployUserProduct product = new DeployUserProduct();
			replyProduct(product);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		DeployUserProduct product = new DeployUserProduct();
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					DeployUserProduct e = getObject(DeployUserProduct.class, index);
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