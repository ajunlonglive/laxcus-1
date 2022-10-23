/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.entrance;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获得授权人账号注册地址（GATE站点）。<br>
 * 
 * 这是TakeAuthorizerSite命令的返回结果。由ENTRANCE反馈给FRONT站点。
 * 
 * @author scott.liang
 * @version 1.0 8/3/2017
 * @since laxcus 1.0
 */
public class TakeAuthorizerSiteProduct extends EchoProduct {

	private static final long serialVersionUID = 4698046986305794269L;

	/** 授权人注册站点集合 **/
	private TreeSet<AuthorizerItem> array = new TreeSet<AuthorizerItem>();

	/**
	 * 构造默认的获得授权人账号注册地址
	 */
	public TakeAuthorizerSiteProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析获得授权人账号注册地址
	 * @param reader 可类化数据读取器
	 */
	public TakeAuthorizerSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成获得授权人账号注册地址的数据副本
	 * @param that TakeAuthorizerSiteProduct实例
	 */
	private TakeAuthorizerSiteProduct(TakeAuthorizerSiteProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个授权人注册站点
	 * @param e 授权人注册站点
	 * @return 返回真或者假
	 */
	public boolean add(AuthorizerItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一个授权人注册站点
	 * @param authorizer 授权人签名
	 * @param site GATE站点
	 * @return 返回真或者假
	 */
	public boolean add(Siger authorizer, Node site) {
		AuthorizerItem e = new AuthorizerItem(authorizer, site);
		return add(e);
	}

	/**
	 * 保存一批授权人注册站点
	 * @param a 授权人注册站点
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<AuthorizerItem> a) {
		int size = array.size();
		array.addAll(a);
		return array.size() - size;
	}

	/**
	 * 删除一个授权人注册站点
	 * @param e 授权人注册站点
	 * @return 返回真或者假
	 */
	public boolean remove(AuthorizerItem e) {
		return array.remove(e);
	}

	/**
	 * 检查一个账号用户名是否在允许范围内。
	 * @param e 账号用户名。
	 * @return 允许返回true，否则返回false。
	 */
	public boolean contains(AuthorizerItem e) {		
		return array.contains(e);
	}

	/**
	 * 返回授权人注册站点列表
	 * @return AuthorizerItem列表
	 */
	public List<AuthorizerItem> list() {
		return new ArrayList<AuthorizerItem>(array);
	}

	/**
	 * 返回授权人注册站点的数量
	 * @return 统计数量
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 如果没有授权人注册站点，返回true；否则返回false。
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.EchoProduct#duplicate()
	 */
	@Override
	public TakeAuthorizerSiteProduct duplicate() {
		return new TakeAuthorizerSiteProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (AuthorizerItem e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			AuthorizerItem e = new AuthorizerItem(reader);
			array.add(e);
		}
	}

}