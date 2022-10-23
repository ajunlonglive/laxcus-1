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
 * WORK站点资源管理池。保存CONDUCT.TO阶段命名。
 * 
 * @author scott.liang
 * @version 1.0 4/23/2012
 * @since laxcus 1.0
 */
public final class WorkOnCallPool extends SlaveOnCallPool { 

	/** WORK静态句柄(全局唯一) **/
	private static WorkOnCallPool selfHandle = new WorkOnCallPool();

	/** WORK站点地址 -> 授权命令 **/
	private Map<Node, PushWorkField> mapFields = new TreeMap<Node, PushWorkField>();
	
	/** 全部WORK站点 **/
	private NodeSet sites = new NodeSet();

	/**
	 * 构造WORK站点资源管理池
	 */
	private WorkOnCallPool() {
		super(SiteTag.WORK_SITE);
		setSleepTimeMillis(60000);
	}

	/**
	 * 返回WORK站点资源管理池静态句柄
	 * @return
	 */
	public static WorkOnCallPool getInstance() {
		return WorkOnCallPool.selfHandle;
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
	 * 输出全部WORK站点
	 * @return WORK站点地址列表
	 */
	public NodeSet list() {
		return sites;
	}

	/**
	 * 注入WORK站点元数据
	 * @param field
	 */
	private void infuse(PushWorkField field) {
		Node node = field.getNode();
		mapFields.put(node, field);
		// 保存阶段命名和用户签名
		infusePhases(node, field.getPhases());
		infuseSigers(node, field.getSigers());
	}

	/**
	 * 注销某个WORK站点数据
	 * @param node WORK站点地址
	 */
	private void effuse(Node node) {
		PushWorkField field = mapFields.remove(node);
		boolean success = (field != null);
		if (success) {
			effusePhases(node, field.getPhases());
			effuseSigers(node, field.getSigers());
		}
	}

	/**
	 * 注入一个WORK站点数据。数据发送自WORK站点。
	 * 
	 * @param field 推送WORK站点元数据
	 */
	public void push(PushWorkField field) {
		Node node = field.getNode();
		
		Logger.debug(this, "push", "from %s", node);
		
		boolean success = false;
		
		super.lockSingle();
		try {
			// 撤销
			effuse(node);
			// 增加
			infuse(field);
			// 保存地址
			sites.add(node);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 延时注册
		if (success) {
			getLauncher().touch();
		}
	}

	/**
	 * 撤消WORK站点数据元数据
	 * @param node WORK站点
	 */
	public void drop(Node node) {
		Logger.debug(this, "drop", "this is %s", node);
		
		boolean success = false;
		
		super.lockSingle();
		try {
			// 删除地址
			sites.remove(node);
			// 删除配置
			effuse(node);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 延时注册
		if (success) {
			getLauncher().touch();
		}
	}

	/**
	 * 根据站点地址，查找它的站点数据
	 * @param node
	 * @return
	 */
	private PushWorkField findField(Node node) {
		if (node != null) {
			return mapFields.get(node);
		}
		return null;
	}

	/**
	 * 判断站点存在
	 * @param node
	 * @return
	 */
	public boolean contains(Node node) {
		boolean success = false;
		super.lockMulti();
		try {
			PushWorkField field = findField(node);
			success = (field != null);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

}