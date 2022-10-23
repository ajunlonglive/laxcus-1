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
 * 单个数据块导出结果
 * 
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class SingleExportEntityResult implements Classable, Serializable {

	private static final long serialVersionUID = 6835516361511289264L;

	/** 成功或者否 **/
	private boolean successful;

	/** 本地磁盘文件 **/
	private File file;

	/** 数据块编号 **/
	private long stub;
	
	/** 行数统计 **/
	private int rows;

	/**
	 * 构造默认和私有的单个数据块导出结果
	 */
	private SingleExportEntityResult() {
		super();
		successful = false;
		stub = 0;
		rows =0;
	}

	/**
	 * 构造单个数据块导出结果
	 * @param success 成功
	 * @param file 磁盘本地磁盘文件
	 * @param stub 数据块编号
	 */
	public SingleExportEntityResult(boolean success,File file, long stub, int rows) {
		super();
		setSuccessful(success);
		setFile(file);
		setStub(stub);
		setRows(rows);
	}

	/**
	 * 从可类化读取器解析单个数据块导出结果
	 * @param reader 可类化读取器
	 */
	public SingleExportEntityResult(ClassReader reader) {
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
	 * 设置本地磁盘文件
	 * @param e 文件实例
	 */
	public void setFile(File e) {
		Laxkit.nullabled(e);
		file = e;
	}

	/**
	 * 返回本地磁盘文件
	 * @return 文件实例
	 */
	public File getFile() {
		return file;
	}

	/**
	 * 设置数据块编号
	 * @param i
	 */
	public void setStub(long i) {
		stub = i;
	}

	/**
	 * 返回数据块编号
	 * @return
	 */
	public long getStub() {
		return stub;
	}

	/**
	 * 设置统计行数
	 * @param i
	 */
	public void setRows(int i) {
		rows = i;
	}

	/**
	 * 返回统计行数
	 * @return
	 */
	public int getRows() {
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
		writer.writeLong(stub);
		writer.writeInt(rows);
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
		stub = reader.readLong();
		rows = reader.readInt();
		return reader.getSeek() - seek;
	}

}