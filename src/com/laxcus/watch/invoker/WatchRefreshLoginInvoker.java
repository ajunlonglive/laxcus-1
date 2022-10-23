/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 强制站点注册调用器
 * 
 * @author scott.liang
 * @version 1.0 5/12/2017
 * @since laxcus 1.0
 */
public class WatchRefreshLoginInvoker extends WatchInvoker {

	/**
	 * 构造强制站点注册调用器，指定命令
	 * @param cmd 强制站点注册命令
	 */
	public WatchRefreshLoginInvoker(RefreshLogin cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshLogin getCommand() {
		return (RefreshLogin) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 发送给WATCH站点的注册站点（HOME/TOP中任何一类）
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		RefreshLoginProduct product = null;
		
		int index = findEchoKey(0);
		try {
			if (isSuccessCompleted(index)) {
				product = getObject(RefreshLoginProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 打印结果
		print(product);

		return useful();
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(RefreshLoginProduct product) {
		// 不成功，打印结果
		if (product == null) {
			printFault();
			return;
		}
		
		// 显示运行时间
		printRuntime();
		// 设置标题
		createShowTitle(new String[] { "REFRESH-LOGIN/STATUS",
				"REFRESH-LOGIN/SITE" });

		// 显示单元
		for (RefreshLoginItem e : product.list()) {
			ShowItem item = new ShowItem();
			// 图标
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			// 站点地址
			item.add(new ShowStringCell(1, e.getSite()));
			// 保存单元
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}
}
