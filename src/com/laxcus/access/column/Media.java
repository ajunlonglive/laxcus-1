/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

import com.laxcus.access.column.attribute.*;
import com.laxcus.util.classable.*;

/**
 * 多媒体列。<br>
 * 包括文档、图像、音频、视频，或者未来其它类型的扩展。
 * 
 * @author scott.liang
 * @version 1.0 4/25/2009
 * @since laxcus 1.0
 */
public abstract class Media extends Variable {

	private static final long serialVersionUID = -4348530329563555787L;

	/**
	 * 根据传入的对象生成它的浅层数据副本
	 * @param that
	 */
	protected Media(Media that) {
		super(that);
	}

	/**
	 * 构造一个多媒体列，并且指定它的数据类型
	 * @param family 数据类型
	 */
	protected Media(byte family) {
		super(family);
	}

	/*
	 * 媒体列占用的空间长度
	 * @see com.laxcus.access.column.Variable#capacity()
	 */
	@Override
	public int capacity() {
		if (isNull()) return 1;
		return super.capacity();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Variable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size(); // 刻度
		// 写入标记值
		byte tag = buildTag();
		writer.write(tag);
		// 如果不是空值，将数据定性主。如果是空值，它只有1个字节。
		if (!isNull()) {
			buildVariable(writer);
		}
		// 返回写入数据的字节长度
		return writer.size() - scale;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Variable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 1. 解析标记值
		resolveTag(reader.read());
		// 如果不是空值，解析数据和索引。如果是空值不处理。
		if (!isNull()) {
			resolveVariable(reader);
		}
		// 返回解析的长度
		return reader.getSeek() - scale;
	}

	/**
	 * 返回二进制字节数组的16进制字符串格式
	 * @param packing 封包
	 * @param limit 限制长度，可以是-1
	 * @return 转换成16进制后的字符串
	 */
	public String toString(Packing packing, int limit) {
		if (isNull()) {
			return null;
		}

		// 返回解包后的数据流
		byte[] b = super.getValue(packing);
		if (b == null || b.length == 0) {
			return "";
		}
		
		int max = 256;
		// 生成16进制字符流，加“0x”前缀，表示为二进制数组
		StringBuilder buff = new StringBuilder("0x");
		for (int i = 0; i < b.length; i++) {
			String s = String.format("%X", b[i] & 0xFF);
			// 增加
			if (s.length() == 1) {
				buff.append('0');
				buff.append(s);
			} else {
				buff.append(s);
			}
			// 达到最大值，忽略...
			if (limit > 0 && i + 1 == limit) {
				buff.append("...");
				break;
			} else if (i + 1 >= max) {
				buff.append("...");
				break;
			}
		}
		return buff.toString();
	}

	/**
	 * 返回二进制字节数组的字符串格式
	 * @param packing 封包
	 * @return 转换后的字符串
	 */
	public String toString(Packing packing) {
		return toString(packing, -1);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString(null, -1);
	}
}