/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.container;

/**
 * 应用程序启动编码
 * 
 * 启动码规则：
 * 等于0，完全正确
 * 小于0，错误
 * 
 * @author scott.liang
 * @version 1.0 3/7/2022
 * @since laxcus 1.0
 */
public class ApplicationStartCode {

	/** 成功 **/
	public static final int SUCCESSFUL = 0;

	/** 失败，但是不确定故障 **/
	public static final int FAILED = -1;

	/** 已经存在，本次拒绝执行（前面一个进程已经执行，后面进程判断拒绝执行） **/
	public static final int EXISTS_EXIT = -2;

	/** 非数字返回码 **/
	public static final int ERROR_RETURN_CODE = -3;

}