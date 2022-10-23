/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.mid;

import java.util.*;

import com.laxcus.util.range.*;

/**
 * 内存数据栈。
 * 
 * @author scott.liang
 * @version 1.0 5/21/2012
 * @since laxcus 1.0
 */
public class MemoryStack {

	/** 内存数组单元队列 **/
	private ArrayList<MemoryItem> array = new ArrayList<MemoryItem>();

	/**
	 * 构造默认的内存数据栈
	 */
	public MemoryStack() {
		super();
	}

	/**
	 * 返回当前数据长度
	 * @return 数据长度
	 */
	public long length() {
		long count = 0L;
		for (MemoryItem item : array) {
			count += item.length();
		}
		return count;
	}

	/**
	 * 返回当前内存数据单元数目
	 * @return 内存数据单元数目
	 */
	public int elements() {
		return array.size();
	}

	/**
	 * 返回内存数据单元列表
	 * @return MemoryItem列表
	 */
	public List<MemoryItem> list() {
		return array;
	}

	/**
	 * 清除全部数据
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 返回内存数据单元数目
	 * @return 内存数据单元数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 追加数据到内存
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 数据长度
	 * @return 返回写入的长度
	 */
	public int append(byte[] b, int off, int len) {
		MemoryItem item = new MemoryItem(array.size());
		array.add(item);
		return item.append(b, off, len);
	}

	/**
	 * 更新数据
	 * 
	 * @param seek 指定下标
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 数据长度
	 * @return 更新的数据长度
	 */
	public int update(long seek, byte[] b, int off, int len) {
		if (seek < 0 || len < 1) {
			throw new IndexOutOfBoundsException("index out!");
		}
		// 确定数据块在哪个位置
		long length = this.length();
		// 超过范围
		if (seek >= length) {
			throw new IndexOutOfBoundsException("index out!");
		}

		// 统计更新长度
		int count = 0;
		// 刻度位置，这个参数一旦确定后就固定
		long begin = 0L;
		// 索引
		int index = 0;
		// 确定下标位置
		while (index < array.size()) {
			MemoryItem item = array.get(index++);
			LongRange range = new LongRange(begin, begin + item.length() - 1);
			// 在范围内，退出
			if (range.inside(seek)) {
				int pos = (int) (seek - begin);
				// 开始写入位置
				int used = item.update(pos, b, off, len);
				off += used;
				len -= used;
				count += used;
				break; // 退出
			} else {
				begin += item.length(); // 移动下标
			}
		}

		// 有剩余尺寸，继续
		for (; len > 0; index++) {
			if (index >= array.size()) {
				break;
			}
			// 更新的开始位置
			MemoryItem item = array.get(index);
			int used = item.update(0, b, off, len);
			off += used;
			len -= used;
			count += used;
		}

		// 仍然有数据，以追加方式写入
		if(len > 0) {
			int wlen = this.append(b, off, len);
			count += wlen;
		}

		return count;
	}

	/**
	 * 从内存中读数据
	 * 
	 * @param seek 指定下标
	 * @param size 指定长度
	 * @return 返回读取的字节数组
	 */
	public byte[] read(long seek, int size) {
		if (seek < 0 || size < 1) {
			throw new IndexOutOfBoundsException("index out!");
		}
		// 确定数据块在哪个位置
		long length = this.length();
		// 超过范围
		if (seek >= length || seek + size > length) {
			throw new IndexOutOfBoundsException("index out!");
		}

		// 刻度位置，这个参数一旦确定后就固定
		long begin = 0L;
		// 索引
		int index = 0;

		byte[] buf = new byte[size];
		int scale = 0;

		// 确定下标位置
		for (; index < array.size(); index++) {
			MemoryItem item = array.get(index);
			LongRange range = new LongRange(begin, begin + item.length() - 1);
			// 在范围内，退出
			if (range.inside(seek)) {
				int off = (int) (seek - begin);
				byte[] b = item.read(off, size);
				System.arraycopy(b, 0, buf, scale, b.length);
				scale += b.length; // 移动刻度位置
				size -= b.length; // 剩余尺寸
				break;
			} else {
				begin += item.length(); // 移动下标
			}
		}

		// 这些是从0下标开始读取
		while (size > 0) {
			index++;
			MemoryItem item = array.get(index);

			byte[] data = item.getData();
			// 本段剩余尺寸
			int left = (size > data.length ? data.length : size);

			System.arraycopy(data, 0, buf, scale, left);
			scale += left; 	// 移动刻度位置
			size -= left; 	// 剩余尺寸
		}
		// 输出字节数组
		return buf;
	}
}
