/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.shutdown.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 远程关闭调用器 <br>
 * WATCH站点发送命令，其它站点接受这个命令。
 * 
 * @author scott.liang
 * @version 1.0 03/03/2021
 * @since laxcus 1.0
 */
public class RayShutdownInvoker extends RayInvoker {

	/**
	 * 构造远程关闭调用器，指定命令
	 * @param cmd 远程关闭命令
	 */
	public RayShutdownInvoker(Shutdown cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Shutdown getCommand() {
		return (Shutdown) super.getCommand();
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
		ShutdownProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ShutdownProduct.class, index);
			}
		} catch (VisitException e) {
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
	private void print(List<ShutdownItem> a) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "SHUTDOWN/STATUS", "SHUTDOWN/SITE" });
		// 处理单元
		for (ShutdownItem e : a) {
			ShowItem item = new ShowItem();
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			item.add(new ShowStringCell(1, e.getSite()));
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
//	 */
//	@Override
//	public boolean ending() {
//		List<Integer> keys = getEchoKeys();
//		ArrayList<Node> sites = new ArrayList<Node>();
//
//		for (int index : keys) {
//			try {
//				if (isSuccessObjectable(index)) {
//					ShutdownProduct e = getObject(ShutdownProduct.class, index);
//					sites.addAll(e.list());
//				}
//			} catch (VisitException e) {
//				Logger.error(e);
//			}
//		}
//
//		// 打印结果
//		print(sites);
//
//		return useful();
//	}
//
//	/**
//	 * 打印结果
//	 * @param array
//	 */
//	private void print(List<Node> array) {
//		// 运行时间
//		printRuntime();
//		// 显示标题
//		createShowTitle(new String[] { "SHUTDOWN/SITE" });
//		// 被停止的站点地址
//		for (Node e : array) {
//			ShowItem item = new ShowItem();
//			item.add(new ShowStringCell(0, e.toString()));
//			addShowItem(item);
//		}
//		// 输出全部记录
//		flushTable();
//	}

}