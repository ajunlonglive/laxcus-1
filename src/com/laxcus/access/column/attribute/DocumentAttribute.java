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
 * 文档列属性。<br>
 * 长度范围: 0 - 2G 字节<br>
 * 
 * @author scott.liang
 * @version 1.0 4/25/2009
 * @since laxcus 1.0
 */
public class DocumentAttribute extends MediaAttribute {

	private static final long serialVersionUID = 63526979323147863L;

	/**
	 * 根据传入参数生成文档列属性的副本
	 * @param that DocumentAttribute实例
	 */
	private DocumentAttribute(DocumentAttribute that) {
		super(that);
	}

	/**
	 * 生成一个默认的文档列属性
	 */
	public DocumentAttribute() {
		super(ColumnType.DOCUMENT);
	}
	
	/**
	 * 生成一个文档列属性，并且指定列编号
	 * @param columnId 列编号
	 */
	public DocumentAttribute(short columnId) {
		this();
		setColumnId(columnId);
	}
	
	/**
	 * 生成一个文档列属性，并且指定列编号和列名称
	 * @param columnId 列编号
	 * @param title 列标题
	 */
	public DocumentAttribute(short columnId, String title) {
		this(columnId);
		setName(title);
	}
	
	/**
	 * 生成一个文档列属性，并且指定列编号、列名称、默认值
	 * @param columnId 列编号
	 * @param title 列标题
	 * @param value 列数值
	 */
	public DocumentAttribute(short columnId, String title, byte[] value) {
		this(columnId, title);
		setValue(value);
	}

	/**
	 * 生成默认的文档列
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#getDefault()
	 */
	@Override
	public com.laxcus.access.column.Column getDefault() {
		short columnId = getColumnId();
		com.laxcus.access.column.Document column = new com.laxcus.access.column.Document(columnId);

		if (getFunction() != null) {
			column = (com.laxcus.access.column.Document) getFunction().getDefault();
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
	public DocumentAttribute duplicate() {
		return new DocumentAttribute(this);
	}
	
}