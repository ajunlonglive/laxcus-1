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
 * 检索用户日志处理结果
 * 
 * @author scott.liang
 * @version 1.0 4/2/2018
 * @since laxcus 1.0
 */
public class ScanUserLogProduct extends EchoProduct {

	private static final long serialVersionUID = 8763434654586638727L;

	/** 检索结果集合 **/
	private TreeSet<ScanUserLogItem> array = new TreeSet<ScanUserLogItem>();

	/**
	 * 构造默认的检索用户日志
	 */
	public ScanUserLogProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析检索用户日志
	 * @param reader 可类化数据读取器
	 */
	public ScanUserLogProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成检索用户日志处理结果的数据副本
	 * @param that 检索用户日志处理结果
	 */
	private ScanUserLogProduct(ScanUserLogProduct that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 增加一个用户日志检索单元，不允许空指针
	 * @param e 用户日志检索单元
	 * @return 成功返回真，否则假
	 */
	public boolean add(ScanUserLogItem e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 保存一批用户日志检索单元
	 * @param a 用户日志检索单元数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<ScanUserLogItem> a) {
		int size = array.size();
		for (ScanUserLogItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存一批用户日志检索单元
	 * @param e
	 * @return
	 */
	public int addAll(ScanUserLogProduct e){
		return addAll(e.array);
	}

	/**
	 * 输出全部用户日志检索单元
	 * @return 用户日志检索单元列表
	 */
	public List<ScanUserLogItem> list() {
		return new ArrayList<ScanUserLogItem>(array);
	}
	
	/**
	 * 查找匹配的用户日志检索单元
	 * @param siger 用户签名
	 * @return 用户日志检索单元列表
	 */
	public List<ScanUserLogItem> find(Siger siger) {
		ArrayList<ScanUserLogItem> a = new ArrayList<ScanUserLogItem>();
		for (ScanUserLogItem e : array) {
			if (Laxkit.compareTo(siger, e.getSiger()) == 0) {
				a.add(e);
			}
		}
		return a;
	}

	/**
	 * 输出用户日志检索单元成员数目
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
	public ScanUserLogProduct duplicate() {
		return new ScanUserLogProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (ScanUserLogItem e : array) {
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
			ScanUserLogItem e = new ScanUserLogItem(reader);
			array.add(e);
		}
	}

}
