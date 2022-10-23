/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.tip;

import com.laxcus.util.local.*;

/**
 * 提示资源选择器
 * 
 * @author scott.liang
 * @version 1.0 11/25/2013
 * @since laxcus 1.0
 */
public class TipSelector extends LocalSelector {

	/**
	 * 构造提示资源选择器，指定入口文件路径
	 */
	public TipSelector(String entry) {
		super(entry);
	}
	
	/**
	 * 返回回显文件路径
	 * 
	 * @return 回显文件路径的字符串描述
	 */
	public String getEchoPath() {
		return findPath("echo");
	}

	/**
	 * 返回信息文件路径
	 * @return 信息文件路径的字符串描述
	 */
	public String getMessagePath() {
		return findPath("message");
	}

	/**
	 * 返回警告文件路径
	 * @return 警告文件路径的字符串描述
	 */
	public String getWarningPath() {
		return findPath("warning");
	}

	/**
	 * 返回故障文件路径
	 * @return 故障文件路径的字符串描述
	 */
	public String getFaultPath() {
		return findPath("fault");
	}
}