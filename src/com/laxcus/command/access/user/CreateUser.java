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
 * 建立账号命令。<br><br>
 * 
 * 命令格式：<br>
 * 1. CREATE USER username password 'XXX' <br>
 * 2. CREATER USER username identified by 'XXX' <br> 
 * 3. CREATE USER username password='XXX' <br>
 * 后缀选项： [MAXSIZE={digit}[M|G|T|P]] [MEMBERS={digit}] [MAX TABLE={digit}] [DEPLOY TO Gate://{ip}:{port}_{port}] <br><br>
 * 
 * 用户名最少6个字符。<br><br>
 * 
 * 处理流程：<br>
 * 1. FRONT 发起 "CREATE USER" 命令到GATE <br>
 * 2. TOP受理和检查参数，错误退出；成功，保存账号，发送SHIFT USER命令到BANK/ACCOUNT <br>
 * 3. BANK受理，返回一个ENTRANCE网关（公网）地址 <br>
 * 4. GATE把ENTRANCE网关地址反馈给FRONT <br>
 * 
 * @author scott.liang
 * @version 1.1 3/29/2015
 * @since laxcus 1.0
 */
public final class CreateUser extends ProcessUser {

	private static final long serialVersionUID = 1620140982682383710L;
	
	/** 用户名称的明文，不做可类化处理 **/
	private String plaintext;

	/**
	 * 构造默认和私有的建立账号命令。
	 */
	private CreateUser() {
		super();
	}

	/**
	 * 根据传入的建立账号命令实例，生成它的数据副本
	 * @param that CreateUser实例
	 */
	private CreateUser(CreateUser that) {
		super(that);
		plaintext = that.plaintext;
	}

	/**
	 * 构造建立账号命令，指定用户账号
	 * @param user User实例
	 */
	public CreateUser(User user) {
		this();
		setUser(user);
	}

	/**
	 * 从可类化读取器中解析建立账号命令
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public CreateUser(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置明文
	 * @param e
	 */
	public void setPlainText(String e) {
		plaintext = e;
	}

	/**
	 * 返回明文
	 * @return
	 */
	public String getPlainText() {
		return plaintext;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CreateUser duplicate() {
		return new CreateUser(this);
	}

	/**
	 * 将用户账号参数写入可类化数据存储器
	 * @since 1.1
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上一层参数
		super.buildSuffix(writer);
	}

	/**
	 * 从可类化数据读取器中解析用户账号参数
	 * @since 1.1
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上一层参数
		super.resolveSuffix(reader);
	}
}