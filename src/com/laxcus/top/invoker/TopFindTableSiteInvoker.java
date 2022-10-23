/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.site.find.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.set.*;

/**
 * 查找数据表站点的异步调用器。
 * 
 * @author scott.liang
 * @version 1.1 8/2/2013
 * @since laxcus 1.0
 */
public class TopFindTableSiteInvoker extends TopInvoker {

	/**
	 * 构造查找数据表站点的异步调用器
	 * @param cmd 查找数据表站点命令
	 */
	public TopFindTableSiteInvoker(FindTableSite cmd) {
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
	 * @see com.laxcus.top.runner.EchoRunner#launch()
	 */
	@Override
	public boolean launch() {
		FindTableSite cmd = getCommand();
		FindSiteTag tag = cmd.getTag();
		Space space = cmd.getSpace();

		List<Node> list = null;
		if (SiteTag.isHome(tag.getFamily())) {
			list = findHomeSite(space);
		}

		// 查询结果
		FindTableSiteProduct product = new FindTableSiteProduct(tag, space);
		if (list != null) {
			product.addSites(list);
		}

		Logger.debug(this, "launch", "check %s site, size is %d",
				SiteTag.translate(tag.getFamily()), product.getSites().size());

		// 发送处理结果
		super.replyProduct(product);

		// 成功完成
		return useful();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 查询数据表名关联HOME站点
	 * @param space - 数据表名
	 * @return - HOME节点地址集合
	 */
	private List<Node> findHomeSite(Space space) {
		NodeSet set = HomeOnTopPool.getInstance().find(space);
		if (set != null) {
			return set.show();
		}
		return null;
	}

	//	public boolean launch1() {
	//		FindSpaceSite cmd = (FindSpaceSite) super.getCommand();
	//		// 在TOP站点查找与表匹配的HOME站点
	//		if (cmd.getFamily() == SiteTag.HOME_SITE) {
	//			SpaceSiteProduct product = new SpaceSiteProduct();
	//			for (Space key : cmd.getSites()) {
	////				List<Node> list = HomeOnTopPool.getInstance().findSite(key);
	////				for (Node node : list) {
	////					product.add(key, node);
	////				}
	//				
	//				NodeSet set = HomeOnTopPool.getInstance().find(key);
	//				if (set != null) {
	//					product.addAll(key, set.show());
	//				}
	//				
	////				SiteHost[] array = HomePool.getInstance().find(key);
	////				if (Laxkit.isEmpty(array)) {
	////					continue;
	////				}
	////				for (int i = 0; i < array.length; i++) {
	////					Node node = new Node(NodeTag.HOME_SITE, array[i]);
	////					product.add(key, node);
	////				}
	//			}
	//			Logger.debug(this, "launch", "site is %d", product.size());
	//			
	//			super.sendObject(product);
	//			return super.useful();
	//		} else {
	//			List<Node> list = HomeOnTopPool.getInstance().getNodes();
	//			if (list.isEmpty()) {
	//				super.sendObject(new SpaceSiteProduct());
	//				return useful(); // 退出返回
	//			} else {
	//				// 转发给全部HOME站点去查找
	//				return super.launchTo(list);
	//			}
	//
	////			SiteHost[] sites = HomePool.getInstance().getAllHosts();
	////			// 如果没有地址，返回一个空的结果
	////			if (Laxkit.isEmpty(sites)) {
	////				super.sendObject(new SpaceSiteProduct());
	////				return fully(); // 退出返回
	////			} else {
	////				// 转发给HOME站点去查找
	////				return super.launchTo(sites);
	////			}
	//		}
	//	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	//	 */
	//	@Override
	//	public boolean ending() {
	//		// 如果存在故障，通知调用端
	//		if (!super.isSuccessObjectable()) {
	//			super.replyFault();
	//			return useful();
	//		}
	//
	//		SpaceSiteProduct product = new SpaceSiteProduct();
	//
	//		int size = super.getBufferSize();
	//		for (int index = 0; index < size; index++) {
	//			try {
	//				Object param = super.getObject(index);
	//				product.join((SpaceSiteProduct) param);
	//			} catch (VisitException e) {
	//				Logger.error(e);
	//			}
	//		}
	//
	//		Logger.debug(this, "ending", "size is %d", product.size());
	//
	//		// 反馈处理结果
	//		super.replyObject(product);
	//		return useful();
	//	}

}