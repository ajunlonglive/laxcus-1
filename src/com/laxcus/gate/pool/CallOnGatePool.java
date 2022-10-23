/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.relate.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.call.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * CALL站点管理池。<br><br>
 * 
 * GATE站点间隔20分钟，从TOP站点获取同账号的CALL站点记录。FRONT站点再从这里获得关联的CALL站点记录。
 * GATE站点不会去PING获取的CALL站点，只是定时更新
 * 
 * @author scott.liang
 * @version 1.3 08/02/2014
 * @since laxcus 1.0
 */
public final class CallOnGatePool extends VirtualPool {

	/** CALL站点管理池 **/
	private static CallOnGatePool selfHandle = new CallOnGatePool();

	/** CALL站点地址 -> CALL站点单元 **/
	private Map<Seat, CallItem> mapItems = new TreeMap<Seat, CallItem>();

	/** 用户签名 -> CALL站点集合 **/
	private Map<Siger, NodeSet> mapUsers = new TreeMap<Siger, NodeSet>();
	
	/** 用户签名  -> 用户登录时间 ，超过这个时间 且用户触发检索时，去HOME节点检索自己的CALL资源 **/
	private Map<Siger, TrackTime> mapTimes = new TreeMap<Siger, TrackTime>();

	/** 再次检查CALL站点 **/
	private volatile long interval;

	/**
	 * 构造CALL站点管理池
	 */
	private CallOnGatePool() {
		super();
		// 30秒检查一次！
		setSleepTime(30);
		// 用户超时
		setInterval(60000);
	}

	/**
	 * 返回CALL站点管理池静态句柄
	 * @return
	 */
	public static CallOnGatePool getInstance() {
		return CallOnGatePool.selfHandle;
	}

	/**
	 * 用户重新检查账号资源的时间，最低10秒钟。<br>
	 * 因为GATE节点属于服务器端，参数是管理员设置，下限可以设置低些。
	 */
	public void setInterval(long ms) {
		if(ms > 10000) {
			interval = ms;
		}
	}
	
	/**
	 * 返回触发超时
	 * @return
	 */
	public long getInterval() {
		return interval;
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
		Logger.info(this, "process", "into ...");

		// 循环处理
		while (!isInterrupted()) {
			// 定时检测
			check();
			sleep();
		}

		Logger.info(this, "process", "exit");
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapItems.clear();
		mapUsers.clear();
	}

	/**
	 * 检查已经不存在的账号，删除它！
	 */
	private void check() {
		// 获取当前用户签名
		List<Siger> array = list();

		// 检索每个账号，清除过期的
		for (Siger siger : array) {
			// 判断是授权人
			boolean success = FrontOnGatePool.getInstance().contains(siger);
			// 判断被授权人账号有
			if (!success) {
				success = (ConferrerFrontOnGatePool.getInstance().hasAuthorizer(siger) 
						|| ConferrerFrontOnGatePool.getInstance().hasConferrer(siger));
			}
			if (!success) {
				remove(siger);
			}
		}
	}
	
	/**
	 * 输出当前全部用户签名
	 * @return 用户签名列表
	 */
	public List<Siger> list() {
		ArrayList<Siger> a = new ArrayList<Siger>();
		super.lockMulti();
		try {
			a.addAll(mapUsers.keySet());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return a;
	}

	/**
	 * 判断与签名关联的CALL站点存在
	 * @param issuer 用户签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger issuer) {
		return find(issuer) != null;
	}

	/**
	 * 返回与签名关联的CALL站点集合
	 * @param issuer 用户签名
	 * @return NodeSet实例
	 */
	public NodeSet find(Siger issuer) {
		super.lockMulti();
		try {
			return mapUsers.get(issuer);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据用户签名，返回内存中的全部CALL站点地址和它的配置参数
	 * @param issuer 用户签名
	 * @return CallItem列表 
	 */
	private List<CallItem> searchLocal(Siger issuer) {
		ArrayList<CallItem> array = new ArrayList<CallItem>();

		// 锁定!
		super.lockMulti();
		try {
			// 查找关联参数
			NodeSet set = mapUsers.get(issuer);
			if (set != null) {
				for (Node node : set.list()) {
					Seat seat = new Seat(issuer, node);
					CallItem item = mapItems.get(seat);
					// 注意！必须生成副本输出！
					if (item != null) {
						array.add(item.duplicate());
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
	 * 重置超时，即恢复时间刻度为0
	 * @param issuer 用户签名
	 * @return 成功返回真，否则假
	 */
	public boolean resetTimeout(Siger issuer) {
		if (issuer == null) {
			return false;
		}

		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			TrackTime time = mapTimes.get(issuer);
			success = (time != null);
			if (success) {
				time.resetTime();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}
	
	/**
	 * 没有超时，仍然在时间范围内，拒绝处理！
	 * @param issuer 用户签名
	 * @return 返回真或者假
	 */
	private boolean isTimeout(Siger issuer) {
		boolean success = false;
		super.lockMulti();
		try {
			TrackTime time = mapTimes.get(issuer);
			success = (time != null && time.isTimeout(interval)) ;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}
	
	/**
	 * 根据用户签名，返回全部CALL站点地址和它的配置参数
	 * @param issuer 用户签名
	 * @return CallItem列表 
	 */
	public List<CallItem> search(Siger issuer) {
		// 用户不存在，或者超时，重新启动搜索
		boolean reload = (!contains(issuer) || isTimeout(issuer));
		if (reload) {
			// 重新加载
			loadCallSites(issuer);
		}
		// 执行本地搜索
		return searchLocal(issuer);
	}

	/**
	 * 删除账号以及账号下的全部注册CALL站点
	 * @param siger 用户签名
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Siger siger) {
		int count = 0;
		// 锁定
		super.lockSingle();
		try {
			NodeSet set = mapUsers.remove(siger);
			if (set != null) {
				for (Node node : set.list()) {
					Seat seat = new Seat(siger, node);
					// 删除一个单元
					CallItem element = mapItems.remove(seat);
					if (element != null) {
						count++;
					}
				}
			}
			// 清除时间 
			mapTimes.remove(siger);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 判断成功
		boolean success = (count > 0);

		Logger.debug(this, "remove", success, "delete %s count: %d", siger, count);

		return success;
	}

	/**
	 * 更新记录
	 * @param product
	 * @return
	 */
	private boolean refresh(TakeCallItemProduct product) {
		Siger siger = product.getSiger();

		boolean success = false;
		// 锁定！
		super.lockSingle();
		try {
			// 删除旧记录
			NodeSet set = mapUsers.remove(siger);
			if (set != null) {
				for (Node node : set.list()) {
					Seat seat = new Seat(siger, node);
					mapItems.remove(seat);
				}
			}

			// 建立新记录并且保存
			set = new NodeSet();
			mapUsers.put(siger, set);
			for (CallItem item : product.list()) {
				// 内网记录
				Node inner = item.getPrivate();
				Seat seat = new Seat(siger, inner);
				mapItems.put(seat, item);
				set.add(inner);

				// 外网记录
				Node outer = item.getPublic();
				seat = new Seat(siger, outer);
				mapItems.put(seat, item);
				set.add(outer);

				// DEBUG CODE, BEGIN
				CallMember member = item.getMember();
				Logger.debug(this, "refresh", "%s, space size:%d, phase size:%d",
						siger, member.getTables().size(), member.getPhases().size());

				for(Space space : member.getTables()) {
					Logger.debug(this, "refresh", "this is %s", space);
				}
				for(Phase phase : member.getPhases()) {
					Logger.debug(this, "refresh", "this is %s", phase);
				}
				// DEBUG CODE, END
			}
			
			// 记录新的时间
			mapTimes.put(siger, new TrackTime());
			
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "refresh", success, "refresh [%s]", siger);

		return success;
	}

	/**
	 * 通过BANK站点获得CALL站点地址
	 * @param siger 账号签名
	 * @return 成功返回真，否则假
	 */
	public boolean loadCallSites(Siger siger) {
		TakeCallItem cmd = new TakeCallItem(siger);
		TakeCallItemHook hook = new TakeCallItemHook();
		ShiftTakeCallItem shift = new ShiftTakeCallItem(cmd, hook);
		// 提交给命令管理池，启动线程查询
		boolean success = getLauncher().getCommandPool().press(shift);
		if (!success) {
			Logger.error(this, "loadCallSites", "cannot be admit");
			return false;
		}
		hook.await();

		// 获得处理结果
		TakeCallItemProduct product = hook.getProduct();
		success = (product != null);
		if (success) {
			refresh(product);
		}

		Logger.debug(this, "loadCallSites", success, "load %s", siger);

		return success;
	}

//	/**
//	 * 没有超时，仍然在时间范围内，拒绝处理！
//	 * @param siger 用户签名
//	 * @return 返回真或者假
//	 */
//	public boolean isTimeinRefuse(Siger siger) {
//		boolean success = false;
//		super.lockMulti();
//		try {
//			TrackTime time = mapTimes.get(siger);
//			success = (time != null && time.isTimeout(interval)) ;
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockMulti();
//		}
//		return success;
//	}
	
//	/**
//	 * 根据用户签名，返回全部CALL站点地址和它的配置参数
//	 * @param siger 用户签名
//	 * @return CallItem列表 
//	 */
//	private List<CallItem> __search(Siger siger) {
//		ArrayList<CallItem> array = new ArrayList<CallItem>();
//
//		super.lockMulti();
//		try {
//			TrackTime time = mapTimes.get(siger);
//			// 如果没有记录时间，或者超时，返回空数组，让"loaCallSites"方法去执行更新
//			if(time == null || time.isTimeout(interval)) {
//				return array;
//			}
//
//			// 查找关联参数
//			NodeSet set = mapUsers.get(siger);
//			if (set != null) {
//				for (Node node : set.list()) {
//					Seat seat = new Seat(siger, node);
//					CallItem item = mapItems.get(seat);
//					if (item != null) {
//						array.add(item);
//					}
//				}
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockMulti();
//		}
//
//		return array;
//	}


	
//	/**
//	 * 根据用户签名，返回全部CALL站点地址和它的配置参数
//	 * @param siger 用户签名
//	 * @return CallItem列表 
//	 */
//	public List<CallItem> search(Siger siger) {
//		// 搜索关联参数
//		List<CallItem> array = __search(siger);
//		boolean timeout = this.isTimeout(siger);
//		// 空集合，但是没有超时，不必搜索
//		if(array.isEmpty() && !timeout) {
//			return array;
//		}
//		// 如果是空集合，或者超时时，启动重新搜索。
//		boolean success = (array.isEmpty() || isTimeout(siger));
//		// 如果是空集合，启动即时搜索
//		if (array.isEmpty()) {
//			// 重新加载
//			loadCallSites(siger);
//			// 再次搜索
//			array = __search(siger);
//		}
//
//		return array;
//	}

//	/**
//	 * 根据用户签名，返回全部CALL站点地址和它的配置参数
//	 * @param siger 用户签名
//	 * @return CallItem列表 
//	 */
//	public List<CallItem> search(Siger siger) {
//		ArrayList<CallItem> array = new ArrayList<CallItem>();
//
//		super.lockMulti();
//		try {
//			NodeSet set = mapUsers.get(siger);
//			if (set != null) {
//				for (Node node : set.list()) {
//					Seat seat = new Seat(siger, node);
//					CallItem item = mapItems.get(seat);
//					if (item != null) {
//						array.add(item);
//					}
//				}
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockMulti();
//		}
//
//		return array;
//	}


//	/**
//	 * 尝试发送查询关联HOME站点命令
//	 */
//	private void attempt() {
//		List<Siger> sigers = StaffOnGatePool.getInstance().getSigers();		
//		List<Siger> records = list();
//
//		Logger.debug(this, "attempt", "siger size:%d, record size:%d", sigers.size(), records.size());
//
//		// 注册没有，但是缓存有，删除它
//		for(Siger siger : records) {
//			if(!sigers.contains(siger)) {
//				remove(siger);
//			}
//		}
//
//		// 获得分布的CALL站点
//		for (Siger siger : sigers) {
//			TakeCallItem cmd = new TakeCallItem(siger);
//			cmd.setTimeout(interval);
//			GateCommandPool.getInstance().admit(cmd);
//		}
//	}

}