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
import com.laxcus.util.net.*;

/**
 * RSA密钥令牌关联的IP地址范围。
 * 
 * @author scott.liang
 * @version 1.0 11/19/2016
 * @since laxcus 1.0
 */
public final class SecureRange implements Classable, Cloneable, Serializable, Comparable<SecureRange> {

	private static final long serialVersionUID = -7582491864265144444L;

	/** 开始地址 **/
	private Address begin;

	/** 结束地址 **/
	private Address end;
	
	/**
	 * 构造默认的网络地址范围
	 */
	private SecureRange() {
		super();
	}

	/**
	 * 生成网络地址范围的数据副本
	 * @param that 网络地址范围
	 */
	private SecureRange(SecureRange that) {
		this();
		begin = that.begin.duplicate();
		end = that.end.duplicate();
	}

	/**
	 * 构造一个网络地址范围，包括它的开始和结束段。
	 * @param b 开始地址
	 * @param e 结束地址
	 */
	public SecureRange(Address b, Address e) {
		this();
		set(b, e);
	}

	/**
	 * 从可类化读取器中解析地址范围
	 * @param reader 可类化数据读取器
	 */
	public SecureRange(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 返回地址开始位置
	 * @return Address实例
	 */
	public Address begin() {
		return begin;
	}

	/**
	 * 返回地址结束位置
	 * @return Address实例
	 */
	public Address end() {
		return end;
	}

	/**
	 * 设置地址范围
	 * @param b 开始地址
	 * @param e 结束地址
	 */
	public void set(Address b, Address e) {
		if (b.compareTo(e) > 0) {
			throw new IllegalValueException("%s > %s", b, e);
		}
		begin = b.duplicate();
		end = e.duplicate();
	}

	/**
	 * 判断一个地址是否在指定范围内
	 * @param that 被比较的地址
	 * @return 返回真或者假
	 */
	public boolean contains(Address that) {
		return begin.compareTo(that) <= 0 && that.compareTo(end) <= 0;
	}

	/**
	 * 生成当前安全地址范围实例的数据副本
	 * @return 返回SecureRange实例
	 */
	public SecureRange duplicate() {
		return new SecureRange(this);
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
		if (that == null || that.getClass() != SecureRange.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SecureRange) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return begin.hashCode() ^ end.hashCode();
	}

	/**
	 * 比较两个地址的排列位置
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SecureRange that) {
		if(that == null) {
			return 1;
		}
		int ret = begin.compareTo(that.begin);
		if (ret == 0) {
			ret = end.compareTo(that.end);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s,%s", begin, end);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(begin);
		writer.writeObject(end);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		begin = new Address(reader);
		end = new Address(reader);
		return reader.getSeek() - seek;
	}


	//	public static void main(String[] args) {
	//		String s1 = "192.168.12.1";
	//		String s2 = "192.168.12.100";
	//		s1 = "1080:0:0:0:8:800:200C:4101";
	//		s2 = "1080:0:0:0:8:800:200C:4110";
	//		java.util.Set<Address> set = new java.util.TreeSet<Address>();
	//		
	//		try {
	//			Address b = new Address(s1);
	//			Address e = new Address(s2);
	//			System.out.printf("result is:%d\n", b.compareTo(e));
	//			AddressRange r = new AddressRange(b, e);
	//			System.out.println(r);
	//			
	//			set.add(b);
	//			set.add(e);
	////			set.add(null);
	//			System.out.printf("size is:%d\n", set.size());
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//		System.out.println("okay!");
	//	}

}