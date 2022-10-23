/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 用户资源引用。<br>
 * 用户资源引用建立用户基础资料上，增加记录用户的数据表，快捷组件。
 * 
 * @author scott.liang
 * @version 1.3 2/17/2016
 * @since laxcus 1.0
 */
public final class Refer extends Audit {

	private static final long serialVersionUID = 8152052416443489374L;

	/** 本地数据表名集合，被所属站点使用和存在。 **/
	private TreeSet<Space> localTables = new TreeSet<Space>();

	/**
	 * 根据传入的用户资源引用，生成它的数据副本
	 * @param that 用户资源引用
	 */
	private Refer(Refer that) {
		super(that);
		localTables.addAll(that.localTables);
	}

	/**
	 * 构造一个默认的用户资源引用
	 */
	public Refer() {
		super();
	}

	/**
	 * 根据用户账号，生成用户资源引用
	 * @param account 用户账号
	 */
	public Refer(Account account) {
		super(account);
		localTables.addAll(account.getSpaces());
	}

	/**
	 * 从可类化数据读取器中解析用户资源引用参数
	 * @param reader 可类化读取器
	 * @since 1.3
	 */
	public Refer(ClassReader reader) {
		this(); // 先初始化当前以及上级的参数
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出用户资源引用参数
	 * @param reader 标记化读取器
	 */
	public Refer(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 构造用户资源引用，指定账号
	 * @param user 用户账号
	 */
	public Refer(User user) {
		this();
		setUser(user);
	}
	
	/**
	 * 构造用户资源引用，指定用户签名
	 * @param siger 用户签名
	 */
	public Refer(Siger siger) {
		this(new User(siger));
	}

	/**
	 * 保存数据表名
	 * @param e 数据表名
	 * @return 保存成功返回真，否则假
	 */
	public boolean addTable(Space e) {
		if (e != null) {
			return localTables.add(e);
		}
		return false;
	}

	/**
	 * 删除数据表名
	 * @param e 数据表名
	 * @return 删除成功返回真，或者假
	 */
	public boolean removeTable(Space e) {
		Laxkit.nullabled(e);
		// 删除表名
		return localTables.remove(e);
	}

	/**
	 * 返回数据表名集合
	 * @return 数据表名列表
	 */
	public List<Space> getTables() {
		return new ArrayList<Space>(localTables);
	}

	/**
	 * 判断表存在
	 * @param e 数据表名
	 * @return 返回真或者假
	 */
	public boolean hasTable(Space e) {
		if (e != null) {
			return localTables.contains(e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Audit#duplicate()
	 */
	@Override
	public Refer duplicate() {
		return new Refer(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Audit#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		// 保存数据表名
		writer.writeInt(localTables.size());
		for (Space e : localTables) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Audit#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		// 解析数据表名
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space e = new Space(reader);
			localTables.add(e);
		}
	}

}