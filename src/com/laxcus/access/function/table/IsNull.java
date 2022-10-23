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
import com.laxcus.util.classable.*;

/**
 * <code>SQL IsNull</code>函数。<br>
 * 如果传入列为NULL状态，返回默认值，否则返回实际列。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/28/2012
 * @since laxcus 1.0
 */
public final class IsNull extends ColumnFunction {

	private static final long serialVersionUID = -7215693141930401258L;

	/** 正则表达式 **/
	private final static String REGEX_STRING = "^\\s*(?i)(?:IsNull)\\s*\\(\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*\\,\\s*\\'(.+)\\'\\s*\\)\\s*$";
	private final static String REGEX_DIGIT = "^\\s*(?i)(?:IsNull)\\s*\\(\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*\\,\\s*([0-9\\.]+)\\s*\\)\\s*$";

	/** 默认参数 **/
	private Column defaultColumn;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeDefault(defaultColumn);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		defaultColumn = (Column) reader.readDefault();
	}

	/**
	 * 根据传入参数生成它的副本
	 * @param that IsNull实例
	 */
	private IsNull(IsNull that) {
		super(that);
		if (that.defaultColumn != null) {
			defaultColumn = that.defaultColumn.duplicate();
		}
	}

	/**
	 * 构造默认的IsNull函数
	 */
	public IsNull() {
		super();
		// 不允许产生默认列
		setSupportDefault(false);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.Function#duplicate()
	 */
	@Override
	public IsNull duplicate() {
		return new IsNull(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#matches(java.lang.String)
	 */
	@Override
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(REGEX_STRING);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			pattern = Pattern.compile(REGEX_DIGIT);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#create(com.laxcus.access.schema.Table, java.lang.String)
	 */
	@Override
	public ColumnFunction create(Table table, String primitive) {
		// 语法: IsNull(column_name, [digit|'string'])
		Pattern pattern = Pattern.compile(IsNull.REGEX_DIGIT);
		Matcher matcher = pattern.matcher(primitive);
		boolean match = matcher.matches();

		if (!match) {
			pattern = Pattern.compile(IsNull.REGEX_STRING);
			matcher = pattern.matcher(primitive);
			match = matcher.matches();
		}
		if (!match) {
			return null;
		}

		String name = matcher.group(1);
		String value = matcher.group(2);
		// 查找匹配的列
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throw new ColumnAttributeException("cannot find \'%s\'", name);
		}

		IsNull object = new IsNull();
		// 原语
		object.setPrimitive(primitive);
		// 返回属性
		object.setResultFamily(attribute.getType());
		// 列标识号
		object.setColumnId(attribute.getColumnId());

		// 所属表
		object.setSpace(table.getSpace());
		// 如果是可变长属性，记录打包配置
		if (attribute.isVariable()) {
			object.setPacking(((VariableAttribute) attribute).getPacking());
		}
		// 如果是字符串，记录是否大小写敏感
		if (attribute.isWord()) {
			object.setSentient(((WordAttribute) attribute).isSentient());
		}

		if(attribute.isRaw()) {
			try {
				defaultColumn = VariableGenerator.createRaw(table.isDSM(), (RawAttribute) attribute, value);
			} catch (IOException e) {
				throw new FunctionException(e);
			}
		} else if(attribute.isWord()) {
			try {
				defaultColumn = VariableGenerator.createWord(table.isDSM(), (WordAttribute) attribute, value);
			} catch (IOException e) {
				throw new FunctionException(e);
			}
		} else if (attribute.isShort()) {
			defaultColumn = NumberGenerator.createShort((ShortAttribute) attribute, value);
		} else if (attribute.isInteger()) {
			defaultColumn = NumberGenerator.createInteger((IntegerAttribute) attribute, value);
		} else if (attribute.isLong()) {
			defaultColumn = NumberGenerator.createLong((LongAttribute) attribute, value);
		} else if (attribute.isFloat()) {
			defaultColumn = NumberGenerator.createFloat((FloatAttribute) attribute, value);
		} else if (attribute.isDouble()) {
			defaultColumn = NumberGenerator.createDouble((DoubleAttribute) attribute, value);
		} else if (attribute.isDate()) {
			defaultColumn = CalendarGenerator.createDate((DateAttribute) attribute, value);
		} else if (attribute.isTime()) {
			defaultColumn = CalendarGenerator.createTime((TimeAttribute) attribute, value);
		} else if (attribute.isTimestamp()) {
			defaultColumn = CalendarGenerator.createTimestamp((TimestampAttribute) attribute, value);
		}

		return object;
	}

	/**
	 * 判断参数是否为空，否则返回一个新的列
	 * @see com.laxcus.access.function.table.ColumnFunction#makeup(java.util.List)
	 */
	@Override
	public Column makeup(List<Row> rows) {
		// 只能传入一行记录
		if (rows.size() != 1) {
			throw new FunctionException("sizeout");
		}

		Row row = rows.get(0);
		Column column = row.find(getColumnId());
		if (column == null) {
			throw new FunctionException("cannot find %d", getColumnId());
		}
		if (defaultColumn.getType() != column.getType()) {
			throw new FunctionException("cannot match! %d,%d",defaultColumn.getType(), column.getType());
		}

		// 如果是空值，返回默认参数，否则返回列本身
		if (column.isNull()) {
			return defaultColumn;
		} else {
			return column;
		}
	}

	/**
	 * IsNull函数不支持生成默认列
	 * @see com.laxcus.access.function.table.ColumnFunction#getDefault()
	 */
	@Override
	public Column getDefault() {
		return null;
	}

}