/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.pool;

import java.io.*;
import java.util.*;

import com.laxcus.distribute.mid.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.range.*;

/**
 * SIFT中间数据存取器。
 * 以模为单位进行存取。
 * 
 * @author scott.liang
 * @version 1.1 6/23/2012
 * @since laxcus 1.0
 */
public class SiftBuffer {

	/** 模值 **/
	private long mod;

	/** 本地磁盘文件 **/
	private File file;

	/** 内存数据栈 **/
	private MemoryStack cache;

	/**
	 * 构造默认的SIFT中间数据存取器
	 */
	private SiftBuffer() {
		super();
	}

	/**
	 * 构造SIFT中间数据存取器，指定模值
	 * @param mod
	 */
	public SiftBuffer(long mod) {
		this();
		this.setMod(mod);
	}

	/**
	 * 构造SIFT中间数据缓存，指定模值和文件
	 * @param mod - 模值
	 * @param file - 文件
	 */
	public SiftBuffer(long mod, File file) {
		this(mod);
		this.setFile(file);
	}

	/**
	 * 设置模值
	 * @param i
	 */
	public void setMod(long i) {
		mod = i;
	}

	/**
	 * 返回模值
	 * @return
	 */
	public long getMod() {
		return  mod;
	}

	/**
	 * 判断数据保存在磁盘中
	 * @return
	 */
	public boolean isDisk() {
		return file != null && file.length() > 0;
	}

	/**
	 * 判断数据保存在内存中
	 * @return
	 */
	public boolean isMemory() {
		return !isDisk();
	}

	/**
	 * 指定存储数据的文件
	 * @param e
	 */
	public void setFile(File e) {
		file = e;
	}

	/**
	 * 返回存储数据的文件
	 * @return
	 */
	public File getFile() {
		return file;
	}

	/**
	 * 返回数据长度
	 * @return
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
	 * @return
	 */
	public boolean delete() {
		if (cache != null) {
			cache.clear();
			cache = null;
		}
		if (file != null) {
			if (file.exists() && file.isFile()) {
				boolean b = file.delete();
				if (!b) {
					return false;
				}
			}
			file = null;
		}
		return true;
	}

	/**
	 * 发生错误后，将数据写入磁盘
	 * @param b
	 * @param off
	 * @param len
	 * @return
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
				success = this.appendToDisk(bs, 0, bs.length);
			} else if (success) {
				// 第一次成功后，允许继续写入
				success = this.appendToDisk(bs, 0, bs.length);
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
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	private boolean appendToMemory(byte[] b, int off, int len) {
		boolean success = false;
		// 判断读取
		if (cache == null) {
			cache = new MemoryStack();
		}
		try {
			cache.append(b, off, len);
			//			field.setSize(stack.length());
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
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	private boolean appendToDisk(byte[] b, int off, int len) {
		boolean success = false;
		// 数据以追加的方式写入磁盘
		try {
			FileOutputStream out = new FileOutputStream(file, true);
			out.write(b, off, len);
			out.flush();
			out.close();
			// 更新数据长度
			//			field.setSize(file.length());
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
	 * 允许写入内存
	 * @return
	 */
	private boolean allowToMemory() {
		return isMemory();
	}

	/**
	 * 追加数据到内存或者磁盘
	 * @param memory
	 * @param b
	 * @param off
	 * @param len
	 * @return
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
			return this.appendToMemory(b, off, len);
		} else {
			return this.appendToDisk(b, off, len);
		}
	}

	/**
	 * 从内存的指定位置更新数据。更新操作必须发生在已经存在数据之后。
	 * @param seek - 内存数据下标位置
	 * @param b - 字节数组
	 * @param off - 数据开始下标
	 * @param len - 指定数据长度
	 * @return - 成功返回真，否则假。
	 */
	private boolean updateToMemory(long seek, byte[] b, int off, int len) {
		// 空值时，不允许追加
		Laxkit.nullabled(cache);

		// 超过范围，不允许追加
		if (seek > cache.length()) {
			throw new IndexOutOfBoundsException();
		}

		boolean success = false;
		try {
			cache.update(seek, b, off, len);
			// 更新数据长度
			//			field.setSize(stack.length());
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
			// 内存数据改写入磁盘
			success = memoryToDisk(b, off, len);
		}
		// 返回结果
		return success;
	}

	/**
	 * 更新数据到磁盘指定下标
	 * @param seek
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	private boolean updateToDisk(long seek, byte[] b, int off, int len) {
		if(seek > file.length()) {
			throw new IndexOutOfBoundsException();
		}

		boolean success = false;
		// 保存数据到磁盘
		try {
			// 磁头移到文件指定下标，更新这段区域的数据
			RandomAccessFile out = new RandomAccessFile(file, "rws");
			// 移到指定下标位置
			out.seek(seek);
			// 在这个位置写入数据
			out.write(b, off, len);
			out.close();
			//			field.setSize(file.length());
			success = true;
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		//		// 更新数据长度
		//		if (success) {
		//			field.setSize(file.length());
		//		}

		//		System.out.printf("file length is:%d|%d,%d,%d|%s\n",
		//				file.length(), seek, off, len , field);

		return success;
	}

	/**
	 * 更新数据，前提是必须内存数据或者磁盘文件已经存在
	 * @param memory - 指定写入内存
	 * @param seek - 下标位置
	 * @param b - 字节数据数组
	 * @param off - 数据下标
	 * @param len - 数据长度
	 * @return - 成功返回真，否则假。
	 * @throws - IndexOutOfBoundsException, NullPointerException
	 */
	public boolean update(boolean memory, long seek, byte[] b, int off, int len) {
		// 判断存在错误
		if (off < 0 || len < 1 || off + len > b.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		long length = length();
		// 超过范围是错误，以0下标为基
		if(seek > length) {
			throw new IndexOutOfBoundsException();
		}
		//		if (!field.getRange().inside(seek)) {
		//			throw new IndexOutOfBoundsException();
		//		}
		// 允许写入内存
		if(memory) {
			memory = allowToMemory();
		}

		if (memory) {
			return this.updateToMemory(seek, b, off, len);
		} else {
			return this.updateToDisk(seek, b, off, len);
		}
	}

	/**
	 * 从内存或者磁盘中读数据
	 * @param offset - 基于0的开始下标
	 * @param size - 数据长度（实际是数组长度-1，或者文件长度-1）
	 * @return - 返回读取的字节数组
	 */
	public byte[] read(long offset, long size) {
		// 单次提取数据不允许超过2G。
		if(size >= 0x7FFFFFFFL) {
			throw new OutOfMemoryError("memory out! "+size);
		}

		long length = length();
		LongRange field = new LongRange(0, length - 1);
		// 确定有效范围
		LongRange range = new LongRange(offset, offset + size - 1);
		// 检查，必须在规定范围内
		if (!field.inside(range)) {
			Logger.error(this, "read", "indexout! %s -> %s", field, range);
			return null;
		}

		// 判断是内存或者磁盘
		if (this.isMemory()) {
			return readMemory(range);
		} else {
			return readDisk(range);
		}
	}

	/**
	 * 读内存数据
	 * @param range
	 * @return
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
	 * @param range
	 * @return
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

	public static void main(String[] args) {
		File file = new File("f:/conduct/abc.bin");

		boolean memory = true;
		SiftBuffer item = new SiftBuffer(3, file);
		byte[] b = new byte[16];
		Arrays.fill(b, (byte)'A');
		item.append(memory, b, 0, b.length);
		Arrays.fill(b, (byte)'B');
		item.append(memory, b, 0, b.length);

		b = new byte[64];
		Arrays.fill(b, (byte)'C');
		item.update(memory, 8, b, 0, 5); 
		item.update(memory, 18, b, 5, b.length-20);

		long field = item.length();
		System.out.printf("%s\n", field);

		b = item.read(23, 4);
		String s = new String(b);
		System.out.printf("%s - %d\n", s, s.length());
	}


}
