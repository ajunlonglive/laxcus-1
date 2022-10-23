/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.task.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 检索分布任务组件站点调用器
 * 
 * @author scott.liang
 * @version 1.0 5/26/2017
 * @since laxcus 1.0
 */
public class TopSeekTaskInvoker extends TopInvoker {

	/**
	 * 构造检索分布任务组件站点调用器，指定命令
	 * @param cmd 检索分布任务组件站点
	 */
	public TopSeekTaskInvoker(SeekTask cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekTask getCommand() {
		return (SeekTask) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekTask cmd = getCommand();

		// 站点地址
		TreeSet<Node> sites = new TreeSet<Node>();

		Siger username = cmd.getUsername();
		if (username != null) {
//			push(OldArchiveOnTopPool.getInstance().findSites(username), sites);
			push(HomeOnTopPool.getInstance().findSites(username), sites);
		} else {
//			push(OldArchiveOnTopPool.getInstance().list(), sites);
			push(HomeOnTopPool.getInstance().list(), sites);
		}
		// 没有找到
		if (sites.isEmpty()) {
			replyProduct(new SeekTaskProduct());
			return useful(false);
		}

		// 以容错模式，向目标站点发送命令
		int count = incompleteTo(sites, cmd);
		boolean success = (count > 0);
		// 返回错误
		if (!success) {
			replyFault();
		}

		Logger.debug(this, "launch", success, "sites:%d, sended:%d", sites.size(), count);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		SeekTaskProduct product = new SeekTaskProduct();

		List<Integer> keys = super.getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					SeekTaskProduct e = getObject(SeekTaskProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 发送处理结果
		boolean success = replyProduct(product);

		Logger.debug(this, "ending", success, "item size:%d", product.size());

		return useful(success);
	}

	/**
	 * 添入站点地址
	 * @param set
	 * @param sites
	 */
	private void push(NodeSet set, Set<Node> sites) {
		if(set != null){
			sites.addAll(set.show());
		}
	}

}