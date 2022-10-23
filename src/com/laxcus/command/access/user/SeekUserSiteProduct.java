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
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 检索用户分布站点处理结果
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class SeekUserSiteProduct extends EchoProduct {

	private static final long serialVersionUID = -1523318535357935085L;

	/** 检索结果集合 **/
	private TreeSet<SeekUserSiteItem> array = new TreeSet<SeekUserSiteItem>();

	/**
	 * 构造默认的检索用户分布站点
	 */
	public SeekUserSiteProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析检索用户分布站点
	 * @param reader 可类化数据读取器
	 */
	public SeekUserSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成检索用户分布站点处理结果的数据副本
	 * @param that 检索用户分布站点处理结果
	 */
	private SeekUserSiteProduct(SeekUserSiteProduct that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 增加一个用户资源检索结果单元，不允许空指针
	 * @param e 用户资源检索结果单元
	 * @return 成功返回真，否则假
	 */
	public boolean add(SeekUserSiteItem e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 增加一个用户资源检索结果单元，参数不允许空指针
	 * @param siger 用户签名
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean add(Siger siger, Node node) {
		return add(new SeekUserSiteItem(siger, node));
	}

	/**
	 * 保存一批用户资源检索结果单元
	 * @param a 用户资源检索结果单元数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<SeekUserSiteItem> a) {
		int size = array.size();
		for (SeekUserSiteItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部用户资源检索结果单元
	 * @return 用户资源检索结果单元列表
	 */
	public List<SeekUserSiteItem> list() {
		return new ArrayList<SeekUserSiteItem>(array);
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
	public SeekUserSiteProduct duplicate() {
		return new SeekUserSiteProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (SeekUserSiteItem e : array) {
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
			SeekUserSiteItem e = new SeekUserSiteItem(reader);
			array.add(e);
		}
	}

}
