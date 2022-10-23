/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.tip;

/**
 * 消息提示标签 <br>
 * 
 * 还“X”后缀的标签表示需要有参数参与到字符串的格式化转义中，而不带“X”后缀的标签是一个普通的输出文本
 * 
 * @author scott.liang
 * @version 1.0 11/25/2013
 * @since laxcus 1.0
 */
public final class MessageTip {

	/** 语法正确 **/
	public static final int CORRECT_SYNTAX = 1; 

	/** 命令已经接受 **/
	public static final int COMMAND_ACCEPTED = 2; 
	
	/** 执行异步命令 **/
	public static final int COMMAND_EXECUTE = 3;

	/** 以下4个是事务操作 **/
	public static final int RULE_ATTACH = 4;

	public static final int RULE_ATTACH_RETURN_OK = 5;

	public static final int RULE_DETACH = 6;

	public static final int RULE_DETACH_RETURN_OK = 7;
	
	public static final int LOADING_USER_RESOURCE = 8;
	public static final int LOADED_USER_RESOURCE = 9;
	
	/** 操作已经处理 **/
	public static final int COMMAND_PROCESSED = 10;

	
	/** 以下是带X后缀的标签，从10000开始 **/

	/** ... 成功 **/
	public static final int SUCCESSFUL_X = 10000; 

	/** 命令耗时... **/
	public static final int COMMAND_USEDTIME_X = 10001;

	/** 远程关闭 ... **/
	public static final int SHUTDOWN_X = 10002; 

	/** 加入/撤销 站点 **/
	public static final int PUSH_NODE_X = 10003;
	
	/** 成功/失败统计 ... **/
	public static final int SUCCESS_FAULT_X = 10004;

	/** 分布处理用时... **/
	public static final int DISTRIBUTED_TIME_X = 10005;

	/** 建立 ... 成功 **/
	public static final int CREATE_SUCCESSFUL_X = 10010;

	/** 删除 ... 成功 **/
	public static final int DROP_SUCCESSFUL_X = 10011;

	/** 修改 ... 成功 **/
	public static final int MODIFY_SUCCESSFUL_X = 10012;
	
	/** 格式化时间 ... **/
	public static final int FORMAT_TIME_X = 10013;
	
	/** 检测 ... **/
	public static final int CHECK_X = 10014;

	/** 文件...存在，确认覆盖它 ？(是图形界面)**/
	public static final int OVERRIDE_FILE_GUI = 10015;
	
	/** 文件...存在，确认覆盖它（Yes / No） 是控制台界面 ？**/
	public static final int OVERRIDE_FILE_CONSOLE_X = 10016;
	
	/**  %s 定时刷新 **/
	public static final int TIMEING_REFRESH_NODE_X = 10017;
	
	/** 时间... **/
	public static final int SIMPLE_USEDTIME_X = 10018;

}