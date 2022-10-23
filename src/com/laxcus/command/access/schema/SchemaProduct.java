/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.schema;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据库集合结果
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class SchemaProduct extends EchoProduct {

	private static final long serialVersionUID = -1874280754658069638L;
	
	/** 数据库集合 **/
	private ArrayList<Schema> array = new ArrayList<Schema>();

	/**
	 * 根据传入的数据库集合，生成它的浅层副本
	 * @param that SchemaProduct实例
	 */
	private SchemaProduct(SchemaProduct that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 构造默认的数据库集合
	 */
	public SchemaProduct() {
		super();
	}

	/**
	 * 从可类化读取器中解析数据库集合参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SchemaProduct(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 保存一个数据库，不允许空指针
	 * @param e Schema实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Schema e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 输出全部数据库
	 * @return Schema列表
	 */
	public List<Schema> list() {
		return new ArrayList<Schema>( array);
	}
	
	/**
	 * 统计数据库成员
	 * @return 数据库成员数目
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
	public SchemaProduct duplicate() {
		return new SchemaProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Schema e : array) {
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
			Schema e = new Schema(reader);
			array.add(e);
		}
	}

}
