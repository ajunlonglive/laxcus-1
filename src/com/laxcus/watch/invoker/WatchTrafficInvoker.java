/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.*;
import com.laxcus.command.traffic.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;

/**
 * 检测网络流量调用器
 * 
 * @author scott.liang
 * @version 1.0 8/15/2018
 * @since laxcus 1.0
 */
public abstract class WatchTrafficInvoker extends WatchInvoker {

	/**
	 * 构造检测网络流量调用器，指定命令
	 * @param cmd 检测网络流量命令
	 */
	protected WatchTrafficInvoker(Command cmd) {
		super(cmd);
	}
	
	/**
	 * 生成标题单元
	 */
	protected void printTitle() {
		// 生成表格标题
		String[] cells = new String[] { "TRAFFIC/ATTRIBUTE",
		"TRAFFIC/VALUE" };
		createShowTitle(cells);
	}

	/**
	 * 显示一行参数
	 * @param xmlPath XML标签路径
	 * @param cell 数据单元
	 */
	private void show(String xmlPath, ShowItemCell cell ) {
		ShowItem item = new ShowItem();
		String name = findXMLTitle(xmlPath);
		item.add(new ShowStringCell(0, name));
		cell.setIndex(1);
		item.add(cell);
		addShowItem(item);
	}

	/**
	 * 打印结果
	 * @param product
	 */
	protected void print(TrafficProduct product) {
		printRuntime();

		if (product == null) {
			printFault();
			return;
		}

		// 显示空标题
		printTitle();

		// 第一段
		show("TRAFFIC/STATUS", createConfirmTableCell(1, product.isSuccessful()));
		// 第二段
		show("TRAFFIC/FROM", new ShowStringCell(1, product.getFrom()));
		// 第三段
		show("TRAFFIC/TO", new ShowStringCell(1, product.getTo()));
		
		// 不成功就退出!
		if (!product.isSuccessful()) {
			return;
		}

		// 发送数据长度
		show("TRAFFIC/LENGTH", new ShowStringCell(1, ConfigParser.splitCapacity(product.getSendSize(), 3)));

		// 耗时
		String value = doStyleTime(product.getRunTime());
		show("TRAFFIC/RUNTIME", new ShowStringCell(1, value));

		// 速率
		long rate = (product.getSendSize() / product.getRunTime()) * 1000;
		show("TRAFFIC/RATE", new ShowStringCell(1, ConfigParser.splitCapacity(rate, 3)));

		// 子包数目
		show("TRAFFIC/PACKETS", new ShowIntegerCell(1, product.getSendPacket()));

		// 重试次数
		show("TRAFFIC/RETRIES", new ShowIntegerCell(1, product.getRetries()));

		// 超时次数
		show("TRAFFIC/TIMEOUTS", new ShowIntegerCell(1, product.getTimeoutCount()));
		
		// 输出全部记录
		flushTable();
	}

}