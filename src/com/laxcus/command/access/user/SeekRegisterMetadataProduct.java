/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 用户在线注册元数据检索结果
 * 
 * @author scott.liang
 * @version 1.0 5/12/2018
 * @since laxcus 1.0
 */
public class SeekRegisterMetadataProduct extends EchoProduct {

	private static final long serialVersionUID = 3126466223591018726L;

	/** 检索结果集合 **/
	private TreeSet<SeekRegisterMetadataItem> array = new TreeSet<SeekRegisterMetadataItem>();

	/**
	 * 构造默认的用户在线注册元数据检索结果
	 */
	public SeekRegisterMetadataProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析用户在线注册元数据检索结果
	 * @param reader 可类化数据读取器
	 */
	public SeekRegisterMetadataProduct(ClassReader reader) {
		this();
		this.resolve(reader);
	}
	
	/**
	 * 生成用户在线注册元数据检索结果的数据副本
	 * @param that 用户在线注册元数据检索结果
	 */
	private SeekRegisterMetadataProduct(SeekRegisterMetadataProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 增加一个用户元数据检索结果单元，不允许空指针
	 * @param e 用户元数据检索结果单元
	 * @return 成功返回真，否则假
	 */
	public boolean add(SeekRegisterMetadataItem e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 保存一批用户元数据检索结果单元
	 * @param a 用户元数据检索结果单元数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<SeekRegisterMetadataItem> a) {
		int size = array.size();
		for (SeekRegisterMetadataItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存另一组用户元数据检索结果单元
	 * @param e
	 * @return
	 */
	public int addAll(SeekRegisterMetadataProduct e) {
		return addAll(e.array);
	}

	/**
	 * 输出全部用户元数据检索结果单元
	 * @return 用户元数据检索结果单元列表
	 */
	public List<SeekRegisterMetadataItem> list() {
		return new ArrayList<SeekRegisterMetadataItem>(array);
	}
	
	/**
	 * 查找匹配的签名
	 * @param siger
	 * @return
	 */
	public List<SeekRegisterMetadataItem> find(Siger siger) {
		ArrayList<SeekRegisterMetadataItem> a = new ArrayList<SeekRegisterMetadataItem>();
		for (SeekRegisterMetadataItem e : array) {
			if (Laxkit.compareTo(e.getSeat().getSiger(), siger) == 0) {
				a.add(e);
			}
		}
		return a;
	}

	/**
	 * 输出用户元数据检索结果单元成员数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
	

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SeekRegisterMetadataProduct duplicate() {
		return new SeekRegisterMetadataProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (SeekRegisterMetadataItem e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SeekRegisterMetadataItem e = new SeekRegisterMetadataItem(reader);
			array.add(e);
		}
	}
}
