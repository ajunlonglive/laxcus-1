/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 被刷新的网络资源单元
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class RefreshResourceItem extends RefreshItem {

	private static final long serialVersionUID = 2445111839299128682L;

	/** 站点地址 **/
	private Node site;

	/**
	 * 构造默认和私有被刷新的网络资源单元
	 */
	private RefreshResourceItem() {
		super();
	}

	/**
	 * 根据传入实例，生成被刷新的网络资源单元的数据副本
	 * @param that RefreshResourceItem实例
	 */
	private RefreshResourceItem(RefreshResourceItem that) {
		super(that);
		site = that.site;
	}

	/**
	 * 构造被刷新的网络资源单元，指定用户签名和处理结果
	 * @param site 站点地址
	 * @param siger 用户签名
	 * @param successful 成功
	 */
	public RefreshResourceItem(Node site, Siger siger, boolean successful) {
		super(siger, successful);
		setSite(site);
	}

	/**
	 * 从可类化数据读取器中被刷新的网络资源单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RefreshResourceItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置站点地址，允许空值
	 * @param e Node实例
	 */
	public void setSite(Node e) {
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
	 * 生成当前实例的数据副本
	 * @return RefreshResourceItem实例
	 */
	public RefreshResourceItem duplicate() {
		return new RefreshResourceItem(this);
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
		return compareTo((RefreshResourceItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (site != null) {
			return site.hashCode() ^ super.hashCode();
		} else {
			return super.hashCode();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s#%s", getSiger(), (site != null ? site
				: "Invalid"), (isSuccessful() ? "Successful" : "Failed"));
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RefreshItem that) {
		if (that == null) {
			return 1;
		}

		// 上级比较结果
		int ret = super.compareTo(that);
		// 比较站点地址
		if (ret == 0 && getClass() == that.getClass()) {
			RefreshResourceItem e = (RefreshResourceItem) that;
			ret = Laxkit.compareTo(site, e.site);
		}
		return ret;
	}

	/**
	 * 保存参数
	 * @param writer
	 */
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInstance(site);
	}

	/**
	 * 解析参数
	 * @param reader
	 */
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		site = reader.readInstance(Node.class);
	}

}