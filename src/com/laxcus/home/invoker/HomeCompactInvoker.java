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
import com.laxcus.command.rebuild.*;
import com.laxcus.echo.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.data.*;
import com.laxcus.visit.*;

/**
 * 压缩缓存数据块命令调用器
 * 
 * @author scott.liang
 * @version 1.0 3/28/2018
 * @since laxcus 1.0
 */
public class HomeCompactInvoker extends HomeInvoker {

	/**
	 * 建立压缩缓存数据块命令调用器，指定命令
	 * @param cmd 压缩缓存数据块命令
	 */
	public HomeCompactInvoker(Compact cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Compact getCommand() {
		return (Compact) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Compact cmd = getCommand();
		Space space = cmd.getSpace();

		// 判断表空间存在且允许
		boolean success = StaffOnHomePool.getInstance().allow(space);
		if (!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}
		
		TreeSet<Node> sites = new TreeSet<Node>();		
		// 如果指定DATA站点地址，查找它，否则是RUSH全部DATA主站点
		if (cmd.size() > 0) {
			for (Node node : cmd.list()) {
				DataSite site = (DataSite) DataOnHomePool.getInstance().find(node);
				if (site != null && site.isMaster()) {
					sites.add(node);
				}
			}
		} else {
			List<Node> list = DataOnHomePool.getInstance().findPrimeSites(space);
			sites.addAll(list);
		}
		
		// 没有找到主节点
		if (sites.isEmpty()) {
			replyFault(Major.FAULTED, Minor.NOTFOUND);
			return false;
		}
		// 生成副本命令，取消命令中的DATA站点地址
		Compact sub = new Compact(cmd.getSpace());
		// 以容错模式发送给下属DATA主节点
		int count = incompleteTo(sites, sub);
		success = (count > 0);
		
		// 不成功，通知请求端
		if (!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
		}

		Logger.debug(this, "launch", success, "send size:%d, success count:%d",
				sites.size(), count);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CompactProduct product = new CompactProduct();
		
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					CompactProduct e = getObject(CompactProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 发送回调用端
		boolean success = replyProduct(product);
		
		Logger.debug(this, "ending", success, "Item size:%d", product.size());

		return useful(success);
	}

}