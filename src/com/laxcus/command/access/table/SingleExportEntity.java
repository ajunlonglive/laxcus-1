/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 单个数据块导出。<br><br>
 *  
 * 此操作由FRONT节点发出，通过CALL站点，分发到DATA主站点。<br>
 * FRONT -> CALL -> MASTER DATA (ALL SITES)<br>
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class SingleExportEntity extends ExchangeEntity {

	private static final long serialVersionUID = 8695788940212524921L;

	/** 数据块编号 **/
	private long stub;
	
	/** 磁盘文件名 **/
	private String filename;

	/**
	 * 构造默认和私有单个数据块导出
	 */
	private SingleExportEntity() {
		super();
	}

	/**
	 * 从传入的单个数据块导出，生成它的数据副本
	 * @param that SingleExportEntity实例
	 */
	private SingleExportEntity(SingleExportEntity that) {
		super(that);
		stub = that.stub;
		filename = that.filename;
	}

	/**
	 * 构造单个数据块导出，指定数据表名
	 * @param space 数据表名
	 */
	public SingleExportEntity(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析单个数据块导出
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public SingleExportEntity(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据块编号
	 * @param id 数据块编号
	 */
	public void setStub(long id) {
		stub = id;
	}

	/**
	 * 返回数据块编号
	 * @return 数据块编号
	 */
	public long getStub() {
		return stub;
	}

	/**
	 * 设置文件
	 * @param e
	 */
	public void setFilename(String e) {
		Laxkit.nullabled(e);
		filename = e;
	}
	
	/**
	 * 返回文件名
	 * @return
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * 返回文件名
	 * @return
	 */
	public File getFile() {
		return (filename == null ? null : new File(filename));
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SingleExportEntity duplicate() {
		return new SingleExportEntity(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(stub);
		writer.writeString(filename);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		stub = reader.readLong();
		filename = reader.readString();
	}
}