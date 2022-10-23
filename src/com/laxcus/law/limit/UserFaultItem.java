/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.limit;

import com.laxcus.law.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 用户锁定单元
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public final class UserFaultItem extends FaultItem {
	
	private static final long serialVersionUID = -8712863707699176809L;

	/**
	 * 生成用户锁定单元数据副本
	 * @param that UserFaultItem实例
	 */
	private UserFaultItem(UserFaultItem that) {
		super(that);
	}

	/**
	 * 构造默认的用户锁定单元
	 */
	public UserFaultItem() {
		super(LawRank.USER);
	}

	/**
	 * 从可类化数据读取器中解析用户锁定单元
	 * @param reader 可类化数据读取器
	 */
	public UserFaultItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出用户锁定单元
	 * @param reader 标记化读取器
	 */
	public UserFaultItem(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.FaultItem#duplicate()
	 */
	@Override
	public UserFaultItem duplicate() {
		return new UserFaultItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.FaultItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.limit.FaultItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		
	}

}