/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rebuild;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 设置DSM表压缩倍数命令处理报告
 * 
 * @author scott.liang
 * @version 1.0 5/21/2019
 * @since laxcus 1.0
 */
public class SetDSMReduceProduct extends EchoProduct {
	
	private static final long serialVersionUID = 2306595194741454574L;

	/** 执行单元 **/
	private Set<TissItem> array = new TreeSet<TissItem>();

	/**
	 * 根据传入的设置DSM表压缩倍数命令处理报告实例，生成它的数据副本
	 * @param that SetDSMReduceProduct实例
	 */
	private SetDSMReduceProduct(SetDSMReduceProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造默认的设置DSM表压缩倍数命令处理报告
	 */
	public SetDSMReduceProduct() {
		super();
	}

	/**
	 * 构造设置DSM表压缩倍数命令处理报告，指定参数
	 * @param site 节点地址
	 * @param state 状态
	 */
	public SetDSMReduceProduct(Node site, int state) {
		this();
		add(new TissItem(site, state));
	}

	/**
	 * 从可类化数据读取器中解析设置DSM表压缩倍数命令处理报告
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SetDSMReduceProduct(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 保存一个单元
	 * @param e SetDSMReduceItem实例
	 * @return 返回真或者假
	 */
	public boolean add(TissItem e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 保存一个设置DSM表压缩倍数命令处理报告
	 * @param e SetDSMReduceProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(SetDSMReduceProduct e) {
		int size = array.size();
		array.addAll(e.array);
		return array.size() - size;
	}

	/**
	 * 输出全部处理单元 
	 * @return 返回SetDSMReduceItem列表
	 */
	public List<TissItem> list() {
		return new ArrayList<TissItem>(array);
	}

	/**
	 * 统计单元数目
	 * @return 单元数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SetDSMReduceProduct duplicate() {
		return new SetDSMReduceProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (TissItem e : array) {
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
			TissItem item = new TissItem(reader);
			array.add(item);
		}
	}

}
