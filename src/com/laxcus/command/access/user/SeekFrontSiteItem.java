/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * FRONT站点分布结果单元
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class SeekFrontSiteItem extends SeekUserSiteItem {
	
	private static final long serialVersionUID = 7008461051980633272L;

	/** FRONT站点数组 **/
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 构造默认的FRONT站点分布结果单元
	 */
	public SeekFrontSiteItem() {
		super();
	}

	/**
	 * 生成FRONT站点分布结果单元的数据副本
	 * @param that FRONT站点分布结果单元
	 */
	private SeekFrontSiteItem(SeekFrontSiteItem that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造FRONT站点分布结果单元，指定用户基点
	 * @param seat 用户基点
	 */
	public SeekFrontSiteItem(Seat seat) {
		super(seat);
	}
	
	/**
	 * 构造FRONT站点分布结果单元，指定基础参数
	 * @param siger 用户签名
	 * @param site GATE站点地址
	 */
	public SeekFrontSiteItem(Siger siger, Node site) {
		super(siger, site);
	}

	/**
	 * 从可类化数据读取器中解析FRONT站点分布结果单元
	 * @param reader 可类化数据读取器
	 */
	public SeekFrontSiteItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存FRONT站点，不允许空指针
	 * @param e FRONT站点实例
	 * @return 成功返回真，否则假
	 */
	public boolean addFront(Node e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 保存一批FRONT站点
	 * @param a FRONT站点列表
	 * @return 返回新增成员数目
	 */
	public int addFronts(Collection<Node> a) {
		int size = array.size();
		for (Node e : a) {
			addFront(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部FRONT站点
	 * @return FRONT站点列表
	 */
	public List<Node> getFronts() {
		return new ArrayList<Node>(array);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.SeekUserItem#duplicate()
	 */
	@Override
	public SeekFrontSiteItem duplicate() {
		return new SeekFrontSiteItem(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.MultiUser#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 被检索站点标记
		writer.writeInt(array.size());
		for (Node e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.MultiUser#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 被检索站点标记
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			array.add(e);
		}
	}

}