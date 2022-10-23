/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.util.*;

/**
 * 黑名单管理池。<br>
 * 保存一批被证明无效的账号和登录地址！
 * 
 * @author scott.liang
 * @version 1.0 8/2/2018
 * @since laxcus 1.0
 */
public final class BlackOnGatePool extends VirtualPool {

	/** 黑名单管理池句柄 **/
	private static BlackOnGatePool selfHandle = new BlackOnGatePool();
	
	/** 最大重试值 **/
	private int maxRetry;

	/** 超时时间 **/
	private long timeout;

	/** 用户签名 -> 黑名单 **/
	private TreeMap<Siger, BlackUser> sheets = new TreeMap<Siger, BlackUser>();

	/**
	 * 构造默认的黑名单管理池
	 */
	private BlackOnGatePool() {
		super();
		// 超时时间
		setTimeout(20 * 60000);
		// 默认重试次数是3
		setMaxRetry(3);
	}

	/**
	 * 返回黑名单管理池句柄
	 * 
	 * @return 黑名单管理池句柄
	 */
	public static BlackOnGatePool getInstance() {
		return BlackOnGatePool.selfHandle;
	}
	
	/**
	 * 设置最大值
	 * @param i
	 */
	public int setMaxRetry(int i) {
		if (i > 0) {
			maxRetry = i;
		}
		return maxRetry;
	}

	/**
	 * 返回最大值
	 * @return
	 */
	public int getMaxRetry() {
		return maxRetry;
	}

	/**
	 * 设置超时时间
	 * @param ms 毫秒
	 */
	public void setTimeout(long ms) {
		if (ms < 10000) return;
		
		timeout = ms;
		Logger.debug(this, "setTimeout", "black list timeout: %s ms", ms);
	}

	/**
	 * 返回黑名单在内存的超时时间
	 * @return
	 */
	public long getTimeout() {
		return timeout;
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
		while (!isInterrupted()) {
			delay(10000);
			check();
		}
		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		sheets.clear();
	}

	/**
	 * 判断黑账号存在，且达到最大值
	 * @param user 黑账号
	 * @return 返回真或者假
	 */
	public boolean contains(User user) {
		if (user == null) {
			return false;
		}
		boolean success = false;
		// 锁定，判断有用户名
		super.lockMulti();
		try {
			BlackUser black = sheets.get(user.getUsername());
			// 判断达到最大重试值
			if (black != null) {
				success = black.isMaxRetry(maxRetry);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		if (success) {
			Logger.error(this, "contains", "%s, On the Blacklist!", user);
		}

		return success;
	}

	/**
	 * 保存黑账号
	 * @param user 用户签名
	 */
	public void add(User user) {
		if (user == null) {
			return;
		}

		// 锁定
		super.lockSingle();
		try {
			// 取出
			BlackUser black = sheets.get(user.getUsername());
			// 不存在，生成一个新的
			if (black == null) {
				black = new BlackUser(user);
				sheets.put(user.getUsername(), black);
			}
			// 统计值加1
			black.increase();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.error(this, "add", "%s 加入黑名单", user);
	}
	
	/**
	 * 删除黑名单上的一个注册用户
	 * @param siger 用户签名
	 * @return 找到并且删除返回真，否则假
	 */
	public boolean remove(Siger siger) {
		if (siger == null) {
			return false;
		}
		
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			BlackUser black = sheets.remove(siger);
			success = (black != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		// 显示日志
		if (success) {
			Logger.info(this, "remove", "drop %s, from black list!", siger);
		}

		return success;
	}

	/**
	 * 检查过期的参数
	 */
	private void check() {
		int size = sheets.size();
		if (size < 1) {
			return;
		}

		ArrayList<Siger> array = new ArrayList<Siger>(size);

		// 锁定！
		super.lockSingle();
		try {
			Iterator<Map.Entry<Siger, BlackUser>> iterator = sheets.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, BlackUser> entry = iterator.next();
				// 判断超时，保存它!
				if (entry.getValue().isTimeout(timeout)) {
					array.add(entry.getKey());
				}
			}
			// 删除
			for (Siger issuer : array) {
				sheets.remove(issuer);
				// 删除过期
				Logger.info(this, "check", "drop %s! From the Blacklist!", issuer);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

}

///**
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// * 
// * Copyright 2009 laxcus.com. All rights reserved
// * 
// * @license Laxcus Public License (LPL)
// */
//package com.laxcus.gate.pool;
//
//import java.util.*;
//
//import com.laxcus.access.diagram.*;
//import com.laxcus.log.client.*;
//import com.laxcus.pool.*;
//import com.laxcus.util.*;
//
///**
// * 黑名单管理池。<br>
// * 保存一批被证明无效的账号和登录地址！
// * 
// * @author scott.liang
// * @version 1.0 8/2/2018
// * @since laxcus 1.0
// */
//public final class BlackOnGatePool extends VirtualPool {
//
//	/** 黑名单管理池句柄 **/
//	private static BlackOnGatePool selfHandle = new BlackOnGatePool();
//
//	/** 超时时间 **/
//	private long timeout;
//
//	/** 用户签名 -> 黑名单 **/
//	private TreeMap<User, BlackUser> sheets = new TreeMap<User, BlackUser>();
//
//	/**
//	 * 构造默认的黑名单管理池
//	 */
//	private BlackOnGatePool() {
//		super();
//		// 超时时间
//		setTimeout(20 * 60000);
//	}
//
//	/**
//	 * 返回黑名单管理池句柄
//	 * 
//	 * @return 黑名单管理池句柄
//	 */
//	public static BlackOnGatePool getInstance() {
//		return BlackOnGatePool.selfHandle;
//	}
//
//	/**
//	 * 设置超时时间
//	 * @param ms 毫秒
//	 */
//	public void setTimeout(long ms) {
//		if (ms < 10000) return;
//		
//		timeout = ms;
//		Logger.debug(this, "setTimeout", "black list timeout: %s ms", ms);
//	}
//
//	/**
//	 * 返回超时时间
//	 * @return
//	 */
//	public long getTimeout() {
//		return timeout;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.thread.VirtualThread#init()
//	 */
//	@Override
//	public boolean init() {
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.thread.VirtualThread#process()
//	 */
//	@Override
//	public void process() {
//		Logger.info(this, "process", "into ...");
//		while (!isInterrupted()) {
//			delay(10000);
//			check();
//		}
//		Logger.info(this, "process", "exit");
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.thread.VirtualThread#finish()
//	 */
//	@Override
//	public void finish() {
//		sheets.clear();
//	}
//
//	/**
//	 * 判断黑账号存在！
//	 * @param user 黑账号
//	 * @return 返回真或者假
//	 */
//	public boolean contains(User user) {
//		if (user == null) {
//			return false;
//		}
//		boolean success = false;
//		// 锁定，判断有用户名
//		super.lockMulti();
//		try {
//			success = (sheets.get(user) != null);
//		} finally {
//			super.unlockMulti();
//		}
//
//		if (success) {
//			Logger.error(this, "contains", "%s, On the Blacklist!", user);
//		}
//
//		return success;
//	}
//
////	/**
////	 * 保存黑账号
////	 * @param user 用户签名
////	 */
////	public void add(User user) {
////		if (user == null) {
////			return;
////		}
////		
////		BlackUser black = new BlackUser(user);
////		super.lockSingle();
////		try {
////			sheets.put(black.getUser(), black);
////		} catch (Throwable e) {
////			Logger.fatal(e);
////		} finally {
////			super.unlockSingle();
////		}
////
////		Logger.error(this, "add", "%s 加入黑名单", user);
////	}
//	
//	/**
//	 * 保存黑账号
//	 * @param user 用户签名
//	 */
//	public void add(User user) {
//		if (user == null) {
//			return;
//		}
//		
//		BlackUser black = new BlackUser(user);
//		super.lockSingle();
//		try {
//			sheets.put(black.getUser(), black);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//
//		Logger.error(this, "add", "%s 暂时加入黑名单", user);
//	}
//	
//	/**
//	 * 删除黑名单上的一个注册用户
//	 * @param siger 用户签名
//	 * @return 找到并且删除返回真，否则假
//	 */
//	public boolean remove(Siger siger) {
//		boolean success = false;
//
//		super.lockSingle();
//		try {
//			Iterator<Map.Entry<User, BlackUser>> iterator = sheets.entrySet().iterator();
//			while (iterator.hasNext()) {
//				Map.Entry<User, BlackUser> entry = iterator.next();
//				User user = entry.getKey();
//				// 判断签名一致
//				success = (Laxkit.compareTo(user.getUsername(), siger) == 0);
//				if (success) {
//					sheets.remove(user);
//					break;
//				}
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//
//		// 显示日志
//		if (success) {
//			Logger.info(this, "remove", "drop %s successful! from black list!", siger);
//		}
//		
//		return success;
//	}
//
//	/**
//	 * 检查过期的参数
//	 */
//	private void check() {
//		int size = sheets.size();
//		if (size < 1) {
//			return;
//		}
//
//		ArrayList<User> array = new ArrayList<User>(size);
//
//		// 锁定！
//		super.lockSingle();
//		try {
//			Iterator<Map.Entry<User, BlackUser>> iterator = sheets.entrySet().iterator();
//			while (iterator.hasNext()) {
//				Map.Entry<User, BlackUser> entry = iterator.next();
//				if (entry.getValue().isTimeout(timeout)) {
//					array.add(entry.getKey());
//				}
//			}
//			// 删除
//			for (User user : array) {
//				sheets.remove(user);
//				// 删除过期
//				Logger.info(this, "check", "drop %s! From the Blacklist!", user);
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//	}
//
//}