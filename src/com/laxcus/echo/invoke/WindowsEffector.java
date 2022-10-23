/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.util.*;

import com.laxcus.log.client.*;

/**
 * WINDOWS系统资源检查器 <br>
 * 检查WINDOWS系统的CPU的资源使用情况
 * 
 * @author scott.liang
 * @version 1.0 10/8/2011
 * @since laxcus 1.0
 */
public class WindowsEffector extends TimerTask {

	/**
	 * WINDOWS时间片元组
	 * 
	 * @author scott.liang
	 * @version 1.0 10/8/2011
	 * @since laxcus 1.0
	 */
	final class Tuple {

		/** 空闲的CPU运行时间片 **/
		long idle;

		/** 系统核心占用的CPU运行时间片 **/
		long kernel;

		/** 用户占用的CPU运行时间片 **/
		long user;

		/**
		 * 构造WINDOWS时间片元组
		 */
		public Tuple() {
			super();
			user = kernel = idle = 0L;
		}

		/**
		 * 构造WINDOWS时间片元组，指定全部参数
		 * @param idle
		 * @param kernel
		 * @param user
		 */
		public Tuple(long idle, long kernel, long user) {
			this();
			set(idle, kernel, user);
		}

		/**
		 * 设置WINDOWS时间片元组参数
		 * @param idle 空闲时间片
		 * @param kernel 内核时间片
		 * @param user 用户时间片
		 */
		public void set(long idle, long kernel, long user) {
			this.idle = idle;
			this.kernel = kernel;
			this.user = user;
		}

		/**
		 * 复制参数
		 * @param that
		 */
		public void set(Tuple that) {
			idle = that.idle;
			kernel = that.kernel;
			user = that.user;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("%d,%d,%d", idle, kernel, user);
		}
	}

	/** 句柄 **/
	private static WindowsEffector selfHandler = new WindowsEffector();

	/** 第一次 **/
	private Tuple first;

	/** 第二次 **/
	private Tuple second;

	/** 当前运行资源占用的CPU比率 **/
	private volatile double currentCPURate = 0.0f;

	/**
	 * 构造WINDOWS系统资源检查器
	 */
	private WindowsEffector() {
		super();
	}

	/**
	 * 返回WindowsEffector静态句柄
	 * @return
	 */
	public static WindowsEffector getInstance() {
		return WindowsEffector.selfHandler;
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
	 * @param value CPU占比值
	 */
	private void setRate(double value) {
		currentCPURate = value;
	}
	
	/**
	 * 返回当前CPU使用率
	 * @return CPU使用率真
	 */
	public double getRate() {
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
	 * 从JNI接口中提取时间片参数
	 */
	private void check() {
		long[] b = WindowsTimes.getTimes();
		if (b == null) {
			Logger.warning(this, "check", "wineff is invalied!");
			return;
		}

		if (first == null) {
			first = new Tuple(b[0], b[1], b[2]);
		} else if (second == null) {
			second = new Tuple(b[0], b[1], b[2]);
			evaluate();
		} else {
			first.set(second);
			second.set(b[0], b[1], b[2]);
			evaluate();
		}
	}

	/**
	 * 计算CPU资源使用占用比率
	 */
	private void evaluate() {
//		Logger.debug(this, "evaluate", "%s | %s", first, second);
		long idle = second.idle - first.idle;
		long kernel = second.kernel - first.kernel;
		long user = second.user - first.user;
		long total = kernel + user;

		// CPU占用率 = （总时间-空间时间）/总时间
		double rate = ((double) (total - idle) / (double) (total)) * 100.0f;
		setRate(rate);
//		Logger.debug(this, "evaluate", "rate is %.3f", rate);
	}

}