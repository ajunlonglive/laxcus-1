/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front;

import com.laxcus.util.net.*;

/**
 * 登录状态
 * @author scott.liang
 * @version 1.0 12/2/2018
 * @since laxcus 1.0
 */
class EntryStatus {

	/**
	 * 结果码
	 */
	private int family;

	/**
	 * 重定义主机
	 */
	private SiteHost redirect;

	/**
	 * 设置登录状态，指定站点类型
	 * @param family
	 */
	public EntryStatus(int family) {
		super();
		setFamily(family);
	}

	/**
	 * 设置登录状态，指定站点类型和重定向地址
	 * @param family
	 * @param redirect
	 */
	public EntryStatus(int family, SiteHost redirect) {
		this(family);
		setRedirect(redirect);
	}

	/**
	 * 设置站点类型
	 * @param who 站点类型
	 */
	public void setFamily(int who) {
		family = who;
	}

	/**
	 * 返回站点类型
	 * @return 站点类型
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 判断成功
	 * @return
	 */
	public boolean isSuccessful() {
		return FrontEntryFlag.isSuccessful(family);
	}

	/**
	 * 设置重定向站点
	 * @param e SiteHost实例
	 */
	public void setRedirect(SiteHost e) {
		redirect = e;
	}

	/**
	 * 返回重定向站点
	 * @return SiteHost实例
	 */
	public SiteHost getRedirect() {
		return redirect;
	}
}
