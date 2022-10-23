/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver;

/**
 * FRONT驱动程序入口参数，通过“DriverLauncher.launch”方法传入
 * 
 * @author scott.liang
 * @version 1.0 7/1/2015
 * @since laxcus 1.0
 */
public final class DriverMark {

	/** 日志配置文件 **/
	public final static String LOG_FILE = "log-file";

	/** 本地配置文件参数 **/
	public final static String CONF_FILE = "config-file";

	/** 登录账号 **/
	public final static String ACCOUNT = "driver-user";

	/** SHA格式登录账号 **/
	public final static String SHA_ACCOUNT = "driver-shauser";

	//	//////////////////////////////////////
	//	/// 以下做到配置文件里：local.xml
	//	
	//	/** 注册站点地址 **/
	//	public final static String HUB_SITE = "hub-site";
	//	
	//	/** 本地节点地址 **/
	//	public final static String LOCAL_SITE = "local-node";
	//	
	//	/** 任务组件目录 **/
	//	public final static String TASK_DIRECTORY = "task-directory";
	//
	//	/** 控制台打印 **/
	//	public final static String CONSOLE_PRINT = "driver-print";
	//
	//	/** 自定义命令/调用器资源声明文件 **/
	//	public static final String CUSTOM_STATEMENT = "custom-statement";
	//	
	//	/** 自定义JAR文件存储目录 **/
	//	public static final String CUSTOM_DIRECTORY = "custom-directory";

}