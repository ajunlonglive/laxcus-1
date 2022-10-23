/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column.attribute;

/**
 * 媒体数组属性。<br>
 * 长度范围: 0 - 2G 字节<br>
 * 
 * @author scott.liang
 * @version 1.0 4/25/2009
 * @since laxcus 1.0
 */
public abstract class MediaAttribute extends VariableAttribute {

	private static final long serialVersionUID = -4783869726522409914L;

	/**
	 * 建立一个媒体列属性，并且指定它的数据类型
	 * @param family 列数据类型
	 */
	protected MediaAttribute(byte family) {
		super(family);
	}

	/**
	 * 根据传入MediaAttribute实例建立新的媒体列属性的数据副本
	 * @param that MediaAttribute实例
	 */
	protected MediaAttribute(MediaAttribute that) {
		super(that);
	}

}