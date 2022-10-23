/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.io.*;

import com.laxcus.util.classable.*;

/**
 * 用户签名键。<br>
 * 两个参数：用户签名、用户名明文。通过用户签名表示唯一性。
 * 
 * @author scott.liang
 * @version 1.0 11/4/2012
 * @since laxcus 1.0
 */
public final class SigerKey implements Classable, Serializable, Cloneable , Comparable<SigerKey> {
	
	private static final long serialVersionUID = 5233909561660134754L;

	/** 用户签名 **/
	private Siger siger;
	
	/** 用户名明文 **/
	private String text;
	
	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(siger);
		writer.writeString(text);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		siger = new Siger(reader);
		text = reader.readString();
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个空的用户签名键
	 */
	public SigerKey() {
		super();
	}

	/**
	 * 根据传入实例生成它的数据副本
	 * @param that 传入实例
	 */
	private SigerKey(SigerKey that) {
		this();
		siger = that.siger;
		text = that.text;
	}

	/**
	 * 构造用户签名键，指定全部参数
	 * @param siger 数字签名人
	 */
	public SigerKey(Siger siger) {
		this();
		setSiger(siger);
	}
	
	/**
	 * 构造用户签名键，指定全部参数
	 * @param siger 数字签名人
	 * @param text 明文
	 */
	public SigerKey(Siger siger, String text) {
		this(siger);
		setText(text);
	}
	
	/**
	 * 从可类化数据读取器中解析用户签名键参数
	 * @param reader 可类化数据读取器
	 */
	public SigerKey(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户签名
	 * @param e 用户签名
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);
		siger = e;
	}
	
	/**
	 * 返回用户签名
	 * @return 用户签名
	 */
	public Siger getSiger() {
		return siger;
	}
	
	/**
	 * 设置用户名明文
	 * @param s 用户名明文
	 */
	public void setText(String s) {
		text = s;
	}

	/**
	 * 返回用户名明文
	 * @return 用户名明文
	 */
	public String getText() {
		return text;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return SigerKey实例
	 */
	public SigerKey duplicate() {
		return new SigerKey(this);
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
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SigerKey.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((SigerKey) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return  siger.hashCode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s %s", siger, text);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SigerKey that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(siger, that.siger);
	}

}