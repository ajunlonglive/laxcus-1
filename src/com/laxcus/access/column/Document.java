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
 * 文档列。
 * 
 * @author scott.liang
 * @version 1.0 4/25/2009
 * @since laxcus 1.0
 */
public final class Document extends Media {

	private static final long serialVersionUID = 722169478102866546L;

	/**
	 * 使用传入的文档列对象，生成它的浅层数据副本
	 * @param that - 文档列对象
	 */
	private Document(Document that) {
		super(that);
	}

	/**
	 * 构造默认的文档数据类型
	 */
	public Document() {
		super(ColumnType.DOCUMENT);
	}

	/**
	 * 构造文档数据类型，指定列编号
	 * 
	 * @param columnId - 列编号
	 */
	public Document(short columnId) {
		this();
		setId(columnId);
	}
	
	/**
	 * 构造文档数据类型，指定列编号和数据
	 * 
	 * @param columnId - 列编号
	 * @param value - 文档数据
	 */
	public Document(short columnId, byte[] value) {
		this(columnId);
		setValue(value);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public Document duplicate() {
		return new Document(this);
	}

}
