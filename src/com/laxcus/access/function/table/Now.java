/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.function.table;

import java.util.*;
import java.util.regex.*;

import com.laxcus.access.column.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.util.datetime.*;

/**
 * 当前时间，包括：年/月/日  时：分：秒  毫秒
 * 
 * @author scott.liang
 * @version 1.0 9/2/2009
 * @since laxcus 1.0
 */
public final class Now extends ColumnFunction {

	private static final long serialVersionUID = -3313889673343950672L;

	/** NOW函数正则表达式 **/
	private final static String REGEX = "^\\s*(?i)NOW\\s*\\(\\s*\\)\\s*$";
	
	/**
	 * 根据传入的NOW函数实例，生成它的副本
	 * @param that
	 */
	private Now(Now that) {
		super(that);
	}
	
	/**
	 * 生成一个系统时间戳函数
	 */
	public Now() {
		super(ColumnType.TIMESTAMP);
		// 支持产生默认列
		super.setSupportDefault(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.Function#duplicate()
	 */
	@Override
	public Now duplicate() {
		return new Now(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#matches(java.lang.String)
	 */
	@Override
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#create(com.laxcus.access.schema.Table, java.lang.String)
	 */
	@Override
	public ColumnFunction create(Table table, String input) {
		Pattern pattern = Pattern.compile(Now.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}
		Now now = new Now();
		now.setPrimitive(input);
		return now;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#makeup(java.util.List)
	 */
	@Override
	public Column makeup(List<Row> rows) {
		return this.getDefault();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#getDefault()
	 */
	@Override
	public Column getDefault() {
		Timestamp stamp = new Timestamp();
		stamp.setValue(SimpleTimestamp.format(new java.util.Date()));
		return stamp;
	}

}