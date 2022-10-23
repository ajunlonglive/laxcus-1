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
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.site.*;

/**
 * 检索用户分布区域调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/29/2019
 * @since laxcus 1.0
 */
public class HomeSeekUserAreaInvoker extends HomeInvoker {

	/**
	 * 构造检索用户分布区域调用器，指定命令
	 * @param cmd 检索用户分布区域命令
	 */
	public HomeSeekUserAreaInvoker(SeekUserArea cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekUserArea getCommand() {
		return (SeekUserArea) super.getCommand();
	}
	
	/**
	 * 查找站点
	 * @param siger 用户签名
	 * @return 返回节点地址
	 */
	private List<Node> findSites(Siger siger) {
		TreeSet<Node> array = new TreeSet<Node>();
		
		// 判断在HOME管理池存在！
		boolean success = StaffOnHomePool.getInstance().contains(siger);
		if (success) {
			array.add(getLocal());
		}

		NodeSet set = CallOnHomePool.getInstance().findSites(siger);
		if (set != null) {
			array.addAll(set.show());
		}
		set = DataOnHomePool.getInstance().findSites(siger);
		if (set != null) {
			array.addAll(set.show());
		}
		set = BuildOnHomePool.getInstance().findSites(siger);
		if (set != null) {
			array.addAll(set.show());
		}
		set = WorkOnHomePool.getInstance().findSites(siger);
		if (set != null) {
			array.addAll(set.show());
		}
		return new ArrayList<Node>(array);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekUserArea cmd = getCommand();
		
		SeekUserAreaProduct product = new SeekUserAreaProduct();

		for (Siger siger : cmd.getUsers()) {
			List<Node> nodes = findSites(siger);
			for (Node node : nodes) {
				product.add(siger, node);
			}
		}
		// 返回结果
		replyProduct(product);

		Logger.debug(this, "launch", "node count:%d", product.size());

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
	
}