/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.io.File;
import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 检测本地实体文件的内容编码。<br><br>
 * 
 * 命令格式： CHECK ENTITY CHARSET 文件路径 <br><br>
 * 
 * 这是一个本地命令，只发生在FRONT节点。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/16/2019
 * @since laxcus 1.0
 */
public class CheckEntityCharset extends Command {

	private static final long serialVersionUID = -7317518215530250367L;

	/** 多个磁盘文件 **/
	private ArrayList<File> array = new ArrayList<File>();
	
//	/** 读取的文件实例  **/
//	private File file;

	/**
	 * 构造默认和私有获得检测本地实体文件的内容编码
	 */
	public CheckEntityCharset() {
		super();
	}

	/**
	 * 从传入的获得检测本地实体文件的内容编码，生成它的数据副本
	 * @param that CheckEntityCharset实例
	 */
	private CheckEntityCharset(CheckEntityCharset that) {
		super(that);
		array.addAll(that.array);
	}

//	/**
//	 * 构造获得检测本地实体文件的内容编码，指定数据表名
//	 * @param file 数据表名
//	 */
//	public CheckEntityCharset(File file) {
//		this();
//		setFile(file);
//	}

	/**
	 * 从可类化读取器中解析获得检测本地实体文件的内容编码
	 * @param reader 可类化读取器
	 */
	public CheckEntityCharset(ClassReader reader) {
		this();
		resolve(reader);
	}

//	/**
//	 * 设置文件实例
//	 * @param e
//	 */
//	public void setFile(File e) {
//		Laxkit.nullabled(e);
//		file = e;
//	}
//
//	/**
//	 * 返回文件实例 
//	 * @return
//	 */
//	public File getFile() {
//		return file;
//	}

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
	public CheckEntityCharset duplicate() {
		return new CheckEntityCharset(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.table.ProcessTable#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
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
		// 保存文件名
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			File file = reader.readFile();
			add(file);
		}
	}
}