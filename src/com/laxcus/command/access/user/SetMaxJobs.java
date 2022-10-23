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
 * 设置账号最大并行任务数。最多并行任务数目是用户同时处理的工作数目。<br>
 * 
 * 这项工作只能由管理员或者等于同管理员身份的用户来实施。<br>
 * 特别注意：WATCH站点不执行这项工作。<br>
 * 
 * @author scott.liang
 * @version 1.0 05/07/2017
 * @since laxcus 1.0
 */
public final class SetMaxJobs extends SetMultiUserParameter {
	
	private static final long serialVersionUID = 4761932155822570007L;

	/** 用户最大并行任务数 **/
	private int jobs;

	/**
	 * 根据传入的设置一个FRONT站点用户最大并行任务数，生成它的数据副本
	 * @param that 设置一个FRONT站点用户最大并行任务数
	 */
	private SetMaxJobs(SetMaxJobs that) {
		super(that);
		jobs = that.jobs;
	}

	/**
	 * 构造默认和私有的设置一个FRONT站点用户最大并行任务数
	 */
	private SetMaxJobs() {
		super();
	}

	/**
	 * 构造设置用户最大并行任务数，指定数目
	 * @param max 最大并行任务数目
	 */
	public SetMaxJobs(int max) {
		this();
		setJobs(max);
	}

	/**
	 * 构造设置一个FRONT站点用户最大并行任务数，指定用户签名
	 * @param siger 用户签名
	 */
	public SetMaxJobs(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析设置一个FRONT站点用户最大并行任务数
	 * @param reader 可类化数据读取器
	 */
	public SetMaxJobs(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置一个FRONT站点用户最大并行任务数
	 * @param i  FRONT站点用户最大并行任务数
	 */
	public void setJobs(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		jobs = i;
	}

	/**
	 * 返回一个FRONT站点用户最大并行任务数
	 * @return FRONT站点用户最大并行任务数
	 */
	public int getJobs() {
		return jobs;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.access.user.SetMultiUserParameter#split()
	 */
	@Override
	public List<SetSingleUserParameter> split() {
		ArrayList<SetSingleUserParameter> array = new ArrayList<SetSingleUserParameter>();
		for (Siger siger : users) {
			SetSingleMaxJobs e = new SetSingleMaxJobs(siger, jobs);
			array.add(e);
		}
		return array;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetMaxJobs duplicate() {
		return new SetMaxJobs(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(jobs);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		jobs = reader.readInt();
	}
}