/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.login;

import com.laxcus.log.client.*;
import com.laxcus.ray.*;
import com.laxcus.site.watch.*;
import com.laxcus.util.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.net.*;

/**
 * 登录线程
 * 
 * @author scott.liang
 * @version 1.0 5/21/2021
 * @since laxcus 1.0
 */
class RayLoginThread implements Runnable {

	/** 窗口句柄 **/
	private RayLoginDialog dialog;

	/** 账号的用户名和密码 **/
	private String username;

	private String password;

	/** 服务器地址 **/
	private String host;

	private String port;

	/** 线程句柄 **/
	private Thread thread;
	
	/**
	 * 构造登录线程，指定句柄
	 * @param e
	 */
	public RayLoginThread(RayLoginDialog e) {
		super();
		dialog = e;
	}
	
	/**
	 * 设置明文格式的注册用户账号
	 * @param name 用户名称
	 * @param pwd 密码
	 */
	public void setUser(String name, String pwd) {
		username = name;
		password = pwd;
	}
	
	/**
	 * 设置登录服务器主机地址和端口
	 * @param h
	 * @param p
	 */
	public void setHub(String h, String p){
		host = h;
		port = p;
	}

	/**
	 * 生成主机地址
	 * @return
	 */
	private SiteHost createHub() {
		try {
			Address address = new Address(host);
			int iPort = Integer.parseInt(port);
			return new SiteHost(address, iPort, iPort);
		} catch (Exception e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 启动线程
	 */
	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// 分配追踪器句柄
		RayTrackDialog tracker = new RayTrackDialog(dialog, true);

		SiteHost hub = createHub();
		if (hub == null) {
			dialog.showLoginFailed(host, port, RayEntryFlag.CHECK_FAULT, 0, null);
			thread = null;
			return;
		}

		// 设置用户名和密码
		WatchSite local = RayLauncher.getInstance().getSite();

		// 判断是SHA数字，或者是明文
		if (Siger.validate(username) && SHA512Hash.validate(password)) {
			// 生成签名
			local.setUser(new Siger(username), new SHA512Hash(password));
		} else {
			local.setUser(username, password);
		}

		// 启动登录
		int who = RayLauncher.getInstance().login(hub, false, tracker);
		boolean success = RayEntryFlag.isSuccessful(who);
		// 成功或者失败！
		if (success) {
			dialog.destroy();
		} else {
			// 显示错误！
			dialog.showLoginFailed(host, port, who, tracker.getPitchId(), tracker.getPitchHub());
		}
		thread = null;
	}

}