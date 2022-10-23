/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.turn;


import java.io.*;

import com.laxcus.util.classable.*;

/**
 * 原生数据解码器 <br><br>
 * 
 * RPC反馈结果。由服务端根据请求命令生成，含有在服务端产生的命令应答对象和异常，以字节数组方式输出，通过网络传递给客户端，被客户端接收后解析。
 * 
 * @author scott.liang
 * @version 1.0 11/07/2015
 * @since laxcus 1.0
 */
public final class TubExtractor extends TubFormatter {

	private static final long serialVersionUID = 7515211435181389986L;

	/** 应答结果类 */
	private Object object;

	/** 错误 */
	private Throwable fatal;

	/**
	 * 将原生数据解码器写入可类化存储器
	 * @param writer 可类化存储器
	 * @return 返回生成的字节长度
	 */
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 写入对象
		boolean enabled = (object != null);
		writer.writeBoolean(enabled);
		if (enabled) {
			byte[] b = writeObject(object);
			writer.write(b);
		}
		// 串行化
		enabled = (fatal != null);
		writer.writeBoolean(enabled);
		if (enabled) {
			byte[] b = writeObject(fatal);
			writer.write(b);
		}
		// 返回写入的字节数组长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析原生数据解码器
	 * @param reader 可类化读取器
	 * @return 返回解析的数据长度
	 */
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 读返回对象
		boolean enabled = reader.readBoolean();
		if (enabled) {
			object = readObject(reader);
		} else {
			object = null;
		}
		// 读异常信息
		enabled = reader.readBoolean();
		if (enabled) {
			fatal = (Throwable) readObject(reader);
		} else {
			fatal = null;
		}
		// 返回读取的字节数组长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造原生数据解码器集
	 */
	public TubExtractor() {
		super();
	}

	/**
	 * 构造原生数据解码器，指定全部参数参数
	 * @param object 对象
	 * @param fatal 错误
	 */
	public TubExtractor(Object object, Throwable fatal) {
		this();
		setObject(object);
		setThrowable(fatal);
	}

	/**
	 * 构造原生数据解码器，指定参数
	 * @param object 对象
	 */
	public TubExtractor(Object object) {
		this(object, null);
	}

	/**
	 * 构造构造原生数据解码器，
	 * @param fatal 错误
	 */
	public TubExtractor(Throwable fatal) {
		this(null, fatal);
	}

	/**
	 * 从可类化数据读取器中解析原生数据解码器
	 * @param reader 可类化数据读取器
	 */
	public TubExtractor(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从字节数组中解析原生数据解码器
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 长度
	 */
	public TubExtractor(byte[] b, int off, int len) {
		this(new ClassReader(b, off, len));
	}

	/**
	 * 返回对象
	 * @return Object实例
	 */
	public Object getObject() {
		return this.object;
	}

	/**
	 * 设置对象
	 * @param e Object实例
	 */
	public void setObject(Object e) {
		object = e;
	}

	/**
	 * 设置处理过程中的错误
	 * 
	 * @param e Throwable及子类实例
	 */
	public void setThrowable(Throwable e) {
		fatal = e;
	}

	/**
	 * 返回处理过程中的错误
	 * @return Throwable及子类实例
	 */
	public Throwable getThrowable() {
		return fatal;
	}

	/**
	 * 返回错误堆栈信息
	 * @return 字符串
	 */
	public String getThrowText() {
		return TubExtractor.getMessage(fatal);
	}

	/**
	 * 返回错误信息堆栈
	 * @param e Throwable及子类实例
	 * @return 错误字符串
	 */
	public static String getMessage(Throwable e) {
		if (e == null) {
			return "";
		}
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
		PrintStream s = new PrintStream(buff, true);
		e.printStackTrace(s);
		byte[] b = buff.toByteArray();
		return new String(b, 0, b.length);
	}

	/**
	 * 将原生数据解码器生成字节数组和输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 将传输对象生成字节数组和输出
	 * @param object 对象实例 
	 * @return 字节数组
	 * @throws IOException
	 */
	public static byte[] build(Object object) throws IOException {
		TubExtractor reply = new TubExtractor(object);
		return reply.build();
	}

	/**
	 * 解析原生数据解码器
	 * @param b  字节数组
	 * @param off 数据开始下标
	 * @param len 有效数据长度
	 * @return 返回TubExtractor实例
	 */
	public static TubExtractor resolve(byte[] b, int off, int len) {
		return new TubExtractor(b, off, len);
	}

	/**
	 * 解析原生数据解码器
	 * @param b  字节数组
	 * @return 返回TubExtractor实例
	 */
	public static TubExtractor resolve(byte[] b) {
		return new TubExtractor(b, 0, b.length);
	}

}