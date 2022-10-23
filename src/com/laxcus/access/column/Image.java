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
 * 图像列。
 * 
 * @author scott.liang
 * @version 1.0 4/25/2009
 * @since laxcus 1.0
 */
public final class Image extends Media {

	private static final long serialVersionUID = -2195597588889297403L;

	/**
	 * 使用传入的图像列对象，生成它的浅层数据副本
	 * @param that 图像列对象
	 */
	private Image(Image that) {
		super(that);
	}

	/**
	 * 构造默认的图像数据类型
	 */
	public Image() {
		super(ColumnType.IMAGE);
	}

	/**
	 * 构造图像数据类型，指定列编号
	 * 
	 * @param columnId 列编号
	 */
	public Image(short columnId) {
		this();
		setId(columnId);
	}
	
	/**
	 * 构造图像数据类型，指定列编号和数据
	 * 
	 * @param columnId 列编号
	 * @param value 图像数据
	 */
	public Image(short columnId, byte[] value) {
		this(columnId);
		setValue(value);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public Image duplicate() {
		return new Image(this);
	}

}

