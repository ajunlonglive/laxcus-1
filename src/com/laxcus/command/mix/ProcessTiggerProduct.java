/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 处理Tigger类型报告
 * 
 * @author scott.liang
 * @version 1.0 1/24/2020
 * @since laxcus 1.0
 */
public class ProcessTiggerProduct extends EchoProduct {

	private static final long serialVersionUID = -3967178834629579400L;

	/** 单元数组 **/
	private TreeSet<ProcessTiggerItem> array = new TreeSet<ProcessTiggerItem>();

	/**
	 * 构造默认的处理Tigger类型报告
	 */
	public ProcessTiggerProduct() {
		super();
	}

	/**
	 * 构造处理Tigger类型报告，指定单元
	 * @param item 单元
	 */
	public ProcessTiggerProduct(ProcessTiggerItem item) {
		this();
		add(item);
	}

	/**
	 * 从可类化数据读取器解析处理Tigger类型报告
	 * @param reader 可类化数据读取器
	 */
	public ProcessTiggerProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造处理Tigger类型报告的数据副本
	 * @param that 处理Tigger类型报告
	 */
	private ProcessTiggerProduct(ProcessTiggerProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个处理单元，不允许空指针
	 * @param e ProcessTiggerItem实例
	 * @return 返回真或者假
	 */
	public boolean add(ProcessTiggerItem e) {
		Laxkit.nullabled(e);
		
		return array.add(e);
	}
	
	/**
	 * 保存一个处理单元
	 * @param node Node实例
	 * @param type 当前有效类型
	 * @param successful 成功标记
	 * @return 返回真或者假
	 */
	public boolean add(Node node, int type, boolean successful) {
		return add(new ProcessTiggerItem(node, type, successful));
	}

	/**
	 * 保存一批处理单元
	 * @param a ProcessTiggerItem列表
	 * @return 返回新增成员数目
	 */
	public int addAll(List<ProcessTiggerItem> a) {
		Laxkit.nullabled(a);
		int size = array.size();
		for (ProcessTiggerItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批处理单元
	 * @param e ProcessTiggerProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(ProcessTiggerProduct e) {
		return addAll(e.list());
	}

	/**
	 * 删除一个处理单元
	 * @param e ProcessTiggerItem实例
	 * @return 返回真或者假
	 */
	public boolean remove(ProcessTiggerItem e) {
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e ProcessTiggerItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(ProcessTiggerItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部处理单元列表
	 * @return ProcessTiggerItem列表
	 */
	public List<ProcessTiggerItem> list() {
		return new ArrayList<ProcessTiggerItem>(array);
	}

	/**
	 * 统计处理单元成员数目
	 * @return 处理单元成员数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ProcessTiggerProduct duplicate() {
		return new ProcessTiggerProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (ProcessTiggerItem e : array) {
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
			ProcessTiggerItem item = new ProcessTiggerItem(reader);
			array.add(item);
		}
	}

}