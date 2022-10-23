/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获得BANK某类子站点反馈结果。<br>
 * BANK站点发出、HASH/GATE/ENTRANCE站点接收。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeBankSubSitesProduct extends EchoProduct {

	private static final long serialVersionUID = 6738948156054497065L;

	/** 单元数组 **/
	private TreeSet<BankSubSiteItem> array = new TreeSet<BankSubSiteItem>();
	
	/**
	 * 构造默认的申请主机序列号命令
	 */
	public TakeBankSubSitesProduct() {
		super();
	}

	/**
	 * 生成申请主机序列号的数据副本
	 * @param that 申请主机序列号
	 */
	private TakeBankSubSitesProduct(TakeBankSubSitesProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 从可类化数据读取器中解析申请主机序列号
	 * @param reader 可类化数据读取器
	 */
	public TakeBankSubSitesProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个BANK子站单元
	 * @param e BANK子站单元
	 * @return 注册成功返回真，否则假
	 */
	public boolean add(BankSubSiteItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批BANK子站单元
	 * @param a BANK子站单元列表
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<BankSubSiteItem> a) {
		int size = array.size();
		for (BankSubSiteItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 输出全部BANK子站单元
	 * @return BANK子站单元列表
	 */
	public List<BankSubSiteItem> list() {
		return new ArrayList<BankSubSiteItem>(array);
	}

	/**
	 * 统计BANK子站单元数目
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeBankSubSitesProduct duplicate() {
		return new TakeBankSubSitesProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 保存全部BANK子站单元
		writer.writeInt(array.size());
		for (BankSubSiteItem e : array) {
			writer.writeDefault(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 取出全部BANK子站单元
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			BankSubSiteItem e = (BankSubSiteItem) reader.readDefault();
			add(e);
		}
	}

}