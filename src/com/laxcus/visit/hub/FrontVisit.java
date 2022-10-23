/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.hub;

import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;

/**
 * FRONT远程登录管理接口。<br>
 * 为FRONT站点提供注册和管理的服务，分别由TRANCE/GATE站点实现。<br><br>
 * 
 * 服务内容包括：<br>
 * 1. 获取被注册站点的站点类型（ENTRANCE/GATE）。<br>
 * 2. 获取FRONT站点的激活触发间隔时间，ENTRANCE/GATE站点定义。<br>
 * 3. FRONT向被注册站点注册。<br>
 * 4. 检查上次测试注册结果。<br>
 * 5. FRONT站点注销。<br>
 * 
 * FRONT客户连端连接默认是UDP模式。
 * 
 * @author scott.liang
 * @version 1.0 7/17/2018
 * @since laxcus 1.0
 */
public interface FrontVisit extends Visit {
	
	/**
	 * 返回服务端节点的版本
	 * @return Version实例
	 * @throws VisitException
	 */
	Version getVersion() throws VisitException;

	/**
	 * 返回被登录站点的站点类型，是ENTRANCE/GATE中的一种。见SiteTag定义
	 * @return 站点类型
	 * @throws VisitException
	 */
	byte getHubFamily() throws VisitException;
	
	/**
	 * 返回GATE/ENTRANCE的异步数据接收器（ReplySucker）的公网地址
	 * @return SocketHost
	 * @throws VisitException
	 */
	SocketHost getHubSucker(boolean wide) throws VisitException;
	
	/**
	 * 返回GATE/ENTRANCE的异步数据发送器的公网地址
	 * @return SocketHost
	 * @throws VisitException
	 */
	SocketHost getHubDispatcher(boolean wide) throws VisitException;

	/**
	 * 返回以毫秒为单位的ENTRANCE/GATE站点给FRONT站点设定的定期激活间隔时间
	 * @return 以毫秒为单位的超时时间
	 * @throws VisitException
	 */
	long getTimeout() throws VisitException;
	
	/**
	 * 返回以毫秒为单位的延时间隔时间
	 * @return 以毫秒为单位的超时时间
	 * @throws VisitException
	 */
	long getLingerTimeout() throws VisitException;

	/**
	 * 网络断开后，FRONT节点自动重新注册GATE服务器的时间。注意，是“自动登录”！
	 * 时间以GATE节点的规定为标准，必须符合时间才能重新注册
	 * 
	 * @return 以毫秒为单位的时间间隔
	 * @throws VisitException
	 */
	long getAutoReloginInterval() throws VisitException;
	
	/**
	 * FRONT节点可能存在故障后又马上登录的情况。所以首先要调用“release”方法删除，再使用“login”方法登录。
	 * 此操作在FrontLauncher方法处理。
	 * 
	 * @param site FRONT节点
	 * @return 成功返回真，否则假
	 * @throws VisitException
	 */
	boolean release(FrontSite site) throws VisitException;
	
	/**
	 * FRONT站点注册到GATE或者ENTRANCE站点。
	 * @param site FRONT站点
	 * @return 返回登录状态
	 * @throws VisitException
	 */
	FrontReport login(FrontSite site) throws VisitException;

	/**
	 * 以账号所有人的身份，从GATE站点注销自己的FRONT站点。
	 * @param node FRONT站点地址
	 * @return 成功返回“真”，否则“假”。
	 * @throws VisitException
	 */
	boolean logout(Node node) throws VisitException;

	/**
	 * FRONT节点可能存在故障后又马上登录的情况。所以首先要调用“release”方法删除，再使用“login”方法登录。
	 * 
	 * @param site 被授权的FRONT站点
	 * @return 成功返回真，否则假
	 * @throws VisitException
	 */
	boolean release(ConferrerSite site) throws VisitException;
	
	/**
	 * 被授权FRONT站点注册到GATE站点。
	 * @param site 被授权的FRONT站点
	 * @return 返回登录状态
	 * @throws VisitException
	 */
	FrontReport login(ConferrerSite site) throws VisitException;

	/**
	 * 以被授权人的身份，从GATE站点注销与授权人的记录。
	 * @param node FRONT站点地址
	 * @param authorizer 授权人签名
	 * @return 成功返回“真”，否则“假”。
	 * @throws VisitException
	 */
	boolean logout(Node node, Siger authorizer) throws VisitException;
}