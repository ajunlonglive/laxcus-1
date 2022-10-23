/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import java.util.*;

import com.laxcus.command.field.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;

/**
 * BUILD站点资源管理池。<br>
 * 保存ESTABLISH.SIFT阶段命名。
 * 
 * @author scott.liang
 * @version 1.0 4/23/2012
 * @since laxcus 1.0
 */
public final class BuildOnCallPool extends SlaveOnCallPool {

	/** 分布资源管理池静态句柄(全局唯一) **/
	private static BuildOnCallPool selfHandle = new BuildOnCallPool();
	
	/** BUILD站点地址 -> 授权命令 **/
	private Map<Node, PushBuildField> mapFields = new TreeMap<Node, PushBuildField>();
	
	/** 全部BUILD站点 **/
	private NodeSet sites = new NodeSet();

	/**
	 * 构造ESTABLISH.SIFT阶段资源管理池
	 */
	private BuildOnCallPool() {
		super(SiteTag.BUILD_SITE);
		super.setSleepTimeMillis(60000);
	}

	/**
	 * 返回ESTABLISH.SIFT阶段管理池的静态句柄
	 * @return
	 */
	public static BuildOnCallPool getInstance() {
		return BuildOnCallPool.selfHandle;
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
		Logger.info(this, "process", "into...");
		// 定时检查
		while (!isInterrupted()) {
			sleep();
		}
		Logger.info(this, "process", "exit");
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.call.pool.FieldOnCallPool#finish()
	 */
	@Override
	public void finish() {
		super.finish();
		mapFields.clear();
	}

	/**
	 * 输出全部BUILD站点
	 * @return BUILD站点地址列表
	 */
	public NodeSet list() {
		return sites;
	}

	/**
	 * 根据站点地址，查找它的站点数据
	 * @param node
	 * @return
	 */
	private PushBuildField findField(Node node) {
		if (node != null) {
			return mapFields.get(node);
		}
		return null;
	}
	
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.task.TaskHelper#isSystemLevel(com.laxcus.util.naming.Phase)
//	 */
//	@Override
//	public boolean isSystemLevel(Phase phase) {
//		if (PhaseTag.isEstablish(phase.getFamily())) {
//			return IssueTaskPool.getInstance().isSystemLevel(phase.getRoot());
//		}
//		return false;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.task.TaskHelper#isUserLevel(com.laxcus.util.naming.Phase)
//	 */
//	@Override
//	public boolean isUserLevel(Phase phase) {
//		if (PhaseTag.isEstablish(phase.getFamily())) {
//			return IssueTaskPool.getInstance().isUserLevel(phase.getRoot());
//		}
//		return false;
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.task.establish.seeker.SiftSeeker#findSiftSites(com.laxcus.util.naming.Phase)
//	 */
//	@Override
//	public NodeSet findSiftSites(Phase sift) throws TaskException {
//		// 是系统级命名，返回全部BUILD地址。每个BUILD站点固定保存全部“系统级”SIFT阶段分布组件。
//		if(isSystemLevel(sift)) {
//			return sites;
//		}
//		
//		// 可能是用户级组件，进行ISSUE阶段安全检查，判断签名一致
//		Phase issue = new Phase(sift.getIssuer(), PhaseTag.ISSUE, sift.getRoot());
//		boolean success = IssueTaskPool.getInstance().contains(issue);
//		if (!success) {
//			throw new TaskSecurityException("refuse %s", sift);
//		}
//		
//		return super.findSites(sift);
//	}
	
	/**
	 * 注入BUILD站点数据
	 * @param field
	 */
	private void infuse(PushBuildField field) {
		Node node = field.getNode();
		mapFields.put(node, field);
		// 保存阶段命名和用户签名
		infusePhases(node, field.getPhases());
		infuseSigers(node, field.getSigers());
	}

	/**
	 * 释放BUILD站点数据
	 * @param node BUILD站点地址
	 */
	private void effuse(Node node) {
		// 删除地址
		PushBuildField field = mapFields.remove(node);
		boolean success = (field != null);
		// 删除父级类的阶段命名
		if (success) {
			effusePhases(node, field.getPhases());
			effuseSigers(node, field.getSigers());
		}
	}

	/**
	 * 注入BUILD站点数据。数据发送自BUILD站点。
	 * 
	 * @param field 推送BUILD站点元数据
	 */
	public void push(PushBuildField field) {
		Node node = field.getNode();
		
		Logger.debug(this, "push", "from %s", node);
		boolean success = false;
		
		super.lockSingle();
		try {
			// 撤销
			effuse(node);
			// 保存
			infuse(field);
			// 保存站点地址
			sites.add(node);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		if (success) {
			getLauncher().touch();
		}
	}

	/**
	 * 撤销BUILD站点数据
	 * @param node BUILD站点
	 */
	public void drop(Node node) {
		Logger.debug(this, "drop", "this is %s", node);
		boolean success = false;
		
		super.lockSingle();
		try {
			// 删除站点地址
			sites.remove(node);
			// 撤销参数
			effuse(node);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		if (success) {
			getLauncher().touch();
		}
	}

	/**
	 * 判断站点存在
	 * 
	 * @param node
	 * @return
	 */
	public boolean contains(Node node) {
		boolean success = false;
		super.lockMulti();
		try {
			PushBuildField field = findField(node);
			success = (field != null);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

}