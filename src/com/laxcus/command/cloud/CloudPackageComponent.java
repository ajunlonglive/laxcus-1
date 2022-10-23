/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 云计算应用包 <br><br>
 * 
 * 包含分文件名、数据内容签名、文件字节数组。<br>
 * FRONT节点根据用户要求，通过GATE节点传递给ACCOUNT节点，再分发给CALL/DATA/WORK/BUILD节点。
 * 
 * @author scott.liang
 * @version 1.1 3/22/2020
 * @since laxcus 1.0
 */
public final class CloudPackageComponent implements Classable, Cloneable, Serializable, Comparable<CloudPackageComponent> { 

	private static final long serialVersionUID = -1613785855785059547L;

	/** 文件名，只是文件名称，不包含路径 **/
	private String name;

	/** 内容签名 **/
	private MD5Hash sign;

	/** 字节内容 **/
	private byte[] content;
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		name = null;
		sign = null;
		content = null;
	}

	/**
	 * 根据传入的云计算应用包，生成它的副本
	 * @param that 云计算应用包
	 */
	private CloudPackageComponent(CloudPackageComponent that) {
		this();
		name = that.name;
		sign = that.sign;
		content = that.content;
	}

	/**
	 * 构造一个默认的云计算应用包
	 */
	private CloudPackageComponent() {
		super();
	}
	
	/**
	 * 构造云计算应用包，指定参数
	 * @param name 文件名
	 * @param sign MD5签名
	 */
	public CloudPackageComponent(String name) {
		this();
		setName(name);
	}

	/**
	 * 构造云计算应用包，指定参数
	 * @param name 文件名
	 * @param sign MD5签名
	 * @param b 数据内容
	 */
	public CloudPackageComponent(String name, byte[] b) {
		this(name);
		setContent(b);
	}

	/**
	 * 从可类化数据读取器中解析云计算应用包参数
	 * @param reader 可类化数据读取器
	 */
	public CloudPackageComponent(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从解析云计算应用包参数
	 * @param b 字节数组
	 */
	public CloudPackageComponent(byte[] b) {
		this(new ClassReader(b));
	}

	/**
	 * 设置文件名，只是文件名本身
	 * @param e String实例
	 */
	public void setName(String e) {
		Laxkit.nullabled(e);
		name = e;
	}

	/**
	 * 返回文件名
	 * @return String实例
	 */
	public String getName() {
		return name;
	}

	/**
	 * 返回内容签名
	 * @return MD5散列码
	 */
	public MD5Hash getSign() {
		return sign;
	}
	
	/**
	 * 设置DTC文件内容
	 * @param b 字节数组
	 */
	public void setContent(byte[] b) {
		content = b;
		if (content != null) {
			sign = Laxkit.doMD5Hash(content);
		} else {
			sign = null;
		}
	}

	/**
	 * 返回DTC文件内容
	 * @return 字节数组
	 */
	public byte[] getContent() {
		return content;
	}
	
	/**
	 * 判断签名一致
	 * @return 返回真或者假
	 */
	public boolean confirm() {
		// 生成内容签名
		MD5Hash e = null;
		if (content != null) {
			e = Laxkit.doMD5Hash(content);
		}
		return (e != null && Laxkit.compareTo(e, sign) == 0);
	}

	/**
	 * 生成当前实例的一个数据副本
	 * @return CloudPackageComponent实例
	 */
	public CloudPackageComponent duplicate() {
		return new CloudPackageComponent(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (sign != null) {
			return String.format("%s#%s", sign, name);
		}
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (sign != null) {
			return sign.hashCode() ^ name.hashCode();
		}
		return name.hashCode();
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
		if (that == null || that.getClass() != CloudPackageComponent.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((CloudPackageComponent) that) == 0;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CloudPackageComponent that) {
		if (that == null) {
			return 1;
		}
		int ret = (Laxkit.compareTo(sign, that.sign));
		if (ret == 0) {
			ret = Laxkit.compareTo(name, that.name);
		}
		return ret;
	}

	/**
	 * 输出字节数组
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 写入参数
		writer.writeString(name);
		writer.writeInstance(sign);
		writer.writeByteArray(content);
		// 返回写入的数据长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 解析参数
		name = reader.readString();
		sign = reader.readInstance(MD5Hash.class);
		content = reader.readByteArray();
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

}