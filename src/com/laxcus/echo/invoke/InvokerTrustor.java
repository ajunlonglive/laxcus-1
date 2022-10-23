/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import com.laxcus.echo.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 异步命令调用器代理。<br>
 * 它在线程中执行异步命令调用器的“launch/ending”两个方法。
 * 
 * @author scott.liang
 * @version 1.1 03/20/2014
 * @since laxcus 1.0
 */
public abstract class InvokerTrustor implements Runnable {

	/** 调用器信使 **/
	protected static InvokerMessenger invokerMessenger;

	/**
	 * 设置调用器消息句柄
	 * @param e 实例
	 */
	public static boolean setInvokerMessenger(InvokerMessenger e) {
		if (InvokerTrustor.invokerMessenger == null) {
			InvokerTrustor.invokerMessenger = e;
			return true;
		}
		return false;
	}

	/** 顺序线程号 **/
	private static int threadNumber = 1;

	/**
	 * 返回下一个线程编号
	 * @return 线程编号
	 */
	private static synchronized int nextThreadNumber() {
		return threadNumber++;
	}

	//	/** 当前运行线程数 **/
	//	private static volatile int threadCount = 0;
	//
	//	/**
	//	 * 增加一个运行线程
	//	 */
	//	private static void addThread() {
	//		InvokerTrustor.threadCount = InvokerTrustor.threadCount + 1;
	//	}
	//
	//	/**
	//	 * 释放一个运行线程
	//	 */
	//	private static void dropThread() {
	//		InvokerTrustor.threadCount = InvokerTrustor.threadCount - 1;
	//	}

	/** 当前运行线程数 **/
	private static int threadCount = 0;

	/**
	 * 同步方式增加一个运行线程
	 */
	private static synchronized void addThread() {
		InvokerTrustor.threadCount = InvokerTrustor.threadCount + 1;
	}

	/**
	 * 同步方式释放一个运行线程
	 */
	private static synchronized void dropThread() {
		InvokerTrustor.threadCount = InvokerTrustor.threadCount - 1;
	}

	/**
	 * 统计运行线程数
	 * @return 当前线程数目
	 */
	public static int getThreadCount() {
		return InvokerTrustor.threadCount;
	}

	/** 调用器管理池 **/
	private InvokerPool invokerPool;

	/** 异步命令调用器 **/
	protected EchoInvoker invoker;

	/** 线程句柄 **/
	private Thread thread;

	/** 进入线程状态 **/
	private volatile boolean running;

	/**
	 * 构造异步调用器代理，指定调用器管理池和异步调用器。
	 * @param pool 调用器管理器
	 * @param e 异步调用器实例
	 */
	protected InvokerTrustor(InvokerPool pool, EchoInvoker e) {
		super();
		running = false;
		invokerPool = pool;
		invoker = e;
	}

	/**
	 * 判断线程进入运行状态
	 * @return 返回真或者假
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * 返回异步命令调用器句柄
	 * @return EchoInvoker实例
	 */
	public EchoInvoker getInvoker() {
		return invoker;
	}

	/**
	 * 返回调用器管理池句柄
	 * @return InvokerPool实例
	 */
	protected InvokerPool getInvokerPool() {
		return invokerPool;
	}

	/**
	 * 发送故障错误给请求端
	 * @param msg 故障消息
	 */
	private void sendFault(String msg) {
		// 请求端的回显地址
		Cabin hub = invoker.getCommandSource();
		// 如果没有回显地址，命令是由本地站点发出的，例如终端，或者带“ShiftXXX”前缀的转发命令
		if (hub == null) {
			Logger.warning(this, "sendFault", "null hub");
			return;
		}

		// 默认系统错误
		EchoCode code = new EchoCode(Major.FAULTED, Minor.SYSTEM_FAILED);
		EchoHead head = new EchoHead(code, 0);
		// 有错误消息，放在帮助信息中
		if (msg != null) {
			DefaultEchoHelp help = new DefaultEchoHelp(msg);
			head.setHelp(help);
		}

		// 发送给请求端
		EchoClient client = ClientCreator.createEchoClient(hub);
		boolean success = false;
		// 有效情况下发送包
		if (client != null) {
			client.shoot(head);
			// 销毁
			client.destroy();
		}

		Logger.debug(this, "sendFault", success, "send to %s", hub);
	}

	/**
	 * 绑定资源
	 */
	private void attach() {
		// 线程数加1
		InvokerTrustor.addThread();
		// 异步调用器进入运行状态
		invoker.setRunning(true);

		Logger.debug(this, "attach", "%d#%s into threads:%d",
				invoker.getInvokerId(), invoker.getCommand(), InvokerTrustor.getThreadCount());
	}

	/**
	 * 解除资源
	 */
	private void detach() {
		// 调用异步调用器为“非运行”状态
		invoker.setRunning(false);
		// 线程数减1
		InvokerTrustor.dropThread();

		Logger.debug(this, "detach", "current threads:%d", InvokerTrustor.getThreadCount());
	}

	/**
	 * 任务延时
	 * @param ms 等待时间，单位：毫秒
	 */
	private synchronized void delay(long ms) {
		try {
			if (ms > 0L) {
				wait(ms);
			}
		} catch (InterruptedException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}

	/**
	 * 唤醒线程
	 */
	private synchronized void wakeup() {
		try {
			notify();
		}catch(IllegalMonitorStateException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}

	/**
	 * 启动线程
	 */
	public void start() {
		// 类名称
		String name = invoker.getClass().getSimpleName();
		name = String.format("%s_%d", name, InvokerTrustor.nextThreadNumber());

		// 建立线程
		if (EchoTransfer.getStackSize() > 0) {
			thread = new Thread(null, this, name, EchoTransfer.getStackSize());
		} else {
			thread = new Thread(this, name);
		}
		// 调用器线程优先级在所有线程中最小
		thread.setPriority(Thread.MIN_PRIORITY);
		// 启动线程
		thread.start();

		// 判断进入“run”方法，线程退出。否则最多延时10秒
		long endTime = System.currentTimeMillis() + 10000;
		while (!running) {
			// 延时100毫秒
			delay(100);
			// 如果线程释放，或者超时，退出！
			if (thread == null || System.currentTimeMillis() >= endTime) {
				break;
			}
		}
	}

	/**
	 * 在线程中进行分布任务的处理
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// 锁定
		attach();

		// 线程进入状态
		running = true;
		// 唤醒等待的"start"方法
		wakeup();

		// 调用器进行线程时间
		invoker.setThreadStartTime(System.currentTimeMillis());

		// 记录下一次迭代
		invoker.nextIterate();

		// 设置线程标识符
		invoker.setThreadId(thread.getId());
		// 收集接收的数据
		invoker.collectReceiveFlowSize();
		// 加入限制
		invoker.shackle();

		// 记录调用器
		Tigger.invoker(true, invoker);

		// 调用接口，准备启动
		if (InvokerTrustor.invokerMessenger != null) {
			InvokerTrustor.invokerMessenger.startInvoker(invoker);
		}

		// 处理异步任务。如果截获错误，要发送出错给请求端
		boolean success = false;
		try {
			success = process();
		} catch (InvokerException e) {
			Logger.error(e);
			// 发送错误消息给命令的请求端，前提是有回显地址
			sendFault(e.getMessage());
		} catch (Throwable e) {
			// 严重故障
			Logger.fatal(e);
			// 发送错误信息给命令请求端
			sendFault(e.getMessage());
		}

		// 解除限制
		invoker.unshackle();

		// "push"线程，即ReplyReceiver没有退出时，延时
		// LaunchTrustor / EndingTrustor 都会发生这种现象，特别注意！！！
		while (invoker.getPushThreads() > 0) {
			delay(500);
		}

		Logger.debug(this, "run", success, "'%s' Quit:%s",
				invoker.getCommand(), (invoker.isQuit() ? "Yes" : "No"));

		// 设置本次处理是成功或者失败
		invoker.setPerfectly(success);
		// 撤销线程标识符
		invoker.setThreadId(-1);
		// 记录撤销
		Tigger.invoker(false, invoker);

		// 统计运行时处理时间
		invoker.addProcessTime(invoker.getThreadUsedTime());

		// 调用接口，通知停止
		if (InvokerTrustor.invokerMessenger != null) {
			InvokerTrustor.invokerMessenger.stopInvoker(invoker, success);
		}

		// 处理失败或者完成整套处理流程，都删除句柄
		if (!success || invoker.isQuit()) {
			// 在退出前，取出命令的时间记录，发给日志节点保存起来 
			postTime();
			// 删除
			invokerPool.removeInvoker(invoker);
		}

		// 解除绑定
		detach();

		// 线程结束
		running = false;

		// 清除线程
		thread = null;
	}
	
	/**
	 * 把用户执行信息记录下来，发送给日志
	 */
	private void postTime() {
		// 拒绝FRONT\WATCH\LOG节点
		SiteLauncher launcher = InvokerPool.getLauncher();
		byte siteFamily = launcher.getFamily();
		boolean refuse = (SiteTag.isFront(siteFamily)
				|| SiteTag.isWatch(siteFamily) || SiteTag.isLog(siteFamily));
		if (refuse) {
			return;
		}
		
		Siger issuer = invoker.getIssuer();
		if (issuer != null) {
			String cmd = invoker.getCommand().getClass().getSimpleName();
			 long invokerId = invoker.getInvokerId();
			 int iterateIndex = invoker.getIterateIndex();
			 long initTime = invoker.getInitTime();
			 long endTime = System.currentTimeMillis();
			 long processTime = invoker.getProcessTime();
			 
			// 用户签名、命令名称、调用器编号、迭代次数 & 调用器开始时间、调用器结束时间、有效处理时间（线程执行时间）。保存到日志节点
			// UIM: USER INVOKER MESSAGE
			// UIT: USER INVOKER TIME
			String str = String.format("UIM %s %s %d %d UIT %d %d %d", 
					issuer, cmd, invokerId, iterateIndex, initTime, endTime, processTime);
//			Logger.info(str);
			Biller.cost(str);
		}
	}

	/**
	 * 释放资源
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() {
		if (invokerPool != null) {
			invokerPool = null;
		}
		if (invoker != null) {
			invoker = null;
		}
	}

	/**
	 * 处理调用器的异步工作
	 * @return 处理成功返回“真”，否则“假”。
	 */
	public abstract boolean process();
}