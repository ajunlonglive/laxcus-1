/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.markable.*;

/**
 * 用户登录账号。<br>
 * 账号由两个参数组成：用户名和密码。<br>
 * 
 * <br>
 * 特别注意: <br>
 * <1> 系统所有环节中，用户名采用SHA256算法编码，32个字节；密码采用SHA512编码，64个字节。<br>
 * <2> 用户的账号明文由用户持有，只在客户端（FRONT）由用户输入存在，然后被转换成SHA256码。明文在运行环境不保存，也不会在网络中传输。<br>
 * <3> 账号只能由系统管理员或者等同于管理员身份的用户建立。<br>
 * <4> 用户名是全局唯一的，建立后不能修改。密码可以由账号持有人、系统管理员、等同系统管理员身份的用户修改。<br><br>
 * 
 * 目的：<br>
 * <1> 由于SHA算法逆向破解的难度，即使账号被非法窃取了，窃密者也极难通过逆向方式获得账号明文。（虽然大数据可以加速暴力破解难度 ^_^ ）<br>
 * <2> 登录前因为需要RSA校验，RSA证书是由管理员签发，保证每一个登录用户是基本可信的。<br>
 * <3> 基于此，在账号可信的情况下，用户数据操作安全也能够得保证。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public abstract class SHAUser implements Classable, Markable, Serializable, Cloneable, Comparable<SHAUser> {

	private static final long serialVersionUID = 8563504456902885695L;
	
	/**
	 * 生成用户签名，判断条件：
	 * 1. 如果字符串空，返回空指针
	 * 2. 如果是16进制的SHA256签名（64个字符串明文），生成签名
	 * 3. 以上不成立，认为是明文，对明文签名
	 * @param input 输入参数
	 * @return 返回签名，或者空指针
	 */
	public static Siger doSiger(String input) {
		if (input == null || input.trim().length() == 0) {
			return null;
		}
		// 判断是16进制的SHA256签名（64个字符串），否则认为是明文
		if (Siger.validate(input)) {
			return new Siger(input);
		} else {
			return SHAUser.doUsername(input);
		}
	}

	/**
	 * 非16进制字符串，生成用户名称签名。用户名称字符串需要转为小写字符和UTF8编码，再生成数字签名。
	 * @param input 用户名称文本
	 * @return SHA256散列码的封装格式
	 */
	public static Siger doUsername(String input) {
		// 统一转为小写
		input = input.toLowerCase();
		// UTF8编码后输出
		byte[] b = new UTF8().encode(input);
		// 计算SHA256签名
		SHA256Hash hash = Laxkit.doSHA256Hash(b, 0, b.length);
		return new Siger(hash);
	}

	/**
	 * 生成用户密码签名。用户密码转换为UTF8编码，再生成SHA512的数据签名。
	 * @param input 密码文本
	 * @return SHA512散列码
	 */
	public static SHA512Hash doPassword(String input) {
		// UTF8编码后输出
		byte[] b = new UTF8().encode(input);
		// 计算SHA512签名
		return Laxkit.doSHA512Hash(b, 0, b.length);
	}

	/** 用户名称，SHA256编码 **/
	private Siger username;

	/** 用户密码，SHA512编码 **/
	private SHA512Hash password;

	/**
	 * 将用户登录账号参数输出到可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 用户名和密码
		writer.writeInstance(username);
		writer.writeInstance(password);
		// 构造子类参数
		buildSuffix(writer);
		// 返回写入的字节尺寸
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析用户登录账号参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 用户名和密码
		username = reader.readInstance(Siger.class);
		password = reader.readInstance(SHA512Hash.class);
		// 解析子类参数
		resolveSuffix(reader);
		// 返回读取的字节尺寸
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的SHA账号，生成它的数据副本
	 * @param that SHAUser子类实例
	 */
	protected SHAUser(SHAUser that) {
		this();
		username = that.username.duplicate();
		password = that.password.duplicate();
	}

	/**
	 * 生成一个空的SHA账号
	 */
	protected SHAUser() {
		super();
	}

	/**
	 * 生成SHA账号，指定它的用户名称和密码的明文
	 * @param username 用户名称转为小写后再生成SHA256签名
	 * @param password 密码不考虑大小写，直接生成SHA512签名
	 */
	protected SHAUser(String username, String password) {
		this();
		setTextUsername(username);
		setTextPassword(password);
	}

	/**
	 * 返回SHA256编码的用户名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return username;
	}

	/**
	 * 设置SHA256编码的用户名
	 * @param e Siger实例
	 */
	public void setUsername(Siger e) {
		Laxkit.nullabled(e);
		// 赋值
		username = e;
	}

	/**
	 * 返回SHA512编码的密码
	 * @return SHA512Hash实例
	 */
	public SHA512Hash getPassword() {
		return password;
	}

	/**
	 * 设置SHA512编码的密码
	 * @param e SHA512Hash实例
	 */
	public void setPassword(SHA512Hash e) {
		Laxkit.nullabled(e);
		// 保存密码
		password = e;
	}

	/**
	 * 检查字节数组全0
	 * @param b 字节数组
	 * @return 返回真或者假
	 */
	private boolean isZero(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			if (b[i++] != 0) return false;
		}
		return true;
	}

	/**
	 * 设置用户名
	 * 
	 * @param b 用户名的字节数组
	 */
	protected void setUsername(byte[] b) {
		username = new Siger(b);
	}

	/**
	 * 设置密码
	 * 
	 * @param b 密码的字节数组
	 */
	protected void setPassword(byte[] b) {
		password = new SHA512Hash(b);
	}

	/**
	 * 设置用户名(转化为小写后再生成SHA256码)
	 * @param text 明文
	 */
	public void setTextUsername(String text) {
		username = SHAUser.doUsername(text);
	}

	/**
	 * 设置密码(生成SHA512码保存)
	 * @param text 明文
	 */
	public void setTextPassword(String text) {
		password = SHAUser.doPassword(text);
	}

	/**
	 * 设置用户名(16进制字符串)
	 * @param hex 16进制字符串
	 */
	public void setHexUsername(String hex) {
		username = new Siger(hex);
	}

	/**
	 * 返回16进制的字符串
	 * @return 16进制字符串
	 */
	public String getHexUsername() {
		return username.getHex();
	}

	/**
	 * 设置密码(16进制字符串)
	 * @param hex 16进制字符串
	 */
	public void setHexPassword(String hex) {
		password = new SHA512Hash(hex);
	}

	/**
	 * 返回16进制的字符串
	 * @return 16进制字符串
	 */
	public String getHexPassword() {
		return password.getHexText();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || !(that instanceof SHAUser)) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((SHAUser) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return username.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isZero(password.get())) {
			return getHexUsername();
		} else {
			return String.format("%s:%s", getHexUsername(), getHexPassword());
		}
	}

	/*
	 * 根据用户名和密码，比较两个账号的排序位置
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SHAUser that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		// 比较用户名和密码
		int ret = Laxkit.compareTo(username, that.username);
		if (ret == 0) {
			ret = Laxkit.compareTo(password, that.password);
		}
		return ret;
	}

	/**
	 * 从子类中克隆它的实例副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 由子类实例实现，生成当时自己的数据副本
	 * @return SHA256子类实例
	 */
	public abstract SHAUser duplicate();

	/**
	 * 将子类参数写入可类化存储器，返回写入的字节长度
	 * @param writer 可类化存储器
	 * @since 1.1
	 */
	public abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化读取器中解析子类参数，返回读取的字节长度
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public abstract void resolveSuffix(ClassReader reader) ;

}