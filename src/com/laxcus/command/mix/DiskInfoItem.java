/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.io.*;

import com.laxcus.util.classable.*;

/**
 * 磁盘信息单元
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public final class DiskInfoItem implements Classable, Cloneable, Serializable {

	private static final long serialVersionUID = -6293384810197643693L;

	/** 最大容量 **/
	private long totalCapacity;

	/** 未使用容量 **/
	private long freeCapacity;
	
	/**
	 * 构造默认的被刷新处理单元
	 */
	public DiskInfoItem() {
		super();
		totalCapacity = 0;
		freeCapacity = 0;
	}

	/**
	 * 根据传入实例，生成磁盘信息单元的数据副本
	 * @param that DiskInfoItem实例
	 */
	private DiskInfoItem(DiskInfoItem that) {
		super();
		totalCapacity = that.totalCapacity;
		freeCapacity = that.freeCapacity;
	}

//	/**
//	 * 构造磁盘信息单元，指定站点地址和处理结果
//	 * @param node 站点地址
//	 * @param successful 成功
//	 */
//	public DiskInfoItem(Node node, boolean successful) {
//		this();
//		setSite(node);
//		setSuccessful(successful);
//	}

	/**
	 * 从可类化数据读取器中磁盘信息单元
	 * @param reader 可类化数据读取器
	 */
	public DiskInfoItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置最大容量
	 * @param what 毫秒
	 */
	public void setTotalCapacity(long what) {
		totalCapacity = what;
	}

	/**
	 * 返回FIXP失效时间
	 * @return 毫秒
	 */
	public long getTotalCapacity() {
		return totalCapacity;
	}

	/**
	 * 设置调用器限制时间
	 * @param ms 毫秒
	 */
	public void setFreeCapacity(long ms) {
		freeCapacity = ms;
	}

	/**
	 * 返回调用器限制时间
	 * @return 毫秒
	 */
	public long getFreeCapacity() {
		return freeCapacity;
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return DiskInfoItem实例
	 */
	public DiskInfoItem duplicate() {
		return new DiskInfoItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

//	/*
//	 * (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object that) {
//		if (that == null || getClass() != that.getClass()) {
//			return false;
//		} else if (that == this) {
//			return true;
//		}
//		// 比较
//		return compareTo((DiskInfoItem ) that) == 0;
//	}

//	/* (non-Javadoc)
//	 * @see java.lang.Comparable#compareTo(java.lang.Object)
//	 */
//	@Override
//	public int compareTo(DiskInfoItem that) {
//		if (that == null) {
//			return 1;
//		}
////		// 比较参数
////		int ret = Laxkit.compareTo(node, that.node);
////		if (ret == 0) {
////			ret = Laxkit.compareTo(successful, that.successful);
////		}
////		return ret;
//		
//		return 0;
//	}

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
	 * 保存参数
	 * @param writer 
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(totalCapacity);
		writer.writeLong(freeCapacity);
	}

	/**
	 * 解析参数
	 * @param reader
	 */
	protected void resolveSuffix(ClassReader reader) {
		totalCapacity = reader.readLong();
		freeCapacity = reader.readLong();
	}
}