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
 * TOP站点的设置DSM表压缩倍数异步调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/20/2019
 * @since laxcus 1.0
 */
public class TopSetDSMReduceInvoker extends TopInvoker {

	/**
	 * 构造设置DSM表压缩倍数异步调用器，指定命令
	 * @param cmd SetDSMReduce命令
	 */
	public TopSetDSMReduceInvoker(SetDSMReduce cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetDSMReduce getCommand() {
		return (SetDSMReduce) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetDSMReduce cmd = getCommand();

		Node slave = cmd.getSourceSite();
		// 只能是注册的WATCH/BANK站点，其他一概拒绝
		boolean success = WatchOnTopPool.getInstance().contains(slave);
		if (!success) {
			success = BankOnTopPool.getInstance().contains(slave);
		}
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
		SetDSMReduce sub = new SetDSMReduce(cmd.getSpace(), cmd.getMultiple());
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
		SetDSMReduceProduct product = new SetDSMReduceProduct();

		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					SetDSMReduceProduct e = getObject(SetDSMReduceProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = replyProduct(product);

		Logger.debug(this, "ending", success, "RuleItem size:%d", product.size());

		return useful(success);
	}
	
}