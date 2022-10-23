/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

/**
 * “local.xml”文件中的其他标签
 * @author scott.liang
 * @version 1.0 11/30/2016
 * @since laxcus 1.0
 */
public final class OtherMark {

	/** 安全通信网络（FIXP服务器RSA密钥令牌配置）**/
	public static final String SECURITY_NETWORK = "security-network";
	
	
	/** 边缘计算服务器JAR目录**/
	public static final String TUB_DIRECTORY = "tub-directory";
	
	/** 容器管理池定时检测更新**/
	public static final String TUB_UPDATE_TIMEOUT = "update-timeout";
	
	/** 属性参数，分布任务组件检测超时 **/
	public static final String ATTRIBUTE_TASK_SCANTIMEOUT = "check-timeout";
	
	/** 分布任务组件存放目录 **/
	public static final String TASK_DIRECTORY = "task-directory";

	/** 码位计算器组件存放目录 **/
	public static final String SCALER_DIRECTORY = "scaler-directory";
	
	/** 快捷组件存放目录 **/
	public static final String SWIFT_DIRECTORY = "swift-directory";

	/** 中间数据存取目录 **/
	public static final String MIDDLE_DIRECTORY = "middle-directory";
	
	/** 中间数据最大缓存尺寸 **/
	public static final String MIDDLE_MAX_CACHESIZE = "middle-max-cachesize";
	
	/** 分配给一个分布任务组件的最大数据缓存尺寸 */
	public static final String MIDDLE_USER_CACHESIZE = "middle-user-cachesize";
	
	/** 资源管理池延时间隔 */
	public static final String STAFFPOOL_SLEEP_INTERVAL = "staffpool-sleep-interval";
	
	/** 黑名单上账号超时时间  **/
	public static final String BLACKLIST_DISABLE_TIMEOUT = "blacklist-disable-timeout";
}