/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 投递注册成员到WATCH节点，包括TOP.WATCH, HOME.WATCH, BANK.WATCH三类节点。
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public abstract class CastRegisterMember extends Command {

	private static final long serialVersionUID = -1789069066711775256L;

	/** 用户基点集合 **/
	private TreeSet<Seat> array = new TreeSet<Seat>();

	/**
	 * 构造默认的投递注册成员到WATCH节点
	 */
	protected CastRegisterMember() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析投递注册成员到WATCH节点
	 * @param reader 可类化数据读取器
	 */
	protected CastRegisterMember(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成投递注册成员到WATCH节点处理结果的数据副本
	 * @param that 投递注册成员到WATCH节点处理结果
	 */
	protected CastRegisterMember(CastRegisterMember that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 增加一个用户基点，不允许空指针
	 * @param e 用户基点
	 * @return 成功返回真，否则假
	 */
	public boolean add(Seat e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 增加一个用户基点，参数不允许空指针
	 * @param siger 用户签名
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean add(Siger siger, Node node) {
		return add(new Seat(siger, node));
	}

	/**
	 * 保存一批用户基点
	 * @param a 用户基点数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Seat> a) {
		int size = array.size();
		for (Seat e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批用户基点
	 * @param e 实例
	 * @return 返回新增成员数目
	 */
	public int addAll(CastRegisterMember e) {
		return addAll(e.array);
	}
	
	/**
	 * 根据用户签名，检索匹配地站点地址
	 * @param siger 用户签名
	 * @return 返回区域的用户基点列表
	 */
	public List<Seat> find(Siger siger) {
		ArrayList<Seat> a = new ArrayList<Seat>();

		for (Seat e : array) {
			if (Laxkit.compareTo(e.getSiger(), siger) == 0) {
				a.add(e);
			}
		}

		return a;
	}

	/**
	 * 输出全部用户基点
	 * @return 用户基点列表
	 */
	public List<Seat> list() {
		return new ArrayList<Seat>(array);
	}

	/**
	 * 输出用户基点成员数目
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
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Seat e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Seat e = new Seat(reader);
			array.add(e);
		}
	}

}