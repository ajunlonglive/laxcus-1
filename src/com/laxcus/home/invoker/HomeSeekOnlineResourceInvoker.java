/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.home.pool.*;

/**
 * 检索站点在线资源调用器
 * 
 * @author scott.liang
 * @version 1.0 4/22/2018
 * @since laxcus 1.0
 */
public class HomeSeekOnlineResourceInvoker extends HomeInvoker {

	/**
	 * 构造检索站点在线资源调用器，指定命令
	 * @param cmd 检索站点在线资源
	 */
	public HomeSeekOnlineResourceInvoker(SeekOnlineResource cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekOnlineResource getCommand() {
		return (SeekOnlineResource) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekOnlineResourceProduct product = new SeekOnlineResourceProduct();

		// 保存资源引用
		List<Refer> refers = StaffOnHomePool.getInstance().getRefers();
		for (Refer e : refers) {
			product.addRefer(e);
		}

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