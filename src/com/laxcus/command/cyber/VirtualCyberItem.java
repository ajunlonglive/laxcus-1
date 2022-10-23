/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cyber;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 设置虚拟空间参数应答单元
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public final class VirtualCyberItem implements Serializable, Cloneable, Classable, Comparable<VirtualCyberItem> {

	private static final long serialVersionUID = -1353650592607730546L;

	/** 站点地址 **/
	private Node site;

	/** 成功标识 **/
	private boolean successful;

	/**
	 * 构造默认和私有的设置虚拟空间参数应答单元
	 */
	private VirtualCyberItem() {
		super();
		successful = false;
	}

	/**
	 * 生成设置虚拟空间参数应答单元数据副本
	 * @param that 原本
	 */
	private VirtualCyberItem(VirtualCyberItem that) {
		this();
		site = that.site.duplicate();
		successful = that.successful;
	}

	/**
	 * 构造设置虚拟空间参数应答单元，指定站点地址和成功标记
	 * @param node 站点地址
	 * @param successful 成功标记
	 */
	public VirtualCyberItem(Node node, boolean successful) {
		this();
		setSite(node);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器解析设置虚拟空间参数应答单元
	 * @param reader 可类化数据读取器
	 */
	public VirtualCyberItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置站点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 设置成功标识
	 * @param b 成功标识
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 产生数据副本
	 * @return VirtualCyberItem实例
	 */
	public VirtualCyberItem duplicate() {
		return new VirtualCyberItem(this);
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
		return String.format("%s/%s", site, (successful ? "Successful" : "Failed"));
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != VirtualCyberItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((VirtualCyberItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return site.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(VirtualCyberItem that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(site, that.site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(site);
		writer.writeBoolean(successful);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		site = new Node(reader);
		successful = reader.readBoolean();
		return reader.getSeek() - seek;
	}

}