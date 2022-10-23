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
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 查询阶段命名站点调用器
 * 
 * @author scott.liang
 * @version 1.0 5/28/2012
 * @since laxcus 1.0
 */
public class HomeFindPhaseSiteInvoker extends HomeInvoker {

	/**
	 * 构造查询阶段命名站点调用器，指定命令
	 * @param cmd 查询阶段命名站点命令
	 */
	public HomeFindPhaseSiteInvoker(FindPhaseSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindPhaseSite getCommand() {
		return (FindPhaseSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindPhaseSite cmd = getCommand();
		FindSiteTag tag = cmd.getTag();
		Phase phase = cmd.getPhase();

		List<Node> nodes = null;
		if (SiteTag.isCall(tag.getFamily())) {
			nodes = findCallSite(phase);
		} else if (SiteTag.isData(tag.getFamily())) {
			nodes = findDataSite(tag.getRank(), phase);
		} else if(SiteTag.isWork(tag.getFamily())) {
			nodes = findWorkSite(phase);
		} else if(SiteTag.isBuild(tag.getFamily())) {
			nodes = findBuildSite(phase);
		}

		// 查询结果
		FindPhaseSiteProduct product = new FindPhaseSiteProduct(tag, phase);
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


	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	private List<Node> findCallSite(Phase phase) {
		NodeSet set = CallOnHomePool.getInstance().findSites(phase);
		if (set != null) {
			return set.show();
		}
		return null;
	}

	private List<Node> findWorkSite(Phase phase) {
		NodeSet set = WorkOnHomePool.getInstance().findSites(phase);
		if (set != null) {
			return set.show();
		}
		return null;
	}

	private List<Node> findBuildSite(Phase phase) {
		NodeSet set = BuildOnHomePool.getInstance().findSites(phase);
		if (set != null) {
			return set.show();
		}
		return null;
	}

	private List<Node> findDataSite(byte rank, Phase phase) {
		NodeSet set = DataOnHomePool.getInstance().findSites(phase);
		if (set == null) {
			return null;
		}

		List<Node> nodes = set.show();
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
			array.addAll( nodes);
		}
		return array;
	}

	//	/**
	//	 * 查找站点
	//	 * @param cmd
	//	 * @return
	//	 */
	//	private PhaseSiteProduct doCall(FindPhaseSite cmd) {
	//		PhaseSiteProduct product = new PhaseSiteProduct();
	//
	//		for (Naming naming : cmd.getSites()) {
	//			Phase key = new Phase(cmd.getFamily(), naming);
	//			NodeSet set = CallOnHomePool.getInstance().findSites(key);
	//			if (set != null) {
	//				product.addAll(key, set.show());
	//			}
	//		}
	//
	//		return product;
	//	}
	//
	//	private PhaseSiteProduct doData(FindPhaseSite cmd) {
	//		PhaseSiteProduct product = new PhaseSiteProduct();
	//
	//		//		for (Naming naming : cmd.list()) {
	//		//			Phase key = new Phase(cmd.getFamily(), naming);
	//		//			SiteHost[] sites = DataPool.getInstance().findSite(key);
	//		//			if (Laxkit.isEmpty(sites)) {
	//		//				continue;
	//		//			}
	//		//			for (int i = 0; i < sites.length; i++) {
	//		//				product.add(key, new Node(NodeTag.DATA_SITE, sites[i]));
	//		//			}
	//		//		}
	//
	//		return product;
	//	}
	//
	//	private PhaseSiteProduct doWork(FindPhaseSite cmd) {
	//		PhaseSiteProduct product = new PhaseSiteProduct();
	//
	//		for (Naming naming : cmd.getSites()) {
	//			Phase key = new Phase(cmd.getFamily(), naming);
	//			List<Node> sites = WorkOnHomePool.getInstance().enumlate(key);
	//			product.addAll(key, sites);
	//
	//			// for (int i = 0; i < sites.size(); i++) {
	//			// product.add(key, new Node(NodeTag.WORK_SITE, sites.get(i)));
	//			// }
	//		}
	//
	//		return product;
	//	}
	//
	//	private PhaseSiteProduct doBuild(FindPhaseSite cmd) {
	//		PhaseSiteProduct product = new PhaseSiteProduct();
	//
	//		//		for (Naming naming : cmd.list()) {
	//		//			Phase key = new Phase(cmd.getFamily(), naming);
	//		//			SiteHost[] sites = BuildPool.getInstance().findSite(key);
	//		//			if (Laxkit.isEmpty(sites)) {
	//		//				continue;
	//		//			}
	//		//			for (int i = 0; i < sites.length; i++) {
	//		//				product.add(key, new Node(NodeTag.BUILD_SITE, sites[i]));
	//		//			}
	//		//		}
	//
	//		return product;
	//	}

}