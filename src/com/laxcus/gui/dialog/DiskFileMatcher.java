/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog;

import java.io.*;
import java.util.*;

/**
 * 过滤器
 * 
 * @author scott.liang
 * @version 1.0 9/4/2021
 * @since laxcus 1.0
 */
public class DiskFileMatcher implements FileMatcher {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*([\\w\\W]+?)\\.(?i)(%s+)\\s*$";
	
	/** 允许全部 **/
	private boolean all;
	
	/** 扩展字符串 **/
	private String extension;
	
	/** 去掉间隔符号的扩展字符串 **/
	private String[] extensions;
	
	/** 描述 **/
	private String description;
	
	/** 正则表达式 **/
	private ArrayList<String> regexs = new ArrayList<String>();
	
	/**
	 * 构造磁盘文件检查过滤器
	 * @param description 描述文本
	 * @param extensions 文件后缀，多个之间用逗号分开
	 * @param all 全部文件
	 */
	public DiskFileMatcher(String description, String extensions, boolean all) {
		super();
		setDescription(description);
		setExtension(extensions);
		setAll(all);
	}
	
	/**
	 * 构造磁盘文件检查过滤器
	 * @param description 描述文本
	 * @param extensions 文件后缀，多个之间用逗号分开
	 */
	public DiskFileMatcher(String description, String extensions) {
		this(description, extensions, false);
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
	
//	/**
//	 * 解析参数
//	 * @param input 输入格式
//	 */
//	private void split(String input) {
//		if (input.matches(REGEX_ALL)) {
//			all = true;
//			array.add(input);
//		} else {
//			String[] a = input.split("\\s*\\,\\s*");
//			for (String s : a) {
//				s = s.trim();
//				if (s.length() > 0) {
//					array.add(s);
//				}
//			}
//		}
//	}
	
	/**
	 * 解析参数
	 * @param extens 输入格式
	 */
	private void split(String extens) {
		// 如果是全部...
		if (all) {
			regexs.clear();
			return;
		}
		// 解析，生成正则表达式
		extensions = extens.split("\\s*\\,\\s*");
		for (String suffix : extensions) {
			suffix = suffix.trim();
			String text = String.format(REGEX, suffix);
			regexs.add(text);
		}
	}
	
	/**
	 * 设置扩展字符串
	 * @param s
	 */
	public void setExtension(String s) {
		extension = s;
		split(extension);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.dialog.FileFilter#getExtensions()
	 */
	@Override
	public String getExtension() {
		return extension;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.gui.dialog.FileMatcher#getExtensions()
	 */
	@Override
	public String[] getExtensions() {
		return extensions;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.gui.dialog.FileMatcher#accept(java.lang.String)
	 */
	@Override
	public boolean accept(String name) {
		// 如果是全部
		if (all) {
			return true;
		}

		// 判断文件名称匹配
		for (String regex : regexs) {
			if (name.matches(regex)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File file) {
		String name = file.getName();
		return accept(name);

		//		// 如果是全部
		//		if (all) {
		//			return true;
		//		}
		////		// 如果是目录，无条件支持！
		////		boolean success = (file.exists() && file.isDirectory());
		////		if (success) {
		////			return true;
		////		}
		//
		//		// 判断文件名称匹配
		//		String name = file.getName();
		//		for (String regex : regexs) {
		////			System.out.println(regex);
		//			if (name.matches(regex)) {
		//				return true;
		//			}
		//		}
		//
		//		return false;
	}

	//	/**
	//	 * 判断符合命名规定
	//	 * @param filename 文件名
	//	 * @return 返回真或者假
	//	 */
	//	public boolean accept(String filename) {
	//		return accept(new File(filename));
	//	}
	
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((DiskFileMatcher) that) == 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (extension != null) {
			return extension.hashCode();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FileMatcher that) {
		if (that == null) {
			return 1;
		} else if (that.getClass() != this.getClass()) {
			return -1;
		}
		// 一致性比较
		DiskFileMatcher df = (DiskFileMatcher) that;
		return extension.compareTo(df.extension);
	}
	

	
	public static void main(String[] args) {
		String des = "全部文件";
		String ext = ".";
		DiskFileMatcher matcher = new DiskFileMatcher(des, ext, false);
		File file = new File("d:/unix.cpp");
		boolean b = matcher.accept(file);
		System.out.printf("%s is %s\n", file, b);
	}
	
//	/* (non-Javadoc)
//	 * @see com.laxcus.ui.dialog.FileFilter#accept(java.io.File)
//	 */
//	@Override
//	public boolean accept(File f) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.ui.dialog.FileFilter#getDescription()
//	 */
//	@Override
//	public String getDescription() {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
