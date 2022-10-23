/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.tip;

/**
 * 错误提示标签
 * 
 * @author scott.liang
 * @version 1.0 11/25/2013
 * @since laxcus 1.0
 */
public class WarningTip {
	
	/** 没有实施 **/
	public static final int CANNOT_IMPLEMENT = 1;
	
	/** 搜索是空集合 **/
	public static final int SEARCH_RESULT_EMPTY = 2;
	
	/** 空记录 **/
	public static final int EMPRY_RECORD = 3;

	/** 以下是带X后缀的标签，从10000开始 **/

	/** 磁盘空间不足 **/
	public static final int DISK_MISSING_X = 10000;

	/** 没有实施，带其它参数 **/
	public static final int CANNOT_IMPLEMENT_X = 10001;
	
	/** 撤销站点 **/
	public static final int DROP_NODE_X = 10002;

	/** 用户站点不足 **/
	public static final int USERSITE_MSSING_X = 10003;
	
	/** 内存空间不足 **/
	public static final int MEMORY_MISSING_X = 10004;
	
	/** 虚拟用户空间不足 **/
	public static final int MEMBER_MISSING_X = 10005;

	/** 虚拟用户空间耗尽 **/
	public static final int MEMBER_FULL_X = 10006;
	
	/** FRONT在线用户空间不足 **/
	public static final int FRONT_MISSING_X = 10007;

	/** FRONT在线用户空间耗尽 **/
	public static final int FRONT_FULL_X = 10008;
	
	/** JVM虚拟机内存空间不足 **/
	public static final int VMMEMORY_MISSING_X = 10009;
	
	/** 许可证超时 **/
	public static final int LICENCE_TIMEOUT_X = 10010;
	
	/** 本地没有找到 ...**/
	public static final int NOT_FOUND_LOCAL_X = 10011;
}