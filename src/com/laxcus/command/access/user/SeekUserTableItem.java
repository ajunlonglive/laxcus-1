/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 用户数据表检索结果单元
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class SeekUserTableItem extends SeekUserSiteItem {
	
	private static final long serialVersionUID = 7008461051980633272L;

	/** 数据表数组 **/
	private TreeSet<Space> array = new TreeSet<Space>();

	/**
	 * 构造默认的用户数据表检索结果单元
	 */
	public SeekUserTableItem() {
		super();
	}

	/**
	 * 生成用户数据表检索结果单元的数据副本
	 * @param that 用户数据表检索结果单元
	 */
	private SeekUserTableItem(SeekUserTableItem that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造用户数据表检索结果单元，指定用户基点
	 * @param seat 用户基点
	 */
	public SeekUserTableItem(Seat seat) {
		super(seat);
	}
	
	/**
	 * 构造用户数据表检索结果单元，指定基础参数
	 * @param siger 用户签名
	 * @param site 站点地址
	 */
	public SeekUserTableItem(Siger siger, Node site) {
		super(siger, site);
	}

	/**
	 * 从可类化数据读取器中解析用户数据表检索结果单元
	 * @param reader 可类化数据读取器
	 */
	public SeekUserTableItem(ClassReader reader) {
		this();
		super.resolve(reader);
	}

	/**
	 * 保存数据表，不允许空指针
	 * @param e 数据表实例
	 * @return 成功返回真，否则假
	 */
	public boolean addTable(Space e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 保存一批数据表
	 * @param a 数据表列表
	 * @return 返回新增成员数目
	 */
	public int addTables(Collection<Space> a) {
		int size = array.size();
		for (Space e : a) {
			addTable(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部数据表
	 * @return 数据表列表
	 */
	public List<Space> getTables() {
		return new ArrayList<Space>(array);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.SeekUserItem#duplicate()
	 */
	@Override
	public SeekUserTableItem duplicate() {
		return new SeekUserTableItem(this);
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
		for (Space e : array) {
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
			Space e = new Space(reader);
			array.add(e);
		}
	}

}