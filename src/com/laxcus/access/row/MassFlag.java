/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.row;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块标识 <br><br>
 * 
 * 以数据块为单位，说明一个数据块的基本信息，它在数据块的开头出现。<br>
 * 
 * 数据块标识用来做为解析SELECT、DELETE、INSERT输出数据的判断。<br><br>
 * 
 * 说明：<br>
 * “sm”字段只是用来说明底层的数据存储模型。对于输出的数据格式，无论它的底层数据存储格式是NSM或者DSM，输出的数据格式总是NSM（与数据输入保持一致和方便解析）<br>
 * 
 * @author scott.liang
 * @version 1.1 9/17/2015
 * @since laxcus 1.0
 */
public final class MassFlag implements Classable {

	/** 返回结果中的数据流长度(不包括MassFlag自身的尺寸) **/
	private long length;

	/** 模(兼容数据分片，JNI返回总是0) **/
	private long mod;

	/** 行数 */
	private int rows;

	/** 列数（一行中的列数） */
	private short columns;

	/** JNI底层数据存储模型（NSM/DSM），见 "com.laxcus.access.type.StorageModel" **/
	private byte model;

	/** 数据表名 **/
	private Space space;

	/**
	 * 将参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 后续的数据流长度
		writer.writeLong(length);
		// 模值(用于分布计算，无定义是0)
		writer.writeLong(mod);
		// 行记录总数
		writer.writeInt(rows);
		// 每行的列成员数
		writer.writeShort(columns);
		// 存储模式(NSM/DSM)
		writer.write(model);
		// 写入数据表名
		space.build(writer);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析数据流
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		if (reader.isReadout(presize())) {
			throw new IndexOutOfBoundsException("size missing!");
		}
		// 检索结果数据流长度
		length = reader.readLong();
		// 模值
		mod = reader.readLong();
		// 行记录数
		rows = reader.readInt();
		// 每行列成员数
		columns = reader.readShort();
		// 存储模型
		model = reader.read();
		// 数据表名
		space = new Space(reader);

		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个默认的数据块标识
	 */
	public MassFlag() {
		length = 0L;
		mod = 0L;
		rows = 0;
		columns = 0;
		model = 0;
	}

	/**
	 * 构造数据块标识信息，指定它的数据表名
	 * @param space 数据表名
	 */
	public MassFlag(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析数据块标识
	 * @param reader 可类化读取器
	 */
	public MassFlag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从字节数组中解析数据块标识
	 * @param b 字节数组
	 */
	public MassFlag(byte[] b) {
		this();
		resolve(b);
	}

	/**
	 * 前缀字节长度，忽略数据表名中的字符部分，即带有数据表名前两个字节
	 * @return 返回25
	 */
	public final int presize() {
		return 25; 
	}

	/**
	 * 设置检索结果数据流长度
	 * @param i 长度
	 */
	public void setLength(long i) {
		length = i;
	}

	/**
	 * 检索数据流长度
	 * @return 长整型长度
	 */
	public long getLength() {
		return length;
	}

	/**
	 * 设置模值
	 * @param i 模值
	 */
	public void setMod(long i) {
		mod = i;
	}

	/**
	 * 返回模值
	 * @return 模值
	 */
	public long getMod() {
		return mod;
	}

	/**
	 * 设置行数
	 * @param i 行数
	 */
	public void setRows(int i){
		rows = i;
	}

	/**
	 * 增加行数
	 * @param i 行数
	 */
	public void addRows(int i) {
		rows += i;
	}

	/**
	 * 返回行数
	 * @return 行数
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * 设置列成员数
	 * @param i 列数
	 */
	public void setColumns(short i) {
		columns = i;
	}

	/**
	 * 返回列成员数目
	 * @return 列数
	 */
	public short getColumns() {
		return columns;
	}

	/**
	 * 设置存储模型 (NSM, DSM)
	 * @param who 存储模型
	 */
	public void setModel(byte who) {
		// 如果不合法，弹出异常
		if(!StorageModel.isFamily(who)) {
			throw new IllegalValueException("illegal storage model:%d", who);
		}
		model = who;
	}

	/**
	 * 返回存储模型 (NSM, DSM)
	 * @return 存储模型
	 */
	public byte getModel() {
		return model;
	}

	/**
	 * 判断是行存储模型（NSM，row storage model）
	 * @return 返回真或者假
	 */
	public boolean isNSM() {
		return StorageModel.isNSM(model);
	}

	/**
	 * 判断是列存储模型（DSM、column storage model）
	 * @return 返回真或者假
	 */
	public boolean isDSM() {
		return StorageModel.isDSM(model);
	}

	/**
	 * 设置数据表名
	 * @param e 数据表名
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return 数据表名
	 */
	public final Space getSpace() {
		return space;
	}

	/**
	 * 从输入数据流中解析数据
	 * @param in 输入流
	 * @return 返回解析的字节长度
	 * @throws IOException
	 */
	public int resolve(InputStream in) throws IOException {
		// 读前缀数据
		byte[] prefix = new byte[presize()];
		int len = in.read(prefix, 0, prefix.length);
		if (len != prefix.length) {
			String e = String.format("prefix missing! %d != %d", len, prefix.length);
			throw new IOException(e);
		}

		byte schemaSize = prefix[prefix.length - 2];
		byte tableSize = prefix[prefix.length - 1];
		boolean success = (Space.isSchemaSize(schemaSize) && Space.isTableSize(tableSize));
		if (!success) {
			String e = String.format("space error! schema size:%d, table size:%d", 
					schemaSize, tableSize);
			throw new IOException(e);
		}

		// 读后缀数据（数据表名）
		len = schemaSize + tableSize;
		byte[] suffix = new byte[len];
		len = in.read(suffix, 0, suffix.length);
		if (len != suffix.length) {
			String e = String.format("suffix missing! %d != %d", len, prefix.length);
			throw new IOException(e);
		}

		// 合并数据
		len = prefix.length + suffix.length;
		ClassWriter writer = new ClassWriter(len);
		writer.write(prefix);
		writer.write(suffix);

		// 输出和解析数据
		byte[] b = writer.effuse();
		return resolve(new ClassReader(b));
	}

	/**
	 * 生成参数并且输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter buff = new ClassWriter(128);
		build(buff);
		return buff.effuse();
	}

	/**
	 * 从字节数组中解析
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 返回解析长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}

	/**
	 * 从字节数组中解析
	 * @param b 字节数组
	 * @return 返回解析长度
	 */
	public int resolve(byte[] b) {
		return resolve(b, 0, b.length);
	}

}