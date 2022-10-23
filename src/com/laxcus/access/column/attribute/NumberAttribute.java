/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column.attribute;

/**
 * 数值列属性，子类包括：SHORT、INT、LONG、FLOAT、DOUBLE、DATE、TIME、TIMESTAMP <br>
 * 
 * @author scott.liang
 * @version 1.3 8/21/2013
 * @since laxcus 1.0
 */
public abstract class NumberAttribute extends ColumnAttribute {

	private static final long serialVersionUID = 7732928195007337198L;

	/**
	 * 根据传入的数值列属性生成新的数值列属性的副本
	 * @param that NumberAttribute实例
	 */
	protected NumberAttribute(NumberAttribute that) {
		super(that);
	}

	/**
	 * 构造数值列属性，并且指定它的数据类型
	 * @param family 数据类型
	 */
	protected NumberAttribute(byte family) {
		super(family);
	}
	

}