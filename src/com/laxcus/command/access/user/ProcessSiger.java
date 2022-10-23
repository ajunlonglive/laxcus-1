/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 处理用户签名命令 <br><br>
 * 
 * 只处理用户签名，不包括用户密码。
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public abstract class ProcessSiger extends Command {

	private static final long serialVersionUID = 420469781822073550L;
	
	/** 注册账号的用户签名 **/
	private Siger username;

	/**
	 * 构造处理用户签名命令
	 */
	protected ProcessSiger() {
		super();
	}

	/**
	 * 根据传入的处理用户签名命令实例，生成它的数据副本
	 * @param that ProcessSiger实例
	 */
	protected ProcessSiger(ProcessSiger that) {
		super(that);
		username = that.username;
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
		return this.username;
	}

	/**
	 * 将被处理的账号名称写入可类化存储器
	 * @since 1.1
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(username);
	}

	/**
	 * 从可类化读取器中解析被处理的账号名称
	 * @since 1.1
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		username = new Siger(reader);
	}

}