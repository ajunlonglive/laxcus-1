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
import com.laxcus.command.stub.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.Node;
import com.laxcus.util.set.*;

/**
 * 缓存映像数据块管理池 <br>
 * 
 * 在“DATA从站点”中，保存着一批从DATA主站点备份的缓存数据。这些数据信息被保存在关联的CALL站点上，随时提供检索。
 * 
 * @author scott.liang
 * @version 1.0 12/12/2014
 * @since laxcus 1.0
 */
public final class CacheReflexStubOnCallPool extends VirtualPool {

	/** 实例句柄 **/
	private static CacheReflexStubOnCallPool selfHandle = new CacheReflexStubOnCallPool();

	/** 数据块编号 -> 站点集合 **/
	private Map<Long, SpotSet> mapStubs = new TreeMap<Long, SpotSet>();

	/** 站点地址 -> 数据命令 **/
	private Map<Spot, CacheReflexStub> mapSites = new TreeMap<Spot, CacheReflexStub>();

	/**
	 * 构造一个映像数据块存储池
	 */
	private CacheReflexStubOnCallPool() {
		super();
		setSleepTimeMillis(60000);
	}

	/**
	 * 返回映像数据块存储池静态句柄
	 * @return
	 */
	public static CacheReflexStubOnCallPool getInstance() {
		return CacheReflexStubOnCallPool.selfHandle;
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
			super.sleep();
		}
		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		this.mapSites.clear();
		this.mapStubs.clear();
	}
	
	/**
	 * 查找匹配的站点
	 * @param space
	 * @param stub
	 * @return
	 */
	public List<Node> find(Space space, long stub) {
		ArrayList<Node> array = new ArrayList<Node>();

		super.lockMulti();
		try {
			SpotSet set = mapStubs.get(stub);
			if (set != null) {
				for (Spot spot : set.list()) {
					CacheReflexStub item = mapSites.get(spot);
					if (item.contains(space, stub)) {
						array.add(spot.getSite());
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

	/**
	 * 删除一个表基点下的全部映像缓存块
	 * @param spot - 表基点（DATA站点）
	 * @return - 删除成功返回“真”，否则“假”。
	 */
	public boolean remove(Spot spot) {
		boolean success = false;

		super.lockSingle();
		try {
			CacheReflexStub item = mapSites.remove(spot);
			success = (item != null);
			// 删除全部数据块
			if (success) {
				List<Long> list = item.getStubs();
				for (Long stub : list) {
					SpotSet set = mapStubs.get(stub);
					if (set != null) {
						set.remove(spot);
						if (set.isEmpty()) mapStubs.remove(stub);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "remove", success, "from %s", spot);

		return success;
	}
	
	/**
	 * 删除一个站点下的全部记录
	 * @param node - DATA站点
	 * @return - 返回被删除的表基点数目
	 */
	public int remove(Node node) {
		ArrayList<Spot> array = new ArrayList<Spot>();

		super.lockMulti();
		try {
			for (Spot spot : mapSites.keySet()) {
				Node that = spot.getSite();
				if (node.compareTo(that) == 0) {
					array.add(spot);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		for (Spot spot : array) {
			this.remove(spot);
		}

		return array.size();
	}

	/**
	 * 刷新映像缓存记录
	 * @param item 
	 */
	public boolean refresh(CacheReflexStub item) {
		Spot spot = item.getSpot();
		// 删除旧记录
		this.remove(spot);
		// 没有数据块不保存
		if (item.isEmpty()) {
			Logger.debug(this, "refresh", "%s is empty set", spot);
			return true;
		}

		// 保存新记录
		boolean success = false;
		super.lockSingle();
		try {
			// 保存全部参数
			success = (mapSites.put(spot, item) == null);
			// 保存每个数据块
			if (success) {
				List<Long> list = item.getStubs();
				for (Long stub : list) {
					SpotSet set = mapStubs.get(stub);
					if (set == null) {
						set = new SpotSet();
						mapStubs.put(stub, set);
					}
					set.add(spot);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "refresh", success, "from %s", spot);

		return success;
	}

	/**
	 * 更新一批数据
	 * @param list
	 * @return
	 */
	public int refresh(List<CacheReflexStub> list) {
		int count = 0;
		for (CacheReflexStub item : list) {
			boolean success = this.refresh(item);
			if (success) count++;
		}
		return count;
	}

}
