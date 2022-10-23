/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.bank.pool.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.site.entrance.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 批量处理数据优化调用器。<br>
 * BANK站点通过ENTRANCE站点定位GATE站点，交给关联的GATE处理
 * 
 * @author scott.liang
 * @version 1.0 7/20/2018
 * @since laxcus 1.0
 */
public class BankBatchPressRegulateInvoker extends BankInvoker {

	/**
	 * 构造批量处理数据优化调用器，指定命令
	 * @param cmd 批量处理数据优化
	 */
	public BankBatchPressRegulateInvoker(BatchPressRegulate cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public BatchPressRegulate getCommand() {
		return (BatchPressRegulate) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		BatchPressRegulate cmd = getCommand();

		TakeAuthorizerSite sub = new TakeAuthorizerSite();
		for (PressRegulate e : cmd.list()) {
			sub.add(e.getIssuer());
		}

		// 随机选择一个ENTRANCE站点
		Node slave = EntranceOnBankPool.getInstance().next();
		boolean success = (slave != null);
		if (success) {
			success = directTo(slave, sub);
		}

		// 检查GATE站点
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		TakeAuthorizerSiteProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeAuthorizerSiteProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null);
		if (!success) {
			return false;
		}
		
		BatchPressRegulate shift = getCommand();
		TreeMap<Node, BatchPressRegulate> nodes = new TreeMap<Node, BatchPressRegulate>();
		// 逐个匹配
		for (AuthorizerItem item : product.list()) {
			// 签名和GATE站点
			Siger siger = item.getAuthorizer();
			Node gate = item.getSite();
			// 逐一匹配
			for (PressRegulate single : shift.list()) {
				if (Laxkit.compareTo(siger, single.getIssuer()) == 0) {
					BatchPressRegulate batch = nodes.get(gate);
					if (batch == null) {
						batch = new BatchPressRegulate();
						nodes.put(gate, batch);
					}
					batch.add(single);
				}
			}
		}

		ArrayList<CommandItem> items = new ArrayList<CommandItem>();
		Iterator<Map.Entry<Node, BatchPressRegulate>> iterator = nodes.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, BatchPressRegulate> entry = iterator.next();
			CommandItem item = new CommandItem(entry.getKey(), entry.getValue());
			items.add(item);
		}
		
		// 分别发送给不同的GATE站点
		int count = super.directTo(items, false);
		success = (count>0);
		// 退出
		return useful(success);
	}

}