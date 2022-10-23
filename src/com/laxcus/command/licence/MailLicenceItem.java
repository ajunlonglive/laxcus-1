/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.licence;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 发布许可证单元
 * 
 * @author scott.liang
 * @version 1.0 7/18/2020
 * @since laxcus 1.0
 */
public class MailLicenceItem implements Classable, Cloneable, Serializable, Comparable<MailLicenceItem> {

	private static final long serialVersionUID = 3422178698237148787L;

	/** 本地节点地址 **/
	private Node site;

	/** 成功标记 **/
	private boolean successful;

	/**
	 * 构造默认的被刷新处理单元
	 */
	protected MailLicenceItem() {
		super();
		successful = false;
	}

	/**
	 * 根据传入实例，生成发布许可证单元的数据副本
	 * @param that MailLicenceItem实例
	 */
	protected MailLicenceItem(MailLicenceItem that) {
		this();
		site = that.site;
		successful = that.successful;
	}

	/**
	 * 构造发布许可证单元，指定站点地址和成功标记
	 * @param site 节点地址
	 */
	public MailLicenceItem(Node site, boolean successful) {
		this();
		setSite(site);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中发布许可证单元
	 * @param reader 可类化数据读取器
	 */
	public MailLicenceItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置本地节点地址，不允许空值
	 * @param e 本地节点地址
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);
		site = e;
	}

	/**
	 * 返回本地节点地址
	 * @return 站点地址
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 成功标记
	 * @param b
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断是成功
	 * @return
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return PublishLicenceItem实例
	 */
	public MailLicenceItem duplicate() {
		return new MailLicenceItem(this);
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
		if (that == null || getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((MailLicenceItem ) that) == 0;
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s", site);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(MailLicenceItem that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		return Laxkit.compareTo(site, that.site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		buildSuffix(writer);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		resolveSuffix(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 保存参数到可类化写入器
	 * @param writer 可类化数据写入器
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(site);
		writer.writeBoolean(successful);
	}

	/**
	 * 从可类化读取器解析参数
	 * @param reader 可类化读取器
	 */
	protected void resolveSuffix(ClassReader reader) {
		site = new Node(reader);
		successful = reader.readBoolean();
	}
}