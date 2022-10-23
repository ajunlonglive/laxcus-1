/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo;

/**
 * 回显辅助码。用在EchoCode中。
 * 
 * @author scott.liang
 * @version 1.6 10/20/2015
 * @since laxcus 1.0
 */
public final class Minor {

	/** 辅码无定义（结果已经由主码定义） **/
	public static final short DEFAULT = 0;

	/** 系统故障 **/
	public static final short SYSTEM_FAILED = -1;

	/** 回显处理故障 **/
	public static final short ECHO_ERROR = -2;

	/** 回显服务超时 **/
	public static final short ECHO_TIMEOUT = -3;

	/** 拒绝服务 **/
	public static final short REFUSE = -4;

	/** 没有找到 **/
	public static final short NOTFOUND = -5;

	/** 重复 **/
	public static final short DUPLEX = -6;

	/** 执行过程中发生的故障 **/
	public static final short IMPLEMENT_FAILED = -7;

	/** 客户端错误 **/
	public static final short CLIENT_ERROR = -8;

	/** 通信故障 **/
	public static final short COMMUNICATION_ERROR = -9;

	/** 站点资源不足 **/
	public static final short SITE_MISSING = -10;

	/** 没有找到站点 **/
	public static final short SITE_NOTFOUND = -11;

	/** 权限不足 **/
	public static final short PERMISSION_DENIED = -12;
	
	/** 内存不足 **/
	public static final short MEMORY_MISSING = -13;
	
	/** 容量不足 **/
	public static final short CAPACITY_MISSING = -14;

	/** 授权错误 **/
	public static final short GRANT_ERROR = -20;
	
	/** 不能发布组件 **/
	public static final short CANNOT_PUBLISH = -21;
	
	/** 异步调用器超时 **/
	public static final short INVOKER_TIMEOUT = -22;

	/** 不支持的操作 **/
	public final static short UNSUPPORT = -100;

	/** 事务的限制操作冲突 **/
	public final static short LIMIT_FORBID = -111;

	/** 主动发起故障中断 **/
	public final static short SELF_INTERRUPTED = -122;

	/** JNI接口错误 **/
	public final static short JNI_ERROR = -123;

	/** 没有找到文件 **/
	public final static short FILE_NOTFOUND = -125;

	public final static short USER_NOTFOUND = -126;

	/** 建表错误 **/
	public final static short CREATE_TABLE_FAILED = -127;

	/** 并行运行任务数目溢出 **/
	public final static short MAX_JOBSOUT = -128;

	/** 分布数据构建错误 **/
	public final static short ESTABLISH_ERROR = -200;

	/** 分布计算错误 **/
	public final static short CONDUCT_ERROR = -250;
	
	/** 快速计算错误 **/
	public final static short CONTACT_ERROR = -250;

	/** 建立禁止操作错误 **/
	public final static short CANNOT_CREATE_FORBID = -300;

	/** 没有找到分布任务组件 **/
	public final static short TASK_NOTFOUND = -2011;
	
	public final static short SWIFT_NOTFOUND =-2012;
	
	public final static short SCALER_NOTFOUND = -2013;

	public final static short CHUNK_NOTFOUND = -2020;

	/** 分享执行错误 **/
	public final static short CROSS_ERROR = -2100;

}