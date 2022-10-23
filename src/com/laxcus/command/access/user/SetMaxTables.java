/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;


import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 设置一个账号最大表数目 <br>
 * 
 * 这项工作只能由管理员或者等于同管理员身份的用户来实施。<br>
 * 特别注意：WATCH站点不执行这项工作。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/24/2018
 * @since laxcus 1.0
 */
public final class SetMaxTables extends SetMultiUserParameter {
	
	private static final long serialVersionUID = 1866393728358093419L;

	/** 用户最大表数目 **/
	private int tables;

	/**
	 * 根据传入的设置用户最大表数目命令，生成它的数据副本
	 * @param that 设置用户最大表数目命令
	 */
	private SetMaxTables(SetMaxTables that) {
		super(that);
		tables = that.tables;
	}

	/**
	 * 构造默认和私有的设置用户最大表数目命令
	 */
	private SetMaxTables() {
		super();
	}

	/**
	 * 构造设置用户最大表数目，指定数目
	 * @param max 最大表数目目
	 */
	public SetMaxTables(int max) {
		this();
		setTables(max);
	}

	/**
	 * 构造设置用户最大表数目命令，指定用户签名
	 * @param siger 用户签名
	 */
	public SetMaxTables(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析设置用户最大表数目命令
	 * @param reader 可类化数据读取器
	 */
	public SetMaxTables(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置用户最大表数目
	 * @param i 用户最大表数目
	 */
	public void setTables(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		tables = i;
	}

	/**
	 * 返回用户最大表数目
	 * @return 用户最大表数目
	 */
	public int getTables() {
		return tables;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.access.user.SetMultiUserParameter#split()
	 */
	@Override
	public List<SetSingleUserParameter> split() {
		ArrayList<SetSingleUserParameter> array = new ArrayList<SetSingleUserParameter>();
		for (Siger siger : users) {
			SetSingleMaxTables e = new SetSingleMaxTables(siger, tables);
			array.add(e);
		}
		return array;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetMaxTables duplicate() {
		return new SetMaxTables(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(tables);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		tables = reader.readInt();
	}
}