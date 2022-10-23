/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 本地分布任务组件结果
 * 
 * @author scott.liang
 * @version 1.0 4/3/2022
 * @since laxcus 1.0
 */
public class CheckLocalTaskProduct extends EchoProduct {

	private static final long serialVersionUID = 1340116966141163344L;
	
	/** 阶段命令数组 **/
	private ArrayList<Phase> array = new ArrayList<Phase>();
	
	/**
	 * 构造默认的本地分布任务组件结果
	 */
	public CheckLocalTaskProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析本地分布任务组件结果
	 * @param reader 可类化数据读取器
	 */
	public CheckLocalTaskProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成本地分布任务组件结果副本
	 * @param that 源实例
	 */
	private CheckLocalTaskProduct(CheckLocalTaskProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存阶段命名
	 * @param e 阶段命名
	 * @return 返回真或者假
	 */
	public boolean add(Phase e) {
		Laxkit.nullabled(e);
		// 存在，忽略它
		if (array.contains(e)) {
			return false;
		}
		return array.add(e);
	}
	
	/**
	 * 保存一批阶段命名
	 * @param a 阶段命名数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Phase> a) {
		int size = array.size();
		for (Phase e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部阶段命名
	 * @return 阶段命名列表
	 */
	public List<Phase> list() {
		return new ArrayList<Phase>(array);
	}

	/**
	 * 统计阶段命名数目
	 * @return 阶段命名数目
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
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CheckLocalTaskProduct duplicate() {
		return new CheckLocalTaskProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Phase e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Phase e = new Phase(reader);
			array.add(e);
		}
	}

}