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
 * 套接字主机地址 <br><br>
 * 
 * 套接字主机分为TCP和UDP两种连接模式。<br>
 * 
 * @author scott.liang
 * @version 1.1 03/03/2015
 * @since laxcus 1.0
 */
public final class SocketHost implements Classable, Markable, Cloneable, Serializable, Comparable<SocketHost> {

	private static final long serialVersionUID = 5045657761929405416L;

//	/** IPv4/IPv6的正则表达式，带映射端口（映射端口是可选项）**/
//	public final static String REGEX = "^\\s*(?i)(NONE|TCP|UDP)://([\\p{Graph}]+):([0-9]{1,5})(?:&*)([0-9]*)\\s*$";
	
	/** IPv4/IPv6的正则表达式，不带映射端口（映射端口是可选项）**/
	public final static String REGEX = "^\\s*(?i)(NONE|TCP|UDP)://([\\p{Graph}]+):([0-9]{1,5})\\s*$";
	
	/** 当前套接字连接类型(TCP or UDP) */
	private byte family;

	/** SOCKT网络地址 **/
	private Address address;

	/** 套接字端口号(TCP/UDP端口) */
	private int port;

	/** 虚拟映射端口（网关端口），默认是0，无定义！ **/
	private int reflectPort;

	/**
	 * 根据传入的套接字主机地址，生成它的数据副本
	 * @param that
	 */
	private SocketHost(SocketHost that) {
		this();
		family = that.family;
		address = that.address.duplicate();
		port = that.port;
		reflectPort = that.reflectPort;
	}

	/**
	 * 构造私有和默认的套接字主机地址
	 */
	private SocketHost() {
		super();
		// 未定义
		family = -1;
		// 默认是通配符地址
		address = new Address();
		// 默认端口号
		port = 0;
		reflectPort = 0;
	}

	/**
	 * 构造套接字主机地址，和指定它的传输模式
	 * @param family
	 */
	public SocketHost(byte family) {
		this();
		setFamily(family);
	}

	/**
	 * 构造套接字主机地址并且指定它的地址参数
	 * @param family 数据传输类型
	 * @param address 网络地址
	 * @param port 通信端口
	 * @throws UnknownHostException
	 */
	public SocketHost(byte family, byte[] address, int port) throws UnknownHostException {
		this(family);
		setAddress(address);
		setPort(port);
	}

	/**
	 * 构造套接字主机地址和指定它的地址参数
	 * @param family 数据传输类型
	 * @param address 网络地址
	 * @param port 通信端口
	 */
	public SocketHost(byte family, InetAddress address, int port) {
		this(family);
		setInetAddress(address);
		setPort(port);
	}

	/**
	 * 构造套接字主机地址和指定它的地址参数
	 * @param family 数据传输类型
	 * @param address 网络地址
	 * @param port 通信端口
	 */
	public SocketHost(byte family, Address address, int port) {
		this(family);
		setAddress(address);
		setPort(port);
	}

	/**
	 * 构造套接字主机地址和指定它的地址参数。如果地址不存在，将弹出一个异常。
	 * @param family 数据传输类型
	 * @param address 网络地址，通常是一个域名
	 * @param port 通信端口
	 * @throws UnknownHostException
	 */
	public SocketHost(byte family, String address, int port) throws UnknownHostException {
		this(family, InetAddress.getByName(address), port);
	}

	/**
	 * 创建对象，使用正则表达式解析主机地址参数序列。
	 * @param input 网络地址的正则表达式格式
	 * @throws UnknownHostException
	 */
	public SocketHost(String input) throws UnknownHostException {
		this();
		resolve(input);
	}

	/**
	 * 从可类化数据读取器中解析套接字主机地址参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SocketHost(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出套接字主机地址
	 * @param reader 标记化读取器
	 */
	public SocketHost(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 根据传入的隐性主机实例，生成套接字主机实例（安全的）。<br>
	 * 构造方法中直接复制参数，回避沙箱的“安全检查”。。<br>
	 * 这个构造方法是保护类型，不允许外部调用。。<br>
	 * 
	 * @param that ShadowHost实例
	 */
	protected SocketHost(ShadowHost that){
		this();
		family = that.family;
		address = that.address.getAddress();
		port = that.port;
	}

	/**
	 * 检查操作许可，见xxx.policy中定义
	 * @param suffix 操作名称
	 */
	private void check(String suffix) {
		// 安全检查
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String name = String.format("using.%s", suffix);
			sm.checkPermission(new SocketHostPermission(name));
		}
	}

	/**
	 * 设置套接字连接类型（TCP/UDP中的任意一种）
	 * @param who 套接字类型
	 */
	private void setFamily(byte who) {
		// 安全检查
		check("Family");
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

		// 安全检查
		check("Address");
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
		// 安全检查
		check("Address");
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
	public void setAddress(Address e) {
		Laxkit.nullabled(e);

		// 安全检查
		check("Address");
		// 设置地址
		address = e.duplicate();
	}

	/**
	 * 返回网络地址
	 * @return Address实例
	 */
	public Address getAddress() {
		return address;
		// return address.duplicate();
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
		// 安全检查
		check("Port");
		// 检查有效性
		if (!SocketTag.isPort(who)) {
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
	 * 设置映射端口号
	 * @param who 端口号
	 */
	public void setReflectPort(int who) {
		// 安全检查
		check("Port");
		if (SocketTag.isPort(who)) {
			reflectPort = who;
		}
	}

	/**
	 * 返回映射端口号
	 * @return int
	 */
	public int getReflectPort() {
		return reflectPort;
	}
	
	/**
	 * 判断映射端口有效
	 * @return 返回真或者假
	 */
	public boolean hasReflectPort() {
		return reflectPort > 0;
	}

	//	/**
	//	 * 解析套接字主机格式，根据toString的输出
	//	 * @param input 套接字格式字符串
	//	 */
	//	public void resolve(String input) throws UnknownHostException {
	//		Pattern pattern = Pattern.compile(SocketHost.REGEX);
	//		Matcher matcher = pattern.matcher(input);
	//		if (!matcher.matches()) {
	//			throw new UnknownHostException("invalid socket address:" + input);
	//		}
	//
	//		// 连接类型
	//		family = SocketTag.translate(matcher.group(1));
	//		// 解析IP地址
	//		address = new Address(matcher.group(2));
	//		// 端口号
	//		port = Integer.parseInt(matcher.group(3));
	//		// 映射端口号
	//		String suffix = matcher.group(4);
	//		if (suffix.length() > 0 && ConfigParser.isInteger(suffix)) {
	//			reflectPort = Integer.parseInt(suffix);
	//		}
	//	}

	/**
	 * 解析套接字主机格式，根据toString的输出
	 * @param input 套接字格式字符串
	 */
	public void resolve(String input) throws UnknownHostException {
		Pattern pattern = Pattern.compile(SocketHost.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throw new UnknownHostException("illegal socket address:" + input);
		}

		// 连接类型
		family = SocketTag.translate(matcher.group(1));
		// 解析IP地址
		address = new Address(matcher.group(2));
		// 端口号
		port = Integer.parseInt(matcher.group(3));
		
		// 判断端口号范围
		if (!SocketTag.isPort(port)) {
			throw new UnknownHostException("illegal port!" + port);
		}
	}

	//	/**
	//	 * 返回套接字主机地址的字符串描述
	//	 * @see java.lang.Object#toString()
	//	 */
	//	public String toString() {
	//		if (reflectPort > 0) {
	//			return String.format("%s://%s:%d&%d", SocketTag.translate(family),
	//					address.toString(), port, reflectPort);
	//		} else {
	//			return String.format("%s://%s:%d", SocketTag.translate(family),
	//					address.toString(), port);
	//		}
	//	}

	/**
	 * 返回套接字主机地址的字符串描述
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("%s://%s:%d", SocketTag.translate(family),
			address.toString(), port);
	}
	
	/**
	 * 返回JAVA中的套接字主机地址描述
	 * @return InetSocketAddress实例
	 */
	public InetSocketAddress getSocketAddress() {
		return new InetSocketAddress(address.getInetAddress(), port);
	}

	/**
	 * 返回当前SocketHost对象的数据副本
	 * @return 当前SocketHost复本
	 */
	public SocketHost duplicate() {
		return new SocketHost(this);
	}

	/**
	 * 比较两个套接字主机地址一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SocketHost.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((SocketHost) that) == 0;
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
	 * 比较两个套接字主机地址排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SocketHost that) {
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
	 * 将套接字主机地址信息输出到可类化存储器
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.write(family);
		writer.writeObject(address);
		writer.writeInt(port);
		writer.writeInt(reflectPort);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析套接字主机地址信息
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		family = reader.read();
		address = new Address(reader);
		port = reader.readInt();
		reflectPort = reader.readInt();
		return reader.getSeek() - seek;
	}

	/**
	 * 套接字主机地址数据转换为字节数组输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter buff = new ClassWriter();
		build(buff);
		return buff.effuse();
	}

	/**
	 * 解析套接字主机地址数据，返回解析长度
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 解析字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}

	/**
	 * 判断是有效的套接字主机地址格式
	 * @param input 套接字主机地址
	 * @return 匹配返回“真”，否则“假”。
	 */
	public static boolean validate(String input) {
		boolean success = (input != null);
		if (success) {
			Pattern pattern = Pattern.compile(SocketHost.REGEX);
			Matcher matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	} 

}