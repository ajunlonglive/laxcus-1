/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.command.field.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * DATA站点资源管理池。<br>
 * 保存与DATA站点关联的数据表和阶段命名记录。
 * 
 * @author scott.liang
 * @version 1.0 4/23/2012
 * @since laxcus 1.0
 */
public final class DataOnCallPool extends SlaveOnCallPool {

	/** 静态句柄(全局唯一) **/
	private static DataOnCallPool selfHandle = new DataOnCallPool();

	/** DATA站点地址 -> 元数据命令 **/
	private Map<Node, PushDataField> mapFields = new TreeMap<Node, PushDataField>();

	/** 数据块编号 -> DATA站点集合 **/
	private Map<java.lang.Long, NodeSet> mapStubs = new TreeMap<java.lang.Long, NodeSet>();

	/** 数据表名 -> DATA站点集合 **/
	private Map<Space, NodeSet> mapSpaces = new TreeMap<Space, NodeSet>();

	/** 数据表名 -> DATA主站点集合 **/
	private Map<Space, NodeSet> mapMasters = new TreeMap<Space, NodeSet>();

	/** 全部站点地址 **/
	private NodeSet sites = new NodeSet();

	/**
	 * 构造DATA资源资源管理池
	 */
	private DataOnCallPool() {
		super(SiteTag.DATA_SITE);
	}

	/**
	 * 返回静态句柄
	 * @return
	 */
	public static DataOnCallPool getInstance() {
		return DataOnCallPool.selfHandle;
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
		while (!super.isInterrupted()) {
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
		mapStubs.clear();
		mapSpaces.clear();
		mapMasters.clear();
	}
	
	/**
	 * 查找表的实际持有人（注意！不是被授权人！！！）
	 * @param space 数据表名 
	 * @return 返回数据表持有人，没有返回空指针
	 */
	public Siger findOwner(Space space) {
		return StaffOnCallPool.getInstance().findOwner(space);
	}

	/**
	 * 输出全部DATA站点
	 * @return DATA站点地址列表
	 */
	public NodeSet list() {
		return sites;
	}

	/**
	 * 判断指定DATA站点指定表下的剩余空间符合本次数据尺寸需求
	 * @param node DATA站点地址
	 * @param space 数据表名
	 * @param capacity 要求的磁盘空间
	 * @return 返回真或者假
	 */
	public boolean conform(Node node, Space space, long capacity) {
		boolean success = false;
		// 锁定
		super.lockMulti();
		try {
			PushDataField field = mapFields.get(node);
			success = (field != null);
			if (success) {
				StubTable table = field.find(space);
				success = (table != null);
				
				//				// 判断磁盘剩余空间，保留1G的剩余空间
				//				if (success) {
				//					success = (table.getLeft() - Laxkit.GB >= capacity);
				//				}

				// 判断磁盘空间足够！
				if (success) {
					success = field.isDiskPassed();
				}
				// 判断内存足够!
				if (success) {
					success = field.isMemoryPassed();
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}
	
	/**
	 * 判断有DATA站点的数据表在CALL站点注册存在
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean contains(Space space) {
		Laxkit.nullabled(space);
		super.lockMulti();
		try {
			return mapSpaces.get(space) != null;
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断站点存在
	 * @param node DATA节点
	 * @return 返回真或者假
	 */
	public boolean contains(Node node) {
		Laxkit.nullabled(node);
		
		super.lockMulti();
		try {
			return mapFields.get(node) != null;
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断是DATA主节点
	 * @param node DATA节点
	 * @return 返回真或者假
	 */
	public boolean isMaster(Node node) {
		Laxkit.nullabled(node);

		super.lockMulti();
		try {
			PushDataField field = mapFields.get(node);
			return field != null && field.isMaster();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断是DATA从节点
	 * @param node DATA节点地址
	 * @return 返回真或者假
	 */
	public boolean isSlave(Node node) {
		Laxkit.nullabled(node);

		super.lockMulti();
		try {
			PushDataField field = mapFields.get(node);
			return field != null && field.isSlave();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 注入新的DATA站点元数据信息
	 * @param field DATA站点授权命令
	 */
	private void infuse(PushDataField field) {
		Node node = field.getNode();
		// 保存命令
		mapFields.put(node, field);

		// 分别保存各单元参数
		List<StubTable> tables = field.getStubTables();
		for (StubTable table : tables) {
			Space space = table.getSpace();
			// 数据表名映射
			NodeSet set = mapSpaces.get(space);
			if (set == null) {
				set = new NodeSet();
				mapSpaces.put(space, set);
			}
			set.add(node);

			// 主站点
			if (field.isMaster()) {
				set = mapMasters.get(space);
				if (set == null) {
					set = new NodeSet();
					mapMasters.put(space, set);
				}
				set.add(node);
			}

			// 数据块编号
			for (Long stub : table.list()) {
				set = mapStubs.get(stub);
				if (set == null) {
					set = new NodeSet();
					mapStubs.put(stub, set);
				}
				set.add(node);
			}
		}
		// 保存阶段命名和用户签名
		infusePhases(node, field.getPhases());
		infuseSigers(node, field.getSigers());
	}

	/**
	 * 根据DATA站点地址，撤销DATA站点的授权数据
	 * @param node DATA站点地址
	 */
	private void effuse(Node node) {
		PushDataField field = mapFields.remove(node);
		if (field == null) {
			return;
		}
		// 逐一删除
		List<StubTable> tables = field.getStubTables();
		for (StubTable table : tables) {
			Space space = table.getSpace();
			// 删除数据表名
			NodeSet set = mapSpaces.get(space);
			if (set != null) {
				set.remove(node);
				if (set.isEmpty()) mapSpaces.remove(space);
			}

			// 删除主站点的表名
			set = mapMasters.get(space);
			if (set != null) {
				set.remove(node);
				if (set.isEmpty()) mapMasters.remove(space);
			}

			// 删除数据块编号
			for (Long stub : table.list()) {
				set = mapStubs.get(stub);
				if (set != null) {
					set.remove(node);
					if (set.isEmpty()) mapStubs.remove(stub);
				}
			}
		}

		// 删除阶段命名和用户签名
		effusePhases(node, field.getPhases());
		effuseSigers(node, field.getSigers());
	}

	/**
	 * 注入一个DATA站点数据。数据发送自DATA站点。
	 * 
	 * @param field 推送DATA站点元数据
	 */
	public void push(PushDataField field) {
		Node node = field.getNode();

		Logger.debug(this, "push", "from %s", node);
		
		boolean success = false;

		// 锁定
		super.lockSingle();
		try {
			// 1. 撤销旧记录
			effuse(node);
			// 2. 追加新记录
			infuse(field);
			// 保存这个站点地址
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
	 * 撤销DATA站点数据
	 * @param node DATA站点地址
	 */
	public void drop(Node node) {
		Logger.debug(this, "drop", "this is %s", node);
		boolean success = false;
		
		// 锁定
		super.lockSingle();
		try {
			// 删除站点地址
			sites.remove(node);
			// 撤销记录
			effuse(node);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		if (success) {
			// 删除映像数据块池的数目
			CacheReflexStubOnCallPool.getInstance().remove(node);
			// 延迟注册
			getLauncher().touch();
		}
	}

	/**
	 * 查找数据表所在的DATA站点，包括主/从DATA站点。
	 * @param space 数据表名
	 * @return 返回对应的站点集合，或者空指针
	 */
	public NodeSet findTableSites(Space space) {
		Laxkit.nullabled(space);
		// 锁定
		super.lockMulti();
		try {
			return mapSpaces.get(space);
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 统计一个表的主表磁盘空间尺寸
	 * @param space 表名
	 * @return 返回统计值
	 */
	public long countTableCapacity(Space space) {
		long count = 0;
		
		// 锁定
		super.lockMulti();
		try {
			NodeSet set = mapSpaces.get(space);
			if (set != null) {
				for (Node node : set.list()) {
					PushDataField field = mapFields.get(node);
					if (field != null) {
						StubTable st = field.find(space);
						if (st != null) {
							count += st.getAvailable(); // 已经占用的空间
						}
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		
		Logger.debug(this, "countTableCapacity", "count %s size is %d", space, count);
		
		return count;
	}

	/**
	 * 查找数据表所在的DATA主站点。
	 * @param space 数据表名
	 * @return 返回对应的主站点集合
	 */
	public NodeSet findPrimeTableSites(Space space) {
		Laxkit.nullabled(space);

		super.lockMulti();
		try {
			return mapMasters.get(space);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据数据表名和节点集合，获得节点下的数据块分区
	 * @param space 数据表名
	 * @param nodes 节点集合
	 * @return 返回StubSector集合
	 */
	private List<StubSector> doStubSector(Space space, List<Node> nodes) {
		// 全部数据块编号
		TreeSet<Long> stubs = new TreeSet<Long>();

		// 取出全部数据块
		for(Node node : nodes) {
			// 找数据块区
			PushDataField field = mapFields.get(node);
			if (field == null) {
				continue;
			}
			// 查找数据块索引表
			StubTable table = field.find(space);
			// 筛选，保存唯一的数据块
			if (table == null) {
				continue;
			}
			// 保存数据块编号
			stubs.addAll(table.list());
		}

		// 站点地址 -> 数据块分区
		TreeMap<Node, StubSector> array = new TreeMap<Node, StubSector>();
		// 逐一筛选，保持数据块编号不重叠
		for(Long stub : stubs) {
			NodeSet set = mapStubs.get(stub);
			if (set == null) {
				continue;
			}

			// 顺序读取站点地址，实现平衡分配，保证被选站点在指定集合中
			int size = set.size();
			for (int i = 0; i < size; i++) {
				// 下一个站点
				Node node = set.follow();
				// 如果站点不在指定集合中，忽略它
				if (!nodes.contains(node)) {
					continue;
				}
				// 分配资源 
				StubSector sector = array.get(node);
				if (sector == null) {
					sector = new StubSector(space, node);
					array.put(node, sector);
				}
				// 保存这个数据块，退出
				sector.add(stub);
				break;
			}
		}

		// 返回结果
		return new ArrayList<StubSector>(array.values());
	}

	/**
	 * 根据数据表名查找关联的主数据块分区
	 * @param space 数据表名
	 * @return 返回StubSector集合，没有是空指针
	 */
	public List<StubSector> doPrimeStubSector(Space space) throws TaskException {
		// 锁定处理
		super.lockMulti();
		try {
			// 1. 找关联表名的DATA主站点
			NodeSet set = mapMasters.get(space);
			// 2. 从主块中生成分区
			if (set != null && set.size() > 0) {
				return doStubSector(space, set.list());
			}
		} catch (Throwable e) {
			throw new TaskException(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 根据数据表名获得从数据块分区
	 * @param space 数据表名
	 * @return 返回StubSector集合，没有是空指针
	 */
	public List<StubSector> doSlaveStubSector(Space space) throws TaskException {
		// 锁定处理
		super.lockMulti();
		try {
			// 1. 查找与表关联的全部DATA站点，不分主从！
			NodeSet set = mapSpaces.get(space);
			if (set != null && set.size() > 0) {
				NodeSet copy = new NodeSet(set);
				// 2. 获得与表关联的全部DATA主站点
				set = mapMasters.get(space);
				// 3. 删除其中的主站点，剩下的是从站点地址
				if (set != null) {
					copy.removeAll(set);
				}
				// 根据数据表名和站点地址，取得关联的数据块分区
				return doStubSector(space, copy.list());
			}
		} catch (Throwable e) {
			throw new TaskException(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}


	/**
	 * 根据数据表名获得数据块分区。不区分主/从数据块，每个数据块都获得平均的调用。
	 * @param space 数据表名
	 * @return 返回StubSector集合，没有是空指针
	 */
	public List<StubSector> doStubSector(Space space) throws TaskException {
		// 获得全部数据块
		super.lockMulti();
		try {
			// 1. 找到关联节点
			NodeSet set = mapSpaces.get(space);
			// 2. 生成分区
			if (set != null && set.size() > 0) {
				return doStubSector(space, set.list());
			}
		} catch (Throwable e) {
			throw new TaskException(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}


	/**
	 * 查找数据块关联的节点
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 节点集合
	 */
	public NodeSet findScanStubSites(Space space, long stub) {
		NodeSet array = new NodeSet();

		// 锁定
		super.lockMulti();
		try {
			// 根据数据块编号，查站点集合
			NodeSet set = mapStubs.get(stub);
			// 确认一个数据块在每一个站点存在
			if (set != null && set.size() > 0) {
				for (Node node : set.list()) {
					PushDataField field = mapFields.get(node);
					StubTable table = field.find(space);
					if (table != null && table.contains(stub)) {
						array.add(node);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return array;
	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.establish.seeker.ScanSeeker#isMaster(com.laxcus.util.Siger, com.laxcus.site.Node)
	//	 */
	//	@Override
	//	public boolean isMaster(Siger issuer, Node node) throws TaskException {
	//		// 检查账号有效
	//		allow(issuer);
	//		// 取实例
	//		PushDataField field = this.findField(node);
	//		return (field != null && field.isMaster());
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.establish.seeker.ScanSeeker#isSlave(com.laxcus.util.Siger, com.laxcus.site.Node)
	//	 */
	//	@Override
	//	public boolean isSlave(Siger issuer, Node node) throws TaskException {
	//		// 检查账号有效
	//		allow(issuer);
	//		// 取实例
	//		PushDataField field = this.findField(node);
	//		return (field != null && field.isSlave());
	//	}

	//	/**
	//	 * 建立一个默认列索引区域
	//	 * @param attribute 列属性
	//	 * @return 返回IndexZone子类实例
	//	 * @throws TaskException
	//	 */
	//	private IndexZone createDefaultIndexZone(ColumnAttribute attribute) throws TaskException {
	//		IndexZone zone = IndexZoneCreator.createDefault(attribute.getType());
	//		if (zone == null) {
	//			throw new TaskException("illegal family: %d", attribute.getType());
	//		}
	//		return zone;
	//	}

	//	/**
	//	 * 根据列数据类型建立一个列索引范围平衡分割器
	//	 * @param attribute 列属性
	//	 * @return 返回关联的列索引范围平衡分割器
	//	 * @throws TaskException
	//	 */
	//	private IndexBalancer createBalancer(ColumnAttribute attribute) throws TaskException {
	//		IndexBalancer balancer = IndexBalancerCreator.create(attribute);
	//		if (balancer == null) {
	//			throw new TaskException("illegal attribute: %d", attribute.getType());
	//		}
	//		return balancer;
	//	}


	/**
	 * 查询数据块编号
	 * @param stub
	 * @return
	 */
	public NodeSet findStubSites(java.lang.Long stub) {
		// 查询子站点
		super.lockMulti();
		try {
			return mapStubs.get(stub);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据数据块编号，查询关联的从站点
	 * @param stub 数据块编号
	 * @return 返回站点地址列表
	 */
	public List<Node> findSlaveSites(java.lang.Long stub) {
		TreeSet<Node> array = new TreeSet<Node>();
		// 查询子站点
		super.lockMulti();
		try {
			NodeSet set = mapStubs.get(stub);
			if (set != null && set.size() > 0) {
				for (Node node : set.list()) {
					PushDataField field = mapFields.get(node);
					if (field != null && field.isSlave()) {
						array.add(node);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return new ArrayList<Node>(array);
	}

	/**
	 * 根据数据块编号，查询关联的主
	 * @param stub 数据块编号
	 * @return 返回站点地址列表
	 */
	public List<Node> findPrimeSites(java.lang.Long stub) {
		TreeSet<Node> array = new TreeSet<Node>();
		// 查询子站点
		super.lockMulti();
		try {
			NodeSet set = mapStubs.get(stub);
			if (set != null) {
				for (Node node : set.list()) {
					PushDataField field = mapFields.get(node);
					if (field != null && field.isMaster()) {
						array.add(node);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return new ArrayList<Node>(array);
	}

	/**
	 * 根据数据表名，查询关联的DATA从站点
	 * @param space 数据表名
	 * @return 返回站点地址列表
	 */
	public List<Node> findSlaveSites(Space space) {
		TreeSet<Node> array = new TreeSet<Node>();
		// 根据数据表名查询子站点
		super.lockMulti();
		try {
			NodeSet set = mapSpaces.get(space);
			if (set != null) {
				for (Node node : set.list()) {
					PushDataField field = mapFields.get(node);
					if (field != null && field.isSlave()) {
						array.add(node);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		// 输出全部
		return new ArrayList<Node>(array);
	}

	/**
	 * 根据数据表名和数据块编号，查询关联的DATA从站点
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回Node集合（从站点）
	 */
	public List<Node> findSlaveSites(Space space, java.lang.Long stub) {
		List<Node> s1 = findSlaveSites(space);
		List<Node> s2 = findSlaveSites(stub);

//		Logger.debug(this, "findSlaveSites", "%s size is %d, %x size is %d",
//			space, s1.size(), stub, s2.size());

		// 保留相同值
		s1.retainAll(s2);
		return s1;
	}

	/**
	 * 根据数据表名和数据块编号，查询关联的DATA主站点
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回Node集合（主站点）
	 */
	public List<Node> findPrimeSites(Space space, java.lang.Long stub) {
		NodeSet set = findPrimeTableSites(space);

		ArrayList<Node> s1 = new ArrayList<Node>(set.list());
		List<Node> s2 = findPrimeSites(stub);

		//		Logger.debug(this, "findPrimeSites", "check:%s - 0x%X, space site size:%d, stub site size:%d", 
		//				space, stub, s1.size(), s2.size());

		// 保留相同值
		s1.retainAll(s2);

		//		Logger.debug(this, "findPrimeSites", "%s prime site size:%d", space, s1.size());

		return s1;
	}
	
	/**
	 * 根据数据表名，产生一批备份站点地址
	 * @param space 数据表名
	 * @return 站点列表
	 */
	public List<Node> createReflexCacheSite(Space space) {
		TreeSet<Node> array = new TreeSet<Node>();

		// 锁定，找到所有相关的从节点。
		super.lockMulti();
		try {
			NodeSet set = mapSpaces.get(space);
			if (set != null) {
				for (Node node : set.list()) {
					PushDataField field = mapFields.get(node);
					if (field != null && field.isSlave()) {
						array.add(node);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(findOwner(space), this, "createReflexCacheSite", "%s slave sites %d", space, array.size());

		return new ArrayList<Node>(array);
	}


//	/**
//	 * 根据数据表名，产生一批备份站点地址
//	 * @param space 数据表名
//	 * @return 站点列表
//	 */
//	public List<Node> createReflexCacheSite(Space space) {
//		ArrayList<Node> array = new ArrayList<Node>();
//
//		Table table = StaffOnCallPool.getInstance().findTable(space);
//		if (table == null) {
//			return array;
//		}
//
//		// 分配给从站点的数目
//		int left = table.getChunkCopy() - 1;
//		if (left < 1) {
//			return array;
//		}
//
//		// 锁定
//		super.lockMulti();
//		try {
//			NodeSet set = mapSpaces.get(space);
//			if (set != null && set.size() > 0) {
//				for (Node node : set.list()) {
//					PushDataField field = mapFields.get(node);
//					if (field.isSlave()) {
//						array.add(node);
//						if (array.size() == left) break; // 达到要求，退出
//					}
//				}
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockMulti();
//		}
//
//		Logger.debug(findOwner(space), this, "createReflexCacheSite", "%s slave site %d", space, array.size());
//
//		return array;
//	}

	//	/**
	//	 * 判断用户签名和数据表名有效
	//	 * @param issuer 用户签名
	//	 * @param space 数据表名
	//	 * @throws TaskSecurityException - 无效弹出异常
	//	 */
	//	public void allow(Siger issuer, Space space) throws TaskSecurityException {
	//		// 判断用户签名和数据表名有效且匹配
	//		boolean success = StaffOnCallPool.getInstance().allow(issuer, space);
	//		if (!success) {
	//			throw new TaskSecurityException("security denied \"%s#%s\"", issuer, space);
	//		}
	//	}
	//
	//	/**
	//	 * 判断用户签名有效
	//	 * @param issuer 用户签名
	//	 * @throws TaskSecurityException - 无效弹出异常
	//	 */
	//	public void allow(Siger issuer) throws TaskSecurityException {
	//		boolean success = StaffOnCallPool.getInstance().allow(issuer);
	//		if (!success) {
	//			throw new TaskSecurityException("security denied \"%s\"", issuer);
	//		}
	//	}


	//	/**
	//	 * 根据数据表名获得主数据块分区
	//	 * @param space 数据表名
	//	 * @return StubSector集合
	//	 * @throws TaskException
	//	 */
	//	protected List<StubSector> doPrimeStubSector2(Space space) throws TaskException {
	//		// 获得全部数据块
	//		NodeSet set = mapMasters.get(space);
	//		if (set == null) {
	//			throw new TaskException("cannot be find '%s'", space);
	//		}
	//		return doStubSector(space, set);
	//	}

	//	/**
	//	 * 根据数据表名获得从数据块分区
	//	 * @param space 数据表名
	//	 * @return StubSector集合
	//	 * @throws TaskException
	//	 */
	//	protected List<StubSector> doSlaveStubSector(Space space) throws TaskException {
	//		// 查找全部站点
	//		NodeSet next = mapSpaces.get(space);
	//		if (next == null) {
	//			throw new TaskException("cannot be find '%s'", space);
	//		}
	//		NodeSet set = new NodeSet(next);
	//		// 获得主站点
	//		next = mapMasters.get(space);
	//		// 删除其中的主站点，剩下的是从站点地址
	//		if (next != null) {
	//			set.removeAll(next);
	//		}
	//		// 根据数据表名和站点地址，取得关联的数据块分区
	//		return doStubSector(space, set);
	//	}

	//	/**
	//	 * 根据数据表名获得数据块分区。不区分主/从数据块，每个数据块都获得平均的调用。
	//	 * @param space 数据表名
	//	 * @return StubSector集合
	//	 * @throws TaskException
	//	 */
	//	protected List<StubSector> doStubSector(Space space) throws TaskException {
	//		// 获得全部数据块
	//		NodeSet set = mapSpaces.get(space);
	//		if (set == null) {
	//			throw new TaskException("cannot be find '%s'", space);
	//		}
	//		return doStubSector(space, set);
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.seeker.FromSeeker#createPrimeStubSector(com.laxcus.util.Siger, com.laxcus.access.schema.Space)
	//	 */
	//	@Override
	//	public List<StubSector> createPrimeStubSector(Siger siger, Space space) throws TaskException {
	//		// 判断用户签名和表被接受
	//		allow(siger, space);
	//		// 找主站点
	//		super.lockMulti();
	//		try {
	//			return doPrimeStubSector(space);
	//		} catch (TaskException e) {
	//			throw e;
	//		} catch (Throwable e) {
	//			throw new TaskException(e);
	//		} finally {
	//			super.unlockMulti();
	//		}
	//	}
	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.seeker.FromSeeker#createSlaveStubSector(com.laxcus.util.Siger, com.laxcus.access.schema.Space)
	//	 */
	//	@Override
	//	public List<StubSector> createSlaveStubSector(Siger siger, Space space) throws TaskException {
	//		// 判断用户签名和表被接受
	//		allow(siger, space);
	//		// 从站点分区
	//		super.lockMulti();
	//		try {
	//			return doSlaveStubSector(space);
	//		} catch (TaskException e) {
	//			throw e;
	//		} catch (Throwable e) {
	//			throw new TaskException(e);
	//		} finally {
	//			super.unlockMulti();
	//		}
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.seeker.FromSeeker#createStubSector(com.laxcus.util.Siger, com.laxcus.access.schema.Space)
	//	 */
	//	@Override
	//	public List<StubSector> createStubSector(Siger siger, Space space) throws TaskException {
	//		// 判断用户签名和表被接受
	//		allow(siger, space);
	//
	//		super.lockMulti();
	//		try {
	//			return doStubSector(space);
	//		} catch (TaskException e) {
	//			throw e;
	//		} catch (Throwable e) {
	//			throw new TaskException(e);
	//		} finally {
	//			super.unlockMulti();
	//		}
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.seeker.FromSeeker#createIndexSector(com.laxcus.util.Siger, com.laxcus.access.schema.Dock, int)
	//	 */
	//	@Override
	//	public IndexSector createIndexSector(Siger issuer, Dock dock, int sites) throws TaskException {
	//		// 判断获得许可		
	//		Space space = dock.getSpace();
	//		allow(issuer, space);
	//
	//		// 找表
	//		Table table = StaffOnCallPool.getInstance().findTable(space);
	//		if (table == null) {
	//			throw new TaskException("cannot be find '%s'", space);
	//		}
	//		ColumnAttribute attribute = table.find(dock.getColumnId());
	//		if(attribute == null) {
	//			throw new TaskException("cannot be find '%s'", dock);
	//		}
	//		// 找主站点
	//		NodeSet set = findPrimeTableSites(space);
	//		if (set == null || set.isEmpty()) {
	//			throw new TaskException("cannot be find '%s'", dock);
	//		}
	//
	//		// 通过网络检索索引分区
	//		List<IndexZone> zones = findIndexZone(set, dock);
	//		// 根据列属性建立索引平衡处理器
	//		IndexBalancer balancer = createBalancer(attribute);
	//
	//		// 如果是空集合，生成一个默认的索引分区，否则它们
	//		if (zones.isEmpty()) {
	//			IndexZone e = createDefaultIndexZone(attribute);
	//			balancer.add(e);
	//		} else {
	//			for (IndexZone e : zones) {
	//				balancer.add(e);
	//			}
	//		}
	//
	//		// 输出分区
	//		IndexSector sector = balancer.balance(sites);
	//		// 设置列空间
	//		if (sector != null) {
	//			sector.setDock(dock);
	//		}
	//		return sector;
	//	}

	//	/**
	//	 * 通过网络查询分布的索引分区。通过admit方法和命令钩子进行。采用admit方法是让机器能够调控载荷。
	//	 * @param hubs 服务器地址
	//	 * @param dock 列空间
	//	 * @return 返回索引分区列表
	//	 * @throws TaskException
	//	 */
	//	private List<IndexZone> findIndexZone(NodeSet hubs, Dock dock) throws TaskException {
	//		// 命令和钩子
	//		FindIndexZone cmd =	new FindIndexZone(dock);
	//		IndexZoneHook hook = new IndexZoneHook();
	//		// 转发命令
	//		ShiftFindIndexZone shift = new ShiftFindIndexZone(cmd, hook);
	//		shift.setHubs(hubs.show()); // 设置目标地址
	//
	//		// 直接放入命令队列
	//		boolean success = CallCommandPool.getInstance().admit(shift);
	//		if (!success) {
	//			hook.done(); // 释放等待
	//			throw new TaskException("command denied");
	//		}
	//		// 进入等待，直到完成
	//		hook.await();
	//
	//		return hook.list();
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.seeker.FromSeeker#findFromTable(com.laxcus.util.Siger, com.laxcus.access.schema.Space)
	//	 */
	//	@Override
	//	public Table findFromTable(Siger siger, Space space) throws TaskException {
	//		// 判断用户签名和数据表名有效且匹配
	//		allow(siger, space);
	//
	//		Table table = StaffOnCallPool.getInstance().findTable(space);
	//		if (table == null) {
	//			throw new TaskException("cannot be find '%s'", space);
	//		}
	//		return table;
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.establish.seeker.ScanSeeker#findScanTable(com.laxcus.util.Siger, com.laxcus.access.schema.Space)
	//	 */
	//	@Override
	//	public Table findScanTable(Siger siger, Space space) throws TaskException{
	//		// 判断用户签名和表被接受
	//		allow(siger, space);
	//
	//		Table table = StaffOnCallPool.getInstance().findTable(space);
	//		if (table == null) {
	//			throw new TaskException("cannot be find '%s'", space);
	//		}
	//		return table;
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.TaskHelper#isSystemLevel(com.laxcus.util.naming.Phase)
	//	 */
	//	@Override
	//	public boolean isSystemLevel(Phase phase) {
	//		int family = phase.getFamily();
	//		if (PhaseTag.isConduct(family)) {
	//			return InitTaskPool.getInstance().isSystemLevel(phase.getRoot());
	//		} else if (PhaseTag.isEstablish(family)) {
	//			return IssueTaskPool.getInstance().isSystemLevel(phase.getRoot());
	//		}
	//		return false;
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.TaskHelper#isUserLevel(com.laxcus.util.naming.Phase)
	//	 */
	//	@Override
	//	public boolean isUserLevel(Phase phase) {
	//		int family = phase.getFamily();
	//		if (PhaseTag.isConduct(family)) {
	//			return InitTaskPool.getInstance().isUserLevel(phase.getRoot());
	//		} else if (PhaseTag.isEstablish(family)) {
	//			return IssueTaskPool.getInstance().isUserLevel(phase.getRoot());
	//		}
	//		return false;
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.seeker.FromSeeker#findFromSites(com.laxcus.util.naming.Phase)
	//	 */
	//	@Override
	//	public NodeSet findFromSites(Phase from) throws TaskException {
	//		// 如果是系统级阶段命名，返回全部站点。每个DATA主机默认保存全部“系统级”FROM阶段分布组件
	//		if(isSystemLevel(from)) {
	//			return this.sites;
	//		}
	//
	//		// 是用户级组件，进行安全检查，判断签名一致
	//		Phase init = new Phase(from.getIssuer(), PhaseTag.INIT, from.getRoot());
	//		boolean success = InitTaskPool.getInstance().contains(init);
	//		if (!success) {
	//			throw new TaskSecurityException("refuse %s", from);
	//		}
	//		// 查询FROM站点地址
	//		return super.findSites(from);
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.seeker.FromSeeker#findFromSites(com.laxcus.util.Siger, com.laxcus.access.schema.Space)
	//	 */
	//	@Override
	//	public NodeSet findFromSites(Siger siger, Space space) throws TaskException {
	//		// 判断用户签名和数据表名有效且匹配
	//		allow(siger, space);
	//
	//		return findTableSites(space);
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.seeker.FromSeeker#findFromSites(com.laxcus.util.naming.Phase, com.laxcus.util.Siger, com.laxcus.access.schema.Space)
	//	 */
	//	@Override
	//	public NodeSet findFromSites(Phase from, Siger issuer, Space space) throws TaskException {
	//		NodeSet set = new NodeSet();
	//		// 查找阶段命名
	//		NodeSet next = findFromSites(from);
	//		if (next == null) {
	//			return set;
	//		}
	//		// 保存全部
	//		set.addAll(next.show());
	//
	//		// 查找数据表名
	//		next = findFromSites(issuer, space);
	//		if (next == null) {
	//			set.clear();
	//		} else {
	//			set.AND(new TreeSet<Node>(next.show()));
	//		}
	//
	//		return set;
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.establish.seeker.ScanSeeker#findScanSites(com.laxcus.util.Siger, com.laxcus.access.schema.Space)
	//	 */
	//	@Override
	//	public NodeSet findScanSites(Siger issuer, Space space) throws TaskException {
	//		// 判断用户签名和数据表名有效且匹配
	//		allow(issuer, space);
	//		// 查找DATA主站点
	//		return findPrimeTableSites(space);
	//	}

	

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.establish.seeker.ScanSeeker#findScanSites(com.laxcus.util.naming.Phase)
	//	 */
	//	@Override
	//	public NodeSet findScanSites(Phase phase) throws TaskException {
	//		// 是系统级，返回
	//		if (isSystemLevel(phase)) {
	//			return this.clone(sites);
	//		}
	//
	//		Phase issue = new Phase(phase.getIssuer(), PhaseTag.ISSUE, phase.getRoot());
	//		boolean success = IssueTaskPool.getInstance().contains(issue);
	//		if (!success) {
	//			throw new TaskSecurityException("refuse %s", phase);
	//		}
	//
	//		return super.findSites(phase);
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.establish.seeker.ScanSeeker#findStubSites(com.laxcus.util.Siger, com.laxcus.access.schema.Space, long)
	//	 */
	//	@Override
	//	public NodeSet findStubSites(Siger issuer, Space space, long stub) throws TaskException {
	//		// 判断用户签名和数据表名有效且匹配
	//		allow(issuer, space);
	//
	//		NodeSet res = new NodeSet();
	//
	//		super.lockMulti();
	//		try {
	//			// 根据数据块编号，查站点集合
	//			NodeSet set = mapStubs.get(stub);
	//			if (set == null || set.isEmpty()) {
	//				return null;
	//			}
	//			// 确认一个数据块在每一个站点存在
	//			for (Node node : set.list()) {
	//				PushDataField field = mapFields.get(node);
	//				StubTable table = field.find(space);
	//				if (table == null) {
	//					continue;
	//				}
	//				if (table.contains(stub)) {
	//					res.add(node);
	//				}
	//			}
	//		} catch (Throwable e) {
	//			throw new TaskException(e);
	//		} finally {
	//			super.unlockMulti();
	//		}
	//
	//		return res;
	//	}

	//	/**
	//	 * 根据站点地址，查找它的站点数据
	//	 * @param node 节点
	//	 * @return 
	//	 */
	//	private PushDataField findField(Node node) {
	//		Laxkit.nullabled(node);
	//		super.lockMulti();
	//		try {
	//			return mapFields.get(node);
	//		} finally {
	//			super.unlockMulti();
	//		}
	//	}


	//	public void test() throws Exception {
	//		Space space = new Space("Media", "Music");
	//		// 第一组
	//		Node node1 = new Node("DATA://127.0.0.1:8500_8500");
	//		PushDataField field1 = new PushDataField(com.laxcus.site.rank.RankTag.PRIME_SITE, node1);
	//		StubTable table1 = new StubTable(space);
	//		for(int i = 0; i < 3; i++) {
	//			table1.add(Long.MAX_VALUE - i);
	//		}
	//		field1.add(table1);
	//
	//		// 第二组
	//		Node node2 = new Node("DATA://127.0.0.1:8510_8510");
	//		PushDataField field2 = new PushDataField(com.laxcus.site.rank.RankTag.SLAVE_SITE, node2);
	//		StubTable table2 = new StubTable(space);
	//		for(int i = 0; i < 3; i++) {
	//			table2.add(Long.MAX_VALUE - i);
	//		}
	//		field2.add(table2);
	//
	//		// 保存
	//		this.push(field1);
	//		this.push(field2);
	//
	//		System.out.printf("site size is %d\n", mapFields.size());
	//
	//		//		this.drop(node2);
	//
	//		List<StubSector> list = doPrimeStubSector(space);
	////		doPrimeStubSector(space);
	////		List<StubSector> list = doSlaveStubSector(space);
	//		System.out.printf("size is %d\n",  list.size());
	//
	//		for (StubSector e : list) {
	//			System.out.printf("site is %s, space is %s\n", e.getNode(), e.getSpace());
	//			for (long stub : e.list()) {
	//				System.out.printf("stub is %x\n", stub);
	//			}
	//		}
	//	}
	//
	//
	//	public static void main(String[] args) {
	//		try {
	//			DataOnCallPool.getInstance().test();
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//	}

}