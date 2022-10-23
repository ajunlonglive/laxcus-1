/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.forbid;

import com.laxcus.law.*;
import com.laxcus.util.classable.*;

/**
 * 用户级禁止操作单元
 * 
 * @author scott.liang
 * @version 1.0 3/31/2017
 * @since laxcus 1.0
 */
public final class UserForbidItem extends ForbidItem {
	
	private static final long serialVersionUID = -901339785818599031L;

	/**
	 * 生成用户级禁止操作单元数据副本
	 * @param that UserForbidItem实例
	 */
	private UserForbidItem(UserForbidItem that) {
		super(that);
	}

	/**
	 * 构造用户级禁止操作单元
	 */
	public UserForbidItem() {
		super(LawRank.USER);
	}

	/**
	 * 从可类化数据读取器中解析用户级禁止操作单元
	 * @param reader 可类化数据读取器
	 */
	public UserForbidItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 用户级，与同级、子级都存在冲突
	 * @see com.laxcus.law.forbid.ForbidItem#conflict(com.laxcus.law.forbid.ForbidItem)
	 */
	@Override
	public boolean conflict(ForbidItem that) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.account.forbid.ForbidItem#duplicate()
	 */
	@Override
	public ForbidItem duplicate() {
		return new UserForbidItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.account.forbid.ForbidItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.account.forbid.ForbidItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		
	}

}