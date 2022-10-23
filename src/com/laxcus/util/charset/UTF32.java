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
 * UTF32字符集，采用大字头编码(big-endian)
 * 
 * @author scott.liang
 * @version 1.0 04/03/2009
 * @since laxcus 1.0
 */
public final class UTF32 extends Charset {
	
	private static final long serialVersionUID = 8204348560571880049L;

	/** UTF32字符集，采用大头编码 **/
	private final static String NAME = "UTF-32BE";

	/**
	 * 构造一个UTF32的数据副本对象
	 * @param that UTF32实例
	 */
	private UTF32(UTF32 that) {
		super(that);
	}

	/**
	 * 构造一个UTF32字符集对象
	 */
	public UTF32() {
		super();
	}
	
	/**
	 * 从可类化读取器中解析和判断字符集对象
	 * @param reader 可类化读取器
	 */
	public UTF32(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回UTF32的字符集描述
	 * @see com.laxcus.util.charset.Charset#describe()
	 */
	@Override
	public String describe() {
		return UTF32.NAME;
	}

	/**
	 * 生成UTF32字符集的数据副本
	 * @see com.laxcus.util.charset.Charset#duplicate()
	 */
	@Override
	public Charset duplicate() {
		return new UTF32(this);
	}
}