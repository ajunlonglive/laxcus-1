/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.io.*;
import java.util.*;

import com.laxcus.log.client.*;

/**
 * 数据内容缓存。<br>
 * 
 * 保存任意格式的字节数据内容。
 * 
 * @author scott.liang
 * @version 1.0 12/9/2012
 * @since laxcus 1.0
 */
public class ContentBuffer {

	/** 数据缓存 **/
	private byte[] buff;

	/** 缓存下标位置，随数据的写入或者读取，做增加或者减少 **/
	private int seek;
	
	/**
	 * 销毁内存
	 */
	public void destroy() {
		if (buff != null) {
			buff = null;
		}
		seek = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		destroy();
	}

	/**
	 * 构造数据内存缓存，指定缓存字节尺寸
	 * @param len
	 */
	public ContentBuffer(int len) {
		super();
		reset(len);
	}

	/**
	 * 构造默认的数据内存缓存，缓存字节默认是1024字节
	 */
	public ContentBuffer() {
		this(1024);
	}

	/**
	 * 要指定新的缓存尺寸情况下，重置缓存区。
	 * @param len 缓存区长度，不能是负数。
	 */
	public synchronized void reset(int len) {
		if (len < 0) {
			throw new IllegalArgumentException("illegal len:" + len);
		}
		// 释放旧内存
		if (buff != null) {
			buff = null;
		}
		// 分配新内存
		buff = new byte[len];
		seek = 0;
	}

	/**
	 * 清除缓存
	 */
	public void clear() {
		reset(0);
	}

	/**
	 * 判断当前字节数组是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return length() == 0;
	}
	
	/**
	 * 取内存容量
	 * @return 字节数组长度
	 */
	public int capacity() {
		return (buff == null ? 0 : buff.length);
	}

	/**
	 * 返回当前有效长度
	 * @return 有效长度
	 */
	public synchronized int length() {
		return seek;
	}
	
	/**
	 * 将当前的内存数据输出到磁盘文件
	 * @param file 磁盘文件名
	 * @param append 追加模式
	 * @throws IOException
	 */
	public long flushTo(File file, boolean append) throws IOException {
		long size = 0;
		// 文件存在且是增加模式
		if (file.exists() && append) {
			size = file.length();
		}
		// 写入数据
		FileOutputStream writer = new FileOutputStream(file, append);
		writer.write(buff, 0, seek);
		writer.flush();
		writer.close();

		Logger.debug(this, "flushTo", "flush to '%s', append is %s", file, append);

		return file.length() - size;
	}

	/**
	 * 以同步方式，向缓存末尾追加一段数据。
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 返回添加的字节长度
	 * @throws IndexOutOfBoundsException
	 */
	public synchronized int append(byte[] b, int off, int len) {
		if (off < 0 || len < 1 || off > b.length || (off + len > b.length)
				|| (off + len < 0)) {
			String s = String.format("off %d, len:%d", off, len);
			throw new IndexOutOfBoundsException(s);
		} else if (len == 0) {
			return 0;
		}

		int count = seek + len;
		if (count > buff.length) {
			buff = Arrays.copyOf(buff, Math.max(buff.length << 1, count));
		}
		System.arraycopy(b, off, buff, seek, len);
		// 记录新的位置
		seek = count;
		// 返回写入的长度
		return len;
	}
	
	/**
	 * 同步方式，向缓存末尾追加一段数据
	 * @param b 字节数组
	 * @return 返回添加的字节长度
	 */
	public int append(byte[] b) {
		return append(b, 0, b.length);
	}

	/**
	 * 以同步方式，将字节数组写入缓存的指定下标位置。
	 * @param pos 缓存下标位置。必须在"0-seek"之间，超出即错误。
	 * @param b 字节数组
	 * @param off 有效数据下标位置
	 * @param len 有效数据长度
	 * @return 返回写入的字节长度
	 * @throws IndexOutOfBoundsException, OutOfMemoryError
	 */
	public synchronized int write(int pos, byte[] b, int off, int len) {
		if (off < 0 || len < 1 || off > b.length || (off + len > b.length)
				|| (off + len < 0)) {
			throw new IndexOutOfBoundsException(String.format("off %d, len:%d", off, len));
		} else if (!(0 <= pos && pos <= seek)) { // 必须在“0 - seek”之间
			throw new IndexOutOfBoundsException(String.format("pos:%d, end:%d", pos, seek));
		} else if (len == 0) {
			return 0;
		}

		int count = pos + len;
		// 这时会发生“java.lang.OutOfMemoryError”
		if (count > buff.length) {
			buff = Arrays.copyOf(buff, Math.max(buff.length << 1, count));
		}
		// 数据写入内存指定下标
		System.arraycopy(b, off, buff, pos, len);
		// 超过最后下标位置，更新
		if(count > seek) {
			seek = count;
		}
		return len;
	}

	/**
	 * 从缓存中读取一段数据，输出到数组的指定范围
	 * @param b 字节数组
	 * @param off 有效数据下标位置
	 * @param len 有效数据长度
	 * @return 读取的字节尺寸（0或者大于0，没有负数）
	 */
	public synchronized int read(byte[] b, int off, int len) {
		// 检查传入参数
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		}
		// 如果已经读完，返回0
		if (seek == 0) {
			return 0;
		}
		// 读一段有效的字节
		int left = (seek > len ? len : seek);
		// 从开始位置复制数据
		System.arraycopy(buff, 0, b, off, left);
		// 移动数据到内存的前面
		System.arraycopy(buff, left, buff, 0, seek - left);
		// 重新计算有效位置
		seek -= left;
		// 返回读取的字节长度
		return left;
	}

	/**
	 * 读缓存的全部字节数组，清空缓存（位置指针恢复到0，即等于清空缓存）。
	 * @return 如果字节数组是空值，返回一个0长度字节数组，否则返回它的复制
	 */
	public synchronized byte[] readFully() {
		if (seek == 0) {
			return new byte[0];
		}
		int len = seek;
		seek = 0;
		return Arrays.copyOf(buff, len);
	}
	
	/**
	 * 返回缓存的全部字节数组，缓存数据仍然保存。
	 * @return 字节数组
	 */
	public synchronized byte[] toByteArray() {
		if (seek == 0) {
			return new byte[0];
		}
		return Arrays.copyOf(buff, seek);
	}

}