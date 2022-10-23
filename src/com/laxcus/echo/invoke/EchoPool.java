/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import com.laxcus.echo.*;
import com.laxcus.pool.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 异步任务工作管理池。<br><br>
 * 
 * EchoPool基于INVOKE/PRODUCE处理规范而设计，提供计算机集群环境下，异步处理工作的流程和方法。<br><br>
 * 
 * EchoPool有两个真接子类：CommandPool、InvokerPool。
 * 在这两个类之下，各运行站点再分别实现自己的命令管理池和调用器管理池。<br>
 * 
 * CommandPool和InvokerPool在每个运行站点上总是成对出现。
 * CommandPool负责接收外部/本地命令，和根据命令建立异步调用器（EchoInvoker）。
 * InvokerPool负责管理异步调用器（数据处理工作由各个具体的EchoInvoker子类完成），和结束释放异步调用器。<br>
 * 
 * CommandPool和InvokerPool在启动新的工作前，总是要检查系统资源，只有在系统资源充裕的情况下，才启动新工作。<br>
 * 
 * @author scott.liang
 * @version 1.6 03/23/2015
 * @since laxcus 1.0
 */
public abstract class EchoPool extends VirtualPool {

	/** 成员失效检测间隔，包括命令和异步调用器，默认是60秒 **/
	private long disableCheckInterval = 60000;

	/**
	 * 设置成员失效检测间隔，包括命令和异步调用器。默认是60秒，不能少于10秒。
	 * @param ms 以毫秒为单位的时间
	 */
	public void setDisableCheckInterval(long ms) {
		if (ms >= 10000) {
			disableCheckInterval = ms;
		}
	}

	/**
	 * 返回成员失效检测间隔，包括命令和异步调用器
	 * @return 以毫秒为单位的时间
	 */
	public long getDisableCheckInterval() {
		return disableCheckInterval;
	}

	/** 子类线程空闲延时间隔，默认1秒钟 **/
	private volatile long silentInterval = 1000;

	/** 调用器数目记录时间 **/
	private long trackTime;

	/**
	 * 构造异步工作管理池
	 */
	protected EchoPool() {
		super();
		// 线程空闲延时间隔
		setSilentInterval(1000);
		trackTime = System.currentTimeMillis();
	}
	
	/**
	 * 线程空闲延时间隔时间
	 * @param ms
	 */
	public void setSilentInterval(long ms) {
		if (ms > 0) {
			silentInterval = ms;
		}
	}

	/**
	 * 返回线程延时间隔
	 * @return
	 */
	public long getSilentInterval() {
		return silentInterval;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {

	}

	/**
	 * 判断是LINUX系统
	 * @return 返回真或者假
	 */
	public boolean isLinux() {
		return getLauncher().isLinux();
	}

	/**
	 * 判断是WINDOWS系统
	 * @return 返回真或者假
	 */
	public boolean isWindows() {
		return getLauncher().isWindows();
	}

	/**
	 * 判断对象类是与指定类匹配，或者是它的子类
	 * @param object 实例类
	 * @param clazz 指定类类型
	 * @return 匹配成功返回“真”，否则“假”。
	 */
	protected boolean isFrom(Class<?> object, Class<?> clazz) {
		return Laxkit.isClassFrom(object, clazz);
	}

	/**
	 * 判断超出限制时间
	 * @param beginTime 开始时间 
	 * @return 返回真或者假
	 */
	private final boolean isConfineTimeout(long beginTime) {
		long ms = EchoTransfer.getMaxConfineTime();
		return ms > 0 && System.currentTimeMillis() - beginTime >= ms;
	}

	/**
	 * 判断当前的节点已经登录状态。
	 * 其中TOP节点永远是登录状态！
	 * 
	 * @return 返回真或者假
	 */
	@Override
	protected boolean isLogined() {
		byte family = getLauncher().getFamily();
		// TOP节点是顶级节点，总是登录状态！
		if (SiteTag.isTop(family)) {
			return true;
		}
		return getLauncher().isLogined();
	}

//	/**
//	 * 检查当前系统运行负载，包括线程、CPU、内存。
//	 * @return 通过返回真，否则假
//	 */
//	protected boolean checkPower() {
//		// 任务线程低于规定值时，检查内存/CPU是不是允许执行
//
//		// 1. 检查内存，必须在指定范围内才能通过
//		// 参数语义：maxMemory是JVM可分配最大内存数，totalMemory是JVM已经分配的内存，freeMemory是totalMemory中未使用部分
//		Runtime rt = Runtime.getRuntime();
//		long usedMemory = (rt.totalMemory() - rt.freeMemory()); // 实际使用的内存
//		boolean missing = EchoTransfer.isVMMemoryMissing(usedMemory, rt.maxMemory());
//		if (missing) {
//			// 内存不足，执行垃圾回收
//			System.gc();
//
//			// 没有达到超时时间，返回假，不允许了！
//			if (!isConfineTimeout(trackTime)) {
//				return false; // 返回假，不执行
//			}
//		}
//
//		// 2. 判断操作系统，检查CPU负载（LINUX含硬盘IO等待），不得超过规定值
//		boolean success = false;
//		if (isLinux()) {
//			success = LinuxEffector.getInstance().allow(EchoTransfer.getMaxCpuRate());
//		} else if (isWindows()) {
//			success = WindowsEffector.getInstance().allow(EchoTransfer.getMaxCpuRate());
//		}
//
//		// 如果不成功，并且没有达到最大限制时间时，不允许执行
//		if (!success && !isConfineTimeout(trackTime)) {
//			return false;
//		}
//
//		// 判断当前运行的线程达到系统规定的最大值
//		// 1. 如果没有运行的线程，允许启动。这时不考虑内存、CPU！保证任何时候有最少一个任务运行！
//		int runThreads = InvokerTrustor.getThreadCount();
//		success = (runThreads <= EchoTransfer.getMaxInvokers()); // 小于当前实例
//		// 不成立时
//		if (!success) {
//			// 判断超过最大限制时间时...
//			success = isConfineTimeout(trackTime);
//		}
//
//		// 成功，更新时间
//		if (success) {
//			trackTime = System.currentTimeMillis();
//		}
//		
//		return success;
//	}
	
	
	
	/**
	 * 检查当前系统运行负载，包括线程、CPU、内存。
	 * @return 通过返回真，否则假
	 */
	protected boolean checkPower() {
		// 任务线程低于规定值时，检查内存/CPU是不是允许执行

		// 1. 检查内存，必须在指定范围内才能通过
		// 参数语义：maxMemory是JVM可分配最大内存数，totalMemory是JVM已经分配的内存，freeMemory是totalMemory中未使用部分
		Runtime rt = Runtime.getRuntime();
		long usedMemory = (rt.totalMemory() - rt.freeMemory()); // 实际使用的内存
		boolean missing = EchoTransfer.isVMMemoryMissing(usedMemory, rt.maxMemory());
		if (missing) {
			// 内存不足，执行垃圾回收
			System.gc();

//			// 没有达到超时时间，返回假，不允许了！
//			if (!isConfineTimeout(trackTime)) {
//				return false; // 返回假，不执行
//			}
		}

		// 2. 判断操作系统，检查CPU负载（LINUX含硬盘IO等待），不得超过规定值
		boolean success = false;
		if (isLinux()) {
			success = LinuxEffector.getInstance().allow(EchoTransfer.getMaxCpuRate());
		} else if (isWindows()) {
			success = WindowsEffector.getInstance().allow(EchoTransfer.getMaxCpuRate());
		}

//		// 如果不成功，并且没有达到最大限制时间时，不允许执行
//		if (!success && !isConfineTimeout(trackTime)) {
//			return false;
//		}

		// 如果不成功，并且没有达到最大限制时间时，不允许执行
		if (!success) {
			return false;
		}
		
		// 判断当前运行的线程达到系统规定的最大值
		// 1. 如果没有运行的线程，允许启动。这时不考虑内存、CPU！保证任何时候有最少一个任务运行！
		int runThreads = InvokerTrustor.getThreadCount();
		success = (runThreads <= EchoTransfer.getMaxInvokers()); // 小于当前实例
		// 不成立时
		if (!success) {
			// 判断超过最大限制时间时...
			success = isConfineTimeout(trackTime);
		}

		// 成功，更新时间
		if (success) {
			trackTime = System.currentTimeMillis();
		}
		
		return success;
	}
	
	/**
	 * 返回当前CPU的使用比率
	 * @return CPU使用比率
	 */
	public double getCPURate() {
		if (isLinux()) {
			return LinuxEffector.getInstance().getRate();
		} else if (isWindows()) {
			return WindowsEffector.getInstance().getRate();
		}
		return 100.0f;
	}

	/**
	 * 建立RPC通信客户端
	 * @param endpoint 目标站点地址
	 * @return 返回EchoClient句柄
	 */
	protected EchoClient createClient(Cabin endpoint) {
		return ClientCreator.createEchoClient(endpoint);
	}

	/**
	 * 向请求端发送超时通知
	 * @param endpoint 目标站点地址
	 * @return 发送成功返回真，否则假
	 */
	protected boolean replyTimeout(Cabin endpoint) {
		EchoClient client = createClient(endpoint);
		boolean success = (client != null);
		if (success) {
			EchoCode code = new EchoCode(Major.FAULTED, Minor.ECHO_TIMEOUT);
			EchoHead head = new EchoHead(code, 0);
			success = client.doStart(head);
		}
		if (success) {
			EchoTail tail = new EchoTail(true, 0, 0);
			success = client.doStop(tail);
		}
		if (client != null) {
			client.close();
		}
		return success;
	}

}


///**
// * 检查当前系统运行负载，包括线程、CPU、内存。
// * @return 通过返回真，否则假
// */
//protected boolean checkPower() {
//	// 1. 如果没有运行的线程，允许启动。这时不考虑内存、CPU！保证任何时候有最少一个任务运行！
//	int runThreads = InvokerTrustor.getThreadCount();
//	if (runThreads < 1) {
//		return true;
//	}
//
//	// 2. 判断当前运行的线程达到系统规定的最大值
//	if (runThreads >= EchoTransfer.getMaxInvokers()) {
//		// 如果超出规定时间，可以启动一个新的调用器
//		if (isConfineTimeout(trackTime)) {
//			trackTime = System.currentTimeMillis(); // 记录新的时间
//			return true;
//		} else {
//			return false; // 返回假，不执行
//		}
//	}
//
//	// 任务线程低于规定值时，检查内存/CPU是不是允许执行
//
//	// 3. 检查内存，必须在指定范围内才能通过
//	// 参数语义：maxMemory是JVM可分配最大内存数，totalMemory是JVM已经分配的内存，freeMemory是totalMemory中未使用部分
//	Runtime rt = Runtime.getRuntime();
//	long usedMemory = (rt.totalMemory() - rt.freeMemory()); // 实际使用的内存
//	boolean missing = EchoTransfer.isVMMemoryMissing(usedMemory, rt.maxMemory());
//	if (missing) {
//		System.gc(); // 内存不足，执行垃圾回收
//		return false;
//	}
//
//	// 3. 判断操作系统，检查CPU负载（LINUX含硬盘IO等待），不得超过规定值
//	boolean success = false;
//	if (isLinux()) {
//		success = LinuxEffector.getInstance().allow(EchoTransfer.getMaxCpuRate());
//	} else if (isWindows()) {
//		success = WindowsEffector.getInstance().allow(EchoTransfer.getMaxCpuRate());
//	}
//
//	// 如果成功，记录时间，做为超时判断依据
//	if (success) {
//		trackTime = System.currentTimeMillis();
//	}
//
//	return success;
//}

///**
// * 检查当前系统运行负载，包括线程、CPU、内存。
// * @return 通过返回真，否则假
// */
//protected boolean checkPower() {
//	// 1. 如果没有运行的线程，允许启动。这时不考虑内存、CPU！保证任何时候有最少一个任务运行！
//	int runThreads = InvokerTrustor.getThreadCount();
//	if (runThreads < 1) {
//		return true;
//	}
//
//	// 2. 判断当前运行的线程达到系统规定的最大值
//	if (runThreads >= EchoTransfer.getMaxInvokers()) {
//		// 如果超出规定时间，可以启动一个新的调用器
//		if (isConfineTimeout(trackTime)) {
//			trackTime = System.currentTimeMillis(); // 记录新的时间
//			return true;
//		} else {
//			return false; // 返回假，不执行
//		}
//	}
//
//	// 任务线程低于规定值时，检查内存/CPU是不是允许执行
//
//	// 3. 检查内存，必须在指定范围内才能通过
//	// 参数语义：maxMemory是JVM可分配最大内存数，totalMemory是JVM已经分配的内存，freeMemory是totalMemory中未使用部分
//	Runtime rt = Runtime.getRuntime();
//	long usedMemory = (rt.totalMemory() - rt.freeMemory()); // 实际使用的内存
//	boolean missing = EchoTransfer.isVMMemoryMissing(usedMemory, rt.maxMemory());
//	if (missing) {
//		// 内存不足，执行垃圾回收
//		System.gc();
//
//		// 在限制时间内，返回假
//		if (!isConfineTimeout(trackTime)) {
//			return false; // 返回假，不执行
//		}
//	}
//
//	// 3. 判断操作系统，检查CPU负载（LINUX含硬盘IO等待），不得超过规定值
//	boolean success = false;
//	if (isLinux()) {
//		success = LinuxEffector.getInstance().allow(EchoTransfer.getMaxCpuRate());
//	} else if (isWindows()) {
//		success = WindowsEffector.getInstance().allow(EchoTransfer.getMaxCpuRate());
//	}
//
//	// 如果成功，记录时间，做为超时判断依据
//	if (success) {
//		trackTime = System.currentTimeMillis();
//	} else {
//		// 如果达到最大限制时间时，允许它执行
//		if (isConfineTimeout(trackTime)) {
//			trackTime = System.currentTimeMillis();
//			success = true;
//		}
//	}
//
//	return success;
//}