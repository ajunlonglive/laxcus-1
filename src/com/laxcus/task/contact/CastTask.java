/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact;

import com.laxcus.access.schema.*;
import com.laxcus.command.contact.*;
import com.laxcus.distribute.meta.*;
import com.laxcus.task.*;
import com.laxcus.task.contact.seeker.*;

/**
 * CS架构的数据处理和小规模计算分派任务，区别于CONDUCT的大规模数据处理。<br>
 * 提供对网络分布资源和数据的检查、分析、筛选、获取、生成，子类包括：ForkTask,MergeTask <br><br>
 * 
 * @author scott.liang
 * @version 1.0 5/8/2020
 * @since laxcus 1.0
 */
public abstract class CastTask extends AccessTask {

	/** DISTANT阶段资源向导接口（WORK站点） **/
	private DistantSeeker distantSeeker;

	/** 元数据存取代理 **/
	private MetaTrustor metaTrustor;
	

	/**
	 * 构造默认的数据计算规则设计和资源分派任务
	 */
	protected CastTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#getCommand()
	 */
	@Override
	public Contact getCommand() {
		return (Contact) super.getCommand();
	}
	
	/**
	 * 设置DISTANT阶段资源向导接口
	 * @param e DistantSeeker实例
	 */
	public void setDistantSeeker(DistantSeeker e) {
		distantSeeker = e;
	}

	/**
	 * 返回DISTANT阶段资源向导接口
	 * @return DistantSeeker实例
	 */
	public DistantSeeker getDistantSeeker() {
		return distantSeeker;
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
	 * 根据数据表名查找数据表。<br>
	 * 查找时首先判断用户签名合法，然后才去查找数据表。
	 * 
	 * @param space 数据表名
	 * @return 返回Table实例，如果没有找到，弹出异常。
	 * @throws TaskException - 任务异常。
	 */
	protected Table findTable(Space space) throws TaskException {
		// 从数据节点选择器中找到表配置
		Table table = getDistantSeeker().findDistantTable(getInvokerId(), space);
		if (table == null) {
			throw new TaskException("cannot be find '%s'", space);
		}
		return table;
	}
}
