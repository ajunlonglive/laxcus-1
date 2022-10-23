/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

import java.io.*;
import java.util.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.util.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 可变长数据类型。<br><br>
 * 
 * 子类包括二进制数组（RAW）、可变长字符串（CHAR、WCHAR、HCHAR）、多媒体数据类型（文档、音频、视频、图像）。<br>
 * 可变长数据类型支持打包/解包(enpacking/depacking)的处理。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/9/2009
 * @since laxcus 1.0
 */
public abstract class Variable extends Column {

	private static final long serialVersionUID = 614742317821062945L;

	/** 原始字节数组(如果是字符类型，是编码后数据) **/
	protected byte[] value;

	/** 索引值 (如果是字符类型，从编码前的数据中截取一段) */
	protected byte[] index;

	/** 散列码  */
	protected transient int hash;

	/**
	 * 根据传入参数生成可变长字节数组列的副本
	 * @param that
	 */
	protected Variable(Variable that) {
		super(that);
		setValue(that.value);
		setIndex(that.index);
		hash = that.hash;
	}

	/**
	 * 构造可变长数据，指定它的列类型
	 * @param family 列类型
	 */
	protected Variable(byte family) {
		super(family);
		hash = 0; // 默认是0
	}

	/**
	 * 设置可长变数组列的数据值。如果是字符类型，输入的字节数组是编码后的内容
	 * @param b 字节数组
	 */
	public void setValue(byte[] b) {
		if (b == null) {
			this.setValue(null, 0, -1);
		} else {
			this.setValue(b, 0, b.length);
		}
	}

	/**
	 * 设置可长变数组列的数据值(如果是字符串，这里是编码后的值)
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 */
	public void setValue(byte[] b, int off, int len) {
		if (b == null || len < 0) {
			value = null;
		} else {
			// 允许0字节长度
			value = new byte[len];
			if (len > 0) {
				System.arraycopy(b, off, value, 0, len);
			}
		}
		setNull(value == null);
	}

	/**
	 * 返回可长变数组列的数据值(可能是加密、压缩状态)
	 * @return 字节数组
	 */
	public byte[] getValue() {
		return this.value;
	}

	/**
	 * 返回可变长数据列的原始数值(已经解密和解压缩)
	 * @param packing 封包
	 * @return 字节数组
	 */
	public byte[] getValue(Packing packing) {
		if (value != null && value.length > 0 && packing != null && packing.isEnabled()) {
			try {
				return VariableGenerator.depacking(packing, value, 0, value.length);
			} catch (IOException e) {
				Logger.error(e);
				return null;
			} catch (Throwable e) {
				Logger.fatal(e);
				return null;
			}
		}
		return value;
	}

	/**
	 * 判断可变长数组是否属于"空"状态(不是null，但是0字节长)
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return !super.isNull() && (value == null || value.length == 0);
	}

	/**
	 * 设置可变长数组列的索引(如果是字符串，已经编码)
	 * 
	 * @param b 字节数组
	 */
	public void setIndex(byte[] b) {
		if (b == null) {
			this.setIndex(null, 0, 0);
		} else {
			this.setIndex(b, 0, b.length);
		}
	}

	/**
	 * 设置可变长数组列的索引(如果是字符串，已经编码)
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 */
	public void setIndex(byte[] b, int off, int len) {
		if (b == null || len < 1) {
			index = null;
		} else {
			index = new byte[len];
			System.arraycopy(b, off, index, 0, len);
		}
	}

	/**
	 * 返回可变长数组列的索引
	 * 
	 * @return 字节数组
	 */
	public byte[] getIndex() {
		return this.index;
	}

	/**
	 * 返回首选数据，如果索引有效即返回索引，否则返回值
	 * @return 索引或者数值的字节数组
	 */
	public byte[] getPreferred() {
		if (this.index != null) {
			return this.index;
		}
		return this.value;
	}

	/**
	 * 比较参数是否一致。如果索引有效比较索引，否则比较数值
	 * @param that Variable实例
	 * @return 返回真或者假
	 */
	protected boolean equals(Variable that) {
		if (this.getType() != that.getType()) {
			return false;
		}
		if (!isNull() && !that.isNull()) {
			if (index != null) {
				return Laxkit.compareTo(index, that.index) == 0;
			} else {
				return Laxkit.compareTo(value, that.value) == 0;
			}
		}
		return isNull() == that.isNull();
	}

	/**
	 * 返回可变长数组列的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if(isNull()) {
			return 0;
		}

		if (this.hash == 0) {
			if (index != null && index.length > 0) {
				this.hash = Arrays.hashCode(index);
			} else if (value != null && value.length > 0) {
				this.hash = Arrays.hashCode(value);
			}
		}

		return this.hash;
	}

	/**
	 * 这是一个基本的列比较，继承自Column类。在不考虑封包<code>("Raw/Word"共有)</code>属性和小大写敏感<code>("Word"专有)</code>的前提下，
	 * 对字节数组按照字节排序进行比较。比较的依据是如果索引有效优先比较索引，否则比较数值。
	 * 如果考虑更细致的比较(即包装、小大写敏感、是否优先比较索引诸条件时)，请调用<code>"Raw"和"Word"类</code>去完成比较。
	 * @see com.laxcus.access.column.Column#compare(com.laxcus.access.column.Column)
	 */
	@Override
	public int compare(Column that) {
		if(that == null) return 1;
		else if (isNull() && that.isNull()) return 0;
		else if (isNull()) return -1;
		else if (that.isNull()) return 1;

		//		// 必须是可变长类型
		//		if (!(that instanceof Variable)) {
		//			throw new ClassCastException();
		//		}

		// 类型必须一致
		if(getType() != that.getType()) {
			throw new ClassCastException();
		}

		Variable var = (Variable) that;

		// 如果索引存在，比较索引；否则比较数值
		if (index != null || var.index != null) {
			return Laxkit.compareTo(index, var.index);
		} else {
			return Laxkit.compareTo(value, var.value);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Column#compare(com.laxcus.access.column.Column, boolean)
	 */
	@Override
	public int compare(Column that, boolean asc) {
		if(that == null) return 1;
		else if (isNull() && that.isNull()) return 0;
		else if (isNull()) return -1;
		else if (that.isNull()) return 1;

		// 类型必须一致
		if(getType() != that.getType()) {
			throw new ClassCastException();
		}

		Variable var = (Variable) that;

		// 如果索引存在，比较索引；否则比较数值
		if (index != null || var.index != null) {
			if (asc) {
				return Laxkit.compareTo(index, var.index);
			} else {
				return Laxkit.compareTo(var.index, index);
			}
		} else {
			if (asc) {
				return Laxkit.compareTo(value, var.value);
			} else {
				return Laxkit.compareTo(var.value, value);
			}
		}
	}
	
	/**
	 * 占用的空间长度
	 * @see com.laxcus.access.column.Column#capacity()
	 */
	@Override
	public int capacity() {
		if (isNull()) return 1;
		int size = 5;
		size += (index == null ? 0 : index.length);
		return size + (value == null ? 0 : value.length);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Column#hash(com.laxcus.access.column.attribute.ColumnAttribute)
	 */
	@Override
	public SHA256Hash hash(ColumnAttribute attribute) {
		// 产生全0值
		if (isNull()) {
			return new SHA256Hash((byte) 0);
		}
		if (value != null) {
			return Laxkit.doSHA256Hash(value);
		}
		if (index != null) {
			return Laxkit.doSHA256Hash(index);
		}
		// 产生全0的值
		return new SHA256Hash((byte) 0);
	}

	/**
	 * 将值域和索引域写入缓存。先写数据域，再写索引域
	 * @param writer 可类化数据存储器
	 */
	protected void buildVariable(ClassWriter writer) {
		ClassWriter buf = new ClassWriter();
		// 1. 写入值域(值长度和值数据)
		buf.writeInt(value.length);
		if (value.length > 0) {
			buf.write(value);
		}
		// 2.检查索引，索引如果不存在就忽略。
		int len = (index == null ? 0 : index.length);
		if (len > 0) {
			buf.writeInt(len);
			buf.write(index);
		}
		// 输出可变长列数据
		byte[] b = buf.effuse();

		// 写入总长度(含其自身4字节): maxsize = 4 + b.length。不包括标记
		writer.writeInt(4 + b.length);
		// 写入值
		writer.write(b);
	}

	/**
	 * 从缓存中解析索引和数据域。先数据再索引，索引是可以忽略的。
	 * @param reader 可类化读取器
	 */
	protected void resolveVariable(ClassReader reader) {
		// 总长度
		int maxsize = reader.readInt();
		// 1. 值域
		int size = reader.readInt();
		if (size > 0) {
			setValue(reader.read(size));
		} else {
			setValue(new byte[0]);
		}
		// 如果此条件成立，即没有索引。可以退出
		if (maxsize == 8 + size) { //8: maxsize(4) + value size(4)
			return;
		}
		// 2. 索引域
		size = reader.readInt();
		this.setIndex(reader.read(size));
	}

	/**
	 * 将可变长列数据写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
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

	/**
	 * 从可类化读取器中解析可变长列数据参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 1. 解析标记值
		resolveTag(reader.read());
		// 如果不是空值，解析数据和索引。如果是空值不处理。
		if (!isNull()) {
			this.resolveVariable(reader);
		}
		// 返回解析的长度
		return reader.getSeek() - scale;
	}

}