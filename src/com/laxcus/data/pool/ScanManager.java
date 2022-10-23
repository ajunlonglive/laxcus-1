/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.pool;

import com.laxcus.access.*;
import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.index.*;
import com.laxcus.distribute.establish.mid.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.scan.*;
import com.laxcus.task.mid.*;
import com.laxcus.util.*;

/**
 * SCAN阶段资源存取管理池 <br>
 * 
 * 提供SCAN资源检索操作，以及表的锁定、解除锁定操作。
 * 
 * @author scott.liang
 * @version 1.1 9/29/2011
 * @since laxcus 1.0
 */
public final class ScanManager extends MidPool implements ScanTrustor {

	/** 资源池 **/
	private StaffOnDataPool staffPool;

	/** 调用器池 **/
	private DataInvokerPool invokerPool;

	/**
	 * 设置DATA节点资源池
	 * @param e DATA节点资源池实例
	 */
	public void setStaffPool(StaffOnDataPool e) {
		Laxkit.nullabled(e);
		staffPool = e;
	}

	/**
	 * 设置DATA节点异步调用器池
	 * @param e DATA节点异步调用器池实例
	 */
	public void setInvokerPool(DataInvokerPool e) {
		Laxkit.nullabled(e);
		invokerPool = e;
	}

	/** SCAN阶段任务管理池句柄 **/
	private static ScanManager selfHandle = new ScanManager();

	/**
	 * 构造一个默认和私有的SCAN阶段任务管理池
	 */
	private ScanManager() {
		super();
	}

	/**
	 * 返回管理池句柄
	 * @return ScanManager实例
	 */
	public static ScanManager getInstance() {
		// 安全检查
		VirtualPool.check("ScanManager.getInstance");
		// 返回句柄
		return ScanManager.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");
		while (!isInterrupted()) {
			sleep();
		}
		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {

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

	/**
	 * 判断用户签名有效
	 * @param siger 用户签名
	 * @throws TaskSecurityException
	 */
	private void available(Siger siger) throws TaskSecurityException {
		if (!staffPool.allow(siger)) {
			throw new TaskSecurityException("safe denied '%s'", siger);
		}
	}

	/**
	 * 根据调用器编号，判断签名有效
	 * @param invokerId 调用器编号
	 * @throws TaskSecurityException
	 */
	private void available(long invokerId) throws TaskSecurityException {
		Siger siger = findIssuer(invokerId);
		available(siger);
	}

	/**
	 * 判断用户签名和数据表名有效
	 * @param siger 用户签名
	 * @param space 数据表名
	 * @throws TaskSecurityException
	 */
	private void available(Siger siger, Space space) throws TaskSecurityException {
		if (!staffPool.allow(siger, space)) {
			throw new TaskSecurityException("safe denied '<%s>/%s'", siger, space);
		}
	}

	/**
	 * 根据调用器编号和数据表名，判断一个账号和数据表名有效
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @throws TaskSecurityException
	 */
	private void available(long invokerId, Space space) throws TaskSecurityException {
		Siger siger = findIssuer(invokerId);
		available(siger, space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.SigerTrustor#(long)
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.SiteTrustor#getLocal(long)
	 */
	@Override
	public Node getLocal(long invokerId) throws TaskException {
		// 根据调用器编号判断签名有效
		available(invokerId);
		// 返回监听地址
		return getLocal(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.scan.ScanTrustor#hasScanTable(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean hasScanTable(long invokerId, Space space) throws TaskException {
		// 判断有效
		available(invokerId, space);
		// 判断表存在
		return staffPool.hasTable(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.scan.ScanTrustor#findScanTable(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findScanTable(long invokerId, Space space) throws TaskException {
		// 判断有效
		available(invokerId, space);
		// 查找表
		return staffPool.findTable(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.scan.ScanTrustor#detect(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public ScanField detect(long invokerId, Space space) throws TaskException {
		// 检查DATA节点表记录，如果没有不接受
		if (!hasScanTable(invokerId, space)) {
			Logger.error(this, "detect", "cannot find '%s'", space);
			throw new ScanTaskException("cannot be find '%s'", space);
		}

		StubArea area = null;
		try {
			area = AccessTrustor.findIndex(space);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		if (area == null) {
			Logger.error(this, "detect", "'%s' is empty!", space);
			throw new ScanTaskException("%s is null!", space);
		}

		Logger.debug(this, "detect", "'%s' element size:%d", space, area.size());

		// 转换
		return createScanField(area);
	}

	/**
	 * 建立SCAN阶段映像数据域
	 * @param area StubArea实例
	 * @return EstablishScanField
	 * @throws ScanTaskException
	 */
	private ScanField createScanField(StubArea area) throws ScanTaskException {
		Space space = area.getSpace();

		// 建立域
		Node node = getLocal(false); // launcher.getListener();
		EstablishFlag flag = new EstablishFlag(space, node);
		ScanField field = new ScanField(flag); 

		for (StubItem item : area.list()) {
			// 判断通过（忽略CACHE状态数据块）
			boolean allow = (item.isPrime() && item.isChunk());
			if (!allow) {
				continue;
			}

			// 只保存已经封闭的数据块
			field.addStubItem(item);
		}

		Logger.debug(this, "createEstablishScanField", "'%s' element size:%d", space, field.getStubCount());

		return field;
	}

}