/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.limit;

import java.io.*;

import com.laxcus.law.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 故障锁定单元。<br><br>
 * 
 * 故障锁定是分布数据的“写操作”失败后产生，由FRONT站点传递给AID站点，AID站点使用预定义的限制操作单元，
 * 用LimitItem.match(FaultItem)方法判断匹配后，把FaultItem匹配的LimitItem单元保存起来，
 * 限制以后关联操作执行。直到故障修复后，才解除限制。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/26/2017
 * @since laxcus 1.0
 */
public abstract class FaultItem implements Classable, Markable, Serializable, Cloneable, Comparable<FaultItem> {

	private static final long serialVersionUID = 8349315017632169006L;

	/** 锁定级别 **/
	private byte rank;

	/**
	 * 构造默认的故障锁定单元
	 */
	protected FaultItem() {
		super();
	}

	/**
	 * 构造故障锁定单元，指定锁定级别
	 * @param rank 锁定级别
	 */
	protected FaultItem(byte rank) {
		this();
		setRank(rank);
	}

	/**
	 * 根据传入的故障锁定单元，生成它的数据副本
	 * @param that FaultItem窒
	 */
	protected FaultItem(FaultItem that) {
		this();
		rank = that.rank;
	}

	/**
	 * 设置锁定级别
	 * @param who 锁定级别
	 */
	private void setRank(byte who) {
		if(!LawRank.isRank(who)) {
			throw new IllegalValueException("illegal rank:%d", who);
		}
		rank = who;
	}

	/**
	 * 返回锁定级别
	 * @return 锁定级别
	 */
	public byte getRank() {
		return rank;
	}

	/**
	 * 返回锁定级别的文本描述
	 * @return 锁定级别的字符串
	 */
	public String getRankText() {
		return LawRank.translate(rank);
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
		return compareTo((FaultItem) that) == 0;
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
	public int compareTo(FaultItem that) {
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
		// 锁定级别
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
		// 锁定级别
		rank = reader.read();
		// 读子类数据
		resolveSuffix(reader);
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 将当前锁定规则生成一个新的数据副本
	 * @return FaultItem实例
	 */
	public abstract FaultItem duplicate();

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