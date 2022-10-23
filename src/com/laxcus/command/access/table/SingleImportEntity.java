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
import com.laxcus.util.classable.*;

/**
 * 单个数据文件上传集群。<br><br>
 * 
 * 此操作由FRONT节点发出，通过CALL站点，分发到DATA主站点。<br>
 * FRONT -> CALL -> MASTER DATA (ALL SITES)<br>
 * 
 * @author scott.liang
 * @version 1.0 9/24/2019
 * @since laxcus 1.0
 */
public class SingleImportEntity extends ExchangeEntity {

	private static final long serialVersionUID = 830283499702569084L;

	/** 读取的行数 ，默认是1000 **/
	private int rows;

	/** 磁盘文件 **/
	private File file;

	/**
	 * 构造默认和私有获得单个数据文件上传集群
	 */
	public SingleImportEntity() {
		super();
		setRows(1000); // 默认1000行记录
	}

	/**
	 * 从传入的获得单个数据文件上传集群，生成它的数据副本
	 * @param that ImportEntity实例
	 */
	private SingleImportEntity(SingleImportEntity that) {
		super(that);
		rows = that.rows;
		file = that.file;
	}

	/**
	 * 构造获得单个数据文件上传集群，指定数据表名
	 * @param space 数据表名
	 */
	public SingleImportEntity(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析获得单个数据文件上传集群
	 * @param reader 可类化读取器
	 */
	public SingleImportEntity(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置行数
	 * @param i
	 */
	public void setRows(int i) {
		if (i > 0) {
			rows = i;
		}
	}

	/**
	 * 返回行数
	 * @return
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * 设置文件
	 * @param e
	 */
	public void setFile(File e) {
		file = e;
	}

	/**
	 * 返回文件名
	 * @return
	 */
	public File getFile() {
		return file;
	}


	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SingleImportEntity duplicate() {
		return new SingleImportEntity(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(rows);
		// 保存文件
		writer.writeFile(file);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		rows = reader.readInt();
		// 取文件名
		file = reader.readFile();
	}
}