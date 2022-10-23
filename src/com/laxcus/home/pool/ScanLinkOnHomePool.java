/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.pool;

import java.util.*;

import com.laxcus.command.field.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.home.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.call.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * HOME在线用户检查管理池。<br><br>
 * 
 * 检查HOME节点上的注册用户和AID站点之间的关联。如果有注册用户没有在AID站点存在，通知WATCH节点。
 * 
 * @author scott.liang
 * @version 1.0 6/7/2018
 * @since laxcus 1.0
 */
public class ScanLinkOnHomePool extends VirtualPool {

	/** HOME在线用户检查管理池句柄 **/
	private static ScanLinkOnHomePool selfHandle = new ScanLinkOnHomePool();

	/**
	 * 构造HOME在线用户检查管理池
	 */
	private ScanLinkOnHomePool() {
		super();
		// 20分钟检查一次
		setSleepTime(20 * 60 * 1000);
	}

	/**
	 * 返回HOME在线用户检查管理池句柄
	 * @return ScanLinkOnHomePool实例
	 */
	public static ScanLinkOnHomePool getInstance() {
		return ScanLinkOnHomePool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.debug(this, "process", "into...");

		while (!isInterrupted()) {
			sleep();
			// 必须是“管理者”角色，才能够启动检查工作
			if (HomeLauncher.getInstance().isManager()) {
				check();
			}
		}

		Logger.debug(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	/**
	 * 检查命令
	 */
	private void check() {
		checkLose();
		checkMissing();
		checkPush();
	}

	/**
	 * 检查没有在CALL站点注册的用户签名。
	 * 如果存在，从已有的CALL站点中分配一部分给这些用户签名。
	 */
	private void checkLose() {
		// 全部用户签名
		List<Siger> sigers = StaffOnHomePool.getInstance().getUsers();

		// 遗失的用户签名
		ArrayList<Siger> loses = new ArrayList<Siger>();

		// 找到不存在的用户签名，保存它
		for (Siger siger : sigers) {
			boolean success = CallOnHomePool.getInstance().contains(siger);
			if (!success) {
				loses.add(siger);
			}
		}

		// 全部正常，后面不用处理，退出！
		if (loses.isEmpty()) {
			Logger.debug(this, "check", "注册用户完成正常！！！");
			return;
		}

		Logger.debug(this, "check", "有 %d 个签名的CALL站点丢失！", loses.size());

		GatewayScaleTable table = new GatewayScaleTable();

		List<Node> nodes = CallOnHomePool.getInstance().detail();
		for (Node node : nodes) {
			CallSite site = (CallSite) CallOnHomePool.getInstance().find(node);
			if (site == null) {
				continue;
			}
			// 保存节点和账号数目
			table.add(node, site.size());
		}

		// 分配命令，提交给TOP站点
		for (Siger siger : loses) {
			Node remote = table.next();
			if (remote != null) {
				// 生成转发命令，保存到目标站点
				ShiftAwardCreateRefer shift = new ShiftAwardCreateRefer(remote, siger);
				HomeCommandPool.getInstance().admit(shift);
			}
		}
	}

	/**
	 * 签名在本地记录，但是CALL/DATA/WORK/BUILD上面不存在
	 */
	private void checkMissing() {
		List<Siger> signers = StaffOnHomePool.getInstance().getUsers();
		// 通知WATCH站点，DATA/WORK/BUILD站点不足
		SiteMissing missing = new SiteMissing();

		for (Siger siger : signers) {
			// 检查CALL站点，没有记录它
			NodeSet set = CallOnHomePool.getInstance().findSites(siger);
			if (set == null || set.isEmpty()) {
				missing.add(siger, SiteTag.CALL_SITE);
			}
			// 检查DATA站点，没有记录它
			set = DataOnHomePool.getInstance().findSites(siger);
			if (set == null || set.isEmpty()) {
				missing.add(siger, SiteTag.DATA_SITE);
			}
			// 检查BUILD站点，没有记录它
			set = BuildOnHomePool.getInstance().findSites(siger);
			if (set == null || set.isEmpty()) {
				missing.add(siger, SiteTag.BUILD_SITE);
			}
			// 检查WORK站点，没有记录它
			set = WorkOnHomePool.getInstance().findSites(siger);
			if (set == null || set.isEmpty()) {
				missing.add(siger, SiteTag.WORK_SITE);
			}
		}

		// 投递给全部WATCH站点
		if (missing.size() > 0) {
			NodeSet set = WatchOnHomePool.getInstance().list();
			if (set != null && set.size() > 0) {
				ShiftSiteMissing shift = new ShiftSiteMissing(set.show(), missing);
				HomeCommandPool.getInstance().admit(shift);
			}
		}
	}

	/**
	 * 推送命令
	 * @param others
	 * @param callSite
	 * @param siger
	 * @param array
	 */
	private void push(List<Node> others, Node callSite, Siger siger, Map<Node, SelectFieldToCall> array) {
		for (Node sub : others) {
			SelectFieldToCall cmd = array.get(sub);
			if (cmd == null) {
				cmd = new SelectFieldToCall(callSite); // 指定CALL站点
				array.put(sub, cmd); // DATA/WORK/BUILD站点 -> 命令
			}
			cmd.add(siger); // 记录签名
		}
	}

	/**
	 * 重新推送关联.
	 * 
	 * 重新建立完整的CALL节点和DATA/WORK/BUILD节点的关系。
	 */
	private void checkPush() {
		/** DATA/BUILD/WORK节点地址 -> 命令**/
		TreeMap<Node, SelectFieldToCall> array = new TreeMap<Node, SelectFieldToCall>();

		// 生成命令
		NodeSet callSites = new NodeSet(CallOnHomePool.getInstance().getNodes());
		for (Node callSite : callSites.show()) {
			// 拿到这个CALL站点下的全部用户签名
			List<Siger> sigers = CallOnHomePool.getInstance().findUsers(callSite);
			for (Siger siger : sigers) {
				// 检查DATA站点，记录关联
				NodeSet set = DataOnHomePool.getInstance().findSites(siger);
				if (set != null) {
					push(set.show(), callSite, siger, array);
				}
				// 检查BUILD站点，记录关联
				set = BuildOnHomePool.getInstance().findSites(siger);
				if (set != null) {
					push(set.show(), callSite, siger, array);
				}
				// 检查WORK站点，记录关联
				set = WorkOnHomePool.getInstance().findSites(siger);
				if (set != null) {
					push(set.show(), callSite, siger, array);
				}
			}
		}

		// 生成转发命令，推送给DATA/WORK/BUILD站点，要求它们重新注册到CALL站点
		Iterator<Map.Entry<Node, SelectFieldToCall>> iterator = array.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, SelectFieldToCall> entry = iterator.next();
			Node other = entry.getKey();
			SelectFieldToCall select = entry.getValue();

			ShiftSelectFieldToCall shift = new ShiftSelectFieldToCall(other, select);
			HomeCommandPool.getInstance().admit(shift);
		}
	}

	// CALL节点有某个签名，DATA/WORK/BUILD节点没有，点搞？

	//	/**
	//	 * 随机选择一个CALL站点
	//	 * @return
	//	 */
	//	private Node next(NodeSet callSites) {
	//		return callSites.next();
	//	}

	//	/**
	//	 * 保存一个命令
	//	 * @param siger
	//	 * @param callSites
	//	 * @param sources
	//	 * @param array
	//	 */
	//	private void push(Siger siger, NodeSet callSites, List<Node> sources, List<ShiftSelectFieldToCall> array) {
	//		// 下一个节点
	//		Node call = next(callSites);
	//		// 生成命令
	//		SelectFieldToCall cmd = new SelectFieldToCall(call);
	//		cmd.add(siger);
	//		// 转发
	//		for (Node source : sources) {
	//			ShiftSelectFieldToCall shift = new ShiftSelectFieldToCall(source, cmd);
	//			array.add(shift);
	//		}
	//	}
	//
	//	/**
	//	 * 检查HOME站点上的在线资源
	//	 */
	//	private void check() {
	//		List<Siger> signers = StaffOnHomePool.getInstance().getUsers();
	//
	//		// CALL站点上的用户签名
	//		TreeSet<Siger> callSigners = new TreeSet<Siger>(CallOnHomePool.getInstance().getUsers());
	//		// CALL节点
	//		NodeSet callSites = new NodeSet(CallOnHomePool.getInstance().getNodes());
	//		
	//		Logger.debug(this, "check", "signer size:%d, call sigers:%d, call sites:%d",
	//				signers.size(), callSigners.size(), callSites.size());
	//
	////		ArrayList<Siger> warns = new ArrayList<Siger>();
	////		// 循环检查CALL站点，没有保存这个签名
	////		for (Siger siger : signers) {
	////			if (!callSigners.contains(siger)) {
	////				warns.add(siger);
	////			}
	////		}
	////		Logger.debug(this, "check", "warning signers:%d", warns.size());
	//
	////		warns.addAll(callSigners);
	//		
	//		// 转发命令
	//		ArrayList<ShiftSelectFieldToCall> shifts = new ArrayList<ShiftSelectFieldToCall>();
	//		// 通知WATCH站点，DATA/WORK/BUILD站点不足
	//		SiteMissing missing = new SiteMissing();
	//
	//		for (Siger siger : signers) {
	//			Refer refer = StaffOnHomePool.getInstance().find(siger);
	//			if(refer == null) {
	//				continue;
	//			}
	//			
	//			// 检查CALL站点，没有记录它
	//			NodeSet set = CallOnHomePool.getInstance().findSites(siger);
	//			if (set == null || set.isEmpty()) {
	//				// 分配一个账号给CALL站点
	////				missing.add(siger, SiteTag.CALL_SITE);
	//				
	//				// 选择一个CALL站点，强制推送给它
	//				AwardCreateRefer award = new AwardCreateRefer(refer);
	//				Node node = next(callSites);
	//				
	////				ShiftAwardCreateRefer shift = new ShiftAwardCreateRefer(award);
	//				continue;
	//			}
	//			
	//			// 检查DATA站点
	//			set = DataOnHomePool.getInstance().findSites(siger);
	//			// DATA有这个用户签名，通知DATA节点重新注册；没有通知WATCH站点
	//			if (set != null && set.size() > 0) {
	//				push(siger, callSites, set.show(), shifts);
	//			} else {
	//				missing.add(siger, SiteTag.DATA_SITE);
	//			}
	//			// 检查BUILD站点
	//			set = BuildOnHomePool.getInstance().findSites(siger);
	//			if (set != null && set.size() > 0) {
	//				push(siger, callSites, set.show(), shifts);
	//			} else {
	//				missing.add(siger, SiteTag.BUILD_SITE);
	//			}
	//			// 检查WORK站点
	//			set = WorkOnHomePool.getInstance().findSites(siger);
	//			if (set != null && set.size() > 0) {
	//				push(siger, callSites, set.show(), shifts);
	//			} else {
	//				missing.add(siger, SiteTag.WORK_SITE);
	//			}
	//		}
	//		
	//		Logger.debug(this, "check", "shift size:%d, missing item:%d", shifts.size(), missing.size());
	//
	//		// 通知DATA/BUILD/WORK站点，重新注册到CALL站点
	//		for (ShiftSelectFieldToCall shift : shifts) {
	//			HomeCommandPool.getInstance().admit(shift);
	//		}
	//		// 投递给WATCH站点
	//		if (missing.size() > 0) {
	//			NodeSet set = WatchOnHomePool.getInstance().list();
	//			if (set != null && set.size() > 0) {
	//				ShiftSiteMissing shift = new ShiftSiteMissing(set.show(), missing);
	//				HomeCommandPool.getInstance().admit(shift);
	//			}
	//		}
	//	}

}