/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 检测表分布数据容量调用器。<br>
 * 
 * 流程：FRONT -> CALL -> DATA
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0
 */
public class DesktopScanSketchInvoker extends DesktopRuleInvoker {

	/** 处理步骤 **/
	private int step = 1;

	/**
	 * 构造检测表分布数据容量调用器，指定命令
	 * @param cmd 检测表分布数据容量命令
	 */
	public DesktopScanSketchInvoker(ScanSketch cmd) {
		super(cmd);
		init();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanSketch getCommand() {
		return (ScanSketch) super.getCommand();
	}

	/**
	 * 初始化表事务
	 */
	private void init() {
		ScanSketch cmd = getCommand();
		// 表级事务，互斥写操作（独享表资源和排它）

		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setSpace(cmd.getSpace());
		// 设置表事务
		addRule(rule);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.DesktopRuleInvoker#process()
	 */
	@Override
	protected boolean process() {
		boolean success = false;
		switch (step) {
		case 1:
			success = doLaunch();
			break;
		case 2:
			success = doEnd();
			break;
		}
		// 不成功，选择退出
		if (!success) {
			return true;
		}
		// 自增1
		step++;
		return (step > 2);
	}

	/**
	 * 选择一个CAL站点，发送命令
	 * @return 成功返回真，否则假
	 */
	private boolean doLaunch() {
		ScanSketch cmd = getCommand();
		Space space = cmd.getSpace();

		NodeSet set = getStaffPool().findTableSites(space);
		Node hub = (set != null ? set.next() : null);
		// 没有站点
		if (hub == null) {
			faultX(FaultTip.ILLEGAL_SITE_X, space);
			return false;
		}
		// 投递到CALL站点
		return fireToHub(hub, cmd);
	}

	/**
	 * 接收CALL站点反馈结果
	 * @return 成功返回真，否则假
	 */
	private boolean doEnd() {
		ScanSketchProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ScanSketchProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);

		if (success) {
			// 显示运行时间
			printRuntime();
			print(product);
		} else {
			printFault();
		}

		return success;
	}

	/**
	 * 打印结果
	 * @param product
	 */
	private void print(ScanSketchProduct product ){
		// 显示运行时间
		printRuntime();

		// 统计数据
		long stubs = 0;
		long length = 0;
		long rows = 0;
		long availables = 0;

		MasterSketchItem prime = product.getPrimeCapacityItem();
		if (prime != null) {
			stubs += prime.getStubs();
			length += prime.getLength();
			rows += prime.getRows();
			availables += prime.getAvaliableRows();
		}
		SlaveSketchItem slave = product.getSlaveCapacityItem();
		if (slave != null) {
			stubs += slave.getStubs();
			length += slave.getLength();
			rows += slave.getRows();
			availables += slave.getAvaliableRows();
		}

		// 显示标题
		createShowTitle(new String[] { "SCAN-SKETCH/DATABASE", "SCAN-SKETCH/TABLE", "SCAN-SKETCH/STUBS",
				"SCAN-SKETCH/SIZE","SCAN-SKETCH/ROWS","SCAN-SKETCH/AROWS", "SCAN-SKETCH/RATE" });

		// 比率
		Space space = product.getSpace();
//		double rate = ((double) availables / (double) rows) * 100.0f;
//		String.format("%.2f", rate)
//		
//		// 显示比率
//		String rate = "";
//		if (availables == rows) {
//			rate = "100%";
//		} else {
//			double value = ((double) availables / (double) rows) * 100.0f;
//			rate = String.format("%.2f", value) + "%";
//		}

		// 显示比率
		String rate = ConfigParser.splitRate(availables, rows);

		// 显示单元
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, space.getSchemaText()));
		item.add(new ShowStringCell(1, space.getTableText()));
		
		item.add(new ShowLongCell(2, stubs));
		item.add(new ShowStringCell(3, ConfigParser.splitCapacity(length)));
		item.add(new ShowLongCell(4, rows));
		item.add(new ShowLongCell(5, availables));
		item.add(new ShowStringCell(6, rate));

		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}

}