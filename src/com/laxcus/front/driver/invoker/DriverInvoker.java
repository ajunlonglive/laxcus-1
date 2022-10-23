/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import java.io.*;

import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.product.*;
import com.laxcus.front.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.front.driver.pool.*;
import com.laxcus.front.invoker.*;
import com.laxcus.mission.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * FRONT.DRIVER异步调用器 <br>
 * 
 * 根据FRONT的驱动程序的操作模式，提供相关的异步操作服务和接口。
 * 
 * @author scott.liang
 * @version 1.1 10/08/2013
 * @since laxcus 1.0
 */
public abstract class DriverInvoker extends FrontInvoker {

	/** 任务实例 **/
	private DriverMission mission;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		mission = null;
	}

	/**
	 * 构造驱动程序异步调用器，指定任务
	 * @param mission 默认任务
	 */
	protected DriverInvoker(DriverMission mission) {
		super(mission.getCommand());
		// 保存句柄
		setMission(mission);
	}

	/**
	 * 设置任务实例，不允许空指针
	 * @param e 任务实例
	 * @throws NullPointerException 空指针异常
	 */
	private void setMission(DriverMission e) {
		Laxkit.nullabled(e);
		mission = e;
	}

	/**
	 * 返回任务实例
	 * @return DriverMission
	 */
	public DriverMission getMission() {
		return mission;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.invoker.FrontInvoker#getInvokerPool()
	 */
	@Override
	public DriverInvokerPool getInvokerPool() {
		return (DriverInvokerPool) super.getInvokerPool();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.invoker.FrontInvoker#getCommandPool()
	 */
	@Override
	public DriverCommandPool getCommandPool() {
		return (DriverCommandPool) super.getCommandPool();
	}
	
	/**
	 * 将".echo"后缀文件，改为".driver"后缀
	 * @param file ECHO文件
	 * @return DRIVER文件
	 */
	protected File rename(File file) {
		final String suffix = ".driver";
		String filename = file.getAbsolutePath();
		int last = filename.lastIndexOf(EchoArchive.FILE_SUFFIX);
		File driver = null;
		if (last == -1) {
			driver = new File(filename + suffix);
		} else {
			String prefix = filename.substring(0, last);
			driver = new File(String.format("%s%s", prefix, suffix));
		}
		// 改名
		boolean success = file.renameTo(driver);
		if (!success) {
			faultX(FaultTip.FAILED_X, driver);
		}

		return driver;
	}

	/**
	 * 输出故障信息
	 * @param no 故障编号
	 */
	protected void faultX(int no) {
		FrontLauncher launcher = getLauncher();
		String text = launcher.fault(no);
		setFault(new MissionException(text));
	}

	/**
	 * 显示经过格式化处理的故障信息
	 * @param no 故障编号
	 * @param params 被格式字符串引用的参数
	 */
	protected void faultX(int no, Object... params) {
		FrontLauncher launcher = getLauncher();
		String text = launcher.fault(no, params);
		setFault(new MissionException(text));
	}

	/**
	 * 输出错误消息
	 * @param message
	 */
	protected void fault(String message) {
		setFault(new MissionException(message));
	}

	/**
	 * 格式化和输出错误消息
	 * @param format
	 * @param args
	 */
	protected void fault(String format, Object... args) {
		String message = String.format(format, args);
		fault(message);
	}

	/**
	 * 设置驱动任务异常
	 * @param e 异常实例
	 */
	protected void setFault(MissionException e) {
		Laxkit.nullabled(e);
		mission.setException(e);
	}

	/**
	 * 设置驱动任务异常
	 * @param message 异常消息
	 */
	protected void setFault(String message) {
		MissionException e = new MissionException(message);
		setFault(e);
	}

	/**
	 * 设置驱动任务处理结果
	 * @param e 驱动任务处理结果
	 */
	protected void setResult(MissionResult e) {
		Laxkit.nullabled(e);
		mission.setResult(e);
	}
	
	/**
	 * 设置驱动任务报告
	 * @param product 回显报告
	 */
	protected void setProduct(EchoProduct product) {
		MissionProductResult result = new MissionProductResult(product);
		setResult(result);
	}
	
	/**
	 * 设置对象结果
	 * @param object 对象实例
	 */
	protected void setObject(Object object) {
		MissionObjectResult result = new MissionObjectResult(object);
		setResult(result);
	}
	
//	/**
//	 * 设置驱动任务处理结果
//	 * @param e 驱动任务处理结果
//	 */
//	protected void setResult(DriverResult e) {
//		Laxkit.nullabled(e);
//		mission.setResult(e);
//	}
	
//	/**
//	 * 设置驱动任务报告
//	 * @param product 回显报告
//	 */
//	protected void setProduct(EchoProduct product) {
//		DriverProductResult result = new DriverProductResult(product);
//		setResult(result);
//	}
	
//	/**
//	 * 设置对象结果
//	 * @param object 对象实例
//	 */
//	protected void setObject(Object object) {
//		DriverObjectResult result = new DriverObjectResult(object);
//		setResult(result);
//	}

	/**
	 * 输出错误消息
	 * @param e
	 */
	protected void fault(Throwable e) {
		setFault(new MissionException(e));
	}

	/**
	 * 投递命令到目标站点
	 * @param hub 目标站点
	 * @param cmd 命令
	 * @param ondisk 异步应答数据写入磁盘
	 * @return 成功返回真，否则假
	 */
	protected boolean fireToHub(Node hub, Command cmd, boolean ondisk) {
		CommandItem item = new CommandItem(hub, cmd);
		// 提交命令
		boolean success = completeTo(item);
		// 不成功，通知驱动任务
		if (!success) {
			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}
		return success;
	}

	/**
	 * 投递命令到目标站点
	 * @param hub 目标站点
	 * @param cmd 命令
	 * @return 成功返回真，否则假
	 */
	protected boolean fireToHub(Node hub, Command cmd) {
		return fireToHub(hub, cmd, isStream());
	}

	/**
	 * 投递默认的命令到注册站点（GATE）
	 * 
	 * @param ondisk 异步应答数据写入磁盘，或者否
	 * @return 投递成功返回真，否则假。提前之前，首先在本地建立回显缓存，然后提交命令到服务器
	 */
	protected boolean fireToHub(boolean ondisk) {
		Node hub = getHub();
		Command cmd = getCommand();
		return fireToHub(hub, cmd, ondisk);
	}

	/**
	 * 投递默认的命令到注册站点（GATE/TOP），回显数据默认写入内存。
	 * @return 投递成功返回真，否则假。
	 */
	protected boolean fireToHub() {
		boolean memory = isStream();
		return fireToHub(!memory);
	}
}