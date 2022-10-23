/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

import com.laxcus.util.classable.*;

/**
 * 数值列。<br>
 * 数值列长度固定，子类包括: short, int, long, float, double, date, time, timestamp
 * 
 * @author scott.liang
 * @version 1.0 3/10/2009
 * @since laxcus 1.0
 */
public abstract class Number extends Column {

	private static final long serialVersionUID = 5251609663677054366L;

	/**
	 * 构造一个数值列，指定它的列类型
	 * @param family 列类型
	 */
	protected Number(byte family) {
		super(family);
	}

	/**
	 * 根据传入参数构造数值列的副本
	 * @param that Number子类实例
	 */
	protected Number(Number that) {
		super(that);
	}

	/**
	 * 将一列信息生成为数组流信息
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		//1.生成列标记
		byte tag = buildTag();
		writer.write(tag);
		//2.如果不是空值，将数值写入
		if (!isNull()) {
			writer.write(getNumber());
		}
		//3. 返回写入长度
		return writer.size() - size;
	}

	/**
	 * 解析列信息，返回解析的长度
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		//1. 读列标记，解析出空值和列类型
		resolveTag(reader.read());
		//2. 如果不是空值，读取数值
		if (!isNull()) {
			// 按照数字类型，取它的字节长度
			int size = getNumberSize();
			byte[] b = reader.read(size);
			// 设置参数值
			setNumber(b, 0, b.length);
		}
		//3. 返回数据长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据数据流中的字节，转换为实际的数值(子类实现)
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 */
	public abstract void setNumber(byte[] b, int off, int len);

	/**
	 * 返回数值的字节数组描述
	 * @return 字节数组
	 */
	public abstract byte[] getNumber();

	/**
	 * 返回它的字节数
	 * @return int
	 */
	public abstract int getNumberSize();

}