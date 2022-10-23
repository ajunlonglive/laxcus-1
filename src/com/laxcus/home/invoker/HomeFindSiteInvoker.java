/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.site.find.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.data.*;

/**
 * 查询站点调用器
 * 
 * @author scott.liang
 * @version 1.0 5/28/2012
 * @since laxcus 1.0
 */
public class HomeFindSiteInvoker extends HomeInvoker {

	/**
	 * 构造查询站点调用器，指定命令
	 * @param cmd 查询站点命令
	 */
	public HomeFindSiteInvoker(FindSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindSite getCommand() {
		return (FindSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindSite cmd = getCommand();
		FindSiteTag tag = cmd.getTag();
		
		List<Node> nodes = null;
		if (SiteTag.isCall(tag.getFamily())) {
			nodes = doCallSites();
		} else if (SiteTag.isData(tag.getFamily())) {
			nodes = doDataSites(tag.getRank());
		} else if (SiteTag.isWork(tag.getFamily())) {
			nodes = doWorkSites();
		} else if (SiteTag.isBuild(tag.getFamily())) {
			nodes = doBuildSites();
		} else if (SiteTag.isLog(tag.getFamily())) {
			nodes = doLogSites();
		} else if (SiteTag.isWatch(tag.getFamily())) {
			nodes = doWatchSites();
		}

		// 查询结果
		FindSiteProduct product = new FindSiteProduct(tag);
		if (nodes != null) {
			product.addSites(nodes);
		}

		Logger.debug(this, "launch", "check %s site, size is %d",
				SiteTag.translate(tag.getFamily()),	product.getSites().size());

		// 发送处理结果
		super.replyProduct(product);

		// 成功完成
		return useful();
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
	 * 提取全部CALL站点
	 * @return - 返回CALL站点地址
	 */
	public List<Node> doCallSites() {
		return CallOnHomePool.getInstance().detail();
	}
	
	/**
	 * 提取全部WORK站点
	 * @return - 返回WORK站点地址
	 */
	public List<Node> doWorkSites() {
		return WorkOnHomePool.getInstance().detail();
	}
	
	/**
	 * 提取全部BUILD站点
	 * @return - 返回BUILD站点地址
	 */
	public List<Node> doBuildSites() {
		return BuildOnHomePool.getInstance().detail();
	}
	
	/**
	 * 提取全部LOG站点
	 * @return - 返回LOG站点地址
	 */
	public List<Node> doLogSites() {
		return LogOnHomePool.getInstance().detail();
	}
	
	/**
	 * 提取全部WATCH站点
	 * @return - 返回WATCH站点地址
	 */
	public List<Node> doWatchSites() {
		return WatchOnHomePool.getInstance().detail();
	}
	
	/**
	 * 根据DATA站点等级定义，提取全部DATA站点
	 * @param rank - 站点等级
	 * @return - 返回DATA站点地址
	 */
	public List<Node> doDataSites(byte rank) {
		List<Node> nodes = DataOnHomePool.getInstance().detail();
		ArrayList<Node> array = new ArrayList<Node>();

		if (RankTag.isMaster(rank)) {
			for (Node node : nodes) {
				DataSite site = (DataSite) DataOnHomePool.getInstance().find(node);
				if (site != null && site.isMaster()) {
					array.add(node);
				}
			}
		} else if (RankTag.isSlave(rank)) {
			for (Node node : nodes) {
				DataSite site = (DataSite) DataOnHomePool.getInstance().find(node);
				if (site != null && site.isSlave()) {
					array.add(node);
				}
			}
		} else {
			array.addAll(nodes);
		}
		return array;

	}
}
