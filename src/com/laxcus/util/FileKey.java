/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.io.*;

import com.laxcus.util.classable.*;
import com.laxcus.util.datetime.*;

/**
 * 文件键。<br>
 * 三个参数：文件路径、文件长度、文件时间。通过这三个参数表示一个文件的唯一性。
 * 
 * @author scott.liang
 * @version 1.0 11/4/2012
 * @since laxcus 1.0
 */
public final class FileKey implements Classable, Serializable, Cloneable , Comparable<FileKey> {
	
	private static final long serialVersionUID = -4155074435058876865L;

	/** 文件路径 **/
	private String path;
	
	/** 文件长度 **/
	private long length;
	
	/** 最后修改日期 **/
	private long modified;

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 文件路径、长度、时间
		writer.writeString(path);
		writer.writeLong(length);
		writer.writeLong(modified);
		// 生成数据长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 文件路径、长度、时间
		path = reader.readString();
		length = reader.readLong();
		modified = reader.readLong();
		// 返回解析尺寸
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个空的文件键
	 */
	public FileKey() {
		super();
		path = "";
	}

	/**
	 * 根据传入实例生成它的数据副本
	 * @param that 传入实例
	 */
	private FileKey(FileKey that) {
		this();
		path = that.path;
		length = that.length;
		modified = that.modified;
	}
	
	/**
	 * 构造文件键，指定全部参数
	 * @param path 磁盘文件全路径
	 * @param length 文件长度
	 * @param modified 最后修改日期
	 */
	public FileKey(String path, long length, long modified) {
		this();
		setPath(path);
		setLength(length);
		setModified(modified);
	}

	/**
	 * 根据导入文件，构造文件键
	 * @param file 磁盘文件
	 */
	public FileKey(File file) {
		this(Laxkit.canonical(file), file.length(), file.lastModified());
	}

	/**
	 * 从可类化数据读取器中解析任务文件参数
	 * @param reader 可类化数据读取器
	 */
	public FileKey(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置文件路径
	 * @param e 文件路径
	 */
	public void setPath(String e) {
		Laxkit.nullabled(e);
		path = e;
	}
	
	/**
	 * 返回文件路径
	 * @return 文件路径
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * 返回文件实例!
	 * @return File实例
	 */
	public File getFile() {
		return new File(path);
	}
	
	/**
	 * 设置文件长度
	 * @param i 文件长度
	 */
	public void setLength(long i) {
		if (i < 0) {
			throw new IllegalValueException("illegal length:%d", i);
		}
		length = i;
	}

	/**
	 * 返回文件长度
	 * @return 文件长度
	 */
	public long getLength() {
		return length;
	}

	/**
	 * 设置文件最后修改日期
	 * @param i
	 */
	public void setModified(long i) {
		if (i < 0) {
			throw new IllegalValueException("illegal time:%d", i);
		}
		modified = i;
	}

	/**
	 * 返回文件最后修改日期
	 * @return 整数值
	 */
	public long getModified() {
		return modified;
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return FileKey实例
	 */
	public FileKey duplicate() {
		return new FileKey(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != FileKey.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((FileKey) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int)( path.hashCode() ^ length ^ modified);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s %s %d", path,
				SimpleTimestamp.format(modified), length);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FileKey that) {
		if (that == null) {
			return 1;
		}
		// 考虑到WINDOWS，忽略大小写
		int ret = Laxkit.compareTo(path, that.path, false);
		if (ret == 0) {
			ret = Laxkit.compareTo(length, that.length);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(modified, that.modified);
		}
		return ret;
	}

}