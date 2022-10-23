/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.net;

import java.io.*;
import java.net.*;
import java.util.regex.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.markable.*;

/**
 * 站点主机地址。是FIXP服务器的IP地址、TCP端口号、UDP端口号的组合<br>
 * 应用于所有站点之间的网络通讯。<br>
 * 
 * @author scott.liang
 * @version 1.1 04/03/2015
 * @since laxcus 1.0
 */
public final class SiteHost implements Serializable, Cloneable, Classable, Markable, Comparable<SiteHost> {

	private static final long serialVersionUID = 8002146021022839290L;

	/** SiteHost的正则表达式 */
	private final static String REGEX = "^\\s*(?i)(?:HOST)://([\\p{Graph}]+):([0-9]{1,5})_([0-9]{1,5})\\s*$";
	
	/** 节点的网络地址 **/
	private Address address;

	/** TCP端口 **/
	private int tcport;

	/** UDP端口 **/
	private int udport;

	/** TCP动态映射端口 **/
	private int reflectTcport;

	/** UDP动态映射端口 **/
	private int reflectUdport;

	/**
	 * 将站点主机地址参数写入可类化数据存储器
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeObject(address);
		writer.writeInt(tcport);
		writer.writeInt(udport);
		writer.writeInt(reflectTcport);
		writer.writeInt(reflectUdport);
		return writer.size() - size;
	}

	/**
	 * 从可类化数据读取器中解析站点主机地址参数
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		address = new Address(reader);
		tcport = reader.readInt();
		udport = reader.readInt();
		reflectTcport = reader.readInt();
		reflectUdport = reader.readInt();
		return reader.getSeek() - seek;
	}

	/**
	 * 构造默认的站点主机地址
	 */
	public SiteHost() {
		super();
		address = new Address();
		udport = tcport = 0;
		reflectTcport = reflectUdport = 0;
	}

	/**
	 * 根据传入的站点主机地址实例，生成它的数据副本
	 * @param that SiteHost实例
	 */
	private SiteHost(SiteHost that) {
		this();
		address = that.address.duplicate();
		tcport = that.tcport;
		udport = that.udport;
		reflectTcport = that.reflectTcport;
		reflectUdport = that.reflectUdport;
	}

	/**
	 * 构造站点主机地址，指定全部参数
	 * @param address IP地址
	 * @param tcport TCP端口
	 * @param udport UDP端口
	 */
	public SiteHost(InetAddress address, int tcport, int udport) {
		this();
		setInetAddress(address);
		setTCPort(tcport);
		setUDPort(udport);
	}

	/**
	 * 构造站点主机地址，指定全部参数
	 * @param address IP地址
	 * @param tcport TCP端口
	 * @param udport UDP端口
	 */
	public SiteHost(byte[] address, int tcport, int udport) throws UnknownHostException {
		this();
		setAddress(address);
		setTCPort(tcport);
		setUDPort(udport);
	}

	/**
	 * 构造站点主机地址，指定全部参数
	 * @param address IP地址
	 * @param tcport TCP端口
	 * @param udport UDP端口
	 */
	public SiteHost(Address address, int tcport, int udport) {
		this();
		setAddress(address);
		setTCPort(tcport);
		setUDPort(udport);
	}

	/**
	 * 构造站点主机地址，指定全部参数
	 * @param address IP地址的字符串描述，可以是“域名”或者“IPV4/IPV6“格式。
	 * @param tcport TCP端口
	 * @param udport UDP端口
	 */
	public SiteHost(String address, int tcport, int udport) throws UnknownHostException {
		this(InetAddress.getByName(address), tcport, udport);
	}

	/**
	 * 构造站点主机地址，用正则表达式解析传入的站点主机地址。
	 * @param input 站点主机的字符串描述，正则表达式解析。
	 */
	public SiteHost(String input) throws UnknownHostException {
		this();
		resolve(input);
	}

	/**
	 * 从可类化数据读取器中解析站点主机地址
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SiteHost(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出站点主机地址
	 * @param reader 标记化读取器
	 */
	public SiteHost(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 检查操作许可
	 * @param method 被调用的节点方法名
	 */
	private void check(String method) {
		// 安全检查
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String name = String.format("using.%s", method);
			sm.checkPermission(new SiteHostPermission(name));
		}
	}

	/**
	 * 设置TCP/IP网络地址
	 * @param e InetAddress实例
	 */
	public void setInetAddress(InetAddress e) {
		Laxkit.nullabled(e);

		// 安全检查
		check("Address");
		// 设置地址
		address.setInetAddress(e);
	}

	/**
	 * 返回TCP/IP网络地址
	 * @return InetAddress实例
	 */
	public InetAddress getInetAddress() {
		return address.getInetAddress();
	}

	/**
	 * 设置TCP/IP网络地址
	 * @param e Address实例
	 */
	public void setAddress(Address e) {
		Laxkit.nullabled(e);

		// 安全检查
		check("Address");
		// 设置地址
		address = e.duplicate();
	}

	/**
	 * 设置网络地址
	 * @param b 字节数组
	 * @throws UnknownHostException
	 */
	public void setAddress(byte[] b) throws UnknownHostException {
		if (Laxkit.isEmpty(b)) {
			throw new NullPointerException();
		}
		// 安全检查
		check("Address");
		// 设置地址
		address.setAddress(b);
	}

	/**
	 * 返回网络地址
	 * @return Address实例
	 */
	public Address getAddress() {
		return address.duplicate();
	}

	/**
	 * 返回二进制IP地址描述
	 * @return 字节数组
	 */
	public byte[] getRawAddress() {
		return address.bits();
	}

	/**
	 * 设置TCP端口
	 * 
	 * @param i TCP端口
	 */
	public void setTCPort(int who) {
		check("Port");
		// 检查有效性
		if (!SocketTag.isPort(who)) {
			throw new IllegalValueException("illegal port:%d", who);
		}
		tcport = who;
	}

	/**
	 * 返回TCP端口
	 * @return TCP端口
	 */
	public int getTCPort() {
		return tcport;
	}

	/**
	 * 设置UDP端口
	 * @param who UDP端口
	 */
	public void setUDPort(int who) {
		check("Port");
		// 检查有效性
		if (!SocketTag.isPort(who)) {
			throw new IllegalValueException("illegal port:%d", who);
		}
		udport = who;
	}

	/**
	 * 返回UDP端口
	 * @return UDP端口
	 */
	public int getUDPort() {
		return udport;
	}

	/**
	 * 设置TCP动态映射端口
	 * 
	 * @param i TCP动态映射端口
	 */
	public void setReflectTCPort(int who) {
		check("Port");
		if (SocketTag.isPort(who)) {
			reflectTcport = who;
		}
	}

	/**
	 * 返回TCP动态映射端口
	 * @return TCP动态映射端口
	 */
	public int getReflectTCPort() {
		return reflectTcport;
	}
	
	/**
	 * 判断TCP动态映射端口有效
	 * @return TCP动态映射端口
	 */
	public boolean hasReflectTCPort() {
		return reflectTcport > 0;
	}

	/**
	 * 设置UDP动态映射端口
	 * @param who UDP动态映射端口
	 */
	public void setReflectUDPort(int who) {
		check("Port");
		if (SocketTag.isPort(who)) {
			reflectUdport = who;
		}
	}

	/**
	 * 返回UDP动态映射端口
	 * @return UDP动态映射端口
	 */
	public int getReflectUDPort() {
		return reflectUdport;
	}

	/**
	 * 判断UDP动态映射端口有效
	 * @return UDP动态映射端口
	 */
	public boolean hasReflectUDPort() {
		return reflectUdport > 0;
	}
	
	/**
	 * 返回流模式套接字地址
	 * @return SocketHost实例
	 */
	public SocketHost getStreamHost() {
		SocketHost host = new SocketHost(SocketTag.TCP, address, tcport);
		host.setReflectPort(reflectTcport);
		return host;
	}

	/**
	 * 返回包模式套接字地址
	 * @return SocketHost实例
	 */
	public SocketHost getPacketHost() {
		SocketHost host = new SocketHost(SocketTag.UDP, address, udport);
		host.setReflectPort(reflectUdport);
		return host;
	}

	/**
	 * 根据流标识，选择一个SOCKET连接地址
	 * @param stream 数据流模式
	 * @return SocketHost实例
	 */
	public SocketHost choice(boolean stream) {
		if (stream) {
			return getStreamHost();
		} else {
			return getPacketHost();
		}
	}

	/**
	 * 返回当前SiteHost对象的浅层副本
	 * @return SiteHost对象
	 */
	public SiteHost duplicate() {
		return new SiteHost(this);
	}

	/**
	 * 比较两个站点主机地址一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SiteHost.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((SiteHost) that) == 0;
	}

	/**
	 * 返回站点主机地址的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return address.hashCode() ^ tcport ^ udport;
	}

	/**
	 * 比较站点主机地址排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SiteHost that) {
		// 空对象排在前面，当前对象排在后面
		if (that == null) {
			return 1;
		}

		int ret =  Laxkit.compareTo(address, that.address);
		if (ret == 0) {
			ret = Laxkit.compareTo(tcport, that.tcport);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(udport, that.udport);
		}
		// 返回比较结果
		return ret;
	}

	/**
	 * 用正则表达式解析站点主机地址
	 * @param input 站点主机语句
	 * @throws UnknownHostException
	 */
	public void resolve(String input) throws UnknownHostException {
		Pattern pattern = Pattern.compile(SiteHost.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throw new UnknownHostException("illegal site address!" + input);
		}

		// 解析IP地址
		address = new Address(matcher.group(1));
		// TCP/UDP端口号
		tcport = Integer.parseInt(matcher.group(2));
		udport = Integer.parseInt(matcher.group(3));
		// 判断端口号范围
		if (!SocketTag.isPort(tcport)) {
			throw new UnknownHostException("illegal tcp port!" + tcport);
		}
		if (!SocketTag.isPort(udport)) {
			throw new UnknownHostException("illegal udp port!" + udport);
		}
	}

	/**
	 * 站点主机地址转换为字符串描述
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("host://%s:%d_%d", address, tcport, udport);
	}

	/**
	 * 根据当前站点主机地址实例，克隆节点主机地址
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}
	
	/**
	 * 生成主机地址的数字签名
	 * @return 输出SHA256码
	 */
	public SHA256Hash sign() {
		byte[] b = build();
		return Laxkit.doSHA256Hash(b);
	}

	/**
	 * 站点数据转换为字节流输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter buff = new ClassWriter();
		build(buff);
		return buff.effuse();
	}

	/**
	 * 解析站点数据，返回解析长度
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
	 * 判断是有效的站点主机地址格式
	 * @param input 站点主机地址
	 * @return 匹配返回“真”，否则“假”。
	 */
	public static boolean validate(String input) {
		boolean success = (input != null);
		if (success) {
			Pattern pattern = Pattern.compile(SiteHost.REGEX);
			Matcher matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	} 
}