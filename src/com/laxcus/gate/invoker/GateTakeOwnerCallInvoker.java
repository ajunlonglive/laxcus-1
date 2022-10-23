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
 * 获得账号所有人CALL站点调用器。<br>
 * 命令由FRONT站点发出，GATE站点根据注册名称，在本地检索匹配的参数，再返回给FRONT站点。<br><br>
 * 
 * 规则：<br>
 * 1. 允许FRONT节点任意时间发出TakeOwnerCall命令请求 <br>
 * 2. GATE节点的CallOnGatePool有超时时间，在时间范围内，只能提取内存中原来记录，超时后，才会去TOP/HOME，提取新的记录。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 7/1/2018
 * @since laxcus 1.0
 */
public class GateTakeOwnerCallInvoker extends GateInvoker {

	/**
	 * 构造查找CALL站点调用器，指定命令
	 * @param cmd 获得账号所有人CALL站点命令
	 */
	public GateTakeOwnerCallInvoker(TakeOwnerCall cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeOwnerCall getCommand() {
		return (TakeOwnerCall) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeOwnerCall cmd = getCommand();
		Siger siger = cmd.getSiger();
		
		// 判断FRONT命令来自公网
		Node from = getCommandSite();
		boolean fromWide = (from != null && from.getAddress().isWideAddress()); 

		TakeOwnerCallProduct product = new TakeOwnerCallProduct();
		// 检查间隔时间，通知FRONT节点
		product.setCheckInterval(CallOnGatePool.getInstance().getInterval());

		// 判断用户有效
		boolean success = StaffOnGatePool.getInstance().contains(siger);
		// 判断FRONT站点已经注册
		if (success) {
			Node node = cmd.getSource().getNode();
			success = FrontOnGatePool.getInstance().contains(node);
		}
		
		// 查找关联的CALL站点，保存它的表名和分布任务组件的阶段命名
		if (success) {
			List<CallItem> array = CallOnGatePool.getInstance().search(siger);
			// 保存参数
			for (CallItem item : array) {
				CallMember member = item.getMember();

				// 如果来自外网，取CALL节点的外网地址，否则是CALL节点内网
				if (fromWide) {
					Node node = item.getPublic();
					SiteHost host = node.getHost();
					// 如果有映射端口，取映射端口
					boolean has = (host.hasReflectTCPort() && host.hasReflectUDPort());
					if (has) {
						SiteHost reflect = new SiteHost(host.getAddress(), host.getReflectTCPort(), host.getReflectUDPort());
						node.setHost(reflect);
					}
					product.addPhases(node, member.getPhases());
					product.addSpaces(node, member.getTables());
					product.addCloudField(node, member.getCloudField());
					
					Logger.debug(this, "launch", "public node %s, has reflect port %s", node, (has ? "Yes" : "No"));
				} else {
					product.addPhases(item.getPrivate(), member.getPhases());
					product.addSpaces(item.getPrivate(), member.getTables());
					product.addCloudField(item.getPrivate(), member.getCloudField());
					
					Logger.debug(this, "launch", "private node %s", item.getPrivate());
				}
			}
		}

		// 反馈处理结果到FRONT站点
		replyProduct(product);

		Logger.debug(this, "launch", success, "[%s] tables sites: %d, phases sites: %d",
				siger, product.getSpaces().size(), product.getPhases().size());

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