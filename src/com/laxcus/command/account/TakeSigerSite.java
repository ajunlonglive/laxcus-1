/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.account;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获得签名的ACCOUNT站点
 * 
 * @author scott.liang
 * @version 1.1 7/28/2018
 * @since laxcus 1.0
 */
public final class TakeSigerSite extends Command {

	private static final long serialVersionUID = -6398837352214459508L;

	/** 用户签名 **/
	private TreeSet<Siger> array = new TreeSet<Siger>();

	/**
	 * 根据传入的获得签名的ACCOUNT站点命令，生成它的数据副本
	 * @param that 获得签名的ACCOUNT站点实例
	 */
	private TakeSigerSite(TakeSigerSite that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造默认的获得签名的ACCOUNT站点命令
	 */
	public TakeSigerSite() {
		super();
	}

	/**
	 * 构造获得签名的ACCOUNT站点，保存一批用户签名
	 * @param users 用户签名集合
	 */
	public TakeSigerSite(List<Siger> users) {
		this();
		addAll(users);
	}

	/**
	 * 从可类化读取器中解析获得签名的ACCOUNT站点命令。
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public TakeSigerSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个用户签名
	 * @param e Siger实例
	 * @return 返回真或者假
	 */
	public boolean add(Siger e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 保存一批用户签名
	 * @param a Siger数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<Siger> a) {
		int size = array.size();
		for (Siger e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部用户签名
	 * @return 返回Siger列表
	 */
	public List<Siger> list() {
		return new ArrayList<Siger>(array);
	}

	/**
	 * 统计成员数目
	 * @return 返回成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeSigerSite duplicate() {
		return new TakeSigerSite(this);
	}

	/* (non-Javadoc)
	 * @since 1.1
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 写成员数目
		writer.writeInt(array.size());
		// 写成员对象
		for (Siger user : array) {
			writer.writeObject(user);
		}
	}

	/* (non-Javadoc)
	 * @since 1.1
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 读成员数目
		int size = reader.readInt();
		// 读表成员
		for (int i = 0; i < size; i++) {
			Siger user = new Siger(reader);
			array.add(user);
		}
	}

}