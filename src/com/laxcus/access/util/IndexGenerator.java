/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.util;

import java.io.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.index.*;
import com.laxcus.access.type.*;
import com.laxcus.util.each.*;

/**
 * SQL查询索引生成器。<BR>
 * 
 * @author scott.liang
 * @version 1.0 12/17/2011
 * @since laxcus 1.0
 */
public final class IndexGenerator {

	/**
	 * 建立一个空值索引
	 * @param attribute - 列索引
	 * @param nullable - 是否null状态
	 * @return 列索引
	 */
	public static ColumnIndex createNullIndex(ColumnAttribute attribute, boolean nullable) throws IOException {
		// 根据参数生成列
		Column column = ColumnCreator.create(attribute.getType());
		if (column == null) {
			throw new IOException("illegal " + attribute.getType());
		}

		ColumnIndex index = null;
		switch (attribute.getType()) {
		case ColumnType.SHORT:
			index = new ShortIndex((short) 0, column);
			break;
		case ColumnType.DATE:
		case ColumnType.TIME:
		case ColumnType.INTEGER:
			index = new IntegerIndex(0, column);
			break;
		case ColumnType.RAW:
		case ColumnType.CHAR:
		case ColumnType.WCHAR:
		case ColumnType.HCHAR:
		case ColumnType.TIMESTAMP:
		case ColumnType.LONG:
		case ColumnType.DOCUMENT:
		case ColumnType.IMAGE:
		case ColumnType.AUDIO:
		case ColumnType.VIDEO:
			index = new LongIndex(0L, column);
			break;
		case ColumnType.FLOAT:
			index = new FloatIndex(0.0f, column);
			break;
		case ColumnType.DOUBLE:
			index = new DoubleIndex(0.0f, column);
			break;
		}
		if (index == null) {
			throw new IOException("invalid column: " + attribute.getType());
		}

		index.getColumn().setId(attribute.getColumnId());
		index.getColumn().setNull(nullable);

		return index;
	}

	/**
	 * 建立一个图像列索引
	 * @param dsm 列存储模型
	 * @param attribute 图像列属性
	 * @param value 图像值
	 * @return 长整型列索引
	 * @throws IOException
	 */
	public static LongIndex createImageIndex(boolean dsm,
			ImageAttribute attribute, byte[] value) throws IOException {
		Image column = VariableGenerator.createImage(dsm, attribute, value);
		column.setId(attribute.getColumnId());
		byte[] b = column.getPreferred();
		long hash = EachTrustor.sign(b, 0, b.length);
		return new LongIndex(hash, column);
	}

	/**
	 * 建立一个文档列索引
	 * @param dsm 列存储模型
	 * @param attribute 文档列属性
	 * @param value 文档值
	 * @return 长整型列索引
	 * @throws IOException
	 */
	public static LongIndex createDocumentIndex(boolean dsm,
			DocumentAttribute attribute, byte[] value) throws IOException {
		Document column = VariableGenerator.createDocument(dsm, attribute, value);
		column.setId(attribute.getColumnId());
		byte[] b = column.getPreferred();
		long hash = EachTrustor.sign(b, 0, b.length);
		return new LongIndex(hash, column);
	}

	/**
	 * 建立一个音频列索引
	 * @param dsm 列存储模型
	 * @param attribute 音频列属性
	 * @param value 音频值
	 * @return 长整型列索引
	 * @throws IOException
	 */
	public static LongIndex createAudioIndex(boolean dsm,
			AudioAttribute attribute, byte[] value) throws IOException {
		Audio column = VariableGenerator.createAudio(dsm, attribute, value);
		column.setId(attribute.getColumnId());
		byte[] b = column.getPreferred();
		long hash = EachTrustor.sign(b, 0, b.length);
		return new LongIndex(hash, column);
	}

	/**
	 * 建立一个视频列索引
	 * @param dsm 列存储模型
	 * @param attribute 视频列属性
	 * @param value 视频值
	 * @return 长整型列索引
	 * @throws IOException
	 */
	public static LongIndex createVideoIndex(boolean dsm,
			VideoAttribute attribute, byte[] value) throws IOException {
		Video column = VariableGenerator.createVideo(dsm, attribute, value);
		column.setId(attribute.getColumnId());
		byte[] b = column.getPreferred();
		long hash = EachTrustor.sign(b, 0, b.length);
		return new LongIndex(hash, column);
	}

	/**
	 * 建立一个二进制字节数组索引
	 * @param dsm 列存储模型
	 * @param attribute 字节数组列属性
	 * @param value 字节数组
	 * @return 长整弄列索引
	 * @throws IOException
	 */
	public static LongIndex createRawIndex(boolean dsm, RawAttribute attribute,
			byte[] value) throws IOException {
		Raw column = VariableGenerator.createRaw(dsm, attribute, value);
		column.setId(attribute.getColumnId());
		byte[] b = column.getPreferred();
		long hash = EachTrustor.sign(b, 0, b.length);
		return new LongIndex(hash, column);
	}

	/**
	 * 建立一个二进制字节数组索引
	 * @param dsm 列存储模型
	 * @param attribute 字节数组列属性
	 * @param value 16进制格式字符串
	 * @return 长整型列索引
	 * @throws IOException
	 */
	public static LongIndex createRawIndex(boolean dsm,
			RawAttribute attribute, String value) throws IOException {
		byte[] b = VariableGenerator.htob(value);
		return IndexGenerator.createRawIndex(dsm, attribute, b);
	}

	/**
	 * 建立一个字符索引
	 * @param dsm 列存储模型
	 * @param attribute 字符列属性
	 * @param value 字符值
	 * @return 长整型列索引
	 * @throws IOException
	 */
	public static LongIndex createWordIndex(boolean dsm, WordAttribute attribute,
			String value) throws IOException {
		Word column = VariableGenerator.createWord(dsm, attribute, value);
		column.setId(attribute.getColumnId());
		byte[] b = column.getPreferred();
		long hash = EachTrustor.sign(b, 0, b.length);
		return new LongIndex(hash, column);
	}

	/**
	 * 建立一个模糊检索索引
	 * @param dsm 列存储模型
	 * @param attribute 字符列属性
	 * @param left 左侧空长度
	 * @param right 右侧空长度
	 * @param value 字符值
	 * @return 长整型列索引
	 * @throws IOException
	 */
	public static LongIndex createLikeIndex(boolean dsm, WordAttribute attribute,
			short left, short right, String value) throws IOException {
		if (left == 0 && right == 0) {
			return IndexGenerator.createWordIndex(dsm, attribute, value);
		} else {
			RWord column = null;
			if (attribute.isChar()) {
				column = VariableGenerator.createRChar((CharAttribute) attribute, left, right, value);
			} else if (attribute.isWChar()) {
				column = VariableGenerator.createRWChar((WCharAttribute) attribute, left, right, value);
			} else if (attribute.isHChar()) {
				column = VariableGenerator.createRHChar((HCharAttribute) attribute, left, right, value);
			}
			byte[] b = column.getIndex();
			long hash = EachTrustor.sign(b, 0, b.length);

			return new LongIndex(hash, column);
		}
	}

	/**
	 * 建立一个日期索引
	 * @param value 日期值
	 * @param attribute 日期列属性
	 * @return 整型列索引
	 */
	public static IntegerIndex createDateIndex(int value, ColumnAttribute attribute) {
		com.laxcus.access.column.Date column = new com.laxcus.access.column.Date(attribute.getColumnId(), value);
		return new IntegerIndex(value, column);
	}

	/**
	 * 建立一个时间索引
	 * @param value 时间值
	 * @param attribute 时间列属性
	 * @return 整型列索引
	 */
	public static IntegerIndex createTimeIndex(int value, ColumnAttribute attribute) {
		com.laxcus.access.column.Time column = new com.laxcus.access.column.Time(attribute.getColumnId(), value);
		return new IntegerIndex(value, column);
	}

	/**
	 * 建立一个时间戳索引
	 * @param value 时间戳值
	 * @param attribute 时间戳列属性
	 * @return 长整型列索引
	 */
	public static LongIndex createTimestampIndex(long value, ColumnAttribute attribute) {
		com.laxcus.access.column.Timestamp column = new com.laxcus.access.column.Timestamp(attribute.getColumnId(), value);
		return new LongIndex(value, column);
	}

	/**
	 * 建立一个短整型索引
	 * @param value 短整型值
	 * @param attribute 短整型列属性
	 * @return 短整型列索引
	 */
	public static ShortIndex createShortIndex(short value, ColumnAttribute attribute) {
		com.laxcus.access.column.Short column = new com.laxcus.access.column.Short(attribute.getColumnId(), value);
		return new ShortIndex(value, column);
	}

	/**
	 * 建立一个整形索引
	 * @param value 整型值
	 * @param attribute 整型列属性
	 * @return 整型列索引
	 */
	public static ColumnIndex createIntegerIndex(int value, ColumnAttribute attribute) {
		com.laxcus.access.column.Integer column = new com.laxcus.access.column.Integer(attribute.getColumnId(), value);
		return new IntegerIndex(value, column);
	}

	/**
	 * 建立一个长整型索引
	 * @param value 长整型值
	 * @param attribute 长整型列属性
	 * @return 长整型列索引
	 */
	public static LongIndex createLongIndex(long value, ColumnAttribute attribute) {
		com.laxcus.access.column.Long column = new com.laxcus.access.column.Long(attribute.getColumnId(), value);
		return new LongIndex(value, column);
	}

	/**
	 * 建立一个单浮点索引
	 * @param value 单浮点值
	 * @param attribute 单浮点列属性
	 * @return 单浮点列索引
	 */
	public static FloatIndex createFloatIndex(float value, ColumnAttribute attribute) {
		com.laxcus.access.column.Float column = new com.laxcus.access.column.Float(attribute.getColumnId(), value);
		return new FloatIndex(value, column);
	}

	/**
	 * 建立一个双浮点数字索引
	 * @param value 双浮点值
	 * @param attribute 双浮点列属性
	 * @return 双浮点列索引
	 */
	public static DoubleIndex createDoubleIndex(double value, ColumnAttribute attribute) {
		com.laxcus.access.column.Double column = new com.laxcus.access.column.Double(attribute.getColumnId(), value);
		return new DoubleIndex(value, column);
	}

//	/**
//	 * 根据索引建立列索引
//	 * @param attribute 列属性
//	 * @return 返回ColumnIndex实例
//	 */
//	public static ColumnIndex createColumnIndex(ColumnAttribute attribute) {
//		Column column = ColumnCreator.create(attribute.getType());
//		column.setId(attribute.getColumnId());
//		// 短整型
//		if (column.isShort()) {
//			return new ShortIndex((short) 0, column);
//		}
//		// 整数
//		else if (column.isInteger() || column.isDate() || column.isTime()) {
//			return new IntegerIndex(0, column);
//		}
//		// 单浮点
//		else if (column.isFloat()) {
//			return new FloatIndex(0, column);
//		}
//		// 双浮点
//		else if (column.isDouble()) {
//			return new DoubleIndex(0, column);
//		}
//		// 以下是可变长数据类型
//		return new LongIndex(0, column);
//	}

}