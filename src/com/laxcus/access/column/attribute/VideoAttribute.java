/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column.attribute;

import com.laxcus.access.type.*;

/**
 * 视频列属性。<br>
 * 长度范围: 0 - 2G 字节<br>
 * 
 * @author scott.liang
 * @version 1.0 4/25/2009
 * @since laxcus 1.0
 */
public class VideoAttribute extends MediaAttribute {

	private static final long serialVersionUID = 41346172013642951L;

	/**
	 * 根据传入参数生成视频列属性的副本
	 * @param that VideoAttribute实例
	 */
	private VideoAttribute(VideoAttribute that) {
		super(that);
	}

	/**
	 * 生成一个默认的视频列属性
	 */
	public VideoAttribute() {
		super(ColumnType.VIDEO);
	}
	
	/**
	 * 生成一个视频列属性，并且指定列编号
	 * @param columnId 列编号
	 */
	public VideoAttribute(short columnId) {
		this();
		setColumnId(columnId);
	}
	
	/**
	 * 生成一个视频列属性，并且指定列编号和列名称
	 * @param columnId 列编号
	 * @param title 列标题
	 */
	public VideoAttribute(short columnId, String title) {
		this(columnId);
		setName(title);
	}
	
	/**
	 * 生成一个视频列属性，并且指定列编号、列名称、默认值
	 * @param columnId 列编号
	 * @param title 列标题
	 * @param value 列数值
	 */
	public VideoAttribute(short columnId, String title, byte[] value) {
		this(columnId, title);
		setValue(value);
	}

	/**
	 * 生成默认的视频列
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#getDefault(short)
	 */
	@Override
	public com.laxcus.access.column.Column getDefault() {
		short columnId = getColumnId();
		com.laxcus.access.column.Video column = new com.laxcus.access.column.Video(columnId);

		if (getFunction() != null) {
			column = (com.laxcus.access.column.Video) getFunction().getDefault();
			column.setId(columnId);
		} else if (value != null || index != null) {
			column.setValue(value);
			column.setIndex(index);
		}
		return column;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#duplicate()
	 */
	@Override
	public VideoAttribute duplicate() {
		return new VideoAttribute(this);
	}
	
}