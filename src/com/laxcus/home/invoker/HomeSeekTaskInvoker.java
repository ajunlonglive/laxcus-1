/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.task.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 检索分布任务组件站点调用器。<br><br>
 * 
 * 检索条件：
 * 1. 有用户签名，查找用户签名的全部关联站点
 * 2. 只有组件根命名，查找
 * 
 * @author scott.liang
 * @version 1.0 5/26/2017
 * @since laxcus 1.0
 */
public class HomeSeekTaskInvoker extends HomeInvoker {

	/**
	 * 构造检索分布任务组件站点调用器，指定命令
	 * @param cmd 检索分布任务组件站点
	 */
	public HomeSeekTaskInvoker(SeekTask cmd) {
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
			push(DataOnHomePool.getInstance().findSites(username), sites);
			push(WorkOnHomePool.getInstance().findSites(username), sites);
			push(CallOnHomePool.getInstance().findSites(username), sites);
			push(BuildOnHomePool.getInstance().findSites(username), sites);
		} else {
			push(DataOnHomePool.getInstance().list(), sites);
			push(WorkOnHomePool.getInstance().list(), sites);
			push(CallOnHomePool.getInstance().list(), sites);
			push(BuildOnHomePool.getInstance().list(), sites);
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
