/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.monitor;

import java.util.*;

import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.util.lock.*;
import com.laxcus.util.net.*;

/**
 * 密钥存储器。<br>
 * 密钥存储器保存UDP通信时的密钥数据(包括以客户模式和服务器模式通信的密钥)
 * 
 * @author scott.liang
 * @version 1.0 12/6/2012
 * @since laxcus 1.0
 */
final class SecureCollector extends TimerTask {

	/** 互斥锁(多读/单写模式) */
	private MutexLock lock = new MutexLock();

	/** FIXP数据包在服务器状态，保存的(节点地址 -> 密钥信息 ) */
	private Map<SocketHost, Cipher> mapCiphers = new TreeMap<SocketHost, Cipher>();

	/**
	 * 构造密钥存储器
	 */
	public SecureCollector() {
		super();
	}

	/**
	 * 根据地址保存一个密钥。如果旧的密钥，将被替换。
	 * 
	 * @param endpoint 密钥地址
	 * @param cipher 密钥
	 * @return 成功返回真，否则假
	 */
	public boolean add(SocketHost endpoint, Cipher cipher) {
		if (endpoint == null || cipher == null) {
			return false;
		}
		// 保存参数
		boolean success = false;
		boolean update = false;
		// 锁定
		lock.lockSingle();
		try {
			// 保存它，同时判断它已经存在
			update = (mapCiphers.put(endpoint, cipher) != null);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlockSingle();
		}

		Logger.note(this, "add", success, "%s, from %s & %s",
				(update ? "replaced" : "newly increased"), endpoint, cipher);	

		return success;
	}

	/**
	 * 根据地址删除一个密钥
	 * @param endpoint 密钥地址
	 * @return 返回被删除的密钥
	 */
	public Cipher drop(SocketHost endpoint) {
		if (endpoint == null) {
			return null;
		}

		Cipher cipher = null;
		// 锁定删除
		lock.lockSingle();
		try {
			cipher = mapCiphers.remove(endpoint);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlockSingle();
		}

		return cipher;
	}
	
	/**
	 * 根据地址删除一个密钥
	 * @param endpoint 密钥地址
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean remove(SocketHost endpoint) {
		if (endpoint == null) {
			return false;
		}

		Cipher cipher = null;
		// 锁定删除
		lock.lockSingle();
		try {
			cipher = mapCiphers.remove(endpoint);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlockSingle();
		}

		// 判断删除成功!
		boolean success = (cipher != null);

//		if (success) {
//			Logger.info(this, "remove", "from %s & %s", endpoint, cipher);
//		} else {
//			Logger.warning(this, "remove", "from %s & %s", endpoint, cipher);
//		}

		return success;
	}

	/**
	 * 根据目标地址查找一个密钥
	 * @param endpoint 目标站点地址
	 * @return 返回Cipher实例，或者空指针
	 */
	public Cipher find(SocketHost endpoint) {
		if (endpoint == null) {
			return null;
		}
		Cipher cipher = null;
		// 锁定
		lock.lockMulti();
		try {
			cipher = mapCiphers.get(endpoint);
			if (cipher != null) {
				cipher.refresh(); // 如果被调用，就刷新一次，保持它的激活状态
			}
		} finally {
			lock.unlockMulti();
		}
		return cipher;
	}

	/**
	 * 根据目标地址，判断一个密钥是否存在
	 * @param endpoint 目标站点地址
	 * @return 返回真或者假
	 */
	public boolean contains(SocketHost endpoint) {
		if (endpoint == null) {
			return false;
		}
		boolean success = false;
		lock.lockMulti();
		try {
			success = (mapCiphers.get(endpoint) != null);
		} finally {
			lock.unlockMulti();
		}
		return success;
	}

	/**
	 * 根据目标地址，判断有关联密钥存在
	 * @param endpoint 目标站点地址
	 * @return 返回真或者假
	 */
	public boolean hasCipher(SocketHost endpoint){
		return contains(endpoint);
	}

	/**
	 * 清除全部密钥
	 */
	public void clear() {
		Logger.debug(this, "clear", "ciphers size:%d", mapCiphers.size());
		
		// 锁定
		lock.lockSingle();
		try {
			mapCiphers.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlockSingle();
		}
	}

	/**
	 * 释放超时的节点密钥
	 */
	private void check() {
		int size = mapCiphers.size();
		if (size < 1) {
			return;
		}

		// 失效的地址数组
		ArrayList<SocketHost> array = new ArrayList<SocketHost>(size);

		lock.lockMulti();
		try {
			Iterator<Map.Entry<SocketHost, Cipher>> iterator = mapCiphers.entrySet().iterator();
			// 找到超时密钥并且保存目标地址
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, Cipher> entry = iterator.next();
				Cipher cipher = entry.getValue();
				// 判断超时
				if (cipher.isTimeout()) {
					array.add(entry.getKey());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			lock.unlockMulti();
		}

		// 删除超时记录
		for (SocketHost endpoint : array) {
			remove(endpoint);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		check();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		mapCiphers.clear();
	}

}