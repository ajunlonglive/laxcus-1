/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import java.util.*;

import com.laxcus.gate.pool.*;
import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.relate.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.site.Node;

/**
 * 获得授权人数据表调用器。<br>
 * FRONT发出命令，GATE站点查找授权人和他的数据表。
 * 
 * @author scott.liang
 * @version 1.0 8/6/2018
 * @since laxcus 1.0
 */
public class GateTakeAuthorizerTableInvoker extends GateInvoker {

	/**
	 * 构造查找CALL站点调用器，指定命令
	 * @param cmd 获得授权人数据表命令
	 */
	public GateTakeAuthorizerTableInvoker(TakeAuthorizerTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeAuthorizerTable getCommand() {
		return (TakeAuthorizerTable) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeAuthorizerTable cmd = getCommand();
		Siger authorizer = cmd.getAuthorizer();
		Siger conferrer = cmd.getConferrer();

		TakeAuthorizerTableProduct product = new TakeAuthorizerTableProduct();

		// 判断被授权人已经注册
		boolean success = ConferrerStaffOnGatePool.getInstance().contains(conferrer);
		// 判断FRONT站点已经注册
		if (success) {
			Node node = cmd.getSource().getNode();
			success = ConferrerFrontOnGatePool.getInstance().contains(node);
		}

		// 查找关联的CALL站点，保存它的表名和分布任务组件的阶段命名
		if (success) {
			// 拿到被授权人的资源引用
			Refer refer = ConferrerStaffOnGatePool.getInstance().findRefer(conferrer);
			success = (refer != null);
			if (success) {
				List<PassiveItem> items = refer.findPassiveItems(authorizer);
				for (PassiveItem item : items) {
					// 查找授权表
					Space space = item.getSpace();
					Table table = ConferrerStaffOnGatePool.getInstance().findTable(conferrer, space);
					if (table != null) {
						product.add(table);
					}
				}
			}
		}

		// 反馈处理结果到FRONT站点
		replyProduct(product);

		Logger.debug(this, "launch", success, "[%s] table size %d", authorizer, product.size());

		return useful(success);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}