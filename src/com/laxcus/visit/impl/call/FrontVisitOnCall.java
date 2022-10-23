/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.call;

import com.laxcus.access.diagram.*;
import com.laxcus.call.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;
import com.laxcus.visit.hub.*;

/**
 * CALL远程登录访问接口。<br>
 * 实现FrontVisit接口，提供Call节点的ReplyDispatcher地址
 * 
 * @author scott.liang
 * @version 1.0 3/1/2019
 * @since laxcus 1.0
 */
public class FrontVisitOnCall implements FrontVisit {

	/**
	 * 构造CALL远程登录访问接口
	 */
	public FrontVisitOnCall() {
		super();
	}

	private void pushToWatch(Siger siger, Node front) {
		ShiftPushOnlineMember cmd = new ShiftPushOnlineMember(siger, front);
		CallCommandPool.getInstance().admit(cmd);
	}

	private void dropToWatch(Siger siger, Node front) {
		ShiftDropOnlineMember cmd = new ShiftDropOnlineMember(siger, front);
		CallCommandPool.getInstance().admit(cmd);
	}

	/**
	 * 删除CALL节点上保存的FRONT客户机密钥
	 * @param node FRONT节点地址
	 * @return 成功返回真，否则假
	 */
	private boolean removeCipher(Node node) {
		Cipher cipher = CallLauncher.getInstance().dropCipher(node);
		boolean success = (cipher != null);
		Logger.debug(this, "removeCipher", success, "drop secure cipher %s # %s", node, cipher);
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#release(com.laxcus.site.front.FrontSite)
	 */
	@Override
	public boolean release(FrontSite site) throws VisitException {
		// 存在FRONT节点故障又重新登录的情况，所以先删除可能存在的旧值，再进行登录
		boolean success = false;
		Node old = FrontOnCallPool.getInstance().remove(site.getHash());
		if (old != null) {
			success = removeCipher(old);
		}
		success = (old != null || success);
		
		Logger.debug(this, "release", success, "drop history %s", site.getNode());
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#login(com.laxcus.site.Site)
	 */
	@Override
	public FrontReport login(FrontSite site) throws VisitException {
		// 判断签名有效
		Siger siger = site.getUsername();
		boolean success = StaffOnCallPool.getInstance().allow(siger);

		// 找到账号，判断账号有效：1. 处于开放状态, 2. 没有到期
		if (success) {
			Refer refer = StaffOnCallPool.getInstance().findRefer(siger);
			success = (refer != null);
			if (success) {
				success = refer.getUser().isEnabled();
				Logger.debug(this, "login", success, "enabled is");
			}
		}

		// 保存到管理池
		if (success) {
			success = FrontOnCallPool.getInstance().add(site);
			// 投递给HOME节点，转交给WATCH节点
			pushToWatch(siger, site.getNode());
		}

		// 成功，删除FRONT密钥，数据清零，重新开始
		if (success) {
			removeCipher(site.getNode());
		}

		Logger.debug(this, "login", success, "from %s", site);

		return new FrontReport(success ? FrontStatus.LOGINED : FrontStatus.FAILED);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#logout(com.laxcus.site.Node)
	 */
	@Override
	public boolean logout(Node node) throws VisitException {
		boolean success = false;
		if (node.isFront()) {
			// 找到注册地址
			Site site = FrontOnCallPool.getInstance().find(node);

			// 删除记录
			success = FrontOnCallPool.getInstance().remove(node);

			// 通知HOME.WATCH / TOP.WATCH节点
			if (site != null && success) {
				FrontSite front = (FrontSite) site;
				dropToWatch(front.getUsername(), node);
			}
		}

		// 成功，删除密钥
		if (success) {
			removeCipher(node);
		}

		Logger.debug(this, "logout", success, "from %s", node);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#release(com.laxcus.site.front.ConferrerSite)
	 */
	@Override
	public boolean release(ConferrerSite site) throws VisitException {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#login(com.laxcus.site.front.ConferrerSite)
	 */
	@Override
	public FrontReport login(ConferrerSite site) throws VisitException {
		return new FrontReport(FrontStatus.FAILED);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#logout(com.laxcus.site.Node, com.laxcus.util.Siger)
	 */
	@Override
	public boolean logout(Node node, Siger authorizer) throws VisitException {
		return false; // 忽略
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHubFamily()
	 */
	@Override
	public byte getHubFamily() throws VisitException {
		return SiteTag.CALL_SITE;
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.visit.hub.FrontVisit#getHubSucker()
	//	 */
	//	@Override
	//	public SocketHost getHubSucker() throws VisitException {
	//		// 公网上的ReplySucker主机地址
	//		return CallLauncher.getInstance().getReplyHelper()
	//				.getDefinePublicHost();
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.visit.hub.FrontVisit#getHubDispatcher()
	//	 */
	//	@Override
	//	public SocketHost getHubDispatcher() throws VisitException {
	//		// 公网上的ReplyDispatcher主机地址
	//		return CallLauncher.getInstance().getReplyWorker()
	//				.getDefinePublicHost();
	//	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getHubSucker(boolean)
	 */
	@Override
	public SocketHost getHubSucker(boolean wide) throws VisitException {
		// 判断Front来自公网还是内网，返回对应的ReplySucker主机地址
		SocketHost host = (wide ? CallLauncher.getInstance().getReplyHelper().getDefinePublicHost() :
			CallLauncher.getInstance().getReplyHelper().getDefinePrivateHost());

		// 如果Front来自公网，且有映射地址时...
		if (wide && host.hasReflectPort()) {
			return new SocketHost(host.getFamily(), host.getAddress(), host.getReflectPort());
		}
		return host;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getHubDispatcher(boolean)
	 */
	@Override
	public SocketHost getHubDispatcher(boolean wide) throws VisitException {
		// 判断Front来自公网还是内网，返回对应的ReplyDispatcher主机地址
		SocketHost host = (wide ? CallLauncher.getInstance().getReplyWorker().getDefinePublicHost() 
				: CallLauncher.getInstance().getReplyWorker().getDefinePrivateHost());

		Logger.debug(this, "getHubDispatcher", "from wide %s!, dispatcher host %s", (wide ? "Yes" : "No"), host);

		// 如果Front来自公网，且有映射地址时，返回映射地址
		if (wide && host.hasReflectPort()) {
			return new SocketHost(host.getFamily(), host.getAddress(), host.getReflectPort());
		}
		return host;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getTimeout()
	 */
	@Override
	public long getTimeout() throws VisitException {
		long ms = FrontOnCallPool.getInstance().getActiveTimeMillis();

		// 提前2秒
		ms -= 2000;
		if (ms < 10000) {
			ms = 10000;
		}

		Logger.debug(this, "getTimeout", "front timeout %d", ms);
		return ms;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getLingerTimeout()
	 */
	@Override
	public long getLingerTimeout() throws VisitException {
		// 默认2秒
		return 2000;
	}

	/**
	 * CALL节点忽略这个参数
	 * 
	 * @see com.laxcus.visit.hub.FrontVisit#getAutoReloginInterval()
	 */
	@Override
	public long getAutoReloginInterval() throws VisitException {
		long max = Math.max(Cipher.getTimeout(), 
				FrontOnCallPool.getInstance().getDeleteTimeMillis());

		return max;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getVersion()
	 */
	@Override
	public Version getVersion() throws VisitException {
		return CallLauncher.getInstance().getVersion();
	}
}
