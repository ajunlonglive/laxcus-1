/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.top;

import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;
import com.laxcus.visit.hub.*;

/**
 * TOP登录访问管理接口，实现HubVisit接口。为AID/FRONT(只限管理员)/HOME站点提供注册服务。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 2/2/2009
 * @since laxcus 1.0
 */
public class HubVisitOnTop implements HubVisit {

	/**
	 * 构造TOP登录访问管理接口
	 */
	public HubVisitOnTop() {
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
		boolean success = TopLauncher.getInstance().isMonitor();
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
		return SiteTag.TOP_SITE;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHub()
	 */
	@Override
	public Node getHub() throws VisitException {
		return TopLauncher.getInstance().getListener();
	}
	
	/**
	 * 删除TOP节点保存的客户机密钥
	 * @param node 客户机主机地址
	 * @return 成功返回真，失败假
	 */
	private boolean removeCipher(Node node) {
		Cipher cipher = TopLauncher.getInstance().dropCipher(node);
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
		// 不是管理站点拒绝接受
		if (!accept(site)) {
			Logger.error(this, "login", "cannot be accpet %s", site);
			return false;
		}
		// 子站注册：MONITOR TOP/HOME/BANK/WATCH/LOG
		boolean success = false;
		if (site.isTop()) {
			success = MonitorOnTopPool.getInstance().add(site);
		} else if (site.isHome()) {
			success = HomeOnTopPool.getInstance().add(site);
		} else if (site.isBank()) {
			success = BankOnTopPool.getInstance().add(site);
		} else if (site.isWatch()) {
			success = WatchOnTopPool.getInstance().add(site);
		} else if (site.isLog()) {
			success = LogOnTopPool.getInstance().add(site);
		}
		
		// 如果注册成功，删除客户端UDP通信密钥
		// 本处于SiteLauncher.login对应，它删除服务端密钥，即HUB节点的密钥
		if (success) {
			removeCipher(site.getNode());
		}
		
		Logger.note(this, "login", success, "site is %s", site);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#logout(com.laxcus.site.Node)
	 */
	@Override
	public boolean logout(Node node) throws VisitException {
		if (!accept(node)) {
			Logger.error(this, "logout", "cannot be accpet %s", node);
			return false;
		}
		
		// 	子站注销：HOME / MONITOR TOP / WATCH / BANK / LOG
		boolean success = false;
		if (node.isHome()) {
			success = HomeOnTopPool.getInstance().remove(node);
		} else if (node.isTop()) {
			success = MonitorOnTopPool.getInstance().remove(node);
		} else if (node.isWatch()) {
			success = WatchOnTopPool.getInstance().remove(node);
		} else if (node.isBank()) {
			success = BankOnTopPool.getInstance().refresh(node);
		} else if (node.isLog()) {
			success = LogOnTopPool.getInstance().remove(node);
		}
		
		// 成功，TOP服务器撤销客户端节点密钥
		if (success) {
			removeCipher(node);
		}
		
		Logger.note(this, "logout", success, "site is %s", node);

		return success;
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
		long timeout = 20000; // 毫秒
		if (SiteTag.isHome(siteFamily)) {
			timeout = HomeOnTopPool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isBank(siteFamily)) {
			timeout = BankOnTopPool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isLog(siteFamily)) {
			timeout = LogOnTopPool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isTop(siteFamily)) {
			timeout = MonitorOnTopPool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isWatch(siteFamily)) {
			timeout = WatchOnTopPool.getInstance().getActiveTimeMillis();
		}

		// 考虑到大规模网络的通信延迟现象，数据包要求提前发送，所以减去2秒
		timeout -= 2000;
		if (timeout < 10000) timeout = 10000;

		Logger.debug(this, "getSiteTimeout", "%s timeout %d", SiteTag.translate(siteFamily), timeout);
		return timeout;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#findLogSite(byte)
	 */
	@Override
	public Node findLogSite(byte siteFamily) throws VisitException {
		// 根据站点的类型返回匹配的日志包发送地址
		SiteHost host = LogOnTopPool.getInstance().selectLog(siteFamily);
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
		// 根据站点的类型返回匹配的日志包发送地址
		SiteHost host = LogOnTopPool.getInstance().selectTig(siteFamily);
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
		// 根据站点的类型返回匹配的日志包发送地址
		SiteHost host = LogOnTopPool.getInstance().selectBill(siteFamily);
		if (host == null) {
			return null;
		}
		return new Node(SiteTag.LOG_SITE, host);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHubRegisterInterval()
	 */
	@Override
	public long getHubRegisterInterval() throws VisitException {
		return TopLauncher.getInstance().getHubRegisterInterval();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHubMaxRegisterInterval()
	 */
	@Override
	public long getHubMaxRegisterInterval() throws VisitException {
		return TopLauncher.getInstance().getHubMaxRegisterInterval();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getVersion()
	 */
	@Override
	public Version getVersion() throws VisitException {
		return TopLauncher.getInstance().getVersion();
	}

	

}