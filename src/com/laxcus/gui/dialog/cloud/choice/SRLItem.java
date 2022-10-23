/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.cloud.choice;

import javax.swing.*;

import com.laxcus.command.cloud.store.*;
import com.laxcus.util.*;

/**
 * 文件单元
 * 
 * @author scott.liang
 * @version 1.0 9/3/2021
 * @since laxcus 1.0
 */
final class SRLItem implements Comparable<SRLItem> {
	
	/** 路径 **/
	VPath parent;

	/** 路径 **/
	VPath path;
	
	/** 路径 **/
	SRL srl;
	
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
	String typeDescription = "";

//	/**
//	 * 构造文件单元
//	 * @param file
//	 */
//	public SRLItem(SRL srl) {
//		super();
//		path = srl; Laxkit.canonical(file);
//		name = file.getName();
//		lastModified = file.lastModified();
//		length = file.length();
//		directory = file.isDirectory();
//	}

	/**
	 * 构造文件单元
	 * @param file
	 */
	public SRLItem(VPath path) {
		super();
		setPath(path);
	}
	
	/**
	 * 返回SRL
	 * @return
	 */
	public SRL getSRL(){
		return srl;
	}
	
	/**
	 * 设置SRL路径
	 * @param e
	 */
	public void setSRL(SRL e){
		srl = e;
	}
	
	/**
	 * 设置父级虚拟路径
	 * @param e
	 */
	public void setParent(VPath e){
		parent = e;
	}

	/**
	 * 返回父级虚拟路径
	 * @return
	 */
	public VPath getParent() {
		return parent;
	}
	
	/**
	 * 设置虚拟路径
	 * @param e
	 */
	public void setPath(VPath e){
		path = e;
	}

	/**
	 * 返回虚拟路径
	 * @return
	 */
	public VPath getPath() {
		return path;
	}

	/**
	 * 设置最后修改日期
	 * @param v 日期
	 */
	public void setLastModified(long v) {
		lastModified = v;
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
	
	public void setDirectory(boolean b) {
		directory = b;
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
		if (s != null) {
			typeDescription = s;
		}
	}

	public String getTypeDescription() {
		return typeDescription;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return path.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SRLItem that) {
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(directory, that.directory);
		if (ret == 0) {
			ret = Laxkit.compareTo(lastModified, that.lastModified);
		}
		if (ret == 0) {
			ret = path.compareTo(that.path);
		}
		return ret;
	}
}
