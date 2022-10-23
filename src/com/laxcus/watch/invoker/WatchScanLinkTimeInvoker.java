/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.site.watch.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 定时扫描用户关联的间隔时间调用器。
 * 只限HOME站点。
 * 
 * @author scott.liang
 * @version 1.0 6/3/2018
 * @since laxcus 1.0
 */
public class WatchScanLinkTimeInvoker extends WatchInvoker {

	/**
	 * 构造HOME定时扫描用户关联的间隔时间调用器，指定命令
	 * @param cmd HOME定时扫描用户关联的间隔时间
	 */
	public WatchScanLinkTimeInvoker(ScanLinkTime cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanLinkTime getCommand() {
		return (ScanLinkTime) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 必须是TOP/HOME节点，否则拒绝！
		boolean success = (isTopHub() || isHomeHub());
		if (!success) {
			faultX(FaultTip.TOP_HOME_RETRY);
			return false;
		}
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		ScanLinkTimeProduct product = null;
		// 判断成功
		if (isSuccessCompleted(index)) {
			try {
				product = getObject(ScanLinkTimeProduct.class, index);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = (product != null && product.getInterval() > 0);
		if (success) {
			print(product.getInterval());
		} else {
			print(-1);
		}

		return useful(success);
	}

	/**
	 * 打印时间
	 * @param ms
	 */
	private void print(long ms) {
		printRuntime();
		
		createShowTitle(new String[] { "SCANLINK-TIME/TIME" });

		ShowItem item = new ShowItem();
		if (ms > 0) {
			String text = doStyleTime(ms);
			item.add(new ShowStringCell(0, text));
		} else {
			String error = findXMLTitle("SCANLINK-TIME/ERROR");
			java.awt.Color color = findXMLForeground("SCANLINK-TIME/ERROR", java.awt.Color.BLACK);
			item.add(new ShowStringCell(0, error, color));
		}
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}

}