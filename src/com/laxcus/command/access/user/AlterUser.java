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
 * 修改账号密码。<br><br>
 * 
 * 由用户自己或者管理员来操作。<br>
 * 
 * 命令格式：<br>
 * 1. ALTER USER username IDENTIFIED BY 'XXX' <br>
 * 2. ALTER USER username PASSWORD 'XXX' <br>
 * 3. ALTER USER username PASSWORD='XXX'<br>
 * 
 * 命令流程：
 * GATE -> ACCOUNT -> GATE（生成AwardAlterUser分发给BANK） -> BANK -> (OTHER GATE) -> TOP -> HOME -> DATA/WORK/BUILD/CALL
 * 
 * @author scott.liang
 * @version 1.1 3/29/2015
 * @since laxcus 1.0
 */
public class AlterUser extends ProcessUser {

	private static final long serialVersionUID = -2244420724868448717L;

	/** 用户名称的明文，不做可类化处理 **/
	private String plaintext;

	/**
	 * 构造默认和私有的修改账号密码命令。
	 */
	private AlterUser() {
		super();
	}

	/**
	 * 根据传入的修改账号密码命令实例，生成它的数据副本
	 * @param that 修改账号密码实例
	 */
	private AlterUser(AlterUser that) {
		super(that);
		plaintext = that.plaintext;
	}

	/**
	 * 构造修改账号密码命令，指定用户账号
	 * @param user 用户账号实例
	 */
	public AlterUser(User user) {
		this();
		setUser(user);
	}

	/**
	 * 从可类化读取器中解析修改账号密码命令
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public AlterUser(ClassReader reader) {
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
	public AlterUser duplicate() {
		return new AlterUser(this);
	}

}