/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.manage;

import java.io.*;
import java.util.regex.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 应用软件标记。<br><br>
 * 
 * 由启动类路径名和软件包散列码组成，具有唯一性！
 * 
 * @author scott.liang
 * @version 1.0 8/3/2021
 * @since laxcus 1.0
 */
public final class WKey implements Classable, Cloneable, Serializable, Comparable<WKey> {

	private static final long serialVersionUID = -5530416825000746507L;

	/** 启动类 **/
	private String className;

	/** 软件包内容签名 **/
	private SHA256Hash hash;

	/**
	 * 构造默认和私有应用软件标记
	 */
	private WKey() {
		super();
	}

	/**
	 * 根据传入的应用软件标记，生成它的数据副本
	 * @param that 应用软件标记
	 */
	private WKey(WKey that) {
		this();
		className = that.className;
		hash = that.hash.duplicate();
	}

	/**
	 * 构造应用软件标记，指定参数
	 * @param className 启动类路径
	 * @param hash 内容签名
	 */
	public WKey(String className) {
		this();
		setClassName(className);
	}

	/**
	 * 构造应用软件标记，指定参数
	 * @param className 启动类路径
	 * @param hash 内容签名
	 */
	public WKey(String className, SHA256Hash hash) {
		this();
		setClassName(className);
		setHash(hash);
	}

	/**
	 * 从可类化数据读取器中解析应用软件标记
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public WKey(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置分布任务组件启动类路径，不允许空指针
	 * @param e String实例
	 */
	public void setClassName(String e) {
		Laxkit.nullabled(e);

		className = e;
	}

	/**
	 * 返回分布任务组件启动类路径
	 * @return String实例
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * 设置内容签名
	 * @param e SHA256散列码
	 */
	public void setHash(SHA256Hash e) {
		Laxkit.nullabled(e);

		hash = e;
	}

	/**
	 * 返回内容签名
	 * @return SHA256散列码
	 */
	public SHA256Hash getHash() {
		return hash;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return WKey实例
	 */
	public WKey duplicate() {
		return new WKey(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != WKey.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((WKey) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return className.hashCode() ^ hash.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s{%s}", className, hash);
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
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(WKey that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(className, that.className);
		if (ret == 0) {
			ret = Laxkit.compareTo(hash, that.hash);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeString(className);
		writer.writeObject(hash);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		className = reader.readString();
		hash = new SHA256Hash(reader);
		return reader.getSeek() - seek;
	}
	
	final static String REGEX = "^\\s*([\\w\\W&&[^\\s]]+)\\{([0-9a-fA-F]{64})\\}\\s*$";
	
	/**
	 * 判断有效
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public static boolean validate(String input) {
		boolean success = (input != null);
		if (success) {
			success = input.matches(WKey.REGEX); 
		}
		return success;
	}
	
	/**
	 * 解析参数
	 * @param input
	 * @return
	 */
	public static WKey translate(String input) {
		if (!WKey.validate(input)) {
			return null;
		}

		Pattern pattern = Pattern.compile(WKey.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			return null;
		}

		// 类名和SHA256散列码
		String s1 = matcher.group(1);
		String s2 = matcher.group(2);
		// 不匹配，返回假
		if (!SHA256Hash.validate(s2)) {
			return null;
		}
		SHA256Hash hash = new SHA256Hash(s2);
		return new WKey(s1, hash);

	}

}