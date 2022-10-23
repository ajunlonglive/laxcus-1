/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.secure;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 客户机密钥 <br><br>
 * 
 * 包括加密类型和RSA公钥，RSA公钥允许空指针
 * 
 * @author scott.liang
 * @version 1.0 2/10/2021
 * @since laxcus 1.0
 */
public class PublicSecure implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = 662601900640319860L;

	/** 加密类型 **/
	private int family;

	/** RSA公钥 **/
	private PublicStripe stripe;

	/**
	 * 构造客户机密钥
	 */
	private PublicSecure() {
		super();
	}

	/**
	 * 生成客户机密钥副本
	 * @param that 客户机密钥
	 */
	private PublicSecure(PublicSecure that) {
		this();
		family = that.family;
		stripe = that.stripe;
	}

	/**
	 * 从可类化读取器解析客户机密钥
	 * @param reader 可类化读取器
	 */
	public PublicSecure(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从字节数组解析客户机密钥
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 长度
	 */
	public PublicSecure(byte[] b, int off, int len) {
		this(new ClassReader(b, off, len));
	}

	/**
	 * 从字节数组解析客户机密钥
	 * @param b 字节数组
	 */
	public PublicSecure(byte[] b) {
		this(b, 0, b.length);
	}

	/**
	 * 构造客户机密钥，指定加密类型
	 * @param family 加密类型
	 */
	public PublicSecure(int family) {
		this();
		setFamily(family);
	}

	/**
	 * 构造客户机密钥，指定加密类型和RSA公钥
	 * @param family 加密类型
	 * @param stripe RSA公钥
	 */
	public PublicSecure(int family, PublicStripe stripe) {
		this(family);
		setStripe(stripe);
	}

	/**
	 * 设置类型类型
	 * @param who 加密类型
	 */
	public void setFamily(int who) {
		if (!SecureType.isFamily(who)) {
			throw new IllegalValueException("illegal value:%d", who);
		}
		family = who;
	}

	/**
	 * 返回加密类型
	 * @return 加密类型
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 设置RSA公钥，允许空指针
	 * @param e RSA公钥
	 */
	public void setStripe(PublicStripe e) {
		stripe = e;
	}

	/**
	 * 返回RSA公钥
	 * @return 对象或者空指针
	 */
	public PublicStripe getStripe() {
		return stripe;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return family ^ stripe.hashCode();
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
	public PublicSecure duplicate() {
		return new PublicSecure(this);
	}
	
	/**
	 * 输出字节数组
	 * @return byte数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeInt(family);
		boolean success = (stripe != null);
		writer.writeBoolean(success);
		// 有效，保存对象！
		if (success) {
			writer.writeObject(stripe);
		}
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		family = reader.readInt();
		// 判断对象有效或者否
		boolean success = reader.readBoolean();
		if (success) {
			stripe = new PublicStripe(reader);
		} else {
			stripe = null;
		}
		// 返回读取的数据长度
		return reader.getSeek() - seek;
	}

}