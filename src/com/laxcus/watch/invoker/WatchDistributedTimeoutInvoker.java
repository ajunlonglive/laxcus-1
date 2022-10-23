/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 设置分布处理超时调用器。
 * 
 * @author scott.liang
 * @version 1.0 9/15/2019
 * @since laxcus 1.0
 */
public class WatchDistributedTimeoutInvoker extends WatchInvoker {

	/**
	 * 构造设置分布处理超时调用器，指定命令
	 * @param cmd 设置分布处理超时
	 */
	public WatchDistributedTimeoutInvoker(DistributedTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DistributedTimeout getCommand() {
		return (DistributedTimeout) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DistributedTimeout cmd = getCommand();

		// 如果是本地，在本地处理显示
		if (cmd.isLocal()) {
			// 如果是本地，拒绝命令模式
			if (cmd.isCommand()) {
				faultX(FaultTip.SYSTEM_DENIED);
				return false;
			}
			
			// 设置本地任务超时时间
			long interval = cmd.getInterval();
			// getInvokerPool().setMemberTimeout(interval);
			EchoTransfer.setInvokerTimeout(interval);
			print(interval);
			return useful();
		} else {
			// 投递到HUB站点，分别处理
			return fireToHub();
		}
	}

	/**
	 * 打印时间
	 * @param interval
	 */
	private void print(long interval) {
		createShowTitle(new String[] { "DISTRIBUTED-TIMEOUT-LOCAL/TIME" });
		String text = (interval < 1 ? getXMLAttribute("DISTRIBUTED-TIMEOUT-LOCAL/TIME/unlimit")
				: doStyleTime(interval));
		
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, text));
		addShowItem(item);
		// 输出全部记录
		flushTable();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		DistributedTimeoutProduct product = null; 
		int index = findEchoKey(0);
		try {
			if(isSuccessObjectable(index)) {
				product = getObject(DistributedTimeoutProduct.class, index);
			}
		} catch(VisitException e){
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			print(product.list());
		} else {
			printFault();
		}

		return useful(success);
	}

	/**
	 * 显示反馈结果
	 * @param a
	 */
	private void print(List<DistributedTimeoutItem> a) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "DISTRIBUTED-TIMEOUT-REMOTE/STATUS", "DISTRIBUTED-TIMEOUT-REMOTE/SITE" });
		// 处理单元
		for (DistributedTimeoutItem e : a) {
			ShowItem item = new ShowItem();
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			item.add(new ShowStringCell(1, e.getSite()));
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}

}
