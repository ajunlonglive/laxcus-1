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
 * 内存信息单元
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public final class MemoryInfoItem implements Classable, Cloneable, Serializable {

	private static final long serialVersionUID = -6293384810197643693L;

	/** 全部容量 **/
	private long total;

//	/** 剩余内存空间 **/
//	private long free;

	/** 可用内存容量，包括free memory + cache + buffer **/
	private long available;
	
	private long buffers;
	
	private long cached;
	
	private long swapCached;
	
	private long swapTotal;
	
	private long swapFree;

	/**
	 * 构造默认的被刷新处理单元
	 */
	public MemoryInfoItem() {
		super();
		total = 0;
		available = 0;
		buffers = 0;
		cached = 0;
		swapTotal = 0;
		swapFree = 0;
		swapCached = 0;
	}

	/**
	 * 根据传入实例，生成内存信息单元的数据副本
	 * @param that MemoryInfoItem实例
	 */
	private MemoryInfoItem(MemoryInfoItem that) {
		super();
		total = that.total;
		available = that.available;
		buffers = that.buffers;
		cached = that.cached;
		swapTotal = that.swapTotal;
		swapFree = that.swapFree;
		swapCached = that.swapCached;
	}

	/**
	 * 从可类化数据读取器中内存信息单元
	 * @param reader 可类化数据读取器
	 */
	public MemoryInfoItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置全部内存空间
	 * @param len 容量
	 */
	public void setTotal(long len) {
		total = len;
	}

	/**
	 * 返回全部内存空间
	 * @return 容量
	 */
	public long getTotal() {
		return total;
	}
	
	/**
	 * 设置有效内存空间
	 * @param len 容量
	 */
	public void setAvailable(long len) {
		available = len;
	}

	/**
	 * 返回有效内存空间
	 * @return 容量
	 */
	public long getAvailable() {
		return available;
	}
	
//	/**
//	 * 设置剩余内存空间
//	 * @param len 容量
//	 */
//	public void setFree(long len) {
//		free = len;
//	}
//
//	/**
//	 * 返回剩余内存空间
//	 * @return 容量
//	 */
//	public long getFree() {
//		return free;
//	}

	/**
	 * 设置缓冲内存空间
	 * @param len 容量
	 */
	public void setBuffers(long len) {
		buffers = len;
	}

	/**
	 * 返回缓冲内存空间
	 * @return 容量
	 */
	public long getBuffers() {
		return buffers;
	}
	
	/**
	 * 设置缓存内存空间
	 * @param len 容量
	 */
	public void setCached(long len) {
		cached = len;
	}

	/**
	 * 返回缓存内存空间
	 * @return 容量
	 */
	public long getCached() {
		return cached;
	}
	
	/**
	 * 设置交换缓存内存空间
	 * @param len 容量
	 */
	public void setSwapTotal(long len) {
		swapTotal = len;
	}

	/**
	 * 返回交换缓存内存空间
	 * @return 容量
	 */
	public long getSwapTotal() {
		return swapTotal;
	}
	
	/**
	 * 设置剩余交换缓存内存空间
	 * @param len 容量
	 */
	public void setSwapFree(long len) {
		swapFree = len;
	}

	/**
	 * 返回剩余交换缓存内存空间
	 * @return 容量
	 */
	public long getSwapFree() {
		return swapFree;
	}
	
	/**
	 * 设置交换缓存缓存内存空间
	 * @param len 容量
	 */
	public void setSwapCached(long len) {
		swapCached = len;
	}

	/**
	 * 返回交换缓存缓存内存空间
	 * @return 容量
	 */
	public long getSwapCached() {
		return swapCached;
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return MemoryInfoItem实例
	 */
	public MemoryInfoItem duplicate() {
		return new MemoryInfoItem(this);
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
		writer.writeLong(total);
		writer.writeLong(available);
		writer.writeLong(buffers);
		writer.writeLong(cached);
		writer.writeLong(swapTotal);
		writer.writeLong(swapFree);
		writer.writeLong(swapCached);
	}

	/**
	 * 解析参数
	 * @param reader
	 */
	protected void resolveSuffix(ClassReader reader) {
		total = reader.readLong();
		available = reader.readLong();
		buffers = reader.readLong();
		cached = reader.readLong();
		swapTotal = reader.readLong();
		swapFree = reader.readLong();
		swapCached = reader.readLong();
	}

}