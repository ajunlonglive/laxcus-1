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
 * 关闭用户账号命令。调用这个命令，用户账号将被禁用。<br><br>
 * 
 * 命令格式：<br>
 * 1. CLOSE USER 用户签名 <br>
 * 2. CLOSE USER SIGN SHA256签名 <br>
 * 
 * 流程：<br>
 * GATE -> ACCOUNT -> GATE -> BANK(GATE/HASH) -> TOP -> HOME(MULTI) -> DATA/CALL/WORK/BUILD
 * 
 * @author scott.liang
 * @version 1.0 1/4/2020
 * @since laxcus 1.0
 */
public class CloseUser extends ProcessSiger {

	private static final long serialVersionUID = 6299002982315456934L;

	/** 用户名称的明文，不做可类化处理 **/
	private String plaintext;

	/**
	 * 构造默认和私有的关闭用户账号命令
	 */
	private CloseUser() {
		super();
	}

	/**
	 * 根据传入的关闭用户账号命令，生成它的数据副本
	 * @param that CloseUser实例
	 */
	private CloseUser(CloseUser that) {
		super(that);
		plaintext = that.plaintext;
	}

	/**
	 * 构造关闭用户账号命令，设置用户签名
	 * @param siger Siger实例
	 */
	public CloseUser(Siger siger) {
		this();
		setUsername(siger);
	}
	
	/**
	 * 从可类化读取器中解析关闭用户账号命令参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public CloseUser(ClassReader reader) {
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
	 * 根据当前关闭用户账号命令，生成它的数据副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CloseUser duplicate() {
		return new CloseUser(this);
	}

}