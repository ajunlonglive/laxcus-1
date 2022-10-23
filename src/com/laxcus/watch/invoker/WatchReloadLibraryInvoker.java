/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.awt.Color;
import java.util.*;

import com.laxcus.command.reload.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 重新加载目标节点的动态链接库命令调用器。
 * 命令先投递到管理节点，再经过管理节点转发到下属节点
 * 
 * @author scott.liang
 * @version 1.0 5/30/2018
 * @since laxcus 1.0
 */
public class WatchReloadLibraryInvoker extends WatchInvoker {

	/**
	 * 构造重新加载目标节点的动态链接库命令调用器，指定命令
	 * @param cmd 重装加载安全规则命令
	 */
	public WatchReloadLibraryInvoker(ReloadLibrary cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReloadLibrary getCommand() {
		return (ReloadLibrary) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		ReloadLibraryProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(ReloadLibraryProduct.class, index);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = (product != null);
		// 打印结果
		if (success) {
			print(product.list());
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}
		return useful(success);
	}

	/**
	 * 打印空格
	 */
	private void printGap() {
		ShowItem showItem = new ShowItem();
		for (int i = 0; i < 2; i++) {
			showItem.add(new ShowStringCell(i, ""));
		}
		// 增加一行记录
		addShowItem(showItem);
	}
	
	/**
	 * 打印结果
	 * @param array
	 */
	private void print(List<ReloadLibraryItem> array) {
		// 显示运行时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "RELOAD-LIBRARY/T1", "RELOAD-LIBRARY/T2" });

		String site = findXMLTitle("RELOAD-LIBRARY/SITE");
		Color clsSite = findXMLForeground("RELOAD-LIBRARY/SITE", Color.BLACK);
		
		String library = findXMLTitle("RELOAD-LIBRARY/LIBRARY");
		Color clrLibrary = findXMLForeground("RELOAD-LIBRARY/LIBRARY", Color.BLACK);
		
		for (int index = 0; index < array.size(); index++) {
			if (index > 0) printGap();

			ReloadLibraryItem e = array.get(index);

			// 显示加载的链接库
			ShowItem showItem = new ShowItem();
			showItem.add(new ShowStringCell(0, site, clsSite));
			showItem.add(new ShowStringCell(1, e.getSite()));
			addShowItem(showItem);
			for (String lib : e.list()) {
				showItem = new ShowItem();
				showItem.add(new ShowStringCell(0, library, clrLibrary));
				showItem.add(new ShowStringCell(1, lib));
				addShowItem(showItem);
			}
		}
		// 输出全部记录
		flushTable();
	}

}