/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.forbid;

import java.io.*;

import com.laxcus.law.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 禁止操作单元。<br>
 * 
 * 这个参数用在检查和修复数据完整性和一致性，当它执行过程中，任何读写操作都被禁止。
 * 
 * @author scott.liang
 * @version 1.0 3/31/2017
 * @since laxcus 1.0
 */
public abstract class ForbidItem implements Classable, Serializable, Cloneable, Comparable<ForbidItem> {

	private static final long serialVersionUID = 593295211995076460L;

	/** 禁止操作级别 **/
	private byte rank;

	/**
	 * 构造默认的禁止操作单元
	 */
	protected ForbidItem() {
		super();
	}

	/**
	 * 构造禁止操作单元，指定禁止操作级别
	 * @param rank 禁止操作级别
	 */
	protected ForbidItem(byte rank) {
		this();
		setRank(rank);
	}

	/**
	 * 根据传入的禁止操作单元，生成它的数据副本
	 * @param that ForbidItem实例
	 */
	protected ForbidItem(ForbidItem that) {
		this();
		rank = that.rank;
	}

	/**
	 * 设置禁止操作级别
	 * @param who 禁止操作级别
	 */
	private void setRank(byte who) {
		if(!LawRank.isRank(who)) {
			throw new IllegalValueException("illegal rank:%d", who);
		}
		rank = who;
	}

	/**
	 * 返回禁止操作级别
	 * @return 禁止操作级别
	 */
	public byte getRank() {
		return rank;
	}

	/**
	 * 返回禁止操作级别的文本描述
	 * @return String
	 */
	public String getRankText() {
		return LawRank.translate(rank);
	}

	/**
	 * 判断是用户级别
	 * @return 返回真或者假
	 */
	public boolean isUserRank() {
		return LawRank.isUser(rank);
	}

	/**
	 * 判断是数据库级别
	 * @return 返回真或者假
	 */
	public boolean isSchemaRank() {
		return LawRank.isSchema(rank);
	}

	/**
	 * 判断是表级别
	 * @return 返回真或者假
	 */
	public boolean isTableRank() {
		return LawRank.isTable(rank);
	}

	/**
	 * 判断是行级别
	 * @return 返回真或者假
	 */
	public boolean isRowRank() {
		return LawRank.isRow(rank);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != this.getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		// 比较两个锁定规则参数完成一致
		return compareTo((ForbidItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return rank;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return LawRank.translate(rank);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ForbidItem that) {
		if (that == null) {
			return 1;
		}

		return Laxkit.compareTo(rank, that.rank);
	}

	/**
	 * 将站点参数写入可类化数据存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 禁止操作级别
		writer.write(rank);
		// 写入子类数据
		buildSuffix(writer);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化数据读取器中解析站点参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 禁止操作级别
		rank = reader.read();
		// 读子类数据
		resolveSuffix(reader);
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}
	
	/**
	 * 判断两相禁止操作存在冲突
	 * @param that 禁止操作单元
	 * @return 返回真或者假
	 */
	public abstract boolean conflict(ForbidItem that);

	/**
	 * 生成当前禁止操作单元的数据副本
	 * @return 禁止操作单元
	 */
	public abstract ForbidItem duplicate();

	/**
	 * 将子类数据写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中读取子类数据
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);

}