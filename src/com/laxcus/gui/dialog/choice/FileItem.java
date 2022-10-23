/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.choice;

import java.io.*;

import javax.swing.*;

import com.laxcus.util.*;

/**
 * 文件单元
 * 
 * @author scott.liang
 * @version 1.0 9/3/2021
 * @since laxcus 1.0
 */
final class FileItem implements Comparable<FileItem> {
	
	/** 路径 **/
	String path;
	
	/** 最后修改时间 **/
	long lastModified;
	
	/** 文件长度 **/
	long length;

	/** 名称 **/
	String name;

	/** 目录或者否 **/
	boolean directory;
	
	/** 图标 **/
	Icon icon;
	
	/** 类型描述 **/
	String typeDescription="";
	
	/**
	 * 构造文件单元
	 * @param file
	 */
	public FileItem(File file) {
		super();
		path = Laxkit.canonical(file);
		name = file.getName();
		lastModified = file.lastModified();
		length = file.length();
		directory = file.isDirectory();
	}

	/**
	 * 返回最后修改日期
	 * @return
	 */
	public long lastModified() {
		return lastModified;
	}

	/**
	 * 返回长度
	 * @return
	 */
	public long length() {
		return length;
	}
	
	public void setLength(long i) {
		length = i;
	}

	/**
	 * 返回文件路径
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 设置名称，或者是路径或者其它定义名称
	 * @param s
	 */
	public void setName(String s) {
		name = s;
	}

	/**
	 * 返回名称
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 判断是磁盘
	 * @return
	 */
	public boolean isFile() {
		return !directory;
	}

	/**
	 * 判断是目录
	 * @return
	 */
	public boolean isDirectory() {
		return directory;
	}

	/**
	 * 设置图标
	 * @param e
	 */
	public void setIcon(Icon e) {
		icon = e;
	}

	/**
	 * 返回图标
	 * @return
	 */
	public Icon getIcon() {
		return icon;
	}

	public void setTypeDescription(String s) {
		if(s !=null){
		this.typeDescription = s;
		}
	}

	public String getTypeDescription() {
		return this.typeDescription;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return path;
	}



//	public FileItem(String s, boolean b) {
//		super();
//		name = s;
//		directory = b;
//	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FileItem that) {
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(directory, that.directory);
		if (ret == 0) {
			ret = path.compareTo(that.path);
		}
		return ret;
	}
}
