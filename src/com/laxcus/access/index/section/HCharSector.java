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
 * 大字符(UTF32)字符分割器。
 * 
 * @author scott.liang
 * @version 1.1 03/17/2015
 * @since laxcus 1.0
 */
public final class HCharSector extends WordSector {

	private static final long serialVersionUID = 6794264941578540803L;

	/**
	 * 使用传入参数，构造一个新的大字符数据分区
	 * @param that
	 */
	private HCharSector(HCharSector that) {
		super(that);
	}

	/**
	 * 构造大字符数据分区
	 */
	public HCharSector() {
		super();
	}

	/**
	 * 从可类化读取器中解析大字符(UTF32)分区记录
	 * @param reader
	 * @since 1.1
	 */
	public HCharSector(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#duplicate()
	 */
	@Override
	public HCharSector duplicate() {
		return new HCharSector(this);
	}

}