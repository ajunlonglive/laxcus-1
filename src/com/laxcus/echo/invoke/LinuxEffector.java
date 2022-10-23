/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.log.client.*;

/**
 * LINUX系统资源检查器 <br>
 * 检查LINUX系统的CPU的资源使用情况
 * 
 * @author scott.liang
 * @version 1.0 12/17/2011
 * @since laxcus 1.0
 */
public class LinuxEffector extends TimerTask {

	/**
	 * LINUX CPU时间片元组 (单位：jiffies)
	 * 
	 * @author scott.liang
	 * @version 1.0 12/17/2011
	 * @since laxcus 1.0
	 */
	final class Tuple {
		/** 用户态占用的CPU运行时间 **/
		long user;

		/** 负进程占用的CPU运行时间 **/
		long nice;

		/** 核心占用的CPU运行时间 **/
		long system;

		/** 空闲态CPU运行运行，不包括iowait **/
		long idle;

		/** 磁盘IO等待占用的CPU运行时间 **/
		long iowait;

		/** 硬件中断占用CPU时间 **/
		long irq;

		/** 软件中断占用CPU时间 **/
		long sirq;

		/** 虚拟机占用CPU时间 **/
		long st;

		/** 虚任务占用CPU时间 **/
		long quest;

		/**
		 * 构造默认LINUX CPU时间片元组。默认参数全部是0
		 */
		public Tuple() {
			user = nice = system = idle = 0L;
			iowait = irq = sirq = 0L;
			st = quest = 0L;
		}

		/**
		 * 替换参数
		 * @param that CPU时间片元组
		 */
		public void set(Tuple that) {
			user = that.user;
			nice = that.nice;
			system = that.system;
			idle = that.idle;

			iowait = that.iowait;

			irq = that.irq;
			sirq = that.sirq;
			st = that.st;
			quest = that.quest;
		}

		/**
		 * 统计参数总值
		 * @return 总值
		 */
		public long sum() {
			return user + nice + system + idle + iowait + irq + sirq + st
					+ quest;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("%d %d %d %d %d %d %d %d %d", user, nice,
					system, idle, iowait, irq, sirq, st, quest);
		}
	}

	/** 句柄 **/
	private static LinuxEffector selfHandler = new LinuxEffector();

	/** 第一次 **/
	private Tuple first;

	/** 第二次 **/
	private Tuple second;

	/** 当前运行资源占用的CPU比率 **/
	private volatile double currentCPURate = 0.0f;

	/**
	 * 构造LINUX系统资源检查器
	 */
	private LinuxEffector() {
		super();
	}

	/**
	 * 返回LinuxEffector静态句柄
	 * @return LinuxEffector实例
	 */
	public static LinuxEffector getInstance() {
		return LinuxEffector.selfHandler;
	}

	/**
	 * 根据最大限值，判断当前CPU负载在允许范围内
	 * @param max 最大限值
	 * @return 返回真或者假
	 */
	public boolean allow(double max) {
		return currentCPURate <= max;
	}

	/**
	 * 设置当前CPU占用比率
	 * @param value CPU占比
	 */
	private void setRate(double value) {
		currentCPURate = value;
	}
	
	/**
	 * 返回当前CPU使用比率
	 * @return CPU使用比率
	 */
	public double getRate(){
		return currentCPURate;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		check();
	}
	
	/**
	 * 检查磁盘文件
	 */
	private void check() {
		final String regex = "^\\s*(?i)(?:CPU)\\s+([0-9|\\x20]+)\\s*$";

		while (true) {
			// 读第一行
			String line = readLine();
			//			Logger.debug(this, "check", "this is '%s'", line);
			// 判断参数正确，必须是数字（正数）和空格的组合
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(line);
			// 参数不正确，退出
			if (!matcher.matches()) {
				break;
			}

			line = matcher.group(1);
			Tuple tuple = split(line);
			evaluate(tuple);
			break;
		}
	}


	/**
	 * 计算CPU当前被占用的比率
	 * @param e
	 */
	private void evaluate(Tuple e) {
		if (first == null) {
			first = e;
		} else if (second == null) {
			second = e;
			calculate();
		} else {
			first.set(second);
			second.set(e);
			calculate();
		}
	}

	/**
	 * 获取当前CPU使用比率
	 */
	private void calculate() {
		// 替换后，计算参数
		long t1 = first.sum();
		long t2 = second.sum();
		double ts = 0.0f;
		// 必须大于0
		if (t2 - t1 > 0) {
			long total = t2 - t1;
			long idle = second.idle - first.idle;
			ts = ((double)(total - idle) / (double)total) * 100.0f;
		}
		// 设置CPU当前被占用比率
		setRate(ts);
		//		System.out.printf("rt1 is %f, %d\n", ts, (t2 - t1));

		//		Logger.debug(this, "calculate", "CPU Rate is %.3f", ts);
	}

	/**
	 * 分割参数
	 * @param line
	 * @return
	 */
	private Tuple split(String line) {
		String[] items = line.split("\\s+");
		// 解析元组参数
		Tuple tuple = new Tuple();
		if (items.length >= 4) {
			tuple.user = parseLong(items[0]);
			tuple.nice = parseLong(items[1]);
			tuple.system = parseLong(items[2]);
			tuple.idle = parseLong(items[3]);
		}
		if (items.length >= 5) {
			tuple.iowait = parseLong(items[4]);
		}
		if (items.length >= 7) {
			tuple.irq = parseLong(items[5]);
			tuple.sirq = parseLong(items[6]);
		}
		if (items.length >= 8) {
			tuple.st = parseLong(items[7]);
		}
		if (items.length >= 9) {
			tuple.quest = parseLong(items[8]);
		}
		return tuple;
	}

	/**
	 * 返回LONG
	 * @param e
	 * @return
	 */
	private long parseLong(String e) {
		return Long.parseLong(e);
	}

	/**
	 * 从系统文件中，读取CPU第一行参数。
	 * @return  字符串
	 */
	private String readLine() {
		// 系统文件名
		final String filename = "/proc/stat";
		// 只读文件的第一行参数。以“cpu ”开头。
		try {
			byte[] b = new byte[256];  // 256个字节保存第一行参数绰绰有余
			FileInputStream in = new FileInputStream(filename);
			int len = in.read(b);
			in.close();

			String line = new String(b, 0, len);
			StringReader sr = new StringReader(line);
			BufferedReader buf = new BufferedReader(sr);
			line = buf.readLine();
			buf.close();
			sr.close();
			return line;
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}
	
	/** CPU名称 **/
	private String cpu;
	
	/**
	 * 返回CPU名称
	 * @return
	 */
	public String getCPUName() {
		if (cpu != null) {
			return cpu;
		}

		// 正则表达式
		final String regex = "^\\s*(?i)(?:model\\s+name\\s+\\:)\\s+(.+?)\\s*$";
		Pattern pattern = Pattern.compile(regex);

		// 系统文件名
		final String filename = "/proc/cpuinfo";
		// 只读文件的第一行参数。以“cpu ”开头。
		try {
			byte[] b = new byte[10240];
			FileInputStream in = new FileInputStream(filename);
			int len = in.read(b);
			in.close();

			StringReader sr = new StringReader(new String(b, 0, len));
			BufferedReader buf = new BufferedReader(sr);
			// 找到匹配的参数
			while (true) {
				String line = buf.readLine();
				if (line == null) {
					break;
				}
				// 匹配
				Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) {
					cpu = matcher.group(1).replaceAll("\\s+", " ").trim();
					break;
				}
			}
			// 关闭
			buf.close();
			sr.close();
		} catch (IOException e) {
			Logger.error(e);
		}
		
		return cpu;
	}
	
	/** LINUX版本 **/
	private String version;
	
	/**
	 * 返回LINUX版本名称
	 * @return 版本名称
	 */
	public String getVersion() {
		if (version != null) {
			return version;
		}

		// 系统文件名
		final String filename = "/etc/issue";
		// 只读文件的第一行，是LINUX版本
		try {
			byte[] b = new byte[512];
			FileInputStream in = new FileInputStream(filename);
			int len = in.read(b);
			in.close();

			// 判断有效，取第一行
			if (len > 0) {
				StringReader sr = new StringReader(new String(b, 0, len));
				BufferedReader buf = new BufferedReader(sr);
				version = buf.readLine().replaceAll("\\s+", " ").trim();
				// 关闭
				buf.close();
				sr.close();
			}
		} catch (IOException e) {
			Logger.error(e);
		}
		
		return version;
	}

	//	public static void main(String[] args) {
	//		//		String[] s = new String[] { "1331 1 1971 80422 5006 20 5 0 0",
	//		//				"1333 1 1973 83694 5006 21 5 0 0",
	//		//		"1469 1 2113 115970 5032 25 6 0 0" };
	//		String[] s = new String[]{"5856 0 1624 26133 5590 17 15 0 0","5858 0 1625 26331 5590 17 15 0 0",
	//				"5861 0 1628 26527 5590 17 15 0 0","5862 0 1629 26726 5590 17 15 0 0"};
	//		for(String line : s) {
	//			Tuple tuple = LinuxEffector.getInstance().split(line);
	//			System.out.println(tuple);
	//			LinuxEffector.getInstance().evaluate(tuple);
	//		}
	//
	//	}
}