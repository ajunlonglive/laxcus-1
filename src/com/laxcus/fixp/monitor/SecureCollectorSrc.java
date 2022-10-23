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
import com.laxcus.thread.*;
import com.laxcus.util.net.*;

/**
 * 密文存储器。<br>
 * 密文存储器保存UDP通信时的密文数据(包括以客户模式和服务器模式通信的密文)
 * 
 * @author scott.liang
 * @version 1.0 12/6/2012
 * @since laxcus 1.0
 */
final class SecureCollectorSrc extends MutexThread {
	
	/** FIXP数据包在服务器状态，保存的(节点地址 -> 密文信息 ) */
	private Map<SocketHost, Cipher> mapCiphers = new TreeMap<SocketHost, Cipher>();

	/**
	 * 构造密文存储器
	 */
	public SecureCollectorSrc() {
		super();
	}

	/**
	 * 根据地址保存一个密文。如果旧的密文，将被替换。
	 * 
	 * @param endpoint 密文地址
	 * @param cipher 密文
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
		super.lockSingle();
		try {
			// 保存它，同时判断它已经存在
			update = (mapCiphers.put(endpoint, cipher) != null);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		Logger.note(this, "add", success, "%s, from %s & %s",
				(update ? "replaced" : "newly increased"), endpoint, cipher);	

		return success;
	}

	/**
	 * 根据地址删除一个密文
	 * @param endpoint 密文地址
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean remove(SocketHost endpoint) {
		if (endpoint == null) {
			return false;
		}

		Cipher cipher = null;
		// 锁定删除
		super.lockSingle();
		try {
			cipher = mapCiphers.remove(endpoint);
		} finally {
			super.unlockSingle();
		}

		// 判断删除成功!
		boolean success = (cipher != null);

		Logger.note(this, "remove", success, "from %s & %s", endpoint, cipher);

		return success;
	}
	
	/**
	 * 根据目标地址查找一个密文
	 * @param endpoint 目标站点地址
	 * @return 返回Cipher实例，或者空指针
	 */
	public Cipher find(SocketHost endpoint) {
		if (endpoint == null) {
			return null;
		}
		Cipher cipher = null;
		super.lockMulti();
		try {
			cipher = mapCiphers.get(endpoint);
			if (cipher != null) {
				cipher.refresh(); // 如果被调用，就刷新一次，保持它的激活状态
			}
		} finally {
			super.unlockMulti();
		}
		return cipher;
	}

	/**
	 * 根据目标地址，判断一个密文是否存在
	 * @param endpoint 目标站点地址
	 * @return 返回真或者假
	 */
	public boolean contains(SocketHost endpoint) {
		if (endpoint == null) {
			return false;
		}
		boolean success = false;
		super.lockMulti();
		try {
			success = (mapCiphers.get(endpoint) != null);
		} finally {
			super.unlockMulti();
		}
		return success;
	}
	
	/**
	 * 根据目标地址，判断有关联密文存在
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
		// 锁定
		super.lockSingle();
		try {
			mapCiphers.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 释放超时的节点密文
	 */
	private void check() {
		int size = mapCiphers.size();
		if (size < 1) {
			return;
		}

		// 失效的地址数组
		ArrayList<SocketHost> array = new ArrayList<SocketHost>(size);
		
		super.lockMulti();
		try {
			Iterator<Map.Entry<SocketHost, Cipher>> iterator = mapCiphers.entrySet().iterator();
			// 找到超时密文并且保存目标地址
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, Cipher> entry = iterator.next();
				Cipher cipher = entry.getValue();
				// 判断超时
				if(cipher.isTimeout()) {
					array.add(entry.getKey());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		
		// 删除超时记录
		for (SocketHost endpoint : array) {
			remove(endpoint);
		}
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

		// 2秒检查超时密文
		while (!isInterrupted()) {
			check();
			delay(2000);
		}

		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapCiphers.clear();
	}

}