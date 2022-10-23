/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 诊断用户消耗的资源结果
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public class CheckUserCostProduct extends EchoProduct {

	private static final long serialVersionUID = 3146106632408001248L;

	/** 用户签名 -> 单元 **/
	private Map<Siger, UserCostElement> array = new TreeMap<Siger, UserCostElement>();

	/**
	 * 构造默认的服务器系统信息检测结果
	 */
	public CheckUserCostProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器解析服务器系统信息检测结果
	 * @param reader 可类化数据读取器
	 */
	public CheckUserCostProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造服务器系统信息检测结果的数据副本
	 * @param that CheckUserCostProduct实例
	 */
	private CheckUserCostProduct(CheckUserCostProduct that) {
		super(that);
		array.putAll(that.array);
	}

	/**
	 * 加一个
	 * @param product
	 */
	public void add(CheckUserCostProduct product) {
		for (UserCostElement element : product.array.values()) {
			add(element.getIssuer(), element);
		}
	}

	/**
	 * 加全部
	 * @param issuer 用户签名
	 * @param element 成员
	 */
	public void add(Siger issuer, UserCostElement element) {
		UserCostElement that = array.get(issuer);
		if (that != null) {
			that.addAll(element.list());
		} else {
			array.put(issuer, element);
		}
	}

	/**
	 * 加一个单元
	 * @param issuer 签名
	 * @param item 成员
	 */
	public void add(Siger issuer, UserCostItem item) {
		UserCostElement element = array.get(issuer);
		if (element != null) {
			element.add(item);
		} else {
			element = new UserCostElement(issuer);
			element.add(item);
			array.put(element.getIssuer(), element);
		}
	}

	/**
	 * 输出全部
	 * @return
	 */
	public List<UserCostElement> list() {
		return new ArrayList<UserCostElement>(array.values());
	}
	
	/**
	 * 查找一个成员
	 * @param siger 签名
	 * @return 返回匹配的成员
	 */
	public UserCostElement find(Siger siger) {
		return array.get(siger);
	}

	/**
	 * 判断是空
	 * @return
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 有效单元数目
	 * @return 整数
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CheckUserCostProduct duplicate() {
		return new CheckUserCostProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (UserCostElement e : array.values()) {
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
			UserCostElement element = new UserCostElement(reader);
			add(element.getIssuer(), element);
		}
	}

}