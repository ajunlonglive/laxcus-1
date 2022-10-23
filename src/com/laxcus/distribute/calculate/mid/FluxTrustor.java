/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.calculate.mid;

import com.laxcus.task.*;

/**
 * CONDUCT命令的中间数据存取代理。<br><br>
 * 
 * 为分布计算时的中间数据，提供读、写、删除操作。<br>
 * FluxTrustor存取的中间数据是实体数据，同时根据实体数据产生元数据。<br><br>
 * 
 * 用户首先需要调用“createStack”方法建立一个存取堆栈，指明把中间数据存储存入硬盘还是内存。
 * 在CONDUCT分布计算过程中，如果一个命令在所有站点的中间数据，都通过内存实现存取，那么这实质就是一个流式处理。
 * 
 * @author scott.liang
 * @version 1.2 7/18/2013
 * @since laxcus 1.0
 */
public interface FluxTrustor {

	/**
	 * 返回系统定义、能够分配给每个用户使用的最大内存空间。<br>
	 * 注意：是单个用户！！！
	 * 
	 * @param invokerId 调用器编号
	 * 
	 * @return 以字节计算的长整型值
	 * @throws TaskException - 调用器编号异常
	 */
	long getMemberMemory(long invokerId) throws TaskException;

	/**
	 * 建立DIFFUSE/CONVERGE分布计算的中间数据存取栈。<br>
	 * 如果数据要求写入内存，要求同时指定内存容量，且小于或者等于系统分配给每个用户的内存容量时，系统才能接受。
	 * 
	 * @param invokerId 异步调用器编号
	 * @param memory 要求中间数据写入内存。此参数为“真”时，要求同时指定内存容量
	 * @param capacity 内存容量
	 * 
	 * @return 返回数据存取栈的任务编号（编号在0 - Long.MAX_VALUE之间），如果失败是负数。
	 * @throws TaskException - 在建立堆栈过程中产生异常
	 */
	long createStack(long invokerId, boolean memory, long capacity) throws TaskException;

	/**
	 * 建立一个默认的DIFFUSE/CONVERGE中间数据存取栈，中间数据将被指定写入磁盘。即createStack(invokerId, false, -1L)
	 * 
	 * @param invokerId 用户签名
	 * 
	 * @return 返回数据存取栈的任务编号（编号在0 - Long.MAX_VALUE之间），如果失败是负数。
	 * @throws TaskException - 在建立堆栈过程中产生异常
	 */
	long createStack(long invokerId) throws TaskException;

	/**
	 * 从内存或者硬盘删除中间数据存取栈
	 * 
	 * @param invokerId 用户签名
	 * @param taskId 任务编号
	 * @return 成功返回真，否则假
	 */
	boolean deleteStack(long invokerId, long taskId) throws TaskException;

	/**
	 * 根据任务编号，查找关的中间数据写入器
	 * 
	 * @param invokerId 用户签名
	 * @param taskId 任务编号
	 * @return 返回一个已经存在的中间数据写入器
	 */
	FluxWriter findWriter(long invokerId, long taskId) throws TaskException;

	/**
	 * 根据任务编号，查找一个中间数据读取器
	 * 
	 * @param invokerId 用户签名
	 * @param taskId 任务编号
	 * @return 返回一个已经存在的中间数据读取器
	 */
	FluxReader findReader(long invokerId, long taskId) throws TaskException;
}