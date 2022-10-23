/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.pool;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.stub.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.law.cross.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.contact.distant.*;
import com.laxcus.util.*;

/**
 * SWIFT.DISTANT本地资源管理器
 * 
 * @author scott.liang
 * @version 1.0 5/3/2020
 * @since laxcus 1.0
 */
public class DistantManager extends TrackManager implements DistantTrustor {

	/** 快捷组件本地资源管理器 **/
	private static DistantManager selfHandle = new DistantManager();

	/** 资源池 **/
	private StaffOnWorkPool staffPool;

	/**
	 * 设置WORK节点资源池
	 * @param e WORK节点资源池实例
	 */
	public void setStaffPool(StaffOnWorkPool e) {
		Laxkit.nullabled(e);
		staffPool = e;
	}

	/**
	 * 构造默认和私有的快捷组件本地资源管理器
	 */
	private DistantManager() {
		super();
	}

	/**
	 * 返回快捷组件本地资源管理器类
	 * @return 快捷组件本地资源管理器
	 */
	public static DistantManager getInstance() {
		// 安全检查
		TrackManager.check("DistantManager.getInstance");
		// 返回本地实例
		return DistantManager.selfHandle;
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
			throw new TaskSecurityException("security denied '%s'", siger);
		}
	}

	/**
	 * 根据调用器编号，判断签名有效
	 * @param invokerId 调用器编号
	 * @throws TaskSecurityException
	 */
	private void available(long invokerId) throws TaskSecurityException {
		Siger signer = findIssuer(invokerId);
		available(signer);
	}

	/**
	 * 判断有效
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @throws TaskSecurityException
	 */
	private void available(long invokerId, Space space) throws TaskSecurityException {
		Siger siger = findIssuer(invokerId);
		if (!staffPool.allow(siger, space)) {
			throw new TaskSecurityException("security denied '<%s>/%s'", siger, space);
		}
	}

	/**
	 * 判断有效
	 * @param invokerId 调用器编号
	 * @param space 数据表名
	 * @param operator 共享操作符
	 * @throws TaskSecurityException
	 */
	private void available(long invokerId, Space space, int operator) throws TaskSecurityException {
		// 判断操作合法
		Siger siger = findIssuer(invokerId);
		CrossFlag flag = new CrossFlag(space, operator);
		if (!staffPool.allow(siger, flag)) {
			throw new TaskSecurityException("security denied '<%s>/%s'", siger, flag);
		}
	}

	/**
	 * 去CALL站点查询数据块编号
	 * @param invokerId 
	 * @param hub CALL站点地址
	 * @param cmd 查询数据块命令
	 * @return StubEntry列表
	 * @throws DistantTaskException
	 */
	private List<StubEntry> findStubSites(long invokerId, Node hub, FindStubSite cmd) throws TaskException {
		// 必须是CALL站点，否则是错误
		if (!hub.isCall()) {
			throw new DistantTaskException("must be call site! %s", hub);
		}

		// 检查有效性
		available(invokerId, cmd.getSpace());

		// 钩子和转发命令
		FindStubSiteHook hook = new FindStubSiteHook();
		ShiftFindStubSite shift = new ShiftFindStubSite(hub, cmd, hook);
		shift.setRelateId(invokerId);

		// 提交给WORK命令状态切换管理池，走admit/dispatch流程。
		boolean success = switchPool.admit(shift);
		if (!success) {
			throw new DistantTaskException("cannot be admit!");
		}
		// 钩子等待
		hook.await();

		// 判断存在故障
		Throwable fault = hook.getFault();
		if(fault != null) {
			throw new DistantTaskException(fault);
		}

		FindStubSiteProduct product = hook.getStubSiteProduct();
		// 出错
		if (product == null) {
			throw new DistantTaskException("cannot be find stub site");
		}

		// 返回关联的DATA站点地址
		return product.list();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#allow(long)
	 */
	@Override
	public boolean allow(long invokerId) throws TaskException {
		Siger siger = findIssuer(invokerId);
		return staffPool.allow(siger);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#allow(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean allow(long invokerId, Space space) throws TaskException {
		Siger siger = findIssuer(invokerId);
		return staffPool.allow(siger, space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#allow(long, com.laxcus.law.cross.CrossFlag)
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
	 * @see com.laxcus.swift.DistantTrustor#getLocal(long)
	 */
	@Override
	public Node getLocal(long invokerId) throws TaskException {
		// 判断调用器有效
		available(invokerId);
		// 返回本地站点地址
		return getLocal(true);
	}

	/**
	 * 去HOME站点搜索表
	 * @param space 数据表名
	 * @return 返回表实例，或者空指针
	 */
	private Table searchTable(long invokerId, Space space) throws TaskNotFoundException {
		TakeTable cmd = new TakeTable(space);
		TakeTableHook hook = new TakeTableHook();
		ShiftTakeTable shift = new ShiftTakeTable(cmd, hook);
		shift.setRelateId(invokerId); // 设置调用器关联编号

		// 关给调用器处理
		boolean success = switchPool.press(shift);
		if (!success) {
			throw new TaskNotFoundException("cannot submit to hub");
		}
		// 进入等待状态
		hook.await();

		// 返回表实例
		return hook.getTable();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.contact.distant.DistantTrustor#findDistantTable(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findDistantTable(long invokerId, Space space) throws TaskException {		
		// 检查有效性
		available(invokerId, space);

		// 查找本地缓存的数据表
		Table table = staffPool.findLocalTable(space);
		// 没有，去HOME站点查找
		if (table == null) {
			table = searchTable(invokerId, space);
		}
		if (table == null) {
			throw new TaskNotFoundException("cannot be find %s", space);
		}
		return table;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#findPrimeSites(long, com.laxcus.site.Node, com.laxcus.access.schema.Space)
	 */
	@Override
	public List<Node> findPrimeSites(long invokerId, Node hub, Space space) throws TaskException {
		if(!hub.isCall()) {
			throw new DistantTaskException("must be call site! %s", hub);
		}

		// 检查有效性
		available(invokerId, space);

		// 生成命令
		FindSpacePrimeSite cmd = new FindSpacePrimeSite(space);		
		FindSpacePrimeSiteHook hook = new FindSpacePrimeSiteHook();
		ShiftFindSpacePrimeSite shift = new ShiftFindSpacePrimeSite(hub, cmd, hook);
		shift.setRelateId(invokerId);

		// 提交给WORK命令状态切换管理池，走admit/dispatch流程。
		boolean success = switchPool.admit(shift);
		if (!success) {
			throw new DistantTaskException("cannot be admit!");
		}
		// 钩子等待
		hook.await();

		// 判断存在故障
		Throwable fault = hook.getFault();
		if(fault != null) {
			throw new DistantTaskException(fault);
		}

		FindSpacePrimeSiteProduct product = hook.getProduct();
		if(product == null) {
			throw new TaskNotFoundException("cannot be find");
		}
		return product.list();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#filteStubs(long, com.laxcus.site.Node, com.laxcus.command.access.Select)
	 */
	@Override
	public List<Long> filteStubs(long invokerId, Node hub, Select select)
	throws TaskException {
		// 本方法只去DATA站点查询，非DATA站点是错误
		if (!hub.isData()) {
			throw new DistantTaskException("must be data site! %s", hub);
		}

		// 检查有效性
		available(invokerId, select.getSpace(), CrossOperator.SELECT);

		// 生成过滤数据块命令
		FilteSelectStub cmd = new FilteSelectStub(select);		
		// 转发查询
		FilteSelectStubHook hook = new FilteSelectStubHook();
		ShiftFilteSelectStub shift = new ShiftFilteSelectStub(hub, cmd, hook);
		shift.setRelateId(invokerId); //设置异步调用器关联编号

		// 提交给WORK命令状态切换管理池，走admit/dispatch流程。
		boolean success = switchPool.admit(shift);
		if (!success) {
			throw new DistantTaskException("cannot be admit!");
		}
		// 钩子等待
		hook.await();

		// 判断存在故障
		Throwable fault = hook.getFault();
		if(fault != null) {
			throw new DistantTaskException(fault);
		}

		// 返回结果
		StubProduct product = hook.getProduct();
		if(product == null) {
			throw new DistantTaskException("cannot be find");
		}
		return product.list();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#findStubSites(long, com.laxcus.site.Node, com.laxcus.access.schema.Space, java.util.List)
	 */
	@Override
	public List<StubEntry> findStubSites(long invokerId, Node hub, Space space, List<Long> stubs)
	throws TaskException {
		ChoiceStubSite cmd = new ChoiceStubSite(space, stubs);
		return findStubSites(invokerId, hub, cmd);
	}

	/* 
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#findStubPrimeSites(long, com.laxcus.site.Node, com.laxcus.access.schema.Space, java.util.List)
	 */
	@Override
	public List<StubEntry> findStubPrimeSites(long invokerId, Node hub,
			Space space, List<Long> stubs) throws TaskException {
		FindStubPrimeSite cmd = new FindStubPrimeSite(space, stubs);
		return findStubSites(invokerId, hub, cmd);
	}

	/* 
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#select(long, com.laxcus.site.Node, com.laxcus.command.access.Select, long)
	 */
	@Override
	public byte[] select(long invokerId, Node hub, Select select, long stub)
	throws TaskException {
		if (!hub.isData()) {
			throw new DistantTaskException("must be data site! %s", hub);
		}

		// 不允许有嵌套
		if (select.hasNested()) {
			throw new DistantTaskException("cannot be nested!");
		}

		// 检查有效性
		available(invokerId, select.getSpace(), CrossOperator.SELECT);

		// 生成检索命令
		CastSelect cmd = new CastSelect(select, stub);
		// 钩子和转发命令
		CastSelectHook hook = new CastSelectHook();
		ShiftCastSelect shift = new ShiftCastSelect(hub, cmd, hook);
		shift.setRelateId(invokerId);

		// 提交给WORK命令状态切换管理池，走admit/dispatch流程。
		boolean success = switchPool.admit(shift);
		if (!success) {
			throw new DistantTaskException("cannot be admit!");
		}
		// 钩子等待
		hook.await();

		// 判断存在故障
		Throwable fault = hook.getFault();
		if(fault != null) {
			throw new DistantTaskException(fault);
		}

		// 返回实体数据，或者空
		return hook.getData();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#insert(long, com.laxcus.site.Node, com.laxcus.command.access.Insert)
	 */
	@Override
	public AssumeInsert insert(long invokerId, Node hub, Insert insert)
			throws TaskException {
		// 判断是DATA站点
		if (!hub.isData() || !hub.isMaster()) {
			throw new DistantTaskException("must be master data site! %s", hub);
		}

		// 检查有效性
		available(invokerId, insert.getSpace(), CrossOperator.INSERT);

		// INSERT钩子
		InsertHook hook = new InsertHook();
		ShiftInsert shift = new ShiftInsert(hub, insert, hook);
		shift.setRelateId(invokerId);

		// 提交给WORK命令状态切换管理池，走admit/dispatch流程。
		boolean success = switchPool.admit(shift);
		if (!success) {
			throw new DistantTaskException("cannot be admit!");
		}
		// 钩子等待
		hook.await();

		// 判断存在故障
		Throwable fault = hook.getFault();
		if(fault != null) {
			throw new DistantTaskException(fault);
		}
		// 判断成功
		return hook.getProduct();
	}

	/* 
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#decide(long, com.laxcus.echo.Cabin, com.laxcus.command.access.AssertInsert)
	 */
	@Override
	public AssumeInsert decide(long invokerId, Cabin hub, AssertInsert cmd)
			throws TaskException {
		// 判断是DATA站点
		if (!hub.getNode().isData()) {
			throw new DistantTaskException("must be data site! %s", hub);
		}

		// 检查有效性
		available(invokerId, cmd.getSpace(), CrossOperator.INSERT);

		// 命令钩子
		InsertHook hook = new InsertHook();
		ShiftAssertInsert shift = new ShiftAssertInsert(hub, cmd, hook);
		shift.setRelateId(invokerId);

		// 提交给WORK命令状态切换管理池，走admit/dispatch流程。
		boolean success = switchPool.admit(shift);
		if (!success) {
			throw new DistantTaskException("cannot be admit!");
		}
		// 钩子等待
		hook.await();

		// 判断存在故障
		Throwable fault = hook.getFault();
		if(fault != null) {
			throw new DistantTaskException(fault);
		}
		// 判断成功
		return hook.getProduct();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#delete(long, com.laxcus.site.Node, com.laxcus.command.access.Delete, java.util.List)
	 */
	@Override
	public AssumeDelete delete(long invokerId, Node hub, Delete delete,
			List<Long> stubs) throws TaskException {
		// 判断是DATA站点
		if (!hub.isData()) {
			throw new DistantTaskException("must be data site! %s", hub);
		}
		// 不允许有嵌套
		if (delete.hasNested()) {
			throw new DistantTaskException("cannot be nested");
		}

		// 检查有效性
		available(invokerId, delete.getSpace(), CrossOperator.DELETE);

		// SELECT投递命令
		CastDelete cast = new CastDelete(delete, stubs);	
		// 命令钩子和转发命令
		DeleteHook hook = new DeleteHook();
		ShiftCastDelete shift = new ShiftCastDelete(hub, cast, hook);
		shift.setRelateId(invokerId);

		// 提交给WORK命令状态切换管理池，走admit/dispatch流程。
		boolean success = switchPool.admit(shift);
		if (!success) {
			throw new DistantTaskException("cannot be admit!");
		}
		// 钩子等待
		hook.await();

		// 判断存在故障
		Throwable fault = hook.getFault();
		if (fault != null) {
			throw new DistantTaskException(fault);
		}
		// 判断成功
		return hook.getProduct();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#decide(long, com.laxcus.echo.Cabin, com.laxcus.command.access.AssertDelete)
	 */
	@Override
	public AssumeDelete decide(long invokerId, Cabin hub, AssertDelete cmd)
			throws TaskException {
		// 判断是DATA站点
		if (!hub.getNode().isData()) {
			throw new DistantTaskException("must be data site! %s", hub);
		}

		// 检查有效性
		available(invokerId, cmd.getSpace(), CrossOperator.DELETE);

		// 命令钩子
		DeleteHook hook = new DeleteHook();
		ShiftAssertDelete shift = new ShiftAssertDelete(hub, cmd, hook);
		shift.setRelateId(invokerId);

		// 提交给WORK命令状态切换管理池，走admit/dispatch流程。
		boolean success = switchPool.admit(shift);
		if (!success) {
			throw new DistantTaskException("cannot be admit!");
		}
		// 钩子等待
		hook.await();

		// 判断存在故障
		Throwable fault = hook.getFault();
		if(fault != null) {
			throw new DistantTaskException(fault);
		}
		// 判断成功
		return hook.getProduct();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#update(long, com.laxcus.site.Node, com.laxcus.command.access.Update, java.util.List)
	 */
	@Override
	public AssumeUpdate update(long invokerId, Node hub, Update update,
			List<Long> stubs) throws TaskException {
		// 判断是DATA站点
		if (!hub.isData()) {
			throw new DistantTaskException("must be data site! %s", hub);
		}
		// 不允许有嵌套
		if (update.hasNested()) {
			throw new DistantTaskException("cannot be nested");
		}

		// 检查有效性
		available(invokerId, update.getSpace(), CrossOperator.UPDATE);

		// UPDATE投递命令
		CastUpdate cast = new CastUpdate(update, stubs);		
		// UPDATE命令钩子和转发命令
		UpdateHook hook = new UpdateHook();
		ShiftCastUpdate shift = new ShiftCastUpdate(hub, cast, hook);
		shift.setRelateId(invokerId);

		// 提交给WORK命令状态切换管理池，走admit/dispatch流程。
		boolean success = switchPool.admit(shift);
		if (!success) {
			throw new DistantTaskException("cannot be admit!");
		}
		// 钩子等待
		hook.await();

		// 判断存在故障
		Throwable fault = hook.getFault();
		if (fault != null) {
			throw new DistantTaskException(fault);
		}
		// 判断成功
		return hook.getProduct();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swift.DistantTrustor#decide(long, com.laxcus.echo.Cabin, com.laxcus.command.access.AssertUpdate)
	 */
	@Override
	public AssumeUpdate decide(long invokerId, Cabin hub, AssertUpdate cmd)
	throws TaskException {
		// 判断是DATA站点
		if (!hub.getNode().isData()) {
			throw new DistantTaskException("must be data site! %s", hub);
		}

		// 检查有效性
		available(invokerId, cmd.getSpace(), CrossOperator.UPDATE);
		// 转发命令
		UpdateHook hook = new UpdateHook();
		ShiftAssertUpdate shift = new ShiftAssertUpdate(hub, cmd, hook);
		shift.setRelateId(invokerId);

		// 提交给WORK命令状态切换管理池，走admit/dispatch流程。
		boolean success = switchPool.admit(shift);
		if (!success) {
			throw new DistantTaskException("cannot be admit!");
		}
		// 钩子等待
		hook.await();

		// 判断存在故障
		Throwable fault = hook.getFault();
		if (fault != null) {
			throw new DistantTaskException(fault);
		}
		// 判断成功
		return hook.getProduct();
	}

}