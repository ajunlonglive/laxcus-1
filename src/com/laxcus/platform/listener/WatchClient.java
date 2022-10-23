/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;

/**
 * WATCH监视器监听器。<br><br>
 * 这是一个用户级的监听器，在Watch Monitor启动注册，结束后注销。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/9/2022
 * @since laxcus 1.0
 */
public interface WatchClient extends PlatformListener {
	
	/**
	 * 显示运行记录
	 * @param cmd
	 */
	void showRuntime(SiteRuntime cmd);
	
	/**
	 * 推送一个注册地址
	 * @param seat
	 */
	void pushRegisterMember(Seat seat);

	/**
	 * 删除注册地址
	 * @param seat
	 */
	void dropRegisterMember(Seat seat);
	
	/**
	 * 删除注册节点
	 * @param node
	 */
	void dropRegisterMember(Node node);

	/**
	 * 增加在线成员
	 * @param seat
	 */
	void pushOnlineMember(FrontSeat seat);
	
	/**
	 * 删除在线成员
	 * @param seat
	 */
	void dropOnlineMember(FrontSeat seat);
	
	/**
	 * 基于某个节点，删除在线成员
	 * @param node
	 */
	void dropOnlineMember(Node node);

	/**
	 * 增加节点
	 * @param node
	 * @return
	 */
	boolean pushSite(Node node);

	/**
	 * 删除节点
	 * @param node
	 * @return
	 */
	boolean dropSite(Node node);

	/**
	 * 销毁节点
	 * @param node
	 * @return
	 */
	boolean destroySite(Node node);
	
	/**
	 * 释放全部记录，清空界面
	 * 这个操作发生在WATCH节点注销登录或者重新登录时
	 */
	void release();

}