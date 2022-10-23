/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

/**
 * LIKE语句解析结果
 * 
 * @author scott.liang
 * @version 1.0 8/28/2010
 * @since laxcus 1.0
 */
public final class LikeString {

	/** 两侧空格字符长度，如果是-1表示无限制 **/
	private short left, right;

	/** 关键字敏感，默认是TRUE **/
	private boolean sentient;

	/** 字符串文本值 **/
	private String value;

	/**
	 * 构造一个LIKE字符串
	 */
	private LikeString() {
		super();
		// 默认无限制
		left = right = -1;
		this.setSentient(true);
	}

	/**
	 * 构造一个LIKE字符串，并且指定范围和文本内容
	 * @param left
	 * @param right
	 * @param input
	 */
	public LikeString(short left, short right, String input) {
		this();
		this.setRange(left, right);
		this.setValue(input);
	}

	/**
	 * 大小写敏感 (CASE or NOTCASE)
	 * 
	 * @param b
	 */
	public void setSentient(boolean b) {
		this.sentient = b;
	}

	/**
	 * 是否大小写敏感
	 * 
	 * @return
	 */
	public boolean isSentient() {
		return this.sentient;
	}
	
	
	public void setRange(short left, short right) {
		this.left = left;
		this.right = right;
	}
	
	public short getLeft() {
		return this.left;
	}
	
	public short getRight() {
		return this.right;
	}
	
	public void setValue(String s) {
		this.value = s;
	}
	
	public String getValue() {
		return this.value;
	}
}