/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 被刷新的处理单元
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class RefreshItem implements Classable, Cloneable, Serializable, Comparable<RefreshItem> {

	private static final long serialVersionUID = 6380860552699821364L;

	/** 用户签名 **/
	private Siger siger;

	/** 成功标记 **/
	private boolean successful;

	/**
	 * 构造默认的被刷新处理单元
	 */
	protected RefreshItem () {
		super();
	}

	/**
	 * 根据传入实例，生成被刷新的处理单元的数据副本
	 * @param that RefreshItem实例
	 */
	protected RefreshItem (RefreshItem  that) {
		super();
		siger = that.siger;
		successful = that.successful;
	}

	/**
	 * 构造被刷新的处理单元，指定用户签名和处理结果
	 * @param siger 用户签名
	 * @param successful 成功
	 */
	public RefreshItem(Siger siger, boolean successful) {
		this();
		setSiger(siger);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中被刷新的处理单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RefreshItem (ClassReader reader) {
		this();
		resolve(reader);
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
	 * 设置成功标记
	 * @param b 成功标记
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
	 * 生成当前实例的数据副本
	 * @return RefreshItem实例
	 */
	public RefreshItem duplicate() {
		return new RefreshItem(this);
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
		if (that == null || this.getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((RefreshItem ) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return siger.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", siger, (successful ? "Successful" : "Failed"));
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RefreshItem that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		int ret = Laxkit.compareTo(siger, that.siger);
		if (ret == 0) {
			ret = Laxkit.compareTo(successful, that.successful);
		}
		return ret;
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
		writer.writeObject(siger);
		writer.writeBoolean(successful);
	}

	/**
	 * 从可类化读取器解析参数
	 * @param reader 可类化读取器
	 */
	protected void resolveSuffix(ClassReader reader) {
		siger = new Siger(reader);
		successful = reader.readBoolean();
	}
}