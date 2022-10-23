/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import java.io.*;

import com.laxcus.law.cross.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 共享资源单元
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public final class ShareCrossItem implements Classable, Serializable, Cloneable, Comparable<ShareCrossItem> {

	private static final long serialVersionUID = 6478252712506976924L;

	/** 授权人/被授权人签名 **/
	private Siger siger;

	/** 共享资源标识 **/
	private CrossFlag flag;

	/**
	 * 构造默认的共享资源单元
	 */
	private ShareCrossItem() {
		super();
	}

	/**
	 * 生成共享资源单元的数据副本
	 * @param that 共享资源单元实例
	 */
	private ShareCrossItem(ShareCrossItem that) {
		this();
		siger = that.siger;
		flag = that.flag;
	}

	/**
	 * 构造共享资源单元，指定参数
	 * @param siger 表资源授权人/被授权人签名
	 * @param flag 共享资源标识
	 */
	public ShareCrossItem(Siger siger, CrossFlag flag) {
		this();
		setSiger(siger);
		setFlag(flag);
	}

	/**
	 * 从可类化数据读取器中解析共享资源单元
	 * @param reader 可类化数据读取器
	 */
	public ShareCrossItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置授权人/被授权人签名
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);

		siger = e;
	}

	/**
	 * 返回授权人/被授权人签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 设置共享资源标识
	 * @param e CrossFlag实例
	 */
	public void setFlag(CrossFlag e) {
		Laxkit.nullabled(e);

		flag = e;
	}

	/**
	 * 返回共享资源标识
	 * @return CrossFlag实例
	 */
	public CrossFlag getFlag() {
		return flag;
	}

	/**
	 * 生成数据副本
	 * @return ShareCrossItem实例
	 */
	public ShareCrossItem duplicate() {
		return new ShareCrossItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		// 比较两个锁定规则参数完成一致
		return compareTo((ShareCrossItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return siger.hashCode() ^ flag.hashCode();
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
	public int compareTo(ShareCrossItem that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(siger, that.siger);
		if (ret == 0) {
			ret = Laxkit.compareTo(flag, that.flag);
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
		writer.writeObject(flag);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		siger = new Siger(reader);
		flag = new CrossFlag(reader);
		return reader.getSeek() - seek;
	}

}