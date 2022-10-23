/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.gate;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.site.gate.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.fixp.*;
import com.laxcus.gate.*;
import com.laxcus.gate.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;
import com.laxcus.visit.hub.*;

/**
 * FRONT访问GATE站点登录接口。<br>
 * 
 * 实现FrontVisit接口，为所有FRONT用户（包括系统管理员或者普通注册用户）提供注册和注销服务。<br>
 * 注册是所有服务的基础，在此之后，才能提供分布资源检索和事务的管理服务。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/17/2018
 * @since laxcus 1.0
 */
public class FrontVisitOnGate implements FrontVisit {

	/**
	 * 构造默认的GATE站点登录接口
	 */
	public FrontVisitOnGate() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getHubFamily()
	 */
	@Override
	public byte getHubFamily() throws VisitException {
		return SiteTag.GATE_SITE;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getHubSucker(boolean)
	 */
	@Override
	public SocketHost getHubSucker(boolean wide) throws VisitException {
		// 判断Front来自公网还是内网，返回对应的ReplySucker主机地址
		SocketHost host = (wide ? GateLauncher.getInstance().getReplyHelper().getDefinePublicHost() :
			GateLauncher.getInstance().getReplyHelper().getDefinePrivateHost());
		
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
		SocketHost host = (wide ? GateLauncher.getInstance().getReplyWorker().getDefinePublicHost() 
				: GateLauncher.getInstance().getReplyWorker().getDefinePrivateHost());
		
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
		// 获取GATE站点定义的FRONT站点激活时间
		long ms = FrontOnGatePool.getInstance().getActiveTimeMillis();

		// 提前2秒，最少10秒钟
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
		return GateLauncher.getInstance().getLingerTimeout();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getAutoReloginInterval()
	 */
	@Override
	public long getAutoReloginInterval() throws VisitException {
		// 比较找出最大的延时时间
		long max = Math.max(BlackOnGatePool.getInstance().getTimeout(),
				FaultOnGatePool.getInstance().getTimeout());
		max = Math.max(max, FrontOnGatePool.getInstance().getDeleteTimeMillis());
		max = Math.max(max, Cipher.getTimeout());
		return max;
	}

	/**
	 * 删除密钥
	 * @param node FRONT节点地址
	 * @return 成功返回真，否则假
	 */
	private boolean removeCipher(Node node) {
		Cipher cipher = GateLauncher.getInstance().dropCipher(node);
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
		// FRONT节点可能存在故障后又马上登录的情况。这个时候要删除它，再进行后续登录
		// FrontHash根据MAC地址和类名生成，是每个FRONT节点的唯一值。
		boolean success = false;
		ClassCode hash = site.getHash();
		Node old = FrontOnGatePool.getInstance().remove(hash);
		if (old != null) {
			success = removeCipher(old);
		}
		success = (old != null || success);
		
		Logger.debug(this, "release", success, "drop history %s", site.getNode());
		return success;
	}
	
	/**
	 * FRONT节点登录到GATE节点。<br><br>
	 * 
	 * Front登录Gate流程：<br>
	 * 1. Front.FixpPacketMonitor信道与Gate.FixpPacketMonitor信道建立联系，包括 <br>
	 * 		<1> Front对Gate加密判断和交换密钥 <br>
	 * 		<2> Gate.FixpPacketMonitor向Front.FixpPacketMonitor返回它的网关出口地址（reflect命令）<br>
	 * 2. Front使用FixpPacketClient注册到Gate节点<br><br>
	 * 
	 * 注意，与其它客户端登录HubVisitOnTop/HubVisitOnHome/HubVisitOnBank，删除密钥不同，
	 * Front在登录Gate前，已经建立与Gate信道联系（见第一项）。所以，Gate判断Front登录成功后，不要删除本地的Front信道密钥，只在注销(logout方法)时删除！<br><br>
	 * 
	 * @see com.laxcus.visit.hub.FrontVisit#login(com.laxcus.site.front.FrontSite)
	 */
	@Override
	public FrontReport login(FrontSite site) throws VisitException {
		User user = site.getUser();
		Siger authorizer = user.getUsername();
		
		Logger.debug(this, "login", "登录来自: %s", site.getNode());
		
		// 1. 判断在黑名单
		
		// 判断在黑名单中
		if (BlackOnGatePool.getInstance().contains(user)) {
			return new FrontReport(FrontStatus.MAX_RETRY);
		}
		// 不属于黑名单，但是登录时失败的账号。如果有，删除账号，通知服务器资源紧张，稍后再试！
		if (FaultOnGatePool.getInstance().contains(user)) {
			FaultOnGatePool.getInstance().remove(user);
			return new FrontReport(FrontStatus.SERVICE_MISSING);
		}

		// 判断管理员直接注册，否则用异步调用器完成注册。
		boolean success = StaffOnGatePool.getInstance().isAdminstrator(user);
		if (success) {
			// 检查同时登录的节点数目
			int members = FrontOnGatePool.getInstance().findMembers(authorizer);
			Administrator admin = StaffOnGatePool.getInstance().getAdministrator();
			if (members >= admin.getMembers()) {
				Logger.warning(this, "login", "online members: %d >= max members: %d", 
						members, admin.getMembers());
				return new FrontReport(FrontStatus.MAXUSER);
			}
			
			// 保存到内存中
			success = FrontOnGatePool.getInstance().add(site);
			if (success) {
				pushToWatch(authorizer, site.getNode()); // 通知WATCH节点
			}
			Logger.debug(this, "login", success, "this is a administrator!");
			return new FrontReport(success ? FrontStatus.LOGINED : FrontStatus.FAILED);
		}

		// 1. 检查账号存在于驻留管理池
		Node node = site.getNode();
		success = StayFrontOnGatePool.getInstance().contains(node);
		if (success) {
			return new FrontReport(FrontStatus.LINGER); // 处于驻留状态
		}

		// 2. 判断账号在管理池中。如果存在，即是已经登录成功！
		success = FrontOnGatePool.getInstance().contains(node);
		if (success) {
			pushToWatch(authorizer, site.getNode()); // 通知FRONT节点			
			return new FrontReport(FrontStatus.LOGINED);
		}
		
		// 3. 判断达到最大用户数目，登录失败!
		if (FrontOnGatePool.getInstance().isMaxout(authorizer)) {
			Logger.warning(this, "login", "%s is maxout!", user);
			return new FrontReport(FrontStatus.MAXUSER);
		}

		// 4. 以上不成立，注册到驻留管理池
		FrontLogin cmd = new FrontLogin(site);
		success = GateCommandPool.getInstance().admit(cmd);
		if (success) {
			success = StayFrontOnGatePool.getInstance().add(site);
		}
		// 返回结果
		return new FrontReport(success ? FrontStatus.LINGER : FrontStatus.FAILED);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#logout(com.laxcus.site.Node)
	 */
	@Override
	public boolean logout(Node node) throws VisitException {
		Logger.debug(this, "logout", "注销来自: %s", node);
		
		// 找到注册账号
		FrontSite site = (FrontSite) FrontOnGatePool.getInstance().find(node);
		// 没有找到FRONT站点记录，拒绝注销
		if (site == null) {
			Logger.error(this, "logout", "cannot be find %s", node);
			return false;
		}

		// 签名人自己
		Siger authorizer = site.getUsername();
		// 删除这个地址
		boolean success = FrontOnGatePool.getInstance().remove(node);
		
		// 检查被授权记录
		if (success) {
			// 三种可能：1.账号持有人有多个连接，2.被授权账号还有账号持有人资源，3. 事务规则管理池还有账号持有人
			boolean exists = (FrontOnGatePool.getInstance().contains(authorizer) || 
					ConferrerFrontOnGatePool.getInstance().hasAuthorizer(authorizer) || 
					RuleHouse.getInstance().contains(authorizer));
			// 如果被授权人账号和事务管理池没有授权人，删除记录
			if (!exists) {
				StaffOnGatePool.getInstance().drop(authorizer);
				CallOnGatePool.getInstance().remove(authorizer);
				// 删除事务
				RuleHouse.getInstance().remove(authorizer, node);
			}
			
			// 通知WATCH注销
			dropToWatch(authorizer, node);
		}
		
		// 成功，清除密钥
		if (success) {
			removeCipher(node);
		}

		Logger.debug(this, "logout", success, "%s, authorizer:%s", node, authorizer);

		return success;
	}
	
	/**
	 * 通过BANK节点，通知WATCH节点用户登录
	 * @param siger 用户签名
	 */
	private void pushToWatch(Siger siger, Node front) {
		ShiftPushOnlineMember shift = new ShiftPushOnlineMember(siger, front);
		GateCommandPool.getInstance().admit(shift);
	}

	/**
	 * 通过BANK节点，通知WATCH节点注销
	 * @param siger 用户签名
	 */
	private void dropToWatch(Siger siger, Node front) {
		ShiftDropOnlineMember shift = new ShiftDropOnlineMember(siger, front);
		GateCommandPool.getInstance().admit(shift);
	}
	
	/**
	 * 被授权人与授权人可能在一台Gate主机上。
	 * 注册/注销都是以被授权人的身份做判断，而不是授权人，因为加密信道来自被授权人。
	 * 删除的前提是，必须是被授权人账号没有登录这台Gate主机。
	 * 
	 * @param node FRONT节点地址
	 * @param conferrer 被授权人签名
	 * @return 删除成功返回真，否则假
	 */
	private boolean removeConferrerCipher(Node node, Siger conferrer) {
		Cipher cipher = null;
		boolean exists = FrontOnGatePool.getInstance().contains(conferrer);
		// 被授权人自己的账号不在这台主机上，可以删除！
		if (!exists) {
			cipher = GateLauncher.getInstance().dropCipher(node);
		}
		boolean success = (cipher != null);
		Logger.debug(this, "removeConferrerCipher", success, "drop secure cipher %s # %s", node, cipher);
		return success;
	}
	
	/**
	 * 存在FRONT节点故障又重新登录的情况，所以先删除可能存在的旧值，再进行登录
	 * 
	 * @see com.laxcus.visit.hub.FrontVisit#release(com.laxcus.site.front.ConferrerSite)
	 */
	@Override
	public boolean release(ConferrerSite site) throws VisitException {
		User user = site.getConferrer();
		boolean success = false;
		// FrontHash是每个FRONT节点的唯一值！
		Node old = ConferrerFrontOnGatePool.getInstance().remove(site.getHash());
		if (old != null) {
			success = removeConferrerCipher(old, user.getUsername());
		}
		
		success = (old != null || success);
		Logger.debug(this, "release", success, "drop conferrer history %s", site.getNode());
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#login(com.laxcus.site.front.ConferrerSite)
	 */
	@Override
	public FrontReport login(ConferrerSite site) throws VisitException {
		// 被授权人账号
		User user = site.getConferrer();
		// 取出授权人签名
		List<Siger> authorizers = site.getAuthorizers();

		// 判断两处：1. 有且只有一个签名，2.在黑名单中
		if (authorizers.size() != 1 || BlackOnGatePool.getInstance().contains(user)) {
			return new FrontReport(FrontStatus.FAILED);
		}
		
		// 判断是管理员
		boolean success = StaffOnGatePool.getInstance().isAdminstrator(user);
		if (success) {
			success = FrontOnGatePool.getInstance().add(site);
			Logger.error(this, "login", "this is a administrator, system refuse!");
			return new FrontReport(FrontStatus.FAILED);
		}

		// 1. 检查账号存在于驻留管理池
		Node node = site.getNode();
		success = StayConferrerFrontOnGatePool.getInstance().contains(node);
		if (success) {
			return new FrontReport(FrontStatus.LINGER); // 处于驻留状态
		}

		// 2. 如果注册地址、被授权人签名、授权人签名，都在被授权管理池，即是已经登录成功！
		Siger authorizer = authorizers.get(0);
		success = ConferrerFrontOnGatePool.getInstance().contains(node)
				&& ConferrerFrontOnGatePool.getInstance().hasAuthorizer(authorizer)
				&& ConferrerFrontOnGatePool.getInstance().hasConferrer(user.getUsername());
		if (success) {
			
			// 删除密钥，参数清零
			removeConferrerCipher(node, user.getUsername());
			
			return new FrontReport(FrontStatus.LOGINED);
		}

		// 以上不成立，注册到驻留管理池
		ConferrerLogin cmd = new ConferrerLogin(site);
		success = GateCommandPool.getInstance().admit(cmd);
		if (success) {
			success = StayConferrerFrontOnGatePool.getInstance().add(site);
		}
		// 返回结果
		return new FrontReport(success ? FrontStatus.LINGER : FrontStatus.FAILED);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#logout(com.laxcus.site.Node, com.laxcus.util.Siger)
	 */
	@Override
	public boolean logout(Node node, Siger authorizer) throws VisitException {
		ConferrerSite site = (ConferrerSite) ConferrerFrontOnGatePool.getInstance().find(node);
		// 没有找到FRONT站点记录，或者没有授权人，拒绝注销
		boolean success = (site != null && site.hasAuthorizer(authorizer));
		if (!success) {
			Logger.error(this, "logout", "cannot find %s", node);
			return false;
		}
		
		// 被授权人自己
		Siger conferrer = site.getConferrerUsername();

		// 删除被授权人节点地址和资源引用
		success = ConferrerFrontOnGatePool.getInstance().remove(node, authorizer);
		
		// 操作成功，检查被授权池
		if (success) {
			// 资源池没有与其它授权人关联，删除资源引用
			if (!ConferrerFrontOnGatePool.getInstance().hasConferrer(conferrer)) {
				ConferrerStaffOnGatePool.getInstance().drop(conferrer);
			}
		}

		// 判断授权人账号自己也注册中...
		if (success) {
			boolean exists = (ConferrerFrontOnGatePool.getInstance().hasAuthorizer(authorizer)
					|| FrontOnGatePool.getInstance().contains(authorizer) 
					|| RuleHouse.getInstance().contains(authorizer));

			// 如果没有，删除授权人账号
			if (!exists) {
				StaffOnGatePool.getInstance().drop(authorizer);
				CallOnGatePool.getInstance().remove(authorizer);
				// 删除事务，包括授权人和它的来源地址
				RuleHouse.getInstance().remove(authorizer, node);
			}
		}
		
		// 以上成功，删除被授权人自己的密钥
		if (success) {
			removeConferrerCipher(node, conferrer);
		}

		Logger.debug(this, "logout", success, "%s conferrer:%s | authorizer:%s", 
				node, conferrer, authorizer);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getVersion()
	 */
	@Override
	public Version getVersion() throws VisitException {
		return GateLauncher.getInstance().getVersion();
	}


//	/**
//	 * 被授权人在登录时，删除密钥。注意，这个密钥可能存在！
//	 * 
//	 * 条件： 必须保证被授权人自己的账号不在这个主机上，才可以删除
//	 * 
//	 * @param node FRONT节点地址
//	 * @param conferrer 被授权人签名
//	 * @return 删除成功返回真，否则假
//	 */
//	private boolean removeCipherOnLogin(Node node, Siger conferrer) {
//		boolean exists = FrontOnGatePool.getInstance().contains(conferrer);
//		// 被授权人自己的账号不在这台主机上，可以删除！
//		if (!exists) {
////			return GateLauncher.getInstance().removeCipher(node);
//		}
//		return false;
//	}
	

//	/**
//	 * 被授权人在注销时，删除密钥。这个密钥是可能存在！
//	 * 
//	 * @param node FRONT节点地址
//	 * @param conferrer 被授权人签名
//	 * @return 删除成功返回真，否则假
//	 */
//	private boolean removeCipherOnLogout(Node node, Siger conferrer) {
//		boolean exists = FrontOnGatePool.getInstance().contains(conferrer);
//		if(!exists) {
////			return GateLauncher.getInstance().removeCipher(node);
//		}
//		return false;
//	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.visit.hub.FrontVisit#getHubSucker()
//	 */
//	@Override
//	public SocketHost getHubSucker() throws VisitException {
//		// 公网上的ReplySucker主机地址
//		return GateLauncher.getInstance().getReplyHelper()
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
//		return GateLauncher.getInstance().getReplyWorker()
//				.getDefinePublicHost();
//	}

}