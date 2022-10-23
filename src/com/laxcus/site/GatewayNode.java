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

/**
 * 网关节点。<br><br>
 * 
 * 网关节点位于LAXCUS集群的“边界”位置。是在一台计算机上，配置内网和公网两个通讯地址。<br>
 * 内网地址负责与集群内部其它站点的通讯，外网地址负责接受集群外FRONT站点的接入<br><br>
 * 
 * LAXCUS集群网关节点有两个：AID/CALL。
 * AID负责数据管理工作，如事务控制、账号管理等。
 * CALL负责数据处理工作，如数据的存储、检索，以及CONDUCT/ESTABLISH语句处理。<br>
 * 
 * @author scott.liang
 * @version 1.1 04/06/2015
 * @since laxcus 1.0
 */
public final class GatewayNode implements Serializable, Cloneable, Classable, Comparable<GatewayNode> {

	private static final long serialVersionUID = 5634351882387685716L;

	/** 内网节点地址 **/
	private Node inner;

	/** 公网节点地址 **/
	private Node outer;

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeObject(inner);
		writer.writeObject(outer);
		return writer.size() - scale;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		inner = new Node(reader);
		outer = new Node(reader);
		return reader.getSeek() - scale;
	}

	/**
	 * 构造默认和私有网关节点地址
	 */
	private GatewayNode() {
		super();
	}

	/**
	 * 根据传入的网关节点地址，生成它的数据副本
	 * @param that DoubleNode实例
	 */
	private GatewayNode(GatewayNode that) {
		this();
		inner = that.inner.duplicate();
		outer = that.outer.duplicate();
	}

	/**
	 * 构造网关节点地址，指定内网和公网地址
	 * @param inner 内网地址
	 * @param outer 公网地址
	 */
	public GatewayNode(Node inner, Node outer) {
		this();
		setPrivate(inner);
		setPublic(outer);
	}

	/**
	 * 从可类化数据读取器中解析网关节点地址参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public GatewayNode(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置内网节点地址
	 * @param e Node实例
	 */
	public void setPrivate(Node e) {
		Laxkit.nullabled(e);

		inner = e.duplicate();
	}

	/**
	 * 返回内网节点地址
	 * @return Node实例
	 */
	public Node getPrivate() {
		return inner;
	}

	/**
	 * 设置公网节点地址
	 * @param e Node实例
	 */
	public void setPublic(Node e) {
		Laxkit.nullabled(e);

		outer = e.duplicate();
	}

	/**
	 * 返回公网节点地址
	 * @return Node实例
	 */
	public Node getPublic() {
		return outer;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return DoubleNode实例
	 */
	public GatewayNode duplicate() {
		return new GatewayNode(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() == GatewayNode.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((GatewayNode) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return inner.hashCode() ^ outer.hashCode();
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
		return String.format("%s|%s", inner, outer);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GatewayNode that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(inner, that.inner);
		if (ret == 0) {
			ret = Laxkit.compareTo(outer, that.outer);
		}
		return ret;
	}

}