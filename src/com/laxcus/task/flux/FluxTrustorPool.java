/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.flux;

import java.util.*;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.mid.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.task.*;
import com.laxcus.task.mid.*;
import com.laxcus.util.*;

/**
 * CONDUCT中间数据存取管理池 <br><br>
 * 
 * 分配给DATA.FROM/WORK.TO分布任务组件使用，FROM/TO组件通过“FluxTrustor”来实现存取中间数据。
 * 
 * @author scott.liang
 * @version 1.3 8/12/2014
 * @since laxcus 1.0
 */
public final class FluxTrustorPool extends MidPool implements FluxTrustor {
	
	/** 调用器管理池 **/
	private InvokerPool invokerPool;

	/** CONDUCT中间数据存取管理池，全局唯一。**/
	private static FluxTrustorPool selfHandle = new FluxTrustorPool();

	/** 任务编号 -> 中间数据存储器 **/
	private Map<Long, FluxStack> stacks = new TreeMap<Long, FluxStack>();

	/** 数字签名人代理。对应DATA.FROM/WORK.TO/CONTACT.DISTANT 3个阶段，判断使用存取中间数据服务的用户是有效的。**/
	private SigerTrustor trustor;

	/**
	 * 构造默认和私有的CONDUCT中间数据存取管理池
	 */
	private FluxTrustorPool() {
		super();
		// 20秒检查一次
		setSleepTime(20);
	}

	/**
	 * 返回CONDUCT中间数据存取管理池句柄
	 * @return FluxTrustorPool静态句柄
	 */
	public static FluxTrustorPool getInstance() {
		// 安全检查
		VirtualPool.check("FluxTrustorPool.getInstance");
		// 返回句柄实例
		return FluxTrustorPool.selfHandle;
	}

	/**
	 * 建立一个任务编号
	 * @return 长整型的任务编号
	 */
	private long createTaskId() {
		do {
			long taskId = super.nextTaskId();
			FluxStack stack = find(taskId);
			if (stack == null) {
				return taskId;
			}
		} while (true);
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
		
		// 获得调用器管理池
		invokerPool = getLauncher().getInvokerPool();

		Logger.info(this, "init", "middle cache size is %d M", counter.getMaxSize()/0x100000L);

		// 拿到调用器管理池
		return (invokerPool != null);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");
		while (!isInterrupted()) {
			// 定时检查超时
			check(); 
			// 延时
			sleep();
		}
		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 清除中间数据（内存或者硬盘上的）
		clear();
	}

	/**
	 * 设置数字签名人代理 <br>
	 * 对应DATA.FROM/WORK.TO两个阶段，用来判断使用存取中间数据服务的用户是存在且有效的。<br>
	 * 
	 * @param e SigerTrustor对象实例
	 */
	public void setSigerTrustor(SigerTrustor e) {
		// 不允许空指针
		Laxkit.nullabled(e);

		trustor = e;
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
	 * 根据调用器编号，判断签名有效
	 * @param invokerId 调用器编号
	 * @throws TaskSecurityException
	 */
	private void available(long invokerId) throws TaskException {
		trustor.allow(invokerId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.mid.FluxTrustor#getMemberMemory(long)
	 */
	@Override
	public long getMemberMemory(long invokerId) throws TaskException {
		// 判断来源有效
		available(invokerId);

		// 找到中间缓存尺寸
		long middleBuffer = trustor.getMiddleBufferSize(invokerId);
		// 如果没有定义，取默认尺寸
		if (middleBuffer < 1) {
			// 取规定的成员内存容量
			middleBuffer = getMemberMemorySize();
		}

		//	System.out.printf("FluxTrustorPool.getMemberMemory, middle buffer %d\n", middleBuffer);

		return middleBuffer;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.mid.FluxTrustor#createStack(long, boolean, long)
	 */
	@Override
	public long createStack(long invokerId, boolean memory, long capacity) throws TaskException {
		Siger siger = findIssuer(invokerId);
		// 判断用户签名有效
		available(invokerId);

		// 如果指定内存存取模式，必须在系统规定范围内
		if (memory) {
			memory = (capacity > 0 && capacity <= getMemberMemory(invokerId));
		}

		// 生成一个新的任务编号
		long taskId = createTaskId();
		// 建立缓存
		FluxStack stack = new FluxStack(siger, taskId, this);
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
					MemoryCounter counter = new MemoryCounter(capacity);
					stack.setCacheCounter(counter);
				}
			}
			// 返回任务编号
			return taskId;
		} catch (Throwable e) {
			Logger.fatal(siger, e);
			throw new TaskException(e);
		} finally {
			super.unlockSingle();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.mid.FluxTrustor#createStack(long)
	 */
	@Override
	public long createStack(long invokerId) throws TaskException {
		return createStack(invokerId, false, -1L);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.mid.FluxTrustor#findWriter(long, long)
	 */
	@Override
	public FluxWriter findWriter(long invokerId, long taskId) throws TaskException {
		// 判断签名有效
		Siger siger = findIssuer(invokerId);
		available(invokerId);
		// 返回FLUX写入器
		return find(siger, taskId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.mid.FluxTrustor#findReader(long, long)
	 */
	@Override
	public FluxReader findReader(long invokerId, long taskId) throws TaskException {
		// 判断签名有效
		Siger siger = findIssuer(invokerId);
		available(invokerId);
		// 返回FLUX读取器
		return find(siger, taskId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.mid.FluxTrustor#deleteStack(long, long)
	 */
	@Override
	public boolean deleteStack(long invokerId, long taskId) throws TaskException {
		// 判断签名有效
		Siger siger = findIssuer(invokerId);
		available(invokerId);

		// 查找FLUX堆栈
		FluxStack stack = find(siger, taskId);
		boolean success = (stack != null);
		// 删除SIFT堆栈
		if (success) {
			success = delete(taskId);
		}

		Logger.debug(siger, this, "deleteStack", success, "release %s#%d", siger, taskId);

		return success;
	}

	/**
	 * 根据任务编号，返回对应的存取堆栈
	 * @param taskId 任务编号
	 * @return FluxStack实例
	 */
	private FluxStack find(long taskId) {
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
	 * 根据用户签名和任务编号，返回对应的存取堆栈
	 * @param issuer 用户签名
	 * @param taskId 任务编号
	 * @return FluxStack实例
	 * @throws TaskSecurityException
	 */
	private FluxStack find(Siger issuer, long taskId) throws TaskSecurityException {
		FluxStack stack = find(taskId);
		if (stack == null) {
			return null;
		}
		if (Laxkit.compareTo(stack.getIssuer(), issuer) != 0) {
			throw new TaskSecurityException("safe denied '%s'", issuer);
		}
		return stack;
	}

	/**
	 * 根据任务编号删除对应的堆栈，和回收内存
	 * @param taskId 任务编号
	 * @return 成功返回真，否则假
	 */
	private boolean delete(long taskId) {
		FluxStack stack = null;
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
	 * 检查超时记录，删除它
	 */
	private void check() {
		int size = stacks.size();
		if (size == 0) {
			return;
		}

		long timeout = getTimeout();
		ArrayList<Long> array = new ArrayList<Long>(size);
		super.lockMulti();
		try {
			Iterator<Map.Entry<Long, FluxStack>> iterator = stacks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Long, FluxStack> entry = iterator.next();
				if (entry.getValue().isTimeouted(timeout)) {
					array.add(entry.getKey());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		// 删除超时任务
		for (Long taskId : array) {
			delete(taskId);
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

	/**
	 * 设置一个模值的数据进入完成状态
	 * @param taskId 任务编号
	 * @param mod 模值
	 * @return 成功返回真，否则假。
	 */
	public boolean complete(long taskId, long mod) {
		FluxStack stack = find(taskId);
		boolean success = (stack != null);
		if (success) {
			success = stack.setCompleted(mod, true);
		}
		if (success) {
			boolean finished = stack.isComplted();
			if (finished) {
				delete(taskId);
			}
		}

		Logger.debug(this, "complete", success, "%d/%d is", taskId, mod);
		return success;
	}
}