/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.account;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.set.*;

/**
 * 获得签名的ACCOUNT站点的处理结果
 * 
 * @author scott.liang
 * @version 1.1 7/28/2018
 * @since laxcus 1.0
 */
public class TakeSigerSiteProduct extends EchoProduct {

	private static final long serialVersionUID = -8796094924111918745L;

	/** 用户签名 -> ACCOUNT站点地址列表 **/
	private Map<Siger, NodeSet> mapUsers = new TreeMap<Siger, NodeSet>();

	/**
	 * 构造默认的获得签名的ACCOUNT站点的处理结果实例
	 */
	public TakeSigerSiteProduct() {
		super();
	}

	/**
	 * 生成传入实例的数据副本
	 * @param that 获得签名的ACCOUNT站点的处理结果实例
	 */
	private TakeSigerSiteProduct(TakeSigerSiteProduct that) {
		super(that);
		mapUsers.putAll(that.mapUsers);
	}
	
	/**
	 * 保存用户签名和ACCOUNT站点
	 * @param issuer 用户签名
	 * @param node ACCOUNT站点
	 */
	public void add(Siger issuer, Node node) {
		NodeSet set = mapUsers.get(issuer);
		if (set == null) {
			set = new NodeSet();
			mapUsers.put(issuer, set);
		}
		set.add(node);
	}

	/**
	 * 输出全部用户签名
	 * @return Siger列表
	 */
	public List<Siger> getUsers() {
		return new ArrayList<Siger>(mapUsers.keySet());
	}

	/**
	 * 查找关联的ACCOUNT站点列表
	 * @param issuer 用户签名
	 * @return 返回Node列表，或者空指针
	 */
	public List<Node> findSites(Siger issuer) {
		NodeSet set = mapUsers.get(issuer);
		if (set != null) {
			return set.list();
		}
		return null;
	}
	
	/**
	 * 统计用户数目
	 * @return 返回整数
	 */
	public int size() {
		return mapUsers.size();
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
	public TakeSigerSiteProduct duplicate() {
		return new TakeSigerSiteProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 数目
		writer.writeInt(mapUsers.size());
		// 成员
		Iterator<Map.Entry<Siger, NodeSet>> iterator = mapUsers.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Siger, NodeSet> entry = iterator.next();
			writer.writeObject(entry.getKey());
			writer.writeObject(entry.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Siger issuer = new Siger(reader);
			NodeSet set = new NodeSet(reader);
			mapUsers.put(issuer, set);
		}
	}

}