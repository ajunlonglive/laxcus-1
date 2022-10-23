/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.scan.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 扫描数据块命令调用器。
 * 
 * @author scott.liang
 * @version 1.2 2/23/2013
 * @since laxcus 1.0
 */
public class CallScanEntityInvoker extends CallInvoker {

	/**
	 * 构造扫描数据块命令调用器，指定命令
	 * @param cmd 扫描数据块命令
	 */
	public CallScanEntityInvoker(ScanEntity cmd) {
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

		// 查找关联的站点
		NodeSet set = DataOnCallPool.getInstance().findTableSites(space);

		// 判断站点地址
		List<Node> sites = (set != null ? set.show() : null);
		boolean success = (sites != null && sites.size() > 0);
		// 以容错发送到目标站点
		if (success) {
			int count = incompleteTo(sites, cmd);
			success = (count > 0);
		}

		// 不成功，向请求端反馈结果
		if (!success) {
			replyFault(Major.FAULTED, Minor.SITE_NOTFOUND);
		}

		// 返回
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ScanEntity cmd = getCommand();
		Space space = cmd.getSpace();
		ScanEntityItem item = new ScanEntityItem(getLocal(), space);

		// 统计数据
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			if (!isSuccessObjectable(index)) {
				continue;
			}
			// 统计参数
			try {
				ScanEntityProduct sub = getObject(ScanEntityProduct.class, index);
				for (ScanEntityItem e : sub.list()) {
					item.addLength(e.getLength());
					item.addStubs(e.getStubs());
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		Logger.debug(this, "ending", "stubs:%d, length:%d", item.getStubs(), item.getLength());

		// 返回结果
		ScanEntityProduct product = new ScanEntityProduct(item);
		replyProduct(product);

		return useful();
	}

}