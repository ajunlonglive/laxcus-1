/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.meta;

import java.io.*;
import java.util.*;

import com.laxcus.distribute.meta.*;
import com.laxcus.distribute.mid.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.range.*;

/**
 * 元数据存取器。以元数据缓存标识为依据进行数据存取。
 * 
 * @author scott.liang
 * @version 1.1 6/23/2012
 * @since laxcus 1.0
 */
public class MetaBuffer {

	/** 元数据缓存标识 **/
	private MetaTag tag;

	/** 本地磁盘文件 **/
	private File file;

	/** 内存数据栈 **/
	private MemoryStack cache;

	/**
	 * 构造默认的元数据存取器
	 */
	private MetaBuffer() {
		super();
	}

	/**
	 * 构造元数据存取器，指定元数据缓存标识
	 * @param tag 元数据缓存标识
	 */
	public MetaBuffer(MetaTag tag) {
		this();
		setTag(tag);
	}

	/**
	 * 构造元数据缓存，指定元数据缓存标识和文件
	 * @param tag 元数据缓存标识
	 * @param file 文件
	 */
	public MetaBuffer(MetaTag tag, File file) {
		this(tag);
		setFile(file);
	}

	/**
	 * 设置元数据缓存标识
	 * @param e MetaTag实例
	 */
	public void setTag(MetaTag e) {
		Laxkit.nullabled(e);

		tag = e;
	}

	/**
	 * 返回元数据缓存标识
	 * @return MetaTag实例
	 */
	public MetaTag getTag() {
		return tag;
	}

	/**
	 * 设置异步调用器编号
	 * @return 长整值的调用器编号
	 */
	public long getInvokerId() {
		return tag.getInvokerId();
	}

	/**
	 * 判断数据保存在磁盘中
	 * @return 返回真或者假
	 */
	public boolean isDisk() {
		return file != null && file.length() > 0;
	}

	/**
	 * 判断数据保存在内存中
	 * @return 返回真或者假
	 */
	public boolean isMemory() {
		return !isDisk();
	}

	/**
	 * 指定存储数据的文件
	 * @param e File实例
	 */
	public void setFile(File e) {
		file = e;
	}

	/**
	 * 返回存储数据的文件
	 * @return File实例
	 */
	public File getFile() {
		return file;
	}

	/**
	 * 返回数据长度
	 * @return 数据长度
	 */
	public long length() {
		if (cache != null) {
			return cache.length();
		} else {
			return file.length();
		}
	}

	/**
	 * 释放内存和删除磁盘文件
	 */
	public void delete() {
		if (cache != null) {
			cache.clear();
			cache = null;
		}
		if (file != null) {
			if (file.exists() && file.isFile()) {
				file.delete();
			}
			file = null;
		}
	}

	/**
	 * 发生错误后，将数据写入磁盘
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 成功返回真，否则假
	 */
	private boolean memoryToDisk(byte[] b, int off, int len) {
		// 写入磁盘
		boolean success = false;
		List<MemoryItem> list = cache.list();
		int size = list.size();
		for (int index = 0; index < size; index++) {
			MemoryItem item = list.get(index);
			byte[] bs = item.getData();
			if (index == 0) {
				success = appendToDisk(bs, 0, bs.length);
			} else if (success) {
				// 第一次成功后，允许继续写入
				success = appendToDisk(bs, 0, bs.length);
			}
		}

		// 以上成功，或者没有保存时，将最后的数据写入磁盘
		if(success || size == 0) {
			success = appendToDisk(b, off, len);
			cache = null;
		}
		return success;
	}

	/**
	 * 追加数据到内存
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 成功返回真，否则假
	 */
	private boolean appendToMemory(byte[] b, int off, int len) {
		boolean success = false;
		// 判断读取
		if (cache == null) {
			cache = new MemoryStack();
		}
		try {
			cache.append(b, off, len);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
			// 内存数据写入磁盘
			success = memoryToDisk(b, off, len);
		}
		// 返回处理结果
		return success;
	}

	/**
	 * 追加数据到磁盘
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 成功返回真，否则假
	 */
	private boolean appendToDisk(byte[] b, int off, int len) {
		boolean success = false;
		// 数据以追加的方式写入磁盘
		try {
			FileOutputStream out = new FileOutputStream(file, true);
			out.write(b, off, len);
			out.flush();
			out.close();
			success = true;
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 返回处理结果
		return success;
	}

	/**
	 * 判断允许写入内存
	 * @return 返回真或者假
	 */
	private boolean allowToMemory() {
		return isMemory();
	}

	/**
	 * 追加数据到内存或者磁盘
	 * @param memory 内存模式
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 成功返回真，否则假
	 */
	public boolean append(boolean memory, byte[] b, int off, int len) {
		// 判断存在错误
		if (off < 0 || len < 1 || off + len > b.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		// 如果要求写到内存时，检查它的空间
		if (memory) {
			memory = allowToMemory();
		}

		if (memory) {
			return appendToMemory(b, off, len);
		} else {
			return appendToDisk(b, off, len);
		}
	}

	/**
	 * 从内存或者磁盘中读出全部元数据
	 * @return 返回读取的字节数组
	 */
	public byte[] read() {
		long length = length();
		LongRange field = new LongRange(0, length - 1);
		// 判断是内存或者磁盘
		if (isMemory()) {
			return readMemory(field);
		} else {
			return readDisk(field);
		}
	}

	/**
	 * 读内存数据
	 * @param range 数据范围
	 * @return 从内存读取的字节数组
	 */
	private byte[] readMemory(LongRange range) {
		LongRange full = new LongRange(0, cache.length() - 1);
		// 必须有范围内
		if (!full.inside(range)) {
			Logger.error(this, "readMemory", "indexout! %s -> %s", full, range);
			return null;
		}

		try {
			return cache.read(range.begin(), (int) range.size()); 
		} catch (OutOfMemoryError e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return null;
	}

	/**
	 * 读磁盘数据
	 * @param range 数据范围
	 * @return 从硬盘读取的字节数组
	 */
	private byte[] readDisk(LongRange range) {
		LongRange full = new LongRange(0, file.length() - 1);
		// 必须有范围内
		if (!full.inside(range)) {
			Logger.error(this, "readDisk", "indexout! %d -> %s", file.length(), range);
			return null;
		}

		// 分配内存和读数据
		try {
			byte[] buff = new byte[(int)range.size()];
			FileInputStream in = new FileInputStream(file);
			// 跨过指定长度，到达下标位置
			long seek = in.skip(range.begin());
			if (seek != range.begin()) {
				in.close(); // 出错
				Logger.error(this, "readDisk", "cannot skip");
				return null;
			}
			// 读数据流，然后关闭
			int len = in.read(buff, 0, buff.length);
			in.close();
			// 检查读取的字节长度
			if (len != buff.length) {
				Logger.error(this, "readDisk", "read missing! %d - %d", len, buff.length);
				return null;
			}
			return buff;
		} catch (IOException e) {
			Logger.error(e);
		} catch (OutOfMemoryError e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() {
		delete();
	}

	//	public static void main(String[] args) {
	//		File file = new File("f:/conduct/abc.bin");
	//
	//		boolean memory = true;
	//		FlashBuffer item = new FlashBuffer(3, file);
	//		byte[] b = new byte[16];
	//		Arrays.fill(b, (byte)'A');
	//		item.append(memory, b, 0, b.length);
	//		Arrays.fill(b, (byte)'B');
	//		item.append(memory, b, 0, b.length);
	//
	//		b = new byte[64];
	//		Arrays.fill(b, (byte)'C');
	//		item.update(memory, 8, b, 0, 5); 
	//		item.update(memory, 18, b, 5, b.length-20);
	//
	//		long field = item.length();
	//		System.out.printf("%s\n", field);
	//
	//		b = item.read(23, 4);
	//		String s = new String(b);
	//		System.out.printf("%s - %d\n", s, s.length());
	//	}


}
