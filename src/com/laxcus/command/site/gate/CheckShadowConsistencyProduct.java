/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.gate;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * GATE注册用户与GATE站点编号一致性检查结果。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/20/2019
 * @since laxcus 1.0
 */
public class CheckShadowConsistencyProduct extends EchoProduct {

	private static final long serialVersionUID = 4459199583335851591L;

	/** GATE注册用户与GATE站点编号一致性检查单元 **/
	private TreeSet<GateUserConsistencyItem> array = new TreeSet<GateUserConsistencyItem>();

	/**
	 * 构造GATE注册用户与GATE站点编号一致性检查单元的浅层副本
	 * @param that CheckShadowConsistencyProduct实例
	 */
	private CheckShadowConsistencyProduct(CheckShadowConsistencyProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造GATE注册用户与GATE站点编号一致性检查结果
	 */
	public CheckShadowConsistencyProduct() {
		super();
	}

	/**
	 * 从可类化读取器中解析EXPRESS结果参数
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public CheckShadowConsistencyProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存GATE注册用户与GATE站点编号一致性检查单元
	 * @param e GateUserConsistencyItem实例
	 * @return 保存成功返回“真”，否则“假”。
	 */
	public boolean add(GateUserConsistencyItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批GATE注册用户与GATE站点编号一致性检查单元
	 * @param a GateUserConsistencyItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<GateUserConsistencyItem> a) {
		int size = array.size();
		for (GateUserConsistencyItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存一批GATE注册用户与GATE站点编号一致性检查单元
	 * @param a 结果实例
	 * @return 返回新增成员数目
	 */
	public int addAll(CheckShadowConsistencyProduct e) {
		return addAll(e.array);
	}

	/**
	 * 输出GATE注册用户与GATE站点编号一致性检查单元
	 * @return GateUserConsistencyItem列表
	 */
	public List<GateUserConsistencyItem> list() {
		return new ArrayList<GateUserConsistencyItem>(array);
	}

	/**
	 * 统计GATE注册用户与GATE站点编号一致性检查单元数目
	 * @return GateUserConsistencyItem成员数目
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
	public CheckShadowConsistencyProduct duplicate() {
		return new CheckShadowConsistencyProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (GateUserConsistencyItem e : array) {
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
			GateUserConsistencyItem e = new GateUserConsistencyItem(reader);
			array.add(e);
		}
	}

}