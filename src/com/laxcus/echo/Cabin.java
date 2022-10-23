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
import com.laxcus.util.hash.*;
import com.laxcus.util.net.*;

/**
 * 回显地址（异步通信地址）。<br><br>
 * 
 * 回显地址是执行异步操作时的通信地址。它由客户机（EchoInvoker）产生，随命令提交到到服务器节点。
 * 服务器节点完成数据处理后，通过回显地址，发送结果给客户机，从而完成一次异步数据处理过程。
 * 通过回显地址，异步通信管理池可以正确识别到每一个异步调用器，和每一个异步调用器中的子任务。<br><br>
 * 
 * 回显地址参数：<br>
 * (1) 站点地址（节点类型 + FIXP服务器监听地址）。<br>
 * (2) 回显标识（工作编号 + 子任务在集合的索引编号）。<br><br>
 * 
 * 重要说明：<br>
 * 在LAXCUS定义中，每个运行站点即是客户机又是服务器。<br><br>
 * 
 * @author scott.liang
 * @version 1.1 6/2/2015
 * @since laxcus 1.0
 */
public final class Cabin implements Classable, Serializable, Cloneable, Comparable<Cabin> {

	private static final long serialVersionUID = 3652565291139014402L;

	/** 站点地址 **/
	private Node node;

	/** 回显标识 **/
	private EchoFlag flag;
	
	/** 当前节点在内网中，通过NAT实现通信，默认是假 **/
	private boolean pock;

	/**
	 * 将回显地址配置参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 站点地址
		writer.writeObject(node);
		// 回显标识
		writer.writeObject(flag);
		// NAT标记
		writer.writeBoolean(pock);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析回显地址配置参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 站点地址
		node = new Node(reader);
		// 回显标识
		flag = new EchoFlag(reader);
		// NAT标记
		pock = reader.readBoolean();
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个私有的回显地址配置
	 */
	private Cabin() {
		super();
		pock = false;
	}

	/**
	 * 根据传入的回显地址，生成它的数据副本
	 * @param that Cabin实例
	 */
	private Cabin(Cabin that) {
		this();
		node = that.node.duplicate();
		flag = that.flag.duplicate();
		pock = that.pock;
	}

	/**
	 * 构造回显地址，指定站点地址和回显标识
	 * @param node 站点地址（FIXP地址）
	 * @param flag 回显标识
	 */
	public Cabin(Node node, EchoFlag flag) {
		this();
		setNode(node);
		setFlag(flag);
	}

	/**
	 * 构造一个回显地址，指定它的全部参数
	 * @param node 站点地址（FIXP地址）
	 * @param invokerId 调用器编号
	 * @param index 子任务在集合中的下标
	 */
	public Cabin(Node node, long invokerId, int index) {
		this(node, new EchoFlag(invokerId, index));
	}

	/**
	 * 构造一个回显地址，指定它的全部参数
	 * @param node 站点地址（FIXP地址）
	 * @param invokerId 调用器编号
	 * @param index 子任务在集合中的下标
	 * @param pock 节点在内网标记
	 */
	public Cabin(Node node, long invokerId, int index, boolean pock) {
		this(node, new EchoFlag(invokerId, index));
		setPock(pock);
	}
	
	/**
	 * 构造一个回显地址，指定它的全部参数
	 * @param family 站点属性
	 * @param host 站点监听地址
	 * @param flag 回显标识
	 */
	public Cabin(byte family, SiteHost host, EchoFlag flag) {
		this(new Node(family, host), flag);
	}

	/**
	 * 构造一个回显地址，指定它的全部参数
	 * @param family 站点属性
	 * @param host 站点监听地址
	 * @param invokerId 调用器编号
	 * @param index 子任务在集合中的下标
	 */
	public Cabin(byte family, SiteHost host, long invokerId, int index) {
		this(new Node(family, host), new EchoFlag(invokerId, index));
	}

	/**
	 * 从可类化数据读取器中解析回显地址配置参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Cabin(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从传入的字节数组中解析回显地址参数
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 */
	public Cabin(byte[] b, int off, int len) {
		this();
		resolve(b, off, len);
	}

	/**
	 * 返回站点地址
	 * 
	 * @return Address实例
	 */
	public Address getAddress() {
		return node.getAddress();
	}

	/**
	 * 根据流标识，选择一个SOCKET连接地址
	 * @param stream 数据流模式
	 * @return SocketHost实例
	 */
	public SocketHost choice(boolean stream) {
		return node.choice(stream);
	}

	/**
	 * 返回站点地址
	 * @return 站点地址
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * 设置站点地址
	 * @param e 站点地址
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);

		node = e.duplicate();
	}

	/**
	 * 返回站点类型
	 * @return 站点类型
	 */
	public byte getFamily() {
		return node.getFamily();
	}

	/**
	 * 返回回显地址的主机地址
	 * @return 主机地址
	 */
	public SiteHost getHost() {
		return node.getHost();
	}

	/**
	 * 设置回显标识
	 * @param e EchoFlag实例
	 */
	public void setFlag(EchoFlag e) {
		Laxkit.nullabled(e);

		flag = e.duplicate();
	}

	/**
	 * 返回回显标识
	 * @return EchoFlag实例
	 */
	public EchoFlag getFlag() {
		return flag;
	}

	/**
	 * 返回命令源异步调用器的编号
	 * @return 长整型，大于或者等于0。
	 */
	public long getInvokerId() {
		return flag.getInvokerId();
	}

	/**
	 * 返回命令源异步调用器的子任务编号
	 * @return 整型，大于或者等于0。
	 */
	public int getIndex() {
		return flag.getIndex();
	}
	
	/**
	 * 设置节点内网标记
	 * @param b 真或者假
	 */
	public void setPock(boolean b) {
		pock = b;
	}

	/**
	 * 判断节点在内网中
	 * @return 返回真或者假
	 */
	public boolean isPock() {
		return pock;
	}

	/**
	 * 建立一个当前回显地址的数据副本
	 * @return Cabin实例
	 */
	public Cabin duplicate() {
		return new Cabin(this);
	}

	/**
	 * 比较两个回显地址一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Cabin.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Cabin) that) == 0;
	}

	/**
	 * 返回回显地址的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return node.hashCode() ^ flag.hashCode();
	}

	/**
	 * 返回回显地址的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		//		return String.format("%s#%s#%s", (pock ? "NAT" : "NON-NAT"), flag, node);
		return String.format("%s#%s#%s", node, flag, (pock ? "NAT" : "NON-NAT"));
	}

	/**
	 * 根据当前回显地址配置，克隆一个它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个回显地址的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Cabin that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		// 比较站点地址和回显标识
		int ret = node.compareTo(that.node);
		if (ret == 0) {
			ret = flag.compareTo(that.flag);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(pock, that.pock);
		}
		return ret;
	}

	/**
	 * 回显地址生成数据流输出
	 * @return 返回字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}
	
	/**
	 * 生成回显地址的数字签名
	 * @return 数字签名
	 */
	public SHA256Hash sign() {
		byte[] b = build();
		return Laxkit.doSHA256Hash(b);
	}

	/**
	 * 从数据流中解析回显地址，返回解析的长度
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 * @return 返回解析的长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}
}