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
import com.laxcus.command.stub.find.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;

/**
 * 查找数据块DATA从站点调用器。<br>
 * 
 * FindSlaveStubSite命令由DATA.delete/insert发出，目标是CALL站点，CALL站点返回每个数据块编号关联的全部从站点。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class CallFindStubSlaveSiteInvoker extends CallInvoker {

	/**
	 * 构造查找数据块DATA从站点调用器，指定命令
	 * @param cmd 查找数据块DATA从站点
	 */
	public CallFindStubSlaveSiteInvoker(FindStubSlaveSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindStubSlaveSite getCommand() {
		return (FindStubSlaveSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindStubSlaveSite cmd = getCommand();
		Space space = cmd.getSpace();
		FindStubSiteProduct product = new FindStubSiteProduct();

		// 去记录中查找数据块从站点
		for (Long stub : cmd.list()) {
			List<Node> list = null;
			// 查找匹配的站点
			if (space != null) {
				if (stub == 0) { // 如果是0编码，找到全部对应子站点
					list = DataOnCallPool.getInstance().findSlaveSites(space);
				} else {
					list = DataOnCallPool.getInstance().findSlaveSites(space, stub);
				}
			} else {
				list = DataOnCallPool.getInstance().findSlaveSites(stub);
			}
			// 空指针不处理
			if (list == null) {
				continue;
			}
			// 记录它
			for (Node hub : list) {
				product.add(hub, stub);
			}
		}

		// 发送给请求端
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "site size:%d, send to %s",
				product.size(),	cmd.getSource());

		// 退出
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