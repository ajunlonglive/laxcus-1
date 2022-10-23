/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.classable;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.charset.*;

/**
 * 可类化数据写入器。<br><br>
 * 可类化数据写入器有三种写入处理：<br>
 * 1. 数据类型通过对应的"writeXXX"方法转化为字节数组保存起来。<br>
 * 2. 调用"writeObject"方法，将实现了Classable接口的类对象，调用它的"build"方法，写入类数据，但是不写入这个类的名称。<br>
 * 3. 调用"writeDefault"方法，将实现了Classable接口的类对象，首先写入这个类的名称，再调用它的"build"方法写入类数据。<br><br>
 * 
 * 在数据写入过程中，随着数据增加，缓冲区也将自动增长。缓冲区尺寸受到JVM的限制，在分配内存时可能会发生内存溢出的错误。<br>
 * 可类化存储器也可以做为数据缓存使用。<br><br>
 * 
 * 类方法说明：<br>
 * 1. "write"方法是基本的写入接口。<br>
 * 2. "writeXXX"方法是写入不同类型的数据。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/29/2015
 * @since laxcus 1.0
 */
public final class ClassWriter {

	/** 字节缓存 **/
	private byte[] buff;

	/** 当前缓存下标位置，随缓存数据增加同步移动 **/
	private int seek;
	
	/**
	 * 销毁数据
	 */
	public void destroy() {
		buff = null;
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
	 * 建立可类化数据写入器，同时指定它的缓冲尺寸。
	 * @param size 字节缓冲长度
	 */
	public ClassWriter(int size) {
		if (size < 1) {
			throw new IllegalArgumentException("illegal size:" + size);
		}
		buff = new byte[size];
		seek = 0;
	}

	/**
	 * 建立默认的可类化数据写入器。缓冲尺寸是32字节
	 */
	public ClassWriter() {
		this(32);
	}
	
	/**
	 * 从指定的下标位置替换数据。 <br>
	 * 能够替换的要求是下标位置和数据范围在内存范围内
	 * 
	 * @param index 指定的内存下标位置
	 * @param b 字节数组
	 * @param off 字节数组的下标位置
	 * @param len 字节数组的有效长度
	 */
	public void replace(int index, byte[] b, int off, int len) {
		if (index < 0 || index > seek || (index + len > buff.length)) {
			throw new IndexOutOfBoundsException();
		}
		System.arraycopy(b, off, buff, index, len);
	}

	/**
	 * 从指定的下标位置替换数据
	 * 
	 * @param index 指定的内存下标位置
	 * @param b 字节数组
	 */
	public void replace(int index, byte[] b) {
		replace(index, b, 0, b.length);
	}

	/**
	 * 向缓存写入一段字节数组。这是最基本的写入接口，所有写入操作的调用它来完成。
	 * @param b 字节数组句柄
	 * @param off 下标开始位置
	 * @param len 字节数组长度
	 * @throws IndexOutOfBoundsException
	 */
	public void write(byte[] b, int off, int len) {
		if (off < 0 || len < 0 || off > b.length || (off + len > b.length) || (off + len < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}

		int count = seek + len;
		if (count > buff.length) {
			buff = Arrays.copyOf(buff, Math.max(buff.length << 1, count));
		}
		System.arraycopy(b, off, buff, seek, len);
		seek = count;
	}

	/**
	 * 向缓冲区写入一个完整的字节数组
	 * @param b 字节数组
	 */
	public void write(byte[] b) {
		write(b, 0, b.length);
	}

	/**
	 * 写入一个字节
	 * @param b 写入字节
	 */
	public void write(byte b) {
		write(new byte[] { b });
	}

	/**
	 * 写入一个预定义长度字节数组，在写入的字节数组前面标注它的长度
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 */
	public void writeByteArray(byte[] b, int off, int len) {
		if (b == null) {
			len = 0;
		} else if (off < 0 || len < 1 || off > b.length
				|| (off + len > b.length) || (off + len < 0)) {
			throw new IndexOutOfBoundsException();
		}

		// 写入字节数组长度
		writeInt(len);
		// 写入字节数组
		if (len > 0) {
			write(b, off, len);
		}
	}

	/**
	 * 写入一个预定义长度字节数组，在写入的字节数组前面标注它的长度。
	 * @param b 字节数组
	 */
	public void writeByteArray(byte[] b) {
		writeByteArray(b, 0, (b == null ? 0 : b.length));
	}

	/**
	 * 写入一个布尔值
	 * @param b 布尔值
	 */
	public void writeBoolean(boolean b) {
		write((byte) (b ? 1 : 0));
	}

	/**
	 * 写入一个布尔数组
	 * @param array 布尔值数组
	 */
	public void writeBooleanArray(boolean[] array) {
		int size = (array == null ? -1 : array.length);
		writeInt(size);
		for (int i = 0; i < size; i++) {
			writeBoolean(array[i]);
		}
	}

	/**
	 * 写入一个字符
	 * @param ch 字符
	 */
	public void writeChar(char ch) {
		write(Laxkit.toBytes(ch));
	}

	/**
	 * 写入一个字符数组
	 * @param array 字符数组
	 */
	public void writeCharArray(char[] array) {
		int size = (array == null ? -1 : array.length);
		writeInt(size);
		for (int i = 0; i < size; i++) {
			writeChar(array[i]);
		}
	}

	/**
	 * 写入一个短整型 
	 * @param value 短整型
	 */
	public void writeShort(short value) {
		write(Laxkit.toBytes(value));
	}

	/**
	 * 写入一个短整型数组
	 * @param array 短整型数组
	 */
	public void writeShortArray(short[] array) {
		int size = (array == null ? -1 : array.length);
		writeInt(size);
		for (int i = 0; i < size; i++) {
			writeShort(array[i]);
		}
	}

	/**
	 * 写入一个整型
	 * @param value 整型
	 */
	public void writeInt(int value) {
		byte[] b = Laxkit.toBytes(value);
		write(b);
	}

	/**
	 * 写入一个整型数组
	 * @param array 整型数组
	 */
	public void writeIntArray(int[] array) {
		int size = (array == null ? -1 : array.length);
		writeInt(size);
		for (int i = 0; i < size; i++) {
			writeInt(array[i]);
		}
	}

	/**
	 * 写入一个长整型
	 * @param value 长整型
	 */
	public void writeLong(long value) {
		write(Laxkit.toBytes(value));
	}

	/**
	 * 写入一个整型数组
	 * @param array 长整型数组
	 */
	public void writeLongArray(long[] array) {
		int size = (array == null ? -1 : array.length);
		writeInt(size);
		for (int i = 0; i < size; i++) {
			writeLong(array[i]);
		}
	}

	/**
	 * 写入一个单浮点
	 * @param value 单浮点
	 */
	public void writeFloat(float value) {
		int that = java.lang.Float.floatToIntBits(value);
		writeInt(that);
	}

	/**
	 * 写入一个单浮点值数组
	 * @param array 单浮点数组
	 */
	public void writeFloatArray(float[] array) {
		int size = (array == null ? -1 : array.length);
		writeInt(size);
		for (int i = 0; i < size; i++) {
			writeFloat(array[i]);
		}
	}

	/**
	 * 写入一个双浮点
	 * @param value 双浮点
	 */
	public void writeDouble(double value) {
		long that = java.lang.Double.doubleToLongBits(value);
		writeLong(that);
	}

	/**
	 * 写入一个双浮点数组
	 * @param array 双浮点数组
	 */
	public void writeDoubleArray(double[] array) {
		int size = (array == null ? -1 : array.length);
		writeInt(size);
		for (int i = 0; i < size; i++) {
			writeDouble(array[i]);
		}
	}

	/**
	 * 写入一个字符串。<br>
	 * 字符串统一采用UTF8编码后写入，也允许输入一个空值。<br>
	 * @param value 字符串
	 */
	public void writeString(String value) {
		// 空值写入-1；0字长写入0。
		if (value == null) {
			writeInt(-1);
		} else if (value.length() == 0) {
			writeInt(0);
		} else {
			// UTF8编码
			byte[] b = new UTF8().encode(value);
			// 标记字节长度
			writeInt(b.length);
			// 写入字节数组
			write(b, 0, b.length);
		}
	}
	
	/**
	 * 写入一个文件实例。允许空指针。
	 * @param file File实例或者空指针。
	 */
	public void writeFile(File file) {
		String path = null;
		if (file != null) {
			path = Laxkit.canonical(file);
		}
		writeString(path);
	}

	/**
	 * 写入一个字符串数组
	 * @param array 字符串数组
	 */
	public void writeStringArray(String[] array) {
		int size = (array == null ? -1 : array.length);
		// 写入字符串长度
		writeInt(size);
		// 写入每一个字符串
		for (int i = 0; i < size; i++) {
			writeString(array[i]);
		}
	}

	/**
	 * 写入一个实现可类化接口的对象实例,但是不标记它的类的实体名称
	 * @param that 可类化接口对象
	 */
	public void writeObject(Classable that) {
		that.build(this);
	}

	/**
	 * 写入一个带布尔标记前缀的可类化接口实例，这是与“writeObject”区别。
	 * @param that 可类化接口对象
	 */
	public void writeInstance(Classable that) {
		boolean enabled = (that != null);
		// 判断对象有效
		writeBoolean(enabled);
		// 对象有效，类对象转为字节数组，写入内存
		if (enabled) {
			writeObject(that);
		}
	}

	/**
	 * 写入一个实现可类化接口的对象实例，并且在它的前面标记类的实体名称。允许空对象。
	 * @param that 可类化接口对象
	 */
	public void writeDefault(Classable that) {
		// 判断不是空值
		boolean enabled = (that != null);
		writeBoolean(enabled);
		// 写入类型
		if (enabled) {
			String clazzName = that.getClass().getName();
			// 写入类名称
			writeString(clazzName);
			// 写入对象
			writeObject(that);
		}
	}

	/**
	 * 重置字节数组缓存为空状态
	 */
	public void reset() {
		seek = 0;
	}

	/**
	 * 字节数组缓存的有效尺寸
	 * @return 有效尺寸
	 */
	public int size() {
		return seek;
	}

	/**
	 * 输出全部原始字节数据（未加密的状态）。
	 * @return 当前字节数组的数据副本
	 */
	public byte[] effuse() {
		return Arrays.copyOf(buff, seek);
	}

	/**
	 * 数据写入磁盘
	 * @param b 字节数组
	 * @param file 文件名
	 * @param append 追加方式
	 * @return 返回写入的字节长度
	 * @throws IOException
	 */
	private long writeDisk(byte[] b, File file, boolean append) throws IOException {
		long length = 0;
		// 如果文件存在，判断它的长度
		if (file.exists() && file.isFile()) {
			length = file.length();
		}
		FileOutputStream out = new FileOutputStream(file, append);
		out.write(b);
		out.flush();
		out.close();
		return file.length() - length;
	}

	/**
	 * 以覆盖或者追加方式，将数据写入磁盘
	 * @param file 磁盘文件名
	 * @param append 追加方式
	 * @return 返回写入的字节长度
	 * @throws IOException
	 */
	public long effuse(File file, boolean append) throws IOException {
		byte[] b = effuse();
		return writeDisk(b, file, append);
	}

	/**
	 * 以覆盖方式，将数据写入磁盘
	 * @param file 磁盘文件名
	 * @return 返回写入的字节长度
	 * @throws IOException
	 */
	public long effuse(File file) throws IOException {
		return effuse(file, false);
	}

	/**
	 * 将字节数组按照要求的加密/压缩处理后输出
	 * @param inflator 加密/压缩接口
	 * @return 写入后的字节数组
	 */
	public byte[] effuse(ClassInflator inflator) {
		byte[] b = effuse();
		return inflator.inflate(b, 0, b.length);
	}

	/**
	 * 指定可类化数据编码器，以覆盖或者追加方式，将数据写入磁盘
	 * @param inflator 可类化数据编码器
	 * @param file 文件名
	 * @param append 追加方式
	 * @return 返回写入的字节长度
	 * @throws IOException
	 */
	public long effuse(ClassInflator inflator, File file, boolean append)
			throws IOException {
		byte[] b = effuse(inflator);
		return writeDisk(b, file, append);
	}

	/**
	 * 指定可类化数据编码器，以覆盖方式，将数据写入磁盘
	 * @param inflator 可类化数据编码器
	 * @param file 文件名
	 * @return 返回写入的字节长度
	 * @throws IOException
	 */
	public long effuse(ClassInflator inflator, File file) throws IOException {
		return effuse(inflator, file, false);
	}
}	