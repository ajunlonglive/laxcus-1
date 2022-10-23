/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 节点不足单元。
 * 由TOP/HOME通知WATCH站点，某种类型的节点不存在！
 * 
 * @author scott.liang
 * @version 1.0 6/8/2018
 * @since laxcus 1.0
 */
public class SiteMissingItem implements Classable, Serializable, Cloneable, Comparable<SiteMissingItem> {

	private static final long serialVersionUID = -2916950430387106061L;

	/** 用户签名 **/
	private Siger siger;

	/** 节点类型 **/
	private byte siteFamily;

	/**
	 * 构造默认和私有的节点不足单元
	 */
	private SiteMissingItem() {
		super();
	}

	/**
	 * 生成节点不足单元的数据副本
	 * @param that SiteMissingItem实例
	 */
	private SiteMissingItem(SiteMissingItem that) {
		this();
		siger = that.siger;
		siteFamily = that.siteFamily;
	}

	/**
	 * 构造节点不足单元，指定参数
	 * @param siger 用户签名
	 * @param siteFamily 节点类型
	 */
	public SiteMissingItem(Siger siger, byte siteFamily) {
		this();
		setSiger(siger);
		setSiteFamily(siteFamily);
	}

	/**
	 * 从可类化数据读取器中解析节点不足单元
	 * @param reader 可类化数据读取器
	 */
	public SiteMissingItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户签名，允许空指针
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
	 * 设置节点类型
	 * @param who 节点类型
	 */
	public void setSiteFamily(byte who) {
		if (!SiteTag.isSite(who)) {
			throw new IllegalValueException("illega site type:%d", who);
		}
		siteFamily = who;
	}

	/**
	 * 返回节点类型。
	 * @return 节点类型
	 */
	public byte getSiteFamily(){
		return siteFamily;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return SiteMissingItem实例
	 */
	public SiteMissingItem duplicate() {
		return new SiteMissingItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SiteMissingItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SiteMissingItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return siger.hashCode() ^ siteFamily;
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
	public int compareTo(SiteMissingItem that) {
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(siger, that.siger);
		if (ret == 0) {
			ret = Laxkit.compareTo(siteFamily, that.siteFamily);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();

		writer.writeObject(siger);
		writer.write(siteFamily);

		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();

		siger = new Siger(reader);
		siteFamily = reader.read();

		return reader.getSeek() - seek;
	}

}