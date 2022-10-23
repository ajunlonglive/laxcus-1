/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import java.util.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.index.balance.*;
import com.laxcus.access.index.section.*;
import com.laxcus.access.index.zone.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.command.zone.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.seeker.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * CONDUCT.FROM分布资源管理器。
 * 
 * @author scott.liang
 * @version 1.0 5/2/2018
 * @since laxcus 1.0
 */
public class FromSeekManager extends ConductSeekManager implements FromSeeker {
	
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
	private static FromSeekManager selfHandle = new FromSeekManager();

	/**
	 * 构造私有的CONDUCT.FROM分布资源管理器
	 */
	private FromSeekManager() {
		super();
	}

	/**
	 * 返回CONDUCT.FROM分布资源管理器实例
	 * @return CONDUCT.FROM分布资源管理器句柄
	 */
	public static FromSeekManager getInstance() {
		// 安全检查
		TrackManager.check("FromSeekManager.getInstance");
		// 返回句柄
		return FromSeekManager.selfHandle;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeSeeker#isSystemLevel(long, com.laxcus.util.naming.Phase)
	 */
	@Override
	public boolean isSystemLevel(long invokerId, Phase phase) throws TaskException {
		// 用户签名有效
		available(invokerId);
		
		if (PhaseTag.isConduct(phase.getFamily())) {
			return initPool.isSystemLevel(phase.getSock());
		} 
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeSeeker#isUserLevel(long, com.laxcus.util.naming.Phase)
	 */
	@Override
	public boolean isUserLevel(long invokerId,  Phase phase) throws TaskException {
		// 用户签名有效
		available(invokerId);
		
		if (PhaseTag.isConduct(phase.getFamily())) {
			return initPool.isUserLevel(phase.getSock());
		} 
		return false;
	}

	/* 
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.seeker.FromSeeker#createPrimeStubSector(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public List<StubSector> createPrimeStubSector(long invokerId, Space space) throws TaskException {
		// 判断调用器编号和表有效
		available(invokerId, space);
		
		// 找到主块数据分区
		return metaPool.doPrimeStubSector(space);
		
//		// 找主站点
//		super.lockMulti();
//		try {
//			return metaPool.doPrimeStubSector(space);
//		} catch (TaskException e) {
//			throw e;
//		} catch (Throwable e) {
//			throw new TaskException(e);
//		} finally {
//			super.unlockMulti();
//		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.seeker.FromSeeker#createSlaveStubSector(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public List<StubSector> createSlaveStubSector(long invokerId, Space space) throws TaskException {
		// 判断用户签名和表被接受
		available(invokerId, space);
		
		// 生成数据从块分区
		return metaPool.doSlaveStubSector(space);

//		// 从站点分区
//		super.lockMulti();
//		try {
//			return metaPool.doSlaveStubSector(space);
//		} catch (TaskException e) {
//			throw e;
//		} catch (Throwable e) {
//			throw new TaskException(e);
//		} finally {
//			super.unlockMulti();
//		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.seeker.FromSeeker#createStubSector(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public List<StubSector> createStubSector(long invokerId, Space space)
			throws TaskException {
		// 判断用户签名和表被接受
		available(invokerId, space);
		
		return metaPool.doStubSector(space);

//		super.lockMulti();
//		try {
//			return metaPool.doStubSector(space);
//		} catch (TaskException e) {
//			throw e;
//		} catch (Throwable e) {
//			throw new TaskException(e);
//		} finally {
//			super.unlockMulti();
//		}
	}

	/**
	 * 通过网络查询分布的索引分区。通过admit方法和命令钩子进行。采用admit方法是让机器能够调控载荷。
	 * @param hubs 服务器地址
	 * @param dock 列空间
	 * @return 返回索引分区列表
	 * @throws TaskException
	 */
	private List<IndexZone> findIndexZone(NodeSet hubs, Dock dock) throws TaskException {
		// 命令和钩子
		FindIndexZone cmd =	new FindIndexZone(dock);
		IndexZoneHook hook = new IndexZoneHook();
		// 转发命令
		ShiftFindIndexZone shift = new ShiftFindIndexZone(cmd, hook);
		shift.setHubs(hubs.show()); // 设置目标地址

		// 直接放入命令队列
		boolean success = switchPool.admit(shift);
		if (!success) {
			hook.done(); // 释放等待
			throw new TaskException("command denied");
		}
		// 进入等待，直到完成
		hook.await();

		return hook.list();
	}
	
	/**
	 * 根据列数据类型建立一个列索引范围平衡分割器
	 * @param attribute 列属性
	 * @return 返回关联的列索引范围平衡分割器
	 * @throws TaskException
	 */
	private IndexBalancer createBalancer(ColumnAttribute attribute) throws TaskException {
		IndexBalancer balancer = IndexBalancerCreator.create(attribute);
		if (balancer == null) {
			throw new TaskException("illegal attribute: %d", attribute.getType());
		}
		return balancer;
	}
	
	/**
	 * 建立一个默认列索引区域
	 * @param attribute 列属性
	 * @return 返回IndexZone子类实例
	 * @throws TaskException
	 */
	private IndexZone createDefaultIndexZone(ColumnAttribute attribute) throws TaskException {
		IndexZone zone = IndexZoneCreator.createDefault(attribute.getType());
		if (zone == null) {
			throw new TaskException("illegal family: %d", attribute.getType());
		}
		return zone;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.seeker.FromSeeker#createIndexSector(long, com.laxcus.access.schema.Dock, int)
	 */
	@Override
	public ColumnSector createIndexSector(long invokerId, Dock dock, int sites) throws TaskException {
		// 判断获得许可		
		Space space = dock.getSpace();
		available(invokerId, space);

		// 找本地表
		Table table = staffPool.findLocalTable(space); 
		// 去管理节点找
		if (table == null) {
			table = findHubTable(-1, space);
		}
		if (table == null) {
			throw new TaskNotFoundException("cannot be find '%s'", space);
		}
		ColumnAttribute attribute = table.find(dock.getColumnId());
		if (attribute == null) {
			throw new TaskNotFoundException("cannot be find '%s'", dock);
		}
		// 找主站点
		NodeSet set = metaPool.findPrimeTableSites(space);
		if (set == null || set.isEmpty()) {
			throw new TaskNotFoundException("cannot be find '%s'", dock);
		}

		// 通过网络检索索引分区
		List<IndexZone> zones = findIndexZone(set, dock);
		// 根据列属性建立索引平衡处理器
		IndexBalancer balancer = createBalancer(attribute);

		// 如果是空集合，生成一个默认的索引分区，否则它们
		if (zones.isEmpty()) {
			IndexZone e = createDefaultIndexZone(attribute);
			balancer.add(e);
		} else {
			for (IndexZone e : zones) {
				balancer.add(e);
			}
		}

		// 输出分区
		ColumnSector sector = balancer.balance(sites);
		// 设置列空间
		if (sector != null) {
			sector.setDock(dock);
		}
		return sector;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.seeker.FromSeeker#findFromTable(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findFromTable(long invokerId, Space space) throws TaskException {
		// 判断签名和表有效
		available(invokerId, space);

		// 在本地找
		Table table = staffPool.findLocalTable(space);
		// 去管理节点找
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
	 * @see com.laxcus.task.conduct.seeker.FromSeeker#findFromSites(long, com.laxcus.util.naming.Phase)
	 */
	@Override
	public NodeSet findFromSites(long invokerId, Phase from) throws TaskException {
		// 如果是系统级阶段命名，返回全部站点。每个DATA主机默认保存全部“系统级”FROM阶段分布组件
		if (isSystemLevel(invokerId, from)) {
			return metaPool.list();
		}

		// 是用户级组件，进行安全检查，判断签名一致
		Phase init = new Phase(from.getIssuer(), PhaseTag.INIT, from.getSock());
		boolean success = initPool.contains(init);
		if (!success) {
			throw new TaskSecurityException("refuse %s", from);
		}
		// 查询FROM站点地址
		return metaPool.findSites(from);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.seeker.FromSeeker#findFromSites(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public NodeSet findFromSites(long invokerId, Space space) throws TaskException {
		// 判断用户签名和数据表名有效且匹配
		available(invokerId, space);
		// 返回全部表
		return metaPool.findTableSites(space);
	}

	/* 
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.seeker.FromSeeker#findFromSites(long, com.laxcus.util.naming.Phase, com.laxcus.access.schema.Space)
	 */
	@Override
	public NodeSet findFromSites(long invokerId, Phase from, Space space)
			throws TaskException {
		NodeSet set = new NodeSet();
		// 查找阶段命名
		NodeSet next = findFromSites(invokerId, from);
		if (next == null) {
			return set;
		}
		// 保存全部
		set.addAll(next.show());

		// 查找数据表名
		next = findFromSites(invokerId, space);
		if (next == null) {
			set.clear();
		} else {
			set.AND(new TreeSet<Node>(next.show()));
		}

		return set;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.seeker.FromSeeker#getFromSites(long)
	 */
	@Override
	public int getFromSites(long invokerId) throws TaskException {
		// 找到用户签名
		Siger issuer = findIssuer(invokerId);
		if (issuer == null) {
			throw new TaskNotFoundException("cannot be find issuer! %d", invokerId);
		}
		// 判断允许
		boolean success = allow(issuer);
		if (!success) {
			throw new TaskSecurityException("security denied '%s'", issuer);
		}
		NodeSet set = metaPool.findSites(issuer);
		return (set == null ? 0 : set.size());
	}

}