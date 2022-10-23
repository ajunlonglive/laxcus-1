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
import com.laxcus.command.stub.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;

/**
 * 设置缓存映像数据块调用器 <br>
 * 
 * 对于这个命令，HOME站点起中转站的作用。它查找与数据表名关联的CALL站点，并且转发给这些CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 4/10/2011
 * @since laxcus 1.0
 */
public class HomeSetCacheReflexStubInvoker extends HomeInvoker {

	/**
	 * 构造设置缓存映像数据块调用器，指定命令
	 * @param cmd - 设置缓存映像数据块命令
	 */
	public HomeSetCacheReflexStubInvoker(SetCacheReflexStub cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetCacheReflexStub getCommand() {
		return (SetCacheReflexStub) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetCacheReflexStub cmd = getCommand();

		Map<Node, SetCacheReflexStub> sites = new TreeMap<Node, SetCacheReflexStub>();

		for (CacheReflexStub item : cmd.list()) {
			Space space = item.getSpace();
			NodeSet set = CallOnHomePool.getInstance().findSites(space);
			if (set == null) {
				continue;
			}
			for (Node node : set.show()) {
				SetCacheReflexStub sub = sites.get(node);
				if (sub == null) {
					sub = new SetCacheReflexStub();
					sub.setDirect(true);	// 单向操作
					sub.setQuick(true);		// 快速投递
					sites.put(node, sub);
				}
				sub.add(item);
			}
		}

		// 分配到不同的CALL站点
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		Iterator<Map.Entry<Node, SetCacheReflexStub>> iterator = sites.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, SetCacheReflexStub> entry = iterator.next();
			CommandItem item = new CommandItem(entry.getKey(), entry.getValue());
			array.add(item);
		}

		// 发送到CALL站点地址，不需要反馈
		int count = directTo(array, false);
		boolean success = (count == array.size());

		Logger.debug(this, "launch", success, "send sites: %d, successful sites: %d", array.size(), count);

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
