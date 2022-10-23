/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish;

import com.laxcus.access.schema.*;
import com.laxcus.command.establish.*;
import com.laxcus.distribute.meta.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.seeker.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 数据构建的规则设计和资源分派接口。<br><br>
 * 
 * 提供对分布数据资源的检索、判断、分析、生成工作。子类有IssueTask、AssignTask两种类型，都运行在CALL站点。<br>
 * 
 * @author scott.liang
 * @version 1.23 11/7/2015
 * @since laxcus 1.0
 */
public abstract class SerialTask extends AccessTask {

	/** SCAN阶段资源向导接口 **/
	private ScanSeeker scanSeeker;

	/** SIFT阶段资源向导接口 **/
	private SiftSeeker siftSeeker;

	/** 元数据存取代理 **/
	private MetaTrustor metaTrustor;

	/**
	 * 构造数据构建的串行处理任务
	 */
	protected SerialTask() {
		super();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#getCommand()
	 */
	@Override
	public Establish getCommand() {
		return (Establish) super.getCommand();
	}

	/**
	 * 设置SCAN阶段资源向导接口
	 * @param e ScanSeeker实例
	 */
	public void setScanSeeker(ScanSeeker e) {
		scanSeeker = e;
	}

	/**
	 * 返回SCAN阶段资源向导接口
	 * @return ScanSeeker实例
	 */
	public ScanSeeker getScanSeeker() {
		return scanSeeker;
	}

	/**
	 * 设置SIFT阶段资源向导接口
	 * @param e SiftSeeker实例
	 */
	public void setSiftSeeker(SiftSeeker e) {
		siftSeeker = e;
	}

	/**
	 * 返回SIFT阶段资源向导接口
	 * @return SiftSeeker实例
	 */
	public SiftSeeker getSiftSeeker() {
		return siftSeeker;
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
	 * @param off 有效数据开始下标
	 * @param len 有效数据长度
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
	 * @return 关联的元数据字节数组
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
	 * 根据数据表名，查找数据表
	 * @param space 数据表名
	 * @return 返回数据表。如果没有找到，弹出异常
	 * @throws TaskException
	 */
	protected Table findTable(Space space) throws TaskException {
		Table table = getScanSeeker().findScanTable(getInvokerId(), space);
		if (table == null) {
			throw new TaskException("cannot be find '%s#%s'", getIssuer(), space);
		}
		return table;
	}

	/**
	 * 查找SCAN阶段的主站点地址集合
	 * @param space 数据表名
	 * @return 站点集合。如果没有找到，或者是空集合，弹出异常
	 * @throws TaskException
	 */
	protected NodeSet findScanSites(Space space) throws TaskException {
		NodeSet set = getScanSeeker().findScanSites(getInvokerId(), space);
		if (set == null || set.isEmpty()) {
			throw new TaskSiteNotFoundException("cannot be find scan site by '%s#%s'", getIssuer(), space);
		}
		return set;
	}

	/**
	 * 查找SIFT阶段命名的BUILD站点集合
	 * @param phase SIFT阶段命名
	 * @return 站点集合
	 * @throws TaskException
	 */
	protected NodeSet findSiftSites(Phase phase) throws TaskException {
		NodeSet set = getSiftSeeker().findSiftSites(getInvokerId(), phase);
		if (set == null || set.isEmpty()) {
			throw new TaskSiteNotFoundException("cannot be find sift site by '%s'", phase);
		}
		return set;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#destroy()
	 */
	@Override
	protected void destroy() {
		super.destroy();
		scanSeeker = null;
		siftSeeker = null;
		metaTrustor = null;
	}
}