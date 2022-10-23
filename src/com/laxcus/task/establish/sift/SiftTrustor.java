/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.sift;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.index.*;
import com.laxcus.command.access.*;
import com.laxcus.distribute.establish.mid.*;
import com.laxcus.task.*;

/**
 * SIFT工作代理 <br>
 * 
 * 此接口在BUILD站点实现。ESTABLISH.SIFT阶段任务实例调用这个接口实现数据构建操作。
 * 
 * 本处提供基于内存的数据读写操作，在指定要求采用内存模式时，是否能够提供内存操作，要视当时具体环境而定。
 * 即提供内存存取操作，但是不保证一定能够按照要求实现内存存取操作。
 * 
 * @author scott.liang
 * @version 1.1 6/12/2012
 * @since laxcus 1.0
 */
public interface SiftTrustor extends SiteTrustor {
	
	/**
	 * 根据异步调用器编号和数据表名，查找表实例
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @return 返回Table实例
	 */
	Table findSiftTable(long invokerId, Space space) throws TaskException;

	/**
	 * 判断表存在
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	boolean hasSiftTable(long invokerId, Space space) throws TaskException;
	
	/**
	 * 判断磁盘上有表空间
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	boolean hasDiskSpace(long invokerId, Space space) throws TaskException;

	/**
	 * 建立磁盘空间
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @return 成功返回真，否则假
	 */
	boolean createDiskSpace(long invokerId, Space space) throws TaskException;

	/**
	 * 删除磁盘空间
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @return 成功返回真，否则假
	 */
	boolean deleteDiskSpace(long invokerId, Space space) throws TaskException;

	/**
	 * 使用SELECT命令检索数据
	 * @param invokerId 异步调用器编号
	 * @param select SELECT命令
	 * @param stub 数据块编号
	 * @return AccessStack字节数组
	 */
	byte[] select(long invokerId, Select select, long stub) throws TaskException;

	/**
	 * 使用DELETE命令删除数据
	 * @param invokerId 异步调用器编号
	 * @param delete DELETE命令
	 * @param stub 数据块编号
	 * @return AccessStack字节数组
	 */
	byte[] delete(long invokerId, Delete delete, long stub) throws TaskException;

	/**
	 * 使用INSERT命令写入数据
	 * @param invokerId 异步调用器编号
	 * @param cmd INSERT命令
	 * @return AccessStack字节数组
	 */
	byte[] insert(long invokerId, Insert cmd) throws TaskException;

	/**
	 * 强制将一个CACHE状态数据块，转换为CHUNK状态。对应Access.rush方法。
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @return 大于等于0是正确，否则是错误码
	 */
	int rush(long invokerId, Space space) throws TaskException;

	/**
	 * MARSHAL数据排序。 <br><br>
	 * 
	 * 对一个表的所有数据，指定列编号，进行升序排序。
	 * 在数据排序前，将首先进行用户许可检查，然后调用Access.marshal接口。<br>
	 * 
	 * @param invokerId 异步调用器编号
	 * @param dock 列空间名称
	 * @return 如果成功，返回被排序的数据长度。否则是负数（错误码）
	 */
	long marshal(long invokerId, Dock dock) throws TaskException;

	/**
	 * 取消MARSHAL/EDUCE操作
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @return 成功返回0，否则是负数（错误码）
	 */
	int unmarshal(long invokerId, Space space) throws TaskException;

	/**
	 * EDUCE数据读取 <br><br>
	 * 
	 * 在MARSHAL排序后，EDUCE将从JNI.DB读取排序后的数据。
	 * 正常情况下，这个操作会发生多次，直到全部读完。
	 * 
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @param readlen 指定读取长度
	 * 
	 * @return 返回读取的字节数组。如果读完，字节数组0长度； 如果出错，返回空指针。
	 */
	byte[] educe(long invokerId, Space space, int readlen) throws TaskException;

	/**
	 * 返回“CHUNK”状态的全部数据块编号
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @return 数据块编号数组
	 */
	long[] findChunkStubs(long invokerId, Space space) throws TaskException;

	/**
	 * 返回“CACHE”状态的数据块编号（每个表只有一个）
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @return 返回数据块编号，0值是无效。
	 */
	long findCacheStub(long invokerId, Space space) throws TaskException;

	/**
	 * 加载数据块
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 成功，返回大于等于0；否则是负数（错误码）
	 */
	int loadChunk(long invokerId, Space space, long stub) throws TaskException;

	/**
	 * 从磁盘删除数据块
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 成功，返回大于等于0；否则是负数（错误码）
	 */
	int deleteChunk(long invokerId, Space space, long stub) throws TaskException;

	/**
	 * 根据数据表名和数据块编号，产生本地的文件。这个文件是不存在的。
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 本地系统的文件路径
	 */
	String doChunkFile(long invokerId, Space space, long stub) throws TaskException;

	/**
	 * 查找数据块索引
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @return 返回StubArea实例，或者空指针
	 */
	StubArea findIndex(long invokerId, Space space) throws TaskException;

	/**
	 * 检测索引
	 * @param invokerId 异步调用器编号
	 * @param space 数据表名
	 * @return 返回SiftField实例，或者空指针。
	 */
	SiftField detect(long invokerId, Space space) throws TaskException;

	/**
	 * 以下为SIFT中间数据存取接口。<br>
	 * SIFT的开始数据是从JNI.DB读取，最后也要写入JNI.DB。在这个过程中，需要缓存数据。以下方法将提供这个功能 
	 **/

	/**
	 * 返回系统定义、能够分配给每个用户使用的最大内存空间。<br>
	 * 注意：是单个用户！！！
	 * 
	 * @return 以字节计算的长整型值
	 */
	long getMemberMemory();
	
	/**
	 * 以内存或者磁盘模式，建立一个数据读写堆栈
	 * @param invokerId 异步调用器编号
	 * @param memory 内存模式
	 * @param capacity 指定内存数量，当memory=true时，这个参数才生效。
	 * 
	 * @return 返回数据存取栈的任务编号（编号在0 - Long.MAX_VALUE之间），如果失败是负数。
	 * @throws TaskException - 在建立堆栈过程中产生异常
	 */
	long createStack(long invokerId, boolean memory, long capacity) throws TaskException;

	/**
	 * 建立一个默认的数据读取堆栈。即 createStack(invokerId, false, -1L);
	 * @param invokerId 异步调用器编号
	 * 
	 * @return 返回数据存取栈的任务编号（编号在0 - Long.MAX_VALUE之间），如果失败是负数。
	 * @throws TaskException - 在建立堆栈过程中产生异常
	 */
	long createStack(long invokerId) throws TaskException;

	/**
	 * 根据任务编号，返回对应的SIFT阶段数据写入接口
	 * @param invokerId 异步调用器编号
	 * @param taskId 任务编号
	 * @return SiftWriter子类实例
	 */
	SiftWriter findWriter(long invokerId, long taskId) throws TaskException;

	/**
	 * 根据任务编号，返回对应的SIFT阶段数据读取接口
	 * @param invokerId 异步调用器编号
	 * @param taskId 任务编号
	 * @return SiftReader子类实例
	 */
	SiftReader findReader(long invokerId, long taskId) throws TaskException;

	/**
	 * 删除指定堆栈
	 * @param invokerId 异步调用器编号
	 * @param taskId 任务编号
	 * @return 成功返回“真”，否则“假”。
	 */
	boolean deleteStack(long invokerId, long taskId) throws TaskException;

}