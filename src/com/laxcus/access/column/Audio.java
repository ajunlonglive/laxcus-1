/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

import com.laxcus.access.type.*;

/**
 * 音频列。
 * 
 * @author scott.liang
 * @version 1.0 4/25/2009
 * @since laxcus 1.0
 */
public final class Audio extends Media {

	private static final long serialVersionUID = 7224773932810384815L;

	/**
	 * 使用传入的音频列对象，生成它的浅层数据副本
	 * @param that 音频列对象
	 */
	private Audio(Audio that) {
		super(that);
	}

	/**
	 * 构造默认的音频数据类型
	 */
	public Audio() {
		super(ColumnType.AUDIO);
	}

	/**
	 * 构造音频数据类型，指定列编号
	 * 
	 * @param columnId 列编号
	 */
	public Audio(short columnId) {
		this();
		setId(columnId);
	}

	/**
	 * 构造音频数据类型，指定列编号和数据
	 * 
	 * @param columnId 列编号
	 * @param value 音频数据
	 */
	public Audio(short columnId, byte[] value) {
		this(columnId);
		super.setValue(value);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public Audio duplicate() {
		return new Audio(this);
	}

}
