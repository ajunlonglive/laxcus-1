/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;
import com.laxcus.front.driver.mission.*;
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
 * @version 1.0 9/25/2015
 * @since laxcus 1.0
 */
public class DriverScanSketchInvoker extends DriverRuleInvoker {

	/** 处理步骤 **/
	private int step = 1;

	/**
	 * 构造检测表分布数据容量调用器，指定命令
	 * @param cmd 检测表分布数据容量命令
	 */
	public DriverScanSketchInvoker(DriverMission cmd) {
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
	 * @see com.laxcus.front.meet.invoker.DriverRuleInvoker#process()
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
		// 发送到指定的CALL站点
		return fireToHub(hub, cmd);

		//		// 发送到指定的CALL站点
		//		boolean success = launchTo(hub, cmd);
		//		if (!success) {
		//			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		//		}
		//		return success;
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
			setProduct(product);
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}

		return success;
	}

}
