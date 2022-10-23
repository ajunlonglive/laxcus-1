/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.section;

import com.laxcus.util.classable.*;

/**
 * UTF8编码的字符分区
 * 
 * @author scott.liang
 * @version 1.1 03/17/2015
 * @since laxcus 1.0
 */
public final class CharSector extends WordSector {

	private static final long serialVersionUID = -6375466610282285777L;

	/**
	 * 使用传入参数，生成一个副本
	 * @param that CharSector实例
	 */
	private CharSector(CharSector that) {
		super(that);
	}

	/**
	 * 构造UTF8编码的字符分区
	 */
	public CharSector() {
		super();
	}
	
	/**
	 * 从可类化读取器中解析UTF8字符分区记录
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public CharSector(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#duplicate()
	 */
	@Override
	public CharSector duplicate() {
		return new CharSector(this);
	}

}