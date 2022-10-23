/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.row;

import java.io.*;

import com.laxcus.util.classable.*;

/**
 * 行标记 <br><br>
 * 
 * 在行的开始出现，由4个参数组成，共11个字节：<br>
 * 1. 行状态（1个字节，有效或者删除）<br>
 * 2. 行的CRC32校验和（4个字节）<br>
 * 3. 一行长度（4字节，包括本身的11个字节）<br>
 * 4. 一行的列成员数目（2个字节）<br><br>
 * 
 * @author scott.liang
 * @version 1.1 9/17/2015
 * @since laxcus 1.0
 */
public final class RowTag implements Classable, Cloneable, Serializable {

	private static final long serialVersionUID = 3710488527582809257L;

	/** 当前行状态 (有效或者被删除) **/
	public final static byte VALID = 1;

	public final static byte DELETE = 2;

	/** 行状态，有效或被删除 **/
	byte status; 

	/** CRC32校验码 (不包括状态和校验码，从行总长度开始之后的数据校验值) **/
	int checksum; 

	/** 一行数据长度（包括头标识11字节，和列数据流长度) **/
	int length; 

	/** 列成员数目 **/
	short columns; 

	/**
	 * 构造默认的行标记
	 */
	public RowTag() {
		super();
		status = 0;
		checksum = 0;
		length = 0;
		columns = 0;
	}

	/**
	 * 生成行标记的数据副本
	 * @param that 行标记
	 */
	private RowTag(RowTag that) {
		this();
		set(that);
	}

	/**
	 * 从可类化读取器中解析“行标记”参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RowTag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 行头标记长度(11字节)
	 * @return 返回数字11
	 */
	public final int volume() {
		return 11;
	}

	/**
	 * 设置全部参数
	 * @param that 传入的行标记实例
	 */
	public void set(RowTag that) {
		status = that.status;
		checksum = that.checksum;
		length = that.length;
		columns = that.columns;
	}

	/**
	 * 设置行状态
	 * @param b 行状态
	 */
	public void setStatus(byte b) {
		status = b;
	}

	/**
	 * 返回行状态
	 * @return 行状态
	 */
	public byte getStatus() {
		return status;
	}

	/**
	 * 返回CRC32校验和
	 * @return 校验和
	 */
	public int getChecksum() {
		return checksum;
	}

	/**
	 * 设置CRC32校验和
	 * @param i 校验和
	 */
	public void setChunksum(int i) {
		checksum = i;
	}

	/**
	 * 返回一行数据长度
	 * @return 长度
	 */
	public int getLength() {
		return length;
	}

	/**
	 * 设置一行数据长度
	 * @param i  长度
	 */
	public void setLength(int i) {
		length = i;
	}

	/**
	 * 设置列数
	 * @param i 列数
	 */
	public void setColumns(short i) {
		columns = i;
	}

	/**
	 * 返回列数
	 * @return 列数
	 */
	public short getColumns() {
		return columns;
	}

	/**
	 * 生成数据副本
	 * @return RowTag实例
	 */
	public RowTag duplicate() {
		return new RowTag(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.write(status);
		writer.writeInt(checksum);
		writer.writeInt(length);
		writer.writeShort(columns);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		status = reader.read();
		checksum = reader.readInt();
		length = reader.readInt();
		columns = reader.readShort();
		return reader.getSeek() - seek;
	}

}