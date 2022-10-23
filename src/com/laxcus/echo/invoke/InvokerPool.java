/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 异步命令调用器管理池。<br><br>
 * 
 * 异步调用器管理池的职责是向外发起异步命令，实现“EchoAcceptor”接口。处理顺序是先保存“异步调用器”，然后启动“异步调用器”（EchoInvoker.launch）；
 * 在线程中等待异步处理结果，当收到异步数据到时，调用保存的异步调用器，去完成数据处理（EchoInvoker.ending）。
 * 
 * 删除超时异步调用器，达到这两个条件：
 * 1. 达到系统规定的超时时间，判断是invokerTimeout > 0，这个参数在“conf/local.xml”定义。
 * 2. 达到用户规定的超时时间，参数是Command.getTimeout > 0。
 * 存在一个问题，如果两个判断条件都不成立，那么异步调用器将“死”在管理池中。所以invokerTimeout必须大于0。
 * 
 * @author scott.liang
 * @version 1.3 01/08/2015
 * @since laxcus 1.0
 */
public abstract class InvokerPool extends EchoPool implements EchoAcceptor {

	/** 命令管理池句柄 **/
	private static CommandPool brother;

	/**
	 * 设置命令管理池句柄。由CommandPool在建立时设置
	 * @param e 命令管理池句柄
	 */
	protected static void setCommandPool(CommandPool e) {
		InvokerPool.brother = e;
	}

	/** 异步调用器队列 **/
	private Map<java.lang.Long, EchoInvoker> invokers = new TreeMap<java.lang.Long, EchoInvoker>();

	/** 普通回显标识集合 **/
	private ArrayList<Trigger> slacks = new ArrayList<Trigger>(512);

	/** 快速处理回显标识集合 **/
	private ArrayList<Trigger> quicks = new ArrayList<Trigger>(512);

	/** 闪速处理回显标识集合 **/
	private ArrayList<Trigger> fasts = new ArrayList<Trigger>(512);

	/**
	 * 构造默认的调用器管理池
	 */
	protected InvokerPool() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.remote.client.EchoAcceptor#look(com.laxcus.echo.EchoFlag)
	 */
	@Override
	public boolean look(EchoFlag flag) {
		// 忽略空指针
		if (flag == null) {
			return false;
		}

		// 查找异步调用器
		EchoInvoker invoker = findInvoker(flag.getInvokerId());
		// 如果没找到，是错误!
		if (invoker == null) {
			Logger.error(this, "look", "cannot be find invoker: %d", flag.getInvokerId());
			return false;
		}

		// 如果在运行状态，延时处理；否则是立即处理
		boolean running = invoker.isRunning(); 
		long touchTime = 0L;
		if (running) {
			touchTime = System.currentTimeMillis() + EchoTransfer.getCrossInterval();
		}

		// 生成触发标识
		Trigger trigger = new Trigger(flag, touchTime);

		// 命令等级
		byte priority = invoker.getPriority();
		// 默认值是假
		boolean success = false;
		// 锁定，判断级别，保存一个触发标识
		super.lockSingle();
		try {
			if (CommandPriority.isFast(priority)) {
				success = fasts.add(trigger);
			} else if (CommandPriority.isQuick(priority)) {
				success = quicks.add(trigger);
			} else {
				success = slacks.add(trigger);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 没有延时，唤醒它！
		if (success && touchTime == 0) {
			wakeup();
		}

		return success;
	}

	/**
	 * 判断有普通回显标识
	 * @return 返回真或者假
	 */
	private boolean hasSlack() {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			success = (slacks.size() > 0);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 弹出达到触发时间的普通回显标识。首先选择排列在最前面的回显标识。
	 * @return 返回回显标识或者空
	 */
	private EchoFlag pollSlack() {
		super.lockSingle();
		try {
			int size = slacks.size();
			for (int index = 0; index < size; index++) {
				Trigger trigger = slacks.get(index);
				// 删除和忽略可能存在的空指针，返回空指针
				if (trigger == null) {
					slacks.remove(index);
					return null;
				}
				// 可以触发，删除内存中的对象，返回回显标识
				if (trigger.isTouchable()) {
					slacks.remove(index);
					return trigger.getEchoFlag();
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 处理普通的异步调用器
	 * @return 成功返回真，否则假
	 */
	private boolean doSlack() {
		// 检查有回显任务
		boolean success = hasSlack();
		if (!success) {
			return false;
		}
		// 检查负载（最大线程和机器载荷）
		success = checkPower();
		// 以上都通过时，分派任务。
		if (success) {
			EchoFlag flag = pollSlack();
			success = (flag != null);
			// 许可！分派回显标识
			if (success) {
				success = dispatch(flag);
			}
		}
		// 返回处理结果
		return success;
	}

	/**
	 * 判断有快速处理标识
	 * @return 返回真或者假
	 */
	private boolean hasQuick() {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			success = (quicks.size() > 0);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 弹出达到触发时间的快速回显标识。首先选择排列在最前面的回显标识。
	 * @return 返回回显标识或者空
	 */
	private EchoFlag pollQuick() {
		super.lockSingle();
		try {
			int size = quicks.size();
			for (int index = 0; index < size; index++) {
				Trigger trigger = quicks.get(index);
				// 删除和忽略可能存在的空指针，返回空指针
				if (trigger == null) {
					quicks.remove(index);
					return null;
				}
				// 可以触发，删除内存中的对象，返回回显标识
				if (trigger.isTouchable()) {
					quicks.remove(index);
					return trigger.getEchoFlag();
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 处理快速异步调用器
	 * @return 成功返回真，否则假
	 */
	private boolean doQuick() {
		// 检查有快速处理任务
		boolean success = hasQuick();
		if (!success) {
			return false;
		}
		// 检查负载（最大线程和机器载荷）
		success = checkPower();
		// 以上都通过时，分派任务。
		if (success) {
			EchoFlag flag = pollQuick();
			success = (flag != null);
			// 许可！分派回显标识
			if (success) {
				success = dispatch(flag);
			}
		}
		// 返回处理结果
		return success;
	}

	/**
	 * 判断有极速处理标识
	 * @return 返回真或者假
	 */
	private boolean hasFast() {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			success = (fasts.size() > 0);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 弹出达到触发时间的极速回显标识。首先选择排列在最前面的回显标识。
	 * @return 返回回显标识或者空
	 */
	private EchoFlag pollFast() {
		super.lockSingle();
		try {
			int size = fasts.size();
			for (int index = 0; index < size; index++) {
				Trigger trigger = fasts.get(index);
				// 删除和忽略可能存在的空指针，返回空指针
				if (trigger == null) {
					fasts.remove(index);
					return null;
				}
				// 可以触发，删除内存中的对象，返回回显标识
				if (trigger.isTouchable()) {
					fasts.remove(index);
					return trigger.getEchoFlag();
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 处理极速异步调用器。<br>
	 * 与“快速处理”的区别是：不检查机器资源，直接分配资源！只限于极少的处理命令。
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean doFast() {
		// 检查有极速处理任务
		boolean success = hasFast();
		// 以上都通过时，分派任务。
		if (success) {
			EchoFlag flag = pollFast();
			success = (flag != null);
			// 许可！分派回显标识
			if (success) {
				success = dispatch(flag);
			}
		}
		// 返回处理结果
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoPool#init()
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

		// 超时计时器，以外部规定的时间为准，默认是1分钟
		EchoTimer timer = new EchoTimer(getDisableCheckInterval());

		// 处理快速异步调用器
		while (!isInterrupted()) {
			// 判断处于登录状态
			if (isLogined()) {
				// 极速处理
				boolean success = doFast();
				// 快速处理
				if (!success) {
					success = doQuick();
				}
				// 处理普通异步调用器
				if (!success) {
					success = doSlack();
				}
				// 如果成功，继续处理下一个
				if (success) {
					continue;
				}
			}

			// 延时，时间根据任务数动态调整
			delay(getSilentInterval());

			// 失效检测器超时，检测失效过期的调用器
			// 包括对系统定义的超时和用户定义的超时
			if (timer.isTimeout()) {
				// 刷新，时间移到下一阶段
				timer.refresh();
				// 释放超时的异步调用器。两个条件：1. 达到系统规定的超时时间；2. 达到用户规定的超时时间
				doReleaseInvoker();
			}
		}
		Logger.info(this, "process", "exit...");
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoPool#finish()
	 */
	@Override
	public void finish() {
		// 全部销毁调用器
		destroyInvokers();

		// 全部释放
		slacks.clear();
		quicks.clear();
		fasts.clear();
		invokers.clear();
	}

	/**
	 * 产生一个调用器编号。调用器编号一定当前集合中没有，并且在有效范围（0-Long.MAX_VALUE）内的长整型。
	 * @return 长整型数值
	 */
	public long createInvokerId() {
		SiteLauncher launcher = VirtualPool.getLauncher();
		do {
			long invokerId = launcher.nextIterateIndex();
			// 根据调用器编号，判断异步调用器存在；不存在即有效
			if (!hasInvoker(invokerId)) {
				return invokerId;
			}
		} while (true);
	}

	/**
	 * 指定调用器编号和索引编号，生成一个回显地址。
	 * 
	 * @param invokerId 调用器编号
	 * @param index 调用异步单元索引编号 (必须大于等于0)
	 * @return Cabin实例
	 * @throws IllegalValueException - 回显缓存索引编号小于0
	 */
	public Cabin createCabin(long invokerId, int index) {
		SiteLauncher launcher = VirtualPool.getLauncher();
		Node node = launcher.getListener();
		return new Cabin(node, invokerId, index);
	}

	/**
	 * 指定一个调用器编号，生成一个回显地址 <br><br>
	 * 
	 * 回显地址中的索引下标编号默认是0
	 * 
	 * @param invokerId 调用器编号
	 * @return Cabin实例
	 */
	public Cabin createDefaultCabin(long invokerId) {
		return createCabin(invokerId, 0);
	}

	/**
	 * 生成一个基于当前运行站点的默认回显地址 <br><br>
	 * 
	 * 回显地址中的调用器编号由SiteLauncher产生，索引下标编号默认是0。
	 * @return Cabin实例
	 */
	public Cabin createDefaultCabin() {
		long invokerId = createInvokerId();
		return createDefaultCabin(invokerId);
	}

	/**
	 * 保存一个异步调用器，用它的已经分配的调用器编号做为“键”值保存。
	 * @param invoker 异步调用器
	 * @return 保存成功返回“真”，否则“假”。
	 */
	public boolean addInvoker(EchoInvoker invoker) {
		// 判断空指针，忽略
		if (invoker == null) {
			return false;
		}
		// 本地回显地址
		long invokerId = invoker.getInvokerId();
		if (InvokerIdentity.isInvalid(invokerId)) {
			throw new IllegalValueException("illegal invoker identity %d", invokerId);
		}
		// 保存
		boolean success = false;
		super.lockSingle();
		try {
			// 判断不存在，且保证保存成功
			success = (invokers.get(invokerId) == null);
			if (success) {
				success = (invokers.put(invokerId, invoker) == null);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 判断一个调用器处于运行状态
	 * @param invokerId 调用器编号
	 * @return 返回真或者假
	 */
	public boolean isRunning(long invokerId) {
		// 默认是假
		boolean running = false;
		// 锁定
		super.lockMulti();
		try {
			EchoInvoker e = invokers.get(invokerId);
			if (e != null) {
				running = e.isRunning();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return running;
	}

	/**
	 * 根据调用器编号，查找一个异步调用器
	 * @param invokerId 调用器编号
	 * @return 返回异步调用器句柄。没有找到返回空指针。
	 */
	public EchoInvoker findInvoker(long invokerId) {
		// 锁定！
		super.lockSingle();
		try {
			return invokers.get(invokerId);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 找到命令关联的调用器编号
	 * @param clazz 类定义
	 * @return 返回调用器编号
	 */
	public List<Long> findInvokerKeys(Class<?> clazz) {
		ArrayList<Long> array = new ArrayList<Long>();
		// 锁定!
		super.lockSingle();
		try {
			Iterator<Map.Entry<Long, EchoInvoker>> iterator = invokers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Long, EchoInvoker> entry = iterator.next();
				EchoInvoker invoker = entry.getValue();
				Command cmd = invoker.getCommand();
				if (Laxkit.isClassFrom(cmd, clazz)) {
					array.add(entry.getKey());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return array;
	}

	/**
	 * 根据调用器编号，判断异步调用器存在
	 * @param invokerId 调用器编号
	 * @return 返回真或者假
	 */
	public boolean hasInvoker(long invokerId) {
		EchoInvoker invoker = findInvoker(invokerId);
		return invoker != null;
	}

	/**
	 * 删除极速处理回显标识
	 * @param invokerId 调用器编号
	 */
	private int removeFast(long invokerId) {
		ArrayList<Trigger> array = new ArrayList<Trigger>();
		// 检查等待队列中有相同调用器编号的触发标识，把它们保留。
		for (Trigger trigger : fasts) {
			if (trigger.getInvokerId() == invokerId) {
				array.add(trigger);
			}
		}
		// 删除这些触发标识
		int size = array.size();
		if (size > 0) {
			fasts.removeAll(array);
		}
		return size;
	}

	/**
	 * 删除快速处理回显标识
	 * @param invokerId 调用器编号
	 */
	private int removeQuick(long invokerId) {
		ArrayList<Trigger> array = new ArrayList<Trigger>();
		// 检查等待队列中有相同调用器编号的触发标识，把它们保留。
		for (Trigger trigger : quicks) {
			if (trigger.getInvokerId() == invokerId) {
				array.add(trigger);
			}
		}
		// 删除这些触发标识
		int size = array.size();
		if (size > 0) {
			quicks.removeAll(array);
		}
		return size;
	}

	/**
	 * 删除普通回显标识
	 * @param invokerId 调用器编号
	 */
	private int removeSlack(long invokerId) {
		ArrayList<Trigger> array = new ArrayList<Trigger>();
		// 检查等待队列中有相同调用器编号的触发标识，把它们保留。
		for (Trigger trigger : slacks) {
			if (trigger.getInvokerId() == invokerId) {
				array.add(trigger);
			}
		}
		// 删除这些触发标识
		int size = array.size();
		if (size > 0) {
			slacks.removeAll(array);
		}
		return size;
	}

	/**
	 * 根据调用器编号，释放超时的异步调用器 <br>
	 * <br>
	 * 
	 * 这是一种故障的处理，超时的调用器必须存在且处于非线程运行状态。
	 * 如果来自FRONT/WATCH的前端有UI界面的节点，EndingTrustor线程启动调用异步调用器。
	 * 如果是其它后台节点，直接删除，然后通知调用端节点。
	 * 涉及本地删除的，在InvokerPool锁定状态下处理，其它在锁定外处理。目的：防止相互调用造成死锁！这个事情发生过！<br>
	 * 
	 * @param ReleaseFlag 释放标识
	 * @return 删除成功返回真，否则假
	 */
	private boolean releaseTimeoutInvoker(ReleaseFlag flag) {
		// 调用器
		long invokerId = flag.getInvokerId();

		EndingTrustor endingTrustor = null;
		EchoInvoker releaseInvoker = null;

		// 锁定它，处理在外部，避免发生相互死锁
		super.lockSingle();
		try {
			EchoInvoker invoker = invokers.get(invokerId);
			// 调用器不存在，或者调用器处于运行状态，不处理！
			if (invoker == null) {
				Logger.error(this, "releaseTimeoutInvoker", "cannot be find invoker, %d", invokerId);
				return false;
			} else if (invoker.isRunning()) {
				Logger.warning(this, "releaseTimeoutInvoker", "invoker %d is running!", invokerId);
				return false;
			}

			Logger.error(this, "releaseTimeoutInvoker","release %d # %s # timeout %d",
					invokerId, invoker.getClass().getName(), invoker.getCommandTimeout());

			// 以下情况，调用器一定存在且处于没有EndingTrustor线程调用的停止状态
			// 如果是FRONT/WATCH节点，设置超时反馈。然后调用它！
			if (invoker.isFront() || invoker.isWatch()) {
				// 生成“EndingTrustor”线程
				endingTrustor = new EndingTrustor(this, invoker);
			} else {
				// 取出优先级
				byte priority = invoker.getPriority();
				// 根据调用器编号删除
				invokers.remove(invokerId);
				// 删除在队列中相同编号的回显标识（它们属于一个异步调用器）
				if (CommandPriority.isFast(priority)) {
					removeFast(invokerId);
				} else if (CommandPriority.isQuick(priority)) {
					removeQuick(invokerId);
				} else {
					removeSlack(invokerId);
				}
				releaseInvoker = invoker;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 在没有锁定情况下处理
		boolean success = false;
		// 有代理时...
		if (endingTrustor != null) {
			EchoInvoker invoker = endingTrustor.getInvoker();
			// 调用器的全部EchoBuffer设置超时通知
			invoker.doTimeoutFault();
			// 启动它
			endingTrustor.start();
			Logger.warning(this, "releaseTimeoutInvoker", "call EndingTrustor, delete %s", invoker.getClass().getName());
			success = true;
		}
		// 有调用器时
		else if (releaseInvoker != null) {
			// 销毁异步数据
			releaseInvoker.destroy();
			// 向命令来源发送超时通知
			Cabin source = flag.getCabin();
			if (source != null) {
				replyTimeout(source);
			}
			success = true;
		}

		Logger.note(this, "releaseTimeoutInvoker", success, "release %s", flag);

		return success;
	}

	/**
	 * 根据调用器编号，释放异步任务调用器。这些调用器属于FRONT节点。 <br><br>
	 * 
	 * <b>特别说明：不能释放运行状态的异步调用器。所谓调用器的运行状态，即它被包装在LaunchTrustor/EndingTrustor线程中运行。</b>
	 * 
	 * @param invokerId 调用器编号
	 * @return 释放成功返回真，否则假
	 */
	public boolean releaseMissionInvoker(long invokerId) {
		boolean success = false;

		// 锁定它
		super.lockSingle();
		try {
			EchoInvoker invoker = invokers.get(invokerId);
			success = (invoker != null && invoker.isStopped());
			if (success) {
				// 取出优先级
				byte priority = invoker.getPriority();
				// 根据调用器编号删除
				invokers.remove(invokerId);
				// 删除在队列中相同编号的回显标识（它们属于一个异步调用器）
				if (CommandPriority.isFast(priority)) {
					removeFast(invokerId);
				} else if (CommandPriority.isQuick(priority)) {
					removeQuick(invokerId);
				} else {
					removeSlack(invokerId);
				}
				// 销毁异步数据
				invoker.destroy();
			}
		} catch(Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "releaseMissionInvoker", success, "release %d", invokerId);

		return success;
	}

	/**
	 * 根据调用器编号，删除一个异步调用器 <br>
	 * 
	 * removeInvoker和releaseMissionInvoker的区别是：releaseMissionInvoker方法只能删除非运行状态的异步调用器，而removeInvoker不考虑这个选项。
	 * 
	 * @param invokerId 调用器编号
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean removeInvoker(long invokerId) {
		boolean success = false;
		// 在队列中，属于相同调用器编号，且剩余未使用的回显标识
		int lingers = 0;

		super.lockSingle();
		try {
			EchoInvoker invoker = invokers.remove(invokerId);
			success = (invoker != null);
			// 删除在队列中剩余的回显标识，和销毁异步调用器的资源
			if (success) {
				byte priority = invoker.getPriority();
				// 删除在队列中相同编号的回显标识（它们属于一个异步调用器）
				if (CommandPriority.isFast(priority)) {
					lingers = removeFast(invokerId);
				} else if (CommandPriority.isQuick(priority)) {
					lingers = removeQuick(invokerId);
				} else {
					lingers = removeSlack(invokerId);
				}
				// 销毁异步调用器资源
				invoker.destroy();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "removeInvoker", success, "remove %d, linger flags:%d, left flags:%d", 
				invokerId, lingers, slacks.size() + quicks.size() + fasts.size());

		// 检查CommandPool命令队列，如果资源足够，马上执行
		if (success) {
			InvokerPool.brother.taste();
		}

		return success;
	}

	/**
	 * 根据异步调用器，从异步队列中删除它。
	 * @param invoker 异步调用器
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean removeInvoker(EchoInvoker invoker) {
		long invokerId = invoker.getInvokerId();
		return removeInvoker(invokerId);
	}

	/**
	 * 启动异步调用器 <br>
	 * 
	 * 启动成功的异步调用器，将交给LaunchTrustor托管处理。
	 * 
	 * @param invoker 异步调用器
	 * @return 成功返回“真”，失败“假”
	 */
	protected boolean defaultLaunch(EchoInvoker invoker) {
		// 生成本地回显地址
		Cabin cabin = createDefaultCabin();
		// 给异步调用器设置本地回显地址
		invoker.setListener(cabin);
		// 给异步调用器设置异步数据受理器
		invoker.setEchoAcceptor(this);
		// 重置启动时间
		invoker.resetLaunchTime();

		// 保存异步调用器
		boolean success = addInvoker(invoker);
		// 交给一个线程去处理异步接收器
		if (success) {
			LaunchTrustor trustor = new LaunchTrustor(this, invoker);
			trustor.start();
		}

		return success;
	}

	/**
	 * 根据回显标识，找到异步调用器，委托给“EndingTrustor”处理。EndingTrustor执行“ending”操作。<br>
	 * 
	 * 说明：“ending”是迭代的，一个异步调用器可以被EndingTrustor多次调用。
	 * 
	 * @param flag 回显标识
	 * @return 处理成功返回“真”，否则“假”。
	 */
	protected boolean defaultDispatch(EchoFlag flag) {
		// 忽略空指针
		if (flag == null) {
			return false;
		}
		// 调用器编号
		long invokerId = flag.getInvokerId();

		EndingTrustor trustor = null;
		// 锁定
		super.lockSingle();
		try {
			// 获得异步调用器
			EchoInvoker invoker = invokers.get(invokerId);

			/** 没有找到异步调用器是严重错误！ **/
			if (invoker == null) {
				Logger.error(this, "defaultDispatch", "cannot be git %d", invokerId);
				return false;
			}

			/**
			 * 发生交叠现象，根据系统交叠间隔时间，延时再处理。
			 * 此现象发生的原因是：由于计算机或者网络太快，当前处理的工作量太多且没有退出，下次处理请求已经到达，就发生这种现象。
			 * 解决办法：在触发标识中设置一个延时时间，重新放回队列，待本次完成后再处理。
			 **/
			if (invoker.isRunning()) {
				Logger.warning(this, "defaultDispatch", "%d is running", invokerId);
				// 标识放回队列，要求1秒后再触发
				Trigger trigger = new Trigger(flag, System.currentTimeMillis() + EchoTransfer.getCrossInterval());

				// 取优先级
				byte priority = invoker.getPriority();
				if (CommandPriority.isFast(priority)) {
					fasts.add(trigger);
				} else if (CommandPriority.isQuick(priority)) {
					quicks.add(trigger);
				} else {
					slacks.add(trigger);
				}
				return false;
			}

			/** 设计规定：全部异步单元收到数据才执行。所以这个回显标识被丢弃，等待最后那个回显标识到来。**/
			if (!invoker.isCompleted()) {
				//	Logger.warning(this, "defaultDispatch", "%d not completed!", invokerId);
				return false;
			}

			// 生成实例，交给“EndingTrustor”线程处理
			trustor = new EndingTrustor(this, invoker);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 判断有效，启动线程
		boolean success = (trustor != null);
		if (success) {
			trustor.start();
		}

		return success;
	}

	/**
	 * 释放超时调用器，超时触发条件：<br>
	 * 1. 达到系统规定的超时时间。<br>
	 * 2. 达到用户规定的超时时间。<br>
	 * 
	 * 系统规定的超时时间在每个节点的“conf/local.xml”的“<echo>”标签中定义。<br>
	 * 用户规定的超时时间，在Command.setTimeout中定义。<br>
	 */
	private void doReleaseInvoker() {
		int size = invokers.size();
		// 如果不限制超时，或者队列是空集合时不处理
		if (size == 0) {
			return;
		}
		// 生成一个指定长度的队列
		ArrayList<ReleaseFlag> array = new ArrayList<ReleaseFlag>(size);

		// 系统规定的调用器超时时间
		long sysTimeout = EchoTransfer.getInvokerTimeout(); // getMemberTimeout();

		// 锁定处理
		super.lockSingle();
		try {
			// 检查超时和删除
			Iterator<Map.Entry<Long, EchoInvoker>> iterators = invokers.entrySet().iterator();
			while (iterators.hasNext()) {
				Map.Entry<Long, EchoInvoker> entry = iterators.next();
				EchoInvoker invoker = entry.getValue();
				// 如果在运行，忽略它！
				if (invoker.isRunning()) {
					continue;
				}

				// 两个情况，任何一个时间达到，都保存它！
				// 1. 系统的超时时间
				// 2. 用户定义的超时时间
				boolean success = (sysTimeout > 0 && invoker.isTimeout(sysTimeout));
				if (!success) {
					long userTimeout = invoker.getCommandTimeout(); // 用户定义的超时间
					success = (userTimeout > 0 && invoker.isTimeout(userTimeout));
				}
				// 条件成立，保存它！
				if(success) {
					ReleaseFlag flag = new ReleaseFlag(entry.getKey(), entry
							.getValue().getCommandSource());
					array.add(flag);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 释放过期异步调用器
		if (array.size() > 0) {
			releaseTimeoutInvoker(array);
		}
	}

	/**
	 * 释放超时的异步调用器。<br><br>
	 * 
	 * 执行两步操作： <br>
	 * 1. 是WATCH/FRONT节点，设置超时，启动线程处理它！<br>
	 * 2. 不是FRONT/WATCH节点，删除过期异步命令调用器；然后向请求端发送超时通知。<br>
	 * 
	 * @param array 释放单元数组
	 */
	private void releaseTimeoutInvoker(ArrayList<ReleaseFlag> array) {
		for (ReleaseFlag flag : array) {
			// 释放异步调用器
			releaseTimeoutInvoker(flag);
		}
	}

	/**
	 * 返回调用器成员数目
	 * @return 调用器数目
	 */
	public int size() {
		int count = 0;
		// 锁定
		super.lockSingle();
		try {
			count = invokers.size();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return count;
	}

	/**
	 * 输出全部命令的数据副本
	 * @return 命令实例
	 */
	public List<Command> getCommands() {
		ArrayList<Command> array = new ArrayList<Command>();
		super.lockSingle();
		try {
			Iterator<Map.Entry<java.lang.Long, EchoInvoker>> iterator = invokers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<java.lang.Long, EchoInvoker> entry = iterator.next();
				EchoInvoker invoker = entry.getValue();
				array.add(invoker.getCommand().duplicate());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return array;
	}

	/**
	 * 打印处于运行状态的异步调用器
	 */
	public List<String> printInvokers() {
		int capacity = size();
		ArrayList<String> array = new ArrayList<String>(capacity + 10);

		// 锁定
		super.lockSingle();
		try {
			Iterator<Map.Entry<java.lang.Long, EchoInvoker>> iterator = invokers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<java.lang.Long, EchoInvoker> entry = iterator.next();
				EchoInvoker invoker = entry.getValue();

				String s = String.format( "%s | 耗时: %d 秒 | 状态：%s | 线程号：%d", 
						invoker.getClass().getName(), invoker.getRunTime() / 1000,
						(invoker.isRunning() ? "运行" : "等待"), 
						(invoker.isRunning() ? invoker.getThreadId() : -1));
				array.add(s);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return array;
	}

	/**
	 * 向全部异步调用器发出“强制退出线程”命令，包括运行和非运行状态的。运行状态中的必须要退出。
	 */
	private void destroyInvokers() {
		super.lockSingle();
		try {
			Iterator<Map.Entry<java.lang.Long, EchoInvoker>> iterator = invokers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<java.lang.Long, EchoInvoker> entry = iterator.next();
				EchoInvoker invoker = entry.getValue();
				invoker.setForceExit(true);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 启动异步命令。具体处理工作由子类去完成。<br><br>
	 * 
	 * 子类要完成的标准工作（不包括个性化的处理）：<br>
	 * 1. 分配本地回显地址给EchoInvoker（放进“EchoInvoker.setLocal”方法）。<br>
	 * 2. 分配异步数据受理器（就是本实例。在有异步应答数据到达时通知管理池）。<br>
	 * 3. 保存EchoInvoker（调用器编号是保存标识）。<br>
	 * 4. 调用“EchoInvoker.launch”方法，返回服务器的受理结果。<br>
	 * 5. 或者忽略“4”，由启动线程调用“EchoInvoker.launch”，这时总是返回“真”。<br><br>
	 * 
	 * 说明：<br>
	 * 本地回显地址是给调用器使用，操作命令中的回显地址是标识命令的源头。<br>
	 * 
	 * @param invoker 异步调用器。
	 * @return 启动成功返回“真”，否则“假”。是“EchoInvoker.launch”的返回结果。
	 */
	public abstract boolean launch(EchoInvoker invoker);

	/**
	 * 分派回显任务。<br><br>
	 * 
	 * 通过回显标识找到异步调用器，检查它是否具备调用条件。在确认后调用“EchoInvoker.ending”方法。<br>
	 * “dispatch”只被“InvokerPool.process”方法调用。检查和调用规则由子类去完成，
	 * 或者直接调用标准的“defaultDispatch”方法来完成。<br>
	 * 
	 * @param flag 回显标识
	 * @return 分派处理成功返回“真”，否则“假”。
	 */
	protected abstract boolean dispatch(EchoFlag flag);

}

///* (non-Javadoc)
//* @see com.laxcus.thread.VirtualThread#process()
//*/
//@Override
//public void process() {
//	Logger.info(this, "process", "into...");
//
//	// 超时计时器，以外部规定的时间为准，默认是1分钟
//	EchoTimer timer = new EchoTimer(getDisableCheckInterval());
//
//	// 处理快速异步调用器
//	while (!isInterrupted()) {
//		// 判断处于登录状态
//		if (isLogined()) {
//			// 极速处理
//			boolean success = doFast();
//			// 快速处理
//			if (!success) {
//				success = doQuick();
//			}
//			// 处理普通异步调用器
//			if (!success) {
//				success = doSlack();
//			}
//			// 如果成功，继续处理下一个
//			if (success) {
//				downSilentInterval(); // 连续处理发生，降低时延1秒钟
//				continue;
//			} else {
//				upSilentInterval(); // 没有任务发生，降低时延1秒钟
//			}
//		} else {
//			// 没有登录，增加时延
//			upSilentInterval();
////			Logger.warning(this, "process", "%s is Logout!", SiteTag.translate(getLauncher().getFamily()));
//		}
//
//		// 延时，时间根据任务数动态调整
//		delay(getSilentInterval());
//
//		// 失效检测器超时，检测失效过期的调用器
//		// 包括对系统定义的超时和用户定义的超时
//		if (timer.isTimeout()) {
//			// 刷新，时间移到下一阶段
//			timer.refresh();
//			// 释放超时的异步调用器。两个条件：1. 达到系统规定的超时时间；2. 达到用户规定的超时时间
//			doReleaseInvoker();
//		}
//	}
//	Logger.info(this, "process", "exit...");
//}
