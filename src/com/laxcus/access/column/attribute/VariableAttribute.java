/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column.attribute;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 可变长数组属性，子类包括：RAW、CHAR、WCHAR、HCHAR
 * 
 * @author scott.liang
 * @version 1.0 4/23/2009
 * @since laxcus 1.0
 */
public abstract class VariableAttribute extends ColumnAttribute {

	private static final long serialVersionUID = 4142942021574224998L;

	/** 索引长度限制, 16个字符 **/
	public final static int INDEX_LIMIT = 16;

	/** 压缩/加密属性 **/
	protected Packing packing = new Packing();

	/** 默认值 (适用于RAW、CHAR、WCHAR、HCHAR) **/
	protected byte[] value;

	/** 最大索引长度(限可变长类型:RAW、CHAR、WCHAR、HCHAR) **/
	protected int indexSize;

	/** 默认索引 **/
	protected byte[] index;

	/**
	 * 建立一个可变长数组列属性，并且指定它的数据类型
	 * @param family 数据类型
	 */
	protected VariableAttribute(byte family) {
		super(family);
		setIndexSize(VariableAttribute.INDEX_LIMIT);
	}

	/**
	 * 根据传入的可变长数组列实例建立可变长数组列属性的副本
	 * @param that VariableAttribute实例
	 */
	protected VariableAttribute(VariableAttribute that) {
		super(that);
		packing = that.packing.duplicate();
		indexSize = that.indexSize;
		setValue(that.value);
		setIndex(that.index);
	}

	/**
	 * 数据打包配置
	 * 
	 * @param compress - 压缩算法标识号
	 * @param encrypt - 加密算法标识号
	 * @param password - 加密密码
	 */
	public void setPacking(int compress, int encrypt, byte[] password) {
		packing.setPacking(compress, encrypt, password);
	}

	/**
	 * 设置数据封装
	 * @param e Packing实例
	 */
	public void setPacking(Packing e) {
		Laxkit.nullabled(e);
		// 赋值
		packing = e.duplicate();
	}

	/**
	 * 返回数据封装
	 * @return Packing实例
	 */
	public Packing getPacking() {
		return packing;
	}

	/**
	 * 最大索引长度
	 * 
	 * @param size 整型索引长度
	 */
	public void setIndexSize(int size) {
		indexSize = size;
	}

	/**
	 * 最大索引长度
	 * 
	 * @return 整型索引长度
	 */
	public int getIndexSize() {
		return indexSize;
	}

	/**
	 * 设置默认值
	 * 
	 * @param b 字节数组
	 * @return 设置成功返回真，否则假
	 */
	public boolean setValue(byte[] b) {
		return setValue(b, 0, (b == null ? 0 : b.length));
	}

	/**
	 * 设置默认值
	 * 
	 * @param b  字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 设置成功返回真，否则假
	 */
	public boolean setValue(byte[] b, int off, int len) {
		if (len < 1 || !super.isSetStatus()) {
			return false;
		}

		value = new byte[len];
		System.arraycopy(b, off, value, 0, len);
		// 非空状态
		setNull(false);

		return true;
	}

	/**
	 * 返回默认值
	 * 
	 * @return 字节数组
	 */
	public byte[] getValue() {
		return value;
	}

	/**
	 * 设置默认索引 (from value)
	 * 
	 * @param b 字节数组
	 */
	public void setIndex(byte[] b) {
		setIndex(b, 0, (b == null ? 0 : b.length));
	}

	/**
	 * 设置默认索引 (from value)
	 * 
	 * @param b  字节数组
	 * @param off 下标
	 * @param len 长度
	 */
	public void setIndex(byte[] b, int off, int len) {
		if (b == null || len == 0) {
			index = null;
		} else {
			index = new byte[len];
			System.arraycopy(b, off, index, 0, len);
		}
	}

	/**
	 * 返回默认索引
	 * 
	 * @return 字节数组
	 */
	public byte[] getIndex() {
		return index;
	}
	
	/**
	 * 将可变长列属性参数输出到可类化存储器，兼容C接口。
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 可变长数据类型的最大索引长度
		writer.writeInt(indexSize);
		// 封装参数
		writer.writeObject(packing);
		
		// 默认可变长值长度和值
		int len = (value == null ? 0 : value.length);
		writer.writeInt(len);
		if (len > 0) {
			writer.write(value, 0, len);
		}
		// 默认索引长度和索引
		len = (index == null ? 0 : index.length);
		writer.writeInt(len);
		if (len > 0) {
			writer.write(index, 0, len);
		}
	}
	
	/**
	 * 从可类化读取器中解析可变长列属性，兼容C接口。
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 可变长数据类型的最大索引长度限制
		indexSize = reader.readInt();
		// 封装参数
		packing.resolve(reader);

		// 可变长数据类型的值长度和值
		int len = reader.readInt();
		if (len > 0) {
			setValue(reader.read(len));
		}
		// 可变长数据类型的索引长度和值
		len = reader.readInt();
		if (len > 0) {
			setIndex(reader.read(len));
		}
	}

}