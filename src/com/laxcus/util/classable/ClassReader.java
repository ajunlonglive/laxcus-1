/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.classable;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.charset.*;

/**
 * 可类化数据读取器。<br><br>
 * 
 * 可类化数据读取器与可类化数据写入器的功能是对应的，它完成3种读取操作：<br>
 * 1. 将字节数组通过"readXXX"方法转换为各种类型的数据。<br>
 * 2. 调用"readObject"方法，根据外部提供的类定义，生成这个类实例，调用"resolve"方法，解析数据和返回。<br>
 * 3. 调用"readDefault"方法，根据字节数组中已经提供的类名称，转化为类定义，再调用"readObject"方法返回类实例。<br><br>
 * 
 * 在数据读取过程中，字节缓存区是恒定不变的，唯一变化的是位置指针(seek)。如果调用"attach"方法，可类化读取器将关联一组新的字节数组，旧的字节数组将消失。<br>
 * 可类化读取器在读取过程中，可能会产生如下的异常：ClassableException、IndexOutOfBoundsException。<br><br>
 * 
 * 类方法说明：<br>
 * 1. "read"方法是基本的读取接口。<br>
 * 2. "readXXX"方法是读取不同类型的数据。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/29/2015
 * @since laxcus 1.0
 */
public final class ClassReader {

	/** 字节缓冲数组 **/
	private byte[] buff;

	/** 数据的开始位置、当前下标位置、结束位置 **/
	private int begin, seek, end;

	/**
	 * 销毁数据
	 */
	public void destroy() {
		begin = seek = end = 0;
		buff = null;
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
	 * 构造可类化数据读取器，指定它绑定的字节数组
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 */
	public ClassReader(byte[] b, int off, int len) {
		super();
		attach(b, off, len);
	}

	/**
	 * 构造一个可类化数据读取器，指定它绑字的字节数组
	 * @param b 字节数组
	 */
	public ClassReader(byte[] b) {
		this(b, 0, b.length);
	}

	/**
	 * 构造可类化数据读取器，并且用解压器还原为明文数据
	 * @param deflator
	 * @param b
	 * @param off
	 * @param len
	 */
	public ClassReader(ClassDeflator deflator, byte[] b, int off, int len) {
		super();
		byte[] s = deflator.deflate(b, off, len);
		attach(s, 0, s.length);
	}

	/**
	 * 构造可类化数据读取器，在读取数据将，用指定的“解压/解密器”还原为明文数据
	 * @param deflator
	 * @param b
	 */
	public ClassReader(ClassDeflator deflator, byte[] b) {
		this(deflator, b, 0, b.length);
	}

	/**
	 * 构造构造可类化数据读取器，从磁盘文件中解析数据流
	 * @param file 磁盘文件
	 * @param fileoff 文件下标
	 * @throws IOException
	 */
	public ClassReader(File file, long fileoff) throws IOException {
		// 下标
		if (fileoff < 0) {
			throw new FileNotFoundException("illegal file offset:" + fileoff);
		}
		// 文件不存在、不是文件属性、空文件
		if (!(file.exists() && file.isFile())) {
			throw new FileNotFoundException(file.toString());
		} else if (file.length() == 0) {
			throw new FileNotFoundException("empty " + file.toString());
		}
		// 判断下标溢出
		if (fileoff > file.length()) {
			throw new FileNotFoundException("read out! " + fileoff + " > "
					+ file.length());
		}

		// 把文件内容全部读取出来，保存到内存
		long readlen = file.length() - fileoff;
		byte[] b = new byte[(int) readlen];
		FileInputStream in = new FileInputStream(file);
		if (fileoff > 0) {
			in.skip(fileoff);
		}
		int len = in.read(b, 0, b.length);
		in.close();
		// 绑定
		attach(b, 0, len);
	}

	/**
	 * 构造构造可类化数据读取器，从磁盘文件中解析数据流
	 * @param file 磁盘文件
	 * @throws IOException
	 */
	public ClassReader(File file) throws IOException {
		this(file, 0);
	}

	//	public ClassReader(File file) throws IOException {
	//		// 文件不存在、不是文件属性、空文件
	//		if (!(file.exists() && file.isFile())) {
	//			throw new FileNotFoundException(file.toString());
	//		} else if (file.length() == 0) {
	//			throw new FileNotFoundException("empty " + file.toString());
	//		}
	//		// 把文件内容全部读取出来，保存到内存
	//		byte[] b = new byte[(int) file.length()];
	//		FileInputStream in = new FileInputStream(file);
	//		int len = in.read(b, 0, b.length);
	//		in.close();
	//		// 绑定
	//		this.attach(b, 0, len);
	//	}

	/**
	 * 构造构造可类化数据读取器，从磁盘文件中解析数据流，在解析前先解码
	 * @param deflator 可类化数据解码器
	 * @param file 磁盘文件
	 * @throws IOException
	 */
	public ClassReader(ClassDeflator deflator, File file) throws IOException {
		// 文件不存在、不是文件属性、空文件
		if (!(file.exists() && file.isFile())) {
			throw new FileNotFoundException(file.toString());
		} else if (file.length() == 0) {
			throw new FileNotFoundException("empty " + file.toString());
		}
		// 读文件
		byte[] b = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		int len = in.read(b, 0, b.length);
		in.close();
		// 解码
		byte[] decodes = deflator.deflate(b, 0, len);
		// 绑定
		attach(decodes, 0, decodes.length);
	}

	/**
	 * 关联一个字节数组对象，同时与之前的数组对象解除关联，所有参数将重新设置。
	 * @param b 新的字节数组对象
	 * @param off 开始下标，最小下标是0。
	 * @param len 有效字节长度，必须大于0。
	 */
	public void attach(byte[] b, int off, int len) {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 1 || off > b.length
				|| (off + len > b.length) || (off + len < 0)) {
			throw new IndexOutOfBoundsException();
		}
		buff = b;
		begin = seek = off;
		end = off + len;
	}

	/**
	 * 字节数组的开始位置，不允许是负数。在关联时设置，之后不会改变。
	 * @return 开始位置
	 */
	public int getBegin() {
		return begin;
	}

	/**
	 * 字节数组的结束位置，不允许是负数。在关联时设置，之后不会改变。
	 * @return 结束位置
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * 返回当前字节数组的下标位置。这个位置随着数据读取将向后移动。
	 * @return 当前下标
	 */
	public int getSeek() {
		return seek;
	}

	/**
	 * 返回已经读取的长度
	 * @return 读取长度
	 */
	public int getUsed() {
		return seek - begin;
	}

	/**
	 * 返回剩余长度
	 * @return 返回整数值
	 */
	public int getLeft() {
		return end - seek;
	}

	/**
	 * 判断有剩余数据
	 * @return 返回真或者假
	 */
	public boolean hasLeft() {
		return getLeft() > 0;
	}

	/**
	 * 返回字节数据的有效长度
	 * @return 有效长度
	 */
	public int getLength() {
		return end - begin;
	}

	/**
	 * 根据传入的数据长度，判断读操作长度溢出
	 * @param len 指定的数据长度
	 * @return 读操作溢出返回真，否则假
	 * @throws ArithmeticException，如果传入长度是负数时
	 */
	public boolean isReadout(int len) {
		if (len < 0) {
			String e = String.format("%d < 0, must be >= 0", len);
			throw new ArithmeticException(e);
		}
		return seek + len > end;
	}

	/**
	 * 检查读长度溢出。如果发生溢出，将弹出数据长度异常
	 * @param len 指定的数据长度
	 * @throws ArrayIndexOutOfBoundsException, ArithmeticException
	 */
	public void checkReadout(int len) {
		if (isReadout(len)) {
			String e = String.format("%d + %d > %d", seek, len, end);
			throw new ArrayIndexOutOfBoundsException(e);
		}
	}

	/**
	 * 从当前下标开始，向后移动指定的长度
	 * @param len 如果此数是负数，表示回退(向左移动)；如果是正数，表示向前移动。
	 * 无论正数、负数，必须在定义的有效范围内(>=begin  and <end)。
	 * @throws IndexOutOfBoundsException
	 */
	public void skip(int len) {
		if (len < 0) {
			int size = Math.abs(len); // 绝对值
			if (seek - size < begin) { // 超出最小值
				String s = String.format("%d - %d < %d", seek, size, begin);
				throw new IndexOutOfBoundsException(s);
			}
			seek -= size;
		} else {
			if (seek + len > end) { // 超过最大值
				String s = String.format("%d + %d > %d", seek, len, end);
				throw new IndexOutOfBoundsException(s);
			}
			seek += len;
		}
	}

	/**
	 * 从指定下标位置读取一个字节，但是不移动位置指针
	 * @param off 指定下标
	 * @throws IndexOutOfBoundsException
	 * @return 读取的字节
	 */
	public byte at(int off) {
		if (off < begin || off >= end) {
			String s = String.format("%d < %d || %d >= %d", off, begin, off, end);
			throw new IndexOutOfBoundsException(s);
		}
		return buff[off];
	}

	/**
	 * 从指定下标位置读取指定长度的字节数组，但是不移动位置指针
	 * @param off 下标
	 * @param len 长度
	 * @throws IndexOutOfBoundsException
	 * @return 读取的字节数组
	 */
	public byte[] at(int off, int len) {
		if (off < 0 || len < 1 || off + len < 1 || off + len > end) {
			String s = String.format("%d < 0 || %d < 1 || %d + %d < 1 || %d + %d > %d",
					off, len, off, len, off, len, end);
			throw new IndexOutOfBoundsException(s);
		}
		return Arrays.copyOfRange(buff, off, off + len);
	}

	/**
	 * 跨过N个字节，读取1个字节
	 * @param skip 跨过长度（负数向前，正数向后）
	 * @return 返回字节
	 */
	public byte shift(int skip) {
		int off = seek + skip;
		return at(off);
	}

	/**
	 * 跨过N个字节，读取N个字节
	 * @param skip 跨过长度（负数向前，正数向后）
	 * @param len 读取长度，必须大于等于0，是正整数
	 * @return 字节数组
	 */
	public byte[] shift(int skip, int len) {
		if (len < 0) {
			String s = String.format("len %d < 0", len);
			throw new IndexOutOfBoundsException(s);
		}
		int off = seek + skip;
		return at(off, len);
	}

	/**
	 * 返回当前下标的字节
	 * @throws IndexOutOfBoundsException
	 * @return 当前字节
	 */
	public byte current() {
		if (seek >= end) {
			String s = String.format("%s >= %d", seek, end);
			throw new IndexOutOfBoundsException(s);
		}
		return buff[seek];
	}

	/**
	 * 从当前下标位置开始，复制指定长度的字节数组，但是不移动下标位置
	 * @throws IndexOutOfBoundsException
	 * @param len 长度
	 * @return 读取长度的字节数组
	 */
	public byte[] current(int len) {
		if (len < 1 || seek + len > end) {
			String s = String.format("%d < 1 || %d + %d > %d", len, seek, len, end);
			throw new IndexOutOfBoundsException(s);
		}
		return Arrays.copyOfRange(buff, seek, seek + len);
	}

	/**
	 * 读取指定长度的字节数组，并且字节数组指针移动要求的长度。这是最基本的读取接口。
	 * @param len 必须大于0，而且在有效的长度范围内
	 * @throws IndexOutOfBoundsException
	 * @return 读取的字节数组
	 */
	public byte[] read(int len) {
		if (len < 1 || seek + len > end) {
			String s = String.format("%d < 1 || %d + %d > %d", len, seek, len, end);
			throw new IndexOutOfBoundsException(s);
		}
		byte[] b = Arrays.copyOfRange(buff, seek, seek + len);
		seek += len;
		return b;
	}

	/**
	 * 读取一个字节，并且字节数组指针移动1个字节。这是最基本的读取接口。
	 * @throws IndexOutOfBoundsException
	 * @return 读取一个字节
	 */
	public byte read() {
		//		if (seek + 1 > end) {
		//			String s = String.format("%d + 1 > %d", seek, end);
		//			throw new IndexOutOfBoundsException(s);
		//		}
		//		byte value = buff[seek];
		//		seek += 1;
		//		return value;

		byte[] b = read(1);
		return b[0];
	}

	/**
	 * 读取一个预定义长度的字节数组。在字节数组的前面已经标识了长度
	 * @return 字节数组句柄
	 */
	public byte[] readByteArray() {
		// 读取长度
		int len = readInt();
		// 如果是负数，这是一个错误
		if (len < 0) {
			throw new ClassableException("illegal size %d", len);
		} else if (len == 0) {
			return null; // 允许空值
		}
		// 读字节数组
		return read(len);
	}

	/**
	 * 读取一个布尔值，并且字节数组指针移动1个字节
	 * @throws IndexOutOfBoundsException
	 * @return 读取一个布尔值
	 */
	public boolean readBoolean() {
		byte b = read();
		return (b == 1);
	}

	/**
	 * 读取布尔值数组
	 * @return 字节数组
	 */
	public boolean[] readBooleanArray() {
		int size = readInt();
		// -1是空指针，0是空数组，其它小于0的数字是异常
		if (size == -1) {
			return null;
		} else if (size == 0) {
			return new boolean[0];
		} else if (size < 0) {
			throw new ClassableException("illegal size %d", size);
		}

		boolean[] array = new boolean[size];
		for (int i = 0; i < size; i++) {
			array[i] = readBoolean();
		}
		return array;
	}

	/**
	 * 读取一个字符，并且字节数组指针移动2个字节
	 * @return 一个字符
	 * @throws IndexOutOfBoundsException
	 */
	public char readChar() {
		byte[] b = read(2);
		return Laxkit.toChar(b);
	}

	/**
	 * 读字符数组。字符长度在内部指定。
	 * @return 字符数组
	 */
	public char[] readCharArray() {
		int size = readInt();

		// -1是空指针，0是空数组，其它小于0的数字是异常
		if (size == -1) {
			return null;
		} else if (size == 0) {
			return new char[0];
		} else if (size < 0) {
			throw new ClassableException("illegal size %d", size);
		}

		char[] array = new char[size];
		for (int i = 0; i < size; i++) {
			array[i] = readChar();
		}
		return array;
	}

	/**
	 * 读取一个短整型，并且字节数组指针移动2个字节
	 * @return 短整型数值
	 */
	public short readShort() {
		byte[] b = read(2);
		return Laxkit.toShort(b);
	}

	/**
	 * 读短整型数组
	 * @return 短整型数组
	 */
	public short[] readShortArray() {
		int size = readInt();
		// -1是空指针，0是空数组，其它小于0的数字是异常
		if (size == -1) {
			return null;
		} else if (size == 0) {
			return new short[0];
		} else if (size < 0) {
			throw new ClassableException("illegal size %d", size);
		}

		short[] array = new short[size];
		for (int i = 0; i < size; i++) {
			array[i] = readShort();
		}
		return array;
	}

	/**
	 * 读取一个整型，并且字节数组指针移动4个字节
	 * @return 整型值
	 */
	public int readInt() {
		byte[] b = read(4);
		return Laxkit.toInteger(b);
	}

	/**
	 * 读整型值数组
	 * @return 整型值数组
	 */
	public int[] readIntArray() {
		int size = readInt();
		// -1是空指针，0是空数组，其它小于0的数字是异常
		if (size == -1) {
			return null;
		} else if (size == 0) {
			return new int[0];
		} else if (size < 0) {
			throw new ClassableException("illegal size %d", size);
		}

		int[] array = new int[size];
		for (int i = 0; i < size; i++) {
			array[i] = readInt();
		}
		return array;
	}

	/**
	 * 读取一个长整型，然后字节数组指针移动8个字节
	 * @throws IndexOutOfBoundsException
	 * @return 长整数
	 */
	public long readLong() {
		byte[] b = read(8);
		return Laxkit.toLong(b);
	}

	/**
	 * 读长整型数组
	 * @return 长整型数组
	 */
	public long[] readLongArray() {
		int size = readInt();

		// -1是空指针，0是空数组，其它小于0的数字是异常
		if (size == -1) {
			return null;
		} else if (size == 0) {
			return new long[0];
		} else if (size < 0) {
			throw new ClassableException("illegal size %d", size);
		}

		long[] array = new long[size];
		for (int i = 0; i < size; i++) {
			array[i] = readLong();
		}
		return array;
	}

	/**
	 * 读取一个单浮点数，并且字节数组指针移动4个字节
	 * @throws IndexOutOfBoundsException
	 * @return 单浮点数
	 */
	public float readFloat() {
		int value = readInt();
		return java.lang.Float.intBitsToFloat(value);
	}

	/**
	 * 读浮点值数组
	 * @return 单浮点数组
	 */
	public float[] readFloatArray() {
		int size = readInt();
		// -1是空指针，0是空数组，其它小于0的数字是异常
		if (size == -1) {
			return null;
		} else if (size == 0) {
			return new float[0];
		} else if (size < 0) {
			throw new ClassableException("illegal size %d", size);
		}

		float[] array = new float[size];
		for (int i = 0; i < size; i++) {
			array[i] = readFloat();
		}
		return array;
	}

	/**
	 * 读取一个双浮点数，并且字节数组指针移动8个字节
	 * @throws IndexOutOfBoundsException
	 * @return 双浮点
	 */
	public double readDouble(){
		long value = readLong();
		return java.lang.Double.longBitsToDouble(value);
	}

	/**
	 * 读双浮点数组
	 * @return 双浮点数组
	 */
	public double[] readDoubleArray() {
		int size = readInt();
		// -1是空指针，0是空数组，其它小于0的数字是异常
		if (size == -1) {
			return null;
		} else if (size == 0) {
			return new double[0];
		} else if (size < 0) {
			throw new ClassableException("illegal size %d", size);
		}

		double[] array = new double[size];
		for (int i = 0; i < size; i++) {
			array[i] = readDouble();
		}
		return array;
	}

	/**
	 * 读取一个字符串，字符串统计采用UTF8解码。允许输出空指针。
	 * @throws IndexOutOfBoundsException
	 * @return 字符串
	 */
	public String readString() {
		// 读长度
		int len = readInt();

		// 如果是-1，表示空指针。0表示空字符串，其它小于0是异常。
		if (len == -1) {
			return null;
		} else if (len == 0) {
			return new String();
		} else if (len < 0) {
			throw new ClassableException("illegal size %d", len);
		}

		// 读字节
		byte[] b = read(len);
		// UTF8解码后输出
		return new UTF8().decode(b);
	}

	/**
	 * 读取一个文件实例。先读取字符串，判断转成File实例或者空指针输出。
	 * @return 返回文件实例，或者空指针。
	 */
	public File readFile() {
		String path = readString();
		if (path == null) {
			return null;
		}
		return new File(path);
	}

	/**
	 * 读字符串数组
	 * @return 字符串数组
	 */
	public String[] readStringArray() {
		int size = readInt();
		// -1是空指针，0是空数组，其它小于0的数字是异常
		if (size == -1) {
			return null;
		} else if (size == 0) {
			return new String[0];
		} else if (size < 0) {
			throw new ClassableException("illegal size %d", size);
		}

		String[] array = new String[size];
		for (int i = 0; i < size; i++) {
			array[i] = readString();
		}
		return array;
	}

	/**
	 * 支持一个类是否实现了Classable接口
	 * @param that 判断类
	 * @return 支持Classable接口返回真，否则假
	 */
	private boolean checkClassable(Class<?> that) {
		Class<?>[] clazz = that.getInterfaces();
		for (int i = 0; i < clazz.length; i++) {
			if (clazz[i] == Classable.class) {
				return true;
			}
		}
		Class<?> parent = that.getSuperclass();
		if (parent != null) {
			return checkClassable(parent);
		}
		return false;
	}

	/**
	 * 打印错误堆栈
	 * @param fatal 异常或者错误
	 * @return 返回错误堆栈文本
	 */
	private String printThrowable(Throwable fatal) {
		return Laxkit.printThrowable(fatal);
	}

	/**
	 * 根据传入的java.lang.Class<?>，构建一个实现“Classable”接口和解析后的类实例。<br><br>
	 * 
	 * 传入类的要求和特点说明：<br>
	 * 1. 实现Classable接口的“build”和“resolve”方法。<br>
	 * 2. 类公共构造方法，必须有这样两个中的任意一个：不带形参，即xxx()；或者形参声明“ClassReader”，即 xxx(ClassReader reader)。两者选一，使用“newInstance”方法产生类实例。<br>
	 * 3. 若公共构造方法达不到上述要求，将抛出ClassableException异常。<br>
	 * 4. 私有和保护的构造方法不会被处理。<br>
	 * 
	 * @param clazz 需要被构造的类。
	 * @throws ClassableException, IndexOutOfBoundsException
	 * @return 返回被要求构造的类实例
	 */
	public Classable readObject(java.lang.Class<?> clazz) {
		// 枚举所有公共构造方法
		Constructor<?>[] constructors = clazz.getConstructors();
		// 选择一个首先出现和匹配的公共构造方法
		try {
			for (int i = 0; i < constructors.length; i++) {
				// 提取构造方法中的参数
				Class<?>[] types = constructors[i].getParameterTypes();

				if (types.length == 0) {
					// 调用空构造方法
					Object object = constructors[i].newInstance((Object[]) null);
					// 检查是否继承Classable接口
					if (!checkClassable(object.getClass())) {
						throw new ClassableException("illegal Classable");
					}
					// 转化为可类化接口，调用"resolve"方法解析类参数
					Classable that = (Classable) object;
					that.resolve(this);
					return that;
				} else if (types.length == 1 && types[0] == ClassReader.class) {
					// 在构造方法的同时，构造方法调用"resolve"方法解析参数
					Object object = constructors[i].newInstance(new Object[] { this });
					// 检查是否支持Classable接口
					if (!checkClassable(object.getClass())) {
						throw new ClassableException("illegal Classable");
					}
					return (Classable) object;
				}
			}
		} catch (IllegalArgumentException e) {
			throw new ClassableException(clazz.getName() + "/" + printThrowable(e));
		} catch (InstantiationException e) {
			throw new ClassableException(clazz.getName() + "/" + printThrowable(e));
		} catch (IllegalAccessException e) {
			throw new ClassableException(clazz.getName() + "/" + printThrowable(e));
		} catch (InvocationTargetException e) {
			throw new ClassableException(clazz.getName() + "/" + printThrowable(e));
		}
		// wrong 
		throw new ClassableException("illegal %s", clazz.getName());
	}

	/**
	 * 根据传入的类类型，解析和建立一个实现“Classable”接口的类实例；如果对象无效，返回空指针。<br>
	 * 
	 * @param <T> 类类型
	 * @param clazz 需要被构造的类。
	 * @return Classable对象实例，或者空指针
	 */
	@SuppressWarnings("unchecked")
	public <T> T readInstance(java.lang.Class<?> clazz) {
		// 判断对象有效
		boolean enabled = readBoolean();
		// 如果有效，解析它；否则是空指针返回
		if (enabled) {
			Classable that = readObject(clazz);
			return (T) that;
		} else {
			return null;
		}
	}

	/**
	 * 判断类有效，然后读取类的实体名称，生成一个实现了可类化接口对象实例并且返回
	 * @return 返回可类化对象或者空值
	 */
	public Classable readDefault() {
		// 读空值标记符
		boolean enabled = readBoolean();
		// 是空值，返回空指针；否则读对象
		if (!enabled) {
			return null;
		}
		// 读取类名称
		String clazzName = readString();
		// 转化为类
		Class<?> clazz = null;
		try {
			clazz = java.lang.Class.forName(clazzName);
		} catch (ClassNotFoundException e) {
			throw new ClassableException(printThrowable(e));
		}
		// 根据类实体名称，读取实例
		return readObject(clazz);
	}

}