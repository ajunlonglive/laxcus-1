/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 建立数据表命令调用器。<br>
 * 交给BANK站点去处理
 * 
 * @author scott.liang
 * @version 1.0 07/06/2018
 * @since laxcus 1.0
 */
public class GateCreateTableInvoker extends GateInvoker {

	/**
	 * 构造建立数据表的调用器
	 * @param cmd 建立数据表命令
	 */
	public GateCreateTableInvoker(CreateTable cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateTable getCommand() {
		return (CreateTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CreateTable cmd = getCommand();
		Table table = cmd.getTable();
		Space space = table.getSpace();
		
		// 判断操作者拥有建立数据表权限
		boolean success = canCreateTable(space);
		// 如果要求独享计算机资源，判断它！
		if (success && table.isExclusive()) {
			success = canExclusive();
		}
		
		if (success) {
			success = transmit(); // 转发给BANK站点
		}
		if (!success) {
			refuse();
		}
		return success;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 接受BANK节点的反馈!
		CreateTableProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateTableProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 判断成功
		boolean success = (product != null && product.isSuccessful());
		
		if (success) {
			// 判断FRONT命令来自公网
			Node from = getCommandSite();
			boolean fromWide = (from != null && from.getAddress().isWideAddress());
			
			// 调整网关地址，如果FRONT在内网，返回内网地址；如果在外网，返回外网地址！
			ArrayList<GatewayNode> array = new ArrayList<GatewayNode>();
			List<GatewayNode> gateways = product.list();
			for (GatewayNode node : gateways) {
				if (fromWide) {
					array.add(new GatewayNode(node.getPublic(), node.getPublic()));
				} else {
					array.add(new GatewayNode(node.getPrivate(), node.getPrivate()));
				}
			}
			
			// 重新调整后输出结果
			product.clear();
			product.addAll(array);
			success = replyProduct(product);
		} else {
			return reflect();
		}

		return useful(success);
	}

}