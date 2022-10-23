/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * BANK子站单元
 * 
 * @author scott.liang
 * @version 1.0 6/28/2018
 * @since laxcus 1.0
 */
public class BankSubSiteItem implements Cloneable, Serializable, Classable, Comparable<BankSubSiteItem> {

	private static final long serialVersionUID = -5198594185479982246L;

	/** BANK子站点地址 **/
	private Node site;

	/**
	 * 构造默认的BANK子站单元
	 */
	protected BankSubSiteItem() {
		super();
	}

	/**
	 * 构造BANK子站单元，指定参数
	 * @param site BANK子站点地址
	 */
	public BankSubSiteItem( Node site) {
		this();
		setSite(site);
	}

	/**
	 * 从可类化数据读取器解析BANK子站单元
	 * @param reader 可类化数据读取器
	 */
	public BankSubSiteItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成BANK子站单元的数据副本
	 * @param that BANK子站单元实例
	 */
	protected BankSubSiteItem(BankSubSiteItem that) {
		this();
		site = that.site;
	}

	/**
	 * 设置BANK子站点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回BANK子站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(BankSubSiteItem that) {
		if(that == null) {
			return 1;
		}
		return Laxkit.compareTo(site, that.site);
	}

	/**
	 * 生成BANK子站单元的数据副本
	 * @return BANK子站单元实例
	 */
	public BankSubSiteItem duplicate() {
		return new BankSubSiteItem(this);
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
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that != this) {
			return true;
		}
		// 比较一致
		return compareTo((BankSubSiteItem) that) == 0;
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
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(site);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		site = new Node(reader);
		return reader.getSeek() - seek;
	}

}