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
 * UTF16字符集，采用大字头编码(big-endian)
 * 
 * @author scott.liang
 * @version 1.0 04/03/2009
 * @since laxcus 1.0
 */
public final class UTF16 extends Charset {

	private static final long serialVersionUID = 2857627332717900435L;

	/** UTF16字符集描述，采用大头编码 **/
	private final static String NAME = "UTF-16BE";

	/**
	 * 构造一个UTF16的数据副本对象
	 * @param that UTF16
	 */
	private UTF16(UTF16 that) {
		super(that);
	}

	/**
	 * 构造默认的UTF16字符集对象
	 */
	public UTF16() {
		super();
	}
	
	/**
	 * 从可类化读取器中解析和判断字符集对象
	 * @param reader 可类化数据读取器
	 */
	public UTF16(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回UTF16的字符集描述
	 * @see com.laxcus.util.charset.Charset#describe()
	 */
	@Override
	public String describe() {
		return UTF16.NAME;
	}

	/**
	 * 生成UTF16字符集的数据副本
	 * @see com.laxcus.util.charset.Charset#duplicate()
	 */
	@Override
	public Charset duplicate() {
		return new UTF16(this);
	}
}