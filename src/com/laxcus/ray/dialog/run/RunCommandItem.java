/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.run;

import java.io.*;

/**
 * 命令单元
 * 
 * @author scott.liang
 * @version 1.0 7/1/2021
 * @since laxcus 1.0
 */
class RunCommandItem implements Serializable {

	private static final long serialVersionUID = 1422585602601354183L;

	/** 显示文本 **/
	private String text;

	/**
	 * 构造默认的命令单元
	 */
	public RunCommandItem() {
		super();
	}

	/**
	 * 构造命令单元，指定文本
	 * @param text 显示文本
	 */
	public RunCommandItem(String text) {
		this();
		setText(text);
	}

	/**
	 * 设置文本
	 * @param s
	 */
	public void setText(String s) {
		text = s;
	}

	/**
	 * 返回文本
	 * @return
	 */
	public String getText() {
		return text;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return text;
	}

}
