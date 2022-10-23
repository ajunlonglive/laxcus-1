/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.product;

import com.laxcus.util.classable.*;

/**
 * 字符串返回结果
 * 
 * @author scott.liang
 * @version 1.0 3/19/2013
 * @since laxcus 1.0
 */
public class StringProduct extends EchoProduct {

	private static final long serialVersionUID = -1705645824040776691L;

	/** 参数值 **/
	private String value;

	/**
	 * 构造默认的字符串返回结果
	 */
	public StringProduct() {
		super();
	}

	/**
	 * 构造字符串返回结果，指定参数
	 * @param value 参数
	 */
	public StringProduct(String value) {
		this();
		setValue(value);
	}

	/**
	 * 从可类化数据读取器中解析字符串返回结果
	 * @param reader 可类化读取器
	 */
	public StringProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 建立字符串返回结果的数据副本
	 * @param that SuccessProduct实例
	 */
	private StringProduct(StringProduct that) {
		super(that);
		value = that.value;
	}

	/**
	 * 设置参数值
	 * @param e 参数值
	 */
	public void setValue(String e) {
		value = e;
	}

	/**
	 * 返回参数值
	 * @return 返回真或者假
	 */
	public String getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeString(value);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		value = reader.readString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return value;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public StringProduct duplicate() {
		return new StringProduct(this);
	}

}