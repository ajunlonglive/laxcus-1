/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 设置一个用户的最大并行任务数。
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class SetSingleMaxJobs extends SetSingleUserParameter {

	private static final long serialVersionUID = 5504350164714024747L;

	/** 用户最大并行任务数数 **/
	private int jobs;

	/**
	 * 根据传入的设置最大并行任务数命令，生成它的数据副本
	 * @param that 设置最大并行任务数命令
	 */
	private SetSingleMaxJobs(SetSingleMaxJobs that) {
		super(that);
		jobs = that.jobs;
	}

	/**
	 * 构造默认和私有的设置最大并行任务数命令
	 */
	private SetSingleMaxJobs() {
		super();
	}

	/**
	 * 构造设置最大并行任务数，指定数目
	 * @param siger 用户签名
	 * @param max 最大并行任务数数目
	 */
	public SetSingleMaxJobs(Siger siger, int max) {
		this();
		setSiger(siger);
		setJobs(max);
	}

	/**
	 * 从可类化数据读取器中解析设置最大并行任务数命令
	 * @param reader 可类化数据读取器
	 */
	public SetSingleMaxJobs(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置最大并行任务数数
	 * @param i 最大并行任务数数
	 */
	public void setJobs(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		jobs = i;
	}

	/**
	 * 返回最大并行任务数数
	 * @return 最大并行任务数数
	 */
	public int getJobs() {
		return jobs;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetSingleMaxJobs duplicate() {
		return new SetSingleMaxJobs(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(jobs);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		jobs = reader.readInt();
	}

}