/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.call;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * CALL站点单元。<br>
 * 由CALL站点地址和CALL站点成员组成的单个CALL站点信息。
 * 
 * @author scott.liang
 * @version 1.1 6/12/2015
 * @since laxcus 1.0
 */
public final class CallItem implements Serializable, Cloneable, Classable, Comparable<CallItem> {

	private static final long serialVersionUID = -6787547017503379714L;

	/** CALL站点内网地址 **/
	private Node inner;

	/** CALL外网地址 **/
	private Node outer;

	/** CALL站点成员 **/
	private CallMember member;

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeObject(inner);
		writer.writeObject(outer);
		writer.writeObject(member);
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
		member = new CallMember(reader);
		return reader.getSeek() - scale;
	}

	/**
	 * 根据CALL站点单元，建立它的浅层数据副本
	 * @param that CallItem实例
	 */
	private CallItem(CallItem that) {
		super();
		inner = that.inner.duplicate();
		outer = that.outer.duplicate();
		member = that.member.duplicate();
	}

	/**
	 * 构造默认和私有的CALL站点单元
	 */
	private CallItem() {
		super();
	}

	//	/**
	//	 * 构造CALL站点单元，指定站点地址和站点成员
	//	 * @param node CALL站点地址
	//	 * @param memeber CALL站点成员 
	//	 */
	//	public CallItem(Node inner, CallMember memeber) {
	//		this();
	//		setNode(inner);
	//		setMember(memeber);
	//	}

	/**
	 * 构造CALL站点单元，指定站点地址和站点成员
	 * @param inner CALL站点内网地址
	 * @param outer CALL站点外网地址
	 * @param memeber CALL站点成员 
	 */
	public CallItem(Node inner, Node outer, CallMember memeber) {
		this();
		setPrivate(inner);
		setPublic(outer);
		setMember(memeber);
	}

	/**
	 * 从可类化数据读取器中解析CALL站点单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CallItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	//	/**
	//	 * 返回个人站点签名
	//	 * @return Seat实例
	//	 */
	//	public Seat getSeat() {
	//		return new Seat(member.getSiger(), inner);
	//	}

	/**
	 * 设置站点地址
	 * @param e Node实例
	 */
	public void setPrivate(Node e) {
		Laxkit.nullabled(e);

		inner = e;
	}

	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getPrivate() {
		return inner;
	}

	/**
	 * 设置公网站点实例
	 * @param e
	 */
	public void setPublic(Node e) {
		Laxkit.nullabled(e);
		outer = e;
	}

	/**
	 * 设置外网站点实际
	 * @return
	 */
	public Node getPublic() {
		return outer;
	}

	/**
	 * 设置CALL站点成员实例
	 * @param e CallMember实例
	 */
	public void setMember(CallMember e) {
		Laxkit.nullabled(e);

		member = e;
	}

	/**
	 * 返回CALL站点成员实例
	 * @return CallMember实例
	 */
	public CallMember getMember() {
		return member;
	}

	/**
	 * 返回元数据拥有者
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return member.getSiger();
	}

	/**
	 * 返回当前实例的浅层数据副本
	 * @return CallItem实例
	 */
	public CallItem duplicate() {
		return new CallItem(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CallItem that) {
		if (that == null) {
			return 1;
		}
		// 比较
		int ret = Laxkit.compareTo(inner, that.inner);
		if(ret == 0) {
			ret = Laxkit.compareTo(outer, that.outer);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(member, that.member);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CallItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((CallItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return inner.hashCode() ^ outer.hashCode() ^ member.hashCode();
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
		return String.format("%s#%s@%s", inner, outer, member.getSiger());
	}
}