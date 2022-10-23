/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.awt.*;

import javax.swing.*;

import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.*;
import com.laxcus.front.desktop.*;
import com.laxcus.front.desktop.pool.*;
import com.laxcus.front.invoker.*;
import com.laxcus.site.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.local.*;
import com.laxcus.util.tip.*;

/**
 * FRONT站点的交互操作异步调用器。<br><br>
 * 
 * DesktopInvoker应用于终端、控制台两种模式。基于用户的可视化操作和调用。<br>
 * 
 * DesktopInvoker操作由用户产生，每次关联一个异步命令，注册到DesktopInvokerPool <br><br>
 * 
 * @author scott.liang
 * @version 1.5 7/19/2015
 * @since laxcus 1.0
 */
public abstract class DesktopInvoker extends FrontDisplayInvoker {

	/** DesktopInvoker调用器界面配置加载器 **/
	private static SurfaceLoader loader = new SurfaceLoader();

	/**
	 * 加载MEET调用器的多语言配置文件
	 */
	private static void loadInvokerSurface() {
		LocalSelector selector = new LocalSelector("conf/desktop/invoker/config.xml");
		String path = selector.findPath("invoker");
		loader.load(path);
	}

	static {
		loadInvokerSurface();
	}

	/**
	 * 根据XML路径，取出标签中的属性内容
	 * @param xmlPath XML路径
	 * @return 返回标签属性信息
	 */
	public static String getXMLAttribute(String xmlPath) {
		return loader.getAttribute(xmlPath);
	}

	/**
	 * 根据XML路径，取出标签中的文本内容
	 * @param xmlPath XML路径
	 * @return 返回文本块信息
	 */
	public static String getXMLContent(String xmlPath) {
		return loader.getContent(xmlPath);
	}

//	/** 成功/失败状态图标 **/
//	private static Icon successIcon;
//
//	private static Icon failedIcon;

	//	/** 声音 **/
	//	private boolean sound1;

	/**
	 * 构造默认的FRONT DESKTOP调用器
	 */
	protected DesktopInvoker() {
		super();
		//		// 清除状态栏信息
		//		if (isTerminal()) {
		//			setStatusText("");
		//		}
		//		sound = true; // 默认是真
	}

	/**
	 * 构造交互操作异步调用器调用器，指定命令
	 * @param cmd 异步命令
	 */
	protected DesktopInvoker(Command cmd) {
		super(cmd);
		//		// 清除状态栏信息
		//		if (isTerminal()) {
		//			setStatusText("");
		//		}
		//		sound = true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.invoker.FrontInvoker#getLauncher()
	 */
	@Override
	public DesktopLauncher getLauncher() {
		return (DesktopLauncher) super.getLauncher();
	}

	/**
	 * 返回桌面调用器管理池
	 * @return DesktopInvokerPool实例
	 */
	public DesktopInvokerPool getInvokerPool() {
		return (DesktopInvokerPool) super.getInvokerPool();
	}

	/**
	 * 返回桌面命令管理池
	 * @return DesktopCommandPool实例
	 */
	public DesktopCommandPool getCommandPool() {
		return (DesktopCommandPool) super.getCommandPool();
	}

	/**
	 * 调用当前命令管理池的“press”方法，投递一个新的命令
	 * @param next 转发命令
	 * @return CommandPool接受返回真，否则假
	 */
	protected boolean press(Command next, MeetDisplay display) {
		// 设置命令超时和当前用户的签名
		next.setTimeout(getCommandTimeout());
		next.setIssuer(getIssuer());

		return getCommandPool().press(next, display);
	}
	
//	/**
//	 * 播放消息提示声音
//	 */
//	protected void playMessage() {
//		if (isSound()) {
//			getLauncher().playSound(SoundTag.MESSAGE);
//		}
//	}
//
//	/**
//	 * 播放警告声音
//	 */
//	protected void playWarning() {
//		if (isSound()) {
//			getLauncher().playSound(SoundTag.WARNING);
//		}
//	}
//
//	/**
//	 * 播放错误声音
//	 */
//	protected void playError() {
//		if (isSound()) {
//			getLauncher().playSound(SoundTag.ERROR);
//		}
//	}


	//	/** 前端显示器。显示命令操作过程中产生信息 **/
	//	private MeetDisplay display;
	//
	//	/**
	//	 * 设置静态的前端显示器。在进程启动时设置
	//	 * @param e MeetDisplay实例
	//	 */
	//	public void setDisplay(MeetDisplay e) {
	//		display = e;
	//	}
	//
	//	/**
	//	 * 返回静态的前端显示器
	//	 * @return MeetDisplay实例
	//	 */
	//	public MeetDisplay getDisplay() {
	//		return display;
	//	}

	//	/**
	//	 * 播发声音
	//	 * @param b 真或者假
	//	 */
	//	protected void setSound(boolean b) {
	//		sound = b;
	//	}
	//
	//	/**
	//	 * 判断播放声音
	//	 * @return 返回真或者假
	//	 */
	//	protected boolean isSound() {
	//		return sound;
	//	}

	/**
	 * 根据XML路径，取出属性段的标题参数。标题属性用关键字“title”。
	 * @param xmlRoot XML路径
	 * @return 返回标题参数
	 */
	protected String findXMLTitle(String xmlRoot) {
		// "title"是固定关键字，全小写格式
		String xmlTitle = String.format("%s/title", xmlRoot);
		return getXMLAttribute(xmlTitle);
	}

	/**
	 * 根据XML路径，取出属性段中的宽度参数。宽度参数用关键字“width”
	 * @param xmlRoot XML路径
	 * @return 返回宽度参数
	 */
	protected String findXMLWidth(String xmlRoot) {
		// "width"是固定关键字，全小写格式
		String xmlTitle = String.format("%s/width", xmlRoot);
		return getXMLAttribute(xmlTitle);
	}

	/**
	 * 根据XML路径，取出标签属性中的背景色。标签属性关键字是“background”
	 * @param xmlRoot XML根路径
	 * @param defaultColor 默认的颜色值
	 * @return 返回定义的颜色值，没有是默认值
	 */
	protected Color findXMLBackground(String xmlRoot, Color defaultColor) {
		String xmlTitle = String.format("%s/background", xmlRoot);
		String value = getXMLAttribute(xmlTitle);

		// 如果是ESL颜色，返回这个值
		if (ConfigParser.isESLColor(value)) {
			return ConfigParser.splitESLColor(value, defaultColor);
		}
		// 如果是数字，转成颜色值；不是，去颜色模板中取颜色
		else if (ConfigParser.isInteger(value)) {
			int rgb = ConfigParser.splitInteger(value, 0);
			return new Color(rgb);
		} else {
			return ColorTemplate.findColor(value, defaultColor);
		}
	}

	/**
	 * 根据XML路径，取出属性段中的标题参数前景色。标题属性用关键字“foreground”。
	 * @param xmlRoot XML根路径（不包括标签）
	 * @param defaultColor 默认的颜色值
	 * @return 返回定义的颜色值，没有是默认值
	 */
	protected Color findXMLForeground(String xmlRoot, Color defaultColor) {
		String xmlTitle = String.format("%s/foreground", xmlRoot);
		String value = getXMLAttribute(xmlTitle);

		// 如果是ESL颜色，返回这个值
		if (ConfigParser.isESLColor(value)) {
			return ConfigParser.splitESLColor(value, defaultColor);
		}
		// 如果不是数字，去系统的颜色模板中取颜色
		else if (ConfigParser.isInteger(value)) {
			int rgb = ConfigParser.splitInteger(value, 0);
			return new Color(rgb);
		} else {
			return ColorTemplate.findColor(value, defaultColor);
		}
	}

	/**
	 * 根据XML路径，取出属性段中的标题参数前景色。标题属性用关键字“foreground”。
	 * @param xmlRoot XML根路径（不包括标签）
	 * @return 返回定义的颜色值，没有是空指针
	 */
	protected Color findXMLForeground(String xmlRoot) {
		return findXMLForeground(xmlRoot, null);
	}

	/**
	 * 生成一个表格标题单元
	 * @param index 索引下标
	 * @param xmlRoot 标题的XML根路径
	 * @param defaultWidth 默认宽度
	 * @return 返回ShowTitleCell实例
	 */
	protected ShowTitleCell createShowTitleCell(int index, String xmlRoot, int defaultWidth) {
		int width = (defaultWidth < 1 ? 120 : defaultWidth);

		String title = findXMLTitle(xmlRoot);
		// 如果在配置中定义宽度，提取宽度参数
		String w = findXMLWidth(xmlRoot);
		if (w != null && w.matches("^\\s*(?i)(\\d+)\\s*$")) {
			width = Integer.parseInt(w.trim());
		}

		// 生成显示列
		return new ShowTitleCell(index, title, width);
	}

	/**
	 * 生成一个表格标题单元
	 * @param index 索引
	 * @param xmlRoot 标题的XML根路径
	 * @return 返回ShowTitleCell实例
	 */
	protected ShowTitleCell createShowTitleCell(int index, String xmlRoot) {
		return createShowTitleCell(index, xmlRoot, -1);
	}

	/**
	 * 根据XML路径生成表格标题
	 * @param xmlRoots 标题的XML根路径数组
	 */
	protected void createShowTitle(String[] xmlRoots) {
		ShowTitle title = new ShowTitle();
		// 下标从0开始
		for (int index = 0; index < xmlRoots.length; index++) {
			ShowTitleCell cell = createShowTitleCell(index, xmlRoots[index]);
			title.add(cell);
		}
		setShowTitle(title);
	}

//	/**
//	 * 判断是字符控制台或者图形终端，生成字符单元或者图标单元
//	 * @param index 指定列编号
//	 * @param successful 成功标记
//	 * @return 返回字符或者图标的ShowItemCell实例
//	 */
//	protected ShowItemCell createConfirmTableCell(int index, boolean successful) {
//		// 如果是控制台，显示文本，否则是图像
//		if (isConsole()) {
//			String text = (successful ? getXMLContent("Confirm/OK") : getXMLContent("Confirm/ERROR"));
//			return new ShowStringCell(index, text);
//		}
//
//		// 取出图标
//		if (successIcon == null && failedIcon == null) {
////			ResourceLoader loader = new ResourceLoader("conf/desktop/image/invoker/");
////			successIcon = loader.findImage("success.png");
////			failedIcon = loader.findImage("failed.png");
//			
//			successIcon = UIManager.getIcon("Invoker.SuccessIcon");
//			failedIcon = UIManager.getIcon("Invoker.FailedIcon");
//		}
//		return new ShowImageCell(index, (successful ? successIcon : failedIcon));
//	}
	
//	/**
//	 * 按照要求生成图标单元
//	 * @param index 在标题行中的列下标
//	 * @param success 成功标记
//	 * @return 返回TableImageCell实例
//	 */
//	protected ShowImageCell createConfirmTableCell(int index, boolean success) {
//		Icon icon = (success ? UIManager.getIcon("Invoker.SuccessIcon")
//				: UIManager.getIcon("Invoker.FailedIcon"));
//		return new ShowImageCell(index, icon);
//	}

//	/**
//	 * 按照要求生成图标单元
//	 * @param index 在标题行中的列下标
//	 * @param success 成功标记
//	 * @return 返回TableImageCell实例
//	 */
//	protected ShowItemCell createConfirmTableCell(int index, boolean success) {
//		String text = (success ? getXMLContent("Confirm/OK"): getXMLContent("Confirm/ERROR"));
//		Color color = (success ? findXMLForeground("Confirm/OK") : findXMLForeground("Confirm/ERROR"));
//		return new ShowStringCell(index, text, color);
//	}

	/**
	 * 按照要求生成图标单元
	 * @param index 在标题行中的列下标
	 * @param success 成功标记
	 * @return 返回TableImageCell实例
	 */
	protected ShowImageCell createConfirmTableCell(int index, boolean success) {
		Icon icon = (success ? UIManager.getIcon("Invoker.SuccessIcon") : UIManager.getIcon("Invoker.FailedIcon"));
		String text = (success ? getXMLContent("Confirm/OK"): getXMLContent("Confirm/ERROR"));
		Color color = (success ? findXMLForeground("Confirm/OK") : findXMLForeground("Confirm/ERROR"));
		ShowImageCell cell = new ShowImageCell(index, icon, text);
		cell.setForeground(color);
		return cell;
	}
	
	/**
	 * 显示本次调用运行时间
	 */
	protected void printRuntime() {
		long time = getRunTime();
		if (time < 1) {
			return;
		}

		FrontLauncher launcher = getLauncher();
		String input = launcher.message(MessageTip.COMMAND_USEDTIME_X);
		RuntimeFormat e = new RuntimeFormat();
		String text = e.format(input, time);
		message(text);
	}

	/**
	 * 打印运行时间，包括成功和失败的数目
	 * @param successes 成功数目
	 * @param faults 失败数目
	 */
	protected void printRuntime(int successes, int faults) {
		long time = getRunTime();
		if (time < 1) {
			return;
		}

		FrontLauncher launcher = getLauncher();
		// 使用时间
		String input = launcher.message(MessageTip.COMMAND_USEDTIME_X);
		RuntimeFormat e = new RuntimeFormat();
		String text = e.format(input, time);
		// 成功和失败数目
		input = launcher.message(MessageTip.SUCCESS_FAULT_X);
		String suffix = String.format(input, successes, faults);

		message(text + suffix);
	}

	/**
	 * 格式化分布时间
	 * @param time 分布时间
	 * @return 返回格式化时间
	 */
	protected String formatDistributeTime(long time) {
		FrontLauncher launcher = getLauncher();
		String input = launcher.message(MessageTip.DISTRIBUTED_TIME_X);
		RuntimeFormat e = new RuntimeFormat();
		return e.format(input, time);
	}

	//	/**
	//	 * 生成一个标准格式时间
	//	 * @param time 以毫秒为单位的时间
	//	 * @return 字符串描述
	//	 */
	//	protected String doStyleTime(long time) {
	//		String input = getLauncher().message(MessageTip.FORMAT_TIME_X);
	//		RuntimeFormat rf = new RuntimeFormat();
	//		return rf.format(input, time);
	//	}
	//
	//	/**
	//	 * 向窗口投递消息
	//	 * @param text 消息文本
	//	 * @param focus 获得焦点
	//	 * @param paly 播放声音
	//	 * @param 播放声音
	//	 */
	//	protected void message(String text, boolean focus, boolean play) {
	//		// 如果仍然激活状态，显示它
	//		if (display != null && display.isUsabled()) {
	//			display.message(text, focus);
	//		}
	//		if (play) {
	//			playMessage();
	//		}
	//	}
	//
	//	/**
	//	 * 向窗口投递消息
	//	 * @param text 消息文本
	//	 * @param focus 获得焦点
	//	 */
	//	protected void message(String text, boolean focus) {
	//		message(text, focus, true);
	//	}
	//
	//	/**
	//	 * 向窗口投递消息
	//	 * @param text 消息文本
	//	 */
	//	protected void message(String text) {
	//		message(text, true);
	//	}
	//	
	//	/**
	//	 * 向窗口投递警告
	//	 * @param text 警告文本
	//	 * @param focus 获得焦点
	//	 * @param play 播放声音
	//	 */
	//	protected void warning(String text, boolean focus, boolean play) {
	//		if (display != null && display.isUsabled()) {
	//			display.warning(text, focus);
	//		}
	//		// 播放警告声音
	//		if (play) {
	//			playWarning();
	//		}
	//	}
	//
	//	/**
	//	 * 向窗口投递警告
	//	 * @param text 警告文本
	//	 * @param focus 获得焦点
	//	 */
	//	protected void warning(String text, boolean focus) {
	//		warning(text, focus, true);
	//	}
	//
	//	/**
	//	 * 向窗口投递警告
	//	 * @param text 警告文本
	//	 */
	//	protected void warning(String text) {
	//		warning(text, true);
	//	}
	//	
	//	/**
	//	 * 向窗口投递错误
	//	 * @param text 错误文本
	//	 * @param focus 获得焦点
	//	 */
	//	protected void fault(String text, boolean focus, boolean play) {
	//		if (display != null && display.isUsabled()) {
	//			display.fault(text, focus);
	//		}
	//		// 播放错误声音
	//		if (play) {
	//			playError();
	//		}
	//	}
	//
	//	/**
	//	 * 向窗口投递错误
	//	 * @param text 错误文本
	//	 * @param focus 获得焦点
	//	 */
	//	protected void fault(String text, boolean focus) {
	//		fault(text, focus, true);
	//	}
	//
	//	/**
	//	 * 向窗口投递错误
	//	 * @param text 错误文本
	//	 */
	//	protected void fault(String text) {
	//		fault(text, true);
	//	}

	//	/**
	//	 * 根据消息编号和当前语言配置，在窗口显示对应的消息提示
	//	 * 
	//	 * @param focus 获得焦点
	//	 * @param no 提示编号
	//	 */
	//	protected void messageX(boolean focus, int no) {
	//		FrontLauncher launcher = getLauncher();
	//		String text = launcher.message(no);
	//		message(text, focus);
	//	}
	//
	//	/**
	//	 * 根据消息编号和当前语言配置，在窗口显示对应的消息提示
	//	 * @param no 提示编号
	//	 */
	//	protected void messageX(int no) {
	//		messageX(true, no);
	//	}

	//	/**
	//	 * 根据消息编号和当前语言配置，显示经过格式化处理的消息提示
	//	 * 
	//	 * @param focus 获得焦点
	//	 * @param sound 播放声音
	//	 * @param no 提示编号
	//	 * @param params 被格式字符串引用的参数
	//	 */
	//	protected void messageX(boolean focus, boolean sound, int no, Object... params) {
	//		FrontLauncher launcher = getLauncher();
	//		String text = launcher.message(no, params);
	//		message(text, focus, sound);
	//	}
	//
	//	/**
	//	 * 根据消息编号和当前语言配置，显示经过格式化处理的消息提示
	//	 * 
	//	 * @param focus 获得焦点
	//	 * @param no 提示编号
	//	 * @param params 被格式字符串引用的参数
	//	 */
	//	protected void messageX(boolean focus, int no, Object... params) {
	//		messageX(focus, true, no, params);
	//	}
	//
	//	/**
	//	 * 根据消息编号和当前语言配置，显示经过格式化处理的消息提示
	//	 * @param no 提示编号
	//	 * @param params 被格式字符串引用的参数
	//	 */
	//	protected void messageX(int no, Object... params) {
	//		messageX(true, true, no, params);
	//	}
	//
	//	/**
	//	 * 根据警告编号和当前语言配置，在窗口显示对应的警告提示
	//	 * 
	//	 * @param focus 获得焦点
	//	 * @param no 提示编号
	//	 */
	//	protected void warningX(boolean focus, int no) {
	//		FrontLauncher launcher = getLauncher();
	//		String text = launcher.warning(no);
	//		warning(text, focus);
	//	}
	//
	//	/**
	//	 * 根据警告编号和当前语言配置，在窗口显示对应的警告提示
	//	 * @param no 提示编号
	//	 */
	//	protected void warningX(int no) {
	//		warningX(true, no);
	//	}
	//
	//	/**
	//	 * 根据警告编号和当前语言配置，显示经过格式化处理的警告提示
	//	 * 
	//	 * @param focus 获得焦点
	//	 * @param no 提示编号
	//	 * @param params 被格式字符串引用的参数
	//	 */
	//	protected void warningX(boolean focus, int no, Object... params) {
	//		FrontLauncher launcher = getLauncher();
	//		String text = launcher.warning(no, params);
	//		warning(text, focus);
	//	}
	//
	//	/**
	//	 * 根据警告编号和当前语言配置，显示经过格式化处理的警告提示
	//	 * @param no 提示编号
	//	 * @param params 被格式字符串引用的参数
	//	 */
	//	protected void warningX(int no, Object... params) {
	//		warningX(true, no, params);
	//	}
	//
	//	/**
	//	 * 根据故障编号和当前语言配置，在窗口显示对应的故障提示
	//	 * 
	//	 * @param focus 获得焦点
	//	 * @param no 提示编号
	//	 */
	//	protected void faultX(boolean focus, int no) {
	//		FrontLauncher launcher = getLauncher();
	//		String text = launcher.fault(no);
	//		fault(text, focus);
	//	}
	//
	//	/**
	//	 * 根据故障编号和当前语言配置，在窗口显示对应的故障提示
	//	 * 
	//	 * @param no 提示编号
	//	 */
	//	protected void faultX(int no) {
	//		faultX(true, no);
	//	}
	//
	//	/**
	//	 * 根据故障编号和当前语言配置，显示经过格式化处理的故障提示
	//	 * 
	//	 * @param focus 获得焦点
	//	 * @param no 提示编号
	//	 * @param params 被格式字符串引用的参数
	//	 */
	//	protected void faultX(boolean focus, int no, Object... params) {
	//		FrontLauncher launcher = getLauncher();
	//		String text = launcher.fault(no, params);
	//		fault(text, focus);
	//	}
	//
	//	/**
	//	 * 根据故障编号和当前语言配置，显示经过格式化处理的故障提示
	//	 * @param no 提示编号
	//	 * @param params 被格式字符串引用的参数
	//	 */
	//	protected void faultX(int no, Object... params) {
	//		faultX(true, no, params);
	//	}
	//
	//	/**
	//	 * 导出错误堆栈
	//	 * @param e Throwable实例
	//	 * @return 返回错误堆栈字符串
	//	 */
	//	protected String export(Throwable e) {
	//		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
	//		PrintStream output = new PrintStream(buff, true);
	//		e.printStackTrace(output);
	//		byte[] b = buff.toByteArray();
	//		return new String(b, 0, b.length);
	//	}
	//
	//	/**
	//	 * 显示故障信息
	//	 * @param e Throwable实例
	//	 */
	//	protected void fault(Throwable e) {
	//		fault(export(e));
	//	}

	//	/** 标题 **/
	//	private ShowTitle title;
	//
	//	/** 表单元 **/
	//	private ArrayList<ShowItem> items = new ArrayList<ShowItem>();
	//
	//	/**
	//	 * 一次性输出标题和表单元
	//	 */
	//	protected void flushTable() {
	//		if (display != null && display.isUsabled()) {
	//			display.showTable(title, items);
	//		}
	//	}
	//
	//	/**
	//	 * 设置表标题。在此之前，旧标题和全部行将被删除。
	//	 * @param e ShowTitle实例
	//	 */
	//	protected void setShowTitle(ShowTitle e) {
	//		title = e;
	//	}
	//
	//	/**
	//	 * 增加表记录。在增加表记录前，必须调用“setShowTitle”方法。
	//	 * @param e ShowItem实例
	//	 */
	//	protected void addShowItem(ShowItem e) {
	//		items.add(e);
	//	}
	//
	//	/**
	//	 * 返回标题单元数目
	//	 * @return 标题单元数目
	//	 */
	//	protected int getTitleCellCount() {
	//		if (display != null && display.isUsabled()) {
	//			return display.getTitleCellCount();
	//		}
	//		return 0;
	//	}
	//
	//	/**
	//	 * 设置状态栏消息
	//	 * @param text 文本
	//	 */
	//	protected void setStatusText(String text) {
	//		if (display != null && display.isUsabled()) {
	//			display.setStatusText(text);
	//		}
	//	}
	//
	//	/**
	//	 * 取出回显码的错误码，打印错误文本
	//	 * @param code EchoCode实例
	//	 */
	//	protected void printFault(EchoCode code) {
	//		FrontLauncher launcher = getLauncher();
	//		String text = launcher.echo(code.getMinor());
	//		text = String.format("%s (%d,%d)", text, code.getMajor(), code.getMinor());
	//		fault(text);
	//	}
	//
	//	/**
	//	 * 打印错误信息
	//	 */
	//	protected void printFault() {
	//		int index = findEchoKey(0);
	//		EchoBuffer bf = findBuffer(index);
	//		if (bf == null) {
	//			faultX(FaultTip.FAILED_X, getCommand());
	//			return;
	//		}
	//		// 找到回显报头
	//		EchoHead head = bf.getHead(); 
	//		EchoCode code = head.getCode();
	//		if (code.isSuccessful()) {
	//			return;
	//		}
	//		printFault(code);
	//		// 服务器端返回的故障提示
	//		EchoHelp help = head.getHelp();
	//		if (help != null) {
	//			fault(help.toString());
	//		}
	//	}

	/**
	 * 提交命令到指定的站点（GATE/CALL的任意一种）<br>
	 * 说明：在提交命令的流程是，首先在本地建立回显缓存，然后再将命令提交到服务器
	 * 
	 * @param hub 目标站点
	 * @param cmd 分布命令
	 * @return 提交成功返回真，否则假。
	 */
	protected boolean fireToHub(Node hub, Command cmd) {
		// 已经注销
		if (isLogout()) {
			faultX(FaultTip.SITE_NOT_LOGING);
			// 如果有结果监听器，通知它
			ProductListener listener = getProductListener();
			if (listener != null) {
				listener.push(null);
			}
			return false;
		}

		CommandItem item = new CommandItem(hub, cmd);
		// 提交命令
		boolean success = completeTo(item);

		// 提示出错
		if (success) {
			// 没有异步监听器情况下，发送提示
			if (!hasProductListener()) {
				messageX(MessageTip.COMMAND_ACCEPTED);
			}
		} else {
			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
			// 如果有监听器，通知它
			ProductListener listener = getProductListener();
			if (listener != null) {
				listener.push(null);
			}
		}

		return success;
	}

	/**
	 * 投递默认的命令到注册站点（GATE）
	 * @return 投递成功返回真，否则假。提前之前，首先在本地建立回显缓存，然后提交命令到服务器
	 */
	protected boolean fireToHub() {
		Node hub = getHub();
		Command cmd = getCommand();
		return fireToHub(hub, cmd);
	}

	/**
	 * 投递默认的命令到注册站点（GATE）
	 * @param cmd 指定的命令
	 * @return 投递成功返回真，否则假。提前之前，首先在本地建立回显缓存，然后提交命令到服务器
	 */
	protected boolean fireToHub(Command cmd) {
		Node hub = getHub();
		return fireToHub(hub, cmd);
	}

	/**
	 * 投递默认的命令到指定的站点（GATE/CALL）
	 * @param hub 站点地址
	 * @return 投递成功返回真，否则假。
	 */
	protected boolean fireToHub(Node hub) {
		Command cmd = getCommand();
		return fireToHub(hub, cmd);
	}

	/**
	 * 显示一行指定列数的空格
	 * @param columns 列数
	 * @param foreground 前景色
	 * @param background 背景色
	 */
	protected void printGap(int columns, Color foreground, Color background) {
		ShowItem showItem = new ShowItem();
		for (int i = 0; i < columns; i++) {
			ShowStringCell cell = new ShowStringCell(i, "");
//			cell.setForeground(foreground);
//			cell.setBackground(background);
			showItem.add(cell);
		}
		// 增加一行记录
		addShowItem(showItem);
	}

	/**
	 * 显示一行指定列数的空格
	 * @param columns 列数
	 */
	protected void printGap(int columns) {
		Color foreground = null;
		Color background = findXMLBackground("GAP/ITEM", new Color(0xd5e5e9));
		printGap(columns, foreground, background);
	}

	/**
	 * 显示一行参数
	 * @param columns 参数
	 */
	protected void printRow(Object[] columns) {
		ShowItem item = new ShowItem();

		// 逐列解析添加到集合
		for (int index = 0; index < columns.length; index++) {
			Object e = columns[index];
			if (Laxkit.isClassFrom(e, String.class)) {
				item.add(new ShowStringCell(index, (String) e));
			} else if (Laxkit.isClassFrom(e, Short.class)) {
				short value = ((Short) e).shortValue();
				item.add(new ShowShortCell(index, value));
			} else if (Laxkit.isClassFrom(e, Integer.class)) {
				int value = ((Integer) e).intValue();
				item.add(new ShowIntegerCell(index, value));
			} else if (Laxkit.isClassFrom(e, Long.class)) {
				long value = ((Long) e).longValue();
				item.add(new ShowLongCell(index, value));
			} else if (Laxkit.isClassFrom(e, Float.class)) {
				float value = ((Float) e).floatValue();
				item.add(new ShowFloatCell(index, value));
			} else if (Laxkit.isClassFrom(e, Double.class)) {
				double value = ((Double) e).doubleValue();
				item.add(new ShowDoubleCell(index, value));
			} else if(Laxkit.isClassFrom(e, Boolean.class)) {
				Boolean b = (Boolean)e;
				item.add(createConfirmTableCell(index, b.booleanValue()));
			} else if (Laxkit.isClassFrom(e, Icon.class)) {
				item.add(new ShowImageCell(index, (Icon) e));
			} else {
				item.add(new ShowStringCell(index, e));
			}
		}

		// 保存一行记录
		addShowItem(item);
	}
}