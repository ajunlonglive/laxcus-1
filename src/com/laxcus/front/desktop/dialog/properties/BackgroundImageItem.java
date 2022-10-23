/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.properties;

import java.io.*;

import javax.swing.*;

/**
 * 背景图片单元
 * 
 * @author scott.liang
 * @version 1.0 6/17/2021
 * @since laxcus 1.0
 */
class BackgroundImageItem implements Serializable , java.lang.Comparable<BackgroundImageItem>{

	private static final long serialVersionUID = 1422585602601354183L;

	/** 文件路径 **/
	private File path;

	/** 备注 **/
	private String comment;
	
	/** 图标 **/
	private Icon icon;
	
	/** 图片文件尺寸 **/
	long length;
	/** 宽和高 **/
	int width;
	int height;

	/**
	 * 构造默认的背景图片单元
	 */
	public BackgroundImageItem() {
		super();
		this.length = 0;
		this.width = this.height = 0;
	}

	/**
	 * 构造背景图片单元
	 * @param path 文件路径
	 */
	public BackgroundImageItem(File path) {
		this();
		setPath(path);
	}

	/**
	 * 构造背景图片单元
	 * @param path 文件路径
	 * @param comment 备注
	 */
	public BackgroundImageItem(File path, String comment) {
		this(path);
		setComment(comment);
	}
	
	public void setWidth(int w) {
		width = w;
	}
	public int getWidth() {
		return width;
	}
	
	public void setHeight(int h){
		this.height = h;
	}
	public int getHeight() {
		return this.height;
	}

	/**
	 * 设置备注
	 * @param s
	 */
	public void setComment(String s) {
		comment = s;
	}

	/**
	 * 返回备注
	 * @return
	 */
	public String getComment(){
		return comment;
	}
	
	public long getLength(){
		return this.length;
	}

	/**
	 * 设置文件路径
	 * @param file
	 */
	public void setPath(File file) {
		path = file;
		if (path != null) {
			setComment(splitComment(path));
			boolean b = (path.exists() && path.isFile());
			if (b) {
				this.length = path.length();
			}
		}
	}

	/**
	 * 返回文件路径
	 * @return
	 */
	public File getPath() {
		return path;
	}

	/**
	 * 解析备注
	 * @param file
	 * @return
	 */
	private String splitComment(File file) {
		String name = file.getName();
		int last = name.lastIndexOf('.');
		if (last > 0) {
			return name.substring(0, last);
		}
		return null;
	}

	/**
	 * 解析后缀
	 * @return
	 */
	private String splitSuffix() {
		if (path == null) {
			return null;
		}
		String name = path.getName();
		int last = name.lastIndexOf('.');
		if (last > 0) {
			return name.substring(last + 1).trim();
		}
		return null;
	}

	/**
	 * 判断是没有定义任何一种
	 * @return
	 */
	public boolean isNone() {
		// 如果没有目录，是无定义
		return (path == null);
	}

	/**
	 * 判断是PNG
	 * @return
	 */
	public boolean isPNG() {
		String suffix = splitSuffix();
		return suffix != null && suffix.equalsIgnoreCase("PNG");
	}

	/**
	 * 判断是GIF
	 * @return
	 */
	public boolean isGIF() {
		String suffix = splitSuffix();
		return suffix != null && suffix.equalsIgnoreCase("GIF");
	}

	/**
	 * 判断是JPEG
	 * @return
	 */
	public boolean isJPEG() {
		String suffix = splitSuffix();
		return suffix != null
		&& (suffix.equalsIgnoreCase("JPEG") || suffix.equalsIgnoreCase("JPG"));
	}

	/**
	 * 判断是其它
	 * @return
	 */
	public boolean isOther() {
		boolean success = isNone();
		if (!success) {
			success = (isPNG() || isGIF() || isJPEG());
		}
		return !success;
	}
	
	/**
	 * 设置图标
	 * @param e
	 */
	public void setIcon(Icon e){
		icon = e;
	}
	
	/**
	 * 返回图标
	 * @return
	 */
	public Icon getIcon() {
		return icon;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(BackgroundImageItem that) {
		if (that == null) {
			return -1;
		} else if (that.path == null) {
			return -1;
		} else if (path == null) {
			return 1;
		}
		// 比较
		return path.compareTo(that.path);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (path != null) {
			return path.toString();
		}
		if (comment != null) {
			return comment;
		}
		return "";
	}

}