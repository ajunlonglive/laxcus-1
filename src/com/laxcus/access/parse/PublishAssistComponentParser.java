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
 * 发布各种类型的应用附件
 * @author scott.liang
 * @version 1.0 10/8/2019
 * @since laxcus 1.0
 */
public class PublishAssistComponentParser extends SyntaxParser {

	/** JAR文件 **/
	protected final String JAR_SUFFIX = "^\\s*([\\w\\W]+)(?i)(\\.JAR)\\s*$";

	/**
	 * 构造发布各种类型的应用附件解析器
	 */
	protected PublishAssistComponentParser() {
		super();
	}

	/**
	 * 判断是JAR文件
	 * @param file File实例
	 * @return 返回真或者假
	 */
	protected boolean isJAR(File file) {
		// 判断是文件
		boolean success = (file.exists() && file.isFile());
		// 判断是“.jar”后缀
		if (success) {
			String name = file.getName();
			success = name.matches(JAR_SUFFIX);
		}
		return success;
	}

}