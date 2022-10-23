/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub.sign;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块签名表 <br>
 * 
 * 保存一个站点下面的全部数据块签名
 * 
 * @author scott.liang
 * @version 1.0 9/12/2016
 * @since laxcus 1.0
 */
public final class SignTable implements Classable, Cloneable, Comparable<SignTable>, Serializable {

	private static final long serialVersionUID = 2645404130316568261L;

	/** 表名 **/
	private Space space;

	/** 数据块编号 - 数据块签名 **/
	private Map<Long, StubSign> signs = new TreeMap<Long, StubSign>();

	/**
	 * 构造默认和私有的数据块签名表
	 */
	public SignTable() {
		super();
	}

	/**
	 * 生成数据块签名表的数据副本
	 * @param that
	 */
	private SignTable(SignTable that) {
		this();
		space = that.space;
		signs.putAll(that.signs);
	}

	/**
	 * 构造数据块签名表，指定数据表名
	 * @param space  数据表名
	 */
	public SignTable(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析数据块签名表
	 * @param reader  可类化数据读取器
	 */
	public SignTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据表名，不允许空值
	 * @param e  数据表名
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 输出数目块编号
	 * @return 数据块编号数目
	 */
	public List<Long> getStubs() {
		return new ArrayList<Long>(signs.keySet());
	}

	/**
	 * 保存一个数据块签名 
	 * @param e  数据块签名
	 * @return  保存成功返回“真”，否则“假”。
	 */
	public boolean add(StubSign e) {
		Laxkit.nullabled(e);
		
		return signs.put(e.getStub(), e) == null;
	}

	/**
	 * 保存一批数据块签名
	 * @param a  数据块签名集合
	 * @return  返回新增加的数据块签名数目
	 */
	public int addAll(Collection<StubSign> a) {
		int size = signs.size();
		for (StubSign e : a) {
			add(e);
		}
		return signs.size() - size;
	}

	/**
	 * 删除数据块签名
	 * @param e  数据块签名实例
	 * @return  删除成功返回“真”，否则“假”。
	 */
	public boolean remove(StubSign e) {
		// 判断一致
		boolean success = (contains(e));
		// 删除它
		if (success) {
			success = signs.remove(e.getStub()) != null;
		}
		return success;
	}

	/**
	 * 判断数据块签名存在
	 * @param e  数据块签名实例
	 * @return  存在返回“真”，否则“假”。
	 */
	public boolean contains(StubSign e) {
		StubSign that = signs.get(e.getStub());
		return (that != null && e.compareTo(that) == 0);
	}

	/**
	 * 返回数据块签名
	 * @param stub 数据块编号
	 * @return StubSign实例
	 */
	public StubSign find(long stub) {
		return signs.get(stub);
	}

	/**
	 * 返回数据块签名列表
	 * @return StubSign列表
	 */
	public List<StubSign> list() {
		return new ArrayList<StubSign>(signs.values());
	}

	/**
	 * 返回数据块签名数目
	 * @return
	 */
	public int size() {
		return signs.size();
	}

	/**
	 * 判断数据块签名集合是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return signs.isEmpty();
	}

	/**
	 * 生成当前实例的数据副本
	 * @return SignTable实例
	 */
	public SignTable duplicate(){
		return new SignTable(this);
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
		if (that == null || that.getClass() != SignTable.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SignTable) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getSpace().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SignTable that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(space, that.space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int scale = writer.size();
		// 数据表名
		writer.writeObject(space);
		// 数据块签名
		writer.writeInt(signs.size());
		for (StubSign sign : signs.values()) {
			writer.writeObject(sign);
		}
		return signs.size() - scale;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 数据表名
		space = new Space(reader);
		// 数据块签名
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			StubSign sign = new StubSign(reader);
			signs.put(sign.getStub(), sign);
		}
		return reader.getSeek() - seek;
	}

}