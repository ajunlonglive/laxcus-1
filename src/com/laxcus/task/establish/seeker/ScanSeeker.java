/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.seeker;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * SCAN阶段资源向导接口，指示集群中SCAN阶段组件的分布状况。<br>
 * SCAN阶段资源向导接口运行在CALL站点上。
 * 
 * @author scott.liang
 * @version 1.0 11/11/2009
 * @since laxcus 1.0
 */
public interface ScanSeeker extends DistributedSeeker {

	/**
	 * 根据调用器编号和数据表名，查找对应的数据表
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @return 返回数据表实例
	 * @throws TaskException - 账号用户签名不致或者表不存在，弹出组件异常 
	 */
	Table findScanTable(long invokerId, Space space) throws TaskException;

	/**
	 * 根据调用器编号和数据表名，查找全部DATA主站点地址（注意！是主站点，从站点不在其中）。
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @return 返回主站点地址集合
	 * @throws TaskException - 账号调用器编号不致或者表不存在，弹出组件异常
	 */
	NodeSet findScanSites(long invokerId, Space space) throws TaskException;

	/**
	 * 根据调用器编号和SCAN阶段命名，找到全部SCAN阶段站点（DATA主站点）地址
	 * @param invokerId 调用器编号
	 * @param scan SCAN阶段命名
	 * @return 返回DATA主站点地址集合
	 * @throws TaskException - 账号调用器编号不一致或者命令不存在时，弹出组件异常
	 */
	NodeSet findScanSites(long invokerId, Phase scan) throws TaskException;

	/**
	 * 根据调用器编号、数据表名、数据块编号，找到全部的站点集合（不区分主、从）
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回DATA站点集合
	 * @throws TaskException - 如果签名被拒绝，弹出安全异常
	 */
	NodeSet findStubSites(long invokerId, Space space, long stub) throws TaskException;

	/**
	 * 根据调用器编号和节点地址，判断是主站点
	 * @param invokerId 调用器编号
	 * @param node 节点地址
	 * @return 条件成立返回真，否则假
	 * @throws TaskException - 如果签名被拒绝，弹出安全异常
	 */
	boolean isMaster(long invokerId, Node node) throws TaskException;

	/**
	 * 根据调用器编号和节点地址，判断是从站点
	 * @param invokerId 调用器编号
	 * @param node 节点地址
	 * @return 条件成立返回真，否则假
	 * @throws TaskException
	 */
	boolean isSlave(long invokerId, Node node) throws TaskException;
}