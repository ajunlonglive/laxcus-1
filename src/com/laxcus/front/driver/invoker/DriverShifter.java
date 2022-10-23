/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.front.driver.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.front.driver.pool.*;
import com.laxcus.front.pool.*;
import com.laxcus.mission.*;
import com.laxcus.util.*;

/**
 * 驱动任务转换器。<br><br>
 * 
 * 根据驱动任务，生成对应的异步调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 2/8/2018
 * @since laxcus 1.0
 */
public abstract class DriverShifter {

	/**
	 * 构造默认的驱动任务转义器
	 */
	protected DriverShifter() {
		super();
	}
	
	/**
	 * 根据命令转义和生成关联的异步调用器
	 * @param mission 驱动任务
	 * @return 返回驱动调用器实例
	 */
	public abstract DriverInvoker createInvoker(DriverMission mission);

	/**
	 * 返回DRIVER站点启动器
	 * @return DRIVER站点实例
	 */
	public DriverLauncher getLauncher() {
		return DriverLauncher.getInstance();
	}

	/**
	 * 判断是驱动程序站点
	 * @return 返回真或者假
	 */
	public boolean isDriver() {
		DriverLauncher launcher = getLauncher();
		return launcher.isDriver();
	}

	/**
	 * 判断是字符控制台站点
	 * @return 返回真或者假
	 */
	public boolean isConsole() {
		DriverLauncher launcher = getLauncher();
		return launcher.isConsole();
	}

	/**
	 * 判断是图形终端站点
	 * @return 返回真或者假
	 */
	public boolean isTerminal() {
		DriverLauncher launcher = getLauncher();
		return launcher.isTerminal();
	}

	/**
	 * 返回资源管理池
	 * @return StaffOnFrontPool实例
	 */
	public StaffOnFrontPool getStaffPool() {
		return getLauncher().getStaffPool();
	}

	/**
	 * 判断是被授权数据表
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean isPassiveTable(Space space) {
		StaffOnFrontPool instance = getStaffPool();
		return instance.isPassiveTable(space);
	}

	/**
	 * 根据数据表名，查找被授权表的授权人
	 * @param space 数据表名
	 * @return Siger签名
	 */
	public Siger findAuthorizer(Space space) {
		StaffOnFrontPool instance = getStaffPool();
		return instance.findAuthorizer(space);
	}

	/**
	 * 返回DRIVER调用器管理池
	 * @return DriverInvokerPool实例
	 */
	public DriverInvokerPool getInvokerPool() {
		return (DriverInvokerPool) getLauncher().getInvokerPool();
	}

	/**
	 * 返回DRIVER命令管理池
	 * @return DriverCommandPool实例
	 */
	public DriverCommandPool getCommandPool() {
		return (DriverCommandPool) getLauncher().getCommandPool();
	}

	/**
	 * 判断是管理员
	 * @return 返回真或者假
	 */
	public boolean isAdministrator() {
		DriverLauncher launcher = getLauncher();
		return launcher.isAdministrator();
	}

	/**
	 * 判断是普通注册用户
	 * @return 返回真或者假
	 */
	public boolean isUser() {
		DriverLauncher launcher = getLauncher();
		return launcher.isUser();
	}

	/**
	 * 返回当前账号的注册用户名
	 * @return SHA256散列码的用户签名
	 */
	public Siger getUsername() {
		DriverLauncher launcher = getLauncher();
		return launcher.getUsername();
	}

	/**
	 * 判断系统默认命令是流处理模式（内存计算模式）
	 * @return 返回真或者假
	 */
	public boolean isStream() {
		DriverLauncher launcher = getLauncher();
		return launcher.isMemory();
	}

	/**
	 * 返回用户定义的命令超时
	 * @return 超时时间，-1是无限制。
	 */
	public long getCommandTimeout() {
		DriverLauncher launcher = getLauncher();
		return launcher.getCommandTimeout();
	}

	/**
	 * 输出故障信息
	 * @param no 故障编号
	 */
	protected MissionException createFaultX(int no) {
		DriverLauncher launcher = getLauncher();
		String text = launcher.fault(no);
		return new MissionException(text);
	}

	/**
	 * 显示经过格式化处理的故障信息
	 * @param no 故障编号
	 * @param params 被格式字符串引用的参数
	 */
	protected MissionException createFaultX(int no, Object... params) {
		DriverLauncher launcher = getLauncher();
		String text = launcher.fault(no, params);
		return new MissionException(text);
	}

	/**
	 * 输出错误消息
	 * @param message
	 */
	protected MissionException createFault(String message) {
		return new MissionException(message);
	}

	/**
	 * 格式化和输出错误消息
	 * @param format
	 * @param args
	 */
	protected MissionException createFault(String format, Object... args) {
		String message = String.format(format, args);
		return createFault(message);
	}
	
}
