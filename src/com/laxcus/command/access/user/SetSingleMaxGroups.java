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
 * 设置一个用户的最多子域集群数目。
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class SetSingleMaxGroups extends SetSingleUserParameter {

	private static final long serialVersionUID = 5504350164714024747L;

	/** 用户最多子域集群数目数 **/
	private int groups;

	/**
	 * 根据传入的设置最多子域集群数目命令，生成它的数据副本
	 * @param that 设置最多子域集群数目命令
	 */
	private SetSingleMaxGroups(SetSingleMaxGroups that) {
		super(that);
		groups = that.groups;
	}

	/**
	 * 构造默认和私有的设置最多子域集群数目命令
	 */
	private SetSingleMaxGroups() {
		super();
	}

	/**
	 * 构造设置最多子域集群数目，指定数目
	 * @param siger 用户签名
	 * @param max 最多子域集群数目数目
	 */
	public SetSingleMaxGroups(Siger siger, int max) {
		this();
		setSiger(siger);
		setGroups(max);
	}

	/**
	 * 从可类化数据读取器中解析设置最多子域集群数目命令
	 * @param reader 可类化数据读取器
	 */
	public SetSingleMaxGroups(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置最多子域集群数目数
	 * @param i 最多子域集群数目数
	 */
	public void setGroups(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		groups = i;
	}

	/**
	 * 返回最多子域集群数目数
	 * @return 最多子域集群数目数
	 */
	public int getGroups() {
		return groups;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetSingleMaxGroups duplicate() {
		return new SetSingleMaxGroups(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(groups);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		groups = reader.readInt();
	}

}