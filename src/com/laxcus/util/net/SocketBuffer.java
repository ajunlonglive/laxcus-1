/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.net;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 套接字缓存尺寸 <br><br>
 * 
 * @author scott.liang
 * @version 1.1 09/12/2020
 * @since laxcus 1.0
 */
public final class SocketBuffer implements Classable, Markable, Cloneable, Serializable, Comparable<SocketBuffer> {

	private static final long serialVersionUID = -233156110716293939L;

	/** SOCKET发送缓存尺寸 **/
	private int send;

	/** SOCKET接收缓存尺寸 */
	private int receive;

	/**
	 * 根据传入的套接字缓存尺寸，生成它的数据副本
	 * @param that
	 */
	private SocketBuffer(SocketBuffer that) {
		this();
		send = that.send;
		receive = that.receive;
	}

	/**
	 * 构造私有和默认的套接字缓存尺寸
	 */
	private SocketBuffer() {
		super();
		send = 0;
		receive = 0;
	}

	/**
	 * 构造套接字缓存尺寸和指定它的地址参数
	 * @param receive 接收尺寸
	 * @param send 发送尺寸
	 */
	public SocketBuffer(int receive, int send) {
		this();
		this.setSend(send);
		this.setReceive(receive);
	}

	/**
	 * 从可类化数据读取器中解析套接字缓存尺寸参数
	 * @param reader 可类化数据读取器
	 */
	public SocketBuffer(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出套接字缓存尺寸
	 * @param reader 标记化读取器
	 */
	public SocketBuffer(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置接收缓存尺寸
	 * @param who 接收缓存尺寸
	 */
	public void setReceive(int who) {
		receive = who;
	}

	/**
	 * 返回地址端口
	 * @return int
	 */
	public int getReceive() {
		return receive;
	}

	/**
	 * 设置发送缓存尺寸
	 * @param who 发送缓存尺寸
	 */
	public void setSend(int who) {
		send = who;
	}

	/**
	 * 返回发送缓存尺寸
	 * @return int
	 */
	public int getSend() {
		return send;
	}

	/**
	 * 返回套接字缓存尺寸的字符串描述
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("%d:%d", send, receive);
	}

	/**
	 * 返回当前SocketBuffer对象的数据副本
	 * @return 当前SocketBuffer复本
	 */
	public SocketBuffer duplicate() {
		return new SocketBuffer(this);
	}

	/**
	 * 比较两个套接字缓存尺寸一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SocketBuffer.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((SocketBuffer) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return send ^ receive;
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
	 * 比较两个套接字缓存尺寸排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SocketBuffer that) {
		// 空对象排在前面，当前对象排在后面
		if (that == null) {
			return 1;
		}
		// 参数比较
	
		int	ret = Laxkit.compareTo(send, that.send);
		if (ret == 0) {
			ret = Laxkit.compareTo(receive, that.receive);
		}
		return ret;
	}

	/**
	 * 将套接字缓存尺寸信息输出到可类化存储器
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeInt(send);
		writer.writeInt(receive);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析套接字缓存尺寸信息
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		send = reader.readInt();
		receive = reader.readInt();
		return reader.getSeek() - seek;
	}

	/**
	 * 套接字缓存尺寸数据转换为字节数组输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter buff = new ClassWriter();
		build(buff);
		return buff.effuse();
	}

	/**
	 * 解析套接字缓存尺寸数据，返回解析长度
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