/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块导入集群命令。<br><br>
 * 
 * 命令格式：IMPORT ENTITY 数据库.表  FROM 磁盘文件1, 磁盘文件2,...  TYPE [CSV|TXT] CHARSET [GBK|UTF8|UTF16|UTF32] SECTION 单次读取行数 <br><br>
 * 
 * 此操作由FRONT节点发出，通过CALL站点，分发到DATA主站点。<br>
 * FRONT -> CALL -> MASTER DATA (ALL SITES)<br>
 * 
 * @author scott.liang
 * @version 1.0 5/11/2019
 * @since laxcus 1.0
 */
public class ImportEntity extends ExchangeEntity {

	private static final long serialVersionUID = -2987843714911392792L;

	/** 读取的行数 ，默认是1000 **/
	private int rows;

	/** 多个磁盘文件 **/
	private ArrayList<File> array = new ArrayList<File>();

	/**
	 * 构造默认和私有获得数据块导入集群命令
	 */
	private ImportEntity() {
		super();
		setRows(1000); // 默认1000行记录
	}

	/**
	 * 从传入的获得数据块导入集群命令，生成它的数据副本
	 * @param that ImportEntity实例
	 */
	private ImportEntity(ImportEntity that) {
		super(that);
		rows = that.rows;
		array.addAll(that.array);
	}

	/**
	 * 构造获得数据块导入集群命令，指定数据表名
	 * @param space 数据表名
	 */
	public ImportEntity(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析获得数据块导入集群命令
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public ImportEntity(ClassReader reader) {
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
	 * 保存文件
	 * @param file 磁盘文件
	 * @return 返回真或者假
	 */
	public boolean add(File file) {
		Laxkit.nullabled(file);
		if (array.contains(file)) {
			return false;
		}
		return array.add(file);
	}

	/**
	 * 输出文件
	 * @return
	 */
	public List<File> list() {
		return new ArrayList<File>(array);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ImportEntity duplicate() {
		return new ImportEntity(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(rows);
		// 全部文件
		writer.writeInt(array.size());
		for (File file : array) {
			writer.writeFile(file);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		rows = reader.readInt();
		// 保存文件名
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			File file = reader.readFile(); 
			add(file);
		}
	}
}