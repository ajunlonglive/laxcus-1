/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.rabbet;

import java.io.*;
import java.net.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * 辅助连接器<br><br>
 * 
 * 在LAXCUS系统里，某些站点以“客户机”身份存在时，需要与多个“服务器站点”保持联系，这时它通过“Rabbet”来维持彼此的关系。<br>
 * 在这些服务器站点里，只有一个是“主维持站点”，通过xxxLauncher保持连接；其它是“辅助维持站点”，通过Rabbet发送PING握手消息保持连接。<br>
 * 
 * “Rabbet”只被客户机站点使用，定时与服务器站点保持握手通知，检查服务器站点处于活跃状态。<br><br>
 * 
 * 目前有三类：<br>
 * 1. AidRabbet，被FRONT站点使用，以“被授权人”身份登录到CALL站点。<br>
 * 2. CallRabbet, 被FRONT站点使用，登录到CALL站点。<br>
 * 3. HomeRabbet, 被CALL站点使用，登录到HOME站点（实现多集群共享服务）。<br><br>
 * 
 * @author scott.liang
 * @version 1.1 05/03/2015
 * @since laxcus 1.0
 */
public abstract class Rabbet implements Classable, Serializable, Cloneable, Comparable<Rabbet> {

	private static final long serialVersionUID = 5132824587733656549L;

	/**
	 * 将辅助连接器参数写入可类化数据存储器
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 服务器地址
		writer.writeObject(hub);
		// 服务器超时时间
		writer.writeLong(timeout);
		// 将子类的参数信息写入
		buildSuffix(writer);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化数据读取器中解析辅助连接器参数
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 服务器地址
		hub.resolve(reader);
		// 服务器超时时间
		timeout = reader.readLong();
		// 调用子类的实际数据
		resolveSuffix(reader);
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

	/** 服务器地址 **/
	private Node hub;

	/** 服务器超时时间  **/
	private long timeout;

	/** 本地最后一次刷新时间 **/
	private long refreshTime;

	/**
	 * 构造一个私有未定义的辅助连接器实例
	 */
	private Rabbet() {
		super();
		refreshTime();
	}
	
	/**
	 * 根据传入的辅助连接器实例，生成它的数据副本
	 * @param that Rabbet实例
	 */
	protected Rabbet(Rabbet that) {
		super();
		hub = that.hub;
		timeout = that.timeout;
		refreshTime = that.refreshTime;
	}

	/**
	 * 建立辅助连接器实例和指定站点类型
	 * @param siteFamily 站点类型
	 */
	protected Rabbet(byte siteFamily) {
		this();
		setFamily(siteFamily);
	}

	/**
	 * 辅助连接器刷新时间
	 */
	public void refreshTime() {
		refreshTime = System.currentTimeMillis();
	}

	/**
	 * 判断辅助连接器超时
	 * @param timeout 辅助连接器超时
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public boolean isTimeout(long timeout) {
		return System.currentTimeMillis() - refreshTime >= timeout;
	}

	/**
	 * 设置站点类型
	 * @param family 站点类型
	 */
	public void setFamily(byte family) {
		if (hub == null) {
			hub = new Node(family);
		} else {
			hub.setFamily(family);
		}
	}

	/**
	 * 返回站点类型
	 * @return 站点类型
	 */
	public final byte getFamily() {
		if (hub == null) {
			return 0;
		}
		return hub.getFamily();
	}

	/**
	 * 判断是TOP站点
	 * @return 返回真或者假
	 */
	public boolean isTop() {
		return hub.isTop();
	}

	/**
	 * 判断是WATCH站点
	 * @return 返回真或者假
	 */
	public boolean isWatch() {
		return hub.isWatch();
	}

	/**
	 * 判断是HOME站点
	 * @return 返回真或者假
	 */
	public boolean isHome() {
		return hub.isHome();
	}

	/**
	 * 判断是LOG站点
	 * @return 返回真或者假
	 */
	public boolean isLog() {
		return hub.isLog();
	}

	/**
	 * 判断是FRONT站点
	 * @return 返回真或者假
	 */
	public boolean isFront() {
		return hub.isFront();
	}

	/**
	 * 判断是CALL站点
	 * @return 返回真或者假
	 */
	public boolean isCall() {
		return hub.isCall();
	}

	/**
	 * 判断是DATA站点
	 * @return 返回真或者假
	 */
	public boolean isData() {
		return hub.isData();
	}

	/**
	 * 判断是WORK站点
	 * @return 返回真或者假
	 */
	public boolean isWork() {
		return hub.isWork();
	}

	/**
	 * 判断是BUILD站点
	 * @return 返回真或者假
	 */
	public boolean isBuild() {
		return hub.isBuild();
	}

	/**
	 * 设置站点级别(主站点/从站点)
	 * @param who 站点级别
	 */
	public void setRank(byte who) {
		hub.setRank(who);
	}

	/**
	 * 返回站点级别
	 * @return 站点级别
	 */
	public byte getRank() {
		return hub.getRank();
	}

	/**
	 * 没有定义站点级别
	 * @return 返回真或者假
	 */
	public boolean isNoneRank() {
		return hub.isNoneRank();
	}

	/**
	 * 判断是主站点
	 * @return 返回真或者假
	 */
	public boolean isMaster() {
		return hub.isMaster();
	}

	/**
	 * 判断是从站点
	 * @return 返回真或者假
	 */
	public boolean isSlave() {
		return hub.isSlave();
	}

	/**
	 * 返回网络地址
	 * @return InetAddress实例
	 */
	public InetAddress getInetAddress() {
		return hub.getInetAddress();
	}

	/**
	 * 设置网络地址
	 * @param address InetAddress实例
	 */
	public void setInetAddress(InetAddress address) {
		hub.setInetAddress(address);
	}

	/**
	 * 返回网络地址
	 * @return Address实例
	 */
	public Address getAddress() {
		return hub.getAddress();
	}

	/**
	 * 设置网络地址
	 * @param e Address实例
	 */
	public void setAddress(Address e) {
		hub.setAddress(e);
	}

	/**
	 * 返回TCP端口号
	 * @return TCP端口号
	 */
	public int getTCPort() {
		return hub.getTCPort();
	}

	/**
	 * 返回UDP端口号
	 * @return UDP端口号
	 */
	public int getUDPort() {
		return hub.getUDPort();
	}

	/**
	 * 设置站点主机地址
	 * @param e SiteHost实例
	 */
	public void setHost(SiteHost e) {
		hub.setHost(e);
	}

	/**
	 * 返回节点主机地址
	 * @return SiteHost实例
	 */
	public SiteHost getHost() {
		return hub.getHost();
	}

	/**
	 * 设置服务器地址。如果是子类是网关站点，这个地址属于内网地址。
	 * @param e 服务器地址
	 */
	public void setHub(Node e) {
		Laxkit.nullabled(e);

		if (e.getFamily() != getFamily()) {
			throw new IllegalValueException("cannot match %d:%d", e.getFamily(), getFamily());
		}
		hub = e.duplicate();
	}

	/**
	 * 返回服务器地址
	 * @return Node实例
	 */
	public Node getHub() {
		return hub;
	}

	/**
	 * 设置服务器超时时间。单位：毫秒
	 * @param ms 毫秒为单位的超时时间
	 */
	public void setHubTimeout(long ms) {
		if (ms > 0) {
			timeout = ms;
		}
	}

	/**
	 * 返回服务器超时时间。单位：毫秒
	 * @return 超时时间
	 */
	public long getHubTimeout() {
		return timeout;
	}

	/**
	 * 判断站点超时
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public boolean isHubTimeout() {
		return isTimeout(timeout);
	}

	/**
	 * 判断站点达到最大超时。最大超时时间是普通超时时间的三倍
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public boolean isMaxHubTimeout() {
		return isTimeout(timeout * 3);
	}

	/**
	 * 判断站点是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || !(that instanceof Rabbet)) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Rabbet) that) == 0;
	}

	/**
	 * 返回当前站点的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hub.hashCode();
	}

	/**
	 * 站点排序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Rabbet that) {
		// 排序时空对象在前面
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(hub, that.hub);
	}

	/**
	 * 调用子类接口，克隆一个它的站点参数副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 返回站点的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return hub.toString();
	}

	/**
	 * 将站点参数写入可类化存储器后，转为字节数组输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从传入的字节数组中解析站点参数，返回读取的字节长度
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 返回解析长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader); 
	}

	/**
	 * 子类生成一个属于自己的数据副本
	 * @return Rabbet子类实例
	 */
	public abstract Rabbet duplicate();

	/**
	 * Rabbet子类将私有参数写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * Rabbet子类从可类化数据读取器中解析属于自己的私有参数
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);	

}