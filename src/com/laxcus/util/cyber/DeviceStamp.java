/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.cyber;

import java.io.*;

import com.laxcus.util.classable.*;

/**
 * 计算机容量。
 * 
 * @author scott.liang
 * @version 1.0 10/30/2019
 * @since laxcus 1.0
 */
public final class DeviceStamp implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = -2281112032009778301L;

	/** 最大容量 **/
	private long maxCapacity;
	
	/** 当前容量 **/
	private long realCapacity;

	/** 容量不足 **/
	private boolean missing;	
	
	/**
	 * 构造默认的计算机容量
	 */
	public DeviceStamp() {
		super();
	}
	
	/**
	 * 生成计算机容量副本
	 * @param that 计算机容量
	 */
	private DeviceStamp(DeviceStamp that) {
		this();
		maxCapacity = that.maxCapacity;
		realCapacity = that.realCapacity;
		missing = that.missing;
	}
	
	/**
	 * 从可类化读取器解析计算机容量
	 * @param reader 可类化数据读取器
	 */
	public DeviceStamp(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造计算机容量，指定参数
	 * @param maxCpacity 最大容量
	 * @param realCapacity 实际容量
	 * @param missing 容量不足
	 */
	public DeviceStamp(long maxCpacity, long realCapacity, boolean missing) {
		this();
		setMaxCapacity(maxCpacity);
		setRealCapacity(realCapacity);
		setMissing(missing);
	}

	/**
	 * 可以承载的最大容量
	 * @param more 成员数
	 */
	public void setMaxCapacity(long more) {
		maxCapacity = more;
	}

	/**
	 * 返回可以支持的最多用户数目
	 * @return 整数
	 */
	public long getMaxCapacity() {
		return maxCapacity;
	}

	/**
	 * 设置用户数目容量不足
	 * @param f 用户数目容量不足
	 */
	public void setMissing(boolean f) {
		missing = f;
	}

	/**
	 * 返回用户数目容量不足
	 * @return 浮点数
	 */
	public boolean isMissing() {
		return missing;
	}

	/**
	 * 设置实际容量，不能低于10秒
	 * @param more 成员数
	 */
	public void setRealCapacity(long more) {
		realCapacity = more;
	}

	/**
	 * 返回实际容量
	 * @return 成员数
	 */
	public long getRealCapacity() {
		return realCapacity;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d # %d # %s", maxCapacity, realCapacity, missing);
	}

	/**
	 * 生成副本
	 * @return Tok副本
	 */
	public DeviceStamp duplicate() {
		return new DeviceStamp(this);
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
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 最大容量
		writer.writeLong(maxCapacity);
		// 实际容量
		writer.writeLong(realCapacity);
		// 容量不足
		writer.writeBoolean(missing);
		// 返回写入的数据长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 最大容量
		maxCapacity = reader.readLong();
		// 实际容量
		realCapacity = reader.readLong();
		// 容量不足
		missing = reader.readBoolean();
		// 返回读取的数据长度
		return reader.getSeek() - seek;
	}
}