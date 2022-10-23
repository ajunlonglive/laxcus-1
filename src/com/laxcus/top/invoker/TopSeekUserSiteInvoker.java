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
import com.laxcus.top.pool.*;
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
public class TopSeekUserSiteInvoker extends TopInvoker {

	/**
	 * 构造检索用户站点分布，指定命令
	 * @param cmd 检索用户站点分布命令
	 */
	public TopSeekUserSiteInvoker(SeekUserSite cmd) {
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
		if(cmd.isAllUser()) {
			users.addAll(HomeOnTopPool.getInstance().getSigers());
		} else {
			users.addAll(cmd.getUsers());
		}

		SeekUserSiteProduct product = new SeekUserSiteProduct();
		for (Siger siger : users) {
			for (SeekSiteTag tag : cmd.getTags()) {
				List<Node> nodes = null;
				if (SiteTag.isHome(tag.getFamily())) {
					nodes = findHomeSites(siger);
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

	/**
	 * 取出匹配用户签名的注册地址
	 * @param siger 用户签名
	 * @return 地址列表
	 */
	private List<Node> findHomeSites(Siger siger) {
		NodeSet set = HomeOnTopPool.getInstance().findSites(siger);
		return (set != null ? set.show() : null);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		SeekUserSite cmd = getCommand();
	//		
	//		TreeSet<Siger> users = new TreeSet<Siger>();
	//		if(cmd.isAllUser()) {
	//			users.addAll(ArchiveOnTopPool.getInstance().getSigers());
	//			users.addAll(HomeOnTopPool.getInstance().getSigers());
	//			users.addAll(AidOnTopPool.getInstance().getSigers());
	//		} else {
	//			users.addAll(cmd.getUsers());
	//		}
	//		
	//		SeekUserSiteProduct product = new SeekUserSiteProduct();
	//		for (Siger siger : users) {
	//			for (SeekSiteTag tag : cmd.getTags()) {
	//				List<Node> nodes = null;
	//				if (SiteTag.isAid(tag.getFamily())) {
	//					nodes = findAidSites(siger);
	//				} else if (SiteTag.isHome(tag.getFamily())) {
	//					nodes = findHomeSites(siger);
	//				} else if (SiteTag.isArchive(tag.getFamily())) {
	//					nodes = findArchiveSites(siger);
	//				}
	//				if (nodes == null) {
	//					continue;
	//				}
	//				for (Node node : nodes) {
	//					product.add(siger, node);
	//				}
	//			}
	//		}
	//
	//		boolean success = replyProduct(product);
	//		
	//		Logger.debug(this, "launch", success, "item size:%d", product.size());
	//		
	//		return useful(success);
	//	}



	//	/**
	//	 * 取出匹配用户签名的注册地址
	//	 * @param siger 用户签名
	//	 * @return 地址列表
	//	 */
	//	private List<Node> findAidSites(Siger siger) {
	//		ArrayList<Node> a = new ArrayList<Node>();
	//		Node node = AidOnTopPool.getInstance().find(siger);
	//		if (node != null) {
	//			a.add(node);
	//		}
	//		return a;
	//	}



	//	/**
	//	 * 取出匹配用户签名的注册地址
	//	 * @param siger 用户签名
	//	 * @return 地址列表
	//	 */
	//	private List<Node> findArchiveSites(Siger siger) {
	//		NodeSet set = ArchiveOnTopPool.getInstance().findSites(siger);
	//		return (set != null ? set.show() : null);
	//	}

}
