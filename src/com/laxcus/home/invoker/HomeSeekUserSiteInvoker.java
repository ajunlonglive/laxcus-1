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
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 检索用户站点分布调用器
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class HomeSeekUserSiteInvoker extends HomeInvoker {

	/**
	 * 构造检索用户站点分布，指定命令
	 * @param cmd 检索用户站点分布命令
	 */
	public HomeSeekUserSiteInvoker(SeekUserSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekUserSite getCommand() {
		return (SeekUserSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekUserSite cmd = getCommand();

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

		SeekUserSiteProduct product = new SeekUserSiteProduct();
		for (Siger siger : users) {
			for (SeekSiteTag tag : cmd.getTags()) {
				List<Node> nodes = null;
				if (SiteTag.isCall(tag.getFamily())) {
					nodes = findCallSites(siger);
				} else if (SiteTag.isData(tag.getFamily())) {
					nodes = findDataSites(siger, tag.getRank());
				} else if (SiteTag.isWork(tag.getFamily())) {
					nodes = findWorkSites(siger);
				} else if (SiteTag.isBuild(tag.getFamily())) {
					nodes = findBuildSites(siger);
				}
				if (nodes == null) {
					continue;
				}
				for (Node node : nodes) {
					product.add(siger, node);
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
	 * 取出匹配用户的注册地址
	 * @param siger 用户签名
	 * @param rank DATA地址类型
	 * @return 地址列表
	 */
	private List<Node> findDataSites(Siger siger, byte rank) {
		if (RankTag.isMaster(rank)) {
			return DataOnHomePool.getInstance().findPrimeSites(siger);
		} else if (RankTag.isSlave(rank)) {
			return DataOnHomePool.getInstance().findSlaveSites(siger);
		} else {
			NodeSet set = DataOnHomePool.getInstance().findSites(siger);
			return (set != null ? set.show() : null);
		}
	}

	/**
	 * 取出匹配用户签名的注册地址
	 * @param siger 用户签名
	 * @return 地址列表
	 */
	private List<Node> findCallSites(Siger siger) {
		NodeSet set = CallOnHomePool.getInstance().findSites(siger);
		return (set != null ? set.show() : null);
	}

	/**
	 * 取出匹配用户签名的注册地址
	 * @param siger 用户签名
	 * @return 地址列表
	 */
	private List<Node> findWorkSites(Siger siger) {
		NodeSet set = WorkOnHomePool.getInstance().findSites(siger);
		return (set != null ? set.show() : null);
	}

	/**
	 * 取出匹配用户签名的注册地址
	 * @param siger 用户签名
	 * @return 地址列表
	 */
	private List<Node> findBuildSites(Siger siger) {
		NodeSet set = BuildOnHomePool.getInstance().findSites(siger);
		return (set != null ? set.show() : null);
	}
}