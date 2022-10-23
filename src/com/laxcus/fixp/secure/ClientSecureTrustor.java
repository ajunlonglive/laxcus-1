/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.secure;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.security.*;
import com.laxcus.util.lock.*;
import com.laxcus.util.net.*;

/**
 * 客户机RSA公钥代理
 * 
 * 保存来自服务器的RSA公钥
 * 
 * @author scott.liang
 * @version 1.0 2/10/2021
 * @since laxcus 1.0
 */
public class ClientSecureTrustor extends MutexHandler {
	
	/** 实例 **/
	private static ClientSecureTrustor selfHandle = new ClientSecureTrustor();

	/** 节点地址 -> 客户机RSA安全配置 (见SecureType中的定义) **/
	private Map<SocketHost, ClientSecure> elements = new TreeMap<SocketHost, ClientSecure>();
	
	/**
	 * 构造客户机密钥代理
	 */
	private ClientSecureTrustor() {
		super();
	}
	
	/**
	 * 保存实例
	 * @return
	 */
	public static ClientSecureTrustor getInstance(){
		return ClientSecureTrustor.selfHandle;
	}
	
	/**
	 * 清除记录
	 */
	public void clear() {
		super.lockSingle();
		try {
			elements.clear();
		} finally {
			super.unlockSingle();
		}
	}
	
	/**
	 * 根据地址，保存一个节点的安全属性。安全属性见SecureType中的定义。
	 * @param remote 目标站点
	 * @param secure 安全属性
	 * @return 保存成功返回真，否则假
	 */
	public boolean addSiteSecure(SocketHost remote, PublicSecure secure) {
		// 生成对象
		ClientSecure client = null;
		try {
			client = new ClientSecure(remote, secure);
		} catch (SecureException e) {
			Logger.error(e);
		}

		boolean success = false;
		// 有效，保存它
		if (client != null) {
			super.lockSingle();
			try {
				elements.put(client.getRemote(), client);
				success = false;
			} catch (Throwable e) {
				Logger.fatal(e);
			} finally {
				super.unlockSingle();
			}
		}
		return success;
	}

	/**
	 * 根据地址，检查一个节点的安全属性，未定义，返回-1
	 * @param endpoint 目标站点地址
	 * @return 返回这个节点的安全属性
	 */
	public ClientSecure findSiteSecure(SocketHost endpoint) {
		super.lockMulti();
		try {
			return elements.get(endpoint);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		// 未定义
		return null;
	}
	
	/**
	 * 判断存在有效
	 * @param endpoint 目标地址
	 * @return 真或者假
	 */
	public boolean hasSiteSecure(SocketHost endpoint) {
		ClientSecure e = findSiteSecure(endpoint);
		return e != null;
	}

	/**
	 * 删除超时成员
	 * @param ms 超时时间
	 * @return 返回删除的成员
	 */
	public int remoteTimeoutSecure(long ms) {
		int size = elements.size();
		if (size == 0) {
			return 0;
		}
		ArrayList<SocketHost> array = new ArrayList<SocketHost>(size);

		// 锁定，删除！
		super.lockSingle();
		try {
			Iterator<Map.Entry<SocketHost, ClientSecure>> iterator = elements.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, ClientSecure> entry = iterator.next();
				ClientSecure secure = entry.getValue();
				if (secure.isTimeout(ms)) {
					array.add(entry.getKey());
				}
			}
			// 删除
			for (SocketHost remote : array) {
				elements.remove(remote);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 返回删除的成员数目
		return array.size();
	}
	
}