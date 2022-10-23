/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.dict;

import com.laxcus.access.diagram.*;

/**
 * 账号方位
 * 
 * @author scott.liang
 * @version 1.0 7/4/2018
 * @since laxcus 1.0
 */
public final class AccountSphere {

	/** 账号坐标 **/
	Account account;
	
	/** 账号坐标 **/
	AccountDock dock;

	/** 账号框架 **/
	AccountManager manager;

	/**
	 * 构造账号方位，指定参数
	 * @param account 账号坐标
	 * @param frame 账号框架
	 */
	public AccountSphere(Account account, AccountFrame frame) {
		super();
		setAccount(account);
		setDock(frame.dock);
		setManager(frame.manager);
	}

	/**
	 * 设置账号坐标
	 * @param e 账号坐标
	 */
	public void setAccount(Account e) {
		account = e;
	}

	/**
	 * 返回账号坐标
	 * @return 账号坐标
	 */
	public Account getAccount() {
		return account;
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
	 * 设置账号框架
	 * @param e 账号框架
	 */
	public void setManager(AccountManager e) {
		manager = e;
	}

	/**
	 * 返回账号框架
	 * @return 账号框架
	 */
	public AccountManager getManager() {
		return manager;
	}

//	/**
//	 * 设置账号框架
//	 * @param e 账号框架
//	 */
//	public void setManager(AccountFrame e) {
//		manager = e;
//	}
//
//	/**
//	 * 返回账号框架
//	 * @return 账号框架
//	 */
//	public AccountFrame getManager() {
//		return manager;
//	}
}
