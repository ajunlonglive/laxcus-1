/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.command.site.front.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.home.pool.*;
import com.laxcus.visit.*;

/**
 * 检索FRONT在线用户调用器。<br>
 * HOME节点转发给全部CALL节点处理。
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class HomeSeekFrontUserInvoker extends HomeInvoker {

	/**
	 * 构造检索FRONT在线用户，指定命令
	 * @param cmd 检索FRONT在线用户命令
	 */
	public HomeSeekFrontUserInvoker(SeekFrontUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekFrontUser getCommand() {
		return (SeekFrontUser) super.getCommand();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekFrontUser cmd = getCommand();
		List<Node> slaves = CallOnHomePool.getInstance().detail();

		// 以容错模式发送给全部CALL站点
		int count = incompleteTo(slaves, cmd);
		boolean success = (count > 0);
		if (!success) {
			replyProduct(new SeekFrontSiteProduct());
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		FrontUserProduct product = new FrontUserProduct();
		// 统计CALL站点的记录
		List<Integer> keys = getEchoKeys();
		
		// 保存记录
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					FrontUserProduct e = getObject(FrontUserProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		
		// 发送结果
		replyProduct(product);
		return useful();
	}

}
