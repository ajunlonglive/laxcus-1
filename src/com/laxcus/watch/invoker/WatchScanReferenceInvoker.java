/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 扫描用户资源调用器
 * 
 * @author scott.liang
 * @version 1.0 12/2/2013
 * @since laxcus 1.0
 */
public abstract class WatchScanReferenceInvoker extends WatchInvoker {

	/**
	 * 构造扫描用户资源调用器，指定命令
	 * @param cmd 扫描用户资源命令
	 */
	protected WatchScanReferenceInvoker(ScanReference cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 发送到HOME/TOP站点的任意一个
		boolean success = (isTopHub() || isHomeHub());
		if (!success) {
			faultX(FaultTip.TOP_HOME_RETRY);
			return false;
		}
		return fireToHub();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ScanTableProduct product = null;

		// 取向结果
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ScanTableProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null);
		// 显示结果
		if (success) {
			print(product.list());
		} else {
			printFault();
		}
		// 退出
		return useful(success);
	}

	/**
	 * 显示处理结果
	 * @param array
	 */
	private void print(List<ScanTableItem> array) {
		// 显示运行时间
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "SCAN-TABLE/SITE", "SCAN-TABLE/DATABASE", "SCAN-TABLE/TABLE",
				"SCAN-TABLE/STUBS", "SCAN-TABLE/ROWS", "SCAN-TABLE/AROWS" });

		// 显示
		for (ScanTableItem item : array) {
			Space space = item.getSpace();

			// 数组
			Object[] a = new Object[]{item.getSite(),space.getSchemaText(), space.getTableText(),
					item.getStubs(), item.getRows(), item.getAvailableRows()};
			printRow(a);

			//			ShowItem e = new ShowItem();
			//			e.add(new ShowStringCell(0, item.getSite()));
			//			e.add(new ShowStringCell(1, space.getSchemaText()));
			//			e.add(new ShowStringCell(2, space.getTableText()));
			//			e.add(new ShowIntegerCell(3, item.getStubs()));
			//			e.add(new ShowLongCell(4, item.getRows()));
			//			e.add(new ShowLongCell(5, item.getAvailableRows()));
			//			addShowItem(e);
		}

		// 输出全部记录
		flushTable();
	}

}