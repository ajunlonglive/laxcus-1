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
import com.laxcus.access.stub.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.stub.reflex.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 查询缓存映像数据块站点调用器。<br>
 * 
 * 命令由DATA站点发出，HOME站点根据数据块编号和数据表名，返回对应的的DATA从站点（data slave site）。
 * 如果没有分配表，从当前站点中分配一个。
 * 
 * @author scott.liang
 * @version 1.12 07/31/2016
 * @since laxcus 1.0
 */
public class CallFindCacheReflexStubSiteInvoker extends CallInvoker {

	/**
	 * 构造查询缓存映像数据块站点调用器，指定命令
	 * @param cmd
	 */
	public CallFindCacheReflexStubSiteInvoker(FindCacheReflexStubSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindCacheReflexStubSite getCommand() {
		return (FindCacheReflexStubSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindCacheReflexStubSite cmd = getCommand();
		StubFlag flag = cmd.getFlag();
		Space space = flag.getSpace();
		long stub = flag.getStub();

		Logger.debug(this, "launch", "check %s", flag);
		
		ReflexStubSiteProduct product = new ReflexStubSiteProduct();

		// 查找与数据表名和数据块编号关联的DATA从站点
		List<Node> list = CacheReflexStubOnCallPool.getInstance().find(space, stub);
		// 以上不存在，找到从站点中分配一个
		if (list.isEmpty()) {
			list = DataOnCallPool.getInstance().createReflexCacheSite(space);
		}
		// 保存站点
		product.addAll(list);

		// 发送报告
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "%s slave site size %d",
				flag, product.size());

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
