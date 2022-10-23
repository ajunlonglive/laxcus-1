/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import java.io.Serializable;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 权限表<br><br>
 * 
 * 每个权限表针对一个账号。从高到低分为三个级别：用户级、数据库级、表级。
 * 每一个级别有多个操作选项，部分选项会在多个级别存在。这些同质选项，根据它所属级别有高低划分。如SELECT，用户SELECT高于数据库SELECT，数据库SELECT又高于表SELECT。
 * 不同的操作选项，它们是独立的，之间没有任何关联。
 * 比如获得了“用户级”的建立数据库权力（CREATE SCHEMA），不等于拥有“表级”的查询权力（SELECT）
 * 要获得操作权力，必须得到授权。<br><br>
 * 
 * 授权由管理员（DBA）或者等同管理员身份的用户授予，用户不能给自己授权，撤销授权也是。<br><br>
 * 
 * 另外，管理员拥有管理权，没有数据操作权，管理权是授予和撤销，操作权是对实施具体工作。操作权包括：“CREATE SCHEMA、CREATE TABLE、SELECT/INSERT/DELETE/UPDATE”。
 * 即管理员可以给用户授权，但是对建库/建表/数据处理的权力，系统是禁止的，因为这可能引起产生权的混乱。删除用户的数据库和表的权力被允许（DROP DATABASE、DROP TABLE）。<br><br>
 * 
 * 
 * @author scott.liang
 * @version 1.1 6/23/2015
 * @since laxcus 1.0
 */
public abstract class Permit implements Classable, Markable, Serializable, Cloneable {

	private static final long serialVersionUID = -1955713602302521555L;

	/** 当前权限表类型 **/
	private int family;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 权限表类型
		writer.writeInt(family);
		// 写入子类信息
		buildSuffix(writer);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 权限表类型
		family = reader.readInt();
		// 解析子类信息
		resolveSuffix(reader);
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的权限表实例，生成它的副本
	 * @param that 权限表实例
	 */
	protected Permit(Permit that) {
		super();
		family = that.family;
	}

	/**
	 * 构造一个默认和私有的权限表
	 */
	private Permit() {
		super();
		family = 0;
	}

	/**
	 * 构造权限表，指定权限表的类型（只能由子类调用这个方法）
	 * @param family  权限表类型
	 */
	protected Permit(int family) {
		this();
		setFamily(family);
	}

	/**
	 * 设置权限表类型
	 * @param who 权限表类型
	 */
	public void setFamily(int who) {
		if(!PermitTag.isFamily(who)) {
			throw new IllegalValueException("illegal rank %d" , who);
		}
		family = who;
	}

	/**
	 * 返回权限表类型
	 * @return 权限表类型
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 判断是用户级操作权限表
	 * @return 返回真或者假
	 */
	public boolean isUserPermit() {
		return PermitTag.isUserPermit(family);
	}

	/**
	 * 判断是数据库级操作权限表
	 * @return 返回真或者假
	 */
	public boolean isSchemaPrimit() {
		return PermitTag.isSchemaPrimit(family);
	}

	/**
	 * 判断是表级操作权限表
	 * @return 返回真或者假
	 */
	public boolean isTablePermit() {
		return PermitTag.isTablePermit(family);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 子类根据自己的配置，生成它的权限表
	 * @return  Permit子类实例
	 */
	public abstract Permit duplicate();

	/**
	 * 增加一批操作权限
	 * @param e 权限表实例
	 * @return 成功返回真，否则假
	 */
	public abstract boolean add(Permit e);

	/**
	 * 删除一批操作权限
	 * @param e 权限表实例
	 * @return 删除成功返回真，否则假
	 */
	public abstract boolean remove(Permit e);

	/**
	 * 将子类参数写入可类化存储器，返回写入的字节长度
	 * @param writer
	 */
	public abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化读取器中解析子类参数，返回读取的字节长度
	 * @param reader
	 */
	public abstract void resolveSuffix(ClassReader reader) ;

}
