/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import com.laxcus.access.schema.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.seeker.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * ESTABLISH.SCAN分布资源管理器。部署在CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 5/2/2018
 * @since laxcus 1.0
 */
public class ScanSeekManager extends EstablishSeekManager implements ScanSeeker {
	
	/** DATA站点元数据管理池 **/
	protected DataOnCallPool metaPool;

	/**
	 * 设置DATA站点元数据管理池
	 * 
	 * @param e
	 */
	public void setMetaPool(DataOnCallPool e) {
		Laxkit.nullabled(e);
		metaPool = e;
	}

	/** 定义句柄 **/
	private static ScanSeekManager selfHandle = new ScanSeekManager();

	/**
	 * 构造私有的ESTABLISH.SCAN分布资源管理器
	 */
	private ScanSeekManager() {
		super();
	}

	/**
	 * 返回ESTABLISH.SCAN分布资源管理器实例
	 * @return ESTABLISH.SCAN分布资源管理器句柄
	 */
	public static ScanSeekManager getInstance() {
		// 安全检查
		TrackManager.check("ScanSeekManager.getInstance");
		// 返回句柄
		return ScanSeekManager.selfHandle;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeSeeker#isSystemLevel(long, com.laxcus.util.naming.Phase)
	 */
	@Override
	public boolean isSystemLevel(long invokerId, Phase phase) throws TaskException {
		// 用户签名有效
		available(invokerId);
		
		if (PhaseTag.isEstablish(phase.getFamily())) {
			return issuePool.isSystemLevel(phase.getSock());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeSeeker#isUserLevel(long, com.laxcus.util.naming.Phase)
	 */
	@Override
	public boolean isUserLevel(long invokerId, Phase phase) throws TaskException {
		// 用户签名有效
		available(invokerId);
		
		if (PhaseTag.isEstablish(phase.getFamily())) {
			return issuePool.isUserLevel(phase.getSock());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.seeker.ScanSeeker#findScanTable(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findScanTable(long invokerId, Space space) throws TaskException {
		// 判断用户签名和表被接受
		available(invokerId, space);

		// 在本地查找
		Table table = staffPool.findLocalTable(space);
		// 通过网络，去管理节点找
		if (table == null) {
			table = findHubTable(invokerId, space);
		}
		if (table == null) {
			throw new TaskException("cannot be find '%s'", space);
		}
		return table;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.seeker.ScanSeeker#findScanSites(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public NodeSet findScanSites(long invokerId, Space space) throws TaskException {
		// 判断用户签名和数据表名有效且匹配
		available(invokerId, space);
		// 查找DATA主站点
		return metaPool.findPrimeTableSites(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.establish.seeker.ScanSeeker#findScanSites(com.laxcus.util.naming.Phase)
	 */
	@Override
	public NodeSet findScanSites(long invokerId, Phase phase) throws TaskException {
		// 判断是系统组件
		if (isSystemLevel(invokerId, phase)) {
			return metaPool.list();
		}

		Phase issue = new Phase(phase.getIssuer(), PhaseTag.ISSUE, phase.getSock());
		boolean success = issuePool.contains(issue);
		if (!success) {
			throw new TaskSecurityException("refuse %s", phase);
		}

		return metaPool.findSites(phase);
	}

	//	/**
	//	 * 产生一个站点地址副本
	//	 * @param set
	//	 * @return
	//	 */
	//	private NodeSet clone(NodeSet set) {
	//		NodeSet nodes = new NodeSet();
	//		super.lockSingle();
	//		try {
	//			int index = set.getIterateIndex();
	//			nodes.addAll(set);
	//			nodes.setIterateIndex(index);
	//			if (set.size() >= index) {
	//				set.setIterateIndex(0);
	//			} else {
	//				set.setIterateIndex(index + 1);
	//			}
	//		} catch (Throwable e) {
	//			Logger.error(e);
	//		} finally {
	//			super.unlockSingle();
	//		}
	//		return nodes;
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.establish.seeker.ScanSeeker#findStubSites(com.laxcus.util.Siger, com.laxcus.access.schema.Space, long)
	 */
	@Override
	public NodeSet findStubSites(long invokerId, Space space, long stub) throws TaskException {
		// 判断用户签名和数据表名有效且匹配
		available(invokerId, space);

		// 返回数据块关联节点
		return metaPool.findScanStubSites(space, stub);

		//		// 锁定处理
		//		NodeSet set = metaPool.findScanStubSites(space, stub);;
		//		super.lockMulti();
		//		try {
		//			set = metaPool.findScanStubSites(space, stub);
		//		} catch (Throwable e) {
		//			throw new TaskException(e);
		//		} finally {
		//			super.unlockMulti();
		//		}
		//		
		//		if(set == null  || set.isEmpty()) {
		//			throw new TaskException("cannot be find %s#%x", space, stub);
		//		}
		//		return set;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.seeker.ScanSeeker#isMaster(long, com.laxcus.site.Node)
	 */
	@Override
	public boolean isMaster(long invokerId, Node node) throws TaskException {
		// 检查账号有效
		available(invokerId);
		// 判断是主节点
		return metaPool.isMaster(node);

		//		// 取实例
		//		PushDataField field = metaPool.findField(node);
		//		return (field != null && field.isMaster());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.seeker.ScanSeeker#isSlave(long, com.laxcus.site.Node)
	 */
	@Override
	public boolean isSlave(long invokerId, Node node) throws TaskException {
		// 检查账号有效
		available(invokerId);
		// 判断是从节点
		return metaPool.isSlave(node);
		
		//		// 取实例
		//		PushDataField field = metaPool.findField(node);
		//		return (field != null && field.isSlave());
	}

}
