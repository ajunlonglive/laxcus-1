/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.markable;

import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.classable.*;

/**
 * 标记化声明。<br>
 * 
 * 执行编号（短整数）和名字（字符串）的转换工作。
 * 
 * @author scott.liang
 * @version 1.0 10/21/2017
 * @since laxcus 1.0
 */
public class MarkStamp {
	
	/** 标记化记录器 **/
	private MarkRecorder recorder ;

	/**
	 * 设置标记化记录器，不允许空指针
	 * @param e 标记化记录器
	 */
	protected void setRecorder(MarkRecorder e) {
		Laxkit.nullabled(e);
		recorder = e;
	}

	/**
	 * 返回标记化记录器
	 * @return 标记化记录器
	 */
	protected MarkRecorder getRecorder() {
		return recorder;
	}

	/**
	 * 构造默认的标记化声明
	 */
	protected MarkStamp() {
		super();
	}

	/**
	 * 生成标记化声明的数据副本
	 * @param that 标记化声明
	 */
	protected MarkStamp(MarkStamp that) {
		this();
		recorder = that.recorder;
	}
	
	/**
	 * 采用大字头编码，将短整数转成字节数组
	 * @param value 短整数
	 * @return 字节数组
	 */
	private byte[] toBytes(short value) {
		byte[] b = new byte[2];
		b[0] = (byte) ((value >> 8) & 0xFF);
		b[1] = (byte) (value & 0xFF);
		return b;
	}

	/**
	 * 把大字头编码的字节数组转为短整数
	 * @param b 字节数组
	 * @return 输出短整数
	 */
	private short toShort(byte[] b) {
		short value = (short) ((b[0] & 0xFF) << 8);
		value |= (short) (b[1] & 0xFF);
		return value;
	}

	/**
	 * 采用大字头编码，将整数转成字节数组
	 * @param value 整数
	 * @return 字节数组
	 */
	private byte[] toBytes(int value) {
		byte[] b = new byte[4];
		b[0] = (byte) ((value >> 24) & 0xFF);
		b[1] = (byte) ((value >> 16) & 0xFF);
		b[2] = (byte) ((value >> 8) & 0xFF);
		b[3] = (byte) (value & 0xFF);
		return b;
	}

	/**
	 * 把大字头编码的字节数组转为整数
	 * @param b 字节数组
	 * @return 输出整数
	 */
	private int toInteger(byte[] b) {
		int value = ((b[0] & 0xFF) << 24);
		value |= ((b[1] & 0xFF) << 16);
		value |= ((b[2] & 0xFF) << 8);
		value |= ((b[3] & 0xFF));
		return value;
	}

	/**
	 * 判断包含标记码
	 * @param reader 可类化读取器
	 * @return 返回真或者假
	 */
	protected boolean isIdentity(ClassReader reader) {
		byte b = reader.current();
		return (b & 0x80) == 0x80; // 用“与”操作判断有标记
	}

	/**
	 * 写入标记码
	 * @param id 标记码
	 * @param writer 可类化写入器
	 */
	protected void writeIdentity(short id, ClassWriter writer) {
		byte[] b = toBytes(id);
		b[0] = (byte) (b[0] ^ 0x80); // 用“异或”插入一个标记
		writer.write(b);
	}

	/**
	 * 从可类化读取器中读取标记码
	 * @param reader 可类化读取器
	 * @return 返回短整数
	 */
	protected short readIdentity(ClassReader reader) {
		byte[] b = reader.read(2);
		short value = toShort(b);
		return (short) (value & 0x7FFF);
	}

	/**
	 * 写入名称
	 * @param name 名称
	 * @param writer 可类化存储器
	 */
	protected void writeName(String name, ClassWriter writer) {
		byte[] b = new UTF8().encode(name);
		byte[] len = toBytes(b.length);
		writer.write(len);
		writer.write(b);
	}

	/**
	 * 读出名称
	 * @param reader 可类化读取器
	 * @return 返回名称
	 */
	protected String readName(ClassReader reader) {
		byte[] b = reader.read(4);
		int len = toInteger(b);
		b = reader.read(len);
		return new UTF8().decode(b);
	}

}