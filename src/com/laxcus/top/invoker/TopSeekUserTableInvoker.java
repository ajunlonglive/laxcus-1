/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.home.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 检索用户数据表分布调用器。<br>
 * 
 * 在TOP站点上，只检索HOME节点的数据表。
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class TopSeekUserTableInvoker extends TopInvoker {

	/**
	 * 构造检索用户数据表分布，指定命令
	 * @param cmd 检索用户数据表分布命令
	 */
	public TopSeekUserTableInvoker(SeekUserTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekUserTable getCommand() {
		return (SeekUserTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekUserTable cmd = getCommand();

		TreeSet<Siger> users = new TreeSet<Siger>();
		// 匹配全部，取出全部用户签名
		if (cmd.isAllUser()) {
			users.addAll(HomeOnTopPool.getInstance().getSigers());
		} else {
			users.addAll(cmd.getUsers());
		}

		SeekUserTableProduct product = new SeekUserTableProduct();
		for (Siger siger : users) {
			for (SeekSiteTag tag : cmd.getTags()) {
				if (SiteTag.isHome(tag.getFamily())) {
					findHomeSites(siger, product);
				}
			}
		}

		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "item size:%d", product.size());

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
	 * 收集元数据
	 * @param siger 用户签名
	 * @param product 存储结果数据
	 */
	private void findHomeSites(Siger siger, SeekUserTableProduct product) {
		NodeSet set = HomeOnTopPool.getInstance().findSites(siger);
		if (set == null) {
			return;
		}

		ArrayList<Node> nodes = new ArrayList<Node>(set.show());

		Map<Seat, SeekUserTableItem> array = new TreeMap<Seat, SeekUserTableItem>();

		for (Node node : nodes) {
			HomeSite site = (HomeSite) HomeOnTopPool.getInstance().find(node);
			if (site == null) {
				continue;
			}
			HomeMember member = site.find(siger);
			if (member == null) {
				continue;
			}
			// 形成映像
			Seat seat = new Seat(siger, site.getNode());
			SeekUserTableItem item = array.get(seat);
			if (item == null) {
				item = new SeekUserTableItem(seat);
				array.put(seat, item);
			}
			// 保存表名
			item.addTables(member.getSpaces());
		}
		// 保存参数
		product.addAll(array.values());
	}

}