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
import com.laxcus.command.stub.site.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 查询数据块编号的从站点调用器。<br>
 * 命令由执行“数据优化（Regulate）”的DATA主站点发出，目标是关联CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 4/21/2013
 * @since laxcus 1.0
 */
public class CallSeekSlaveStubSiteInvoker extends CallInvoker {

	/**
	 * 建立数据块编号查询调用器，指定命令
	 * @param cmd - 
	 */
	public CallSeekSlaveStubSiteInvoker(SeekSlaveStubSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekSlaveStubSite getCommand() {
		return (SeekSlaveStubSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekSlaveStubSite cmd = getCommand();

		Space space = cmd.getSpace();
		List<Long> stubs = cmd.getStubs();

		// 站点集合
		Map<Node, SlaveStubSite> sites = new TreeMap<Node, SlaveStubSite>();

		// 循环查询
		for(long stub : stubs) {
			List<Node> nodes = DataOnCallPool.getInstance().findSlaveSites(space, stub);
			for(Node node : nodes) {
				SlaveStubSite site = sites.get(node);
				if(site == null) {
					site = new SlaveStubSite(node, space);
					sites.put(site.getSource(), site);
				}
				site.addStub(stub);
			}
		}

		// 保存参数
		SlaveStubSiteProduct product = new SlaveStubSiteProduct();
		for (SlaveStubSite site : sites.values()) {
			product.add(site);
		}
		// 发送报告
		boolean success = super.replyProduct(product);

		Logger.debug(this, "launch", success, "element size:%d", product.size());

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
