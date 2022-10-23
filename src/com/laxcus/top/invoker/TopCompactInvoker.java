/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 压缩缓存数据块命令异步调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/28/2018
 * @since laxcus 1.0
 */
public class TopCompactInvoker extends TopInvoker {

	/**
	 * 构造压缩缓存数据块命令异步调用器，指定命令
	 * @param cmd 压缩缓存数据块命令
	 */
	public TopCompactInvoker(Compact cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Compact getCommand() {
		return (Compact) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Compact cmd = getCommand();
		// 只能是注册的WATCH站点，否则拒绝
		Node slave = cmd.getSourceSite();
		boolean success = WatchOnTopPool.getInstance().contains(slave);
		if (!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}

		Space space = cmd.getSpace();
		NodeSet set = HomeOnTopPool.getInstance().find(space);
		if (set == null || set.isEmpty()) {
			replyFault(Major.FAULTED, Minor.NOTFOUND);
			return false;
		}

		// 全部HOME站点
		Node[] sites = set.array();
		// 生成副本命令
		Compact sub = new Compact(cmd.getSpace());
		sub.addAll(cmd.list());
		// 以容错模式发送
		int count = incompleteTo(sites, sub);
		success = (count > 0);

		// 以上不成功，返回拒绝通知
		if(!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
		}

		Logger.debug(this, "launch", success, "send size:%d, success count:%d",
				sites.length, count);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CompactProduct product = new CompactProduct();

		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					CompactProduct e = getObject(CompactProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = replyProduct(product);

		Logger.debug(this, "ending", success, "Item Size:%d", product.size());

		return useful(success);
	}
	
}