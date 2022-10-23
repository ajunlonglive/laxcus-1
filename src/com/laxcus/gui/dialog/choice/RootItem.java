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

/**
 * 根单元，可能是目录或者磁盘
 * 
 * @author scott.liang
 * @version 1.0 6/17/2021
 * @since laxcus 1.0
 */
final class RootItem implements Comparable<RootItem> {

	/** 填充位 **/
	private int tab;

	/** 实例目录 **/
	private File file;

	/** 显示图标 **/
	private Icon icon;

	/** 显示名称 **/
	private String displayName;

	/** 描述文本 **/
	private String description;

	/**
	 * 构造默认的根单元
	 */
	public RootItem(int tab) {
		super();
		setTab(tab);
	}
	
	/**
	 * 设置显示名称
	 * @param s
	 */
	public void setDisplayName(String s) {
		if (s != null && s.trim().length() > 0) {
			displayName = s;
		}
	}

	/**
	 * 返回显示名称
	 * @return
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * 设置描述文本
	 * @param s
	 */
	public void setDescription(String s) {
		if (s != null && s.trim().length() > 0) {
			description = s;
		}
	}

	/**
	 * 返回描述文本
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 设置显示图标
	 * @param e
	 */
	public void setIcon(Icon e) {
		icon = e;
	}

	/**
	 * 返回显示图标
	 * @return
	 */
	public Icon getIcon() {
		return icon;
	}
	
	/**
	 * 设置填充位
	 * @param i
	 */
	public void setTab(int i) {
		if (i >= 0) {
			tab = i;
		}
	}

	/**
	 * 返回填充位
	 * @return
	 */
	public int getTab() {
		return tab;
	}
	
	/**
	 * 设置根目录或者磁盘
	 * @param f
	 */
	public void setFile(File f) {
		file = f;
	}

	/**
	 * 返回根目录或者磁盘
	 * @return
	 */
	public File getFile() {
		return file;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RootItem that) {
		if (that == null) {
			return 1;
		}
		return file.compareTo(that.file);
	}
	
}