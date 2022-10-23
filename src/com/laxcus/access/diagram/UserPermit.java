/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import java.util.*;

import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 用户权限表
 * 
 * @author scott.liang
 * @version 1.1 7/23/2015
 * @since laxcus 1.0
 */
public final class UserPermit extends Permit {

	private static final long serialVersionUID = 7267758230940262411L;
	
	/** 操作选项 **/
	private Control set = new Control();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Permit#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeObject(set);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Permit#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		set.resolve(reader);
	}

	/**
	 * 根据传入的用户权限表实例，生成它的副本
	 * @param that 用户权限表实例
	 */
	private UserPermit(UserPermit that) {
		super(that);
		set.addAll(that.set);
	}

	/**
	 * 构造默认的用户权限表
	 */
	public UserPermit() {
		super(PermitTag.USER_PERMIT); 
	}

	/**
	 * 从可类化读取器中解析用户权限表参数
	 * @param reader  可类化读取器
	 * @since 1.1
	 */
	public UserPermit(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出用户权限表参数
	 * @param reader 标记化读取器
	 */
	public UserPermit(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 增加一批资源控制选项
	 * @param e 资源控制
	 * @return 返回增加的资源控制选项数目
	 */
	public boolean add(Control e) {
		return set.addAll(e) > 0;
	}

	/**
	 * 判断允许一个资源控制选项
	 * @param who 资源控制选项
	 * @return  返回真或者假
	 */
	public boolean allow(short who) {
		return set.allow(who);
	}

	/**
	 * 判断允许一批资源控制选项
	 * @param all  资源控制选项数组
	 * @return  全部允许返回真，否则假
	 */
	public boolean allow(short[] all) {
		int count = 0;
		for (short who : all) {
			boolean success = allow(who);
			if (success) count++;
		}
		return count == all.length;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Permit#duplicate()
	 */
	@Override
	public UserPermit duplicate() {
		return new UserPermit(this);
	}
	
	/**
	 * 返回权限列表
	 * @return 整数集合
	 */
	public List<java.lang.Short> list() {
		return set.list();
	}

	/**
	 * 判断当前用户权限表是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 返回用户权限表数目
	 * @return 返回成员数目
	 */
	public int size() {
		return set.size();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Permit#add(com.laxcus.access.diagram.Permit)
	 */
	@Override
	public boolean add(Permit e) {
		if (e == null || e.getClass() != UserPermit.class) {
			return false;
		}
		UserPermit that = (UserPermit) e;
		set.addAll(that.set);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.Permit#remove(com.laxcus.access.diagram.Permit)
	 */
	@Override
	public boolean remove(Permit e) {
		if (e == null || e.getClass() != UserPermit.class) {
			return false;
		}
		UserPermit that = (UserPermit) e;
		int count = set.removeAll(that.set);
		return (count > 0);
	}

}