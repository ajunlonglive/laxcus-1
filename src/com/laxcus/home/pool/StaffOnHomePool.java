/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.pool;

import java.io.*;
import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.refer.*;
import com.laxcus.command.reserve.*;
import com.laxcus.home.*;
import com.laxcus.home.util.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.pool.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.site.*;

/**
 * HOME站点资源管理池。<br>
 * 
 * 磁盘上只保存账号的用户签名，账号参数（数据表，读写限制等）通过网络获取，以资源引用“Refer”的形式驻留在内存里。
 * 
 * @author scott.liang
 * @version 1.2 10/11/2013
 * @since laxcus 1.0
 */
public class StaffOnHomePool extends VirtualPool {

	/** 用户资源文件后缀 **/
	private final static String suffix = ".sketch";

	/** 资源管理池句柄 **/
	private static StaffOnHomePool selfHandle = new StaffOnHomePool();

	/** 用户签名文件数目 **/
	private int blocks;

	/** 模值 -> 签名域 **/
	private Map<java.lang.Integer, SignField> mapFields = new TreeMap<java.lang.Integer, SignField>();

	/** 用户账号 -> 用户资源引用 **/
	private Map<Siger, Refer> mapRefers = new TreeMap<Siger, Refer>();

	/** 数据表名 -> 全部关联表用户签名。包括一个表持有人和任意多个被授权人 **/
	private Map<Space, SigerSet> mapSpaces = new TreeMap<Space, SigerSet>();

	/** 数据表名 -> 表配置 **/
	private Map<Space, Table> mapTables = new TreeMap<Space, Table>();

	/**
	 * 构造HOME站点资源管理池
	 */
	private StaffOnHomePool() {
		super();
	}

	/**
	 * 返回HOME站点资源管理池句柄
	 * @return HOME站点资源管理池实例
	 */
	public static StaffOnHomePool getInstance() {
		return StaffOnHomePool.selfHandle;
	}

	/**
	 * 设置用户签名文件数目
	 * @param how 用户签名文件数目
	 */
	public void setBlocks(int how) {
		if (how < 1) {
			throw new IllegalValueException("must be > 0");
		}
		blocks = how;
	}

	/**
	 * 返回用户签名文件数目
	 * @return 文件数目
	 */
	public int getBlocks() {
		return blocks;
	}

	/**
	 * 根据用户签名，产生它对应的模值。模值是用户的写入区块文件下标。
	 * @param siger 用户签名
	 * @return 模值
	 */
	private int mod(Siger siger) {
		return siger.mod(blocks);
	}

	/**
	 * 删除全部参数
	 */
	private void clear() {
		mapSpaces.clear();
		mapRefers.clear();
		mapTables.clear();
		mapFields.clear();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 清除旧数据（这是一个冗余操作。在HOME监视站点切换到运行后，有垃圾数据）
		clear();

		//		// 加载配置目录下的账号资源图谱
		//		boolean success = loadSketches();
		//
		//		return success;

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		// 加载资源引用
		boolean success = loadRefers();
		
//		// 加载全部表
//		if (success) {
//			loadTables();
//		}

		// 成功重新注册，否则停止
		if (success) {
			getLauncher().checkin(true);
		} else {
			getLauncher().stop();
		}

		// 延时等待退出
		while (!isInterrupted()) {
			sleep();
		}

		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		clear();
	}

	/**
	 * 保存一个表和用户签名
	 * @param space 数据表名
	 * @param siger 用户签名
	 */
	private void addSpace(Space space, Siger siger) {
		SigerSet set = mapSpaces.get(space);
		if (set == null) {
			set = new SigerSet();
			mapSpaces.put(space, set);
		}
		set.add(siger);
	}

	/**
	 * 删除一个表和用户签名
	 * @param space 数据表名
	 * @param siger 用户签名
	 */
	private void removeSpace(Space space, Siger siger) {
		SigerSet set = mapSpaces.get(space);
		if (set != null) {
			set.remove(siger);
			if (set.isEmpty()) mapSpaces.remove(space);
		}
	}

//	/**
//	 * 在启动时，加载全部数据表
//	 */
//	private void loadTables() {
//		ArrayList<Space> array = new ArrayList<Space>(mapSpaces.keySet());
//		for (Space space : array) {
//			Table table = findTable(space);
//			boolean success = (table != null);
//			Logger.debug(this, "loadTables", success, "load %s", space);
//		}
//	}

	/**
	 * 返回账号集合
	 * @return Siger列表
	 */
	public List<Siger> getUsers() {
		super.lockMulti();
		try {
			return new ArrayList<Siger>(mapRefers.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 输出当前运行的全部用户资源引用
	 * @return Refer列表
	 */
	public List<Refer> getRefers() {
		super.lockMulti();
		try {
			return new ArrayList<Refer>(mapRefers.values());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断某个账号有效
	 * @param siger 账号签名
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean allow(Siger siger) {
		Laxkit.nullabled(siger);
		super.lockMulti();
		try {
			return (mapRefers.get(siger) != null);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断数据表名获得许可
	 * @param space 数据表名
	 * @return 许可返回“真”，否则“假”。
	 */
	public boolean allow(Space space) {
		Laxkit.nullabled(space);

		// 判断表存在
		super.lockMulti();
		try {
			return (mapSpaces.get(space) != null);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 修改注册账号密码
	 * @param user 新的账号
	 * @return 修改成功返回真，否则假。
	 */
	public boolean alter(User user) {
		boolean success = false;
		super.lockSingle();
		try {
			Refer refer = mapRefers.get(user.getUsername());
			success = (refer != null);
			if (success) {
				refer.getUser().setPassword(user.getPassword());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "alter", success, "change '%s'", user);

		return success;
	}

	/**
	 * 根据数据表名，查找用户资源引用。（注意：是表的持有人，而不是被授权人！！！）
	 * @param space 数据表名
	 * @return Refer实例
	 */
	public Refer find(Space space) {
		Laxkit.nullabled(space);

		super.lockMulti();
		try {
			SigerSet set = mapSpaces.get(space);
			if (set != null) {
				for (Siger siger : set.list()) {
					Refer refer = mapRefers.get(siger);
					// 判断是表的持有人
					if (refer.hasTable(space)) {
						return refer;
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return null;
	}

	/**
	 * 根据用户签名查找用户资源引用
	 * @param siger 用户数字签名
	 * @return Refer实例
	 */
	public Refer find(Siger siger) {
		Laxkit.nullabled(siger);

		super.lockMulti();
		try {
			return mapRefers.get(siger);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据用户签名判断用户资源引用存在
	 * @param siger 用户签名
	 * @return 返回是或者否
	 */
	public boolean contains(Siger siger) {
		return find(siger) != null;
	}

	/**
	 * 根据用户签名删除他的用户资源引用。发生在用户账号被删除时
	 * @param siger 用户签名
	 * @return 成功返回“真”，否则“假”。
	 * @throws NullPointerException，如果用户签名是空值时 
	 */
	public boolean drop(Siger siger) {
		Laxkit.nullabled(siger);

		boolean success = false;
		super.lockSingle();
		try {
			success = __drop(siger, true);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "drop", success, "drop %s", siger);

		return success;
	}

	/**
	 * 根据用户签名删除用户资源引用
	 * @param siger 被删除的账号名称
	 * @param write 更新后的参数写入本地配置文件
	 * @return 成功返回真，否则假
	 */
	private boolean __drop(Siger siger, boolean write) {
		int mod = mod(siger);

		// 删除资源引用
		Refer refer = mapRefers.remove(siger);
		boolean success = (refer != null);
		// 成功，删除它们
		if (success) {
			//  从分组中删除签名
			SignField field = mapFields.get(mod);
			if(field != null) {
				field.remove(siger);
			}
			// 删除专属表名和表配置
			for (Space space : refer.getTables()) {
				removeSpace(space, siger); // 表持有人
				mapTables.remove(space);
			}
			// 删除共享表名和表配置
			for (Space space : refer.getPassiveTables()) {
				removeSpace(space, siger); // 表的被授权人
				mapTables.remove(space);
			}

			// 更新配置文件，在外部锁定状态下同步写入
			if (write) {
				success = writeSigers(mod);
			}
		}

		return success;
	}

	/**
	 * 建立用户资源。在保存之前，先删除旧的配置
	 * @param refer 用户账号
	 * @param write 参数写入本地配置文件
	 */
	private void __create(Refer refer, boolean write) {
		Laxkit.nullabled(refer);

		// 删除用户资源引用
		Siger siger = refer.getUsername();

		// 取这个签名的模值
		int mod = mod(siger);

		// 保存到账号
		mapRefers.put(siger, refer);

		// 保存账号持有人的自有表和被授权表
		for (Space space : refer.getTables()) {
			addSpace(space, siger); // 表持有人
		}
		for (Space space : refer.getPassiveTables()) {
			addSpace(space, siger); // 表的被授权人
		}

		// 将签名保存到分组
		SignField field = mapFields.get(mod);
		if (field == null) {
			field = new SignField(mod);
			mapFields.put(mod, field);
		}
		field.add(siger);

		// 如果要求写入本地配置文件时...
		if (write) {
			writeSigers(mod);
		}
	}

	/**
	 * 建立用户资源引用。在保存之前，先删除旧的用户资源引用
	 * @param refer 用户资源引用
	 * @param write 写入磁盘
	 * @return 保存成功返回“真”，否则“假”。
	 */
	public boolean create(Refer refer, boolean write) {
		// 写入记录
		boolean success = false;
		super.lockSingle();
		try {
			// 删除旧记录，如果存在的话
			__drop(refer.getUsername(), false);
			// 建立资源引用
			__create(refer, write);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "create", success, "create '%s'", refer.getUsername());

		return success;
	}

	/**
	 * 建立用户资源引用。在保存之前，先删除旧的用户资源引用
	 * @param refer 用户资源引用
	 * @return 保存成功返回“真”，否则“假”。
	 */
	public boolean create(Refer refer) {
		return create(refer, true);
	}

	/**
	 * 强制建立数据表 <br>
	 * 这个方法只提供给“HomeAwardCreateTableInvoker”使用
	 * 
	 * @param table 表配置实例
	 * @return 保存成功返回“真”，否则“假”。
	 */
	public boolean createTable(Table table) {
		Laxkit.nullabled(table);

		boolean success = false;

		Siger siger = table.getIssuer();
		//		int mod = mod(siger);

		Space space = table.getSpace();
		super.lockSingle();
		try {
			// 1. 资源引用必须存在
			Refer refer = mapRefers.get(siger);
			success = (refer != null);
			// 2. 保存表配置
			if (success) {
				// 记录表名
				refer.addTable(space);
				// 记录表执有人
				addSpace(space, refer.getUsername()); 
				// 保存表实例
				mapTables.put(space, table);

				//				// 配置写入到本地文件
				//				success = writeSketches(mod);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.note(this, "createTable", success, "create '%s - %s'", siger, space);

		return success;
	}
	
	/**
	 * 删除数据库及关联表
	 * @param fame 数据库名
	 * @return 返回删除记录
	 */
	public int dropSchema(Fame fame) {
		Laxkit.nullabled(fame);

		ArrayList<Space> array = new ArrayList<Space>();

		// 锁定!
		super.lockSingle();
		try {
			for (Space space : mapSpaces.keySet()) {
				if (fame.compareTo(space.getSchema()) == 0) {
					array.add(space);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 逐个删除
		int count = 0;
		for (Space space : array) {
			boolean success = dropTable(space);
			if (success) {
				count++;
			}
		}

		Logger.note(this, "dropSchema", (count > 0), "drop '%s', count %d",
				fame, count);

		return count;
	}

	/**
	 * 删除表。这个方法被“HomeAwardDropTableInvoker”调用。<br>
	 * 删除范围包括：1.表的持有人。2.表的被授权人。<br>
	 * 
	 * @param space 数据表名
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean dropTable(Space space) {
		Laxkit.nullabled(space);

		// 更新的单元
		ArrayList<java.lang.Integer> mods = new ArrayList<java.lang.Integer>();
		boolean success = false;

		// 锁定
		super.lockSingle();
		try {
			//			// 1. 删除表
			//			Table table = mapTables.remove(space);
			//			// 2. 删除记录
			//			if (table != null) {
			//				Refer refer = mapSpaces.remove(space);
			//				success = (refer != null);
			//				// 账号存在，删除表名
			//				if (success) {
			//					success = refer.removeTable(space);
			//				}
			//				// 删表成功，取模值
			//				if (success) {
			//					Siger siger = refer.getUsername();
			//					mod = mod(siger);
			//				}
			//			}

			// 删除数据表关联人，包括持有人和被授权人
			SigerSet set = mapSpaces.remove(space);
			// 判断成功且存在
			success = (set != null);
			if (success) {
				mapTables.remove(space);
				for (Siger siger : set.list()) {
					Refer refer = mapRefers.get(siger);
					if (refer == null) {
						continue;
					}

					// 从持有人和被授权人资源引用中删除表
					if (refer.hasTable(space)) {
						refer.removeTable(space); // 表持有人
					}
					if (refer.hasPassiveTable(space)) {
						refer.removePassiveItem(space); // 表被授权人
					}
					// 记录模值
					int mod = mod(siger);
					mods.add(mod);
				}

				//				// 更新本地配置文件
				//				for (int mod : mods) {
				//					writeSketches(mod);
				//				}

			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		//		// 成功，更新本地配置文件
		//		if (success) {
		//			for (int mod : mods) {
		//				writeSketches(mod);
		//			}
		//		}

		Logger.note(this, "dropTable", success, "drop '%s'", space);

		return success;
	}

	/**
	 * 查找表配置
	 * @param space 数据表名 
	 * @return 数据表实例
	 */
	public Table findTable(Space space) {
		Laxkit.nullabled(space);

		// 检查集合中已经注册（专属表或者共享表皆可）
		if (!allow(space)) {
			Logger.error(this, "findTable", "refuse %s", space);
			return null;
		}

		Table table = null;
		super.lockMulti();
		try {
			table = mapTables.get(space);
		} finally {
			super.unlockMulti();
		}
		if (table != null) {
			return table;
		}

		// 去TOP站点获取表
		table = searchTable(space);
		// 表有效
		boolean success = (table != null);
		if (success) {
			super.lockSingle();
			try {
				mapTables.put(table.getSpace(), table);
			} finally {
				super.unlockSingle();
			}
		}

		return (success ? table : null);
	}

	/**
	 * 判断有被分享表
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean hasPasstiveTable(Space space) {
		super.lockMulti();
		try {
			Iterator<Map.Entry<Siger, Refer>> iterator = mapRefers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, Refer> entry = iterator.next();
				if (entry.getValue().hasPassiveTable(space)) {
					return true;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return false;
	}

	/**
	 * 向被授权人账号中增加被授权单元
	 * @param conferrer 被授与人签名
	 * @param items 被授权单元列表
	 * @return 成功返回真，否则假
	 */
	public boolean addPassiveItems(Siger conferrer, List<PassiveItem> items) {
		// 判断获得授权许可
		if (!allow(conferrer)) {
			Logger.error(this, "addPassiveItems", "refuse '%s'", conferrer);
			return false;
		}

		boolean success = false;
		super.lockSingle();
		try {
			Refer refer = mapRefers.get(conferrer);
			// 给授与人增加关联表
			success = (refer != null) ;
			if (success) {
				refer.addPassiveItems(items);
				// 保存分享表（存在多个用户共享一个表的可能，只记录最后那个）
				for (PassiveItem e : items) {
					//					mapSpaces.put(e.getSpace(), refer);
					addSpace(e.getSpace(), conferrer);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "addPassiveItems", success, "save to '%s'", conferrer);

		return success;
	}

	/**
	 * 把被授权单元从被授权人账号中移除
	 * 
	 * @param conferrer 被授权人
	 * @param items 被授权列表
	 * @return 成功返回真，否则假
	 */
	public boolean removePassiveItems(Siger conferrer, List<PassiveItem> items) {
		// 判断获得授权许可
		if (!allow(conferrer)) {
			Logger.error(this, "removePassiveItems", "refuse '%s'", conferrer);
			return false;
		}

		boolean success = false;
		super.lockSingle();
		try {
			Refer refer = mapRefers.get(conferrer);
			// 从授与人账号中撤销分享表和本地表记录
			success = (refer != null);
			if (success) {
				// 删除被授权单元
				refer.removePassiveItems(items);
				// 删除记录
				for (PassiveItem e : items) {
					//					mapSpaces.remove(e.getSpace());
					removeSpace(e.getSpace(), conferrer);
					mapTables.remove(e.getSpace());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "removePassiveItems", success, "drop from '%s'", conferrer);

		return success;
	}

	/**
	 * 向授权人账号增加一批授权单元
	 * @param authorizer 授权人
	 * @param items 授权单元列表
	 * @return 成功返回真，否则假
	 */
	public boolean addActiveItems(Siger authorizer, List<ActiveItem> items) {
		// 判断获得授权许可
		if (!allow(authorizer)) {
			Logger.error(this, "addActiveItems", "refuse '%s'", authorizer);
			return false;
		}

		boolean success = false;
		super.lockSingle();
		try {
			Refer refer = mapRefers.get(authorizer);
			// 判断授权人有效
			success = (refer != null);
			// 保存被授权单元
			if (success) {
				refer.addActiveItems(items);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "addActiveItems", success, "save to '%s'", authorizer);

		return success;
	}

	/**
	 * 把授权单元从授权人账号中移除
	 * @param authorizer 授权人（数据表持有人）
	 * @param items 授权单元列表
	 * @return 成功返回真，否则假
	 */
	public boolean removeActiveItems(Siger authorizer, List<ActiveItem> items) {
		// 判断获得授权许可
		if (!allow(authorizer)) {
			Logger.error(this, "removeActiveItems", "refuse '%s'", authorizer);
			return false;
		}

		boolean success = false;
		super.lockSingle();
		try {
			Refer refer = mapRefers.get(authorizer);
			// 判断授权人有效
			success = (refer != null);
			// 删除授权单元
			if (success) {
				refer.removeActiveItems(items);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "removeActiveItems", success, "drop from '%s'", authorizer);

		return success;
	}

	/**
	 * 根据授权人签名，返回他的授权单元
	 * @param authorizer 授权人
	 * @return ActiveItem列表
	 */
	public List<ActiveItem> findActiveItems(Siger authorizer) {
		// 判断获得授权许可
		if (!allow(authorizer)) {
			Logger.error(this, "findPassiveItems", "refuse '%s'", authorizer);
			return null;
		}

		super.lockMulti();
		try {
			Refer refer = mapRefers.get(authorizer);
			// 增加授与人签名
			if (refer != null) {
				return refer.getActiveItems();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return null;
	}

	/**
	 * 加载本地资源引用。
	 * 这里存在故障账号的问题：即ACCOUNT节点上的账号已经删除，但是HOME集群因为某些故障，没有删除它，而一直保留着。
	 * 这种情况应提交给BANK站点去判断和处理
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean loadRefers() {
		// 读磁盘上的文件签名，取出全部签名
		List<Siger> array = readSigers();

		Logger.debug(this, "loadRefers", "member size:%d", array.size());

		// 空集合，退出
		if (array.isEmpty()) {
			return true;
		}

		// 获取全部ACCOUNT站点地址
		AccountOnCommonPool.getInstance().load(array);

		// 发生错误的账号签名
		ArrayList<Siger> faults = new ArrayList<Siger>();

		// 加载账号
		for (Siger siger : array) {
			boolean success = loadRefer(siger);
			// 不成功，记录它！
			if (!success) {
				faults.add(siger);
				Logger.error(this, "loadRefers", "cannot load refer:%s", siger);
			}
		}

		Logger.debug(this, "loadRefers", "successful refers:%d, fault refers:%d",
				array.size() - faults.size(), faults.size());

		// 把这些错误账号提交给TOP，转交给BANK节点处理。
		if (faults.size() > 0) {

		}

		// 判断成功
		return (array.size() - faults.size() > 0);
	}

	/**
	 * 从网络加载一个资源引用
	 * @param siger 账号签名
	 * @return 成功返回真，否则假
	 */
	public boolean loadRefer(Siger siger) {
		// 找到ACCOUNT站点地址
		List<Node> remotes = AccountOnCommonPool.getInstance().findSites(siger);
		Node remote = (remotes != null && remotes.size() > 0 ? remotes.get(0) : null);
		// 如果没有找到ACCOUNT站点，提供一个警告！命令将提交给TOP站点
		if (remote == null) {
			Logger.warning(this, "loadRefer", "cannot find account site by %s", siger);
		}

		// 生成命令，交给命令管理池处理
		TakeRefer cmd = new TakeRefer(siger);
		TakeReferHook hook = new TakeReferHook();
		ShiftTakeRefer shift = new ShiftTakeRefer(cmd, hook);
		boolean success = HomeCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "loadRefer", "cannot be press!");
			return false;
		}
		hook.await();
		// 返回资源引用
		Refer refer = hook.getRefer();
		success = (refer != null);
		if (success) {
			success = create(refer, false);
		}

		Logger.note(this, "loadRefer", success, "load %s", siger);

		return success;
	}

	/**
	 * 向TOP站点查表配置
	 * @param space 数据表名
	 * @return Table实例
	 */
	private Table searchTable(Space space) {
		TakeTable cmd = new TakeTable(space);
		TakeTableHook hook = new TakeTableHook();
		ShiftTakeTable shift = new ShiftTakeTable(cmd, hook);

		boolean success = HomeCommandPool.getInstance().press(shift);
		if (!success) {
			Logger.error(this, "searchTable", "cannot submit to hub");
			return null;
		}
		// 进入悬停状态
		hook.await();

		return hook.getTable();
	}

	/**
	 * 按照模值，将一段账号签名写入磁盘。目的：减少写磁盘时间。<br>
	 * 调用“writeSigers”的方法，在“锁定”状态下同步写入。
	 * 
	 * @param mod 模值
	 * @return 成功返回真，否则假
	 */
	private boolean writeSigers(int mod) {
		String name = String.format("%d%s", mod, suffix);
		File file = HomeLauncher.getInstance().createResourceFile(name);

		// 找到分段数据
		SignField field = mapFields.get(mod);
		boolean success = (field != null);
		// 如果签名域存在，将这个数组中的签名参数保存到磁盘
		if (success) {
			// 输出字节流
			byte[] b = field.build();
			// 写入磁盘文件
			success = HomeLauncher.getInstance().flushFile(file, b);
		}

		// 写入成功，把这个文件分发到监视站点，做为镜像文件保存
		if(success) {
			String filename = file.getAbsolutePath();
			DispatchReserveResource cmd = new DispatchReserveResource(filename);
			HomeCommandPool.getInstance().admit(cmd);
		}

		return success;
	}

	/**
	 * 读当前配置目录中的全部账号签名和关联模值
	 * @return 返回全部账号签名
	 */
	private List<Siger> readSigers() {
		// 保存账号签名的数组
		ArrayList<Siger> array = new ArrayList<Siger>();

		// 取磁盘目录
		File dir = HomeLauncher.getInstance().getResourcePath();
		boolean success = (dir != null && dir.exists() && dir.isDirectory());
		if (!success) {
			return array;
		}

		// 枚举以".sketch"后缀的文件
		File[] files = dir.listFiles();
		// 判断文件
		for (File file : files) {
			success = file.getAbsolutePath().endsWith(suffix);
			if (!success) {
				continue;
			}

			// 读取数据内容
			byte[] data = HomeLauncher.getInstance().readFile(file);

			// 可类化解析
			SignField field = new SignField(data);

			// 锁定！把签名保存到内存里
			super.lockSingle();
			try {
				mapFields.put(field.getMod(), field);
			} catch (Throwable e) {
				Logger.fatal(e);
			} finally {
				super.unlockSingle();
			}

			// 保存账号签名
			array.addAll(field.list());
		}
		
		return array;
	}


	//	/**
	//	 * 按照模值，将一段用户资源数据写入磁盘。目的：减少写磁盘时间。<br>
	//	 * 在它的调用方法，在“锁定”状态下同步写入。
	//	 * 
	//	 * @param mod 模值
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean writeSketches(int mod) {
	//		ArrayList<Sketch> array = new ArrayList<Sketch>();
	//		ClassWriter writer = new ClassWriter(10240);
	//		String name = String.format("%d%s", mod, suffix);
	//		File file = HomeLauncher.getInstance().createResourceFile(name);
	//
	//		// 找到分段数据
	//		SignField field = mapFields.get(mod);
	//		boolean success = (field != null);
	//		// 如果签名域存在，将这个数组中的签名参数保存到磁盘
	//		if (success) {
	//			for (Siger siger : field.list()) {
	//				Refer refer = mapRefers.get(siger);
	//				Sketch sketch = new Sketch(refer.getUsername());
	//				// 在HOME站点，账号下的表包含两种：自己专属表、其他用户的分享表
	//				sketch.addAll(refer.getTables());
	//				sketch.addAll(refer.getPassiveTables());
	//				array.add(sketch);
	//			}
	//			// 保存数据
	//			writer.writeInt(array.size());
	//			for (Sketch e : array) {
	//				writer.writeObject(e);
	//			}
	//			// 生成字节流
	//			byte[] b = writer.effuse();
	//			// 写入分段文件，返回写入结果
	//			success = HomeLauncher.getInstance().flushFile(file, b);
	//		}
	//
	//		// 写入成功，这个分发到监视站点，做为镜像文件保存
	//		if(success) {
	//			String filename = file.getAbsolutePath();
	//			DispatchReserveResource cmd = new DispatchReserveResource(filename);
	//			HomeCommandPool.getInstance().admit(cmd);
	//		}
	//
	//		return success;
	//	}

	//	/**
	//	 * 读当前配置目录中的全部账号数据（用户签名和表名）
	//	 * @return Sketch列表
	//	 */
	//	private List<Sketch> readSketches() {
	//		ArrayList<Sketch> array = new ArrayList<Sketch>();
	//
	//		File dir = HomeLauncher.getInstance().getResourcePath();
	//		boolean success = (dir != null && dir.exists() && dir.isDirectory());
	//		if (!success) {
	//			return array;
	//		}
	//
	//		// 枚举以".sketch"后缀的文件
	//		File[] files = dir.listFiles();
	//		// 判断文件
	//		for (File file : files) {
	//			success = file.getAbsolutePath().endsWith(suffix);
	//			if (!success) {
	//				continue;
	//			}
	//
	//			// 读取数据内容
	//			byte[] data = HomeLauncher.getInstance().readFile(file);
	//			ClassReader reader = new ClassReader(data);
	//			// 解析每一个Sketch，保存起来
	//			int size = reader.readInt();
	//			for (int i = 0; i < size; i++) {
	//				Sketch sketch = new Sketch(reader);
	//				array.add(sketch);
	//			}
	//		}
	//		// 返回全部数据
	//		return array;
	//	}

}