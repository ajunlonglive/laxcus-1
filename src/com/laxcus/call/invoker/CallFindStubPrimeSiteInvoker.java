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
import com.laxcus.site.*;

/**
 * FindStubPrimeSite命令调用器 <br>
 * 
 * 命令从WORK站点发出，CALL站点接收，从内存中给每个数据块编号选择一个关联的DATA主站点地址
 * 
 * @author scott.liang
 * @version 06/20/2013
 * @since laxcus 1.0
 */
public class CallFindStubPrimeSiteInvoker extends CallInvoker {

	/**
	 * 构造FindStubPrimeSite命令调用器
	 * @param cmd - FindStubPrimeSite命令
	 */
	public CallFindStubPrimeSiteInvoker(FindStubPrimeSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindStubPrimeSite getCommand() {
		return (FindStubPrimeSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindStubPrimeSite cmd = getCommand();
		Space space = cmd.getSpace();
		FindStubSiteProduct product = new FindStubSiteProduct();

		// 去记录中查找数据块主站点
		for (Long stub : cmd.list()) {
			List<Node> list = null;
			// 查找匹配的站点
			if (space != null) {
				list = DataOnCallPool.getInstance().findPrimeSites(space, stub);
			} else {
				list = DataOnCallPool.getInstance().findPrimeSites(stub);
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

		Logger.debug(this, "launch", success, "StubEntry size:%d, send to %s",
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
