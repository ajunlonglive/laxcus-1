/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.site.front.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;
import com.laxcus.home.pool.*;

/**
 * 检索FRONT在线用户调用器。<br>
 * 找到匹配的CALL站点，转发给CALL站点处理
 * 
 * @author scott.liang
 * @version 1.0 7/26/2018
 * @since laxcus 1.0
 */
public class HomeSeekFrontSiteInvoker extends HomeInvoker {

	/**
	 * 构造检索FRONT在线用户调用器，指定命令
	 * @param cmd 检索FRONT在线用户命令
	 */
	public HomeSeekFrontSiteInvoker(SeekFrontSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekFrontSite getCommand() {
		return (SeekFrontSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 将命令转发给全部Call站点
		SeekFrontSite cmd = getCommand();

		// 记录全部CALL站点
		ArrayList<Node> slaves = new ArrayList<Node>();
		slaves.addAll(CallOnHomePool.getInstance().detail());

		// 取相同的参数
		if (!cmd.isAll()) {
			slaves.retainAll(cmd.list());
		}

		// 以容错模式发送给CALL站点
		boolean success = (slaves.size() > 0);
		if (success) {
			int count = incompleteTo(slaves, cmd);
			success = (count > 0);
		}
		if (!success) {
			replyProduct(new FrontUserProduct());
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
		List<Integer> keys = super.getEchoKeys();
		
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