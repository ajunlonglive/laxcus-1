/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * FRONT用户基点。<BR>
 * 标记FRONT登录用户的签名、网关地址（GATE/CALL）、FRONT节点地址的全景信息。
 * 
 * @author scott.liang
 * @version 1.0 6/25/2020
 * @since laxcus 1.0
 */
public final class FrontSeat implements Serializable, Cloneable, Classable, Markable, Comparable<FrontSeat> {

	private static final long serialVersionUID = 5043547133009602647L;

	/** 用户签名 */
	private Siger siger;

	/** 网关地址 */
	private Node gateway;

	/** FRONT本地地址 **/
	private Node front;
	
	/** 用户名称的明文。默认是空 **/
	private String plainText;

	/**
	 * 根据传入对象生成它的数据副本
	 * @param that FrontSeat实例
	 */
	protected FrontSeat(FrontSeat that) {
		this();
		siger = that.siger.duplicate();
		gateway = that.gateway.duplicate();
		front = that.front.duplicate();
		plainText = that.plainText;
	}

	/**
	 * 构造默认的FRONT用户基点
	 */
	protected FrontSeat() {
		super();
	}

	/**
	 * 构造FRONT用户基点，指定参数
	 * @param siger 用户签名
	 * @param gateway 网关地址
	 * @param front front地址
	 */
	public FrontSeat(Siger siger, Node gateway, Node front) {
		this();
		setSiger(siger);
		setGateway(gateway);
		setFront(front);
	}

	/**
	 * 构造FRONT用户基点，指定参数
	 * @param gateway 网关地址
	 * @param siger 用户签名
	 * @param front front地址
	 */
	public FrontSeat(Node gateway, Siger siger, Node front) {
		this();
		setSiger(siger);
		setGateway(gateway);
		setFront(front);
	}
	
	/**
	 * 从可类化数据读取器中解析FRONT用户基点参数
	 * @param reader 可类化数据读取器
	 */
	public FrontSeat(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出FRONT用户基点
	 * @param reader 标记化读取器
	 */
	public FrontSeat(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置用户签名
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);

		siger = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 设置网关地址
	 * @param e Node实例
	 */
	public void setGateway(Node e) {
		Laxkit.nullabled(e);

		gateway = e;
	}

	/**
	 * 返回网关地址
	 * @return Node实例
	 */
	public Node getGateway() {
		return gateway;
	}

	/**
	 * 设置FRONT站点地址
	 * @param e Node实例
	 */
	public void setFront(Node e) {
		Laxkit.nullabled(e);

		front = e;
	}

	/**
	 * 返回FRONT站点地址
	 * @return Node实例
	 */
	public Node getFront() {
		return front;
	}

	/**
	 * 设置用户名称的明文，允许空指针
	 * @param e 明文
	 */
	public void setPlainText(String e) {
		plainText = e;
	}

	/**
	 * 返回用户名称的明文
	 * @return 字符串
	 */
	public String getPlainText() {
		return plainText;
	}

	/**
	 * 返回当前FRONT用户基点的数据副本
	 * @return 新的FrontSeat实例
	 */
	public FrontSeat duplicate() {
		return new FrontSeat(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != FrontSeat.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((FrontSeat) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return siger.hashCode() ^ gateway.hashCode() ^ front.hashCode();
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("<%s>/%s/%s", siger, gateway, front);
	}

	/*
	 * 比较两个FRONT用户基点的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FrontSeat that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(siger, that.siger);
		if (ret == 0) {
			ret = Laxkit.compareTo(gateway, that.gateway);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(front, that.front);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(siger);
		writer.writeObject(gateway);
		writer.writeObject(front);
		writer.writeString(plainText);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		siger = new Siger(reader);
		gateway = new Node(reader);
		front = new Node(reader);
		plainText = reader.readString();
		return reader.getSeek() - seek;
	}

}