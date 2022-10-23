/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.mid;

import java.util.*;

import com.laxcus.util.*;

/**
 * 内存数据单元。<br>
 * 首先在内存中保存一段数据，以后允许在这段数据内更新和读取。
 * 
 * @author scott.liang
 * @version 1.0 12/2/2012
 * @since laxcus 1.0
 */
public final class MemoryItem implements Comparable<MemoryItem>{

	/** 索引编号 **/
	private int index;

	/** 内存数据 **/
	private byte[] data;

	/**
	 * 构造默认的内存数据单元
	 */
	private MemoryItem() {
		super();
		index = -1;
	}

	/**
	 * 构造内存数据单元，指定索引编号
	 * @param index 索引编号
	 */
	public MemoryItem(int index) {
		this();
		setIndex(index);
	}

	/**
	 * 设置索引编号
	 * @param i 索引编号
	 */
	public void setIndex(int i) {
		index = i;
	}

	/**
	 * 返回索引编号
	 * @return 索引编号
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * 返回当前字节数组长度
	 * 
	 * @return 字节数组长度
	 */
	public int length() {
		if (data == null) {
			return 0;
		}
		return data.length;
	}

	/**
	 * 返回数组长度
	 * @return
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * 追加一项数据
	 * @param b 输入的数组
	 * @param off 数组下标
	 * @param len 有效数据长度
	 * @return 返回写入的长度
	 * @throws OutOfMemoryError 存在内存分配失败的错误
	 */
	public int append(byte[] b, int off, int len) {
		if (data != null) {
			throw new IllegalValueException("array has be defined!");
		}

		data = new byte[len];
		System.arraycopy(b, off, data, 0, len);
		return data.length;
	}

	/**
	 * 更新数据（在已经存在的范围内更新）
	 * @param seek 指定下标位置
	 * @param b 输入的数组
	 * @param off 数组下标
	 * @param len 有效数据长度
	 * @return 返回写入的字节长度
	 */
	public int update(int seek, byte[] b, int off, int len) {
		if (seek < 0 || seek >= data.length) {
			throw new IndexOutOfBoundsException("cannot be " + seek);
		}
		// 剩余长度
		int left = data.length - seek;
		// 剩余长度超过要求长度，以要求长度为准
		if (left > len) {
			left = len;
		}
		// 覆盖指定位置的数据
		System.arraycopy(b, off, data, seek, left);
		// 返回写入的字节
		return left;
	}

	/**
	 * 从指定下标处读数据
	 * @param seek 数组下标位置
	 * @param len 读取的数据长度
	 * @return 返回字节数组
	 * @throws OutOfMemoryError 存在内存分配失败的错误
	 */
	public byte[] read(int seek, int len) {
		if (seek < 0 || seek >= data.length) {
			throw new IllegalValueException("illegal offset:%d", seek);
		}
		// 剩余长度
		int left = data.length - seek;
		// 剩余长度超过要求长度时，以要求长度为准
		if (left > len) {
			left = len;
		}
		// 读取指定的长度
		return Arrays.copyOfRange(data, seek, seek + left);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(MemoryItem that) {
		return Laxkit.compareTo(index, that.index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		index = -1;
		if (data != null) {
			data = null;
		}
	}
}