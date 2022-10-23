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
import com.laxcus.command.access.user.*;
import com.laxcus.command.refer.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 发布用户到指定站点调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/1/2019
 * @since laxcus 1.0
 */
public class HomeDeployUserInvoker extends HomeInvoker {

	/** 发布结果 **/
	private DeployUserProduct product = new DeployUserProduct();

	/**
	 * 构造发布用户到指定站点调用器，指定命令
	 * @param cmd 发布用户到指定站点命令
	 */
	public HomeDeployUserInvoker(DeployUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployUser getCommand() {
		return (DeployUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DeployUser cmd = getCommand();
		List<Node> sites = cmd.getSites();
		List<Siger> sigers = cmd.getUsers();

		// 保存待发送的命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		
		// 签名
		for (Siger siger : sigers) {
			// 判断账号存在
			boolean success = StaffOnHomePool.getInstance().contains(siger);
			// 如果账号不存在，加载他！
			if (!success) {
				StaffOnHomePool.getInstance().loadRefer(siger);
			}
			// 找到引用
			Refer refer = StaffOnHomePool.getInstance().find(siger);
			// 判断有效
			success = (refer != null);
			// 不成功，忽略它
			if (!success) {
				continue;
			}
			
			// 以下操作是基于账号存在情况下，检查账号
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
				AwardCreateRefer sub = new AwardCreateRefer(refer);
				CommandItem item = new CommandItem(node, sub);
				array.add(item);
			}
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
					Node slave = getBufferHub(index);
					CreateUserProduct e = getObject(CreateUserProduct.class, index);
					// 保存结果
					product.add(e.getUsername(), slave, e.isSuccessful());
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