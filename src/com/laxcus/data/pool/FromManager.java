/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.pool;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.trust.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.law.cross.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.from.*;
import com.laxcus.util.*;

/**
 * CONDUCT.FROM本地资源管理器。<br>
 * 管理器实现FromTrustor接口。
 * 
 * @author scott.liang
 * @version 1.0 9/23/2017
 * @since laxcus 1.0
 */
public class FromManager extends TrackManager implements FromTrustor {

	/** FROM代理器实例 **/
	private static FromManager selfHandle = new FromManager();

	/** 资源池 **/
	private StaffOnDataPool staffPool;

	/**
	 * 设置DATA节点资源池
	 * @param e DATA节点资源池实例
	 */
	public void setStaffPool(StaffOnDataPool e) {
		Laxkit.nullabled(e);
		staffPool = e;
	}


	/**
	 * 构造默认和私有的CONDUCT.FROM本地资源管理器
	 */
	private FromManager() {
		super();
	}

	/**
	 * 返回CONDUCT.FROM本地资源管理器
	 * @return CONDUCT.FROM本地资源管理器
	 */
	public static FromManager getInstance() {
		// 安全检查
		TrackManager.check("FromManager.getInstance");
		// 返回句柄
		return FromManager.selfHandle;
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
		Siger issuer = invoker.getIssuer();
		if (issuer == null) {
			throw new TaskSecurityException("cannot be define issuer by %d", invokerId);
		}
		return issuer;
	}

	/**
	 * 判断用户签名有效
	 * @param issuer 用户签名
	 * @throws TaskSecurityException
	 */
	protected void available(Siger issuer) throws TaskSecurityException {
		if (!staffPool.allow(issuer)) {
			throw new TaskSecurityException("security denied '%s'", issuer);
		}
	}

	/**
	 * 根据调用器编号，判断签名有效
	 * @param invokerId 调用器编号
	 * @throws TaskSecurityException
	 */
	protected void available(long invokerId) throws TaskSecurityException {
		Siger issuer = findIssuer(invokerId);
		available(issuer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.SiteTrustor#getLocal(long)
	 */
	@Override
	public Node getLocal(long invokerId) throws TaskException {
		// 判断调用器有效
		available(invokerId);
		// 返回本地站点地址
		return super.getLocal(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.SigerTrustor#(long)
	 */
	@Override
	public boolean allow(long invokerId) throws TaskException {
		Siger issuer = findIssuer(invokerId);
		return staffPool.allow(issuer);
	}

	/* 
	 * (non-Javadoc)
	 * @see com.laxcus.task.SigerTrustor#allow(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean allow(long invokerId, Space space) throws TaskException {
		Siger issuer = findIssuer(invokerId);
		return staffPool.allow(issuer, space);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.SigerTrustor#allow(long, com.laxcus.law.cross.CrossFlag)
	 */
	@Override
	public boolean allow(long invokerId, CrossFlag flag) throws TaskException {
		Siger issuer = findIssuer(invokerId);
		return staffPool.allow(issuer, flag);
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

	/**
	 * 根据调用器编号和数据表名，检查操作权限
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @throws TaskException
	 */
	private void available(long invokerId, Space space) throws TaskException {
		// 签名
		Siger issuer = findIssuer(invokerId);
		// 如果用户是被授权用户，判断它允许指定表操作
		if (!staffPool.allow(issuer, space)) {
			throw new TaskSecurityException("security denied '%s'", issuer);
		}
	}

	/**
	 * 根据调用器编号，检查操作权限
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @param operator 共享操作符
	 * @throws TaskException
	 */
	private void available(long invokerId, Space space, int operator) throws TaskException {
		// 签名
		Siger issuer = findIssuer(invokerId);
		CrossFlag flag = new CrossFlag(space, operator);
		// 如果用户是被授权用户，判断它允许操作，包括用户自己表或者被授权表
		if (!staffPool.allow(issuer, flag)) {
			throw new TaskSecurityException("security denied '%s#%s'", issuer, flag);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.from.FromTrustor#findFromTable(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findFromTable(long invokerId, Space space) throws TaskException {
		// 判断操作允许
		available(invokerId, space);
		// 从内存里查找表配置
		return staffPool.findTable(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.from.FromTrustor#select(long, com.laxcus.command.access.Select, long)
	 */
	@Override
	public byte[] select(long invokerId, Select cmd, long stub) throws TaskException {
		// 检查操作权限
		available(invokerId, cmd.getSpace(), CrossOperator.SELECT);
		// 通过SELECT代理，执行检索操作（检索代码太多，做一个单独的类处理）
		SelectTasker task = new SelectTasker();
		// 返回检索结果
		return task.process(cmd, stub);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.from.FromTrustor#insert(long, com.laxcus.command.access.Insert)
	 */
	@Override
	public int insert(long invokerId, Insert cmd) throws TaskException {
		// 检查操作权限
		available(invokerId, cmd.getSpace(), CrossOperator.INSERT);

		// 通过本地代理去处理INSERT操作，调用器同时要完成数据备份到从站点的工作
		TrustInsert trust = new TrustInsert(cmd);
		TrustInsertHook hook = new TrustInsertHook();
		ShiftTrustInsert shift = new ShiftTrustInsert(trust, hook);
		shift.setRelateId(invokerId);

		// 提交给命令管理池
		boolean success = switchPool.press(shift);
		// 不成功返回空值
		if (!success) {
			throw new TaskException("cannot be press!");
		}
		hook.await();

		// 如果有故障，弹出异常
		if (hook.isFault()) {
			throw new TaskException(hook.getFault());
		}

		// 返回写入数据
		return hook.getRows();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.from.FromTrustor#delete(long, com.laxcus.command.access.Delete, long)
	 */
	@Override
	public int delete(long invokerId, Delete cmd, long stub)
	throws TaskException {
		// 检查操作权限
		available(invokerId, cmd.getSpace(), CrossOperator.DELETE);

		// 生成命令
		TrustDelete delete = new TrustDelete(cmd, stub);
		TrustDeleteHook hook = new TrustDeleteHook();
		ShiftTrustDelete shift = new ShiftTrustDelete(delete, hook);
		shift.setRelateId(invokerId);

		// 提交给命令管理池
		boolean success = switchPool.press(shift);
		// 不成功返回空值
		if (!success) {
			throw new TaskException("cannot be press!");
		}
		hook.await();

		// 如果有故障，弹出异常
		if (hook.isFault()) {
			throw new TaskException(hook.getFault());
		}

		// 返回删除行数
		return hook.getRows();
	}

}