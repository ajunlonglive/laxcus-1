/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.archive;

import java.io.*;
import java.util.regex.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 软件版本号。<br><br>
 * 
 * 由版本号和副版本号组成。<br>
 * 版本号必须是正整数，正副版本号都在10个数字内
 * 
 * @author scott.liang
 * @version 1.0 5/10/2020
 * @since laxcus 1.0
 */
public final class WareVersion implements Classable, Serializable, Cloneable, Comparable<WareVersion> {
	
	private static final long serialVersionUID = 1364639800017130973L;

	/** 版本号的浮点数正则表达式 **/
	private final static String REGEX1 = "^\\s*([0-9]{1,5}\\.[0-9]{1,12})\\s*$";
	
	/** 版本号正整数表达式 **/
	private final static String REGEX2 = "^\\s*([0-9]{1,5})\\s*$";

	/** 版本号。 **/
	private double version;

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 版本号
		writer.writeDouble(version);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 版本号
		version = reader.readDouble();
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入参数，生成它的数据副本
	 * @param that WareVersion实例
	 */
	private WareVersion(WareVersion that) {
		super();
		version = that.version;
	}

	/**
	 * 构造默认和私有的软件版本号
	 */
	private WareVersion() {
		super();
		version = 0.0f;
	}

	/**
	 * 构造软件版本号。指定版本号，副版本号默认是0。
	 * @param version 版本号
	 */
	public WareVersion(double version) {
		this();
		setVersion(version);
	}

	/**
	 * 构造软件版本号，解析传入的字符
	 * @param input
	 */
	public WareVersion(String input) {
		this();
		split(input);
	}
	
	/**
	 * 从可类化数据读取器中解析软件版本号
	 * @param reader 可类化数据读取器
	 */
	public WareVersion(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置版本号
	 * @param i 版本号
	 */
	public void setVersion(double i) {
		version = i;
	}

	/**
	 * 返回版本号
	 * @return 版本号
	 */
	public double getVersion() {
		return version;
	}

	/**
	 * 解析参数
	 * @param input 版本号字符串
	 */
	private void split(String input) {
		// 浮点数值
		Pattern pattern = Pattern.compile(WareVersion.REGEX1);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			// 解析版本号
			version = Double.parseDouble(matcher.group(1));
			return;
		}

		// 整数值
		pattern = Pattern.compile(WareVersion.REGEX2);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			// 解析版本号
			version = Long.parseLong(matcher.group(1));
			return;
		}

		// 不成功，弹出异常
		throw new IllegalValueException("illegal " + input);
	}

	/**
	 * 生成当前实例的数据副本
	 * @return WareVersion实例
	 */
	public WareVersion duplicate() {
		return new WareVersion(this);
	}

	/**
	 * 检查两个软件版本号一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != WareVersion.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((WareVersion) that) == 0;
	}

	/**
	 * 返回软件版本号的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new Double(version).hashCode();
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
	 * 返回软件版本号的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%.5f", version);
	}

	/**
	 * 比较两个软件版本号相同
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(WareVersion that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(version, that.version);
	}
	
	/**
	 * 判断是有效的版本号
	 * @param input 版本号
	 * @return 匹配返回“真”，否则“假”。
	 */
	public static boolean validate(String input) {
		if (input != null) {
			return input.matches(WareVersion.REGEX1)
					|| input.matches(WareVersion.REGEX2);
		}
		return false;
	}

}