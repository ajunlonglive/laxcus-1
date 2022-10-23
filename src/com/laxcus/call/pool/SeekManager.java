/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import com.laxcus.access.schema.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.law.cross.*;
import com.laxcus.task.*;
import com.laxcus.util.*;

/**
 * CALL节点本地共享资源管理器。部署在CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 5/2/2018
 * @since laxcus 1.0
 */
public class SeekManager extends TrackManager {

	/** 资源管理池 **/
	protected StaffOnCallPool staffPool;

	/**
	 * 构造默认的CALL节点本地共享资源管理器
	 */
	protected SeekManager() {
		super();
	}

	/**
	 * 设置CALL站点资源管理池
	 * @param e ALL站点资源管理池句柄
	 */
	public void setStaffPool(StaffOnCallPool e) {
		Laxkit.nullabled(e);
		staffPool = e;
	}

	/**
	 * 判断用户签名有效
	 * @param siger 用户签名
	 * @return 有效返回真，否则假
	 */
	protected boolean allow(Siger siger) {
		return staffPool.allow(siger);
	}

	/**
	 * 判断用户签名和数据表名有效
	 * @param siger 用户签名
	 * @param space 数据表名
	 * @return 有效返回真，否则假
	 */
	protected boolean allow(Siger siger, Space space) {
		// 判断用户签名和数据表名有效且匹配
		return staffPool.allow(siger, space);
	}

	/**
	 * 判断用户签名和资源共享有效
	 * @param siger 用户签名
	 * @param flag 资源共享标识
	 * @return 有效返回真，否则假
	 */
	protected boolean allow(Siger siger, CrossFlag flag) {
		return staffPool.allow(siger, flag);
	}

	/**
	 * 从调用器编号中，推断出用户签名
	 * @param invokerId 调用器编号
	 * @return 返回用户签名
	 * @throws TaskSecurityException
	 */
	protected Siger findIssuer(long invokerId) throws TaskSecurityException {
		EchoInvoker invoker = invokerPool.findInvoker(invokerId);
		if (invoker == null) {
			throw new TaskSecurityException("cannot be find issuer by %d", invokerId);
		}
		return invoker.getIssuer();
	}

	/**
	 * 判断用户签名有效
	 * @param siger 用户签名
	 * @throws TaskSecurityException
	 */
	protected void available(Siger siger) throws TaskSecurityException {
		boolean success = allow(siger);
		if (!success) {
			throw new TaskSecurityException("security denied '%s'", siger);
		}
	}

	/**
	 * 根据调用器编号，判断签名有效
	 * @param invokerId 调用器编号
	 * @throws TaskSecurityException
	 */
	protected void available(long invokerId) throws TaskSecurityException {
		Siger siger = findIssuer(invokerId);
		available(siger);
	}

	/**
	 * 根据调用器编号和表名，判断调用者的合法性，返回调用器实例
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @throws TaskException
	 */
	protected void available(long invokerId, Space space) throws TaskException {
		// 查找调用器
		EchoInvoker invoker = invokerPool.findInvoker(invokerId);
		if (invoker == null) {
			throw new TaskSecurityException("cannot be find issuer by %d", invokerId);
		}

		// 判断用户签名和数据表有效
		Siger siger = invoker.getIssuer();
		if (!allow(siger, space)) {
			throw new TaskSecurityException("security denied '%s'", siger, space);
		}
	}

	/**
	 * 根据调用器编号、表名、共享操作符，判断调用者的合法性。返回调用器实例
	 * 此处分两种情况：如果表所有调用者自己的本身，共享操作判断忽略。如果是被授权人，使用共享操作符判断合法性。
	 * 
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @param operator 共享操作符
	 * @throws TaskException
	 */
	protected void available(long invokerId, Space space, int operator) throws TaskException {
		// 查找调用器
		EchoInvoker invoker = invokerPool.findInvoker(invokerId);
		if (invoker == null) {
			throw new TaskSecurityException("cannot be find issuer by %d", invokerId);
		}
		// 判断操作合法
		Siger siger = invoker.getIssuer();
		CrossFlag flag = new CrossFlag(space, operator);
		if (!allow(siger, flag)) {
			throw new TaskSecurityException("security denied '<%s>/%s'", siger, flag);
		}
	}
}
