/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.pool;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.law.cross.*;
import com.laxcus.task.*;
import com.laxcus.util.*;

/**
 * 用户签名管理器。<br>
 * 根据调用器编号，判断用户业务存在WORK节点上。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/10/2020
 * @since laxcus 1.0
 */
public class IssuerManager extends TrackManager implements SigerTrustor {

	/** CONDUCT.TO/CONTACT.DISTANT本地资源管理器实例 **/
	private static IssuerManager selfHandle = new IssuerManager();

	/** 资源池 **/
	private StaffOnWorkPool staffPool;

	/**
	 * 设置WORK节点资源池
	 * @param e WORK节点资源池实例
	 */
	public void setStaffPool(StaffOnWorkPool e) {
		staffPool = e;
	}

	/**
	 * 构造默认和私有的CONDUCT.TO/CONTACT.DISTANT本地资源管理器
	 */
	private IssuerManager() {
		super();
	}

	/**
	 * 返回CONDUCT.TO/CONTACT.DISTANT本地资源管理器实例
	 * @return CONDUCT.TO/CONTACT.DISTANT本地资源管理器实例
	 */
	public static IssuerManager getInstance() {
		// 安全检查
		TrackManager.check("IssuerManager.getInstance");
		// 返回句柄
		return IssuerManager.selfHandle;
	}

	/**
	 * 从调用器编号中，推断出用户签名
	 * @param invokerId 调用器编号
	 * @return 返回用户签名
	 * @throws TaskSecurityException
	 */
	private Siger findIssuer(long invokerId) throws TaskSecurityException {
		EchoInvoker invoker = invokerPool.findInvoker(invokerId);
		if (invoker == null) {
			throw new TaskSecurityException("cannot be find issuer by %d", invokerId);
		}
		Siger siger = invoker.getIssuer();
		if (siger == null) {
			throw new TaskSecurityException("cannot be define issuer by %d", invokerId);
		}
		return siger;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.SigerTrustor#allow(long)
	 */
	@Override
	public boolean allow(long invokerId) throws TaskException {
		Siger siger = findIssuer(invokerId);
		return staffPool.allow(siger);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.SigerTrustor#allow(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean allow(long invokerId, Space space) throws TaskException {
		Siger siger = findIssuer(invokerId);
		return staffPool.allow(siger, space);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.SigerTrustor#allow(long, com.laxcus.law.cross.CrossFlag)
	 */
	@Override
	public boolean allow(long invokerId, CrossFlag flag) throws TaskException {
		Siger siger = findIssuer(invokerId);
		return staffPool.allow(siger, flag);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.SigerTrustor#getMiddleBufferSize(long)
	 */
	@Override
	public long getMiddleBufferSize(long invokerId) throws TaskException {
		Siger siger = findIssuer(invokerId);
		// 查找匹配的资源引用，如果没有，弹出异常
		Refer refer = staffPool.findRefer(siger);
		if (refer == null) {
			throw new TaskSecurityException("cannot be find refer by %s#%d", siger, invokerId);
		}
		// 返回它的中间缓存尺寸
		return refer.getUser().getMiddleBuffer();
	}
}