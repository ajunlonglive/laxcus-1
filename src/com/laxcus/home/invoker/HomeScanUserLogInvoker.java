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
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 检索用户日志调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 4/2/2018
 * @since laxcus 1.0
 */
public class HomeScanUserLogInvoker extends HomeInvoker {

	/** 记录结果 **/
	private ScanUserLogProduct product = new ScanUserLogProduct();

	/**
	 * 构造检索用户日志调用器，指定命令
	 * @param cmd 检索用户日志命令
	 */
	public HomeScanUserLogInvoker(ScanUserLog cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanUserLog getCommand() {
		return (ScanUserLog) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanUserLog cmd = getCommand();
		Node node = cmd.getSourceSite();
		// 判断命令来自WATCH/TOP站点，否则拒绝
		boolean success = WatchOnHomePool.getInstance().contains(node);
		if (!success) {
			success = isHub(node);
		}
		if (!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}

		// 找匹配的CALL站点
		Map<Node, ScanUserLog> cmds = new TreeMap<Node, ScanUserLog>();
		for (Siger siger : cmd.getUsers()) {
			NodeSet set = CallOnHomePool.getInstance().findSites(siger);
			Node[] slaves = (set == null ? null : set.array());
			if (Laxkit.isEmpty(slaves)) {
				ScanUserLogItem item = new ScanUserLogItem(siger);
				product.add(item);
				continue;
			}
			// 保存地址
			for (Node slave : slaves) {
				ScanUserLog sub = cmds.get(slave);
				if (sub == null) {
					sub = new ScanUserLog();
					cmds.put(slave, sub);
				}
				sub.addUser(siger);
			}
		}

		// 如果是空值
		if (cmds.isEmpty()) {
			replyProduct(product);
			return useful();
		}

		// 生成命令
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		Iterator<Map.Entry<Node, ScanUserLog>> iterator = cmds.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, ScanUserLog> entry = iterator.next();
			CommandItem item = new CommandItem(entry.getKey(), entry.getValue());
			array.add(item);
		}
		
		// 发送到HOME站点
		int count = incompleteTo(array);
		// 判断成功
		success = (count > 0);
		if (!success) {
			replyProduct(product);
		}

		Logger.debug(this, "launch", success, "send size:%d, success count:%d",
				array.size(), count);

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
					ScanUserLogProduct e = getObject(ScanUserLogProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = replyProduct(product);

		Logger.debug(this, "ending", success, "Item Size:%d", product.size());

		return useful(success);
	}
	
}