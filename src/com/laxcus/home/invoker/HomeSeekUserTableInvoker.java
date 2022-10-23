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
import com.laxcus.site.*;
import com.laxcus.site.build.*;
import com.laxcus.site.call.*;
import com.laxcus.site.data.*;
import com.laxcus.site.work.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 检索用户数据表分布调用器。
 * 
 * 在HOME集群中，数据表只存在于CALL/DATA两类站点。
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class HomeSeekUserTableInvoker extends HomeInvoker {

	/**
	 * 构造检索用户数据表分布，指定命令
	 * @param cmd 检索用户数据表分布命令
	 */
	public HomeSeekUserTableInvoker(SeekUserTable cmd) {
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
			users.addAll(CallOnHomePool.getInstance().getUsers());
			users.addAll(DataOnHomePool.getInstance().getUsers());
			users.addAll(WorkOnHomePool.getInstance().getUsers());
			users.addAll(BuildOnHomePool.getInstance().getUsers());
		} else {
			users.addAll(cmd.getUsers());
		}

		SeekUserTableProduct product = new SeekUserTableProduct();
		for (Siger siger : users) {
			for (SeekSiteTag tag : cmd.getTags()) {
				if (SiteTag.isCall(tag.getFamily())) {
					findCallSites(siger, product);
				} else if (SiteTag.isData(tag.getFamily())) {
					findDataSites(siger, tag.getRank(), product);
				} else if (SiteTag.isBuild(tag.getFamily())) {
					findBuildSites(siger, product);
				} else if (SiteTag.isWork(tag.getFamily())) {
					findWorkSites(siger, product);
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
	private void findCallSites(Siger siger, SeekUserTableProduct product) {
		NodeSet set = CallOnHomePool.getInstance().findSites(siger);
		if (set == null) {
			return;
		}
		ArrayList<Node> nodes = new ArrayList<Node>(set.show());

		Map<Seat, SeekUserTableItem> array = new TreeMap<Seat, SeekUserTableItem>();

		for (Node node : nodes) {
			CallSite site = (CallSite) CallOnHomePool.getInstance().find(node);
			if (site == null) {
				continue;
			}
			CallMember member = site.find(siger);
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
			item.addTables(member.getTables());
		}

		// 保存参数
		product.addAll(array.values());
	}

	/**
	 * 收集元数据
	 * @param siger 用户签名
	 * @param product 存储结果数据
	 */
	private void findDataSites(Siger siger, byte rank, SeekUserTableProduct product) {
		NodeSet set = DataOnHomePool.getInstance().findSites(siger);
		if (set == null) {
			return;
		}
		ArrayList<Node> nodes = new ArrayList<Node>(set.show());

		Map<Seat, SeekUserTableItem> array = new TreeMap<Seat, SeekUserTableItem>();

		for (Node node : nodes) {
			DataSite site = (DataSite) DataOnHomePool.getInstance().find(node);
			if (site == null) {
				continue;
			}
			
			if(RankTag.isMaster(rank) && !site.isMaster()) {
				continue;
			} else if(RankTag.isSlave(rank) && !site.isSlave()) {
				continue;
			}
			
			DataMember member = site.find(siger);
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
	
	/**
	 * 收集元数据
	 * @param siger 用户签名
	 * @param product 存储结果数据
	 */
	private void findWorkSites(Siger siger, SeekUserTableProduct product) {
		NodeSet set = WorkOnHomePool.getInstance().findSites(siger);
		if (set == null) {
			return;
		}
		ArrayList<Node> nodes = new ArrayList<Node>(set.show());

		Map<Seat, SeekUserTableItem> array = new TreeMap<Seat, SeekUserTableItem>();

		for (Node node : nodes) {
			WorkSite site = (WorkSite) WorkOnHomePool.getInstance().find(node);
			if (site == null) {
				continue;
			}
			WorkMember member = site.find(siger);
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
			item.addTables(member.getTables());
		}

		// 保存参数
		product.addAll(array.values());
	}
	
	/**
	 * 收集元数据
	 * @param siger 用户签名
	 * @param product 存储结果数据
	 */
	private void findBuildSites(Siger siger, SeekUserTableProduct product) {
		NodeSet set = BuildOnHomePool.getInstance().findSites(siger);
		if (set == null) {
			return;
		}
		ArrayList<Node> nodes = new ArrayList<Node>(set.show());

		Map<Seat, SeekUserTableItem> array = new TreeMap<Seat, SeekUserTableItem>();

		for (Node node : nodes) {
			BuildSite site = (BuildSite) BuildOnHomePool.getInstance().find(node);
			if (site == null) {
				continue;
			}
			BuildMember member = site.find(siger);
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
			item.addTables(member.getTables());
		}

		// 保存参数
		product.addAll(array.values());
	}
}
