/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.site.find.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;

/**
 * 查找数据表站点的异步调用器
 * 
 * @author scott.liang
 * @version 1.0 5/28/2012
 * @since laxcus 1.0
 */
public class HomeFindTableSiteInvoker extends HomeInvoker {

	/**
	 * 构造查表站点的异步命令调用器，指定命令
	 * @param cmd 查找数据表站点命令
	 */
	public HomeFindTableSiteInvoker(FindTableSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindTableSite getCommand() {
		return (FindTableSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindTableSite cmd = getCommand();
		FindSiteTag tag = cmd.getTag();
		Space space = cmd.getSpace();

		List<Node> nodes = null;
		if (SiteTag.isCall(tag.getFamily())) {
			nodes = findCallSite(space);
		} else if (SiteTag.isData(tag.getFamily())) {
			nodes = findDataSite(tag.getRank(), space);
		} else if(SiteTag.isWork(tag.getFamily())) {
			nodes = findWorkSite(space);
		} else if(SiteTag.isBuild(tag.getFamily())) {
			nodes = findBuildSite(space);
		}

		// 查询结果
		FindTableSiteProduct product = new FindTableSiteProduct(tag, space);
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
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
	
	/**
	 * 查询WORK站点
	 * @param space 数据表名
	 * @return 返回关联的站点地址列表，或者空指针
	 */
	private List<Node> findWorkSite(Space space) {
		NodeSet set = WorkOnHomePool.getInstance().findSites(space);
		return (set != null ? set.show() : null);
	}
	
	/**
	 * 查询BUILD站点
	 * @param space 数据表名
	 * @return 返回关联的站点地址列表，或者空指针
	 */
	private List<Node> findBuildSite(Space space) {
		NodeSet set = BuildOnHomePool.getInstance().findSites(space);
		return (set != null ? set.show() : null);
	}

	/**
	 * 查询CALL站点
	 * @param space 数据表名
	 * @return 返回关联的站点地址列表，或者空指针
	 */
	private List<Node> findCallSite(Space space) {
		NodeSet set = CallOnHomePool.getInstance().findSites(space);
		return (set != null ? set.show() : null);
	}
	

	/**
	 * 查询DATA站点
	 * @param rank DATA站点级别
	 * @param space 数据表名
	 * @return
	 */
	private List<Node> findDataSite(byte rank, Space space) {
		if (RankTag.isMaster(rank)) {
			return DataOnHomePool.getInstance().findPrimeSites(space);
		} else if (RankTag.isSlave(rank)) {
			DataOnHomePool.getInstance().findSlaveSites(space);
		} else {
			NodeSet set = DataOnHomePool.getInstance().findSites(space);
			if (set != null) {
				return set.show();
			}
		}
		return null;
	}

}
