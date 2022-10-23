/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据库配置集合
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class TableProduct extends EchoProduct {

	private static final long serialVersionUID = 8930225920435654441L;

	/** 数据表集合 **/
	private TreeSet<Table> array = new TreeSet<Table>();

	/**
	 * 根据传入的数据表集合，生成它的浅层副本
	 * @param that TableProduct实例
	 */
	private TableProduct(TableProduct that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 构造默认的数据表集合
	 */
	public TableProduct() {
		super();
	}

	/**
	 * 从可类化读取器中解析数据表集合参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TableProduct(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 保存一个数据表，不允许空指针
	 * @param e Table实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Table e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 输出全部数据表
	 * @return Table列表
	 */
	public List<Table> list() {
		return new ArrayList<Table>(array);
	}
	
	/**
	 * 统计数据表尺寸
	 * @return 数据表尺寸
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
	public TableProduct duplicate() {
		return new TableProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Table e : array) {
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
			Table e = new Table(reader);
			array.add(e);
		}
	}

}