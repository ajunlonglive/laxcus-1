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
 * 检查准备导入的数据内容。<br><br>
 * 
 * 通常在执行“IMPORT ENTITY”命令前测试。
 * 
 * 命令格式：CHECK ENTITY CONTENT 数据库.表  FROM 磁盘文件名  TYPE [CSV|TXT] CHARSET [GBK|UTF8|UTF16|UTF32] <br><br>
 * 
 * 此操作只在FRONT本地执行，判断内容正确性。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/1/2019
 * @since laxcus 1.0
 */
public class CheckEntityContent extends ExchangeEntity {

	private static final long serialVersionUID = -6061279200120383522L;

	/** 多个磁盘文件 **/
	private ArrayList<File> array = new ArrayList<File>();

	//	/** 磁盘文件名，不做可类化处理 **/
	//	private File file;
	
	/**
	 * 构造默认和私有获得检查准备导入的数据内容
	 */
	private CheckEntityContent() {
		super();
	}

	/**
	 * 从传入的获得检查准备导入的数据内容，生成它的数据副本
	 * @param that CheckEntityContent实例
	 */
	private CheckEntityContent(CheckEntityContent that) {
		super(that);
		//		file = that.file;
		array.addAll(that.array);
	}

	/**
	 * 构造获得检查准备导入的数据内容，指定数据表名
	 * @param space 数据表名
	 */
	public CheckEntityContent(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析获得检查准备导入的数据内容
	 * @param reader 可类化读取器
	 */
	public CheckEntityContent(ClassReader reader) {
		this();
		resolve(reader);
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
	
//	/**
//	 * 设置文件
//	 * @param e
//	 */
//	public void setFile(File e) {
//		file = e;
//	}
//
//	/**
//	 * 返回文件名
//	 * @return
//	 */
//	public File getFile() {
//		return file;
//	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CheckEntityContent duplicate() {
		return new CheckEntityContent(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
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
		// 保存文件名
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			File file = reader.readFile();
			add(file);
		}
	}

}