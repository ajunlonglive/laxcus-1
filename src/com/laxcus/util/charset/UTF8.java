/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.charset;

import com.laxcus.util.classable.*;

/**
 * UTF8字符集。
 * 
 * @author scott.liang
 * @version 1.0 04/03/2009
 * @since laxcus 1.0
 */
public final class UTF8 extends Charset {

	private static final long serialVersionUID = -3603536589201905689L;

	/** UTF8字符集描述 **/
	private final static String NAME = "UTF-8";

	/**
	 * 构造一个UTF8的数据副本对象
	 * @param that UTF8实例
	 */
	private UTF8(UTF8 that) {
		super(that);
	}

	/**
	 * 构造默认的UTF8字符集对象
	 */
	public UTF8() {
		super();
	}

	/**
	 * 从可类化读取器中解析和判断字符集对象
	 * @param reader 可类化读取器
	 */
	public UTF8(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回UTF8的字符集描述
	 * @see com.laxcus.util.charset.Charset#describe()
	 */
	@Override
	public String describe() {
		return UTF8.NAME;
	}

	/**
	 * 生成UTF8字符集的数据副本
	 * @see com.laxcus.util.charset.Charset#duplicate()
	 */
	@Override
	public Charset duplicate() {
		return new UTF8(this);
	}
}