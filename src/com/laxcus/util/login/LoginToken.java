/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.login;

import com.laxcus.access.diagram.*;
import com.laxcus.site.*;

/**
 * 自动登录标记，包括服务器地址和账号
 * 
 * @author scott.liang
 * @version 1.0 9/19/2019
 * @since laxcus 1.0
 */
public class LoginToken {

	/** 自动显示 **/
	private boolean show;

	/** 服务器地址 **/
	private Node hub;

	/** 用户账号 **/
	private User user;

	/**
	 * 构造默认的自动登录标记
	 */
	public LoginToken() {
		super();
		setShow(false);
	}
	
	/**
	 * 构造自动登录标记
	 * @param hub
	 * @param user
	 */
	public LoginToken(Node hub, User user) {
		this();
		setHub(hub);
		setUser(user);
	}

	/**
	 * 构造自动登录标记
	 * @param show
	 * @param hub
	 * @param user
	 */
	public LoginToken(boolean show, Node hub, User user) {
		this(hub, user);
		setShow(show);
	}

	/**
	 * 显示或者否
	 * @param b
	 */
	public void setShow(boolean b) {
		show = b;
	}

	/**
	 * 判断显示或者否
	 * @return
	 */
	public boolean isShow() {
		return show;
	}

	/**
	 * 设置服务器地址
	 * @param e
	 */
	public void setHub(Node e) {
		hub = e;
	}

	/**
	 * 返回服务器地址
	 * @return
	 */
	public Node getHub() {
		return hub;
	}

	/**
	 * 设置用户账号
	 * @param e
	 */
	public void setUser(User e) {
		user = e;
	}

	/**
	 * 返回用户账号
	 * @return
	 */
	public User getUser() {
		return user;
	}

}