/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.io.*;

/**
 * 发布各种类型的链接库
 * @author scott.liang
 * @version 1.0 10/18/2019
 * @since laxcus 1.0
 */
public class PublishLibraryComponentParser extends SyntaxParser {

	/** 链接库文件 **/
	protected final String LIBRARY_SUFFIX = "^\\s*([\\w\\W]+)(?i)(\\.DLL|\\.SO)\\s*$";

	/**
	 * 构造发布各种类型的链接库解析器
	 */
	protected PublishLibraryComponentParser() {
		super();
	}

	/**
	 * 判断是链接库文件
	 * @param file File实例
	 * @return 返回真或者假
	 */
	protected boolean isLibrary(File file) {
		// 判断是文件
		boolean success = (file.exists() && file.isFile());
		// 判断是“.so/.dll”后缀
		if (success) {
			String name = file.getName();
			success = name.matches(LIBRARY_SUFFIX);
		}
		return success;
	}

}