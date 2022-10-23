/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.secure;

import java.io.*;
import java.util.*;

import com.laxcus.fixp.secure.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.net.*;

/**
 * 密钥令牌信息
 * 
 * @author scott.liang
 * @version 1.0 2/11/2021
 * @since laxcus 1.0
 */
public final class SecureTokenSlice implements Serializable, Cloneable, Classable, Comparable<SecureTokenSlice> {

	private static final long serialVersionUID = -6644296095141805348L;

	/** 令牌名称 **/
	private Naming name;
	
	/** CHECK类型 **/
	private int family;
	
	/** 处理模式 **/
	private int mode;
	
	/** 节点范围 **/
	private TreeSet<SecureRange> ranges = new TreeSet<SecureRange>();
	
	/** RSA私钥 **/
	private PrivateStripe privateStripe;
	
	/** RSA私钥 **/
	private PublicStripe publicStripe;

	/**
	 * 构造默认密钥令牌信息
	 */
	public SecureTokenSlice() {
		super();
		name = null;
		family = -1;
		mode = -1;
	}

	/**
	 * 构造密钥令牌信息
	 * @param naming 命名
	 * @param family 检查类型
	 * @param mode 检查模型
	 */
	public SecureTokenSlice(Naming naming, int family, int mode) {
		this();
		setName(naming);
		setFamily(family);
		setMode(mode);
	}
	
	/**
	 * 生成密钥令牌信息数据副本
	 * @param that ReloadSecureItem实例
	 */
	private SecureTokenSlice(SecureTokenSlice that) {
		this();
		name = that.name;
		family = that.family;
		mode = that.mode;
		ranges.addAll(that.ranges);
		privateStripe = that.privateStripe;
		publicStripe = that.publicStripe;
	}

	/**
	 * 从可类化数据读取器解析密钥令牌信息
	 * @param reader 可类化数据读取器
	 */
	public SecureTokenSlice(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置令牌名称
	 * @param e Naming实例
	 */
	public void setName(Naming e) {
		Laxkit.nullabled(e);

		name = e;
	}
	
	/**
	 * 设置密钥令牌名称
	 * @param str 字符串
	 */
	public void setName(String str) {
		setName(new Naming(str));
	}

	/**
	 * 返回令牌名称
	 * @return Naming实例
	 */
	public Naming getName() {
		return name;
	}

	/**
	 * 设置RSA私钥
	 * @param e
	 */
	public void setPrivateStripe(PrivateStripe e) {
		Laxkit.nullabled(e);
		privateStripe = e;
	}

	/**
	 * 返回RSA私钥
	 * @return
	 */
	public PrivateStripe getPrivateStripe() {
		return privateStripe;
	}

	/**
	 * 设置RSA公钥
	 * @param e
	 */
	public void setPublicStripe(PublicStripe e) {
		Laxkit.nullabled(e);
		publicStripe = e;
	}

	/**
	 * 返回RSA公钥
	 * @return
	 */
	public PublicStripe getPublicStripe() {
		return publicStripe;
	}
	
	/**
	 * 保存一个地址范围
	 * @param begin 开始位置
	 * @param end 结束位置
	 */
	public boolean addRange(Address begin, Address end) {
		if (begin.compareTo(end) > 0) {
			throw new IllegalValueException("%s > %s", begin, end);
		}
		return addRange(new SecureRange(begin, end));
	}

	/**
	 * 保存一个地址范围
	 * @param e 地址范围实例
	 * @return 成功返回真，否则假
	 */
	public boolean addRange(SecureRange e) {
		Laxkit.nullabled(e);
		// 保存
		return ranges.add(e);
	}

	/**
	 * 保存一组地址
	 * @param a 地址数组
	 * @return 新增成员数目
	 */
	public int addRanges(Collection<SecureRange> a) {
		int size = ranges.size();
		for (SecureRange e : a) {
			addRange(e);
		}
		return ranges.size() - size;
	}

	/**
	 * 返回地址范围
	 * @return SecureRange列表
	 */
	public List<SecureRange> getRanges() {
		return new ArrayList<SecureRange>(ranges);
	}
	
	/**
	 * 设置安全检查类型
	 * @param who 安全检查类型
	 */
	public void setFamily(int who) {
		if (!SecureType.isFamily(who)) {
			throw new IllegalValueException("illegal value:%d", who);
		}
		family = who;
	}

	/**
	 * 返回安全检查类型
	 * @return 安全检查类型
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 判断无校验
	 * @return 返回真或者假
	 */
	public boolean isNone() {
		return SecureType.isNone(family);
	}

	/**
	 * 判断是地址校验
	 * @return 返回真或者假
	 */
	public boolean isAddress() {
		return SecureType.isAddress(family);
	}

	/**
	 * 判断是密文校验
	 * @return 返回真或者假
	 */
	public boolean isCipher() {
		return SecureType.isCipher(family);
	}

	/**
	 * 判断是地址/密文双重校验
	 * @return 返回真或者假
	 */
	public boolean isDuplex() {
		return SecureType.isDuplex(family);
	}

	/**
	 * 设置安全管理模式
	 * @param who 安全管理模式
	 */
	public void setMode(int who) {
		if (!SecureMode.isMode(who)) {
			throw new IllegalValueException("illegal secure mode:%d", who);
		}
		mode = who;
	}

	/**
	 * 返回安全管理模式
	 * @return 安全管理模式
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * 判断是公用安全管理模式
	 * @return 返回真或者假
	 */
	public boolean isCommon() {
		return SecureMode.isCommon(mode);
	}

	/**
	 * 判断是特用安全管理模式
	 * @return 返回真或者假
	 */
	public boolean isSpecial() {
		return SecureMode.isSpecial(mode);
	}


	/**
	 * 产生数据副本
	 * @return ReloadSecureItem实例
	 */
	public SecureTokenSlice duplicate() {
		return new SecureTokenSlice(this);
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
		if (that == null || that.getClass() != SecureTokenSlice.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((SecureTokenSlice) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SecureTokenSlice that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(name, that.name);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(name);
		writer.writeInt(family);
		writer.writeInt(mode);
		// 范围
		writer.writeInt(ranges.size());
		for (SecureRange e : ranges) {
			writer.writeObject(e);
		}
		// 密钥
		writer.writeObject(privateStripe);
		writer.writeObject(publicStripe);

		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 参数
		name = new Naming(reader);
		family = reader.readInt();
		mode = reader.readInt();
		// 地址范围
		int elements = reader.readInt();
		for (int i = 0; i < elements; i++) {
			SecureRange e = new SecureRange(reader);
			ranges.add(e);
		}
		// 密钥
		privateStripe = new PrivateStripe(reader);
		publicStripe = new PublicStripe(reader);

		return reader.getSeek() - seek;
	}

}
