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
 * 宽字符(双字节UTF16、WCHAR)分区
 * 
 * @author scott.liang
 * @version 1.1 03/17/2015
 * @since laxcus 1.0
 */
public final class WCharSector extends WordSector {

	private static final long serialVersionUID = -7778759556631627825L;

	/**
	 * 使用传入参数，构造一个宽字符分区副本
	 * @param that
	 */
	private WCharSector(WCharSector that) {
		super(that);
	}

	/**
	 * 构造宽字符分区
	 */
	public WCharSector() {
		super();
	}

	/**
	 * 从可类化读取器中解析宽字符(UTF16)分区记录
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public WCharSector(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#duplicate()
	 */
	@Override
	public WCharSector duplicate() {
		return new WCharSector(this);
	}

}