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
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 启动数据优化命令调用器
 * 
 * @author scott.liang
 * @version 1.0 12/21/2016
 * @since laxcus 1.0
 */
public class CallPressRegulateInvoker extends CallInvoker {

	/**
	 * 构造启动数据优化命令调用器，指定命令
	 * @param cmd 启动数据优化命令
	 */
	public CallPressRegulateInvoker(PressRegulate cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PressRegulate getCommand() {
		return (PressRegulate) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.top.runner.EchoRunner#launch()
	 */
	@Override
	public boolean launch() {
		PressRegulate cmd = getCommand();
		Dock dock = cmd.getDock();

		// 找到全部主站点，发送给它们
		NodeSet set = DataOnCallPool.getInstance().findPrimeTableSites(dock.getSpace());
		if (set == null || set.isEmpty()) {
			replyFault();
			return false;
		}

		Regulate sub = new Regulate(dock);
		TreeSet<Node> sites = new TreeSet<Node>(set.show());

		// 生成命令单元
		ArrayList<CommandItem> items = new ArrayList<CommandItem>();
		for(Node node : sites) {
			CommandItem e = new CommandItem(node, sub);
			items.add(e);
		}

		// 向目标地址发送命令
		boolean success = (items.size() > 0);
		if (success) {
			int count = incompleteTo(items);
			success = (count > 0);
		}
		// 不成功，返回错误
		if (!success) {
			replyFault(Major.FAULTED, Minor.NOTFOUND);
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		PressRegulate cmd = getCommand();
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

		Logger.debug(this, "ending", success, "regulate %s , count is %d", product.getDock(), product.getCount());

		return useful(success);
	}

}