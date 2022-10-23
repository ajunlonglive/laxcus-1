/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 发布数据表到指定站点调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/14/2019
 * @since laxcus 1.0
 */
public class HomeDeployTableInvoker extends HomeInvoker {

	/** 发布结果 **/
	private DeployTableProduct product = new DeployTableProduct();

	/**
	 * 构造发布数据表到指定站点调用器，指定命令
	 * @param cmd 发布数据表到指定站点命令
	 */
	public HomeDeployTableInvoker(DeployTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployTable getCommand() {
		return (DeployTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DeployTable cmd = getCommand();
		Space space = cmd.getSpace();
		
		// 找到表
		Table table = StaffOnHomePool.getInstance().findTable(space);
		if(table == null) {
			replyFault();
			return useful(false);
		}
		
		// 找到签名
		Siger siger = table.getIssuer();
		Refer refer = StaffOnHomePool.getInstance().find(siger);
		if(refer == null) {
			replyFault();
			return useful(false);
		}
		
		List<Node> sites = cmd.getSites();
		
		// 以下操作是基于账号存在情况下，检查账号
		// 保存待发送的命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (Node node : sites) {
			boolean exists = false;
			if (node.isData()) {
				exists = DataOnHomePool.getInstance().contains(node);
			} else if (node.isCall()) {
				exists = CallOnHomePool.getInstance().contains(node);
			} else if (node.isWork()) {
				exists = WorkOnHomePool.getInstance().contains(node);
			} else if (node.isBuild()) {
				exists = BuildOnHomePool.getInstance().contains(node);
			}
			// 不存在，忽略它
			if (!exists) {
				continue;
			}

			// 强制建立引用
			DeployTable sub = new DeployTable(space);
			sub.setRefer(refer);
			sub.setTable(table);
			// 保存单元
			CommandItem item = new CommandItem(node, sub);
			array.add(item);
		}

		// 判断有记录存在
		boolean success = (array.size() > 0);
		// 以容错模式发送到下属节点
		if (success) {
			int count = incompleteTo(array);
			success = (count > 0);
		}
		// 不成功，直接返回结果
		if (!success) {
			replyProduct(product);
		}

		Logger.debug(this, "launch", success, "element size:%d", product.size());

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					DeployTableProduct e = getObject(DeployTableProduct.class, index);
					// 保存结果
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = replyProduct(product);

		Logger.debug(this, "ending", success, "element size:%d", product.size());

		return useful(success);
	}
	
}