/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.hub;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 登录访问管理接口。<br>
 * 为登录站点提供注册和管理的服务。<br><br>
 * 
 * 服务内容包括：<br>
 * 1. 获取当前被注册站点的系统时间（集群保持一个统一时间）。<br>
 * 2. 获得激活超时时间。<br>
 * 3. 获取站点的日志服务器地址（此项操作只限HOME集群中的工作/网关节点）。<br>
 * 4. 站点注册/注销的操作。<br><br>
 * 
 * 访问这个接口的客户连端连接默认是TCP模式。
 * 
 * @author scott.liang
 * @version 1.0 1/26/2009
 * @since laxcus 1.0
 */
public interface HubVisit extends Visit {

	/**
	 * 返回服务端节点的版本
	 * @return
	 * @throws VisitException
	 */
	Version getVersion() throws VisitException;

	/**
	 * 返回登录站点的站点类型，见SiteTag定义
	 * @return 站点类型
	 * @throws VisitException
	 */
	byte getHubFamily() throws VisitException;
	
	/**
	 * 返回登录站点的站点地址，见SiteTag定义。
	 * 只适用TOP/BANK/HOME节点。
	 * 
	 * @return Node实例
	 * @throws VisitException
	 */
	Node getHub() throws VisitException;

	/**
	 * 获取管理站点的当前时间。考虑到网络传输，时间误差控制在1秒内。子级站点用这个时间修改本地系统时间。
	 * @return 返回管理站点时间，以毫秒为单位。
	 * @throws VisitException
	 */
	long currentTime() throws VisitException;

	/**
	 * 根据下属站点的站点类型，返回它的激活间隔时间。
	 * @param siteFamily 站点类型，见SiteTag中的定义。
	 * @return 返回超时时间，以毫秒为单位。
	 * @throws VisitException
	 */
	long getSiteTimeout(byte siteFamily) throws VisitException;
	
	/** 
	 * 管理节点定义的延时注册间隔时间，以毫秒为单位。
	 * 由上级管理节点定义，下级节点无条件遵守。
	 * 定义延时注册是避免子节点在非紧急情况下的频繁注册，造成管理节点的工作过大，分散注册压力。
	 * @return 延时注册间隔时间
	 * @throws VisitException
	 */
	long getHubRegisterInterval() throws VisitException;

	/** 
	 * 管理节点定义的最大延时注册间隔时间，以毫秒为单位。
	 * 由上级管理节点定义，下级节点无条件遵守。
	 * 当达到这个时间后，无论节点是否需求，都启动重新注册。
	 * @return 延时注册间隔时间
	 * @throws VisitException
	 */
	long getHubMaxRegisterInterval() throws VisitException;

	/**
	 * 客户机根据自己的站点类型，要求上级站点返回一个日志站点地址。
	 * 运行过程中，客户机将向这个日志站点发送日志数据。
	 * @param siteFamily 客户机站点类型(CALL/DATA/WORK/BUILD)
	 * @return 服务端受理，返回一个日志节点地址，否则是空指针。
	 * @throws VisitException
	 */
	Node findLogSite(byte siteFamily) throws VisitException;

	/**
	 * 客户机根据自己的站点类型，要求上级站点返回一个操作消息站点地址。
	 * 运行过程中，客户机将向这个操作消息站点发送操作消息数据。
	 * @param siteFamily 客户机站点类型，包括HOME/CALL/DATA/WORK/BUILD、BANK/ACCOUNT/HASH/GATE/ENTRANCE。
	 * @return 服务端受理，返回一个操作消息节点地址，否则是空指针。
	 * @throws VisitException
	 */
	Node findTigSite(byte siteFamily) throws VisitException;

	/**
	 * 客户机根据自己的站点类型，要求上级站点返回一个消耗记录站点地址。
	 * 运行过程中，客户机将向这个消耗记录站点发送消耗记录数据。
	 * @param siteFamily 客户机站点类型，包括HOME/CALL/DATA/WORK/BUILD、BANK/ACCOUNT/HASH/GATE/ENTRANCE。
	 * @return 服务端受理，返回一个消耗记录节点地址，否则是空指针。
	 * @throws VisitException
	 */
	Node findBillSite(byte siteFamily) throws VisitException;

	/**
	 * 下属站点注册到管理站点。
	 * @param site 下属站点配置
	 * @return 成功返回“真”，否则“假”。
	 * @throws VisitException
	 */
	boolean login(Site site) throws VisitException;

	/**
	 * 下属站点从管理站点注销
	 * @param node 下属站点地址
	 * @return 成功返回“真”，否则“假”。
	 * @throws VisitException
	 */
	boolean logout(Node node) throws VisitException;

	
}