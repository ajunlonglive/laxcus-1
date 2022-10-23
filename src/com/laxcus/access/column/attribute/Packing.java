/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column.attribute;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 数据封装 <br><br>
 * 
 * 数据封装为可变长数据类型提供压缩和加密的参数 <br>
 * 可变长数据类型包括：RAW、VIDEO、IMAGE、DOCUMENT、AUDIO、CHAR、WCHAR、HCHAR。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public final class Packing implements Classable, Markable, Serializable, Cloneable {

	private static final long serialVersionUID = -1745372499935389885L;

	/** 封装类型(包含压缩和加密两种方式，允许同时存在)，格式: 压缩|加密。默认是0，不打包 **/
	private int style;

	/** 加密算法密码(在用户要求加密的前提下) **/
	private byte[] password;

	/**
	 * 将封包数据写入可类化写入器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 写标记
		writer.writeInt(style);
		// 写密码
		writer.writeByteArray(password);
		// 输出
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析封包数据
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 读标记
		style = reader.readInt();
		// 读密码
		password = reader.readByteArray();
		// 返回数据的读取长度
		return reader.getSeek() - seek;
	}

	/**
	 * 使用传入的数据封装参数，构造它的副本
	 * @param that Packing实例
	 */
	private Packing(Packing that) {
		this();
		style = that.style;
		setPassword(that.password);
	}

	/**
	 * 构造一个默认的数据封装，不包含封装类型和密码。
	 */
	public Packing() {
		super();
		style = 0;
	}

	/**
	 * 从可类化读取器中解析数据封装参数
	 * @param reader  可类化读取器
	 * @since 1.1
	 */
	public Packing(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出数据封装参数
	 * @param reader 标记化读取器
	 */
	public Packing(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 构造一个数据封装，并且指定压缩算法、加密算法、加密密码
	 * @param compress  压缩算法
	 * @param encrypt  加密算法
	 * @param password  加密密文
	 */
	public Packing(int compress, int encrypt, byte[] password) {
		this();
		setPacking(compress, encrypt, password);
	}

	/**
	 * 设置数据封装参数，包括压缩算法、加密算法、加密密码
	 * @param compress  压缩算法
	 * @param encrypt  加密算法
	 * @param password  加密密码
	 */
	public void setPacking(int compress, int encrypt, byte[] password) {
		// 合并压缩和加密算法
		style = PackingTag.combine(compress, encrypt);
		// 如果不包含加密算法，密码为空值
		if (!PackingTag.isEncrypt(style)) {
			setPassword(null);
		} else {
			if (Laxkit.isEmpty(password)) {
				throw new NullPointerException("null password");
			}
			setPassword(password, 0, password.length);
		}
	}

	/**
	 * 返回封装压缩算法
	 * @return 压缩算法的整型值描述
	 */
	public int getCompress() {
		return PackingTag.getCompress(style);
	}

	/**
	 * 返回封装加密算法
	 * @return 加密算法的整型值描述
	 */
	public int getEncrypt() {
		return PackingTag.getEncrypt(style);
	}

	/**
	 * 判断包含压缩算法
	 * @return 压缩算法的整型值描述
	 */
	public boolean isCompress() {
		return PackingTag.isCompress(style);
	}

	/**
	 * 判断包含加密算法
	 * @return 返回真或者假
	 */
	public boolean isEncrypt() {
		return PackingTag.isEncrypt(style);
	}

	/**
	 * 判断是数据封装
	 * @return 返回真或者假
	 */
	public boolean isEnabled() {
		return style != 0;
	}

	/**
	 * 设置加密密码
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 */
	public void setPassword(byte[] b, int off, int len) {
		if (b == null || len < 1) {
			password = null;
		} else {
			password = new byte[len];
			System.arraycopy(b, off, password, 0, len);
		}
	}

	/**
	 * 设置加密密码
	 * 
	 * @param b 字节数组
	 */
	public void setPassword(byte[] b) {
		setPassword(b, 0, (b == null ? 0 : b.length));
	}

	/**
	 * 返回加密密码
	 * 
	 * @return 字节数组
	 */
	public byte[] getPassword() {
		return password;
	}

	/**
	 * 将封包数据转化为字节数组输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从数据数组中解析封包数据
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}

	/**
	 * 生成当前封装实例的数据副本
	 * @return Packing实例
	 */
	public Packing duplicate() {
		return new Packing(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return PackingTag.translate(style);
	}
}