/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.watch;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * WATCH站点。<br><br>
 * 
 * WATCH站点是监视HOME/TOP集群注册站点的站点。
 * 采用账号登录的方式注册到HOME/TOP站点下，以图形界面在客户端展开，
 * 当集群中有站点登录、注销、发生故障的时候，这些消息会传递到WATCH站点，
 * 用文字、声音、图像等多种方式通知集群管理员。为集群管理员提供可视化的管理服务。<br><br>
 * 
 * 通过WATCH站点，集群管理员可以用指令的方式要求运行站点执行某些操作，比如退出和重新启动等。<br>
 * 
 * 每个TOP/HOME集群应该有最少一个WATCH站点。
 * 
 * @author scott.liang
 * @version 1.1 05/02/2015
 * @since laxcus 1.0
 */
public final class WatchSite extends Site {

	private static final long serialVersionUID = 6551655407899525116L;

	/** 登录账号 **/
	private WatchUser user;

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(user);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		user = reader.readInstance(WatchUser.class);
	}

	/**
	 * 根据传入的WATCH站点实例，生成它的数据副本
	 * @param that WatchSite实例
	 */
	private WatchSite(WatchSite that) {
		super(that);
		if (that.user != null) {
			user = that.user.duplicate();
		}
	}

	/**
	 * 构造默认的WATCH站点
	 */
	public WatchSite() {
		super(SiteTag.WATCH_SITE);
	}

	/**
	 * 从可类化数据读取中解析WATCH站点参数
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public WatchSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置登录账号，允许空值。
	 * @param e WatchUser实例
	 */
	public void setUser(WatchUser e) {
		Laxkit.nullabled(e);

		user = e.duplicate();
	}

	/**
	 * 设置登录账号
	 * @param username 明文文本
	 * @param password 明文文本
	 */
	public void setUser(String username, String password) {
		user = new WatchUser(username, password);
	}
	
	/**
	 * 设置登录账号
	 * @param username 用户签名
	 * @param password 密码签名
	 */
	public void setUser(Siger username, SHA512Hash password) {
		user = new WatchUser(username, password);
	}
	
	/**
	 * 返回WATCH登录账号
	 * @return WatchUser实例
	 */
	public WatchUser getUser() {
		return user;
	}
	
	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		if (user != null) {
			return user.getUsername();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.Site#duplicate()
	 */
	@Override
	public WatchSite duplicate() {
		return new WatchSite(this);
	}

}