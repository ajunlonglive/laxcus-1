/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.fast;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块快速处理单元。
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public final class FastMassItem implements Classable, Cloneable, Comparable<FastMassItem>, Serializable {
	
	private static final long serialVersionUID = -6550877008481175733L;

	/** 节点地址  **/
	private Node site;
	
	/** 处理成功 **/
	private boolean successful;
	
	/**
	 * 构造默认和私有的数据块快速处理单元
	 */
	private FastMassItem(){
		super();
	}
	
	/**
	 * 根据传入的实例，生成它的数据副本
	 * @param that FastMassItem实例
	 */
	private FastMassItem(FastMassItem that){
		this();
		site = that.site;
		successful = that.successful;
	}
	
	/**
	 * 构造数据块快速处理单元，指定全部参数
	 * @param site 站点地址
	 * @param successful 成功标记
	 */
	public FastMassItem(Node site, boolean successful) {
		this();
		setSite(site);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析数据块快速处理单元参数
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public FastMassItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置节点地址，不允许空指针
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回节点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 设置成功状态
	 * @param b 成功状态
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断是成功状态
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return successful;
	}
	
	/**
	 * 返回当前实例的数据副本
	 * @return FastMassItem实例
	 */
	public FastMassItem duplicate() {
		return new FastMassItem(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != this.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((FastMassItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return site.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FastMassItem that) {
		if (that == null) {
			return 1;
		}
		return site.compareTo(that.site);
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
