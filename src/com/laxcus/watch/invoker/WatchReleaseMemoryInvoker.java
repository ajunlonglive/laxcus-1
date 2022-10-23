/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.reload.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 释放节点内存命令调用器。
 * 命令先投递到管理节点，再经过管理节点转发到下属节点
 * 
 * @author scott.liang
 * @version 1.0 10/11/2018
 * @since laxcus 1.0
 */
public class WatchReleaseMemoryInvoker extends WatchInvoker {

	/**
	 * 构造释放节点内存命令调用器，指定命令
	 * @param cmd 重装加载安全规则命令
	 */
	public WatchReleaseMemoryInvoker(ReleaseMemory cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReleaseMemory getCommand() {
		return (ReleaseMemory) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReleaseMemory cmd = getCommand();
		// 判断和清除本地内存
		if (cmd.isLocal()) {
			clear();
			return useful();
		}
		// 投递到HUB站点
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		ReleaseMemoryProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(ReleaseMemoryProduct.class, index);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = (product != null);
		// 打印结果
		if (success) {
			print(product);
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}
		return useful(success);
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(ReleaseMemoryProduct product) {
		// 显示运行时间
		printRuntime();
		// 设置标题
		createShowTitle(new String[] { "RELEASE-MEMORY/STATUS",
				"RELEASE-MEMORY/SITE" });

		// 显示单元
		for (ReleaseMemoryItem e : product.list()) {
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

	/**
	 * 清除本地内存
	 */
	private void clear() {
		System.gc();

		// 设置标题
		createShowTitle(new String[] { "RELEASE-MEMORY/LOCAL" });

		ShowItem item = new ShowItem();
		String text = getXMLContent("RELEASE-MEMORY/LOCAL/FREE") ;
		// 站点地址
		item.add(new ShowStringCell(0, text));
		// 保存单元
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}

}