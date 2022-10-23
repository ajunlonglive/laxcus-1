/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

import com.laxcus.command.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.local.*;
import com.laxcus.util.skin.*;
import com.laxcus.util.sound.*;
import com.laxcus.util.tip.*;
import com.laxcus.watch.*;
import com.laxcus.watch.component.*;
import com.laxcus.watch.pool.*;

/**
 * WATCH站点的异步命令调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 02/03/2012
 * @since laxcus 1.0
 */
public abstract class WatchInvoker extends EchoInvoker {

	/** WATCH调用器界面配置加载器 **/
	private static SurfaceLoader loader = new SurfaceLoader();

	/**
	 * 加载WATCH调用器的多语言配置文件
	 */
	private static void loadInvokerSurface() {
		LocalSelector selector = new LocalSelector("conf/watch/invoker/config.xml");
		String path = selector.findPath("invoker");
		loader.load(path);
	}

	static {
		loadInvokerSurface();
	}

	/**
	 * 根据XML路径，取出标签中的属性内容
	 * @param xmlPath XML路径
	 * @return 标签属性信息
	 */
	public static String getXMLAttribute(String xmlPath) {
		return loader.getAttribute(xmlPath);
	}

	/**
	 * 根据XML路径，取出标签中的文本内容
	 * @param xmlPath XML路径
	 * @return 文本块信息
	 */
	public static String getXMLContent(String xmlPath) {
		return loader.getContent(xmlPath);
	}

	/**
	 * 构造WATCH站点的异步调用器，指定异步命令
	 * @param cmd 异步命令
	 */
	protected WatchInvoker(Command cmd) {
		super();
		// 流式处理或者否，由WATCH管理员决定。
		cmd.setMemory(isStream());
		// 命令超时
		cmd.setTimeout(getCommandTimeout());
		// 账号签名
		cmd.setIssuer(getUsername());
		// 命令优先级
		cmd.setPriority(getCommandPriority());
		// 设置命令
		setCommand(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public WatchLauncher getLauncher() {
		return (WatchLauncher) super.getLauncher();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommandPool()
	 */
	@Override
	public WatchCommandPool getCommandPool() {
		return (WatchCommandPool) super.getCommandPool();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getInvokerPool()
	 */
	@Override
	public WatchInvokerPool getInvokerPool() {
		return (WatchInvokerPool) super.getInvokerPool();
	}
	
	/**
	 * 设置为快速处理
	 * @param b 真或者假
	 */
	protected void setQuick(boolean b) {
		Command cmd = super.getCommand();
		if (cmd != null) {
			cmd.setQuick(b);
		}
	}

	/**
	 * 设置为极速处理
	 * @param b 真或者假
	 */
	protected void setFast(boolean b) {
		Command cmd = super.getCommand();
		if (cmd != null) {
			cmd.setFast(b);
		}
	}
	
	/**
	 * 判断注册站点是TOP节点
	 * @return 返回真或者假
	 */
	public boolean isTopHub() {
		return getHub().isTop();
	}

	/**
	 * 判断注册站点是HOME节点
	 * @return 返回真或者假
	 */
	public boolean isHomeHub() {
		return getHub().isHome();
	}

	/**
	 * 判断注册站点是BANK节点
	 * @return 返回真或者假
	 */
	public boolean isBankHub() {
		return getHub().isBank();
	}

	/**
	 * 返回警告拒绝
	 * @return RadioRefuse实例
	 */
	protected NoticeMuffler getWarningMuffler() {
		return getLauncher().getWarningMuffler();
	}

	/**
	 * 返回故障拒绝器
	 * @return RadioRefuse实例
	 */
	protected NoticeMuffler getFaultMuffler() {
		return getLauncher().getFaultMuffler();
	}

	/**
	 * 判断拒绝警告
	 * @param e 节点
	 * @return 返回真或者假
	 */
	protected boolean isRefuseWarning(Node e) {
		return getWarningMuffler().isRefuse(e);
	}

	/**
	 * 判断拒绝故障
	 * @param e 节点
	 * @return 返回真或者假
	 */
	protected boolean isRefuseFault(Node e) {
		return getFaultMuffler().isRefuse(e);
	}

	/**
	 * 根据XML路径，取出属性段中的标题参数。标题属性用关键字“title”。
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
		// 只接受nimbus普通显示界面，其它忽略！
		if (!Skins.isNimbus()) {
			return null;
		}

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
		// 只接受nimbus普通显示界面，其它忽略！
		if (!Skins.isNimbus()) {
			return null;
		}

		String xmlTitle = String.format("%s/foreground", xmlRoot);
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

		//		if (defaultColor == null) {
		//			// 是数字，转成颜色值；不是，去系统的颜色模板中取颜色
		//			if (ConfigParser.isInteger(value)) {
		//				int rgb = ConfigParser.splitInteger(value, 0);
		//				return new Color(rgb);
		//			} else {
		//				return ColorTemplate.findColor(value);
		//			}
		//		} else {
		//			// 如果不是数字，去系统的颜色模板中取颜色
		//			if (ConfigParser.isInteger(value)) {
		//				int rgb = ConfigParser.splitInteger(value, 0);
		//				return new Color(rgb);
		//			} else {
		//				Color c = ColorTemplate.findColor(value);
		//				return (c == null ? defaultColor : c);
		//			}
		//		}
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
		int width = (defaultWidth < 1 ? 100 : defaultWidth);

		String text = findXMLTitle(xmlRoot);
		// 取宽度定义
		String w = findXMLWidth(xmlRoot);
		if (w != null && w.matches("^\\s*(?i)(\\d+)\\s*$")) {
			width = Integer.parseInt(w.trim());
		}

		// 生成显示列
		return new ShowTitleCell(index, text, width);
	}

	/**
	 * 生成一个表格标题单元
	 * @param index 在标题行中的列下标
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

	/** 成功/失败状态图标 **/
	private static Icon successIcon;

	private static Icon failedIcon;

	/**
	 * 按照要求生成图标单元
	 * @param index 在标题行中的列下标
	 * @param success 成功标记
	 * @return 返回TableImageCell实例
	 */
	protected ShowImageCell createConfirmTableCell(int index, boolean success) {
		// 取出图标
		if (successIcon == null && failedIcon == null) {
			ResourceLoader loader = new ResourceLoader("conf/watch/image/invoker/");
			successIcon = loader.findImage("success.png");
			failedIcon = loader.findImage("failed.png");
		}
		return new ShowImageCell(index, (success ? successIcon : failedIcon));
	}

	/**
	 * 返回当前WATCH站点账号签名
	 * @return 账号签名
	 */
	public Siger getUsername() {
		WatchLauncher launcher = getLauncher();
		return launcher.getUsername();
	}

	/**
	 * 判断系统默认命令采用流处理模式（内存计算模式）
	 * @return 返回真或者假
	 */
	public boolean isStream() {
		WatchLauncher launcher = getLauncher();
		return launcher.isMemory();
	}

	/**
	 * 返回用户定义的命令超时
	 * @return 超时时间，-1是无限制。
	 */
	public long getCommandTimeout() {
		WatchLauncher launcher = getLauncher();
		return launcher.getCommandTimeout();
	}

	/**
	 * 返回用户定义的命令优先级
	 * @return 返回优先级定义
	 */
	public byte getCommandPriority() {
		WatchLauncher launcher = getLauncher();
		return launcher.getCommandPriority();
	}
	
	/**
	 * 返回混合面板
	 * @return WatchMixedPanel实例
	 */
	protected WatchMixedPanel getDisplayPanel() {
		WatchLauncher launcher = getLauncher();
		return launcher.getWindow().getSkeletonPanel().getMixPanel();
	}

	/**
	 * 解析时间参数
	 * @param time 采用LAXCUS系统定义的“年/月/日  时.分.秒  毫秒”格式
	 * @return 返回本地时间系统解析的字符串格式
	 */
	protected String splitLaxcusTime(long time) {
		// 小于1是无定义
		if (time < 1) {
			return "";
		}
		Date date = com.laxcus.util.datetime.SimpleTimestamp.format(time);
		DateFormat dt = DateFormat.getDateTimeInstance(); // 系统默认的日期/时间格式
		return dt.format(date);
	}

	/** 标题 **/
	private ShowTitle title;

	/** 表单元 **/
	private ArrayList<ShowItem> items = new ArrayList<ShowItem>();

	/**
	 * 一次性输出标题和表单元
	 */
	protected void flushTable() {
		WatchMixedPanel display = getDisplayPanel();
		display.showTable(title, items);
	}

	/**
	 * 设置表格标题
	 * @param e ShowTitle实例
	 */
	public void setShowTitle(ShowTitle e) {
		title = e;
	}

	/**
	 * 返回标题单元数目
	 * @return 标题单元数目
	 */
	protected int getTitleCellCount() {
		WatchMixedPanel display = getDisplayPanel();
		return display.getTitleCellCount();
	}

	/**
	 * 增加一行表格记录
	 * @param e ShowItem实例
	 */
	public void addShowItem(ShowItem e) {
		items.add(e);
	}

	/**
	 * 设置状态栏消息
	 * @param text 文本
	 */
	protected void setStatusText(String text) {
		getLauncher().getWindow().setStatusText(text);
	}

	/**
	 * 从回显码中判断错误信息，打印文本错误
	 * @param code 回显码
	 */
	protected void printFault(EchoCode code) {
		WatchLauncher launcher = getLauncher();
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
			fault("SYSTEM MISSING");
			return;
		}
		// 找到回显报头
		EchoHead head = bf.getHead(); 
		EchoCode code = head.getCode();
		if (code.isSuccessful()) {
			return;
		}
		// 打印错误码
		printFault(code);
		// 服务器端返回的故障提示
		EchoHelp help = head.getHelp();
		if (help != null) {
			fault(help.toString());
		}
	}

	/**
	 * 打印运行时间
	 */
	protected void printRuntime() {
		long time = getRunTime();
		if (time < 1) {
			return;
		}

		WatchLauncher launcher = getLauncher();
		String input = launcher.message(MessageTip.COMMAND_USEDTIME_X);
		RuntimeFormat e = new RuntimeFormat();
		String text = e.format(input, time);
		message(text);
	}

	/**
	 * 格式化分布时间
	 * @param time 分布时间
	 * @return 返回格式化时间
	 */
	protected String formatDistributeTime(long time) {
		WatchLauncher launcher = getLauncher();
		String input = launcher.message(MessageTip.DISTRIBUTED_TIME_X);
		RuntimeFormat e = new RuntimeFormat();
		return e.format(input, time);
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

		WatchLauncher launcher = getLauncher();
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
	 * 导出错误堆栈
	 * @param e Throwable实例
	 * @return 返回错误堆栈中的文本
	 */
	protected String export(Throwable e) {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
		PrintStream output = new PrintStream(buff, true);
		e.printStackTrace(output);
		byte[] b = buff.toByteArray();
		return new String(b, 0, b.length);
	}

	/**
	 * 播放消息提示声音
	 */
	protected void playMessage() {
		getLauncher().playSound(SoundTag.MESSAGE);
	}

	/**
	 * 播放警告声音
	 */
	protected void playWarning() {
		getLauncher().playSound(SoundTag.WARNING);
	}

	/**
	 * 播放错误声音
	 */
	protected void playError() {
		getLauncher().playSound(SoundTag.ERROR);
	}

	/**
	 * 显示普通信息
	 * @param text 普通文本信息
	 * @param sound 播放声音
	 */
	protected void message(String text, boolean sound) {
		// 不要处理声音，由里面的线程处理
		WatchMixedPanel display = getDisplayPanel();
		display.message(text, sound);
	}

	/**
	 * 显示警告
	 * @param text 警告文本信息
	 * @param sound 播放声音
	 */
	protected void warning(String text, boolean sound) {
		WatchMixedPanel display = getDisplayPanel();
		display.warning(text, sound);
	}

	/**
	 * 显示故障信息
	 * @param text 故障文本信息
	 * @param sound 播放声音
	 */
	protected void fault(String text, boolean sound) {
		WatchMixedPanel display = getDisplayPanel();
		display.fault(text, sound);
	}

	/**
	 * 显示普通信息
	 * @param text 普通文本信息
	 */
	protected void message(String text) {
		message(text, true);
	}

	/**
	 * 显示警告
	 * @param text 警告文本信息
	 */
	protected void warning(String text) {
		warning(text, true);
	}

	/**
	 * 显示故障信息
	 * @param text 故障文本信息
	 */
	protected void fault(String text) {
		fault(text, true);
	}
	
	/**
	 * 根据消息编号和当前语言配置，在窗口显示对应的消息提示
	 * @param no 提示编号
	 */
	protected void messageX(int no) {
		WatchLauncher launcher = getLauncher();
		String text = launcher.message(no);
		message(text);
	}

	/**
	 * 根据消息编号和当前语言配置，显示经过格式化处理的消息提示
	 * @param no 提示编号
	 * @param params 被格式字符串引用的参数
	 */
	protected void messageX(int no, Object... params) {
		WatchLauncher launcher = getLauncher();
		String text = launcher.message(no, params);
		message(text);
	}

	/**
	 * 根据警告编号和当前语言配置，在窗口显示对应的警告提示
	 * @param no 提示编号
	 */
	protected void warningX(int no) {
		WatchLauncher launcher = getLauncher();
		String text = launcher.warning(no);
		warning(text);
	}

	/**
	 * 根据警告编号和当前语言配置，显示经过格式化处理的警告提示
	 * @param no 提示编号
	 * @param params 被格式字符串引用的参数
	 */
	protected void warningX(int no, Object... params) {
		WatchLauncher launcher = getLauncher();
		String text = launcher.warning(no, params);
		warning(text);
	}

	/**
	 * 根据故障编号和当前语言配置，在窗口显示对应的故障提示
	 * @param no 提示编号
	 */
	protected void faultX(int no) {
		WatchLauncher launcher = getLauncher();
		String text = launcher.fault(no);
		fault(text);
	}

	/**
	 * 根据故障编号和当前语言配置，显示经过格式化处理的故障提示
	 * @param no 提示编号
	 * @param params 被格式字符串引用的参数
	 */
	protected void faultX(int no, Object... params) {
		WatchLauncher launcher = getLauncher();
		String text = launcher.fault(no, params);
		fault(text);
	}

	/**
	 * 显示故障信息
	 * @param e Throwable实例
	 */
	protected void fault(Throwable e) {
		String line = export(e);
		fault(line);
	}

	/**
	 * 提交命令到指定的站点（TOP/HOME/BANK的任意一种）<br>
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
			return false;
		}

		CommandItem item = new CommandItem(hub, cmd);
		// 提交命令
		boolean success = completeTo(item);

		// 提示出错
		if (success) {
			messageX(MessageTip.COMMAND_ACCEPTED);
		} else {
			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}

		return success;
	}

	/**
	 * 以容错模式，向管理站点提交命令（TOP/HOME/BANK任意一种）。
	 * 
	 * @param cmds 命令集合
	 * @return 大于0个命令发送成功，返回真，否则假。
	 */
	protected boolean fireMultiToHub(Collection<Command> cmds) {
		// 已经注销
		if (isLogout()) {
			faultX(FaultTip.SITE_NOT_LOGING);
			return false;
		}

		Node hub = getHub();
		// 生成命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (Command sub : cmds) {
			CommandItem item = new CommandItem(hub, sub);
			array.add(item);
		}

		// 容错模式发送
		int count = incompleteTo(array);
		boolean success = (count > 0);
		// 提示出错
		if (success) {
			messageX(MessageTip.COMMAND_ACCEPTED);
		} else {
			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}

		return success;
	}

	/**
	 * 投递命令到注册站点（TOP/HOME/BANK）
	 * @param cmd 异步命令
	 * @return 成功返回真，否则假
	 */
	protected boolean fireToHub(Command cmd) {
		Node hub = getHub();
		return fireToHub(hub, cmd);
	}

	/**
	 * 投递默认的命令到注册站点（TOP/HOME/BANK）
	 * @return 投递成功返回真，否则假。
	 */
	protected boolean fireToHub() {
		Node hub = getHub();
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
			cell.setForeground(foreground);
			cell.setBackground(background);
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
		Color foreground = null; Color background =null;
		// 只有Nimbus界面才显示颜色
		if (Skins.isNimbus()) {
			background = findXMLBackground("GAP/ITEM", new Color(0xd5e5e9));
		}
		
 		printGap(columns, foreground, background);
	}

	/**
	 * 显示一行指定列数的空格
	 * @param columns 列数
	 */
	protected void printSubGap(int columns) {
		Color foreground = null; Color background =null;
		// 只有Nimbus界面才显示颜色
		if (Skins.isNimbus()) {
			background = findXMLBackground("SUBGAP/ITEM", new Color(0xd5e5e9));
		}
		
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