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
 * CPU信息单元
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public final class CPUInfoItem implements Classable, Cloneable, Serializable {

	private static final long serialVersionUID = -6293384810197643693L;
	
	/** CPU核心编号 **/
	private int processor;
	
	/** CPU制造商 **/
	private String vendor;
	
	/** 名称 **/
	private String modelName;
	
	/** 频率 **/
	private String MHz;

	/** 缓冲尺寸 **/
	private long cacheSize;
	
	/** CPU物理数 **/
	private int physicalId;
	
	/** 单颗CPU核心数目 **/
	private int cores;

	/**
	 * 保存参数
	 * @param writer 
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(processor);
		writer.writeString(vendor);
		writer.writeString(modelName);
		writer.writeString(MHz);
		writer.writeLong(cacheSize);
		
		writer.writeInt(physicalId);
		writer.writeInt(cores);
	}

	/**
	 * 解析参数
	 * @param reader
	 */
	protected void resolveSuffix(ClassReader reader) {
		processor = reader.readInt();
		vendor = reader.readString();
		modelName = reader.readString();
		MHz = reader.readString();
		cacheSize = reader.readLong();
		
		physicalId = reader.readInt();
		cores = reader.readInt();
	}

	/**
	 * 构造默认的被刷新处理单元
	 */
	public CPUInfoItem() {
		super();
		processor = -1;
		cacheSize = 0;
		physicalId = -1;
		cores = -1;
	}

	/**
	 * 根据传入实例，生成CPU信息单元的数据副本
	 * @param that CPUInfoItem实例
	 */
	private CPUInfoItem(CPUInfoItem that) {
		this();
		processor = that.processor;
		vendor = that.vendor;
		modelName = that.modelName;
		MHz = that.MHz;
		cacheSize = that.cacheSize;
		physicalId = that.physicalId;
		cores = that.cores;
	}


	/**
	 * 从可类化数据读取器中CPU信息单元
	 * @param reader 可类化数据读取器
	 */
	public CPUInfoItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 返回核心编号
	 * @param w
	 */
	public void setProcessor(int w) {
		processor = w;
	}

	/**
	 * 设置核心编号
	 * @return
	 */
	public int getProcessor() {
		return processor;
	}

	/**
	 * 设置制造商
	 * @param s
	 */
	public void setVendor(String s) {
		vendor = s;
	}

	/**
	 * 返回制造商
	 * @return
	 */
	public String getVendor() {
		return vendor;
	}

	public void setModelName(String s) {
		modelName = s;
	}
	
	public String getModelName() {
		return modelName;
	}
	
	public void setMHz(String s) {
		MHz = s;
	}
	
	public String getMHz() {
		return MHz;
	}

	/**
	 * 设置CPU物理数
	 * @param what 毫秒
	 */
	public void setPhysicalId(int what) {
		physicalId = what;
	}

	/**
	 * 返回FIXP失效时间
	 * @return 毫秒
	 */
	public int getPhysicalId() {
		return physicalId;
	}

	/**
	 * CPU核心数目
	 * @param what
	 */
	public void setCores(int what) {
		cores = what;
	}

	/**
	 * 返回CPU核心数目
	 * @return
	 */
	public int getCores() {
		return cores;
	}

	/**
	 * 设置调用器限制时间
	 * @param ms 毫秒
	 */
	public void setCacheSize(long ms) {
		cacheSize = ms;
	}

	/**
	 * 返回调用器限制时间
	 * @return 毫秒
	 */
	public long getCacheSize() {
		return cacheSize;
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return CPUInfoItem实例
	 */
	public CPUInfoItem duplicate() {
		return new CPUInfoItem(this);
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

}