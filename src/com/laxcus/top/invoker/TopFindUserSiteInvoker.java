/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.site.find.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 查找用户签名站点的异步调用器
 * 
 * @author scott.liang
 * @version 1.0 5/28/2012
 * @since laxcus 1.0
 */
public class TopFindUserSiteInvoker extends TopInvoker {

	/**
	 * 构造用户签名站点的异步命令调用器，指定命令
	 * @param cmd 查找用户签名站点命令
	 */
	public TopFindUserSiteInvoker(FindUserSite cmd) {
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
		if (SiteTag.isHome(tag.getFamily())) {
			nodes = findHomeSite(siger);
		} 
		
//		else if (SiteTag.isAid(tag.getFamily())) {
//			nodes = findAidSite(siger);
//		} else if (SiteTag.isArchive(tag.getFamily())) {
//			nodes = findArchiveSite(siger);
//		}

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
	 * 查询HOME站点
	 * @param siger 用户签名
	 * @return 返回站点列表，或者空指针
	 */
	private List<Node> findHomeSite(Siger siger) {
		NodeSet set = HomeOnTopPool.getInstance().findSites(siger);
		return (set != null ? set.show() : null);
	}

//	/**
//	 * 查询AID站点
//	 * @param siger 用户签名
//	 * @return 返回站点列表，或者空指针
//	 */
//	private List<Node> findAidSite(Siger siger) {
//		ArrayList<Node> array = new ArrayList<Node>();
//		Node node = OldAidOnTopPool.getInstance().find(siger);
//		if (node != null) {
//			array.add(node);
//		}
//		return array;
//	}

//	/**
//	 * 查询ARCHIVE站点
//	 * @param siger 用户签名
//	 * @return 返回站点列表，或者空指针
//	 */
//	private List<Node> findArchiveSite(Siger siger) {
//		NodeSet set = OldArchiveOnTopPool.getInstance().findSites(siger);
//		return (set != null ? set.show() : null);
//	}

}