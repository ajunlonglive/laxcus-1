/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.local;

import java.io.*;
import java.util.*;

import com.laxcus.task.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.display.show.*;

/**
 * 本地阶段任务。<br><br>
 * 
 * 本地阶段任务被部署和运行在FRONT站点上，是数据构建的最后一环，子类包括PUT/END/NEAR任务 <br>
 * 负责把数据显示在FRONT站点的屏幕上。<br>
 * 
 * @author scott.liang
 * @version 1.1 12/12/2012
 * @since laxcus 1.0
 */
public abstract class LocalTask extends AccessTask {

	/** 任务显示器 **/
	private MeetDisplay display;

	/** 设置接口代理 **/
	private TailTrustor trustor;

	/**
	 * 构造默认的数据构建END阶段任务实例
	 */
	protected LocalTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#destroy()
	 */
	@Override
	public void destroy() {
		display = null;
		trustor = null;
		super.destroy();
	}

	/**
	 * 判断当前是简体中文环境
	 * @return 返回真或者假
	 */
	protected boolean isSimplfiedChinese() {
		Locale local = Locale.getDefault();
		String language = local.getLanguage();
		String country = local.getCountry();

		// 判断一致
		boolean match = language.equalsIgnoreCase(Locale.SIMPLIFIED_CHINESE.getLanguage());
		if (match) {
			match = country.equalsIgnoreCase(Locale.SIMPLIFIED_CHINESE.getCountry());
		}
		return match;
	}

	/**
	 * 设置交互显示器，适用所有图形界面
	 * @param e MeetDisplay实例
	 */
	public void setDisplay(MeetDisplay e) {
		display = e;
	}

	/**
	 * 设置任务显示器
	 * @return MeetDisplay实例
	 */
	public MeetDisplay getDisplay() {
		return display;
	}

	/**
	 * 设置FRONT尾段代理
	 * @param e
	 */
	protected void setTailTrustor(TailTrustor e) {
		trustor = e;
	}

	/**
	 * 返回FRONT尾段代理
	 * @return FRONT尾段代理
	 */
	protected TailTrustor getTailTrustor() {
		return trustor;
	}
	
	/**
	 * 判断是图形桌面
	 * @return 返回真或者假
	 */
	protected boolean isDesktop() {
		return trustor != null && trustor.isDesktop();
	}

	/**
	 * 判断是图形终端界面
	 * @return 返回真或者假
	 */
	protected boolean isTerminal() {
		return trustor != null && trustor.isTerminal();
	}

	/**
	 * 判断是控制台
	 * @return 返回真或者假
	 */
	protected boolean isConsole() {
		return trustor != null && trustor.isConsole();
	}

	/**
	 * 判断是边缘
	 * @return 返回真或者假
	 */
	protected boolean isEdge() {
		return trustor != null && trustor.isEdge();
	}

	/**
	 * 判断是驱动程序
	 * @return 返回真或者假
	 */
	protected boolean isDriver() {
		return trustor != null && trustor.isDriver();
	}

	/**
	 * 判断是应用软件
	 * @return 返回真或者假
	 */
	protected boolean isApplication() {
		return trustor != null && trustor.isApplication();
	}

	/**
	 * 设置状态文本
	 * 
	 * @param text
	 */
	protected void setStatusText(String text) {
		if (display != null) {
			display.setStatusText(text);
		}
	}

	/**
	 * 向窗口投递消息
	 * @param text 字符文本
	 */
	protected void message(String text) {
		if (display != null) {
			display.message(text);
		}
	}

	/**
	 * 向窗口投递警告
	 * @param text 字符文本
	 */
	protected void warning(String text) {
		if (display != null) {
			display.warning(text);
		}
	}

	/**
	 * 向窗口投递错误
	 * @param text 字符文本
	 */
	protected void fault(String text) {
		if (display != null) {
			display.fault(text);
		}
	}

	/**
	 * 显示普通信息
	 * @param format 格式化语句
	 * @param args 格式化参数
	 */
	protected void message(String format, Object... args) {
		String text = String.format(format, args);
		message(text);
	}

	/**
	 * 显示警告信息
	 * @param format 格式化语句
	 * @param args 格式化参数
	 */
	protected void warning(String format, Object... args) {
		String text = String.format(format, args);
		warning(text);
	}

	/**
	 * 显示故障信息
	 * @param format 格式化语句
	 * @param args 格式化参数
	 */
	protected void fault(String format, Object... args) {
		String text = String.format(format, args);
		fault(text);
	}

	/**
	 * 显示表格标题
	 * @param title 标题
	 */
	public void setShowTitle(ShowTitle title) {
		if (display != null) {
			display.setShowTitle(title);
		}
	}

	/**
	 * 增加一行记录
	 * @param item 表记录
	 */
	public void addShowItem(ShowItem item) {
		if (display != null) {
			display.addShowItem(item);
		}
	}

	/**
	 * 数据写入磁盘
	 * @param filename 磁盘文件名
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 成功返回真，否则假
	 */
	public boolean writeTo(String filename, byte[] b, int off, int len) {
		File file = new File(filename);
		boolean success = false;
		try {
			FileOutputStream writer = new FileOutputStream(file);
			writer.write(b, off, len);
			writer.close();
			success = true;
		} catch (IOException e) {
			fault(e.getMessage());
		}
		return success;
	}

	/**
	 * 将磁盘文件中的数据显示出来
	 * @param file 磁盘文件
	 * @return 返回处理的数据长度
	 * @throws TaskException
	 */
	public long display(File file) throws TaskException {
		return display(new File[] { file });
	}

	/**
	 * 显示、保存计算的的处理结果
	 * @param b  计算的的处理结果数组
	 * @param off 数组开始下标
	 * @param len 数据有效长度
	 * @return 返回处理数据的长度
	 * @throws TaskException
	 */
	public abstract long display(byte[] b, int off, int len) 
		throws TaskException;

	/**
	 * 将磁盘文件中的数据显示出来
	 * @param files 磁盘文件
	 * @return 返回处理数据的长度
	 * @throws TaskException
	 */
	public abstract long display(File[] files) throws TaskException;

}