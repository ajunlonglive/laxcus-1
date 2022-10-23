/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 删除用户账号命令。<br><br>
 * 
 * 命令格式：<br>
 * 1. DROP USER 用户签名 <br>
 * 2. DROP USER SIGN SHA256签名 <br>
 * 
 * 流程：<br>
 * GATE -> ACCOUNT -> GATE -> BANK(GATE/HASH) -> TOP -> HOME(MULTI) -> DATA/CALL/WORK/BUILD
 * 
 * @author scott.liang
 * @version 1.2 7/5/2018
 * @since laxcus 1.0
 */
public class DropUser extends ProcessSiger {

	private static final long serialVersionUID = 1809700749960314389L;
	
	/** 用户名称的明文，不做可类化处理 **/
	private String plaintext;

	/**
	 * 构造默认和私有的删除用户账号命令
	 */
	private DropUser() {
		super();
	}

	/**
	 * 根据传入的删除用户账号命令，生成它的数据副本
	 * @param that DropUser实例
	 */
	private DropUser(DropUser that) {
		super(that);
		plaintext = that.plaintext;
	}

	/**
	 * 构造删除用户账号命令，设置用户签名
	 * @param siger Siger实例
	 */
	public DropUser(Siger siger) {
		this();
		setUsername(siger);
	}
	
	/**
	 * 从可类化读取器中解析删除用户账号命令参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public DropUser(ClassReader reader) {
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

	/**
	 * 根据当前删除用户账号命令，生成它的数据副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropUser duplicate() {
		return new DropUser(this);
	}

}
