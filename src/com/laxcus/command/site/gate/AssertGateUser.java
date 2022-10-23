/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.gate;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 判断某个账号在某个GATE站点上存在 <br><br>
 * 
 * @author scott.liang
 * @version 1.0 7/18/2019
 * @since laxcus 1.0
 */
public class AssertGateUser extends Command {

	private static final long serialVersionUID = 2555315333982684703L;

	/** 注册账号的用户签名 **/
	private Siger username;

	/** 判断账号来自公网 **/
	private boolean wide;

	/**
	 * 构造判断某个账号在某个GATE站点上存在
	 */
	public AssertGateUser() {
		super();
	}
	
	/**
	 * 构造判断某个账号在某个GATE站点上存在，指定用户签名
	 * @param username 用户签名
	 * @param wide 来自公网
	 */
	public AssertGateUser(Siger username, boolean wide) {
		super();
		setUsername(username);
		setWide(wide);
	}
	
	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 */
	public AssertGateUser(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的判断某个账号在某个GATE站点上存在实例，生成它的数据副本
	 * @param that AssertGateUser实例
	 */
	private AssertGateUser(AssertGateUser that) {
		super(that);
		username = that.username;
		wide = that.wide;
	}

	/**
	 * 设置检查的账号用户名
	 * @param e Siger实例
	 */
	public void setUsername(Siger e) {
		Laxkit.nullabled(e);
		
		username = e;
	}

	/**
	 * 返回检查的账号用户名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return username;
	}
	
	/**
	 * 定义为公网地址
	 * @param b 真或者假
	 */
	public void setWide(boolean b) {
		wide = b;
	}
	
	/**
	 * 判断是公网地址
	 * @return 真或者假
	 */
	public boolean isWide(){
		return wide;
	}

	/**
	 * 将被处理的账号名称写入可类化存储器
	 * @since 1.1
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(username);
		writer.writeBoolean(wide);
	}

	/**
	 * 从可类化读取器中解析被处理的账号名称
	 * @since 1.1
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		username = new Siger(reader);
		wide = reader.readBoolean();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AssertGateUser duplicate() {
		return new AssertGateUser(this);
	}

}