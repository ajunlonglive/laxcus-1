/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.tub.invoker;

import java.io.*;

import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.product.*;
import com.laxcus.front.*;
import com.laxcus.front.invoker.*;
import com.laxcus.front.tub.mission.*;
import com.laxcus.mission.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 边缘容器调用器。
 * 开放给边缘容器，提供面向云端的分布计算请求。
 * 
 * @author scott.liang
 * @version 1.0 6/23/2019
 * @since laxcus 1.0
 */
public abstract class TubInvoker extends FrontInvoker {

	/** 边缘容器任务 **/
	private TubMission mission;
	
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
	 * 构造边缘容器异步调用器，指定任务
	 * @param mission 默认任务
	 */
	protected TubInvoker(TubMission mission) {
		super(mission.getCommand());
		// 保存句柄
		setMission(mission);
	}

	/**
	 * 设置任务实例，不允许空指针
	 * @param e 任务实例
	 * @throws NullPointerException 空指针异常
	 */
	private void setMission(TubMission e) {
		Laxkit.nullabled(e);
		mission = e;
	}

	/**
	 * 返回任务实例
	 * @return TubMission
	 */
	public TubMission getMission() {
		return mission;
	}

	/**
	 * 将".echo"后缀文件，改为".tub"后缀
	 * @param file ECHO文件
	 * @return tub文件
	 */
	protected File rename(File file) {
		final String suffix = ".tub";
		String filename = file.getAbsolutePath();
		int last = filename.lastIndexOf(EchoArchive.FILE_SUFFIX);
		File tub = null;
		if (last == -1) {
			tub = new File(filename + suffix);
		} else {
			String prefix = filename.substring(0, last);
			tub = new File(String.format("%s%s", prefix, suffix));
		}
		// 改名
		boolean success = file.renameTo(tub);
		if (!success) {
			faultX(FaultTip.FAILED_X, tub);
		}

		return tub;
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

	/**
	 * 输出错误消息
	 * @param e
	 */
	protected void fault(Throwable e) {
		setFault(new MissionException(e));
	}

	/**
	 * 投递命令到目标站点（GATE/CALL任意一种）
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
	 * 投递命令到目标站点（GATE/CALL任意一种）
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