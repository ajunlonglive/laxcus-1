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
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;

/**
 * 分布任务组件的执行部件。<br><br>
 * 
 * 执行部件由两个参数组成：用户签名和阶段类型。<br>
 * 通过执行部件，可以区分一个账号下不同阶段类型的分布任务组件。
 * “阶段类型”是必选参数，“用户签名”是可选参数。如果用户名没有设置，即是系统级别（被集群公用），否则属于用户级别。
 * 
 * @author scott.liang
 * @version 1.1 10/03/2015
 * @since laxcus 1.0
 */
public final class TaskPart implements Classable, Serializable, Cloneable, Comparable<TaskPart> {

	private static final long serialVersionUID = 399934718641641572L;

	/** 用户签名 **/
	private Siger issuer;

	/** 阶段类型 **/
	private int family;

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeInstance(issuer);
		writer.writeInt(family);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		issuer = reader.readInstance(Siger.class);
		family = reader.readInt();
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的执行部件，生成它的数据副本
	 * @param that TaskPart实例
	 */
	private TaskPart(TaskPart that) {
		super();
		issuer = that.issuer;
		family = that.family;
	}

	/**
	 * 构造默认和私有的执行部件。
	 */
	private TaskPart() {
		super();
	}

	/**
	 * 构造执行部件，指定全部参数
	 * @param issuer 发布者的用户签名
	 * @param family 阶段类型
	 */
	public TaskPart(Siger issuer, int family) {
		this();
		setIssuer(issuer);
		setFamily(family);
	}

	/**
	 * 构造执行部件，指定全部参数
	 * @param issuer 发布者的SHA256签名
	 * @param family 阶段类型
	 */
	public TaskPart(SHA256Hash issuer, int family) {
		this(new Siger(issuer), family);
	}
	
	/**
	 * 构造执行部件，指定全部参数
	 * @param family 阶段类型
	 * @param issuer 发布者的用户签名
	 */
	public TaskPart(int family, Siger issuer) {
		this();
		setIssuer(issuer);
		setFamily(family);
	}
	
	/**
	 * 构造系统级执行部件，指定阶段类型
	 * @param family 阶段类型
	 */
	public TaskPart(int family) {
		this();
		setIssuer(null);
		setFamily(family);
	}

	/**
	 * 从可类化数据读取器中解析执行部件
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TaskPart(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return issuer ;
	}

	/**
	 * 判断是系统级阶段命名。没有签名是系统组件。
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public boolean isSystemLevel() {
		return issuer == null;
	}

	/**
	 * 判断是用户级阶段命名。有签名是用户组件！
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public boolean isUserLevel() {
		return issuer != null;
	}

	/**
	 * 返回阶段类型，见PhaseTag中定义
	 * @return 阶段类型
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 设置用户签名，允许空值
	 * @param e Siger实例
	 */
	public void setIssuer(Siger e) {
		issuer = e;
	}

	/**
	 * 设置阶段类型，见PhaseTag中定义
	 * @param who 阶段类型
	 */
	public void setFamily(int who) {
		if (!PhaseTag.isPhase(who)) {
			throw new IllegalPhaseException("illegal phase: %d", who);
		}
		family = who;
	}

	/**
	 * 生成当前实例副本
	 * @return TaskPart实例
	 */
	public TaskPart duplicate() {
		return new TaskPart(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != TaskPart.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TaskPart) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (issuer == null) {
			return family;
		}
		return issuer.hashCode() ^ family;
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
	public int compareTo(TaskPart that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(issuer, that.issuer);
		if (ret == 0) {
			ret = Laxkit.compareTo(family, that.family);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s:<%s>", PhaseTag.translate(family),
				(issuer != null ? issuer : "SYSTEM"));
	}

	//	public byte[] build() {
	//		ClassWriter writer = new ClassWriter();
	//		build(writer);
	//		return writer.effuse();
	//	}
	//	
	//	public void test1() {
	//		String hex = "89e495e7941cf9e40e6980d14a16bf023ccd4c92";
	//		Siger hash = new Siger(hex);
	//		TaskPart part = new TaskPart(hash, PhaseTag.INIT);
	//		byte[] b = part.build();
	//		System.out.printf("byte size is %d\n",b.length);
	//		
	//		ClassReader reader = new ClassReader(b);
	//		TaskPart e  = new TaskPart(reader);
	//		System.out.printf("read seek is:%d, left:%d, used:%d, length:%d\n",
	//				reader.getSeek(), reader.getLeft(), reader.getUsed(), reader.getLength());
	//		System.out.printf("%s - %s\n", part, e);
	//	}
	//	
	//	public void test2() {
	////		String hex = "89e495e7941cf9e40e6980d14a16bf023ccd4c91";
	////		Siger hash = new Siger(hex);
	//		TaskPart part = new TaskPart(null, PhaseTag.INIT);
	//		byte[] b = part.build();
	//		System.out.printf("\n\nbyte size is %d\n",b.length);
	//		
	//		ClassReader reader = new ClassReader(b);
	//		TaskPart e  = new TaskPart(reader);
	//		System.out.printf("read seek is:%d, left:%d, used:%d, length:%d\n",
	//				reader.getSeek(), reader.getLeft(), reader.getUsed(), reader.getLength());
	//
	//		System.out.printf("%s - %s\n", part, e);
	//	}
	//	
	//	public void test3() {
	//		String hex = "89e495e7941cf9e40e6980d14a16bf023ccd4c92";
	//		Siger hash = new Siger(hex);
	//		TaskPart part =  new TaskPart(hash, PhaseTag.INIT);
	//		
	////		part = null;
	//
	//		ClassWriter writer = new ClassWriter();
	//		writer.writeDefault(part);
	//		byte[] b = writer.effuse();
	//		System.out.printf("byte length:%d\n", b.length);
	//		
	//		ClassReader reader = new ClassReader(b);
	//		TaskPart e = (TaskPart)reader.readDefault();
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
	//		TaskPart e = new TaskPart();
	////		e.test1();
	////		e.test2();
	////		e.test3();
	//		e.test4();
	//	}

}