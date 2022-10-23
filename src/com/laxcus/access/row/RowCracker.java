/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.row;

import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 行解析器
 * 
 * @author scott.liang
 * @version 1.1 9/17/2015
 * @since laxcus 1.0
 */
public class RowCracker {

	/** 列属性序列表(区别与Table) **/
	private Sheet sheet;

	/** 行记录的存储器 */
	private ArrayList<Row> buffs = new ArrayList<Row>(1024);

	/**
	 * 构造默认的行解析器
	 */
	private RowCracker() {
		super();
	}

	/**
	 * 构造行解析器，指定列属性序列表
	 * @param sheet Sheet实例
	 */
	public RowCracker(Sheet sheet) {
		this();
		setSheet(sheet);
	}

	/**
	 * 设置列属性序列表，不允许空指针
	 * @param e Sheet实例
	 */
	public void setSheet(Sheet e) {
		Laxkit.nullabled(e);

		sheet = e;
	}

	/**
	 * 返回列属性序列表
	 * @return Sheet实例
	 */
	public Sheet getSheet() {
		return sheet;
	}

	/**
	 * 输入内存中的行记录，同时清除内存已有数据
	 * @return Row列表
	 */
	public List<Row> flush() {
		int size = buffs.size();
		ArrayList<Row> array = new ArrayList<Row>(size);
		// 保存到副本
		array.addAll(buffs);
		// 清除缓存
		buffs.clear();
		// 输出副本
		return array;
	}
	
	/**
	 * 以数组方式，输出全部行记录，同时清除内存已有记录
	 * @return 行的数组形式
	 */
	public Row[] efflux() {
		List<Row> a = flush();
		Row[] rows = new Row[a.size()];
		return a.toArray(rows);
	}
	
	/**
	 * 指定尺寸，输出一组行记录，同时清除内存已有记录
	 * @param size 限制以其
	 * @return 返回字节数组
	 */
	public Row[] efflux(int size) {
		ArrayList<Row> a = new ArrayList<Row>(size);

		while (hasRows()) {
			Row row = poll();
			a.add(row);
			if (a.size() >= size) {
				break;
			}
		}

		// 输出一组记录
		Row[] rows = new Row[a.size()];
		return a.toArray(rows);
	}

	/**
	 * 判断有记录
	 * @return 返回真或者假
	 */
	public boolean hasRows() {
		return buffs.size() > 0;
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 统计行数
	 * @return 行数
	 */
	public int size() {
		return buffs.size();
	}

	/**
	 * 提取并且删除数组中的第一行记录
	 * @return 行记录对象，如果数据为空(empty)，返回一个空指针
	 */
	public Row poll() {
		// 如果集合是空的，返回空指针
		if (buffs.isEmpty()) {
			return null;
		}

		// 每次弹出第一个记录
		Row row = buffs.remove(0);
		return row;
	}

	/**
	 * 解析一组NSM行数据。记录保存到内存中，返回解析的数据流长度
	 * 
	 * @param b NSM字节数组
	 * @return 返回解析的字节长度
	 */
	public int split(byte[] b) {
		if (Laxkit.isEmpty(b)) {
			throw new NullPointerException();
		}
		return split(b, 0, b.length);
	}

	/**
	 * 解析一组NSM行数据。记录保存到内存中，返回解析的数据流长度
	 * @param b NSM字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 返回解析的字节长度
	 */
	public int split(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return split(reader);
	}

	/**
	 * 从可类化数据读取器中解析NSM格式数据。记录保存到内存中，返回解析的数据流长度
	 * @param reader 可类化数据读取器
	 * @return 返回解析的字节长度
	 */
	public int split(ClassReader reader) {
		// 开始下标
		final int seek = reader.getSeek();
		// 匹配的列成员数
		final int columns = sheet.size();

		while (reader.getLeft() > 0) {
			// 解析行头标识
			RowTag tag = new RowTag(reader);

			// 如果不一致，是错误
			if (tag.columns != columns) {
				throw new RowParseException("illegal column! columns size:%d, sheet size:%d", 
						tag.columns, columns);
			}

			Row row = new Row(tag);
			// 解析列
			for (int index = 0; index < tag.columns; index++) {
				// 取得列属性
				ColumnAttribute attribute = sheet.get(index);
				if (attribute == null) {
					throw new RowParseException("cannot be find attribute at %d", index);
				}

				// 状态符（包括空值和列数据类型）
				byte state = reader.current();
				// 列数据类型
				byte family = ColumnType.resolveType(state);
				// 根据列属性建立对应的"列"
				Column column = ColumnCreator.create(family);
				if (column == null) {
					throw new RowParseException("illegal column: %d", family & 0xFF);
				}
				// 比较数据类型匹配
				if (attribute.getType() != column.getType()) {
					throw new RowParseException("cannot be match! %s - %s",
							ColumnType.translate(attribute.getType()), ColumnType.translate(column.getType()));
				}

				// 解析"列"并返回解析长度
				column.resolve(reader);
				// 设置列标识号
				column.setId(attribute.getColumnId());
				// 保存一列
				row.add(column);
			}

			row.trim();

			buffs.add(row);
		}

		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

}