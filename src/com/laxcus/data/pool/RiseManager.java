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
import com.laxcus.access.stub.*;
import com.laxcus.access.stub.index.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.task.mid.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.rise.*;
import com.laxcus.util.*;

/**
 * RISE阶段资源存取管理池 <br>
 * 
 * 为RISE阶段任务组件提供资源代理服务。
 * 
 * @author scott.liang
 * @version 1.1 9/29/2011
 * @since laxcus 1.0
 */
public final class RiseManager extends MidPool implements RiseTrustor {

	//	/** DATA节点启动器 **/
	//	private DataLauncher launcher;
	//	/**
	//	 * 设置DATA节点启动器
	//	 * @param e DATA节点启动器
	//	 */
	//	public void setDataLauncher(DataLauncher e){
	//		launcher = e;
	//	}

	/** 资源池 **/
	private StaffOnDataPool staffPool;

	/** 调用器池 **/
	private DataInvokerPool invokerPool;

	/** 命令切换池 **/
	private SwitchPool switchPool;

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

	/**
	 * 设置命令切换池
	 * @param e 命令切换池实例
	 */
	public void setSwitchPool(SwitchPool e) {
		Laxkit.nullabled(e);
		switchPool = e;
	}

	/** RISE阶段任务管理池句柄 **/
	private static RiseManager selfHandle = new RiseManager();

	/**
	 * 构造一个默认和私有的RISE阶段任务管理池
	 */
	private RiseManager() {
		super();
	}

	/**
	 * 返回RISE阶段任务管理池
	 * @return RiseManager实例
	 */
	public static RiseManager getInstance() {
		// 安全检查
		VirtualPool.check("RiseManager.getInstance");
		// 返回句柄
		return RiseManager.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
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

	/* (non-Javadoc)
	 * @see com.laxcus.task.establish.rise.RiseTrustor#getLocal()
	 */
	@Override
	public Node getLocal(long invokerId) throws TaskException {
		// 判断签名有效
		available(invokerId);
		// 以复制方式，返回监听地址
		return getLocal(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.rise.RiseTrustor#hasRiseTable(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean hasRiseTable(long invokerId, Space space) throws TaskException {
		// 判断签名有效
		available(invokerId, space);
		// 判断数据表名存在
		return staffPool.hasTable(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.rise.RiseTrustor#findRiseTable(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findRiseTable(long invokerId, Space space) throws TaskException {
		// 判断签名有效
		available(invokerId, space);
		// 返回表实例。这个操作只在本地执行，不发生网络通信。
		return staffPool.findTable(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.rise.RiseTrustor#hasChunk(long, com.laxcus.access.schema.Space, long)
	 */
	@Override
	public boolean hasChunk(long invokerId, Space space, long stub) throws TaskException {
		// 判断签名有效
		available(invokerId, space);
		// 判断数据块存在
		return AccessTrustor.hasChunk(space, stub);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.rise.RiseTrustor#findChunk(long, com.laxcus.access.schema.Space, long)
	 */
	@Override
	public StubItem findChunk(long invokerId, Space space, long stub) throws TaskException {
		// 判断签名有效
		available(invokerId, space);
		// 判断数据块存在
		boolean success = AccessTrustor.hasChunk(space, stub);
		// 不存在返回空
		if (!success) {
			return null;
		}
		// 查询数据块文件名路径
		String filename = AccessTrustor.findChunkPath(space, stub);
		if (filename == null) {
			return null;
		}
		// 获取文件长度
		long filen = AccessTrustor.length(filename);
		return new StubItem(stub, filen);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.rise.RiseTrustor#deleteChunk(long, com.laxcus.access.schema.Space, long)
	 */
	@Override
	public StubItem deleteChunk(long invokerId, Space space, long stub) throws TaskException {
		// 判断签名有效
		available(invokerId, space);
		// 判断数据块存在
		boolean success = AccessTrustor.hasChunk(space, stub);
		// 不存在返回空
		if (!success) {
			return null;
		}

		// 查询数据块文件名路径
		String filename = AccessTrustor.findChunkPath(space, stub);
		if (filename == null) {
			return null;
		}
		// 获取文件长度
		long filen = AccessTrustor.length(filename);
		StubItem item = new StubItem(stub, filen);

		// 删除数据块
		int ret = AccessTrustor.deleteChunk(space, stub);
		success = (ret >= 0);

		Logger.note(this, "deleteChunk", success,
				"delete '%s', result code:%d", new StubFlag(space, stub), ret);

		return (success ? item : null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.rise.RiseTrustor#updateChunk(long, com.laxcus.site.Node, com.laxcus.access.schema.Space, long)
	 */
	@Override
	public StubItem updateChunk(long invokerId, Node hub, Space space, long stub) throws TaskException {
		// 判断签名有效
		available(invokerId, space);
		// 生成文件名称
		String filename = AccessTrustor.doChunkFile(space, stub);

		// 设置命令
		StubFlag flag = new StubFlag(space, stub);
		// 下载数据块
		DownloadMass cmd = new DownloadMass(flag);
		// 转发命令
		DownloadMassHook hook = new DownloadMassHook();
		ShiftDownloadMass shift = new ShiftDownloadMass(hub, cmd, hook, filename);

		// 交给切换池，启动下载
		boolean success = switchPool.press(shift);
		if (!success) {
			throw new RiseTaskException("cannot be accepted! %s", flag);
		}
		// 等待，直到完成
		hook.await();

		// 判断成功
		success = hook.isSuccessful();
		if (!success) {
			throw new RiseTaskException("cannot be download %s from %s", flag, hub);
		}

		// 查询索引
		StubArea area = AccessTrustor.findIndex(space);
		success = (area != null);
		// 查找数据块
		if (success) {
			for (StubItem item : area.list()) {
				if (item.getStub() == stub) {
					Logger.debug(this, "updateChunk", "this is %s", item);
					return item;
				}
			}
		}

		Logger.error(this, "updateChunk", "cannot be find %s", hub, flag);
		return null;
	}

}