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
import com.laxcus.fixp.client.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.log.client.*;
import com.laxcus.thread.*;
import com.laxcus.util.net.*;

/**
 * FIXP安全委托代理。<br><br>
 * 
 * FIXP UDP数据包发送前，密文存储器（SecureCollector）没有这个节点地址密文，FIXP安全委托代理负责查询，
 * 确定这个节点是不是要加密，和获取通信双方的密文，最后代理发送FIXP UDP数据包。
 * 
 * @author scott.liang
 * @version 1.1 10/9/2013
 * @since laxcus 1.0
 */
final class SecureTrustor extends MutexThread {

//	/** 节点地址 -> 安全属性 (见SecureType中的定义) **/
//	private Map<SocketHost, Integer> properties = new TreeMap<SocketHost, Integer>();

	/** 保存准备检测节点的数据包 **/
	private ArrayList<Packet> packets = new ArrayList<Packet>(128);
	
	/** UDP数据包监视器 **/
	private FixpPacketMonitor monitor;
	
	/** 密文存储器 **/
	private SecureCollector collector; 
	
	/**
	 * 构造FIXP安全委托代理，指定FIXP数据管理器和FIXP密文存储器
	 * @param e1 FIXP数据管理器
	 * @param e2 FIXP密文存储器
	 */
	public SecureTrustor(FixpPacketMonitor e1, SecureCollector e2) {
		super();
		monitor = e1;
		collector = e2;
	}
	
	/**
	 * 根据地址查找密文
	 * @param server 服务器地址
	 * @return 返回密文，或者空指针
	 */
	private Cipher findCipher(SocketHost server) {
		return collector.find(server);
	}
	
	/**
	 * 清除内存存储的数据包
	 */
	public void clear() {
		Logger.debug(this, "clear", "packet size:%d", packets.size());
		// 锁定
		super.lockSingle();
		try {
			packets.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 清除记录
		ClientSecureTrustor.getInstance().clear();
	}

	/**
	 * 保存一个FIXP数据包
	 * @param packet FIXP数据包
	 * @return 保存成功返回真，否则假
	 */
	public boolean addPacket(Packet packet) {		
		boolean empty = packets.isEmpty();
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			success = packets.add(packet);
		} catch(Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 唤醒线程
		if(empty && success) {
			wakeup();
		}
		
		return success;
	}

//	/**
//	 * 检查一个节点地址的安全属性(见SecureType中的定义)
//	 * @param remote 目标站点
//	 * @return 返回节点安全属性
//	 */
//	private int detect(SocketHost remote) {
//		int timeout = SocketTransfer.getDefaultChannelTimeout();
//		// 通过FixpPacketMonitor信道，以客户机的身份，查询服务端的密文类型
//		int family = monitor.getPacketHelper().askPrivateSecure(remote, timeout);
//
//		// 如果不能确定，这个包可以被丢弃
//		Logger.note(this, "detect", !SecureType.isInvalid(family), "%s secure is \'%s\'", remote,
//				SecureType.translate(family));
//
//		// 返回密文类型
//		return family;
//	}
	
	/**
	 * 检查一个节点地址的安全属性(见SecureType中的定义)
	 * @param remote 目标站点
	 * @return 返回节点安全属性
	 */
	private PublicSecure detect(SocketHost remote) {
		int timeout = SocketTransfer.getDefaultChannelTimeout();
		// 通过FixpPacketMonitor信道，以客户机的身份，查询服务端的加密类型和RSA公钥
		PublicSecure secure = monitor.getPacketHelper().askPrivateSecure(remote, timeout);
		// 若没有，返回空值
		if (secure == null) {
			return null;
		}
		// 如果不能确定，这个包可以被丢弃
		Logger.note(this, "detect", !SecureType.isInvalid(secure.getFamily()), "%s secure is \'%s\'", remote,
				SecureType.translate(secure.getFamily()));

		// 返回密文类型
		return secure;
	}
	
//	/**
//	 * 根据地址，保存一个节点的安全属性。安全属性见SecureType中的定义。
//	 * @param remote 目标站点
//	 * @param family 安全属性
//	 * @return 保存成功返回真，否则假
//	 */
//	public boolean addSiteSecure(SocketHost remote, int family) {
//		super.lockSingle();
//		try {
//			return properties.put((SocketHost) remote.clone(), new Integer(
//					family)) == null;
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//		return false;
//	}

	/**
	 * 根据地址，保存一个节点的安全属性。安全属性见SecureType中的定义。
	 * @param remote 目标站点
	 * @param family 安全属性
	 * @return 保存成功返回真，否则假
	 */
	public boolean addSiteSecure(SocketHost remote, PublicSecure secure) {
		return ClientSecureTrustor.getInstance().addSiteSecure(remote, secure);
		
//		super.lockSingle();
//		try {
//			return properties.put((SocketHost) remote.clone(), new Integer(
//					family)) == null;
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//		return false;
	}
	
	/**
	 * 根据地址，检查一个节点的安全属性，未定义，返回-1
	 * @param endpoint 目标站点地址
	 * @return 返回这个节点的安全属性
	 */
	public ClientSecure findSiteSecure(SocketHost endpoint) {
		return ClientSecureTrustor.getInstance().findSiteSecure(endpoint);
		
//		super.lockMulti();
//		try {
//			Integer family = properties.get(endpoint);
//			if (family != null) {
//				return family.intValue();
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockMulti();
//		}
//		// 未定义
//		return -1;
	}
	
	/**
	 * 弹出一个包
	 * @return Packet实例
	 */
	private Packet poll() {
		super.lockSingle();
		try {
			if (packets.size() > 0) {
				return packets.remove(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}
	
	/**
	 * 处理一个请求包
	 * @param request 请求包
	 */
	private void subprocess(Packet request) {
		SocketHost remote = request.getRemote();
		// 找密文
		Cipher cipher = findCipher(remote);
		// 1.如果密文存在，交给服务器去处理，退出
		if (cipher != null) {
			monitor.__notice(request);
			return;
		}
		// 2. 如果不需要加密时，直接投递
		ClientSecure clientSecure = findSiteSecure(remote);
		if (clientSecure != null) {
			if (clientSecure.getFamily() > SecureType.INVALID && !SecureType.isCipher(clientSecure.getFamily())) {
				monitor.__notice(request);
				return;
			}
		}
		// 3. 以上不成立，检测这个节点
		PublicSecure secure = detect(remote);
		// 如果不能确定，这个包可以被丢弃
		if (secure == null || SecureType.isInvalid(secure.getFamily())) {
			Logger.error(this, "subprocess", "cannot be detect %s, ignore it!", remote);
			return;
		}

		// 4. 保存这个节点的安全属性
		addSiteSecure(remote, secure);
		// 5. 如果不需要加密时，把这个包发送出去
		if (!SecureType.isCipher(secure.getFamily())) {
			monitor.__notice(request);
			return;
		}
		// 5. 需要加密，生成一个服务器密文
		cipher = Cipher.create(false);

		// 以信道通信超时为准
		long timeout = SocketTransfer.getDefaultChannelTimeout();
		// 要求FIXP服务器，以客户机身份，走私密通道，发送安全校验密文到服务端的FIXP服务器
		boolean success = monitor.getPacketHelper().createPrivateSecure(remote, timeout, 3, cipher);
		
		// 成功，保存密文，然后重新发送；失败，丢弃数据包
		if (success) {
			monitor.__notice(request);
		} else {
			Logger.error(this, "subprocess", "create cipher to %s, ignore %s",
					remote, request.getMark());
		}
		
		Logger.note(this, "subprocess", success, "create cipher %s to %s", cipher, remote);
	}
	
	/**
	 * 处理请求包
	 * @return 返回处理的包数目
	 */
	private int subprocess() {
		int size = packets.size();
		for (int i = 0; i < size; i++) {
			Packet request = poll();
			if (request != null) {
				subprocess(request);
			}
		}
		return size;
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
			// 处理请求包集
			int count = subprocess();
			if (count == 0) {
				// 清除失效的公钥，一分钟失效
				ClientSecureTrustor.getInstance().remoteTimeoutSecure(60000L);
				// 延时5秒
				delay(5000);
			}
		}
		Logger.info(this, "process", "exit ...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		packets.clear();
		ClientSecureTrustor.getInstance().clear();
//		properties.clear();
	}

}

///**
// * FIXP安全委托代理。<br><br>
// * 
// * FIXP UDP数据包发送前，密文存储器（SecureCollector）没有这个节点地址密文，FIXP安全委托代理负责查询，
// * 确定这个节点是不是要加密，和获取通信双方的密文，最后代理发送FIXP UDP数据包。
// * 
// * @author scott.liang
// * @version 1.1 10/9/2013
// * @since laxcus 1.0
// */
//final class SecureTrustor extends MutexThread {
//
//	/** 节点地址 -> 安全属性 (见SecureType中的定义) **/
//	private Map<SocketHost, Integer> properties = new TreeMap<SocketHost, Integer>();
//
//	/** 保存准备检测节点的数据包 **/
//	private ArrayList<Packet> packets = new ArrayList<Packet>(128);
//	
//	/** UDP数据包监视器 **/
//	private FixpPacketMonitor monitor;
//	
//	/** 密文存储器 **/
//	private SecureCollector collector; 
//	
//	/**
//	 * 构造FIXP安全委托代理，指定FIXP数据管理器和FIXP密文存储器
//	 * @param e1 FIXP数据管理器
//	 * @param e2 FIXP密文存储器
//	 */
//	public SecureTrustor(FixpPacketMonitor e1, SecureCollector e2) {
//		super();
//		monitor = e1;
//		collector = e2;
//	}
//	
//	/**
//	 * 根据地址查找密文
//	 * @param server 服务器地址
//	 * @return 返回密文，或者空指针
//	 */
//	private Cipher findCipher(SocketHost server) {
//		return collector.find(server);
//	}
//	
//	/**
//	 * 清除内存存储的数据包
//	 */
//	public void clear() {
//		Logger.debug(this, "clear", "packet size:%d", packets.size());
//		// 锁定
//		super.lockSingle();
//		try {
//			packets.clear();
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//	}
//
//	/**
//	 * 保存一个FIXP数据包
//	 * @param packet FIXP数据包
//	 * @return 保存成功返回真，否则假
//	 */
//	public boolean addPacket(Packet packet) {		
//		boolean empty = packets.isEmpty();
//		boolean success = false;
//		// 锁定
//		super.lockSingle();
//		try {
//			success = packets.add(packet);
//		} catch(Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//		// 唤醒线程
//		if(empty && success) {
//			wakeup();
//		}
//		
//		return success;
//	}
//
//	/**
//	 * 检查一个节点地址的安全属性(见SecureType中的定义)
//	 * @param remote 目标站点
//	 * @return 返回节点安全属性
//	 */
//	private int detect(SocketHost remote) {
//		int timeout = SocketTransfer.getDefaultChannelTimeout();
//		// 通过FixpPacketMonitor信道，以客户机的身份，查询服务端的密文类型
//		int family = monitor.getPacketHelper().askPrivateSecure(remote, timeout);
//
//		// 如果不能确定，这个包可以被丢弃
//		Logger.note(this, "detect", !SecureType.isInvalid(family), "%s secure is \'%s\'", remote,
//				SecureType.translate(family));
//
//		// 返回密文类型
//		return family;
//	}
//	
//	/**
//	 * 根据地址，保存一个节点的安全属性。安全属性见SecureType中的定义。
//	 * @param remote 目标站点
//	 * @param family 安全属性
//	 * @return 保存成功返回真，否则假
//	 */
//	public boolean addSiteSecure(SocketHost remote, int family) {
//		super.lockSingle();
//		try {
//			return properties.put((SocketHost) remote.clone(), new Integer(
//					family)) == null;
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//		return false;
//	}
//
//	/**
//	 * 根据地址，检查一个节点的安全属性，未定义，返回-1
//	 * @param endpoint 目标站点地址
//	 * @return 返回这个节点的安全属性
//	 */
//	public int findSiteSecure(SocketHost endpoint) {
//		super.lockMulti();
//		try {
//			Integer family = properties.get(endpoint);
//			if (family != null) {
//				return family.intValue();
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockMulti();
//		}
//		// 未定义
//		return -1;
//	}
//	
//	/**
//	 * 弹出一个包
//	 * @return Packet实例
//	 */
//	private Packet poll() {
//		super.lockSingle();
//		try {
//			if (packets.size() > 0) {
//				return packets.remove(0);
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//		return null;
//	}
//	
//	/**
//	 * 处理一个请求包
//	 * @param request 请求包
//	 */
//	private void subprocess(Packet request) {
//		SocketHost remote = request.getRemote();
//		// 找密文
//		Cipher cipher = findCipher(remote);
//		// 1.如果密文存在，交给服务器去处理，退出
//		if (cipher != null) {
//			monitor.__notice(request);
//			return;
//		}
//		// 2. 如果不需要加密时，直接投递
//		int family = findSiteSecure(remote);
//		if (family > SecureType.INVALID && !SecureType.isCipher(family)) {
//			monitor.__notice(request);
//			return;
//		}
//		// 3. 以上不成立，检测这个节点
//		family = detect(remote);
//		// 如果不能确定，这个包可以被丢弃
//		if (SecureType.isInvalid(family)) {
//			Logger.error(this, "subprocess", "cannot be detect %s, ignore it!", remote);
//			return;
//		}
//
//		// 4. 保存这个节点的安全属性
//		addSiteSecure(remote, family);
//		// 5. 如果不需要加密时，把这个包发送出去
//		if (!SecureType.isCipher(family)) {
//			monitor.__notice(request);
//			return;
//		}
//		// 5. 需要加密，生成一个服务器密文
//		cipher = Cipher.create(false);
//
//		// 以信道通信超时为准
//		long timeout = SocketTransfer.getDefaultChannelTimeout();
//		// 要求FIXP服务器，以客户机身份，走私密通道，发送安全校验密文到服务端的FIXP服务器
//		boolean success = monitor.getPacketHelper().createPrivateSecure(remote, timeout, 3, cipher);
//		
//		// 成功，保存密文，然后重新发送；失败，丢弃数据包
//		if (success) {
//			monitor.__notice(request);
//		} else {
//			Logger.error(this, "subprocess", "create cipher to %s, ignore %s",
//					remote, request.getMark());
//		}
//		
//		Logger.note(this, "subprocess", success, "create cipher %s to %s", cipher, remote);
//	}
//	
//	/**
//	 * 处理请求包
//	 * @return 返回处理的包数目
//	 */
//	private int subprocess() {
//		int size = packets.size();
//		for (int i = 0; i < size; i++) {
//			Packet request = poll();
//			if (request != null) {
//				subprocess(request);
//			}
//		}
//		return size;
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
//			// 处理请求包集
//			int count = subprocess();
//			if (count == 0) {
//				delay(5000);
//			}
//		}
//		Logger.info(this, "process", "exit ...");
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.thread.VirtualThread#finish()
//	 */
//	@Override
//	public void finish() {
//		packets.clear();
//		properties.clear();
//	}
//
//}