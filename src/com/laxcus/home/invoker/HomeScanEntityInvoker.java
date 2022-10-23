/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;
import com.laxcus.echo.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 扫描数据块命令调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 2/23/2013
 * @since laxcus 1.0
 */
public class HomeScanEntityInvoker extends HomeInvoker {

	/**
	 * 构造扫描数据块调用器
	 * @param cmd 扫描数据块命令
	 */
	public HomeScanEntityInvoker(ScanEntity cmd) {
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
	 * @see com.laxcus.pool.echo.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanEntity cmd = getCommand();
		Space space = cmd.getSpace();
		// 找到全部站点地址
		NodeSet set = DataOnHomePool.getInstance().findSites(space);
		List<Node> slaves = (set == null ? null : set.show());

		// 筛选站点
		ArrayList<Node> array = new ArrayList<Node>();
		if (slaves != null) {
			array.addAll(slaves);
			if (cmd.hasSites()) {
				List<Node> sites = cmd.getSites();
				array.retainAll(sites); // 保存匹配站点
			}
		}

		// 判断站点
		boolean success = (array.size() > 0);
		if (success) {
			int count = incompleteTo(slaves, cmd);
			success = (count > 0);
		}

		// 不成功，返回
		if (!success) {
			replyFault(Major.FAULTED, Minor.NOTFOUND);
			// replyProduct(new ScanEntityProduct());
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 汇总DATA站点信息，转发给TOP/WATCH站点。
		ScanEntityProduct product = new ScanEntityProduct();
		
		// 保存参数
		List<Integer> keys = getEchoKeys();
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
