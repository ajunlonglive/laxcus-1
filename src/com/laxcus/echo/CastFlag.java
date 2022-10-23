/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * 异步通信标识。<br>
 * 
 * 被包含在EchoVisit.cast的EchoHead中，投递给服务端，校验服务器端地址，和根据客户机地址判断是否加密。
 * 
 * @author scott.liang
 * @version 1.0 1/2/2018
 * @since laxcus 1.0
 */
public final class CastFlag implements Classable, Serializable, Cloneable, Comparable<CastFlag> {

	private static final long serialVersionUID = -4605739963136875058L;

	/** 服务器地址（接收数据地址） **/
	private Address server;

	/** 来源站点类型 **/
	private byte clientFamily;
	
	/** 客户机SOCKET地址（发送数据地址） **/
	private SocketHost client;

	/** 异步通信码 **/
	private CastCode code;

	/** 辅助信息。用户自定义自解释，不参与唯一性判断。 **/
	private EchoHelp help;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		server = null;
		client = null;
		code = null;
		help = null;
	}

	/**
	 * 根据传入实例生成它的数据副本
	 * @param that RegulateTime实例
	 */
	private CastFlag(CastFlag that) {
		this();
		server = that.server.duplicate();
		clientFamily = that.clientFamily;
		client = that.client.duplicate();
		code = that.code.duplicate();
		// 辅助信息副本
		if (that.help != null) {
			help = that.help.duplicate();
		}
	}

	/**
	 * 构造默认的异步通信标识
	 */
	private CastFlag() {
		super();
		clientFamily = SiteTag.NONE;
	}

	/**
	 * 构造异步通信标识，指定服务器地址（接收数据地址）
	 * @param server 服务器端地址
	 * @param clientFamily 客户端站点类型
	 * @param client 客户机地址（ReplyDispatcher主机地址）
	 * @param code 异步通信码
	 */
	public CastFlag(Address server, byte clientFamily, SocketHost client, CastCode code) {
		this();
		setServer(server);
		setClientFamily(clientFamily);
		setClientHost(client);
		setCode(code);
	}
	
	/**
	 * 从可类化数据读取器中解析异步通信标识
	 * @param reader 可类化数据读取器
	 */
	public CastFlag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 把字节数组解析成解析异步通信标识
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 */
	public CastFlag(byte[] b, int off, int len) {
		this(new ClassReader(b, off, len));
	}

	/**
	 * 把字节数组解析成解析异步通信标识
	 * @param b 字节数组
	 */
	public CastFlag(byte[] b) {
		this(b, 0, b.length);
	}

	/**
	 * 设置生成站点类型
	 * @param who
	 */
	public void setClientFamily(byte who) {
		clientFamily = who;
	}

	/**
	 * 返回生成站点类型
	 * @return 站点类型
	 */
	public byte getClientFamily() {
		return clientFamily;
	}

	/**
	 * 设置服务器地址（接收数据地址）
	 * @param e Address实例
	 */
	public void setServer(Address e) {
		Laxkit.nullabled(e);
		server = e;
	}

	/**
	 * 返回服务器地址（接收数据地址）
	 * @return Address实例
	 */
	public Address getServer() {
		return server;
	}
	
	/**
	 * 设置客户机地址（是准备发送数据的主机地址）<br>
	 * 注意，是ReplyDispatcher主机的内网或者公网地址！！！
	 * 
	 * @param e SocketHost实例
	 */
	public void setClientHost(SocketHost e) {
		Laxkit.nullabled(e);
		client = e;
	}

	/**
	 * 返回客户机地址（是准备发送数据的主机地址）
	 * 注意，是ReplyDispatcher主机的内网或者公网地址！！！
	 * 
	 * @return SocketHost实例
	 */
	public SocketHost getClientHost() {
		return client;
	}

	/**
	 * 返回客户机地址（发送数据地址）
	 * @return Address实例
	 */
	public Address getClient() {
		return client.getAddress();
	}

	/**
	 * 设置快速通信码
	 * @param e CastCode实例
	 */
	public void setCode(CastCode e) {
		Laxkit.nullabled(e);
		code = e;
	}

	/**
	 * 返回快速通信码
	 * @return CastCode实例
	 */
	public CastCode getCode() {
		return code;
	}

	/**
	 * 设置辅助信息，允许空值
	 * @param e 回显辅助信息
	 */
	public void setHelp(EchoHelp e) {
		help = e;
	}

	/**
	 * 返回辅助信息
	 * @return 回显辅助信息
	 */
	public EchoHelp getHelp() {
		return help;
	}

	/**
	 * 判断是指定的类实例
	 * @param clazz 类定义
	 * @return 返回真或者假
	 */
	public boolean isHelp(java.lang.Class<?> clazz) {
		if (help == null) {
			return false;
		}
		return Laxkit.isClassFrom(help, clazz);
	}

	/**
	 * 返回指定类的类实例
	 * @param <T> 类类型
	 * @param clazz 指定类
	 * @return 返回类对象实例，或者空指针
	 */
	@SuppressWarnings("unchecked")
	public <T> T getHelp(java.lang.Class<?> clazz) {
		if (help == null) {
			return null;
		}
		return (T) help;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return
	 */
	public CastFlag duplicate() {
		return new CastFlag(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CastFlag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((CastFlag) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode(){
		return server.hashCode() ^ clientFamily ^ client.hashCode() ^ code.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s # %s %s # %s", server, SiteTag.translate(clientFamily), client, code);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CastFlag that) {
		if (that == null) {
			return 1;
		}

		// 比较参数，判断完全一致！
		int ret = Laxkit.compareTo(clientFamily, that.clientFamily);
		if (ret == 0) {
			ret = Laxkit.compareTo(client, that.client);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(server, that.server);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(code, that.code);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(server);
		writer.write(clientFamily);
		writer.writeObject(client);
		writer.writeObject(code);
		writer.writeDefault(help);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		server = new Address(reader);
		clientFamily = reader.read();
		client = new SocketHost(reader);
		code = new CastCode(reader);
		help = (EchoHelp) reader.readDefault();
		return reader.getSeek() - seek;
	}

	/**
	 * 输出字节数组
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}
}