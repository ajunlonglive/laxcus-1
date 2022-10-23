/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo;

import java.io.*;

import com.laxcus.fixp.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * 异步通信令牌。<br><br>
 * 
 * 由接收端套接字地址、异步通信标识、对称密钥三个部分组成，对称密钥是可选项。
 * 
 * 它在需求快速通信的环境中产生，在通信结束后撤销。
 * 
 * @author scott.liang
 * @version 1.0 12/31/2017
 * @since laxcus 1.0
 */
public final class CastToken implements Classable, Serializable, Cloneable, Comparable<CastToken> {

	private static final long serialVersionUID = 3652565291139014402L;
	
	/** 服务端（接收器）站点类型 **/
	private byte serverFamily;

	/** 异步接口器（REPLY MONITOR）套接字监听地址，必选项。由EchoBuffer/DoubleClient产生。 **/
	private SocketHost listener;

	/** 异步通信标识，必选项 **/
	private CastFlag flag;

	/** 对称密钥。这是可选项 **/
	private Cipher cipher;
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 服务端节点类型
		writer.write(serverFamily);
		// 接收端套接字地址
		writer.writeObject(listener);
		// 异步通信标识
		writer.writeObject(flag);
		// 对称密文
		writer.writeInstance(cipher);		
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 服务端节点类型
		serverFamily = reader.read();
		// 接收端套接字地址
		listener = new SocketHost(reader);
		// 异步通信标识
		flag = new CastFlag(reader);
		// 对称密文
		cipher = reader.readInstance(Cipher.class);
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个私有的异步通信令牌配置
	 */
	private CastToken() {
		super();
		serverFamily = SiteTag.NONE;
	}

	/**
	 * 根据传入的异步通信令牌，生成它的数据副本
	 * @param that CastFlag实例
	 */
	private CastToken(CastToken that) {
		this();
		serverFamily = that.serverFamily;
		listener = that.listener.duplicate();
		flag = that.flag.duplicate();
		if (that.cipher != null) {
			cipher = that.cipher.duplicate();
		}
	}

	/**
	 * 构造异步通信令牌，指定接收端套接字地址和异步通信标识
	 * @param listener 接收端套接字地址
	 * @param flag 异步通信标识
	 */
	public CastToken(byte serverFamily, SocketHost listener, CastFlag flag) {
		this();
		setServerFamily(serverFamily);
		setListener(listener);
		setFlag(flag);
	}

	/**
	 * 构造异步通信令牌，指定接收端套接字地址、异步通信标识、对称密文
	 * @param listener 接收端套接字地址
	 * @param flag 异步通信标识
	 * @param cipher 对称密文
	 */
	public CastToken(byte serverFamily, SocketHost listener, CastFlag flag, Cipher cipher) {
		this(serverFamily, listener, flag);
		setCipher(cipher);
	}

	/**
	 * 从可类化数据读取器中解析异步通信令牌配置参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CastToken(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从传入的字节数组中解析异步通信令牌参数
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 */
	public CastToken(byte[] b, int off, int len) {
		this();
		resolve(b, off, len);
	}

	/**
	 * 设置服务端类型
	 * @param who
	 */
	public void setServerFamily(byte who) {
		serverFamily = who;
	}

	/**
	 * 返回服务端类型
	 * @return 站点类型
	 */
	public byte getServerFamily() {
		return serverFamily;
	}

	/**
	 * 返回异步接口器（REPLY MONITOR）套接字监听地址
	 * @return 接收端套接字地址
	 */
	public SocketHost getListener() {
		return listener;
	}

	/**
	 * 设置异步接口器（REPLY MONITOR）套接字监听地址，不允许空指针
	 * @param e 接收端套接字地址
	 */
	public void setListener(SocketHost e) {
		Laxkit.nullabled(e);
		listener = e;
	}

	/**
	 * 设置异步通信标识，不允许空指针
	 * @param e 异步通信标识
	 */
	public void setFlag(CastFlag e) {
		Laxkit.nullabled(e);
		flag = e;
	}

	/**
	 * 返回异步通信标识
	 * @return 异步通信标识
	 */
	public CastFlag getFlag() {
		return flag;
	}

	/**
	 * 设置对称密文
	 * @param e Cipher实例
	 */
	public void setCipher(Cipher e) {
		cipher = e;
	}

	/**
	 * 返回对称密文
	 * @return Cipher实例
	 */
	public Cipher getCipher() {
		return cipher;
	}

	/**
	 * 建立一个当前异步通信令牌的数据副本
	 * @return CastFlag实例
	 */
	public CastToken duplicate() {
		return new CastToken(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CastToken.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((CastToken) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return serverFamily ^ listener.hashCode() ^ flag.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s & %s", SiteTag.translate(serverFamily), listener, flag);
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
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CastToken that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		// 判断参数一致！
		int ret = Laxkit.compareTo(serverFamily, that.serverFamily);
		if (ret == 0) {
			ret = Laxkit.compareTo(listener, that.listener);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(flag, that.flag);
		}
		return ret;
	}

	/**
	 * 异步通信令牌生成数据流输出
	 * @return 返回字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从数据流中解析异步通信令牌，返回解析的长度
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 * @return 返回解析的长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		listener = null;
		flag = null;
		cipher = null;
	}
}