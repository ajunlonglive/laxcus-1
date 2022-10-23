/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

import java.util.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 模糊检索关键字列(LIKE WORD)，数据来源于对应的字符列，包括：RCHAR、RWCHAR、RHCHAR<br>
 * 
 * @author scott.liang
 * @version 1.0 3/17/2009
 * @since laxcus 1.0
 */
public abstract class RWord extends Column {

	private static final long serialVersionUID = 931332538227900893L;

	/** 左右两侧被忽略的字长度。如果是-1表示无限制，否则有限制字长。 **/
	private short left = -1;

	private short right = -1;

	/** 模糊检索关键字 */
	private byte[] index;

	/** 散列码 **/
	private transient int hash;

	/**
	 * 构造一个默认的模糊关键字基础类
	 */
	protected RWord() {
		super();
		// 默认无限制
		left = right = -1;
		hash = 0;
	}

	/**
	 * 根据传入的模糊关键字列，生成一个它的副本
	 * @param that RWord实例
	 */
	protected RWord(RWord that) {
		super(that);
		left = that.left;
		right = that.right;
		setIndex(that.index);
		hash = that.hash;
	}

	/**
	 * 构造一个模糊关键字基础类，并且指定列类型
	 * @param family 列类型
	 */
	protected RWord(byte family) {
		super(family);
	}

	/**
	 * 构造模糊关键字基础类，并且指定它的列类型和列编号(列编号>1024)
	 * @param family 列类型
	 * @param columnId 列编号
	 */
	protected RWord(byte family, short columnId) {
		super(family, columnId);
	}

	/**
	 * 设置检索的关键字
	 * @param b 字节数组
	 */
	public void setIndex(byte[] b) {
		if (b == null || b.length == 0) {
			setIndex(null, 0, 0);
		} else {
			setIndex(b, 0, b.length);
		}
	}

	/**
	 * 设置检索的关键字
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 */
	public void setIndex(byte[] b, int off, int len) {
		if (b == null || b.length == 0 || len < 0) {
			index = null;
		} else {
			index = new byte[len];
			System.arraycopy(b, off, index, 0, len);
		}
		setNull(index == null);
	}

	/**
	 * 返回检索的关键字
	 * @return 字节数组
	 */
	public byte[] getIndex() {
		return index;
	}

	/**
	 * 设置左侧忽略字长度
	 * @param i short
	 */
	public void setLeft(short i) {
		left = i;
	}

	/**
	 * 返回左侧忽略字长度
	 * @return short
	 */
	public short getLeft() {
		return left;
	}

	/**
	 * 设置右侧忽略字长度
	 * @param i short
	 */
	public void setRight(short i) {
		right = i;
	}

	/**
	 * 返回右侧忽略字长度
	 * @return short
	 */
	public short getRight() {
		return right;
	}

	/**
	 * 设置忽略字范围长度
	 * @param left short
	 * @param right short
	 */
	public void setRange(short left, short right) {
		setLeft(left);
		setRight(right);
	}

	/**
	 * 比较模糊检索字是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || !(that instanceof Column)) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compare((Column) that) == 0;
	}

	/*
	 * 生成散列码
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (hash == 0) {
			if (index != null && index.length > 0) {
				hash = Arrays.hashCode(index);
			}
		}
		return hash ^ left ^ right;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.column.Column#compare(com.laxcus.access.column.Column)
	 */
	@Override
	public int compare(Column that) {
		if (getType() != that.getType()) { 
			return compareTo(that); // 如果列类型不一致时，按照列编号排列
		} else if (isNull() && that.isNull()) {
			return 0;
		} else if (isNull()) {
			return -1;
		} else if (that.isNull()) {
			return 1;
		}

		// 比较数值
		RWord column = (RWord) that;
		int ret = Laxkit.compareTo(left, column.left);
		if (ret == 0) {
			ret = Laxkit.compareTo(right, column.right);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(index, column.index);
		}
		return ret;
	}

	public int compare(Column that, boolean asc) {
		if (getType() != that.getType()) { 
			return compareTo(that); // 如果列类型不一致时，按照列编号排列
		} else if (isNull() && that.isNull()) {
			return 0;
		} else if (isNull()) {
			return -1;
		} else if (that.isNull()) {
			return 1;
		}

		// 比较数值
		RWord column = (RWord) that;
		int ret = 0;
		if (asc) {
			ret = Laxkit.compareTo(left, column.left);
			if (ret == 0) {
				ret = Laxkit.compareTo(right, column.right);
			}
			if (ret == 0) {
				ret = Laxkit.compareTo(index, column.index);
			}
		} else {
			ret = Laxkit.compareTo(column.left, left);
			if (ret == 0) {
				ret = Laxkit.compareTo(column.right, right);
			}
			if (ret == 0) {
				ret = Laxkit.compareTo(column.index, index);
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Column#capacity()
	 */
	@Override
	public int capacity() {
		if (isNull()) return 1;
		return 9 + (index == null ? 0 : index.length);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Column#hash(com.laxcus.access.column.attribute.ColumnAttribute)
	 */
	@Override
	public SHA256Hash hash(ColumnAttribute attribute) {
		if (isNull()) {
			return new SHA256Hash((byte) 0);
		}

		// 生成散列码
		ClassWriter writer = new ClassWriter();
		writer.writeInt(left);
		writer.writeInt(right);
		if (index != null) {
			writer.write(index);
		}
		// 返回结果
		return new SHA256Hash(writer.effuse());
	}

	/**
	 * 将模糊检索关键字写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 1.标记值
		byte tag = buildTag();
		writer.write(tag);
		// 2.非空状态时...
		if (!isNull()) {
			// 索引数组长度
			int len = (index == null ? 0 : index.length);
			// 总长度. maxsize = 8 + 索引长度
			writer.writeInt(8 + len);
			// 左侧忽略字符长
			writer.writeShort(left);
			// 右侧忽略字符长
			writer.writeShort(right);
			// 索引值
			if (len > 0) {
				writer.write(index);
			}
		}
		// 返回写入的字节长度
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析模糊检索关键字参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 读标记值
		resolveTag(reader.read());
		// 非空状态时...
		if (!isNull()) {
			// 总长度
			int total = reader.readInt();
			// 左侧忽略字长度
			left = reader.readShort();
			// 右侧忽略字长度
			right = reader.readShort();
			// 模糊检索关键字长度
			int len = total - 8;
			// 读索引
			if (len > 0) {
				setIndex(reader.read(len));
			}
		}
		// 返回读取的字节数组长度
		return reader.getSeek() - scale;
	}

}