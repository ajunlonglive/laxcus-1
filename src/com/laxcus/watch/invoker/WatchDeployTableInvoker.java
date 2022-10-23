/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 发布数据表到指定站点调用器
 * 
 * @author scott.liang
 * @version 1.0 6/14/2019
 * @since laxcus 1.0
 */
public class WatchDeployTableInvoker extends WatchInvoker {

	/**
	 * 构造发布数据表到指定站点调用器，指定命令
	 * @param cmd 发布数据表到指定站点
	 */
	public WatchDeployTableInvoker(DeployTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployTable getCommand() {
		return (DeployTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 如果是BANK站点，拒绝发送
		if (isBankHub()) {
			faultX(FaultTip.BANK_RETRY);
			return useful(false);
		}

		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		DeployTableProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DeployTableProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			print(product);
		} else {
			printFault();
		}

		return useful(success);
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(DeployTableProduct product) {
		// 打印消耗的时间
		printRuntime();

		// 显示标题
		createShowTitle(new String[] { "DEPLOY-TABLE/STATUS",
				"DEPLOY-TABLE/USERNAME", "DEPLOY-TABLE/SITE" });

		for (DeployTableItem e : product.list()) {
			ShowItem item = new ShowItem();
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			item.add(new ShowStringCell(1, e.getSiger()));
			item.add(new ShowStringCell(2, e.getSite()));
			addShowItem(item);
		}

		// 输出全部记录
		flushTable();
	}

//	/**
//	 * 打印空格
//	 */
//	private void printGap() {
//		ShowItem showItem = new ShowItem();
//		for (int i = 0; i < 3; i++) {
//			showItem.add(new ShowStringCell(i, ""));
//		}
//		// 增加一行记录
//		addShowItem(showItem);
//	}

}