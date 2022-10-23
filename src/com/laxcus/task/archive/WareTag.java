/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.archive;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 分布任务组件标签。<br><br>
 * 
 * 组件标签由以下参数组成。<br>
 * 1. 软件命名（系统内部调用使用） <br>
 * 2. 版本号 <br>
 * 3. 软件名称（显示给用户） <br>
 * 4. 生产者（软件制造商） <br>
 * 5. 软件介绍 <br>
 * 6. 是否自有（不公开发布） <br>
 * 7. 软件发布日期 <br><br>
 * 
 * 
 * 软件名称和版本号标志软件的唯一性。<br>
 * 
 * @author scott.liang
 * @version 1.0 10/03/2015
 * @since laxcus 1.0
 */
public final class WareTag implements Classable, Serializable, Cloneable, Comparable<WareTag> {

	private static final long serialVersionUID = -6859572178336842601L;

	/** 自有软件包 **/
	public final static Naming own = new Naming("own");

	/** 软件命名，在系统内部，为分布计算使用 **/
	private Naming naming;

	/** 软件版本号 **/
	private WareVersion version;

	/** 产品名称，针对用户可以理解的名字 **/
	private String productName;
	
	/** 产品日期 **/
	private int productDate;

	/** 生产商 **/
	private String maker;

	/** 介绍 **/
	private String comment;
	
	/** 别名 **/
	private String alias;

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeObject(naming);
		writer.writeObject(version);
		writer.writeString(productName);
		writer.writeInt(productDate);
		writer.writeString(maker);
		writer.writeString(comment);
		writer.writeString(alias);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		naming = new Naming(reader);
		version = new WareVersion(reader);
		productName = reader.readString();
		productDate = reader.readInt();
		maker = reader.readString();
		comment = reader.readString();
		alias = reader.readString();
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的组件标签，生成它的数据副本
	 * @param that WareTag实例
	 */
	private WareTag(WareTag that) {
		super();
		naming = that.naming;
		version = that.version;
		productName = that.productName;
		productDate = that.productDate;
		maker = that.maker;
		comment = that.comment;
		alias = that.alias;
	}

	/**
	 * 构造默认和私有的组件标签。
	 */
	private WareTag() {
		super();
	}

	/**
	 * 构造组件标签，指定全部参数
	 * @param name 发布者的软件名称
	 * @param version 软件版本号
	 */
	public WareTag(Naming name, WareVersion version) {
		this();
		setNaming(name);
		setVersion(version);
	}

	/**
	 * 构造组件标签，指定全部参数
	 * @param name 软件名称
	 * @param version 软件版本号
	 */
	public WareTag(String name, WareVersion version) {
		this(new Naming(name), version);
	}

	/**
	 * 构造组件标签，指定全部参数
	 * @param version 软件版本号
	 * @param name 发布者的软件名称
	 */
	public WareTag(WareVersion version, Naming name) {
		this();
		setNaming(name);
		setVersion(version);
	}

	/**
	 * 构造组件标签，指定全部参数
	 * @param version 软件版本号
	 * @param name 软件名称
	 */
	public WareTag(WareVersion version, String name) {
		this(new Naming(name), version);
	}

	/**
	 * 从可类化数据读取器中解析组件标签
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public WareTag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回软件命名
	 * @return Naming实例
	 */
	public Naming getNaming() {
		return naming ;
	}

	/**
	 * 返回软件版本号，不允许空指针
	 * @return 软件版本号
	 */
	public WareVersion getVersion() {
		return version;
	}

	/**
	 * 设置软件命名，不允许空值
	 * @param e Naming实例
	 */
	public void setNaming(Naming e) {
		Laxkit.nullabled(e);
		naming = e;
	}

	/**
	 * 返回软件命名的文件描述
	 * @return 字符串
	 */
	public String getNamingText() {
		return naming.toString();
	}

	/**
	 * 判断是系统级组件
	 * @return 返回真或者假
	 */
	public boolean isSystemLevel() {
		return Laxkit.compareTo(Sock.SYSTEM_WARE, naming) == 0;
	}

	/**
	 * 判断是用户级组件
	 * @return 返回真或者假
	 */
	public boolean isUserLevel() {
		return !isSystemLevel();
	}

	/**
	 * 设置软件版本号，不允许空指针
	 * @param e 软件版本号
	 */
	public void setVersion(WareVersion e) {
		Laxkit.nullabled(e);
		version = e;
	}

	/**
	 * 设置软件产品名称
	 * @param s 字符串
	 */
	public void setProductName(String s) {
		productName = s;
	}

	/**
	 * 返回软件产品名称
	 * @return 字符串
	 */
	public String getProductName() {
		return productName;
	}
	
	/**
	 * 设置软件产品生产日期
	 * @param value 日期值
	 */
	public void setProductDate(int value) {
		productDate = value;
	}
	
	/**
	 * 返回软件产品生产日期
	 * @return 日期
	 */
	public int getProductDate() {
		return productDate ;
	}

	/**
	 * 返回软件生产者名称
	 * @return 字符串
	 */
	public String getMaker() {
		return maker ;
	}

	/**
	 * 设置软件生产者名称
	 * @param e 字符串
	 */
	public void setMaker(String e) {
		maker = e;
	}

	/**
	 * 返回软件简单介绍
	 * @return 字符串
	 */
	public String getComment() {
		return comment ;
	}

	/**
	 * 设置软件简单介绍
	 * @param e 字符串
	 */
	public void setComment(String e) {
		comment = e;
	}

	/**
	 * 返回软件别名（由用户定义）
	 * @return 字符串
	 */
	public String getAlias() {
		return alias ;
	}

	/**
	 * 设置软件别名
	 * @param s 字符串
	 */
	public void setSelfly(String s) {
		alias = s;
	}

	/**
	 * 生成当前实例副本
	 * @return WareTag实例
	 */
	public WareTag duplicate() {
		return new WareTag(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != WareTag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((WareTag) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return naming.hashCode() ^ version.hashCode();
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
	public int compareTo(WareTag that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(naming, that.naming);
		if (ret == 0) {
			ret = Laxkit.compareTo(version, that.version);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String s = String.format("%s %s", naming, version);
		if (productName != null) {
			s = String.format("%s %s", s, productName);
		}
		if (maker != null) {
			s = String.format("%s %s", s, maker);
		}
		return s;
	}

	//	public byte[] build() {
	//		ClassWriter writer = new ClassWriter();
	//		build(writer);
	//		return writer.effuse();
	//	}
	//	
	//	public void test1() {
	//		String hex = "89e495e7941cf9e40e6980d14a16bf023ccd4c92";
	//		Naming hash = new Naming(hex);
	//		WareTag part = new WareTag(hash, PhaseTag.INIT);
	//		byte[] b = part.build();
	//		System.out.printf("byte size is %d\n",b.length);
	//		
	//		ClassReader reader = new ClassReader(b);
	//		WareTag e  = new WareTag(reader);
	//		System.out.printf("read seek is:%d, left:%d, used:%d, length:%d\n",
	//				reader.getSeek(), reader.getLeft(), reader.getUsed(), reader.getLength());
	//		System.out.printf("%s - %s\n", part, e);
	//	}
	//	
	//	public void test2() {
	////		String hex = "89e495e7941cf9e40e6980d14a16bf023ccd4c91";
	////		Naming hash = new Naming(hex);
	//		WareTag part = new WareTag(null, PhaseTag.INIT);
	//		byte[] b = part.build();
	//		System.out.printf("\n\nbyte size is %d\n",b.length);
	//		
	//		ClassReader reader = new ClassReader(b);
	//		WareTag e  = new WareTag(reader);
	//		System.out.printf("read seek is:%d, left:%d, used:%d, length:%d\n",
	//				reader.getSeek(), reader.getLeft(), reader.getUsed(), reader.getLength());
	//
	//		System.out.printf("%s - %s\n", part, e);
	//	}
	//	
	//	public void test3() {
	//		String hex = "89e495e7941cf9e40e6980d14a16bf023ccd4c92";
	//		Naming hash = new Naming(hex);
	//		WareTag part =  new WareTag(hash, PhaseTag.INIT);
	//		
	////		part = null;
	//
	//		ClassWriter writer = new ClassWriter();
	//		writer.writeDefault(part);
	//		byte[] b = writer.effuse();
	//		System.out.printf("byte length:%d\n", b.length);
	//		
	//		ClassReader reader = new ClassReader(b);
	//		WareTag e = (WareTag)reader.readDefault();
	//		System.out.printf("read seek is:%d, left:%d, used:%d, length:%d\n",
	//				reader.getSeek(), reader.getLeft(), reader.getUsed(), reader.getLength());
	//
	//		System.out.printf("%s - %s\n", part, e);
	//	}
	//	
	//	public void test4() {
	//		com.laxcus.access.column.attribute.Packing part = new com.laxcus.access.column.attribute.Packing(
	//			com.laxcus.access.column.attribute.PackingTag.GZIP,  com.laxcus.access.column.attribute.PackingTag.DES, "Pentium".getBytes()	);
	//		
	//		ClassWriter writer = new ClassWriter();
	//		writer.writeInstance(part);
	//		byte[] b = writer.effuse();
	//		System.out.printf("byte length:%d\n", b.length);
	//		
	//		ClassReader reader = new ClassReader(b);
	////		com.laxcus.access.column.attribute.Packing  e =  (com.laxcus.access.column.attribute.Packing)reader.readDefault();
	//		
	//		com.laxcus.access.column.attribute.Packing  e =  reader.readInstance(com.laxcus.access.column.attribute.Packing.class);
	//		System.out.printf("read seek is:%d, left:%d, used:%d, length:%d\n",
	//				reader.getSeek(), reader.getLeft(), reader.getUsed(), reader.getLength());
	//
	//		System.out.printf("%s - %s\n", part, e);
	//
	//	}
	//
	//	public static void main(String[] args) {
	//		WareTag e = new WareTag();
	////		e.test1();
	////		e.test2();
	////		e.test3();
	//		e.test4();
	//	}

}