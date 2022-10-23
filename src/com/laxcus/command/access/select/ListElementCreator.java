/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.select;

/**
 * 显示成员生成器。
 * 
 * @author scott.liang
 * @version 1.0 1/12/2012
 * @since laxcus 1.0
 */
public class ListElementCreator {

	/**
	 * 根据显示成员类型，生成显示成员
	 * @param family 显示成员类型
	 * @return  返回ListElement子类实例，不匹配返回空指针
	 */
	public static ListElement create(byte family) {
		switch (family) {
		case ListElement.COLUMN:
			return new ColumnElement();
		case ListElement.FUNCTION:
			return new FunctionElement();
		}
		return null;
	}
}
