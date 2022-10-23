/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.home;

import com.laxcus.fixp.*;
import com.laxcus.home.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;
import com.laxcus.visit.hub.*;

/**
 * HOME登录访问管理接口，HubVisit接口的HOME站点实现。<br><br>
 * 
 * 提供针对HOME站点的HubVisit远程访问。
 * 
 * @author scott.liang
 * @version 1.0 2/2/2009
 * @since laxcus 1.0
 */
public class HubVisitOnHome implements HubVisit {

	/**
	 * 构造HOME登录访问管理接口
	 */
	public HubVisitOnHome() {
		super();
	}

	/**
	 * 注册/注销站点的许可检测。当前站点如果处于“监视器”状态，不接受注册和注销。
	 * @param node 通信站点地址 
	 * @return 接受返回“真”，否则“假”。
	 */
	private boolean accept(Node node) {
		if (node == null) {
			return false;
		}
		// 当前TOP站点是监视站点，拒绝任何注册和注销
		boolean success = HomeLauncher.getInstance().isMonitor();
		if (success) {
			return false;
		}

		return true;
	}

	/**
	 * 注册/注销站点的许可检测。当前站点如果处于“监视器”状态，不接受注册和注销。
	 * @param site 注册/注销站点地址
	 * @return 接受返回“真”，否则“假”。
	 */
	private boolean accept(Site site) {
		if (site == null) {
			return false;
		}
		return accept(site.getNode());
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHubFamily()
	 */
	@Override
	public byte getHubFamily() throws VisitException {
		return SiteTag.HOME_SITE;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHub()
	 */
	@Override
	public Node getHub() throws VisitException {
		return HomeLauncher.getInstance().getListener();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#currentTime()
	 */
	@Override
	public long currentTime() throws VisitException {
		return SystemTime.get();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getSiteTimeout(byte)
	 */
	@Override
	public long getSiteTimeout(byte siteFamily) throws VisitException {
		// 默认超时是20秒，具体时间由各节点去定义。
		long second = 20000;
		if (SiteTag.isCall(siteFamily)) {
			second = CallOnHomePool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isData(siteFamily)) {
			second = DataOnHomePool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isWork(siteFamily)) {
			second = WorkOnHomePool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isBuild(siteFamily)) {
			second = BuildOnHomePool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isLog(siteFamily)) {
			second = LogOnHomePool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isHome(siteFamily)) {
			second = MonitorOnHomePool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isWatch(siteFamily)) {
			second = WatchOnHomePool.getInstance().getActiveTimeMillis();
		}
		// 考虑发送到接收反馈的时间延迟，数据包要提前发送，这里减去2秒。
		second -= 2000;
		if (second < 10000) second = 10000;
		return second;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#findLogSite(byte)
	 */
	@Override
	public Node findLogSite(byte siteFamily) throws VisitException {
		SiteHost host = LogOnHomePool.getInstance().selectLog(siteFamily);
		if (host == null) {
			return null;
		}
		return new Node(SiteTag.LOG_SITE, host);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#findTigSite(byte)
	 */
	@Override
	public Node findTigSite(byte siteFamily) throws VisitException {
		SiteHost host = LogOnHomePool.getInstance().selectTig(siteFamily);
		if (host == null) {
			return null;
		}
		return new Node(SiteTag.LOG_SITE, host);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#findBillSite(byte)
	 */
	@Override
	public Node findBillSite(byte siteFamily) throws VisitException {
		SiteHost host = LogOnHomePool.getInstance().selectBill(siteFamily);
		if (host == null) {
			return null;
		}
		return new Node(SiteTag.LOG_SITE, host);
	}

	/**
	 * 删除HOME节点保存的客户机密钥
	 * @param node 客户机节点地址
	 * @return 成功返回真，否则假
	 */
	private boolean removeCipher(Node node) {
		Cipher cipher = HomeLauncher.getInstance().dropCipher(node);
		boolean success = (cipher != null);
		Logger.debug(this, "removeCipher", success, "drop secure cipher %s # %s", node, cipher);
		return success;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#login(com.laxcus.site.Site)
	 */
	@Override
	public boolean login(Site site) throws VisitException {
		// 判断可以接受
		if (!accept(site)) {
			return false;
		}

		// 根据站点类型，注册到不同的管理池
		boolean success = false;
		if (site.isLog()) {
			success = LogOnHomePool.getInstance().add(site);
		} else if (site.isData()) {
			success = DataOnHomePool.getInstance().add(site);
		} else if (site.isCall()) {
			success = CallOnHomePool.getInstance().add(site);
		} else if (site.isWork()) {
			success = WorkOnHomePool.getInstance().add(site);
		} else if (site.isBuild()) {
			success = BuildOnHomePool.getInstance().add(site);
		} else if (site.isHome()) {
			success = MonitorOnHomePool.getInstance().add(site);
		} else if (site.isWatch()) {
			success = WatchOnHomePool.getInstance().add(site);
		}
		
		// 如果注册成功，删除客户端UDP通信密钥
		// 本处于SiteLauncher.login对应，它删除服务端密钥，即HUB节点的密钥
		if (success) {
			removeCipher(site.getNode());
		}

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#logout(com.laxcus.site.Node)
	 */
	@Override
	public boolean logout(Node node) throws VisitException {
		// 判断可以接受
		if (!accept(node)) {
			return false;
		}
		// 根据站点类型，从对应的管理池上注销
		boolean success = false;
		if (node.isLog()) {
			success = LogOnHomePool.getInstance().remove(node);
		} else if (node.isData()) {
			success = DataOnHomePool.getInstance().remove(node);
		} else if (node.isCall()) {
			success = CallOnHomePool.getInstance().remove(node);
		} else if (node.isWork()) {
			success = WorkOnHomePool.getInstance().remove(node);
		} else if (node.isBuild()) {
			success = BuildOnHomePool.getInstance().remove(node);
		} else if (node.isHome()) {
			success = MonitorOnHomePool.getInstance().remove(node);
		} else if (node.isWatch()) {
			success = WatchOnHomePool.getInstance().remove(node);
		}
		
		// 成功，当前是服务器，撤销客户端的密钥
		if (success) {
			removeCipher(node);
		}
		
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHubRegisterInterval()
	 */
	@Override
	public long getHubRegisterInterval() throws VisitException {
		return HomeLauncher.getInstance().getHubRegisterInterval();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHubMaxRegisterInterval()
	 */
	@Override
	public long getHubMaxRegisterInterval() throws VisitException {
		return HomeLauncher.getInstance().getHubMaxRegisterInterval();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getVersion()
	 */
	@Override
	public Version getVersion() throws VisitException {
		return HomeLauncher.getInstance().getVersion();
	}

	
}