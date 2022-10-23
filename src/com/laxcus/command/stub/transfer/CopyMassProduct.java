/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.transfer;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * DATA主从节点之间的数据块复制结果
 * 
 * @author scott.liang
 * @version 1.0 6/15/2019
 * @since laxcus 1.0
 */
public class CopyMassProduct extends EchoProduct {
	
	private static final long serialVersionUID = -6015497472211412518L;

	/** 被复制的数据块单元集合 **/
	private TreeSet<CopyMassItem> array = new TreeSet<CopyMassItem>();

	/**
	 * 构造默认的DATA主从节点之间的数据块复制结果
	 */
	public CopyMassProduct() {
		super();
	}

	/**
	 * 构造DATA主从节点之间的数据块复制结果，保存一个单元
	 * @param item 单元
	 */
	public CopyMassProduct(CopyMassItem item) {
		this();
		add(item);
	}
	
	/**
	 * 从可类化数据读取器中解析DATA主从节点之间的数据块复制结果
	 * @param reader 可类化数据读取器
	 */
	public CopyMassProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的DATA主从节点之间的数据块复制结果，生成它的数据副本
	 * @param that CopyMasterMassProduct实例
	 */
	private CopyMassProduct(CopyMassProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个处理结果
	 * @param e CopyMassItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(CopyMassItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一个处理结果
	 * @param stub 数据块编号
	 * @param successful 成功
	 * @return 返回真或者假
	 */
	public boolean add(long stub, boolean successful) {
		return add(new CopyMassItem(stub, successful));
	}

	/**
	 * 保存一批处理结果
	 * @param a CopyMassItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<CopyMassItem> a) {
		int size = array.size();
		for (CopyMassItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存一批处理结果
	 * @param e CopyMasterMassProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(CopyMassProduct e) {
		return addAll(e.array);
	}
	
	/**
	 * 输出全部被复制的数据块单元
	 * @return 返回CopyMassItem列表
	 */
	public List<CopyMassItem> list() {
		return new ArrayList<CopyMassItem>(array);
	}

	/**
	 * 统计成员数目
	 * @return 返回被复制的数据块单元成员数目
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

	/**
	 * 清除全部
	 */
	public void clear() {
		array.clear();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (CopyMassItem e : array) {
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
			CopyMassItem e = new CopyMassItem(reader);
			array.add(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CopyMassProduct duplicate() {
		return new CopyMassProduct(this);
	}

}