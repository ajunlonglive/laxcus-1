/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.pool;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 账号图谱。<br><br>
 * 
 * 这是“用户审计引用”的简化版本，包括注册用户名和数据表名两个部分。
 * 
 * @author scott.liang
 * @version 1.1 10/07/2015
 * @since laxcus 1.0
 */
final class Sketch implements Classable, Markable, Serializable, Cloneable, Comparable<Sketch> {

	private static final long serialVersionUID = 7959309900467013938L;

	/** 用户签名，不允许空值 **/
	private Siger siger;

	/** 账号下的数据表名 **/
	private TreeSet<Space> array = new TreeSet<Space>();

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 注册用户签名
		writer.writeObject(siger);
		// 写入表空间数目
		writer.writeInt(array.size());
		for (Space space : array) {
			writer.writeObject(space);
		}
		// 返回写入的字节数
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 注册用户签名
		siger = new Siger(reader);
		// 读成员数目
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space e = new Space(reader);
			array.add(e);
		}
		// 读取字节数
		return reader.getSeek() - seek;
	}

	/**
	 * 构造默认的账号图谱。
	 */
	private Sketch() {
		super();
	}

	/**
	 * 根据传入的账号图谱，生成它的数据副本
	 * @param that 账号图谱
	 */
	private Sketch(Sketch that) {
		this();
		siger = that.siger;
		array.addAll(that.array);
	}

	/**
	 * 构造账号图谱，指定用户签名
	 * @param siger 用户签名
	 */
	public Sketch(Siger siger) {
		this();
		setSiger(siger);
	}

	/**
	 * 从可类化数据读取器中解析账号图谱参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Sketch(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出账号图谱参数
	 * @param reader 标记化读取器
	 */
	public Sketch(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置用户名，不允许空值
	 * @param e 用户签名
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);
		// 赋值
		siger = e;
	}

	/**
	 * 返回用户名 
	 * @return 用户签名实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 保存一个数据表名
	 * @param e  空间名称
	 * @return 返回真或者假
	 */
	public boolean add(Space e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 保存一批数据表名
	 * @param a  数据表名数组
	 * @return  已经保存的成员数目
	 */
	public int addAll(Space[] a) {
		int size = array.size();
		for (int i = 0; i < a.length; i++) {
			add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批数据表名
	 * @param a  数据表名集合
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<Space> a) {
		int size = array.size();
		for (Space space : a) {
			add(space);
		}
		return array.size() - size;
	}

	/**
	 * 删除一个表名
	 * @param e 数据表名
	 * @return 返回真或者假
	 */
	public boolean remove(Space e) {
		return array.remove(e);
	}

	/**
	 * 检查一个表是否存在
	 * @param e 数据表名
	 * @return 返回真或者假
	 */
	public boolean contains(Space e) {
		return array.contains(e);
	}

	/**
	 * 清除全部数据表名
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 返回数据表名列表
	 * @return 表名列表
	 */
	public List<Space> list() {
		return new ArrayList<Space>(array);
	}

	/**
	 * 返回表空间数目
	 * @return 表成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 生成当前映像的数据副本
	 * @return Sketch实例
	 */
	public Sketch duplicate() {
		return new Sketch(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Sketch.class) {
			return false;
		} else if (this == that) {
			return true;
		}
		return compareTo((Sketch) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return siger.hashCode();
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return siger.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Sketch that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(siger, that.siger);
	}
	
//	public void test() {
//		ArrayList<Sketch> array = new ArrayList<Sketch>();
//		Space space = new Space("媒体素材库", "音频");
//		Siger siger = SHAUser.doUsername("demo");
//		Sketch sketch = new Sketch(siger);
//		sketch.add(space);
//		array.add(sketch);
//
//		ClassWriter writer = new ClassWriter(10240);
//		// 保存数据
//		writer.writeInt(array.size());
//		for (Sketch e : array) {
//			writer.writeObject(e);
//		}
//		// 生成字节流
//		byte[] b = writer.effuse();
//
//		try {
//			FileOutputStream out = new FileOutputStream("c:/469.sketch");
//			out.write(b);
//			out.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println("oak!");
//	}
//	
//	public static void main(String[] args) {
//		new Sketch().test();
//	}
}