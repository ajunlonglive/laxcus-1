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
import com.laxcus.command.relate.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.site.Node;
import com.laxcus.site.call.*;

/**
 * 查找授权人CALL站点调用器。<br>
 * FRONT发出命令，GATE站点查找授权人和他的CALL站点地址
 * 
 * @author scott.liang
 * @version 1.0 8/6/2018
 * @since laxcus 1.0
 */
public class GateTakeAuthorizerCallInvoker extends GateInvoker {

	/**
	 * 构造查找CALL站点调用器，指定命令
	 * @param cmd 查找授权人CALL站点命令
	 */
	public GateTakeAuthorizerCallInvoker(TakeAuthorizerCall cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeAuthorizerCall getCommand() {
		return (TakeAuthorizerCall) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeAuthorizerCall cmd = getCommand();
		Siger authorizer = cmd.getAuthorizer();
		Siger conferrer = cmd.getConferrer();
		
		// 判断命令来自外肉
		Node from = getCommandSite();
		boolean fromWide = (from != null && from.getAddress().isWideAddress());

		TakeAuthorizerCallProduct product = new TakeAuthorizerCallProduct(authorizer);

		// 判断被授权人已经注册
		boolean success = ConferrerStaffOnGatePool.getInstance().contains(conferrer);
		// 判断FRONT站点已经注册
		if (success) {
			Node node = cmd.getSource().getNode();
			success = ConferrerFrontOnGatePool.getInstance().contains(node);
		}
		
		Logger.debug(this, "launch", success, "check authorizer/conferrer [%s]/[%s]", authorizer, conferrer);

		// 查找关联的CALL站点，保存它的表名
		if (success) {
			List<CallItem> array = CallOnGatePool.getInstance().search(authorizer);
			
			// 保存参数
			for(CallItem item : array) {
				CallMember member = item.getMember();
				
				// 判断FRONT来自公网或者内网，返回对应的CALL节点主机地址
				if (fromWide) {
					Node node = item.getPublic();
					SiteHost host = node.getHost();
					// 如果包含映射端口时，使用新的映射端口
					boolean has = (host.hasReflectTCPort() && host.hasReflectUDPort());
					if (has) {
						SiteHost reflect = new SiteHost(host.getAddress(), host.getReflectTCPort(), host.getReflectUDPort());
						node.setHost(reflect);
					}
					product.addSpaces(node, member.getTables());
					
					Logger.debug(this, "launch", "public node %s, has reflect port %s", node, (has ? "Yes" : "No"));
				} else {
					product.addSpaces(item.getPrivate(), member.getTables());
					
					Logger.debug(this, "launch", "private node %s", item.getPrivate());
				}
			}
		}

		// 反馈处理结果到FRONT站点
		replyProduct(product);

		Logger.debug(this, "launch", success, "[%s] table sites: %d",
				authorizer, product.getSpaces().size());

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