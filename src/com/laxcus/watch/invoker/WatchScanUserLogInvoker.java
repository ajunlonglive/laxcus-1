/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.awt.*;
import java.text.*;

import com.laxcus.command.access.user.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 扫描用户日志调用器。<br><br>
 * 
 * 这个命令通过TOP/HOME，作用到CALL站点上。
 * 
 * @author scott.liang
 * @version 1.0 4/2/2018
 * @since laxcus 1.0
 */
public class WatchScanUserLogInvoker extends WatchInvoker {

	// 标题颜色
	private Color color = new Color(0x4CA3D2);

	/**
	 * 构造扫描用户日志调用器，指定命令
	 * @param cmd 强制转换命令
	 */
	public WatchScanUserLogInvoker(ScanUserLog cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanUserLog getCommand() {
		return (ScanUserLog) super.getCommand();
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

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		ScanUserLogProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ScanUserLogProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			// 打印结果
			print(product);
		} else {
			printFault(); // 打印故障
		}

		return useful(success);
	}

	/**
	 * 打印单元
	 * @param product
	 */
	private void print(ScanUserLogProduct product) {
		// 显示运行时间
		printRuntime();
		// 标题
		printTitle();

		ScanUserLog cmd = getCommand();
		int count = 0;
		for (Siger siger : cmd.getUsers()) {
			for (ScanUserLogItem e : product.find(siger)) {
				if (count > 0) printGap();
				print(e);
				count++;
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

	/**
	 * 打印剩余参数
	 * @param item
	 */
	private void printLeft(ShowItem item) {
		int count = getTitleColumnsCount();
		int index = item.size();
		for (; index < count; index++) {
			ShowStringCell e = new ShowStringCell(index, "  ");
			item.add(e);
		}
		addShowItem(item);
	}

	/**
	 * 返回标题单元
	 * @return
	 */
	private String[] getTitleCells() {
		return new String[] { "SCAN-USERLOG/STATUS", "SCAN-USERLOG/COMMAND",
				"SCAN-USERLOG/MODE", "SCAN-USERLOG/LAUNCH-TIME",
				"SCAN-USERLOG/USED-TIME", "SCAN-USERLOG/RECE-FLOWS",
				"SCAN-USERLOG/SEND-FLOWS" };
	}
	
	/**
	 * 标题统计
	 * @return
	 */
	private int getTitleColumnsCount() {
		return getTitleCells().length;
	}
	

	/**
	 * 生成标题单元
	 */
	private void printTitle() {
		String[] cells = getTitleCells();
		createShowTitle(cells);
	}

	/**
	 * 显示单元列
	 */
	private void printUnit() {
		String[] cells = getTitleCells();
		ShowItem item = new ShowItem();
		for (int index = 0; index < cells.length; index++) {
			String remark = getXMLAttribute(cells[index] + "/remark");
			item.add(new ShowStringCell(index, remark, color));
		}
		addShowItem(item); // 显示
	}

	/**
	 * 打印单元
	 * @param logs
	 */
	private void print(ScanUserLogItem logs) {
		ScanUserLog cmd = getCommand();
		String siger = cmd.findPlainText(logs.getSiger());
		String username = getXMLAttribute("SCAN-USERLOG/USERNAME/title");

		// 用户签名
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, username, color));
		item.add(new ShowStringCell(1, siger));
		printLeft(item);

		// 如果没有站点，返回
		Node node = logs.getSite();
		if (node == null) {
			return;
		}

		// CALL节点地址
		String site = getXMLAttribute("SCAN-USERLOG/SITE/title");
		item = new ShowItem();
		item.add(new ShowStringCell(0, site, color));
		item.add(new ShowStringCell(1, node));
		printLeft(item);

		// 打印日志
		String memory = getXMLContent("SCAN-USERLOG/MODE/MEMORY");
		String disk = getXMLContent("SCAN-USERLOG/MODE/DISK");
		String style = getXMLContent("SCAN-USERLOG/LAUNCH-TIME/STYLE");
		printUnit();

		SimpleDateFormat st2 = new SimpleDateFormat(style);
		
		for (EchoLog log : logs.list()) {
			item = new ShowItem();
			item.add(createConfirmTableCell(0, log.isPerfectly()));
			item.add(new ShowStringCell(1, log.getCommand()));
			item.add(new ShowStringCell(2, (log.isMemory() ? memory : disk)));

			String launchTime =	st2.format(log.getLaunchTimestamp());
			item.add(new ShowStringCell(3, launchTime));
			item.add(new ShowDoubleCell(4, (double) log.getRunTime() / 1000, 2));
			item.add(new ShowLongCell(5, log.getReceiveFlowSize()));
			item.add(new ShowLongCell(6, log.getSendFlowSize()));
			addShowItem(item); // 显示
		}
	}

}