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
 * 数据块导出单元
 * 
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class ExportEntityItem implements Classable, Serializable, java.lang.Comparable<ExportEntityItem> {

	private static final long serialVersionUID = -961139310488708020L;

	/** 本地磁盘文件 **/
	private File file;

	/** 数据块编号 **/
	private long stub;

	/**
	 * 构造默认和私有的数据块导出单元
	 */
	private ExportEntityItem() {
		super();
		stub = 0;
	}

	/**
	 * 构造数据块导出单元
	 * @param success 成功
	 * @param file 磁盘本地磁盘文件
	 * @param stub 数据块编号
	 */
	public ExportEntityItem(File file, long stub) {
		super();
		setFile(file);
		setStub(stub);
	}

	/**
	 * 从可类化读取器解析数据块导出单元
	 * @param reader 可类化读取器
	 */
	public ExportEntityItem(ClassReader reader) {
		this();
		resolve(reader);
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
	 * @param i 数据块编号
	 */
	public void setStub(long i) {
		stub = i;
	}

	/**
	 * 返回数据块编号
	 * @return 数据块编号
	 */
	public long getStub() {
		return stub;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeFile(file);
		writer.writeLong(stub);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		file = reader.readFile();
		stub = reader.readLong();
		return reader.getSeek() - seek;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ExportEntityItem that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(stub, that.stub);
	}

}