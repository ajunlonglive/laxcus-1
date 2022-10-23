/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 单个数据文件上传结果
 * 
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class SingleImportEntityResult implements Classable, Serializable , java.lang.Comparable<SingleImportEntityResult>{

	private static final long serialVersionUID = 8779560004754962743L;

	/** 成功或者否 **/
	private boolean successful;

	/** 文件 **/
	private File file;

	/** 上传行数 **/
	private long rows;

	/**
	 * 构造默认和私有的单个数据文件上传结果
	 */
	private SingleImportEntityResult() {
		super();
		successful = false;
		rows = 0;
	}

	/**
	 * 构造单个数据文件上传结果
	 * @param success 成功
	 * @param file 磁盘文件
	 * @param rows 上传行数
	 */
	public SingleImportEntityResult(boolean success,File file, long rows) {
		super();
		setSuccessful(success);
		setFile(file);
		setRows(rows);
	}

	/**
	 * 从可类化读取器解析单个数据文件上传结果
	 * @param reader 可类化读取器
	 */
	public SingleImportEntityResult(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置上传成功
	 * @param b
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断上传成功
	 * @return 是或者否
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 设置文件
	 * @param e
	 */
	public void setFile(File e) {
		Laxkit.nullabled(e);
		file = e;
	}

	/**
	 * 返回文件
	 * @return
	 */
	public File getFile() {
		return file;
	}

	/**
	 * 设置上传行数
	 * @param i
	 */
	public void setRows(long i) {
		rows = i;
	}

	/**
	 * 返回上传行数
	 * @return
	 */
	public long getRows() {
		return rows;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeBoolean(successful);
		writer.writeFile(file);
		writer.writeLong(rows);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		successful = reader.readBoolean();
		file = reader.readFile();
		rows = reader.readLong();
		return reader.getSeek() - seek;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SingleImportEntityResult that) {
		if(that == null){
			return 1;
		}
		return Laxkit.compareTo(file, that.file);
	}
}
