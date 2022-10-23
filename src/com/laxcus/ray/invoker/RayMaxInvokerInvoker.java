/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 设置调用器数目调用器。
 * 命令先投递到管理节点，再经过管理节点转发到下属节点
 * 
 * @author scott.liang
 * @version 1.0 9/11/2020
 * @since laxcus 1.0
 */
public class RayMaxInvokerInvoker extends RayInvoker {

	/**
	 * 构造设置调用器数目调用器，指定命令
	 * @param cmd 设置调用器数目
	 */
	public RayMaxInvokerInvoker(MaxInvoker cmd) {
		super(cmd);
		setFast(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MaxInvoker getCommand() {
		return (MaxInvoker) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MaxInvoker cmd = getCommand();
		
		// 判断和清除本地内存
		if (cmd.isLocal()) {
			reset();
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
		MaxInvokerProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(MaxInvokerProduct.class, index);
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
	 * 显示集群节点数
	 * @param count
	 */
	private void printCount(int count) {
		String key = getXMLContent("MAX-INVOKER/COUNT");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowIntegerCell(1, count));
		item.add(new ShowStringCell(2, ""));
		item.add(new ShowStringCell(3, ""));
		addShowItem(item);
	}
	
	/**
	 * 显示处理时间
	 * @param time
	 */
	private void printTime(long time) {
		String key = getXMLContent("MAX-INVOKER/PROCESS-TIME");
		// 时间
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		String text = formatDistributeTime(time); 
		item.add(new ShowStringCell(1, text));
		item.add(new ShowStringCell(2, ""));
		item.add(new ShowStringCell(3, ""));
		// 保存单元
		addShowItem(item);
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(MaxInvokerProduct product) {
		// 显示运行时间
		printRuntime();
		MaxInvoker cmd = this.getCommand();
		// 设置标题
		long ms = cmd.getConfineTime();
		if (ms < 1 || ms >= 1000) {
			createShowTitle(new String[] { "MAX-INVOKER/STATUS",
					"MAX-INVOKER/SITE", "MAX-INVOKER/INVOKERS", "MAX-INVOKER/CONFINE-TIME" });
		} else {
			createShowTitle(new String[] { "MAX-INVOKER/STATUS",
					"MAX-INVOKER/SITE", "MAX-INVOKER/INVOKERS", "MAX-INVOKER/CONFINE-TIME-MS" });
		}
		String alway = getXMLContent("MAX-INVOKER/ALWAY");

		// 打印基础信息
		printTime(product.getProcessTime());
		printCount(product.size());
		if (product.size() > 0) {
			printGap(4);
		}
		
		ArrayList<Node> array = new ArrayList<Node>();
		if (cmd.isAll()) {
			array.addAll(product.getSites());
		} else {
			array.addAll(cmd.list());
		}
		
		// 显示单元
		for (Node node : array) {
			MaxInvokerItem e = product.find(node);
			if (e == null) {
				continue;
			}
			boolean success = e.isSuccessful();
			ShowItem item = new ShowItem();
			// 图标
			item.add(createConfirmTableCell(0, success));
			// 站点地址
			item.add(new ShowStringCell(1, e.getSite()));
			
			// 时间
			ms = e.getConfineTime();

			// 成功失败，不同选择!
			if (success) {
				item.add(new ShowLongCell(2, e.getInvokers()));
				if (ms < 1) {
					item.add(new ShowStringCell(3, alway));
				} else {
					if (ms >= 1000) {
						item.add(new ShowLongCell(3, ms / 1000));
					} else {
						item.add(new ShowLongCell(3, ms));
					}
				}
			} else {
				item.add(new ShowStringCell(2, "--"));
				item.add(new ShowStringCell(3, "--"));
			}
			// 保存单元
			addShowItem(item);
		}
		
		// 输出全部记录
		flushTable();
	}

	/**
	 * 调用器数目
	 */
	private void reset() {
		MaxInvoker cmd = getCommand();

		EchoTransfer.setMaxInvokers(cmd.getInvokers());
		EchoTransfer.setMaxConfineTime(cmd.getConfineTime());
		long ms = EchoTransfer.getMaxConfineTime();

		// 设置标题
		if (ms < 1 || ms >= 1000) {
			createShowTitle(new String[] { "MAX-INVOKER/LOCAL/INVOKERS", "MAX-INVOKER/LOCAL/CONFINE-TIME" });
		} else {
			createShowTitle(new String[] { "MAX-INVOKER/LOCAL/INVOKERS", "MAX-INVOKER/LOCAL/CONFINE-TIME-MS" });
		}
		String alway = getXMLContent("MAX-INVOKER/ALWAY");
		
		// 显示单元
		ShowItem item = new ShowItem();

		item.add(new ShowIntegerCell(0, EchoTransfer.getMaxInvokers()));
		if (ms < 1) {
			item.add(new ShowStringCell(1, alway));
		} else {
			if (ms >= 1000) {
				item.add(new ShowLongCell(1, ms / 1000));
			} else {
				item.add(new ShowLongCell(1, ms));
			}
		}
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}

}