/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.relate;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.call.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * CALL站点成员检索结果
 * 
 * @author scott.liang
 * @version 1.1 6/12/2015
 * @since laxcus 1.0
 */
public final class TakeCallItemProduct extends EchoProduct {

	private static final long serialVersionUID = -9024407651246604056L;

	/** 账号签名 **/
	private Siger siger;

	/** CALL站点成员 **/
	private TreeSet<CallItem> array = new TreeSet<CallItem>();

	/**
	 * 构造默认和私有CALL成员检索结果
	 */
	private TakeCallItemProduct() {
		super();
	}

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that TakeCallItemProduct实例
	 */
	private TakeCallItemProduct(TakeCallItemProduct that) {
		super(that);
		siger = that.siger;
		array.addAll(that.array);
	}

	/**
	 * 构造CALL成员检索结果，指定账号签名
	 * @param siger 账号签名
	 */
	public TakeCallItemProduct(Siger siger) {
		this();
		setSiger(siger);
	}

	/**
	 * 从可类化数据读取器解析CALL成员检索结果参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeCallItemProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置账号签名，不允许空值.
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);

		siger = e;
	}

	/**
	 * 返回账号签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 加入一组记录
	 * @param that TakeCallItemProduct实例
	 * @return 加入成功返回真，否则假
	 */
	public boolean accede(TakeCallItemProduct that) {
		boolean success = (siger.compareTo(that.siger) == 0);
		if (success) {
			array.addAll(that.array);
		}
		return success;
	}

	/**
	 * 保存CALL站点成员，不允许空指针
	 * @param e CallItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(CallItem e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 输出全部CALL站点成员列表
	 * @return CallItem列表
	 */
	public List<CallItem> list() {
		return new ArrayList<CallItem>(array);
	}

	/**
	 * 统计成员数目
	 * @return 成员数目
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
	public TakeCallItemProduct duplicate() {
		return new TakeCallItemProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(siger);
		writer.writeInt(array.size());
		for (CallItem e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		siger = new Siger(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			CallItem e = new CallItem(reader);
			array.add(e);
		}
	}

}