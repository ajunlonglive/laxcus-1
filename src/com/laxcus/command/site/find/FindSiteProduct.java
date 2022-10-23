/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.find;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 站点查询结果
 * 
 * @author scott.liang
 * @version 1.1 3/23/2015
 * @since laxcus 1.0
 */
public class FindSiteProduct extends EchoProduct {

	private static final long serialVersionUID = 3687798752093724122L;

	/** 查询站点标识 **/
	private FindSiteTag tag;

	/** 查询站点数组 **/
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 构造默认的站点查询结果
	 */
	protected FindSiteProduct() {
		super();
	}

	/**
	 * 使用传入实例，生成站点查询结果数据副本
	 * @param that FindSiteProduct实例
	 */
	protected FindSiteProduct(FindSiteProduct that) {
		super(that);
		tag = that.tag;
		array.addAll(that.array);
	}

	/**
	 * 构造站点查询结果，指定标识
	 */
	public FindSiteProduct(FindSiteTag tag) {
		super();
		setTag(tag);
	}

	/**
	 * 从可类化数据读取器中解析站点查询结果
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FindSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置查询站点标识
	 * @param e FindSiteTag实例
	 */
	public void setTag(FindSiteTag e) {
		Laxkit.nullabled(e);

		tag = e;
	}

	/**
	 * 返回查询站点标识
	 * @return FindSiteTag实例
	 */
	public FindSiteTag getTag() {
		return tag;
	}

	/**
	 * 返回站点类型
	 * @return 站点类型
	 */
	public byte getFamily() {
		return (tag != null ? tag.getFamily() : 0);
	}

	/**
	 * 返回站点级别
	 * @return 站点级别
	 */
	public byte getRank() {
		return (tag != null ? tag.getRank() : 0);
	}

	/**
	 * 保存站点地址
	 * @param e Node
	 * @return 成功返回真，否则假
	 */
	public boolean addSite(Node e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批站点地址
	 * @param a Node数组
	 * @return 返回新增成员数目
	 */
	public int addSites(List<Node> a) {
		int size = array.size();
		for (Node e : a) {
			addSite(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批站点地址
	 * @param a Node数组
	 * @return 返回新增成员数目
	 */
	public int addSites(Node[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			addSite(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部站点地址
	 * @return Node列表
	 */
	public List<Node> getSites() {
		return new ArrayList<Node>(array);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public FindSiteProduct duplicate() {
		return new FindSiteProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 查询站点标识
		writer.writeInstance(tag);
		// 相关站点记录
		writer.writeInt(array.size());
		for (Node node : array) {
			writer.writeObject(node);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 查询站点标识
		tag = reader.readInstance(FindSiteTag.class);
		// 相关站点记录
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			array.add(node);
		}
	}

}
