/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.entrance;

import com.laxcus.entrance.*;
import com.laxcus.entrance.pool.*;
import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;
import com.laxcus.visit.hub.*;

/**
 * ENTRANCE远程登录访问接口。<br>
 * 实现FrontVisit接口，将FRONT站点重定向到GATE站点。
 * 
 * @author scott.liang
 * @version 1.0 7/17/2018
 * @since laxcus 1.0
 */
public class FrontVisitOnEntrance implements FrontVisit {

	/**
	 * 构造ENTRANCE远程登录访问接口
	 */
	public FrontVisitOnEntrance() {
		super();
	}
	
	/**
	 * 删除服务器密钥
	 * @param node 节点
	 * @return 成功返回真，否则假
	 */
	private boolean removeCipher(Node node) {
		Cipher cipher = EntranceLauncher.getInstance().dropCipher(node);
		boolean success = (cipher != null);
		Logger.debug(this, "removeCipher", success, "drop secure cipher %s # %s", node, cipher);
		return success;
					
		//		boolean success = EntranceLauncher.getInstance().removeCipher(node);
		//		Logger.debug(this, "removeCipher", success, "drop secure cipher %s", node);
		//		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#release(com.laxcus.site.front.FrontSite)
	 */
	@Override
	public boolean release(FrontSite site) throws VisitException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#login(com.laxcus.site.front.FrontSite)
	 */
	@Override
	public FrontReport login(FrontSite site) throws VisitException {
		Logger.debug(this, "login", "登录来自: %s", site.getNode());
		
		// 取FRONT注册人签名
		Siger siger = site.getUsername();
		// 判断是来自公网
		Node node = site.getNode();
		boolean fromWide = node.getAddress().isWideAddress();
		
		Logger.debug(this, "login", "%s from %s", siger, node);
		
		// 1. 采用HASH定位
		boolean hash = EntranceLauncher.getInstance().isHash();
		if (hash) {
			// 定位到GATE站点，区分“公网/内网”
			Node gate = StaffOnEntrancePool.getInstance().locate(siger, fromWide);
			// 重定向到GATE站点
			if (gate != null) {
				// 如果来自公网，并且GATE节点有映射主机时
				SiteHost host = gate.getHost();
				boolean has = (fromWide && host.hasReflectTCPort() && host.hasReflectUDPort());
				if (has) {
					SiteHost reflect = new SiteHost(host.getAddress(), host.getReflectTCPort(), host.getReflectUDPort());
					gate.setHost(reflect);
				}
				
				Logger.debug(this, "login", "gate site is %s, has reflect port %s", gate, (has ? "Yes" : "No"));
				
				// 返回主机地址
				return new FrontReport(gate);
			}
			// 登录失败
			return new FrontReport(FrontStatus.FAILED);
		}
		
		// 2. 判断管理池中已经存在
		boolean success = FrontOnEntrancePool.getInstance().contains(siger);
		if (success) {
			// 判断已经获得
			success = FrontOnEntrancePool.getInstance().isTouched(siger);
			if (success) {
				Node gate = FrontOnEntrancePool.getInstance().popup(siger);
				// 重定向到GATE站点
				if (gate != null) {
					return new FrontReport(gate);
				}
				// 登录失败
				return new FrontReport(FrontStatus.FAILED);
			} else {
				// 继续等待
				return new FrontReport(FrontStatus.LINGER);
			}
		}
		
		// 3. 如果没有，保存它
		success = FrontOnEntrancePool.getInstance().push(siger, fromWide);
		return new FrontReport(success ? FrontStatus.LINGER : FrontStatus.FAILED);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#logout(com.laxcus.site.Node)
	 */
	@Override
	public boolean logout(Node node) throws VisitException {
//		Logger.debug(this, "logout", "注销来自: %s", node);
		
		// 删除密钥
		boolean success = removeCipher(node);
		
		Logger.debug(this, "logout", success, "delete %s", node);
		
		return success; // 这个方法忽略
	}
	

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#release(com.laxcus.site.front.ConferrerSite)
	 */
	@Override
	public boolean release(ConferrerSite site) throws VisitException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#login(com.laxcus.site.front.ConferrerSite)
	 */
	@Override
	public FrontReport login(ConferrerSite site) throws VisitException {
		// 无效！
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
		return SiteTag.ENTRANCE_SITE;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getHubSucker(boolean)
	 */
	@Override
	public SocketHost getHubSucker(boolean wide) throws VisitException {
		// 判断Front来自公网还是内网，返回对应的ReplySucker主机地址
		SocketHost host = (wide ? EntranceLauncher.getInstance().getReplyHelper().getDefinePublicHost() :
			EntranceLauncher.getInstance().getReplyHelper().getDefinePrivateHost());
		
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
		SocketHost host = (wide ? EntranceLauncher.getInstance().getReplyWorker().getDefinePublicHost() 
				: EntranceLauncher.getInstance().getReplyWorker().getDefinePrivateHost());
		
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
		return 20000;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getLingerTimeout()
	 */
	@Override
	public long getLingerTimeout() throws VisitException {
		// 默认2秒
		return EntranceLauncher.getInstance().getLingerTimeout();
	}

	/**
	 * ENTRANCE节点以密钥超时时间为准
	 * 
	 * @see com.laxcus.visit.hub.FrontVisit#getAutoReloginInterval()
	 */
	@Override
	public long getAutoReloginInterval() throws VisitException {
		return Cipher.getTimeout();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getVersion()
	 */
	@Override
	public Version getVersion() throws VisitException {
		return EntranceLauncher.getInstance().getVersion();
	}

}