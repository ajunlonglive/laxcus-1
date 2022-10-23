/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.access.diagram.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 判断账号存在命令。
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class AssertUser extends ProcessSiger {

	private static final long serialVersionUID = -9072719515941667946L;

	/** 用户明文 **/
	private String usernameText;

	/**
	 * 构造默认和私有的判断账号命令
	 */
	private AssertUser() {
		super();
	}

	/**
	 * 根据传入的判断账号命令，生成它的数据副本
	 * @param that AssertUser实例
	 */
	private AssertUser(AssertUser that) {
		super(that);
		usernameText = that.usernameText;
	}

	/**
	 * 构造判断账号命令，设置用户签名
	 * @param username 用户签名
	 */
	public AssertUser(Siger username) {
		this();
		setUsername(username);
	}

	/**
	 * 构造判断账号命令，设置用户明文
	 * @param username 用户明文
	 */
	public AssertUser(String username) {
		this(SHAUser.doUsername(username));
		setUsernameText(username);
	}

	/**
	 * 从可类化读取器中解析判断账号命令参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public AssertUser(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据当前判断账号命令，生成它的数据副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AssertUser duplicate() {
		return new AssertUser(this);
	}

	/**
	 * 设置用户明文
	 * @param e
	 */
	public void setUsernameText(String e) {
		usernameText = e;
	}

	/**
	 * 返回用户明文
	 * @return
	 */
	public String getUsernameText() {
		return usernameText;
	}
	
	/**
	 * 将被处理的账号名称写入可类化存储器
	 * @since 1.1
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeString(usernameText);
	}

	/**
	 * 从可类化读取器中解析被处理的账号名称
	 * @since 1.1
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		usernameText = reader.readString();
	}
}
