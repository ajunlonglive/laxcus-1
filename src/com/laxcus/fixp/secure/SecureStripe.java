/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.secure;

import java.io.*;
import java.math.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * RSA密钥
 * 
 * 分为系数/指数
 * 
 * @author scott.liang
 * @version 1.0 2/10/2021
 * @since laxcus 1.0
 */
public abstract class SecureStripe implements Classable, Serializable, Cloneable {
	
	private static final long serialVersionUID = 700411993577920179L;

	/** RSA密钥系数 **/
	private byte[] modulus;

	/** RSA密钥指数 **/
	private byte[] exponent;
	
	/**
	 * 构造默认对象
	 */
	protected SecureStripe() {
		super();
	}
	
	/**
	 * 构造对象
	 * @param modulus 系数
	 * @param exponent 指数
	 */
	protected SecureStripe(BigInteger modulus, BigInteger exponent) {
		this();
		setModulus(modulus);
		setExponent(exponent);
	}

	/**
	 * 生成副本
	 * @param that
	 */
	protected SecureStripe(SecureStripe that) {
		this();
		// 生成副本
		setModulus(that.modulus);
		setExponent(that.exponent);
	}
	
	/**
	 * 设置系数
	 * @param b 字节数组
	 */
	public void setModulus(byte[] b) {
		// 判断空值
		if (b == null || b.length == 0) {
			throw new NullPointerException();
		}
		// 保存系数
		modulus = new byte[b.length];
		System.arraycopy(b, 0, modulus, 0, modulus.length);
	}
	
	/**
	 * 设置系数
	 * @param value
	 */
	public void setModulus(BigInteger value) {
		String hex = value.toString(16);
		// System.out.println("modulus "+hex);
		setModulus(Laxkit.htoi(hex));
	}

	/**
	 * 返回系数
	 * @return 字节数组
	 */
	public byte[] getModulus() {
		return modulus;
	}
	
	/**
	 * 返回16进制的字符串系数
	 * @return 字符串
	 */
	public String getHexModulus() {
		return Laxkit.itoh(modulus);
	}
	
	/**
	 * 设置指数
	 * @param b 字节数组
	 */
	public void setExponent(byte[] b) {
		// 判断空值
		if (b == null || b.length == 0) {
			throw new NullPointerException();
		}
		// 保存指数
		exponent = new byte[b.length];
		System.arraycopy(b, 0, exponent, 0, exponent.length);
	}
	
	/**
	 * 设置指数
	 * @param value 数值
	 */
	public void setExponent(BigInteger value) {
		String hex = value.toString(16);
		// System.out.println("exponent "+hex);
		setExponent(Laxkit.htoi(hex));
	}

	/**
	 * 返回指数
	 * @return 字节数组
	 */
	public byte[] getExponent() {
		return exponent;
	}
	
	/**
	 * 返回16进制的字符串指数
	 * @return 字符串
	 */
	public String getHexExponent() {
		return Laxkit.itoh(exponent);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 系数/指数
		writer.writeByteArray(modulus);
		writer.writeByteArray(exponent);
		// 子类参数
		buildSuffix(writer);
		// 返回写入的数据长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		modulus = reader.readByteArray();
		exponent = reader.readByteArray();
		// 子类参数
		resolveSuffix(reader);
		// 返回读取的数据长度
		return reader.getSeek() - seek;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", Laxkit.itoh(modulus), Laxkit.itoh(exponent));
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(modulus) ^ Arrays.hashCode(exponent);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}
	
	/**
	 * 生成副本
	 * @return
	 */
	public abstract SecureStripe duplicate();

	/**
	 * 将操作命令参数信息写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 * @since 1.1
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析操作命令参数信息
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	protected abstract void resolveSuffix(ClassReader reader);

}