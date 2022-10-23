/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 数据块编号处理结果。<br>
 * 
 * @author scott.liang
 * @version 1.1 8/18/2015
 * @since laxcus 1.0
 */
public final class StubProduct extends EchoProduct {

	private static final long serialVersionUID = -845050873005827296L;

	/** 数据块编号处理结果 **/
	private TreeSet<Long> array = new TreeSet<Long>();

	/**
	 * 构造数据块编号处理结果的浅层副本
	 * @param that StubProduct实例
	 */
	private StubProduct(StubProduct that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 构造数据块编号处理结果
	 */
	public StubProduct() {
		super();
	}

	/**
	 * 从可类化读取器中解析数据块编号处理结果
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public StubProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 保存数据块编号
	 * @param stub 数据块编号
	 * @return 成功返回真，否则假
	 */
	public boolean add(long stub) {
		return array.add(stub);
	}

	/**
	 * 保存一批数据块编号
	 * @param a 数据块编号列表
	 * @return 返回新增成员数目
	 */
	public int addAll(List<Long> a) {
		int size = array.size();
		for (Long e : a) {
			add(e.longValue());
		}
		return array.size() - size;
	}

	/**
	 * 保存数据块编号数组
	 * @param a 数据块编号数组
	 * @return 返回新增成员数目
	 */
	public int add(long[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部数据块编号
	 * @return 数据块编号列表
	 */
	public List<Long> list() {
		return new ArrayList<Long>(array);
	}

	/**
	 * 统计数据块编号成员数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public StubProduct duplicate() {
		return new StubProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (long stub : array) {
			writer.writeLong(stub);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();
			array.add(stub);
		}
	}

}