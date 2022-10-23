/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

import com.laxcus.access.schema.*;
import com.laxcus.law.cross.*;

/**
 * 数字签名人代理 <br><br>
 * 
 * 数字签名人代理提供基本的分布操作许可判断，包括对数据执行人和关联表的有效性和可操作判断。<br><br>
 * 
 * 以xxxTrustor命令的代理，提供本地分布任务组件访问系统资源的桥梁。
 * 
 * SigerTrustor是基础接口。
 * 在分布任务组件（分布数据计算和分布数据构建，CONDUCT/ESTABLISH/CONTACT）中，
 * 需要访问本地资源、安全检查、操作本地数据的接口类，都从此派生。<br><br>
 * 
 * @author scott.liang
 * @version 1.2 8/17/2017
 * @since laxcus 1.0
 */
public interface SigerTrustor {

	/**
	 * 通过异步调用器编号，判断数据执行人存在且有效。 <br>
	 * 数据执行人可以数据表的所有人或者被授权人中的任何一种。
	 * 
	 * @param invokerId 异步调用器编号
	 * @return 返回真或者假
	 */
	boolean allow(long invokerId) throws TaskException;

	/**
	 * 通过异步调用器编号，判断数据执行人和数据表匹配并且有效。<br>
	 * 数据表可以是专属表（数据执行人建立和使用）或者共享表的任何一种。如果是共享表，只做数据表存在的判断。<br>
	 * 
	 * @param invokerId 异步调用器编号
	 * @param space 表名
	 * @return 返回真或者假
	 * @since 1.1
	 */
	boolean allow(long invokerId, Space space) throws TaskException;

	/**
	 * 通过异步调用器编号，判断数据执行人和数据表匹配并且有效。<br>
	 * 数据表可以是专属表或者共享表的任意一种。如果是共享表，需要判断数据表存在并且符合操作要求。<br>
	 * 
	 * @param invokerId 异步调用器编号
	 * @param flag 共享资源标识
	 * @return 返回真或者假
	 * @since 1.2
	 */
	boolean allow(long invokerId, CrossFlag flag) throws TaskException;

	
	/**
	 * 通过异步调用器编号，获得这个账号的中间缓存尺寸
	 * @param invokerId 调用器编号
	 * @return 返回一个整数值
	 * @throws TaskException 弹出异常
	 */
	long getMiddleBufferSize(long invokerId) throws TaskException;

	
//	/**
//	 * 通知异步调用器，查找数据执行人的资源引用
//	 * @param invokerId 调用器编号
//	 * @return Refer实例，没有是空指针
//	 * @throws TaskException
//	 */
//	Refer findRefer(long invokerId) throws TaskException;

}