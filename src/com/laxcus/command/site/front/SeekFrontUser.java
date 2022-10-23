/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import com.laxcus.command.access.user.*;
import com.laxcus.util.classable.*;

/**
 * 检索基于用户签名的用户登录信息。<br><br>
 * 
 * 依据用户签名，获取FRONT用户的登录信息。<br>
 * 管理员登录到BANK/HOME子域集群后才能使用。<br><br>
 * 
 * 语法：SEEK SIGN USER 用户名称
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class SeekFrontUser extends MultiUser {

	private static final long serialVersionUID = -3416080952951813570L;

	/**
	 * 构造默认的检索用户签名的用户登录信息
	 */
	public SeekFrontUser() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析检索基于用户签名的用户登录信息
	 * @param reader 可类化数据读取器
	 */
	public SeekFrontUser(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 检索基于用户签名的用户登录信息的数据副本
	 * @param that 检索基于用户签名的用户登录信息
	 */
	private SeekFrontUser(SeekFrontUser that) {
		super(that);
	}

	/**
	 * 判断要求显示全部用户
	 * @return 返回真或者假
	 */
	public boolean isAllUser() {
		return getUsers().size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SeekFrontUser duplicate() {
		return new SeekFrontUser(this);
	}

}