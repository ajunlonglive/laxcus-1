/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.bank;

import com.laxcus.bank.*;
import com.laxcus.bank.pool.*;
import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;
import com.laxcus.visit.hub.*;

/**
 * BANK登录访问管理接口，HubVisit接口的BANK站点实现。<br><br>
 * 
 * 提供针对BANK站点的HubVisit远程访问。
 * 
 * @author scott.liang
 * @version 1.0 6/25/2018
 * @since laxcus 1.0
 */
public class HubVisitOnBank implements HubVisit {

	/**
	 * 构造BANK登录访问管理接口
	 */
	public HubVisitOnBank() {
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
		// 当前BANK站点是监视站点，拒绝任何注册和注销
		boolean success = BankLauncher.getInstance().isMonitor();
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
		return SiteTag.BANK_SITE;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHub()
	 */
	@Override
	public Node getHub() throws VisitException {
		return BankLauncher.getInstance().getListener();
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
		long timeout = 20000;
		// 工作节点
		if (SiteTag.isAccount(siteFamily)) {
			timeout = AccountOnBankPool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isHash(siteFamily)) {
			timeout = HashOnBankPool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isGate(siteFamily)) {
			timeout = GateOnBankPool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isEntrance(siteFamily)) {
			timeout = EntranceOnBankPool.getInstance().getActiveTimeMillis();
		} 
		// 辅助节点
		else if (SiteTag.isLog(siteFamily)) {
			timeout = LogOnBankPool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isBank(siteFamily)) {
			timeout = MonitorOnBankPool.getInstance().getActiveTimeMillis();
		} else if (SiteTag.isWatch(siteFamily)) {
			timeout = WatchOnBankPool.getInstance().getActiveTimeMillis();
		}
		// 考虑发送到接收反馈的时间延迟，数据包要提前发送，这里减去2秒。
		timeout -= 2000;
		if (timeout < 10000) timeout = 10000;
		return timeout;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#findLogSite(byte)
	 */
	@Override
	public Node findLogSite(byte siteFamily) throws VisitException {
		SiteHost host = LogOnBankPool.getInstance().selectLog(siteFamily);
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
		SiteHost host = LogOnBankPool.getInstance().selectTig(siteFamily);
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
		SiteHost host = LogOnBankPool.getInstance().selectBill(siteFamily);
		if (host == null) {
			return null;
		}
		return new Node(SiteTag.LOG_SITE, host);
	}

	/**
	 * 删除BANK服务器节点保存的客户机密钥
	 * @param node 节点
	 * @return 成功返回真，否则假
	 */
	private boolean removeCipher(Node node) {
		Cipher cipher = BankLauncher.getInstance().dropCipher(node);
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
		// 辅助节点
		if (site.isLog()) {
			success = LogOnBankPool.getInstance().add(site);
		} else if (site.isBank()) {
			success = MonitorOnBankPool.getInstance().add(site);
		} else if (site.isWatch()) {
			success = WatchOnBankPool.getInstance().add(site);
		}
		// 工作节点
		else if (site.isHash()) {
			success = HashOnBankPool.getInstance().add(site);
		} else if (site.isAccount()) {
			success = AccountOnBankPool.getInstance().add(site);
		} else if (site.isGate()) {
			success = GateOnBankPool.getInstance().add(site);
		} else if (site.isEntrance()) {
			success = EntranceOnBankPool.getInstance().add(site);
		}
		
		// 如果注册成功，删除客户端UDP通信密钥
		// 本处于SiteLauncher.login对应，它删除服务端密钥，即HUB节点的密钥
		if (success) {
			removeCipher(site.getNode());
		}

		// 返回处理结果
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#logout(com.laxcus.site.Node)
	 */
	@Override
	public boolean logout(Node node) throws VisitException {
		// 判断可以接受
		if (!this.accept(node)) {
			return false;
		}
		// 根据站点类型，从对应的管理池上注销
		boolean success = false;
		// 辅助节点
		if (node.isLog()) {
			success = LogOnBankPool.getInstance().remove(node);
		}  else if (node.isBank()) {
			success = MonitorOnBankPool.getInstance().remove(node);
		} else if (node.isWatch()) {
			success = WatchOnBankPool.getInstance().remove(node);
		}
		// 工作节点
		else if (node.isHash()) {
			success = HashOnBankPool.getInstance().remove(node);
		} else if (node.isAccount()) {
			success = AccountOnBankPool.getInstance().remove(node);
		} else if (node.isGate()) {
			success = GateOnBankPool.getInstance().remove(node);
		} else if (node.isEntrance()) {
			success = EntranceOnBankPool.getInstance().remove(node);
		}
		
		// BANK服务器撤销客户端密钥
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
		// TODO Auto-generated method stub
		return BankLauncher.getInstance().getHubRegisterInterval();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHubMaxRegisterInterval()
	 */
	@Override
	public long getHubMaxRegisterInterval() throws VisitException {
		return BankLauncher.getInstance().getHubMaxRegisterInterval();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getVersion()
	 */
	@Override
	public Version getVersion() throws VisitException {
		return BankLauncher.getInstance().getVersion();
	}

	

}