/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.schema;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 重构时间报告
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class SwitchTimeProduct extends EchoProduct {

	private static final long serialVersionUID = -5253318935626442006L;

	/** 重构时间集合 **/
	private ArrayList<SwitchTime> array = new ArrayList<SwitchTime>();

	/**
	 * 根据传入的重构时间报告，生成它的浅层副本
	 * @param that SwitchTimeProduct实例
	 */
	private SwitchTimeProduct(SwitchTimeProduct that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 构造默认的重构时间报告
	 */
	public SwitchTimeProduct() {
		super();
	}

	/**
	 * 构造重构时间报告，指定集合
	 * @param a 重构时间集合
	 */
	public SwitchTimeProduct(Collection<SwitchTime> a) {
		this();
		addAll(a);
	}

	/**
	 * 从可类化读取器中解析重构时间报告参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SwitchTimeProduct(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 保存一个重构时间，不允许空指针
	 * @param e SwitchTime实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(SwitchTime e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批重构时间
	 * @param a 重构时间数组
	 * @return 返回真增成员数目
	 */
	public int addAll(Collection<SwitchTime> a) {
		int size = array.size();
		for (SwitchTime e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部重构时间
	 * @return SwitchTime列表
	 */
	public List<SwitchTime> list() {
		return array;
	}
	
	/**
	 * 统计重构时间尺寸
	 * @return 重构时间尺寸
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SwitchTimeProduct duplicate() {
		return new SwitchTimeProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (SwitchTime e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SwitchTime e = new SwitchTime(reader);
			array.add(e);
		}
	}

}