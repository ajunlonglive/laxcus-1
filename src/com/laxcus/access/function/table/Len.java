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
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.function.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.util.charset.*;

/**
 * <code>SQL len</code>函数。<br>
 * 统计字符串或者字节数组的长度，返回结果列是一定是整型值(com.laxcus.sql.column.Integer)。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/17/2012
 * @since laxcus 1.0
 */
public class Len extends ColumnFunction {

	private static final long serialVersionUID = -5355645269263371928L;

	/** LEN 正则表达式语句 **/
	private final static String REGEX = "^\\s*(?i)(?:LEN)\\s*\\(\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*\\)\\s*$";
	
	/**
	 * 统计列中的数据长度
	 */
	public Len() {
		// 返回值是整型
		super(ColumnType.INTEGER);
		// 不允许产生默认列
		setSupportDefault(false);
	}

	/**
	 * 根据传入的LEN函数，生成它的副本
	 * @param that
	 */
	private Len(Len that) {
		super(that);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.Function#duplicate()
	 */
	@Override
	public Len duplicate() {
		return new Len(this);
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
	public ColumnFunction create(Table table, String primitive) {
		// 语法: LEN(column_name)
		Pattern pattern = Pattern.compile(Len.REGEX);
		Matcher matcher = pattern.matcher(primitive);
		if (!matcher.matches()) {
			return null;
		}
		String name = matcher.group(1);

		// 查找匹配的列
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throw new ColumnAttributeException("cannot find \'%s\'", name);
		}
		// 只支持可变长数组类型
		if(!attribute.isVariable()) {
			throw new ColumnAttributeException("cannot support \'%s\'", name);
		}

		Len len = new Len();
		// 保存原语
		len.setPrimitive(primitive.trim());
		// 提取列标识号
		len.setColumnId(attribute.getColumnId());
		// 所属表
		len.setSpace(table.getSpace());
		// 如果是可变长属性，记录打包配置
		if (attribute.isVariable()) {
			len.setPacking(((VariableAttribute) attribute).getPacking());
		}
		// 如果是字符串，记录是否大小写敏感
		if (attribute.isWord()) {
			len.setSentient(((WordAttribute) attribute).isSentient());
		}

		return len;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#makeup(java.util.List)
	 */
	@Override
	public Column makeup(List<Row> rows) {
		// LEN在后面进行类型检查
		
		// 只能传入一行记录
		if (rows.size() != 1) {
			throw new FunctionException("sizeout");
		}
		
		Row row = rows.iterator().next();
		Column column = row.find(getColumnId());
		if (column == null) {
			throw new FunctionException("cannot find %d", getColumnId());
		}
		if (!column.isVariable()) {
			throw new FunctionException("cannot support %d", column.getType());
		}
		
		// 兼容二进制字节数组和字符类型
		byte[] value = ((Variable) column).getValue(super.getPacking());
		int size = value.length;
		// 如果是字符类型，在解码后计算它的字符串长度
		if (column.isWord()) {
			Charset charset = ((Word) column).getCharset();
			String s = charset.decode(value, 0, value.length);
			size = s.length();
		}
		
		// 返回整型列
		return new com.laxcus.access.column.Integer((short) 0, size);
	}

	/**
	 * LEN函数不产生默认列
	 * @see com.laxcus.access.function.table.ColumnFunction#getDefault()
	 */
	@Override
	public Column getDefault() {
		return null;
	}

}