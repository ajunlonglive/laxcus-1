/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 修复表数据块的处理结果
 * 
 * @author scott.liang
 * @version 1.0 4/12/2017
 * @since laxcus 1.0
 */
public class RollTableMassProduct extends EchoProduct {

	private static final long serialVersionUID = -214366128145000703L;
	
	/** 回滚处理单元结果集合 **/
	private ArrayList<RollTableItem> array = new ArrayList<RollTableItem>();

	/**
	 * 构造默认的修复表数据块的处理结果
	 */
	public RollTableMassProduct() {
		super();
	}

	/**
	 * 生成修复表数据块的处理结果数据副本
	 * @param that RollTableMassProduct实例
	 */
	private RollTableMassProduct(RollTableMassProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 从可类化数据读取器中解析修复表数据块的处理结果
	 * @param reader 可类化数据读取器
	 */
	public RollTableMassProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个回滚处理单元结果
	 * @param e RollTableItem实例
	 * @return 成功返回真，否则假
	 */
	public boolean add(RollTableItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批回滚处理单元结果
	 * @param a 回滚处理单元结果数组
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<RollTableItem> a) {
		int size = array.size();
		for (RollTableItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存全部单元
	 * @param that RollTableMassProduct实例
	 * @return 返回新增加的成员数目
	 */
	public int addAll(RollTableMassProduct that) {
		return addAll(that.array);
	}

	/**
	 * 返回一个回滚处理单元结果
	 * @param e RollTableItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(RollTableItem e) {
		return array.remove(e);
	}

	/**
	 * 输出全部回滚处理单元结果
	 * @return RollTableItem列表
	 */
	public List<RollTableItem> list() {
		return new ArrayList<RollTableItem>(array);
	}

	/**
	 * 判断包含一个回滚处理单元结果
	 * @param e RollTableItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(RollTableItem e) {
		return array.contains(e);
	}

	/**
	 * 统计成员数目
	 * @return RollTableItem单元数据
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
	public RollTableMassProduct duplicate() {
		return new RollTableMassProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (RollTableItem item : array) {
			writer.writeObject(item);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			RollTableItem item = new RollTableItem(reader);
			array.add(item);
		}
	}


}