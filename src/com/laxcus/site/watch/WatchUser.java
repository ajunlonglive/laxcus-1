/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.watch;

import com.laxcus.access.diagram.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * WATCH登录用户
 * 
 * @author scott.liang
 * @version 1.1 8/1/2015
 * @since laxcus 1.0
 */
public final class WatchUser extends SHAUser {

	private static final long serialVersionUID = -7583704925570918375L;

	/**
	 * 构造WATCH登录用户
	 */
	private WatchUser() {
		super();
	}

	/**
	 * 生成WATCH登录用户数据副本
	 * @param that WatchUser实例
	 */
	private WatchUser(WatchUser that) {
		super(that);
	}

	/**
	 * 构造WATCH登录用户，指定用户名和密码
	 * @param username 用户签名
	 * @param password SHA512散列码
	 */
	public WatchUser(Siger username, SHA512Hash password) {
		this();
		setUsername(username);
		setPassword(password);
	}

	/**
	 * 构造WATCH登录用户，指定用户名和密码
	 * @param username 明文文本
	 * @param password 明文文本
	 */
	public WatchUser(String username, String password) {
		super(username, password);
	}
	
	/**
	 * 从可类化读取器中解析WATCH登录用户
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public WatchUser(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.SHAUser#duplicate()
	 */
	@Override
	public WatchUser duplicate() {
		return new WatchUser(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.SHAUser#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.SHAUser#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		// TODO Auto-generated method stub

	}

}
