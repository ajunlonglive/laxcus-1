/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.choice;

/**
 * 编码类型
 * 
 * @author scott.liang
 * @version 1.0 9/14/2021
 * @since laxcus 1.0
 */
final class EncodeType implements Comparable<EncodeType> {

	/** 编码 **/
	private String encode;

	/** 描述 **/
	private String description;

	/**
	 * 构造编码类型
	 * @param description 描述
	 * @param encode 编码
	 */
	public EncodeType(String description, String encode) {
		super();
		setDescription(description);
		setEncode(encode);
	}

	/**
	 * 设置编码
	 * @param s
	 */
	public void setEncode(String s) {
		encode = s;
	}

	/**
	 * 返回编码
	 * @return
	 */
	public String getEncode() {
		return encode;
	}

	/**
	 * 设置描述文本
	 * @param s 字符串
	 */
	public void setDescription(String s){
		description = s;
	}

	/**
	 * 设置描述文本
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (encode == null) {
			return 0;
		}
		return encode.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null || o.getClass() != EncodeType.class) {
			return false;
		}
		return compareTo((EncodeType) o) == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EncodeType that) {
		if (that == null) {
			return 1;
		} else if (that.encode == null) {
			return 1;
		} else if (encode == null) {
			return -1;
		}
		return encode.compareToIgnoreCase(that.encode);
	}

}