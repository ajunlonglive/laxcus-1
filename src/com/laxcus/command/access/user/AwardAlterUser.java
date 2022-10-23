/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.access.diagram.*;
import com.laxcus.util.classable.*;

/**
 * 授权修改用户账号 <br>
 * 
 * @author scott.liang
 * @version 1.1 3/29/2015
 * @since laxcus 1.0
 */
public final class AwardAlterUser extends ProcessUser {

	private static final long serialVersionUID = 7429381757727342522L;

	/**
	 * 构造默认的授权修改用户账号
	 */
	private AwardAlterUser() {
		super();
	}

	/**
	 * 根据传入的授权修改用户账号实例，生成它的浅层数据副本
	 * @param that AwardAlterUser实例
	 */
	private AwardAlterUser(AwardAlterUser that) {
		super(that);
	}

	/**
	 * 构造授权修改用户账号，指定新的账号
	 * @param user User实例
	 */
	public AwardAlterUser(User user) {
		this();
		setUser(user);
	}

	/**
	 * 从可类化数据读取器中解析授权修改用户账号参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AwardAlterUser(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AwardAlterUser duplicate() {
		return new AwardAlterUser(this);
	}

}