/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site;

import java.io.*;
import java.net.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.net.*;

/**
 * LAXCUS集群分布站点。<br><br>
 * 
 * 分布站点保存一个运行站点上的基本参数，这些参数属于元数据，或者称中间数据。<br>
 * “Site”是基础类，在其下还有一系列的子类定义。<br>
 * “Site”有两个参数：节点地址（Node）、更新时间。通过节点地址判断每一个站点的唯一性。<br>
 * 
 * 站点的网络地址规定：<br>
 * 1. 为保证安全，LAXCUS集群的管理和工作站点（TOP/HOME/WATCH/ARCHIVE/LOG/CALL/DATA/WORK）工作在内网里，它们的节点地址是一个内部网络地址。<br>
 * 2. 如果是网关站点（AID/CALL），分为内网/外网两部分。内网地址负责对集群内部的通信，外网地址接受任务请求。<br>
 * 3. 前端站点（FRONT SITE），这个站点由用户使用，网络地址允许是公网/内网地址的任何一种，只要能够接入集群即可。前端站点的网络地址可以由用户指定，也可以由程序在启动时选择，顺序是先公网地址后内网地址。<br>
 * 
 * @author scott.liang
 * @version 1.1 05/02/2015
 * @since laxcus 1.0
 */
public abstract class Site implements Classable, Serializable, Cloneable, Comparable<Site> {

	private static final long serialVersionUID = 3167354061879517436L;

	//	/**
	//	 * 将站点参数写入可类化数据存储器
	//	 * @since 1.1
	//	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	//	 */
	//	@Override
	//	public int build(ClassWriter writer) {
	//		int size = writer.size();
	//		// 节点
	//		writer.writeObject(node);
	//		// 瞬时记录
	//		writer.writeInstance(moment);
	//		// 将子类的参数信息写入
	//		buildSuffix(writer);
	//		// 返回写入的字节长度
	//		return writer.size() - size;
	//	}
	//
	//	/**
	//	 * 从可类化数据读取器中解析站点参数
	//	 * @since 1.1
	//	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	//	 */
	//	@Override
	//	public int resolve(ClassReader reader) {
	//		int seek = reader.getSeek();
	//		// 本地节点
	//		node.resolve(reader);
	//		// 瞬时记录
	//		moment = reader.readInstance(Moment.class);
	//		// 调用子类接口解析各站点的实际数据
	//		resolveSuffix(reader);
	//		// 返回解析的字节长度
	//		return reader.getSeek() - seek;
	//	}

	/**
	 * 将站点参数写入可类化数据存储器
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter w) {
		int size = w.size();

		ClassWriter writer = new ClassWriter();
		// 节点
		writer.writeObject(node);
		// 瞬时记录
		writer.writeInstance(moment);
		// 将子类的参数信息写入
		buildSuffix(writer);

		// 字节流写入可类化存储器
		byte[] b = writer.effuse();
		w.writeInt(b.length);
		w.write(b);

		// 返回写入的数据长度
		return w.size() - size;
	}

	/**
	 * 从可类化数据读取器中解析站点参数
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader r) {
		final int seek = r.getSeek();

		// 从可类化读取器中读取Command字节流
		int len = r.readInt();
		byte[] b = r.read(len);

		ClassReader reader = new ClassReader(b);
		// 本地节点
		node.resolve(reader);
		// 瞬时记录
		moment = reader.readInstance(Moment.class);
		// 调用子类接口解析各站点的实际数据
		resolveSuffix(reader);

		// 返回解析的字节长度
		return r.getSeek() - seek;
	}

	/** 站点地址 **/
	private Node node;

	/** 节点瞬间记录 **/
	private Moment moment;

	/** 站点更新时间 **/
	private long refreshTime;

	/**
	 * 构造一个私有未定义的站点实例
	 */
	private Site() {
		super();
		refreshTime();
	}

	/**
	 * 根据传入的站点实例，生成它的数据副本
	 * @param that Site实例
	 */
	protected Site(Site that) {
		super();
		if (that.node != null) {
			node = that.node.duplicate();
		}
		// 瞬时记录
		moment = that.moment;
		// 刷新时间
		refreshTime = that.refreshTime;
	}

	/**
	 * 建立站点实例和指定站点类型
	 * @param siteFamily 站点类型
	 */
	protected Site(byte siteFamily) {
		this();
		setFamily(siteFamily);
	}

	/**
	 * 设置节点瞬时记录，允许空指针
	 * @param e 节点瞬时记录
	 */
	public void setMoment(Moment e) {
		moment = e;
	}

	/**
	 * 返回节点瞬时记录
	 * @return 节点瞬时记录
	 */
	public Moment getMoment(){
		return moment;
	}

	/**
	 * 站点刷新时间
	 */
	public void refreshTime() {
		refreshTime = System.currentTimeMillis();
	}

	/**
	 * 判断站点超时
	 * @param timeout 站点超时
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
		if (node == null) {
			node = new Node(family);
		} else {
			node.setFamily(family);
		}
	}

	/**
	 * 返回站点类型
	 * @return 站点类型
	 */
	public final byte getFamily() {
		if (node == null) {
			return 0;
		}
		return node.getFamily();
	}

	/**
	 * 判断是TOP站点
	 * @return 返回真或者假
	 */
	public boolean isTop() {
		return node.isTop();
	}

	/**
	 * 判断是ACCOUNT站点
	 * @return 返回真或者假
	 */
	public boolean isAccount() {
		return node.isAccount();
	}

	/**
	 * 判断是HASH站点
	 * @return 返回真或者假
	 */
	public boolean isHash() {
		return node.isHash();
	}

	/**
	 * 判断是GATE站点
	 * @return 返回真或者假
	 */
	public boolean isGate() {
		return node.isGate();
	}

	/**
	 * 判断是ENTRANCE站点
	 * @return 返回真或者假
	 */
	public boolean isEntrance() {
		return node.isEntrance();
	}

	/**
	 * 判断是BANK站点
	 * @return 返回真或者假
	 */
	public boolean isBank() {
		return node.isBank();
	}

	/**
	 * 判断是WATCH站点
	 * @return 返回真或者假
	 */
	public boolean isWatch() {
		return node.isWatch();
	}

	/**
	 * 判断是HOME站点
	 * @return 返回真或者假
	 */
	public boolean isHome() {
		return node.isHome();
	}

	/**
	 * 判断是LOG站点
	 * @return 返回真或者假
	 */
	public boolean isLog() {
		return node.isLog();
	}

	/**
	 * 判断是FRONT站点
	 * @return 返回真或者假
	 */
	public boolean isFront() {
		return node.isFront();
	}

	/**
	 * 判断是CALL站点
	 * @return 返回真或者假
	 */
	public boolean isCall() {
		return node.isCall();
	}

	/**
	 * 判断是DATA站点
	 * @return 返回真或者假
	 */
	public boolean isData() {
		return node.isData();
	}

	/**
	 * 判断是WORK站点
	 * @return 返回真或者假
	 */
	public boolean isWork() {
		return node.isWork();
	}

	/**
	 * 判断是BUILD站点
	 * @return 返回真或者假
	 */
	public boolean isBuild() {
		return node.isBuild();
	}

	/**
	 * 设置站点级别(主站点/从站点)
	 * @param who
	 */
	public void setRank(byte who) {
		node.setRank(who);
	}

	/**
	 * 返回站点级别
	 * @return 返回站点级别
	 */
	public byte getRank() {
		return node.getRank();
	}

	/**
	 * 没有定义站点级别
	 * @return 返回真或者假
	 */
	public boolean isNoneRank() {
		return node.isNoneRank();
	}

	/**
	 * 判断是DATA主站点
	 * @return 返回真或者假
	 */
	public boolean isMaster() {
		return node.isMaster();
	}

	/**
	 * 判断是DATA从站点
	 * @return 返回真或者假
	 */
	public boolean isSlave() {
		return node.isSlave();
	}

	/**
	 * 判断是HUB管理节点
	 * @return 返回真或者假
	 */
	public boolean isManager() {
		return node.isManager();
	}

	/**
	 * 判断是HUB监视器节点
	 * @return 返回真或者假
	 */
	public boolean isMonitor() {
		return node.isMonitor();
	}

	/**
	 * 判断是FRONT驱动
	 * @return 返回真或者假
	 */
	public boolean isDriver() {
		return node.isDriver();
	}

	/**
	 * 判断是FRONT边缘节点
	 * @return 返回真或者假
	 */
	public boolean isEdge() {
		return node.isEdge();
	}

	/**
	 * 判断是FRONT字符控制台
	 * @return 返回真或者假
	 */
	public boolean isConsole() {
		return node.isConsole();
	}

	/**
	 * 判断是FRONT图形终端
	 * @return 返回真或者假
	 */
	public boolean isTerminal() {
		return node.isTerminal();
	}

	/**
	 * 判断是FRONT虚拟桌面 
	 * @return 返回真或者假
	 */
	public boolean isDesktop() {
		return node.isDesktop();
	}

	/**
	 * 判断是FRONT客户端应用软件
	 * @return 返回真或者假
	 */
	public boolean isApplication() {
		return node.isApplication();
	}

	/**
	 * 返回网络地址
	 * @return InetAddress实例
	 */
	public InetAddress getInetAddress() {
		return node.getInetAddress();
	}

	/**
	 * 设置网络地址
	 * @param address InetAddress实例
	 */
	public void setInetAddress(InetAddress address) {
		node.setInetAddress(address);
	}

	/**
	 * 返回网络地址
	 * @return Address实例
	 */
	public Address getAddress() {
		return node.getAddress();
	}

	/**
	 * 设置网络地址
	 * @param e Address实例
	 */
	public void setAddress(Address e) {
		node.setAddress(e);
	}

	/**
	 * 判断地址匹配。
	 * 包括对外的节点主机地址和隐网主机地址
	 * 
	 * @param address 传入的地址
	 * @return 返回真或者假
	 */
	public boolean matches(Address address) {
		return (Laxkit.compareTo(node.getHost().getAddress(), address) == 0);
	}

	/**
	 * 返回TCP端口号
	 * @return TCP端口号
	 */
	public int getTCPort() {
		return node.getTCPort();
	}

	/**
	 * 返回UDP端口号
	 * @return UDP端口号
	 */
	public int getUDPort() {
		return node.getUDPort();
	}

	/**
	 * 设置站点主机地址
	 * @param e SiteHost实例
	 */
	public void setHost(SiteHost e) {
		node.setHost(e);
	}

	/**
	 * 设置节点主机地址
	 * @param addr IP地址
	 * @param tcport TCP端口号
	 * @param udport UDP端口号
	 */
	public void setHost(Address addr, int tcport, int udport) {
		node.setHost(addr, tcport, udport);
	}

	/**
	 * 设置节点主机地址
	 * @param addr IP地址
	 * @param tcport TCP端口号
	 * @param udport UDP端口号
	 */
	public void setHost(InetAddress addr, int tcport, int udport) {
		node.setHost(addr, tcport, udport);
	}

	/**
	 * 返回节点主机地址
	 * @return SiteHost实例
	 */
	public SiteHost getHost() {
		return node.getHost();
	}

	/**
	 * 设置TCP映射端口
	 * @param port 端口号
	 */
	public void setReflectTCPort(int port) {
		if (node != null && node.getHost() != null) {
			node.getHost().setReflectTCPort(port);
		}
	}

	/**
	 * 设置UDP映射端口
	 * @param port 端口号
	 */
	public void setReflectUDPort(int port) {
		if (node != null && node.getHost() != null) {
			node.getHost().setReflectUDPort(port);
		}
	}

	/**
	 * 设置节点地址。如果是子类是网关站点，这个地址属于内网地址。
	 * @param e 节点地址
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);

		if (e.getFamily() != getFamily()) {
			throw new IllegalValueException("cannot match %d:%d", e.getFamily(), getFamily());
		}
		node = e.duplicate();
	}

	/**
	 * 返回节点地址。
	 * @return Node实例
	 */
	public Node getNode() {
		return node;
	}


	/**
	 * 判断站点是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || !(that instanceof Site)) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Site) that) == 0;
	}

	/**
	 * 返回当前站点的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return node.hashCode();
	}

	/**
	 * 站点排序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Site that) {
		// 排序时空对象在前面
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(node, that.node);
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
		return node.toString();
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
	 * @return 返回解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader); 
	}

	/**
	 * 子类生成一个属于自己的数据副本
	 * @return Site子类实例
	 */
	public abstract Site duplicate();

	/**
	 * Site子类将私有参数写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * Site子类从可类化数据读取器中解析属于自己的私有参数
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);	

}