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
import com.laxcus.util.datetime.*;
import com.laxcus.visit.*;
import com.laxcus.visit.hub.*;

/**
 * CALL登录访问管理接口，实现HubVisit接口。<br>
 * 为FRONT（普通注册用户）提供注册服务，在获得验证后，为FRONT提供数据处理服务。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/23/2009
 * @since laxcus 1.0
 */
public class HubVisitOnCall implements HubVisit {

	/**
	 * 构造CALL登录访问管理接口
	 */
	public HubVisitOnCall() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHubFamily()
	 */
	@Override
	public byte getHubFamily() throws VisitException {
		return SiteTag.CALL_SITE;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHub()
	 */
	@Override
	public Node getHub() throws VisitException {
		// CALL节点不支持
		return null;
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
		if (SiteTag.isFront(siteFamily)) {
			return FrontOnCallPool.getInstance().getActiveTimeMillis() - 2000;
		} 
		// 见CallOnFrontPool.login, 这是一个私有约定，如果SiteTag.FRONT_SITE * 3 等于siteFamily
		// 表示FRONT节点要获取断网后，自动重新注册到CALL节点的最大间隔时间。
		else if(SiteTag.FRONT_SITE * 3 == siteFamily) {
			long max = Math.max(Cipher.getTimeout(), 
					FrontOnCallPool.getInstance().getDeleteTimeMillis());
			return max;
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#findLogSite(byte)
	 */
	@Override
	public Node findLogSite(byte siteFamily) throws VisitException {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#findTigSite(byte)
	 */
	@Override
	public Node findTigSite(byte siteFamily) throws VisitException {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#findBillSite(byte)
	 */
	@Override
	public Node findBillSite(byte siteFamily) throws VisitException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 删除CALL节点保存的FRONT节点密钥
	 * @param node FRONT节点地址
	 * @return 返回真或者假
	 */
	private boolean removeCipher(Node node) {
		return CallLauncher.getInstance().removeCipher(node);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#login(com.laxcus.site.Site)
	 */
	@Override
	public boolean login(Site site) throws VisitException {
		// 必须是FRONT节点，否则拒绝
		if (site.getClass() != FrontSite.class) {
			Logger.error(this, "login", "refuse! from %s", site);
			return false;
		}

		// 判断签名有效
		Siger siger = ((FrontSite) site).getUsername();
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
		
		// 登录成功，删除可能存在的旧密钥
		if (success) {
			removeCipher(site.getNode());
		}

		Logger.debug(this, "login", success, "from %s", site);

		return success;
	}
	
	private void pushToWatch(Siger siger, Node front) {
		ShiftPushOnlineMember cmd = new ShiftPushOnlineMember(siger, front);
		CallCommandPool.getInstance().admit(cmd);
	}
	
	private void dropToWatch(Siger siger, Node front) {
		ShiftDropOnlineMember cmd = new ShiftDropOnlineMember(siger, front);
		CallCommandPool.getInstance().admit(cmd);
	}

	/* (non-Javadoc)
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
		
		// 成功，撤销密钥
		if (success) {
			removeCipher(node);
		}
		
		Logger.debug(this, "logout", success, "from %s", node);
		
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHubRegisterInterval()
	 */
	@Override
	public long getHubRegisterInterval() throws VisitException {
		return CallLauncher.getInstance().getHubRegisterInterval();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHubMaxRegisterInterval()
	 */
	@Override
	public long getHubMaxRegisterInterval() throws VisitException {
		return CallLauncher.getInstance().getHubMaxRegisterInterval();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getVersion()
	 */
	@Override
	public Version getVersion() throws VisitException {
		return CallLauncher.getInstance().getVersion();
	}

	

}