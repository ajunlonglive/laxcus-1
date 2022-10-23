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
 * 设置一个账号最多应用软件数目 <br>
 * 
 * 这项工作只能由管理员或者等于同管理员身份的用户来实施。<br>
 * 特别注意：WATCH站点不执行这项工作。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/10/2018
 * @since laxcus 1.0
 */
public final class SetMaxTasks extends SetMultiUserParameter {
	
	private static final long serialVersionUID = -2245427709142537594L;

	/** 用户最多应用软件数 **/
	private int tasks;

	/**
	 * 根据传入的设置用户最多应用软件数命令，生成它的数据副本
	 * @param that 设置用户最多应用软件数命令
	 */
	private SetMaxTasks(SetMaxTasks that) {
		super(that);
		tasks = that.tasks;
	}

	/**
	 * 构造默认和私有的设置用户最多应用软件数命令
	 */
	private SetMaxTasks() {
		super();
	}

	/**
	 * 构造设置用户最多应用软件数，指定数目
	 * @param max 最多应用软件数目
	 */
	public SetMaxTasks(int max) {
		this();
		setTasks(max);
	}

	/**
	 * 构造设置用户最多应用软件数命令，指定用户签名
	 * @param siger 用户签名
	 */
	public SetMaxTasks(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析设置用户最多应用软件数命令
	 * @param reader 可类化数据读取器
	 */
	public SetMaxTasks(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置最多应用软件数
	 * @param i 最多应用软件数
	 */
	public void setTasks(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		tasks = i;
	}

	/**
	 * 返回最多应用软件数
	 * @return 最多应用软件数
	 */
	public int getTasks() {
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.access.user.SetMultiUserParameter#split()
	 */
	@Override
	public List<SetSingleUserParameter> split() {
		ArrayList<SetSingleUserParameter> array = new ArrayList<SetSingleUserParameter>();
		for (Siger siger : users) {
			SetSingleMaxTasks e = new SetSingleMaxTasks(siger, tasks);
			array.add(e);
		}
		return array;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetMaxTasks duplicate() {
		return new SetMaxTasks(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(tasks);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		tasks = reader.readInt();
	}
}