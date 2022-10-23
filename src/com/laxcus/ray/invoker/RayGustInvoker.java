/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.traffic.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 节点传输流量测试调用器。
 * 
 * @author scott.liang
 * @version 1.0 8/15/2018
 * @since laxcus 1.0
 */
public class RayGustInvoker extends RayTrafficInvoker {

	/**
	 * 构造节点传输流量测试调用器，指定命令
	 * @param cmd 节点传输流量测试命令
	 */
	public RayGustInvoker(Gust cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Gust getCommand() {
		return (Gust) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Gust cmd = getCommand();
		return fireToHub(cmd.getFrom(), cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		TrafficProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TrafficProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		print(product);

		return useful(success);
	}

}