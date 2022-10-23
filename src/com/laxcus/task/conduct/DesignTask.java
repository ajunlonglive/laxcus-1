/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.index.balance.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.meta.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.seeker.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 数据计算规则设计和资源分派任务。<br>
 * 提供对网络分布资源和数据的检查、分析、筛选、获取、生成，子类包括:InitTask、BalanceTask <br>
 * 
 * @author scott.liang
 * @version 1.23 11/7/2015
 * @since laxcus 1.0
 */
public abstract class DesignTask extends AccessTask {

	/** FROM阶段资源向导接口 **/
	private FromSeeker fromSeeker;

	/** TO阶段资源向导接口 **/
	private ToSeeker toSeeker;

	/** 元数据存取代理 **/
	private MetaTrustor metaTrustor;

	/**
	 * 构造默认的数据计算规则设计和资源分派任务
	 */
	protected DesignTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#getCommand()
	 */
	@Override
	public Conduct getCommand() {
		return (Conduct) super.getCommand();
	}

	/**
	 * 设置FROM阶段资源向导接口
	 * @param e FromSeeker实例
	 */
	public void setFromSeeker(FromSeeker e) {
		fromSeeker = e;
	}

	/**
	 * 返回FROM阶段资源向导接口。提供CONDUCT.FROM阶段资源建立/检索服务
	 * @return FromSeeker实例
	 */
	public FromSeeker getFromSeeker() {
		return fromSeeker;
	}

	/**
	 * 设置TO阶段资源向导接口。提供CONDUCT.TO阶段资源检索服务。
	 * @param e ToSeeker实例
	 */
	public void setToSeeker(ToSeeker e) {
		toSeeker = e;
	}

	/**
	 * 返回TO阶段资源向导接口
	 * @return ToSeeker实例
	 */
	public ToSeeker getToSeeker() {
		return toSeeker;
	}

	/**
	 * 设置元数据存取代理
	 * @param e MetaTrustor实例
	 */
	public void setMetaTrustor(MetaTrustor e) {
		metaTrustor = e;
	}

	/**
	 * 返回元数据存取代理
	 * @return MetaTrustor实例
	 */
	protected MetaTrustor getMetaTrustor() {
		return metaTrustor;
	}

	/**
	 * 写入元数据
	 * @param tag 元数据标识
	 * @param b 元数据字节数组
	 * @param off 字节数组开始下标
	 * @param len 字节数组有效长度
	 * @return 写入成功返回真，否则假
	 * @throws TaskException
	 */
	protected boolean write(MetaTag tag, byte[] b, int off, int len)
			throws TaskException {
		return metaTrustor.write(tag, b, off, len);
	}

	/**
	 * 读出元数据
	 * @param tag 元数据标识
	 * @return 返回字节数组
	 * @throws TaskException
	 */
	protected byte[] read(MetaTag tag) throws TaskException {
		return metaTrustor.read(tag);
	}

	/**
	 * 删除元数据
	 * @param tag 元数据标识
	 * @return 删除成功返回真，否则假
	 */
	protected boolean remove(MetaTag tag) {
		return metaTrustor.remove(tag);
	}

	/**
	 * 判断元数据存在
	 * @param tag 元数据标识
	 * @return 返回真或者假
	 */
	protected boolean contains(MetaTag tag) {
		return metaTrustor.contains(tag);
	}

	/**
	 * 检查FROM阶段命名主机数目，如果没有弹出异常
	 * @param from FROM阶段命名
	 * @throws TaskException
	 */
	protected void checkFromSites(Phase from) throws TaskException {
		NodeSet set = getFromSeeker().findFromSites(getInvokerId(), from);
		if (set == null || set.isEmpty()) {
			throw new TaskException("cannot be find FROM(diffuse) sites by '%s'", from);
		}
	}

	/**
	 * 检查TO阶段命名主机数目，如果没有弹出异常
	 * @param to TO阶段命名
	 * @throws TaskException
	 */
	protected void checkToSites(Phase to) throws TaskException {
		Logger.debug(getIssuer(), this, "checkToSites", "phase is %s", to);

		NodeSet set = getToSeeker().findToSites(getInvokerId(), to);
		if (set == null || set.isEmpty()) {
			throw new TaskException("cannot be find TO(converge) sites by '%s'", to);
		}
	}

	/**
	 * 根据阶段命名确定WORK节点主机数目
	 * @param to TO阶段命名
	 * @return 确定WORK节点主机数目
	 * @throws TaskException
	 */
	protected int findToSites(Phase to) throws TaskException {
		Logger.debug(getIssuer(), this, "findToSites", "phase is %s", to);

		NodeSet set = getToSeeker().findToSites(getInvokerId(), to);
		if (set == null || set.isEmpty()) {
			throw new TaskException("cannot be find to sites by '%s'", to);
		}
		return set.size();
	}

	/**
	 * 根据数据表名查找数据表。<br>
	 * 查找时首先判断用户签名合法，然后才去查找数据表。
	 * 
	 * @param space 数据表名
	 * @return 返回Table实例，如果没有找到，弹出异常。
	 * @throws TaskException - 任务异常。
	 */
	protected Table findTable(Space space) throws TaskException {
		// 从数据节点选择器中找到表配置
		Table table = getFromSeeker().findFromTable(getInvokerId(), space);
		if (table == null) {
			throw new TaskNotFoundException("cannot be find '%s'", space);
		}
		return table;
	}

	/**
	 * 根据列空间查找对应的列属性
	 * @param dock 列空间
	 * @return 返回列属性ColumnAttribute，如果没有找到弹出异常
	 * @throws TaskException - 任务异常。
	 */
	protected ColumnAttribute findAttribute(Dock dock) throws TaskException {
		Table table = findTable(dock.getSpace());
		// 找到对应的列属性
		ColumnAttribute attribute = table.find(dock.getColumnId());
		if (attribute == null) {
			throw new TaskNotFoundException("cannot be find '%s'", dock);
		}
		return attribute;
	}

	/**
	 * 根据数据类型建立一个列索引范围平衡分割器
	 * @param family 列数据类型，见ColumnType中定义
	 * @return IndexBalancer子类实例
	 * @throws TaskException - 参数异常
	 */
	protected IndexBalancer createBalancer(byte family) throws TaskException {
		IndexBalancer balancer = IndexBalancerCreator.create(family);
		if (balancer == null) {
			throw new TaskException("illegal family: %d", family);
		}
		return balancer;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#destroy()
	 */
	@Override
	protected void destroy() {
		super.destroy();
		fromSeeker = null;
		toSeeker = null;
		metaTrustor = null;
	}

}