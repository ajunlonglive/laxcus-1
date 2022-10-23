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
import com.laxcus.command.access.fast.*;
import com.laxcus.echo.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 数据块命令调用器。<br>
 * 它是“LoadIndex, StopIndex, LoadEntity, StopEntity”命令的超类，负责将命令分发到DATA站点。
 * 
 * @author scott.liang
 * @version 1.1 09/03/2012
 * @since laxcus 1.0
 */
public abstract class HomeFastMassInvoker extends HomeInvoker {

	/**
	 * 构造数据块操作调用器，指定它的命令
	 * @param cmd 数据块操作命令
	 */
	protected HomeFastMassInvoker(FastMass cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FastMass getCommand() {
		return (FastMass) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FastMass cmd = getCommand();
		Space space = cmd.getSpace();
		// 根据表名找到关联的DATA站点
		NodeSet set = DataOnHomePool.getInstance().findSites(space);
		List<Node> sites = (set != null ? set.show() : null);
		// 如果没有，是错误
		boolean success = (sites != null && sites.size() > 0);
		// 以容错模式，发送命令给指定的DATA站点
		if (success) {
			int count = incompleteTo(sites, cmd);
			success = (count > 0);
		}

		// 通知请求端，拒绝这个操作
		if (!success) {
			replyFault(Major.FAULTED, Minor.SITE_NOTFOUND);
		}

		return success;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		FastMassProduct product = new FastMassProduct();
		
		List<Integer> keys = super.getEchoKeys();
		for(int index : keys) {
			FastMassProduct sub = null;
			// 判断是成功
			if (isSuccessObjectable(index)) {
				try {
					sub = getObject(FastMassProduct.class, index);
				} catch (VisitException e) {
					Logger.error(e);
				}
			}
			// 判断出错或者正常，都要保存
			if (sub == null) {
				Node hub = findHub(index);
				FastMassItem item = new FastMassItem(hub, false);
				product.add(item);
			} else {
				product.addAll(sub);
			}
		}

		// 向目标地址发送记录
		boolean success = replyProduct(product);

		// 完成操作
		return useful(success);
	}

}
