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
import com.laxcus.util.markable.*;

/**
 * 隐性主机 <br><br>
 * 
 * 隐性主机避开沙箱安全检查，做为转换成“SocketHost”之前的过渡存在。包括TCP/UDP两种连接模式。<br>
 * 
 * @author scott.liang
 * @version 1.1 1/12/2016
 * @since laxcus 1.0
 */
public final class ShadowHost implements Classable, Markable, Cloneable, Serializable, Comparable<ShadowHost> {

	private static final long serialVersionUID = -6187204905011575623L;

	/** 当前套接字连接类型(TCP or UDP) */
	protected byte family;

	/** SOCKT网络地址 **/
	protected ShadowAddress address;

	/** 套接字端口号(TCP/UDP端口) */
	protected int port;

	/**
	 * 根据传入的隐性主机，生成它的数据副本
	 * @param that 隐性主机实例
	 */
	private ShadowHost(ShadowHost that) {
		this();
		family = that.family;
		address = that.address.duplicate();
		port = that.port;
	}

	/**
	 * 构造私有和默认的隐性主机
	 */
	private ShadowHost() {
		super();
		// 未定义
		family = -1;
		// 默认是通配符地址
		address = new ShadowAddress();
		// 默认端口号
		port = 0;
	}

	/**
	 * 构造隐性主机，和指定它的传输模式
	 * @param family 套接字主机类型
	 */
	public ShadowHost(byte family) {
		this();
		setFamily(family);
	}

	/**
	 * 构造隐性主机并且指定它的地址参数
	 * @param family 数据传输类型
	 * @param address 网络地址
	 * @param port 通信端口
	 * @throws UnknownHostException
	 */
	public ShadowHost(byte family, byte[] address, int port) throws UnknownHostException {
		this(family);
		setAddress(address);
		setPort(port);
	}

	/**
	 * 构造隐性主机和指定它的地址参数
	 * @param family 数据传输类型
	 * @param address 网络地址
	 * @param port 通信端口
	 */
	public ShadowHost(byte family, InetAddress address, int port) {
		this(family);
		setInetAddress(address);
		setPort(port);
	}

	/**
	 * 构造隐性主机和指定它的地址参数
	 * @param family 数据传输类型
	 * @param address 网络地址
	 * @param port 通信端口
	 */
	public ShadowHost(byte family, ShadowAddress address, int port) {
		this(family);
		setAddress(address);
		setPort(port);
	}

	/**
	 * 构造隐性主机和指定它的地址参数。如果地址不存在，将弹出一个异常。
	 * @param family 数据传输类型
	 * @param address 网络地址，通常是一个域名
	 * @param port 通信端口
	 * @throws UnknownHostException
	 */
	public ShadowHost(byte family, String address, int port) throws UnknownHostException {
		this(family, InetAddress.getByName(address), port);
	}

	/**
	 * 创建对象，使用正则表达式解析主机地址参数序列。
	 * @param input 网络地址的正则表达式格式
	 * @throws UnknownHostException
	 */
	public ShadowHost(String input) throws UnknownHostException {
		this();
		resolve(input);
	}

	/**
	 * 从可类化数据读取器中解析隐性主机参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ShadowHost(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出隐性主机
	 * @param reader 标记化读取器
	 */
	public ShadowHost(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置套接字连接类型（TCP/UDP中的任意一种）
	 * @param who 套接字类型
	 */
	private void setFamily(byte who) {
		// 判断有效，否则弹出异常
		if (!SocketTag.isFamily(who)) {
			throw new IllegalValueException("illegal socket family '%d'", who);
		}
		family = who;
	}

	/**
	 * 返回套接字连接类型，见SocketTag中定义
	 * @return 套接字连接类型
	 */
	public final byte getFamily() {
		return family;
	}

	/**
	 * 判断是否属于“数据流”模式
	 * @return 返回真或者假
	 */
	public boolean isStream() {
		return SocketTag.isStream(family);
	}

	/**
	 * 判断是否属于“数据包”模式
	 * @return 返回真或者假
	 */
	public boolean isPacket() {
		return SocketTag.isPacket(family);
	}

	/**
	 * 设置INTERNET网络地址
	 * @param e InetAddress实例
	 */
	public void setInetAddress(InetAddress e) {
		Laxkit.nullabled(e);
		// 设置地址
		address.setInetAddress(e);
	}

	/**
	 * 设置网络地址
	 * @param b InetAddres字节数组
	 * @throws UnknownHostException
	 */
	public void setAddress(byte[] b) throws UnknownHostException {
		if(Laxkit.isEmpty(b)) {
			throw new NullPointerException();
		}
		// 设置地址
		address.setAddress(b);
	}
	
	/**
	 * 返回INTERNET网络地址
	 * @return InetAddress实例
	 */
	public InetAddress getInetAddress() {
		return address.getInetAddress();
	}

	/**
	 * 设置网络地址
	 * @param e Address实例
	 */
	public void setAddress(ShadowAddress e) {
		Laxkit.nullabled(e);
		// 设置地址
		address = e.duplicate();
	}

	/**
	 * 返回网络地址
	 * @return Address实例
	 */
	public ShadowAddress getAddress() {
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
	 * 判断地址有效。地址不是通配符地址且端口大于0即为有效
	 * 
	 * @return 返回真或者假
	 */
	public boolean isValid() {
		return !address.isAnyLocalAddress() && port > 0;
	}

	/**
	 * 设置端口号
	 * @param who 端口号
	 */
	public void setPort(int who) {
		// 检查有效性
		if (who < 0 || who >= 0xFFFF) {
			throw new IllegalValueException("illegal port:%d", who);
		}
		port = who;
	}

	/**
	 * 返回地址端口
	 * @return int
	 */
	public int getPort() {
		return port;
	}

	/**
	 * 解析套接字主机格式，根据toString的输出
	 * @param input 套接字格式字符串
	 */
	public void resolve(String input) throws UnknownHostException {
		Pattern pattern = Pattern.compile(SocketHost.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throw new UnknownHostException("invalid socket address:" + input);
		}

		// 连接类型
		family = SocketTag.translate(matcher.group(1));
		// 解析IP地址
		address.resolve(matcher.group(2));
		// 端口号
		port = Integer.parseInt(matcher.group(3));
	}

	/**
	 * 返回隐性主机的字符串描述
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("%s://%s:%d", SocketTag.translate(family),
				address.toString(), port);
	}

	/**
	 * 返回JAVA中的隐性主机描述
	 * @return InetSocketAddress实例
	 */
	public InetSocketAddress getSocketAddress() {
		return new InetSocketAddress(address.getInetAddress(), port);
	}

	/**
	 * 返回当前SlackHost对象的数据副本
	 * @return 当前SlackHost复本
	 */
	public ShadowHost duplicate() {
		return new ShadowHost(this);
	}
	
	/**
	 * 生成安全的套接字主机地址，安全套接字主机地址接受沙箱检查。
	 * @return 套接字主机实例
	 */
	public SocketHost getSocketHost() {
		return new SocketHost(this);
	}

	/**
	 * 比较两个隐性主机一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ShadowHost.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((ShadowHost) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return family ^ address.hashCode() ^ port;
	}

	/**
	 * 克隆当前对象实例，生成它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个隐性主机排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ShadowHost that) {
		// 空对象排在前面，当前对象排在后面
		if (that == null) {
			return 1;
		}
		// 参数比较
		int ret = Laxkit.compareTo(family, that.family);
		if (ret == 0) {
			ret = Laxkit.compareTo(address, that.address);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(port, that.port);
		}
		return ret;
	}

	/**
	 * 将隐性主机信息输出到可类化存储器
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.write(family);
		writer.writeObject(address);
		writer.writeInt(port);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析隐性主机信息
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		family = reader.read();
		address = new ShadowAddress(reader);
		port = reader.readInt();
		return reader.getSeek() - seek;
	}

	/**
	 * 隐性主机数据转换为字节数组输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter buff = new ClassWriter();
		build(buff);
		return buff.effuse();
	}

	/**
	 * 解析隐性主机数据，返回解析长度
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 解析字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}

}