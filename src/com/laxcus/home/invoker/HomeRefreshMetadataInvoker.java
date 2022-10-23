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
import com.laxcus.command.field.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 刷新元数据命令调用器。
 * 
 * HOME站点根据签名找到关联的DATA/BUILD/WORK站点，通知它们发送元数据到CALL站点
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public class HomeRefreshMetadataInvoker extends HomeInvoker {

	/**
	 * 刷新元数据命令调用器，指定命令
	 * @param cmd 刷新元数据命令
	 */
	public HomeRefreshMetadataInvoker(RefreshMetadata cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshMetadata getCommand() {
		return (RefreshMetadata) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RefreshMetadata cmd = getCommand();

		// 将已经记录在CALL站点上的签名和新请求的签名合并
		TreeSet<Siger> sigers = new TreeSet<Siger>();
		sigers.addAll(cmd.getUsers());
		for (Siger siger : cmd.getUsers()) {
			NodeSet set = CallOnHomePool.getInstance().findSites(siger);
			if (set == null) {
				continue;
			}
			for (Node node : set.show()) {
				List<Siger> a = CallOnHomePool.getInstance().findUsers(node);
				if (a != null) sigers.addAll(a);
			}
		}

		/** DATA/BUILD/WORK站点地址 - 投递CALL命令 **/
		TreeMap<Node, SelectFieldToCall> array = new TreeMap<Node, SelectFieldToCall>();

		// 处理结果
		RefreshMetadataProduct product = new RefreshMetadataProduct();

		// 根据用户签名，查找关联的CALL/DATA/BUILD/WORK站点。要求DATA/BUILD/WORK站点，向CALL站点投递“SelectFiledToCall”命令。
		for(Siger siger : sigers) {
			// 找到关联的CALL站点
			NodeSet set = CallOnHomePool.getInstance().findSites(siger);
			if (set == null) {
				if (cmd.contains(siger)) product.add(null, siger, false); // 是请求刷新的签名，记录!
				continue;
			}

			for (Node call : set.show()) {
				// 选择关联的DATA站点
				NodeSet nodes = DataOnHomePool.getInstance().findSites(siger);
				choice(call, siger, nodes, array);
				// 选择关联的BUILD站点
				nodes = BuildOnHomePool.getInstance().findSites(siger);
				choice(call, siger, nodes, array);
				// 选择关联的WORK站点
				nodes = WorkOnHomePool.getInstance().findSites(siger);
				choice(call, siger, nodes, array);
			}
		}

		// 向关联的DATA/WORK/BUILD站点发送“SelectFieldToCall”命令
		Iterator<Map.Entry<Node, SelectFieldToCall>> iterator = array.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, SelectFieldToCall> entry = iterator.next();
			Node site = entry.getKey();
			SelectFieldToCall select = entry.getValue();

			// 向目标站点逐一投递，不需要反馈结果
			boolean success = directTo(site, select);
			// 记录结果
			for (Siger siger : select.list()) {
				if (cmd.contains(siger)) product.add(site, siger, success); // 只记录用户请求的
			}
		}

		Logger.debug(this, "launch", "site count:%d, product size:%d", array.size(), product.size());

		// 投递结果
		boolean success = replyProduct(product);

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

	/**
	 * 选择匹配的DATA/BUILD/WORK节点
	 * @param call
	 * @param issuer
	 * @param set
	 * @param array
	 */
	private void choice(Node call, Siger issuer, NodeSet set,
			Map<Node, SelectFieldToCall> array) {
		// 空值忽略
		if (set == null) {
			return;
		}

		for (Node node : set.show()) {
			SelectFieldToCall cmd = array.get(node);
			if (cmd == null) {
				cmd = new SelectFieldToCall(call);
				array.put(node, cmd);
			}
			cmd.add(issuer);
		}
	}
}
