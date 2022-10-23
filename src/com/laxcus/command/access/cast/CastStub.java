/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.cast;

import java.util.*;

import com.laxcus.util.classable.*;

/**
 * 投递数据块编号命令。
 * 
 * @author scott.liang
 * @version 1.1 7/17/2015
 * @since laxcus 1.0
 */
public abstract class CastStub extends CastMethod {

	private static final long serialVersionUID = -1529081988980067686L;

	/** 数据块编号集合 **/
	private TreeSet<Long> array = new TreeSet<Long>();

	/**
	 * 构造默认的投递数据块编号命令
	 */
	protected CastStub() {
		super();
	}

	/**
	 * 根据传入的投递数据块编号命令，生成它的数据副本
	 * @param that CastStub实例
	 */
	protected CastStub(CastStub that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存数据块编号
	 * @param e 数据块编号
	 * @return 返回真或者假
	 */
	public boolean addStub(Long e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 保存一批数据块编号
	 * @param a 数据块编号数组
	 * @return 返回新增成员数目
	 */
	public int addStubs(Collection<Long> a) {
		int size = array.size();
		for (Long e : a) {
			addStub(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部数据块编号
	 * @return Long列表
	 */
	public List<Long> getStubs() {
		return new ArrayList<Long>(array);
	}
	
	/**
	 * 输出全部数据块编号
	 * @return long数组
	 */
	public long[] getStubArray() {
		long[] a = new long[array.size()];
		Iterator<Long> e = array.iterator();
		for (int i = 0; i < a.length; i++) {
			a[i] = e.next().longValue();
		}
		return a;
	}

	/**
	 * 以集合的形式返回全部数据块编号的副本
	 * @return Long集合
	 */
	public Set<Long> getStubSet() {
		return new TreeSet<Long>(array);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.post.PostMethod#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上级数据
		super.buildSuffix(writer);
		// 写入数据块编号
		writer.writeInt(array.size());
		for (Long e : array) {
			writer.writeLong(e.longValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.post.PostMethod#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级数据
		super.resolveSuffix(reader);
		// 读数据块编号
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();
			array.add(stub);
		}
	}

}