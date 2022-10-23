/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.front.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.sound.*;
import com.laxcus.util.tip.*;

/**
 * 显示界面调用器。<br>
 * 用于Console、Terminal、Desktop三种界面上。<br><br>
 * 
 * 桌面调用器播放声音有两道阀门：<br>
 * 1. 在falut/warning方法中的播放声音开关 <br>
 * 2. 在Command命令中的开关 <br><br>
 * 
 * @author scott.liang
 * @version 1.0 5/31/2021
 * @since laxcus 1.0
 */
public abstract class FrontDisplayInvoker extends FrontInvoker {

	/** 静态显示接口。显示命令操作过程中产生信息 **/
	private static MeetDisplay defaultDisplay;

	/**
	 * 设置静态显示接口。在进程启动时设置
	 * @param e MeetDisplay实例
	 */
	public static void setDefaultDisplay(MeetDisplay e) {
		FrontDisplayInvoker.defaultDisplay = e;
	}

	/**
	 * 返回静态显示接口
	 * @return MeetDisplay实例
	 */
	public static MeetDisplay getDefaultDisplay() {
		return FrontDisplayInvoker.defaultDisplay;
	}

	/**
	 * 返回静态的异步结果监听器
	 * @return 返回ProductListener实例，如果空指针
	 */
	public static ProductListener getDefaultProductListener() {
		if (FrontDisplayInvoker.defaultDisplay != null) {
			return FrontDisplayInvoker.defaultDisplay.getProductListener();
		}
		return null;
	}
	
	/**
	 * 判断有静态的异步结果监听器
	 * @return 返回真或者假
	 */
	public static boolean hasDefaultProductListener() {
		return getDefaultProductListener() != null;
	}
	
	/** 临时显示接口，在生成调用器时设置 **/
	private MeetDisplay display;

	/**
	 * 构造默认的显示调用器
	 */
	protected FrontDisplayInvoker() {
		super();
	}

	/**
	 * 构造显示调用器
	 * @param cmd 命令
	 */
	protected FrontDisplayInvoker(Command cmd) {
		super(cmd);
	}

	/**
	 * 设置静态的临时显示接口。在进程启动时设置
	 * @param e MeetDisplay实例
	 */
	public void setDisplay(MeetDisplay e) {
		display = e;
	}

	/**
	 * 返回静态的临时显示接口
	 * @return MeetDisplay实例
	 */
	public MeetDisplay getDisplay() {
		return display;
	}
	
	/**
	 * 返回异步处理监听器（临时的）
	 * @return ProductListener实例，或者空指针
	 */
	public ProductListener getProductListener() {
		if (display != null) {
			return display.getProductListener();
		}
		return null;
	}

	/**
	 * 判断有异步处理监听器
	 * @return
	 */
	public boolean hasProductListener() {
		return getProductListener() != null;
	}
	
	/**
	 * 设置播放声音
	 * @param b
	 */
	protected void setSound(boolean b) {
		Command cmd = getCommand();
		if (cmd != null) {
			cmd.setSound(b);
		}
	}

	/**
	 * 判断播放声音
	 * @return 返回真或者假
	 */
	protected boolean isSound() {
		return getCommand().isSound();
	}
	
	/**
	 * 生成一个标准格式时间
	 * @param time 以毫秒为单位的时间
	 * @return 字符串描述
	 */
	protected String doStyleTime(long time) {
		String input = getLauncher().message(MessageTip.FORMAT_TIME_X);
		RuntimeFormat rf = new RuntimeFormat();
		return rf.format(input, time);
	}
	
	/**
	 * 显示许可信息，由用户决定是否接受协议条款
	 * 
	 * @param content 用户的许可协议文本
	 * @return 接受返回真，否则假
	 */
	protected boolean approveLicence(String content) {
		// 如果临时显示接口有效，用临时的显示接口显示信息
		if (display != null && display.isUsabled()) {
			return display.approveLicence(content);
		}
		// 如果静态显示接口有效，用静态的显示接口显示信息
		else if (defaultDisplay != null && defaultDisplay.isUsabled()) {
			return defaultDisplay.approveLicence(content);
		} else {
			Logger.fatal(this, "approveLicence", "task display is null-pointer!");
			return false;
		}
	}

	/**
	 * 向窗口投递消息
	 * @param text 消息文本
	 * @param paly 播放声音
	 * @param 播放声音
	 */
	protected void message(String text, boolean sound) {
		// 如果临时显示接口有效，用临时的显示接口显示信息
		if (display != null) {
			if (display.isUsabled()) {
				display.message(text, sound);
			}
		}
		// 如果静态显示接口有效，用静态的显示接口显示信息
		else if (defaultDisplay != null) {
			if (defaultDisplay.isUsabled()) {
				defaultDisplay.message(text, sound);
			}
		}
		// 以上不成立，只处理声音
		else {
			if (sound && (isDesktop() || isTerminal())) {
				playMessage();
			}
		}
	}

	/**
	 * 向窗口投递消息
	 * @param text 消息文本
	 */
	protected void message(String text) {
		message(text, isSound());
	}
	
	/**
	 * 向窗口投递警告
	 * @param text 警告文本
	 * @param focus 获得焦点
	 * @param sound 播放声音
	 */
	protected void warning(String text, boolean sound) {
		if (display != null) {
			if (display.isUsabled()) {
				display.warning(text, sound);
			}
		} else if (defaultDisplay != null) {
			if (defaultDisplay.isUsabled()) {
				defaultDisplay.warning(text, sound);
			}
		} else {
			// 以上不成立，只处理警告声音
			if (sound && (isDesktop() || isTerminal())) {
				playWarning();
			}
		}
	}

	/**
	 * 向窗口投递警告
	 * @param text 警告文本
	 */
	protected void warning(String text) {
		warning(text, isSound());
	}
	
	/**
	 * 向窗口投递错误
	 * @param text 错误文本
	 * @param focus 获得焦点
	 */
	protected void fault(String text, boolean sound) {
		if (display != null) {
			if (display.isUsabled()) {
				display.fault(text, sound);
			}
		} else if (defaultDisplay != null) {
			if (defaultDisplay.isUsabled()) {
				defaultDisplay.fault(text, sound);
			}
		} else {
			// 以上不成立，且是图形界面时，播放错误声音
			if (sound && (isDesktop() || isTerminal())) {
				playError();
			}
		}
	}

	/**
	 * 向窗口投递错误
	 * @param text 错误文本
	 */
	protected void fault(String text) {
		fault(text, isSound());
	}
	
	/**
	 * 根据消息编号和当前语言配置，在窗口显示对应的消息提示
	 * 
	 * @param sound 播放声音
	 * @param no 提示编号
	 */
	protected void messageX(boolean sound, int no) {
		FrontLauncher launcher = getLauncher();
		String text = launcher.message(no);
		message(text, sound);
	}

	/**
	 * 根据消息编号和当前语言配置，在窗口显示对应的消息提示
	 * @param no 提示编号
	 */
	protected void messageX(int no) {
		messageX(true, no);
	}

	/**
	 * 根据消息编号和当前语言配置，显示经过格式化处理的消息提示
	 * 
	 * @param sound 播放声音
	 * @param no 提示编号
	 * @param params 被格式字符串引用的参数
	 */
	protected void messageX(boolean sound, int no, Object... params) {
		FrontLauncher launcher = getLauncher();
		String text = launcher.message(no, params);
		message(text, sound);
	}

	/**
	 * 根据消息编号和当前语言配置，显示经过格式化处理的消息提示
	 * @param no 提示编号
	 * @param params 被格式字符串引用的参数
	 */
	protected void messageX(int no, Object... params) {
		messageX(isSound(), no, params);
	}

	/**
	 * 根据警告编号和当前语言配置，在窗口显示对应的警告提示
	 * 
	 * @param sound 获得焦点
	 * @param no 提示编号
	 */
	protected void warningX(boolean sound, int no) {
		FrontLauncher launcher = getLauncher();
		String text = launcher.warning(no);
		warning(text, sound);
	}

	/**
	 * 根据警告编号和当前语言配置，显示经过格式化处理的警告提示
	 * 
	 * @param sound 播放声音
	 * @param no 提示编号
	 * @param params 被格式字符串引用的参数
	 */
	protected void warningX(boolean sound, int no, Object... params) {
		FrontLauncher launcher = getLauncher();
		String text = launcher.warning(no, params);
		warning(text, sound);
	}

	/**
	 * 根据警告编号和当前语言配置，显示经过格式化处理的警告提示
	 * @param no 提示编号
	 * @param params 被格式字符串引用的参数
	 */
	protected void warningX(int no, Object... params) {
		warningX(isSound(), no, params);
	}

	/**
	 * 根据警告编号和当前语言配置，在窗口显示对应的警告提示
	 * @param no 提示编号
	 */
	protected void warningX(int no) {
		warningX(isSound(), no);
	}

	/**
	 * 根据故障编号和当前语言配置，在窗口显示对应的故障提示
	 * 
	 * @param sound 播放声音
	 * @param no 提示编号
	 */
	protected void faultX(boolean sound, int no) {
		FrontLauncher launcher = getLauncher();
		String text = launcher.fault(no);
		fault(text, sound);
	}

	/**
	 * 根据故障编号和当前语言配置，在窗口显示对应的故障提示
	 * 
	 * @param no 提示编号
	 */
	protected void faultX(int no) {
		faultX(isSound(), no);
	}

	/**
	 * 根据故障编号和当前语言配置，显示经过格式化处理的故障提示
	 * 
	 * @param sound 获得焦点
	 * @param no 提示编号
	 * @param params 被格式字符串引用的参数
	 */
	protected void faultX(boolean sound, int no, Object... params) {
		FrontLauncher launcher = getLauncher();
		String text = launcher.fault(no, params);
		fault(text, sound);
	}

	/**
	 * 根据故障编号和当前语言配置，显示经过格式化处理的故障提示
	 * @param no 提示编号
	 * @param params 被格式字符串引用的参数
	 */
	protected void faultX(int no, Object... params) {
		faultX(isSound(), no, params);
	}

	/**
	 * 导出错误堆栈
	 * @param e Throwable实例
	 * @return 返回错误堆栈字符串
	 */
	protected String export(Throwable e) {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
		PrintStream output = new PrintStream(buff, true);
		e.printStackTrace(output);
		byte[] b = buff.toByteArray();
		return new String(b, 0, b.length);
	}

	/**
	 * 显示故障信息
	 * @param e Throwable实例
	 */
	protected void fault(Throwable e) {
		fault(export(e));
	}

	/** 标题 **/
	private ShowTitle title;

	/** 表单元 **/
	private ArrayList<ShowItem> items = new ArrayList<ShowItem>();

	/**
	 * 一次性输出标题和表单元
	 */
	protected void flushTable() {
		if (display != null && display.isUsabled()) {
			display.showTable(title, items);
		} else if (defaultDisplay != null && defaultDisplay.isUsabled()) {
			defaultDisplay.showTable(title, items);
		}
	}

	/**
	 * 设置表标题。在此之前，旧标题和全部行将被删除。
	 * @param e ShowTitle实例
	 */
	protected void setShowTitle(ShowTitle e) {
		title = e;
	}

	/**
	 * 增加表记录。在增加表记录前，必须调用“setShowTitle”方法。
	 * @param e ShowItem实例
	 */
	protected void addShowItem(ShowItem e) {
		items.add(e);
	}

	/**
	 * 返回标题单元数目
	 * @return 标题单元数目
	 */
	protected int getTitleCellCount() {
		if (display != null && display.isUsabled()) {
			return display.getTitleCellCount();
		} else if (defaultDisplay != null && defaultDisplay.isUsabled()) {
			return defaultDisplay.getTitleCellCount();
		}
		return 0;
	}

	/**
	 * 设置状态栏消息
	 * @param text 文本
	 */
	protected void setStatusText(String text) {
		if (display != null && display.isUsabled()) {
			display.setStatusText(text);
		} else if (defaultDisplay != null && defaultDisplay.isUsabled()) {
			defaultDisplay.setStatusText(text);
		}
	}

	/**
	 * 取出回显码的错误码，打印错误文本
	 * @param code EchoCode实例
	 */
	protected void printFault(EchoCode code) {
		FrontLauncher launcher = getLauncher();
		String text = launcher.echo(code.getMinor());
		text = String.format("%s (%d,%d)", text, code.getMajor(), code.getMinor());
		fault(text);
	}

	/**
	 * 打印错误信息
	 */
	protected void printFault() {
		int index = findEchoKey(0);
		EchoBuffer bf = findBuffer(index);
		if (bf == null) {
			faultX(FaultTip.FAILED_X, getCommand());
			return;
		}
		// 找到回显报头
		EchoHead head = bf.getHead(); 
		EchoCode code = head.getCode();
		if (code.isSuccessful()) {
			return;
		}
		printFault(code);
		// 服务器端返回的故障提示
		EchoHelp help = head.getHelp();
		if (help != null) {
			fault(help.toString());
		}
	}

	/**
	 * 播放消息声音
	 */
	protected void playMessage() {
		if (isSound()) {
			getLauncher().playSound(SoundTag.MESSAGE);
		}
	}

	/**
	 * 播放警告声音
	 */
	protected void playWarning() {
		if (isSound()) {
			getLauncher().playSound(SoundTag.WARNING);
		}
	}

	/**
	 * 播放错误声音
	 */
	protected void playError() {
		if (isSound()) {
			getLauncher().playSound(SoundTag.ERROR);
		}
	}
	
}