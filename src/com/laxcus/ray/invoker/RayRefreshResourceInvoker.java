/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;

/**
 * 刷新分布资源命令调用器 <br>
 *  
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public abstract class RayRefreshResourceInvoker extends RayInvoker {

	/**
	 * 构造刷新分布资源命令调用器，指定命令
	 * @param cmd 刷新分布资源命令
	 */
	protected RayRefreshResourceInvoker(RefreshResource cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshResource getCommand() {
		return (RefreshResource) super.getCommand();
	}
	
	private String[] getTitleCells() {
		return new String[] { "REFRESH-RESOURCE/STATUS",
				"REFRESH-RESOURCE/USERNAME", "REFRESH-RESOURCE/SITE" };
	}
	
	/**
	 * 标题统计
	 * @return
	 */
	private int getTitleColumnsCount() {
		return getTitleCells().length;
	}

	/**
	 * 打印结果
	 * @param product 报告
	 * @param gap 有空格
	 */
	protected void print(RefreshResourceProduct product, boolean gap) {
		// 不成功，打印结果
		if (product == null) {
			super.printFault();
			return;
		}

		// 显示运行时间
		printRuntime();
		// 生成标题
		createShowTitle(getTitleCells());

		// 命令
		RefreshResource cmd = getCommand();

		// 显示单元
		List<Siger> all = cmd.getUsers();
		for (int index = 0; index < all.size(); index++) {
			if(index > 0 && gap) printGap();
			
			// 用户签名
			Siger siger = all.get(index);
			// 用户明文
			String username = cmd.findPlainText(siger);
			// 结果记录
			List<RefreshResourceItem> list = product.findAll(siger);
			for (RefreshResourceItem e : list) {
				ShowItem item = new ShowItem();
				// 图标
				item.add(createConfirmTableCell(0, e.isSuccessful()));
				item.add(new ShowStringCell(1, username));
				// 站点地址
				if (e.getSite() == null) {
					item.add(new ShowStringCell(2, getXMLContent("REFRESH-RESOURCE/SITE/INVALID")));
				} else {
					item.add(new ShowStringCell(2, e.getSite()));
				}
				// 保存单元
				addShowItem(item);
			}
		}
		// 输出全部记录
		flushTable();
	}

	/**
	 * 打印空行
	 */
	private void printGap() {
		int count = getTitleColumnsCount();
		ShowItem item = new ShowItem();
		for (int i = 0; i < count; i++) {
			ShowStringCell e = new ShowStringCell(i, "  ");
			item.add(e);
		}
		addShowItem(item);
	}
}
