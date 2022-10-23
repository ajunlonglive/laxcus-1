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
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 查找用户签名站点调用器
 * 
 * @author scott.liang
 * @version 1.0 5/28/2012
 * @since laxcus 1.0
 */
public class HomeFindUserSiteInvoker extends HomeInvoker {

	/**
	 * 构造查找用户签名站点命令调用器，指定命令
	 * @param cmd 查找用户签名站点命令
	 */
	public HomeFindUserSiteInvoker(FindUserSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindUserSite getCommand() {
		return (FindUserSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindUserSite cmd = getCommand();
		FindSiteTag tag = cmd.getTag();
		Siger siger = cmd.getUsername();

		List<Node> nodes = null;
		if (SiteTag.isCall(tag.getFamily())) {
			nodes = findCallSite(siger);
		} else if (SiteTag.isData(tag.getFamily())) {
			nodes = findDataSite(tag.getRank(), siger);
		} else if (SiteTag.isWork(tag.getFamily())) {
			nodes = findWorkSite(siger);
		} else if (SiteTag.isBuild(tag.getFamily())) {
			nodes = findBuildSite(siger);
		}

		// 查询结果
		FindUserSiteProduct product = new FindUserSiteProduct(tag, siger);
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
	 * 查询CALL站点
	 * @param issuer
	 * @return
	 */
	private List<Node> findCallSite(Siger issuer) {
		NodeSet set = CallOnHomePool.getInstance().findSites(issuer);
		if (set != null) {
			return set.show();
		}
		return null;
	}
	
	/**
	 * 查询WORK站点
	 * @param issuer
	 * @return
	 */
	private List<Node> findWorkSite(Siger issuer) {
		NodeSet set = WorkOnHomePool.getInstance().findSites(issuer);
		if (set != null) {
			return set.show();
		}
		return null;
	}
	
	/**
	 * 查询BUILD站点
	 * @param issuer
	 * @return
	 */
	private List<Node> findBuildSite(Siger issuer) {
		NodeSet set = BuildOnHomePool.getInstance().findSites(issuer);
		if (set != null) {
			return set.show();
		}
		return null;
	}

	/**
	 * 查询DATA站点
	 * @param rank
	 * @param issuer
	 * @return
	 */
	private List<Node> findDataSite(byte rank, Siger issuer) {
		if (RankTag.isMaster(rank)) {
			return DataOnHomePool.getInstance().findPrimeSites(issuer);
		} else if (RankTag.isSlave(rank)) {
			return DataOnHomePool.getInstance().findSlaveSites(issuer);
		} else {
			NodeSet set = DataOnHomePool.getInstance().findSites(issuer);
			if (set != null) {
				return set.show();
			}
		}
		return null;
	}

}
