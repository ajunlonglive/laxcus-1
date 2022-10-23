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
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 扫描数据块命令的调用器。<br>
 * 
 * 流程：FRONT -> CALL -> DATA。 
 * 
 * @author scott.liang
 * @version 1.1 7/23/2013
 * @since laxcus 1.0
 */
public class DriverScanEntityInvoker extends DriverInvoker {

	/**
	 * 构造扫描数据块命令的调用器
	 * @param mission 驱动任务
	 */
	public DriverScanEntityInvoker(DriverMission mission) {
		super(mission);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanEntity getCommand() {
		return (ScanEntity) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanEntity cmd = getCommand();
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

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ScanEntityProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ScanEntityProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		// 打印结果
		if (success) {
			setProduct(product);
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}

		return useful(success);
	}

}