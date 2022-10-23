/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.dict;

/**
 * 账号框架 <br>
 * 描述一个账号的位置。
 * 
 * @author scott.liang
 * @version 1.0 7/4/2018
 * @since laxcus 1.0
 */
final class AccountFrame {

	/** 账号坐标 **/
	AccountDock dock;

	/** 账号管理器 **/
	AccountManager manager;

	/**
	 * 构造账号框架，指定参数
	 * @param dock 账号坐标
	 * @param manager 账号管理器
	 */
	public AccountFrame(AccountDock dock, AccountManager manager) {
		super();
		setDock(dock);
		setManager(manager);
	}

	/**
	 * 设置账号坐标
	 * @param e 账号坐标
	 */
	public void setDock(AccountDock e) {
		dock = e;
	}

	/**
	 * 返回账号坐标
	 * @return 账号坐标
	 */
	public AccountDock getDock() {
		return dock;
	}

	/**
	 * 设置账号管理器
	 * @param e 账号管理器
	 */
	public void setManager(AccountManager e) {
		manager = e;
	}

	/**
	 * 返回账号管理器
	 * @return 账号管理器
	 */
	public AccountManager getManager() {
		return manager;
	}
}
