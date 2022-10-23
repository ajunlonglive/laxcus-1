/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.command.refer.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 从指定节点清除用户调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/2/2019
 * @since laxcus 1.0
 */
public class HomeEraseUserInvoker extends HomeInvoker {

	/** 发布结果 **/
	private EraseUserProduct product = new EraseUserProduct();

	/**
	 * 构造从指定节点清除用户调用器，指定命令
	 * @param cmd 从指定节点清除用户命令
	 */
	public HomeEraseUserInvoker(EraseUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public EraseUser getCommand() {
		return (EraseUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		EraseUser cmd = getCommand();
		List<Node> sites = cmd.getSites();
		List<Siger> sigers = cmd.getUsers();

		// 保存待发送的命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		
		// 签名
		for (Siger siger : sigers) {
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

				// 强制删除引用
				AwardDropRefer sub = new AwardDropRefer(siger);
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
					DropUserProduct e = getObject(DropUserProduct.class, index);
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