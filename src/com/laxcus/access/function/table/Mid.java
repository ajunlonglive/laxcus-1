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
import com.laxcus.util.classable.*;

/**
 * <code>SQL MID</code>函数。从列数据中提取指定位置的数据，只针对可变长类型
 * 
 * @author scott.liang
 * @version 1.0 11/2/2009
 * @since laxcus 1.0
 */
public final class Mid extends ColumnFunction {

	private static final long serialVersionUID = -8534446546327356610L;

	/** MID函数格式: MID(column-name, start, [length]), length可选 **/
	private final static String REGEX1 = "^\\s*(?i)(?:MID)\\s*\\(\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*\\,\\s*(\\d+)\\s*\\,\\s*(\\d+)\\s*\\)\\s*$";
	private final static String REGEX2 = "^\\s*(?i)(?:MID)\\s*\\(\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*\\,\\s*(\\d+)\\s*\\)\\s*$";

	/** 截取字符的开始位置(下标) **/
	private int start;

	/** 从下标开始，截取的字符/字节长度 **/
	private int length;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(start);
		writer.writeInt(length);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		start = reader.readInt();
		length = reader.readInt();
	}

	/**
	 * 根据传入的MID函数，生成它的副本
	 * @param that Mid实例
	 */
	private Mid(Mid that) {
		super(that);
		this.start = that.start;
		this.length = that.length;
	}

	/**
	 * 构造一个默认的MID函数
	 */
	public Mid() {
		super();
		// 未定义值
		start = length = -1;
		// 不允许产生默认列
		super.setSupportDefault(false);
	}


	/**
	 * 设置提取的开始位置
	 * @param i 提取的开始位置
	 */
	public void setStart(int i) {
		this.start = i;
	}

	/**
	 * 返回提取的开始位置
	 * @return 提取的开始位置
	 */
	public int getStart() {
		return this.start;
	}

	/**
	 * 设置提取的长度
	 * @param i 提取的长度
	 */
	public void setLength(int i) {
		this.length = i;
	}

	/**
	 * 返回提取的长度
	 * @return 提取的长度
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * 根据当前MID函数，生成一个它的副本
	 * @see com.laxcus.access.function.Function#duplicate()
	 */
	@Override
	public Mid duplicate() {
		return new Mid(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#matches(java.lang.String)
	 */
	@Override
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			pattern = Pattern.compile(REGEX2);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#create(com.laxcus.access.schema.Table, java.lang.String)
	 */
	@Override
	public ColumnFunction create(Table table, String primitive) {
		Pattern pattern = Pattern.compile(Mid.REGEX1);
		Matcher matcher = pattern.matcher(primitive);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(Mid.REGEX2);
			matcher = pattern.matcher(primitive);
			match = matcher.matches();
		}
		// 全部不匹配时，返回NULL
		if (!match) {
			return null;
		}

		// 取列名称
		String name = matcher.group(1);
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throw new ColumnAttributeException("cannot find '%s'", name);
		}
		// 非可变长类型不支持
		if(!attribute.isVariable()) {
			throw new ColumnAttributeException("cannot support '%s'", name);
		}

		Mid mid = new Mid();
		mid.setResultFamily(attribute.getType());
		mid.setPrimitive(primitive.trim());
		mid.setColumnId(attribute.getColumnId());
		// 截取开始位置
		mid.start = java.lang.Integer.parseInt(matcher.group(2));
		// 截取长度
		if (matcher.groupCount() == 3) {
			mid.length = java.lang.Integer.parseInt(matcher.group(3));
		}

		// 如果是可变长属性，记录打包配置
		if (attribute.isVariable()) {
			mid.setPacking(((VariableAttribute) attribute).getPacking());
		}
		// 如果是字符串，记录是否大小写敏感
		if (attribute.isWord()) {
			mid.setSentient(((WordAttribute) attribute).isSentient());
		}

		return mid;
	}

	/**
	 * 确定结束位置
	 * @param total
	 * @return
	 */
	private int ends(int total) {
		if (length == -1 || start + length > total) {
			return total;
		} else {
			return start + length;
		}
	}

	/**
	 * 截取一段字节
	 * @param b
	 * @return
	 */
	private byte[] subytes(byte [] b) {
		if(start > b.length) {
			return new byte[0];
		}
		int end = ends(b.length);
		return Arrays.copyOfRange(b, start, end);
	}

	/**
	 * 截取一段字符串
	 * @param s
	 * @return
	 */
	private String substring(String s) {
		if (start >= s.length()) {
			return "";
		}
		int end = ends(s.length());
		return s.substring(start, end);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#makeup(java.util.List)
	 */
	@Override
	public Column makeup(List<Row> rows) {
		// 只能传入一行记录
		if(rows.size() != 1) {
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

		// 取出数值(原始字节流)
		byte[] value = ((Variable) column).getValue();

		// 解包
		Packing packing = super.getPacking();
		if (packing != null && packing.isEnabled()) {
			try {
				value = VariableGenerator.depacking(packing, value, 0, value.length);
			} catch (IOException e) {
				Logger.error(e);
			}
		}

		// 如果是字符类型，在解码后计算它的字符串长度
		if (column.isRaw()) {
			value = this.subytes(value);
		} else if (column.isWord()) {
			Charset charset = ((Word) column).getCharset();
			String s = charset.decode(value, 0, value.length);
			// 截取子字符串
			s = this.substring(s);
			// 重新编码
			value = charset.encode(s);
		}

		// 重新包装
		if (packing != null && packing.isEnabled()) {
			try {
				value = VariableGenerator.enpacking(packing, value, 0, value.length);
			} catch (IOException e) {
				Logger.error(e);
			}
		}

		// 处理结果
		Column result = null;
		if (column.isRaw()) {
			result = new com.laxcus.access.column.Raw((short) 0, value);
		} else if (column.isChar()) {
			result = new com.laxcus.access.column.Char((short) 0, value);
		} else if (column.isWChar()) {
			result = new com.laxcus.access.column.WChar((short) 0, value);
		} else if (column.isHChar()) {
			result = new com.laxcus.access.column.HChar((short) 0, value);
		}
		return result;
	}

	/**
	 * MID函数不允许产生默认列
	 * @see com.laxcus.access.function.table.ColumnFunction#getDefault()
	 */
	@Override
	public Column getDefault() {
		return null;
	}

}