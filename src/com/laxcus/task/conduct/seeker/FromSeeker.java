/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.seeker;

import java.util.*;

import com.laxcus.access.index.section.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.task.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * FROM阶段资源向导接口。<br><br>
 * 
 * FROM阶段资源向导接口指示集群FROM阶段组件的分布状况，提供FROM阶段分布资源的管理、产生、检索等工作。<br>
 * FROM阶段资源向导接口在CALL站点实现。<br>
 * 
 * @author scott.liang
 * @version 1.0 4/23/2009
 * @since laxcus 1.0
 */
public interface FromSeeker extends DistributedSeeker {

	/**
	 * 根据调用器编号和数据表名，获得全部DATA主站点下的数据块索引分区
	 * 
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @return 数据块索引分区集合
	 * @throws TaskException - 账号用户签名不一致或者表不存在，弹出组件异常
	 */
	List<StubSector> createPrimeStubSector(long invokerId, Space space) throws TaskException;

	/**
	 * 根据调用器编号和数据表名，获得全部DATA从站点下的数据索引分区
	 * 
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @return 数据块索引分区集合
	 * @throws TaskException - 账号用户签名不一致或者表不存在，弹出组件异常
	 */
	List<StubSector> createSlaveStubSector(long invokerId, Space space) throws TaskException;

	/**
	 * 根据调用器编号和数据表名，生成全部数据块索引分区。
	 * 每个分区都指定了DATA站点地址、表名、站点下的数据块编号。
	 * 数据块不区分主从，每个数据块按照平均的原则进行分配，保证每个站点和数据块都得到均匀的调用。
	 * 
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @return 数据块索引分区集合
	 * @throws TaskException - 用户签名错误，或者表不存在时，弹出分布任务组件及子类异常
	 */
	List<StubSector> createStubSector(long invokerId, Space space) throws TaskException;

	/**
	 * 根据调用器编号、列空间、站点数目，生成某一列的数据分区。
	 * 这个方法由系统执行，可能产生网络检索。网络检索将向DATA站点获取需要的列索引区域，然后在本地整合，输出对象。
	 * 
	 * @param invokerId 调用器编号
	 * @param dock 列空间
	 * @param sites 站点地址
	 * @return 数据块分区
	 * @throws TaskException - 账号用户签名不一致或者表不存在，弹出组件异常
	 */
	ColumnSector createIndexSector(long invokerId, Dock dock, int sites) throws TaskException;

	/**
	 * 根据调用器编号和数据表名，查找对应的数据表配置
	 * 
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @return Table实例
	 * @throws TaskException - 账号用户签名不一致或者表不存在，弹出组件异常
	 */
	Table findFromTable(long invokerId, Space space) throws TaskException;

	/**
	 * 根据调用器编号和数据表名，找到全部DATA站点地址。
	 * 
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @return 返回站点集合实例
	 * @throws TaskException - 账号用户签名不一致或者表不存在，弹出组件异常
	 */
	NodeSet findFromSites(long invokerId, Space space) throws TaskException;
	
	/**
	 * 根据调用器编号和FROM阶段命名，找到全部DATA站点地址
	 * 
	 * @param invokerId 调用器编号
	 * @param from FROM阶段名称
	 * @return 返回站点集合实例
	 * @throws TaskException - 账号用户签名不一致或者阶段命名不存在，弹出组件异常
	 */
	NodeSet findFromSites(long invokerId, Phase from) throws TaskException;

	/**
	 * 根据阶段命名、调用器编号、数据表名，找到全部DATA站点地址
	 * 
	 * @param invokerId 调用器编号
	 * @param from FROM阶段命名
	 * @param space 数据表名
	 * @return 返回站点集合实例
	 * @throws TaskException - 账号用户签名不一致或者表不存在，弹出组件异常
	 */
	NodeSet findFromSites(long invokerId, Phase from, Space space) throws TaskException;

	/**
	 * 返回与调用器签名关联的DATA节点
	 * @param invokerId 调用器编号
	 * @return 返回关联DATA节点数目
	 * @throws TaskException
	 */
	int getFromSites(long invokerId) throws TaskException;
}