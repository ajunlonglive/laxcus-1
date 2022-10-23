/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog;

import java.io.*;

/**
 * 文件过滤器
 * 
 * @author scott.liang
 * @version 1.0 9/4/2021
 * @since laxcus 1.0
 */
public interface FileMatcher extends Comparable<FileMatcher> {

	/**
	 * 判断这个文件符合要求
	 * 
	 * @param f
	 * @return
	 */
	boolean accept(File f);
	
	/**
	 * 匹配的名称
	 * @param name
	 * @return
	 */
	boolean accept(String name);

	/**
	 * 返回描述字
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * 返回扩展字符串
	 * 是包含间隔符号的多个
	 * @return
	 */
	String getExtension();
	
	/**
	 * 去掉分隔符后的扩展字符串
	 * @return 字符串
	 */
	String[] getExtensions();
}