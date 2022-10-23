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
 * 视频列。
 * 
 * @author scott.liang
 * @version 1.0 4/25/2009
 * @since laxcus 1.0
 */
public final class Video extends Media {

	private static final long serialVersionUID = -4930323911506467142L;

	/**
	 * 使用传入的视频列对象，生成它的浅层数据副本
	 * @param that 视频列对象
	 */
	private Video(Video that) {
		super(that);
	}

	/**
	 * 构造默认的视频数据类型
	 */
	public Video() {
		super(ColumnType.VIDEO);
	}

	/**
	 * 构造视频数据类型，指定列编号
	 * 
	 * @param columnId 列编号
	 */
	public Video(short columnId) {
		this();
		setId(columnId);
	}

	/**
	 * 构造音频数据类型，指定列编号和数据
	 * 
	 * @param columnId 列编号
	 * @param value 视频数据
	 */
	public Video(short columnId, byte[] value) {
		this(columnId);
		setValue(value);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public Video duplicate() {
		return new Video(this);
	}

}