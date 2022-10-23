/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.talk;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 分布任务组件对话应答结果。
 * 只用于交互过程中，在网络间传输。子类由用户实现。
 * 
 * @author scott.liang
 * @version 1.0 6/13/2018
 * @since laxcus 1.0
 */
public final class TalkReply implements Classable, Cloneable {

	/** 发起人签名 **/
	private Siger issuer;
	
	/** 来源站点 **/
	private Node from;

	/** 原始数据，用户自己定义和解析 **/
	private byte[] primitive;

	/**
	 * 将命令参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 发起人签名
		writer.writeObject(issuer);
		writer.writeObject(from);
		// 写入原始数据
		writer.writeByteArray(primitive);
		// 返回写入的数据长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析命令参数。
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.1
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 发起人签名
		issuer = new Siger(reader);
		from = new Node(reader);
		// 读取原始数据
		primitive = reader.readByteArray();
		// 返回读取的数据长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造默认的分布任务组件对话应答结果
	 */
	private TalkReply() {
		super();
	}

	/**
	 * 构造分布任务组件对话应答结果，指定用户签名
	 * @param issuer 用户签名
	 * @param from 来源站点
	 */
	public TalkReply(Siger issuer, Node from) {
		this();
		setIssuer(issuer);
		setFrom(from);
	}

	/**
	 * 构造分布任务组件对话应答结果，指定参数
	 * 
	 * @param issuer 用户签名
	 * @param from 来源站点
	 * @param primitive 原始数据
	 */
	public TalkReply(Siger issuer, Node from, byte[] primitive) {
		this(issuer, from);
		setPrimitive(primitive);
	}
	
	/**
	 * 从可类化数据读取器中解析分布任务组件对话应答结果
	 * @param reader 可类化读取器
	 */
	public TalkReply(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成分布任务组件对话应答结果的数据副本
	 * @param that 分布任务组件对话应答结果
	 */
	private TalkReply(TalkReply that) {
		super();
		issuer = that.issuer;
		from = that.from;
		primitive = that.primitive;
	}

	/**
	 * 设置发起人签名<br>
	 * @param e Siger实例
	 */
	public void setIssuer(Siger e) {
		Laxkit.nullabled(e);
		issuer = e;
	}

	/**
	 * 返回发起人签名
	 * @return Siger实例
	 */
	public final Siger getIssuer() {
		return issuer;
	}
	
	/**
	 * 设置原始数据，用户自己定义和解析。
	 * @param b 命令原语文本
	 */
	public void setPrimitive(byte[] b) {
		primitive = b;
	}

	/**
	 * 返回原始数据，用户自己定义和解析
	 * @return 原始数据
	 */
	public byte[] getPrimitive() {
		return primitive;
	}
	
	/**
	 * 返回原始数据的字节长度
	 * @return 字节长度
	 */
	public int getPrimitiveLength() {
		return (primitive == null ? -1 : primitive.length);
	}
	
	/**
	 * 设置来源站点
	 * @param e
	 */
	public void setFrom(Node e) {
		Laxkit.nullabled(e);
		from = e;
	}

	/**
	 * 返回来源站点
	 * @return Node实例
	 */
	public Node getFrom() {
		return from;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 生成动态交互的数据副本
	 * 
	 * @return TalkReply副本
	 */
	public TalkReply duplicate() {
		return new TalkReply(this);
	}
	

}
