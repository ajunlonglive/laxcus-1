/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 用户处理命令 <br><br>
 * 
 * 用户账号命令包括用户签名和用户密码两个部分
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public abstract class ProcessUser extends Command {

	private static final long serialVersionUID = 1557140262652599681L;
	
	/** 用户账号 **/
	private User user;

	/**
	 * 构造默认的用户处理命令。
	 */
	protected ProcessUser() {
		super();
	}

	/**
	 * 根据传入的用户处理命令实例，生成它的数据副本
	 * @param that ProcessUser实例
	 */
	protected ProcessUser(ProcessUser that) {
		super(that);
		setUser(that.user);
	}

	/**
	 * 设置用户账号
	 * @param e User实例
	 */
	public void setUser(User e) {
		Laxkit.nullabled(e);

		user = e;
	}

	/**
	 * 返回用户账号
	 * @return User实例
	 */
	public User getUser() {
		return user;
	}

	/**
	 * 返回用户名称
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return user.getUsername();
	}

	/**
	 * 将用户账号参数写入可类化数据存储器
	 * @since 1.1
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(user);
	}

	/**
	 * 从可类化数据读取器中解析用户账号参数
	 * @since 1.1
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		user = new User(reader);
	}


}