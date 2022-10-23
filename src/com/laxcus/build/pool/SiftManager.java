/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.pool;

import java.util.*;

import com.laxcus.access.*;
import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.index.*;
import com.laxcus.command.access.*;
import com.laxcus.distribute.establish.mid.*;
import com.laxcus.distribute.mid.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.sift.*;
import com.laxcus.task.mid.*;
import com.laxcus.util.*;

/**
 * SIFT工作管理池。<br><br>
 * 
 * 为SIFT阶段任务提供资源检索服务、基于JNI.DB的数据存取，以及中间数据存取操作。<br>
 * 
 * 与CONDUCT中间数据（FLUX）不同的是，SIFT中间数据只用于自身，不出现在网络上，并且只在SIFT任务组件运行期内有效。EstablishSiftInvoker释放后，SIFT中间将同步删除。
 * 
 * @author scott.liang
 * @version 1.1 6/12/2012
 * @since laxcus 1.0
 */
public final class SiftManager extends MidPool implements SiftTrustor {

	/** SIFT工作管理池，全局唯一。**/
	private static SiftManager selfHandle = new SiftManager();

	/** 资源池 **/
	private StaffOnBuildPool staffPool;

	/** 调用器池 **/
	private BuildInvokerPool invokerPool;

	/**
	 * 设置BUILD节点资源池
	 * @param e BUILD节点资源池实例
	 */
	public void setStaffPool(StaffOnBuildPool e) {
		Laxkit.nullabled(e);
		staffPool = e;
	}

	/**
	 * 设置BUILD节点异步调用器池
	 * @param e BUILD节点异步调用器池实例
	 */
	public void setInvokerPool(BuildInvokerPool e) {
		Laxkit.nullabled(e);
		invokerPool = e;
	}

	/**
	 * 返回SIFT工作管理池句柄
	 * @return SiftManager实例
	 */
	public static SiftManager getInstance() {
		// 安全检查
		VirtualPool.check("SiftManager.getInstance");
		// 返回句柄
		return SiftManager.selfHandle;
	}

	/** 任务编号 -> 中间数据存储器 **/
	private Map<Long, SiftStack> stacks = new TreeMap<Long, SiftStack>();

	/**
	 * 构造默认和私有的SIFT业务存取管理池
	 */
	private SiftManager() {
		super();
		// 20秒检查一次
		setSleepTime(20);
	}

	/**
	 * 根据调用器编号，查找用户签名
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
			throw new TaskSecurityException("security denied '<%s>/%s'", siger, space);
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
		available(invokerId);
		// 以复制方式，返回本地监听地址
		return getLocal(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#findSiftTable(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findSiftTable(long invokerId, Space space) throws TaskException {
		available(invokerId, space);
		// 查询资源配置
		return staffPool.findTable(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#hasSiftTable(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean hasSiftTable(long invokerId, Space space) throws TaskException {
		Table table = findSiftTable(invokerId, space);
		// 判断有效
		return (table != null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#hasDiskSpace(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean hasDiskSpace(long invokerId, Space space) throws TaskException {
		available(invokerId, space);
		// 空间存在
		return AccessTrustor.hasSpace(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#createDiskSpace(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean createDiskSpace(long invokerId, Space space) throws TaskException {
		available(invokerId, space);

		// 查询SIFT阶段数据表
		Table table = findSiftTable(invokerId, space);
		boolean success = (table != null);

		Logger.debug(this, "createDiskSpace", success, "find sift table %s", space);

		// 调用JNI接口，建立数据库
		if (success) {
			int ret = AccessTrustor.createSpace(table);
			success = (ret >= 0);

			Logger.debug(this, "createDiskSpace", success, "create '%s' is %d", space, ret);
		}

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#deleteDiskSpace(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean deleteDiskSpace(long invokerId, Space space) throws TaskException {
		available(invokerId, space);

		int ret = AccessTrustor.deleteSpace(space);
		boolean success = (ret >= 0);
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#select(long, com.laxcus.command.access.Select, long)
	 */
	@Override
	public byte[] select(long invokerId, Select select, long stub) throws TaskException {
		Space space = select.getSpace();
		available(invokerId, space);
		
		// 判断用户账号和表空间匹配且有效
		available(invokerId, space);
		// SELECT检索
		try {
			return AccessTrustor.select(select, stub);
		} catch (AccessException e) {
			throw new TaskAccessException(e.getCause());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#delete(long, com.laxcus.command.access.Delete, long)
	 */
	@Override
	public byte[] delete(long invokerId, Delete delete, long stub) throws TaskException {
		Space space = delete.getSpace();
		available(invokerId, space);

		// 判断用户账号和表空间匹配且有效
		available(invokerId, space);
		// DELETE删除
		try {
			return AccessTrustor.delete(delete, stub);
		} catch (AccessException e) {
			throw new TaskAccessException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#insert(long, com.laxcus.command.access.Insert)
	 */
	@Override
	public byte[] insert(long invokerId, Insert cmd) throws TaskException {
		Space space = cmd.getSpace();
		// 判断用户账号和表空间匹配且有效
		available(invokerId, space);
		// 写入
		return AccessTrustor.insert(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#rush(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public int rush(long invokerId, Space space) throws TaskException {
		available(invokerId, space);

		// 将剩余的CACHE状态数据，转为CHUNK状态保存并且输出
		return AccessTrustor.rush(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#marshal(long, com.laxcus.access.schema.Dock)
	 */
	@Override
	public long marshal(long invokerId, Dock dock) throws TaskException {
		available(invokerId, dock.getSpace());
		// 执行操作
		return AccessTrustor.marshal(dock);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#unmarshal(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public int unmarshal(long invokerId, Space space) throws TaskException {
		// 判断用户账号和表空间匹配且有效
		available(invokerId, space);
		// 取消
		return AccessTrustor.unmarshal(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#educe(long, com.laxcus.access.schema.Space, int)
	 */
	@Override
	public byte[] educe(long invokerId, Space space, int readlen) throws TaskException {
		// 判断用户账号和表空间匹配且有效
		available(invokerId, space);
		// 进入JNI.DB操作
		return AccessTrustor.educe(space, readlen);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#findChunkStubs(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public long[] findChunkStubs(long invokerId, Space space) throws TaskException {
		// 判断用户账号和表空间匹配且有效
		available(invokerId, space);
		// 返回全部数据块编号
		return AccessTrustor.getChunkStubs(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#findCacheStub(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public long findCacheStub(long invokerId, Space space) throws TaskException {
		// 判断用户账号和表空间匹配且有效
		available(invokerId, space);
		// 返回数据块编号。如果0是无效
		return AccessTrustor.getCacheStub(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#loadChunk(long, com.laxcus.access.schema.Space, long)
	 */
	@Override
	public int loadChunk(long invokerId, Space space, long stub) throws TaskException {
		// 判断用户账号和表空间匹配且有效
		available(invokerId, space);
		// 成功或者失败
		return AccessTrustor.loadChunk(space, stub);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#deleteChunk(long, com.laxcus.access.schema.Space, long)
	 */
	@Override
	public int deleteChunk(long invokerId, Space space, long stub) throws TaskException {
		// 判断用户账号和表空间匹配且有效
		available(invokerId, space);
		// 成功或者失败
		return AccessTrustor.deleteChunk(space, stub);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#doChunkFile(long, com.laxcus.access.schema.Space, long)
	 */
	@Override
	public String doChunkFile(long invokerId, Space space, long stub) throws TaskException {
		// 判断用户账号和表空间匹配且有效
		available(invokerId, space);
		// 生成数据块文件名
		return AccessTrustor.doChunkFile(space, stub);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#findIndex(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public StubArea findIndex(long invokerId, Space space) throws TaskException {
		// 判断用户账号和表空间匹配且有效
		available(invokerId, space);
		// 查询索引
		return AccessTrustor.findIndex(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#detect(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public SiftField detect(long invokerId, Space space) throws TaskException {
		// 判断用户账号和表空间匹配且有效
		available(invokerId, space);

		StubArea region = null;
		try {
			region = AccessTrustor.findIndex(space);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		if (region == null) {
			Logger.error(this, "detect", "'%s' is empty!", space);
			throw new SiftTaskException("%s is null!", space);
		}

		Logger.debug(this, "detect", "'%s' Element size:%d", space, region.size());

		// 建立SIFT域
		return createEstablishSiftField(region);
	}

	/**
	 * 建立SIFT域
	 * @param region
	 * @return
	 * @throws SiftTaskException
	 */
	private SiftField createEstablishSiftField(StubArea region) throws SiftTaskException {
		Space space = region.getSpace();

		// 建立域
		Node local = getLocal(true);
		EstablishFlag flag = new EstablishFlag(space, local);
		SiftField field = new SiftField(flag); 

		for(StubItem item : region.list()) {
			// 判断通过。过滤CACHE状态数据块
			boolean allow = (item.isPrime() && item.isChunk());
			if(!allow) {
				continue;
			}
			// 只保存已经封闭的数据块
			field.addStubItem(item);			
		}

		Logger.debug(this, "createEstablishSiftField", "'%s' element size:%d", space, field.getStubCount());

		return field;
	}

	/**
	 * 根据任务编号，返回SIFT堆栈
	 * @param taskId 任务编号
	 * @return SiftStack实例
	 */
	private SiftStack find(long taskId) {
		super.lockMulti();
		try {
			return stacks.get(taskId);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 根据用户签名和任务编号，查找SIFT堆栈
	 * @param siger 用户签名
	 * @param taskId 任务编号
	 * @return 返回SiftStack实例
	 * @throws TaskSecurityException
	 */
	private SiftStack find(Siger siger, long taskId) throws TaskSecurityException {
		SiftStack stack = find(taskId);
		if (stack == null) {
			return null;
		}
		if (Laxkit.compareTo(stack.getIssuer(), siger) != 0) {
			throw new TaskSecurityException("security denied '%s'", siger);
		}
		return stack;
	}

	/**
	 根据任务编号，删除对应的SIFT堆栈，以及保存的中间数据
	 * @param taskId 任务编号
	 * @return 成功返回真，否则假
	 */
	private boolean delete(long taskId)  {
		SiftStack stack = null;
		super.lockSingle();
		try {
			stack = stacks.remove(taskId);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 判断有效
		boolean success = (stack != null);
		if (success) {
			// 删除内存或者磁盘上的数据
			success = stack.delete();
		}

		// 回收内存
		super.lockSingle();
		try {
			if (success) {
				MemoryCounter scaler = stack.getCacheCounter();
				if (scaler != null) {
					counter.revert(scaler.getMaxSize());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 建立一个任务编号
	 * @return 长整型值
	 */
	private long createTaskId() {
		do {
			long taskId = super.nextTaskId();
			SiftStack stack = find(taskId);
			if (stack == null) {
				return taskId;
			}
		} while (true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#getMemberMemory()
	 */
	@Override
	public long getMemberMemory() {
		return getMemberMemorySize();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#createStack(long, boolean, long)
	 */
	@Override
	public long createStack(long invokerId, boolean memory, long capacity) throws TaskException {
		// 查找用户签名
		Siger siger = findIssuer(invokerId);
		// 判断有效
		available(siger);

		// 如果指定内存存取模式，必须在系统规定范围内
		if (memory) {
			memory = (capacity > 0 && capacity <= getMemberMemory());
		}

		// 从队列中建立一个任务编号
		long taskId = createTaskId();
		// 建立SIFT堆栈
		SiftStack stack = new SiftStack(siger, invokerId, taskId, this);
		super.lockSingle();
		try {
			// 保存实例
			boolean success = (stacks.put(stack.getTaskId(), stack) == null);
			if (!success) {
				stacks.remove(stack.getTaskId());
				return -1L;
			}
			// 成功，且要求申请内存时...
			if (memory) {
				// 分配最大内存
				memory = counter.alloc(capacity);
				if (memory) {
					MemoryCounter scaler = new MemoryCounter(capacity);
					stack.setCacheCounter(scaler);
				}
			}
			// 建立内存或者硬盘模式的数据
			return taskId;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return -1L;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#createStack(long)
	 */
	@Override
	public long createStack(long invokerId) throws TaskException {
		return createStack(invokerId, false, -1L);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#findWriter(long, long)
	 */
	@Override
	public SiftWriter findWriter(long invokerId, long taskId) throws TaskException {
		// 查找用户签名
		Siger siger = findIssuer(invokerId);
		// 检查签名有效
		available(siger);
		// 返回SIFT写入器
		return find(siger, taskId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#findReader(long, long)
	 */
	@Override
	public SiftReader findReader(long invokerId, long taskId) throws TaskException {
		// 查找用户签名
		Siger siger = findIssuer(invokerId);
		// 检查签名有效
		available(siger);
		// 返回SIFT读取器
		return find(siger, taskId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTrustor#deleteStack(long, long)
	 */
	@Override
	public boolean deleteStack(long invokerId, long taskId) throws TaskException {
		// 查找用户签名
		Siger siger = findIssuer(invokerId);
		// 检查签名有效
		available(siger);

		// 查找SIFT堆栈
		SiftStack stack = find(siger, taskId);
		boolean success = (stack != null);
		// 删除SIFT堆栈
		if (success) {
			success = delete(taskId);
		}
		return success;
	}

	/**
	 * 检查无效的SIFT堆栈，并且删除它
	 */
	private void check() {
		int size = stacks.size();
		if(size == 0) {
			return;
		}

		// 收集SIFT堆栈标识
		ArrayList<SiftStackFlag> array = new ArrayList<SiftStackFlag>(size);
		super.lockMulti();
		try {
			Iterator<Map.Entry<Long, SiftStack>> iterator = stacks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Long, SiftStack> entry = iterator.next();
				array.add(entry.getValue().createFlag());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		// 删除无效的SIFT堆栈
		for (SiftStackFlag flag : array) {
			boolean success = invokerPool.hasInvoker(flag.getInvokerId());
			// 如果不存在，删除SIFT堆栈
			if (!success) {
				boolean b = delete(flag.getTaskId());
				Logger.debug(this, "check", b, "delete %s", flag);
			}
		}
	}

	/**
	 * 清除全部参数
	 */
	private void clear() {
		int size = stacks.size();
		if (size == 0) {
			return;
		}
		ArrayList<Long> array = new ArrayList<Long>(size);
		super.lockMulti();
		try {
			array.addAll(stacks.keySet());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		// 删除全部中间数据
		for (long taskId : array) {
			delete(taskId);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 必须由用户指定目录
		if (super.getRoot() == null) {
			Logger.error(this, "init", "cannot be set root directory");
			return false;
		}

		Logger.info(this, "init", "middle cache size is %d M", counter.getMaxSize()/0x100000L);

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");
		while (!super.isInterrupted()) {
			// 定时检查
			check();
			// 延时...
			sleep();
		}
		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		clear();
	}

}