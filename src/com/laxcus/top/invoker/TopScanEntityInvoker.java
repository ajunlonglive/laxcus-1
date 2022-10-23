/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 扫描数据块调用器
 * 
 * @author scott.liang
 * @version 1.0 1/18/2018
 * @since laxcus 1.0
 */
public class TopScanEntityInvoker extends TopInvoker {

	/**
	 * 构造扫描数据块调用器，指定命令
	 * @param cmd 扫描数据块
	 */
	public TopScanEntityInvoker(ScanEntity cmd) {
		super(cmd);
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
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanEntity cmd = getCommand();
		Space space = cmd.getSpace();

		// 查找关联的HOME站点
		NodeSet set = HomeOnTopPool.getInstance().find(space);
		List<Node> slaves = (set == null ? null : set.show());

		boolean success = (slaves != null && slaves.size() > 0);
		if (success) {
			int count = incompleteTo(slaves, cmd);
			success = (count > 0);
		}

		// 不成功，返回
		if (!success) {
			replyFault(Major.FAULTED, Minor.NOTFOUND);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ScanEntityProduct product = new ScanEntityProduct();
		List<Integer> keys = super.getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					ScanEntityProduct e = getObject(ScanEntityProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		replyProduct(product);

		return useful();
	}

}
