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
 * 刷新注册用户 <br><br>
 * 
 * 流程：WATCH -> BANK -> GATE <br>
 * 
 * 命令由WATCH站点发出，通过BANK判断中转分发，最终作用到GATE站点。GATE站点更新账号和CALL站点记录。
 * 
 * @author scott.liang
 * @version 1.1 7/2/2018
 * @since laxcus 1.0
 */
public final class RefreshUser extends RefreshResource {
	
	private static final long serialVersionUID = -7690762608712793679L;

	/**
	 * 根据传入的刷新注册用户命令，生成它的数据副本
	 * @param that 刷新注册用户命令
	 */
	private RefreshUser(RefreshUser that) {
		super(that);
	}

	/**
	 * 构造刷新注册用户命令
	 */
	public RefreshUser() {
		super();
	}
	
	/**
	 * 构造刷新注册用户命令，指定用户签名
	 * @param siger 用户签名
	 */
	public RefreshUser(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析刷新注册用户命令
	 * @param reader 可类化数据读取器
	 */
	public RefreshUser(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RefreshUser duplicate() {
		return new RefreshUser(this);
	}

}