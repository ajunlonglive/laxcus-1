/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.io.*;
import java.util.*;

/**
 * 磁盘文件检查过滤器
 * 
 * @author scott.liang
 * @version 1.0 8/2/2020
 * @since laxcus 1.0
 */
public class DiskFileFilter extends javax.swing.filechooser.FileFilter {

	/** 全部 **/
	private final static String REGEX_ALL = "^\\s*([\\w\\W]+)\\.(?i)(\\*)\\s*$";

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*([\\w\\W]+?)\\.(?i)(%s)\\s*$";
	
	private boolean all;
	
	private ArrayList<String> array = new ArrayList<String>();
	
	/** 描述 **/
	private String description;
	
	/**
	 * 构造磁盘文件检查过滤器
	 * @param description 描述文本
	 * @param suffix 文件后缀，多个之间用逗号分开
	 * @param all 全部文件
	 */
	public DiskFileFilter(String description, String suffix, boolean all) {
		super();
		setAll(all);
		setDescription(description);
		split(suffix);
	}
	
	/**
	 * 构造磁盘文件检查过滤器
	 * @param description 描述文本
	 * @param suffix 文件后缀，多个之间用逗号分开
	 */
	public DiskFileFilter(String description, String suffix) {
		this(description, suffix, false);
	}

	/**
	 * 设置全部
	 * @param b
	 */
	public void setAll(boolean b) {
		all = b;
	}

	/**
	 * 判断支持全部
	 * @return 真或者假
	 */
	public boolean isAll() {
		return all;
	}
	
	/**
	 * 解析参数
	 * @param input 输入格式
	 */
	private void split(String input) {
		if (input.matches(REGEX_ALL)) {
			all = true;
			array.add(input);
		} else {
			String[] a = input.split("\\s*\\,\\s*");
			for (String s : a) {
				s = s.trim();
				if (s.length() > 0) {
					array.add(s);
				}
			}
		}
	}
	
	/**
	 * 返回后缀字符串数组
	 * @return String数组
	 */
	public String[] getExtensions() {
		String[] a = new String[array.size()];
		return array.toArray(a);
	}

	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File file) {
		// 如果是全部
		if (all) {
			return true;
		}
		// 如果是目录，无条件支持！
		boolean success = (file.exists() && file.isDirectory());
		if (success) {
			return true;
		}

		// 判断文件名称匹配
		String name = file.getName();
		for (String suffix : array) {
			String regex = String.format(DiskFileFilter.REGEX, suffix);
			if (name.matches(regex)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * 判断符合命名规定
	 * @param filename 文件名
	 * @return 返回真或者假
	 */
	public boolean accept(String filename) {
		return accept(new File(filename));
	}

	/**
	 * 设置描述文本
	 * @param s 字符串
	 */
	public void setDescription(String s){
		description = s;
	}

	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

}