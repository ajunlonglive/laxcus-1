/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.local;

/**
 * 本地文本匹配器
 * 
 * @author scott.liang
 * @version 1.0 6/12/2015
 * @since laxcus 1.0
 */
public interface LocalMatcher {

	/**
	 * 返回XML标签的属性文本
	 * @param xmlPath XML格式路径
	 * @return 所关联语言的文本信息
	 */
	String findCaption(String xmlPath);

	/**
	 * 返回XML标签包围的文本内容
	 * @param xmlPath XML格式路径
	 * @return 所关联语言的文本信息
	 */
	String findContent(String xmlPath);
}
