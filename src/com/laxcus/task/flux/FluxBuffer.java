/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.flux;

import java.io.*;
import java.util.*;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.mid.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.range.*;

/**
 * CONDUCT中间数据缓存 <br>
 * 以“模”为单位存储和读取。
 * 
 * @author scott.liang
 * @version 1.1 6/23/2012
 * @since laxcus 1.0
 */
public class FluxBuffer {
	
	/** 用户签名 **/
	private Siger issuer;

	/** 中间数据映像域。就是元数据 **/
	private FluxField field = new FluxField();

	/** 完成标记 **/
	private boolean completed;

	/** 本地磁盘文件 **/
	private File file;

	/** 内存数据栈 **/
	private MemoryStack cache;

	/**
	 * 构造默认的CONDUCT中间数据缓存
	 * @param issuer 用户签名
	 */
	public FluxBuffer(Siger issuer) {
		super();
		setCompleted(false);
		setIssuer(issuer);
	}

	/**
	 * 构造CONDUCT中间数据缓存，指定模值和文件
	 * @param issuer 用户签名
	 * @param mod 模值
	 * @param file 文件
	 */
	public FluxBuffer(Siger issuer, long mod, File file) {
		this(issuer);
		setMod(mod);
		setFile(file);
		// 当文件存在，删除它
		if(file != null && file.exists()) {
			file.delete();
		}
	}
	
	/**
	 * 设置用户签名
	 * @param e Siger实例
	 */
	public void setIssuer(Siger e) {
		Laxkit.nullabled(e);
		issuer = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return issuer;
	}

	/**
	 * 设置模值
	 * @param i
	 */
	public void setMod(long i) {
		field.setMod(i);
	}

	/**
	 * 返回模值
	 * @return 模值
	 */
	public long getMod() {
		return field.getMod();
	}

	/**
	 * 判断数据保存在磁盘中
	 * @return 返回真或者假
	 */
	public boolean isDisk() {
		return file.length() > 0;
	}

	/**
	 * 判断数据保存在内存中
	 * @return 返回真或者假
	 */
	public boolean isMemory() {
		return !isDisk();
	}

	/**
	 * 判断已经完成
	 * @return 返回真或者假
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * 设置完成/非完成标记
	 * @param b 完成标记
	 */
	public void setCompleted(boolean b) {
		completed = b;
	}

	/**
	 * 返回当前数据映像域的数据副本
	 * @return FluxField实例
	 */
	public FluxField getField() {
		return field.duplicate();
	}

	/**
	 * 指定存储数据的文件
	 * @param e File实例
	 */
	private void setFile(File e) {
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
	 * 释放内存和删除磁盘文件
	 */
	public void delete() {
		// 删除缓存
		if (cache != null) {
			Logger.debug(issuer, this, "delete", "clear cache, size is %d", cache.size());
			cache.clear();
			cache = null;
		}
		// 删除文件
		if (file != null) {
			boolean success = (file.exists() && file.isFile());
			if (success) {
				success = file.delete();
				Logger.debug(issuer, this, "delete", success, "delete %s", file);
			}
			file = null;
		}
		
		// 清除签名
		issuer = null;
	}

	/**
	 * 发生错误后，将数据写入磁盘
	 * @param b  字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 写入成功返回真，否则假
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
	 * @param b  字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 追加成功返回真，否则假
	 */
	private boolean appendToMemory(byte[] b, int off, int len) {
		boolean success = false;
		// 判断读取
		if (cache == null) {
			cache = new MemoryStack();
		}
		try {
			cache.append(b, off, len);
			field.setSize(cache.length());
			success = true;
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
			// 内存数据写入磁盘
			success = memoryToDisk(b, off, len);
		}
		// 返回处理结果
		return success;
	}

	/**
	 * 追加数据到磁盘
	 * @param b  字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 追加成功返回真，否则假
	 */
	private boolean appendToDisk(byte[] b, int off, int len) {
		// debug code, start
		Logger.debug(issuer, this, "appendToDisk", "before file size is %d", (file.exists() ? file.length() : 0));
		// debug code, end

		boolean success = false;
		// 数据以追加的方式写入磁盘
		try {
			FileOutputStream out = new FileOutputStream(file, true);
			out.write(b, off, len);
			out.flush();
			out.close();
			// 更新数据长度
			field.setSize(file.length());
			success = true;
		} catch (IOException e) {
			Logger.error(issuer, e);
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
		}

		// debug code, start
		Logger.debug(issuer, this, "appendToDisk", "after file size is %d, flux field '%s'", file.length(), field);
		// debug code, end

		// 返回处理结果
		return success;
	}

	/**
	 * 允许写入内存
	 * @return 返回真或者假
	 */
	private boolean allowToMemory() {
		return isMemory();
	}

	/**
	 * 追加数据到内存或者磁盘
	 * @param memory 内存模式
	 * @param b  字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 追加成功返回真，否则假
	 */
	public boolean append(boolean memory, int elements, byte[] b, int off, int len) {
		// 判断存在错误
		if (off < 0 || len < 1 || off + len > b.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		// 如果要求写到内存时，检查它的空间
		if (memory) {
			memory = allowToMemory();
		}

		boolean success = false;
		// 写入内存或者磁盘
		if (memory) {
			success = appendToMemory(b, off, len);
		} else {
			success = appendToDisk(b, off, len);
		}
		// 更新成员数目
		if (success) {
			field.setElements(field.getElements() + elements);
		}
		return success;
	}

	/**
	 * 从内存的指定位置更新数据。更新操作必须发生在已经存在数据之后。
	 * @param seek 内存数据下标位置
	 * @param b 字节数组
	 * @param off 数据开始下标
	 * @param len 指定数据长度
	 * @return 成功返回真，否则假。
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
			field.setSize(cache.length());
			success = true;
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
			// 内存数据改写入磁盘
			success = memoryToDisk(b, off, len);
		}
		// 返回结果
		return success;
	}

	/**
	 * 更新数据到磁盘指定下标
	 * @param seek 在整个数据流的下标位置
	 * @param b 字节数组
	 * @param off 字节数组的下标位置
	 * @param len 字节数组有效长度
	 * @return 成功返回真，否则假
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
			field.setSize(file.length());
			success = true;
		} catch (IOException e) {
			Logger.error(issuer, e);
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
		}

		return success;
	}

	/**
	 * 更新数据，前提是必须内存数据或者磁盘文件已经存在
	 * @param memory 指定写入内存
	 * @param seek 下标位置
	 * @param b 字节数据数组
	 * @param off 数据下标
	 * @param len 数据长度
	 * @return 成功返回真，否则假。
	 * @throws IndexOutOfBoundsException, NullPointerException
	 */
	public boolean update(boolean memory, long seek, byte[] b, int off, int len) {
		// 判断存在错误
		if (off < 0 || len < 1 || off + len > b.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		// 不在范围内是错误
		if (!field.getRange().inside(seek)) {
			throw new IndexOutOfBoundsException();
		}
		// 允许写入内存
		if(memory) {
			memory = allowToMemory();
		}

		if (memory) {
			return updateToMemory(seek, b, off, len);
		} else {
			return updateToDisk(seek, b, off, len);
		}
	}

	/**
	 * 从内存或者磁盘中读数据
	 * @param seek 基于0的开始下标
	 * @param len 数据长度（实际是数组长度-1，或者文件长度-1）
	 * @return 返回读取的字节数组
	 */
	public byte[] read(long seek, int len) {
		// 小于1，非法参数
		if (len < 1) {
			throw new IllegalValueException("illegal length:%d", len);
		}

		// 确定有效范围
		LongRange range = new LongRange(seek, seek + len - 1);
		// 检查，必须在规定范围内
		if (!field.getRange().inside(range)) {
			Logger.error(issuer, this, "read", "indexout! %s -> %s", field, range);
			return null;
		}

		// 判断是内存或者磁盘
		if (isMemory()) {
			return readMemory(range);
		} else {
			return readDisk(range);
		}
	}

	/**
	 * 从内存读数据
	 * @param range 数据范围
	 * @return 读取的字节数组
	 */
	private byte[] readMemory(LongRange range) {
		LongRange full = new LongRange(0, cache.length() - 1);
		// 必须有范围内
		if (!full.inside(range)) {
			Logger.error(issuer, this, "readMemory", "indexout! %s -> %s", full, range);
			return null;
		}

		try {
			return cache.read(range.begin(), (int) range.size()); 
		} catch (OutOfMemoryError e) {
			Logger.error(issuer, e);
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
		}
		return null;
	}

	/**
	 * 从磁盘读数据
	 * @param range 数据范围
	 * @return 读取的字节数组
	 */
	private byte[] readDisk(LongRange range) {
		LongRange full = new LongRange(0, file.length() - 1);
		// 必须有范围内
		if (!full.inside(range)) {
			Logger.error(issuer, this, "readDisk", "indexout! %d -> %s", file.length(), range);
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
				Logger.error(issuer, this, "readDisk", "cannot skip");
				return null;
			}
			// 读数据流，然后关闭
			int len = in.read(buff, 0, buff.length);
			in.close();
			// 检查读取的字节长度
			if (len != buff.length) {
				Logger.error(issuer, this, "readDisk", "read missing! %d - %d", len, buff.length);
				return null;
			}
			return buff;
		} catch (IOException e) {
			Logger.error(issuer, e);
		} catch (OutOfMemoryError e) {
			Logger.error(issuer, e);
		} catch (Throwable e) {
			Logger.fatal(issuer, e);
		}

		return null;
	}

	/**
	 * 当缓存不再使用时，删除磁盘上的文件
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		delete();
	}

	//	public static void main(String[] args) {
	//		File file = new File("f:/conduct/abc.bin");
	//
	//		boolean memory = true;
	//		FluxBuffer item = new FluxBuffer(3, file);
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
	//		FluxField field = item.getField();
	//		System.out.printf("%s\n", field);
	//
	//		b = item.read(23, 4);
	//		String s = new String(b);
	//		System.out.printf("%s - %d\n", s, s.length());
	//	}

}