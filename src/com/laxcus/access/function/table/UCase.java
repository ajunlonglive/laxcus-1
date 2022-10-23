/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.function.table;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.function.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.util.*;
import com.laxcus.log.client.*;
import com.laxcus.util.charset.*;

/**
 * <code>SQL UCASE</code>函数，将字符串转为大写输出(只限字符类型)。<br>
 * 
 * @author scott.liang
 * @version 1.0 03/09/2013
 * @since laxcus 1.0
 */
public final class UCase extends ColumnFunction {

	private static final long serialVersionUID = 8673195240524362802L;

	private final static String REGEX = "^\\s*(?i)(?:UCASE)\\s*\\(\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*\\)\\s*$";
	
	/**
	 * 构造默认的UCASE函数
	 */
	public UCase() {
		super();
		// 不允许产生默认列
		setSupportDefault(false);
	}

	/**
	 * 生成UCASE函数的数据副本
	 * @param that
	 */
	private UCase(UCase that) {
		super(that);
	}

	/**
	 * 根据当前UCASE函数，生成一个它的副本
	 * @see com.laxcus.access.function.Function#duplicate()
	 */
	@Override
	public UCase duplicate() {
		return new UCase(this);
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
		// 语法: UCASE(column_name)
		Pattern pattern = Pattern.compile(UCase.REGEX);
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
		// 只限字符类型
		if (!attribute.isWord()) {
			throw new ColumnAttributeException("cannot support \'%s\'", name);
		}

		UCase ucase = new UCase();
		// 保存原语
		ucase.setPrimitive(primitive);
		// 提取列标识号
		ucase.setColumnId(attribute.getColumnId());
		// 所属表
		ucase.setSpace(table.getSpace());
		// 设置返回类型
		ucase.setResultFamily(attribute.getType());
		// 记录打包配置
		ucase.setPacking(((WordAttribute) attribute).getPacking());
		// 记录是否大小写敏感
		ucase.setSentient(((WordAttribute) attribute).isSentient());		

		return ucase;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#makeup(java.util.List)
	 */
	@Override
	public Column makeup(List<Row> rows) {
		// 检查列类型是否匹配
		super.check(rows, getColumnId());
		
		// 只能传入一行记录
		if(rows.size() != 1) {
			throw new FunctionException("sizeout");
		}
		
		Row row = rows.get(0);
		Column column = row.find(getColumnId());
		if (column == null) {
			throw new FunctionException("cannot find %d", getColumnId());
		}

		// 兼容二进制字节数组和字符类型
		Packing packing = super.getPacking();
		Word element = (Word) column;
		// 解包
		byte[] value = element.getValue(packing);
		// 解码
		Charset charset = element.getCharset();
		// 转成大写字符
		String s = charset.decode(value, 0, value.length);
		s = s.toUpperCase();
		
		// 重新编码
		value = charset.encode(s);
		// 打包
		try {
			value = VariableGenerator.enpacking(packing, value, 0, value.length);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
		
		if (column.isChar()) {
			return new com.laxcus.access.column.Char(getColumnId(), value);
		} else if (column.isWChar()) {
			return new com.laxcus.access.column.WChar(getColumnId(), value);
		} else if (column.isHChar()) {
			return new com.laxcus.access.column.HChar(getColumnId(), value);
		}
		
		return null;
	}

	/**
	 * 不能生成默认值
	 * @see com.laxcus.access.function.table.ColumnFunction#getDefault()
	 */
	@Override
	public Column getDefault() {
		return null;
	}

}