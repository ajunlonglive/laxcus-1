/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ring;

import java.io.*;
import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.log.client.*;
import com.laxcus.util.lock.*;

/**
 * RING命令输入器
 * 
 * @author scott.liang
 * @version 1.0 9/30/2019
 * @since laxcus 1.0
 */
public class RingInputter implements Runnable {

	/** 顺序线程号 **/
	private int nextId = 1;

	/**
	 * 返回下一个线程编号
	 * @return 线程编号
	 */
	private synchronized int nextId() {
		return nextId++;
	}

	/** 线程句柄 */
	private Thread thread;

	/** 线程运行标记 */
	private volatile boolean running;

	/** JAVA控制台 */
	private Console console;

	/** 语法解析器 **/
	private RingSplitter splitter = new RingSplitter();

	/** 锁 **/
	private MutexLock lock = new MutexLock();

	/** 编号 -> 调用器 **/
	private TreeMap<Integer, RingInvoker> invokers = new TreeMap<Integer, RingInvoker>();

	/**
	 * 构造RING命令输入器
	 */
	public RingInputter() {
		super();
		running = false;
	}

	/**
	 * 删除调用器
	 * @param id
	 * @return
	 */
	public boolean remove(int id) {
		RingInvoker invoker = null;
		// 锁定删除
		lock.lockSingle();
		try {
			invoker = invokers.remove(id);
		} catch (Throwable e) {

		} finally {
			lock.unlockSingle();
		}
		// 判断有效
		return invoker != null;
	}

	/**
	 * 线程延时等待。单位：毫秒。
	 * @param ms 超时时间
	 */
	private synchronized void delay(long ms) {
		try {
			if (ms > 0L) {
				wait(ms);
			}
		} catch (InterruptedException e) {
			Logger.error(e);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		running = true;

		// 循环处理
		while (true) {
			boolean exit = todo();
			if (exit) {
				break;
			}
		}

		// 等待全部测试完成退出！
		do {
			boolean success = invokers.isEmpty();
			if (success) {
				break;
			}
			delay(1000);
		} while (true);

		// 停止日志服务
		Logger.stopService();

		running = false;
		thread = null;

		// 关闭进程
		System.exit(0);
	}

	/**
	 * 判断线程处于运行状态
	 * @return 返回真或者假
	 */
	public final boolean isRunning() {
		return running && thread != null;
	}

	/**
	 * 判断线程处于停止状态
	 * @return 返回真或者假
	 */
	public boolean isStopped() {
		return !isRunning();
	}

	/**
	 * 启动线程，在启动线程前调用"init"方法
	 * @param priority 线程优化级，见Thread中的定义
	 * @return 成功返回“真”，失败“假”。
	 */
	public boolean start(int priority) {
		// 检测线程
		synchronized (this) {
			if (thread != null) {
				return false;
			}
		}
		// 启动线程
		thread = new Thread(this);
		thread.setPriority(priority);
		thread.start();
		return true;
	}

	/**
	 * 使用线程较小优先级启动线程
	 * @return 成功返回“真”，失败“假”。
	 */
	public boolean start() {
		return start(Thread.NORM_PRIORITY);
	}

	/**
	 * 初始化控制台
	 * @return 成功返回真，否则假
	 */
	public boolean initialize() {
		if (console == null) {
			console = System.console();
		}

		boolean success = (console != null);
		if (success) {
			String e = "Welcome to Laxcus Ring\nCommand: Help / Ring / Exit|Quit ...";
			System.out.println(e);
		}
		return success;
	}

	/**
	 * 从控制台上接受命令
	 * @return 返回命令，或者空字符串
	 */
	private String input() {
		// 从控制台读取
		String cmd = console.readLine();
		// 如果是空指针，返回一个空字符串，在外部判断退出。
		if (cmd == null) {
			return "";
		}
		return cmd.trim();
	}

	/**
	 * 判断是退出命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	private boolean isExit(String input) {
		return input.matches("^\\s*(?i)(EXIT|QUIT)\\s*$");
	}

	/**
	 * 判断是帮助命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	private boolean isHelp(String input) {
		return input.matches("^\\s*(?i)(HELP)\\s*$");
	}

	/**
	 * 显示帮助命令
	 */
	private void help() {
		System.out.println("RING [-SECURE -COUNT -TIMEOUT -DELAY] TO [SOCKET]");
		System.out.println("-SECURE | -S : yes or no");
		System.out.println("-COUNT | -C : packet count");
		System.out.println("-TIMEOUT | -T : socket timeout");
		System.out.println("-DELAY | -D : send interval");
		System.out.println("-SOCKET : TCP://ip:port or UDP://ip:port");
	}

	/**
	 * 判断是RING命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	private boolean isRing(String input) {
		try {
			if (splitter.matches(input)) {
				return splitter.split(input) != null;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return false;
	}

	/**
	 * 判断是无效语句
	 */
	private void invalid() {
		System.out.println("Incorrect Syntax!");
	}

	/**
	 * 核准退出
	 * @return 退出返回真，否则假
	 */
	private boolean affirm() {
		String suffix = "Exit Laxcus Ring (yes/no)?";
		String cmd = console.readLine("%s", suffix);
		// 如果是空指针，返回假
		if(cmd == null) {
			return false;
		}
		return cmd.matches("^\\s*(?i)(?:YES|Y)\\s*$");
	}

	/**
	 * 核准退出
	 * @return 退出返回真，否则假
	 */
	private boolean yea() {
		String suffix = "Correct Syntax, do it (yes/no)?";
		String cmd = console.readLine("%s", suffix);
		// 如果是空指针，返回假
		if(cmd == null) {
			return false;
		}
		return cmd.matches("^\\s*(?i)(?:YES|Y)\\s*$");
	}

	/**
	 * 执行任意的输入操作
	 * @return 退出返回真，否则假
	 */
	private boolean todo() {
		String cmd = input();

		// 无效命令，返回假
		if (cmd.isEmpty()) {
			System.out.println("Cannot be empty!");
			return false;
		}

		// 判断是空符串或者退出命令
		if (isExit(cmd)) {
			// 确认是退出
			boolean exit = affirm();
			// 在控制台打印退出信息
			if (exit) {
				System.out.println("Close ...");
			}
			return exit; // 退出命令
		}

		// 判断命令
		if (isHelp(cmd)) {
			help();
		} else if (isRing(cmd)) {
			boolean b = yea();
			if (b) launch(cmd);
		} else {
			invalid();
		}

		return false;
	}

	/**
	 * 启动RING命令
	 * @param input 输入语句
	 */
	private void launch(String input) {
		Ring cmd = null;
		try {
			cmd = splitter.split(input);
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
		if (cmd == null) {
			return;
		}

		// 保存和启动
		int no = nextId();
		RingInvoker invoker = new RingInvoker(this, cmd, no);
		boolean success = true;
		lock.lockSingle();
		try {
			invokers.put(no, invoker);
			success = true;
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			lock.unlockSingle();
		}
		// 启动
		if (success) {
			invoker.start();
		}
	}

}