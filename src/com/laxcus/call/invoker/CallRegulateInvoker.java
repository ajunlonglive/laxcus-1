/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 数据优化命令调用器。
 * 
 * @author scott.liang
 * @version 1.1 8/2/2012
 * @since laxcus 1.0
 */
public class CallRegulateInvoker extends CallInvoker {

	/**
	 * 构造数据优化命令调用器，指定命令
	 * @param cmd 数据优化命令
	 */
	public CallRegulateInvoker(Regulate cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Regulate getCommand() {
		return (Regulate) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.top.runner.EchoRunner#launch()
	 */
	@Override
	public boolean launch() {
		Regulate cmd = getCommand();
		Space space = cmd.getSpace();

		// 找到全部主站点，发送给它们
		NodeSet set = DataOnCallPool.getInstance().findPrimeTableSites(space);
		if (set == null || set.isEmpty()) {
			super.replyFault();
			return false;
		}

		// 保存全部DATA主站点
		ArrayList<Node> sites = new ArrayList<Node>(set.show());
		// 如果指定的DATA站点地址，保留相同的站点地址
		List<Node> nodes = cmd.getSites();
		if (nodes.size() > 0) {
			sites.retainAll(nodes);
		}

		// 是空集
		if (sites.isEmpty()) {
			super.replyFault(Major.FAULTED, Minor.NOTFOUND);
			return false;
		}

		// 向目标地址发送命令
		return launchTo(sites);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		Regulate cmd = getCommand();
		RegulateProduct product = new RegulateProduct(cmd.getDock());
		List<Integer> keys = getEchoKeys();
		try {
			for (int index : keys) {
				if (isSuccessObjectable(index)) {
					RegulateProduct e = getObject(RegulateProduct.class, index);
					product.setCount(product.getCount() + e.getCount());
				}
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 返回结果
		boolean success = replyProduct(product);

		Logger.debug(this, "ending", success, "regulate count is %d", product.getCount());

		return useful(success);
	}

}