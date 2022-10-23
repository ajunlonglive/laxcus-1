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
 * 检索用户阶段命名处理结果
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class SeekUserTaskProduct extends EchoProduct {

	private static final long serialVersionUID = 857441820640587728L;

	/** 检索结果集合 **/
	private TreeSet<SeekUserTaskItem> array = new TreeSet<SeekUserTaskItem>();

	/**
	 * 构造默认的检索用户阶段命名
	 */
	public SeekUserTaskProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析检索用户阶段命名
	 * @param reader 可类化数据读取器
	 */
	public SeekUserTaskProduct(ClassReader reader) {
		this();
		this.resolve(reader);
	}
	
	/**
	 * 生成检索用户阶段命名处理结果的数据副本
	 * @param that 检索用户阶段命名处理结果
	 */
	private SeekUserTaskProduct(SeekUserTaskProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 增加一个用户资源检索结果单元，不允许空指针
	 * @param e 用户资源检索结果单元
	 * @return 成功返回真，否则假
	 */
	public boolean add(SeekUserTaskItem e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 保存一批用户资源检索结果单元
	 * @param a 用户资源检索结果单元数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<SeekUserTaskItem> a) {
		int size = array.size();
		for (SeekUserTaskItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部用户资源检索结果单元
	 * @return 用户资源检索结果单元列表
	 */
	public List<SeekUserTaskItem> list() {
		return new ArrayList<SeekUserTaskItem>(array);
	}

	/**
	 * 输出用户资源检索结果单元成员数目
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
	public SeekUserTaskProduct duplicate() {
		return new SeekUserTaskProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (SeekUserTaskItem e : array) {
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
			SeekUserTaskItem e = new SeekUserTaskItem(reader);
			array.add(e);
		}
	}

}
