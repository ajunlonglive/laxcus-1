/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.plaf.*;

import com.laxcus.access.diagram.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.front.meet.*;
import com.laxcus.front.meet.pool.*;
import com.laxcus.front.pool.*;
import com.laxcus.front.terminal.component.*;
import com.laxcus.front.terminal.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.task.establish.end.*;
import com.laxcus.task.guide.pool.*;
import com.laxcus.thread.*;
import com.laxcus.tub.servlet.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.splash.*;
import com.laxcus.util.event.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.local.*;
import com.laxcus.util.login.*;
import com.laxcus.util.skin.*;
import com.laxcus.util.sound.*;
import com.laxcus.xml.*;

/**
 * 终端启动器 <br>
 * 
 * 终端启动器以图形界面显示，是交互操作。
 * 
 * @author scott.liang
 * @version 1.5 7/20/2015
 * @since laxcus 1.0
 */
public final class TerminalLauncher extends MeetLauncher implements LocalMatcher , InvokerMessenger {

	/** 终端启动器实例 **/
	private static TerminalLauncher selfHandle = new TerminalLauncher();

	/** 终端主窗口 **/
	private TerminalWindow window = new TerminalWindow();

	/** 皮肤颜色加载器 **/
	private SkinTokenLoader skinLoader = new SkinTokenLoader();

	/** 窗口界面资源解析器 **/
	private SurfaceLoader surfaceLoader = new SurfaceLoader();

	/** 启动屏窗口 **/
	private SplashWindow splash = new SplashWindow();
	
	/**
	 * 构造默认和私有的FRONT图形终端启动器
	 */
	private TerminalLauncher() {
		super(RankTag.TERMINAL, true, true, TerminalLogTrustor.getInstance());
		
		// 加载帮助上下文
		loadCommentContext(GUIKit.isHighScreen());
		
		// 设置INVOKER句柄
		InvokerTrustor.setInvokerMessenger(this);
	}

	/**
	 * 返回终端启动器静态实例
	 * @return TerminalLauncher实例
	 */
	public static TerminalLauncher getInstance() {
		return TerminalLauncher.selfHandle;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerMessenger#startInvoker(com.laxcus.echo.invoke.EchoInvoker)
	 */
	@Override
	public void startInvoker(EchoInvoker invoker) {
		if (!invoker.isDistributed()) {
			return;
		}
		// 发出
		window.rolling(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerMessenger#stopInvoker(com.laxcus.echo.invoke.EchoInvoker, boolean)
	 */
	@Override
	public void stopInvoker(EchoInvoker invoker, boolean success) {
		if (!invoker.isDistributed()) {
			return;
		}
		
		// 不成功或者退出时，记录流量
		if (!success || invoker.isQuit()) {
			long rs = invoker.getReceiveFlowSize();
			long ss = invoker.getSendFlowSize();
			window.addFlows(rs, ss);
		}
		// 发出闪烁
		window.rolling(false);
	}
	
	/**
	 * 处理平台字体
	 */
	public void doPlatformFont() {
		// 检查平台字体
		checkPlatformFont();
		// 消息对话框文本
		checkMessageDialog();
		checkFileChooser();
	}

	/**
	 * 返回终端的窗口界面
	 * @return TerminalWindow实例
	 */
	public TerminalWindow getWindow() {
		return window;
	}

	/**
	 * 文件选择窗口
	 */
	private void checkFileChooser() {
		String text = findContent("FileChooser/Button/Save");
		UIManager.put("FileChooser.saveButtonText", text);

		text = findContent("FileChooser/Button/Open");
		UIManager.put("FileChooser.openButtonText", text);

		text = findContent("FileChooser/Button/Cancel");
		UIManager.put("FileChooser.cancelButtonText", text);

		text = findContent("FileChooser/Button/Help");
		UIManager.put("FileChooser.helpButtonText", text);

		text = findContent("FileChooser/Button/Update");
		UIManager.put("FileChooser.updateButtonText", text);
	}

	/**
	 * 找到首个匹配的字体
	 * @param fonts
	 * @return 首选字体
	 */
	private Font choiceFirst(Font[] defaultFonts, Font[] fonts) {
		for (Font hot : defaultFonts) {
			String hotFamily = hot.getFamily();
			for (Font font : fonts) {
				String family = font.getFamily();
				if (family.indexOf(hotFamily) > -1) {
					return hot;
				}
			}
		}

		return fonts[0];
	}

	/**
	 * 建立目录
	 * @return
	 */
	public File createConfigDirectory() {
		String bin = System.getProperty("user.dir");
		bin += "/../conf";
		File file = new File(bin);
		boolean success = (file.exists() && file.isDirectory());
		if (!success) {
			success = file.mkdirs();
		}
		return (success ? file : null);
	}

	/**
	 * 解析一行字体
	 * @param input 输入参数
	 * @return 返回字体或者空指针
	 */
	private Font readFont(String input) {
		final String regex = "^\\s*(.+?)\\s*\\,\\s*(?i)([PLAIN|BOLD|ITALIC]+)\\s*\\,\\s*([\\d]+)\\s*$";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}

		String family = matcher.group(1);
		String styleName = matcher.group(2);
		int size = Integer.parseInt(matcher.group(3));

		// 字体样式
		int style = Font.PLAIN;
		if (styleName.matches("^\\s*(?i)(PLAIN)\\s*$")) {
			style = Font.PLAIN;
		} else if (styleName.matches("^\\s*(?i)(BOLD)\\s*$")) {
			style = Font.BOLD;
		} else if (styleName.matches("^\\s*(?i)(ITALIC)\\s*$")) {
			style = Font.ITALIC;
		}

		// 生成字体
		return new Font(family, style, size);
	}

	/**
	 * 读平台定义字体
	 * @return
	 */
	public Font[] readPlatformFont() {
		File dir = createConfigDirectory();
		if (dir == null) {
			return null;
		}
		// 配置目录下的字体文件
		File file = new File(dir, "fonts.conf");
		// 没有这个文件，忽略它
		if(!(file.exists() && file.isFile())) {
			return null;
		}

		// 从配置文件中读取全部配置
		ArrayList<Font> array = new ArrayList<Font>();
		try {
			FileInputStream in = new FileInputStream(file);
			InputStreamReader is = new InputStreamReader(in, "UTF-8");
			BufferedReader bf = new BufferedReader(is);
			do {
				String line = bf.readLine();
				if (line == null) {
					break;
				}
				Font font = readFont(line);
				if (font != null) {
					array.add(font);
				}
			} while (true);
			bf.close();
			is.close();
			in.close();
		} catch (IOException e) {

		}

		if(array.isEmpty()) {
			return null;
		}
		// 输出全部字体
		Font[] fonts = new Font[array.size()];
		return array.toArray(fonts);
	}

	/**
	 * 消息对话框按纽文本
	 */
	private void checkMessageDialog() {
		String text = findContent("MessageDialog/Button/Okay");
		UIManager.put("OptionPane.okButtonText", text);
		text = findContent("MessageDialog/Button/Yes");
		UIManager.put("OptionPane.yesButtonText", text);
		text = findContent("MessageDialog/Button/No");
		UIManager.put("OptionPane.noButtonText", text);
		text = findContent("MessageDialog/Button/Cancel");
		UIManager.put("OptionPane.cancelButtonText", text);

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/front/terminal/image/message/question.png", 32, 32);
		UIManager.put("OptionPane.questionIcon", icon);
		icon = loader.findImage("conf/front/terminal/image/message/info.png", 32, 32);
		UIManager.put("OptionPane.informationIcon", icon);
		icon = loader.findImage("conf/front/terminal/image/message/error.png", 32, 32);
		UIManager.put("OptionPane.errorIcon", icon);
		icon = loader.findImage("conf/front/terminal/image/message/warning.png", 32, 32);
		UIManager.put("OptionPane.warningIcon", icon);
	}

	/**
	 * 检查平台字体
	 */
	private void checkPlatformFont() {
		// 读平台上定义的字体
		Font[] defaultFonts = readPlatformFont();
		if (defaultFonts == null) {
			return;
		}

		// 取出例子文本
		String text = findContent("grade/administrator");
		if(text == null) {
			return;
		}
		// 找到合适的字体
		Font[] fonts = FontKit.findFonts(text);
		if(fonts == null) {
			return;
		}

		// 首选字体
		Font font = choiceFirst(defaultFonts, fonts);
		FontUIResource res = new FontUIResource(font);
		// 设置匹配的字体
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);

			if (value instanceof FontUIResource) {
				UIManager.put(key, res);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.local.LocalMatcher#findCaption(java.lang.String)
	 */
	@Override
	public String findCaption(String xmlPath) {
		if(!surfaceLoader.isLoaded()) {
			surfaceLoader.load(getSurfacePath());
		}
		return surfaceLoader.getAttribute(xmlPath);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.local.LocalMatcher#findContent(java.lang.String)
	 */
	@Override
	public String findContent(String xmlPath) {
		if (!surfaceLoader.isLoaded()) {
			surfaceLoader.load(getSurfacePath());
		}
		return surfaceLoader.getContent(xmlPath);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#disconnect()
	 */
	@Override
	public void disconnect() {
		// 退出登录
		logout();

		// // 强制注销
		// setRoundSuspend(true);
		// // 退出登录
		// logout();

		// // 离线未使用
		// local.setGrade(GradeTag.OFFLINE);
	}
	
	/**
	 * 自动登录，覆盖FrontLauncher.login方法
	 * 
	 * @see com.laxcus.front.FrontLauncher#login()
	 */
	@Override
	public boolean login() {
		// 如果没有达到GATE节点要求的重新注册间隔时间，忽略！
		if (!canAutoReloginInterval()) {
			return false;
		}

		// 确定初始HUB站点地址（ENTRANCE站点）
		Node entrance = getInitHub();
		if (entrance == null) {
			Logger.error(this, "login", "entrance site is null!");
			return false;
		}

		// 自动注销，SiteLauncher.defaultProcess方法不进入强制自循环！
		__logout(true);

		// 在重新注册前，刷新最后调用的时间
		refreshEndTime();

		// 故障声音
		playSound(SoundTag.ERROR);
		// 清除窗口，自动登录，返回结果
		return window.__auto_login(entrance);
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.front.meet.MeetLauncher#showLicence(java.lang.String)
//	 */
//	@Override
//	public boolean showLicence(String content) {
//		TerminalLicenceDialog dialog = new TerminalLicenceDialog(window, true, content);
//		dialog.showDialog();
//		return dialog.isAccpeted();
//	}

	/**
	 * 图形终端播放声音
	 * @param who 声音编号
	 */
	@Override
	public void playSound(int who){
		SoundPlayer.getInstance().play(who);
	}

	/**
	 * 加载声音参数
	 */
	private void loadSound() {
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/sound");
		// 警告声音
		byte[] stream = loader.findStream("warning.wav");
		SoundItem item = SoundItem.create(SoundTag.WARNING, stream);
		if (item != null) {
			SoundPlayer.getInstance().add(item);
		}
		// 错误声音
		stream = loader.findStream("error.wav");
		item = SoundItem.create(SoundTag.ERROR, stream);
		if (item != null) {
			SoundPlayer.getInstance().add(item);
		}
		// 消息提示声音
		stream = loader.findStream("message.wav");
		item = SoundItem.create(SoundTag.MESSAGE, stream);
		if (item != null) {
			SoundPlayer.getInstance().add(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#preinit()
	 */
	@Override
	protected boolean preinit() {
		loadSound();
		return true;
	}

	/**
	 * 继承自SiteLauncher.defaultSubProcess，定时检测CPU压力，控制日志更新频率
	 * @see com.laxcus.launch.SiteLauncher#defaultSubProcess()
	 */
	@Override
	protected void defaultSubProcess() {
		double rate = 100.0f;
		if (isLinux()) {
			rate = LinuxEffector.getInstance().getRate();
		} else if (isWindows()) {
			rate = WindowsEffector.getInstance().getRate();
		}

		// 修改日志刷新频率
		// 1. 达到60%比率，日志发送调整到最低值
		// 2. 超过30%比率，降低日志发送
		// 3. 低于15%比率，提高日志发送
		if (rate >= 60.0f) {
			TerminalLogTrustor.getInstance().low();
		} else if (rate >= 30.0f) {
			TerminalLogTrustor.getInstance().descent();
		} else if (rate < 15.0f) {
			TerminalLogTrustor.getInstance().rise();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#launch()
	 */
	@Override
	protected boolean launch() {
		// 启动窗口，注册到GATE节点
		boolean success = window.showWindow();

		// 设置显示前端
		if (success) {
			PutTaskPool.getInstance().setDisplay(window.getImplementPanel().getTabPanel());
			EndTaskPool.getInstance().setDisplay(window.getImplementPanel().getTabPanel());
			NearTaskPool.getInstance().setDisplay(window.getImplementPanel().getTabPanel());

			// 边缘容器管理池启动（必须保证在图形界面显示成功后才能启动，否则在没有启动图形界面前，TubPool调用图形界面会出错）
			TubPool.getInstance().start();
			
			// 启动日志代理（保证在窗口可视情况下输出日志，否则会有UI卡死现象出现）
			TerminalLogTrustor.getInstance().start();
		}

		// 加载配置
		if (success && CustomConfig.isValidate()) {
			// 加载类定义
			CustomClassLoader loader = new CustomClassLoader();
			loader.load();
			// 加载自定义命令关键字
			String path = CustomConfig.getTokenPath();
			window.addCommandTokens(path);
		}

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#destroy()
	 */
	@Override
	protected void destroy() {
		// 写入配置
		writeConfigure();

		// 销毁窗口
		window.dispose();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isDriver()
	 */
	@Override
	public boolean isDriver() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isConsole()
	 */
	@Override
	public boolean isConsole() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isTerminal()
	 */
	@Override
	public boolean isTerminal() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isEdge()
	 */
	@Override
	public boolean isEdge() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isDesktop()
	 */
	@Override
	public boolean isDesktop() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isApplication()
	 */
	@Override
	public boolean isApplication() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 从总配置文件中选出所关联语言的界面配置文件路径
	 * @return JAR文件中的资源文件路径
	 */
	public String getSurfacePath() {
		LocalSelector selector = new LocalSelector("conf/front/terminal/lang/config.xml");
		return selector.findPath("resource");
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#loadPool()
	 */
	@Override
	protected boolean loadPool() {
		// 委托线程
		VirtualThread[] threads = new VirtualThread[] {
				SwingDispatcher.getInstance(), SoundPlayer.getInstance() };
		startThreads(threads);

		// 业务管理池
		VirtualPool[] pools = new VirtualPool[] {
				MeetInvokerPool.getInstance(), MeetCommandPool.getInstance(),
				StaffOnTerminalPool.getInstance(),
				AuthroizerGateOnFrontPool.getInstance(),
				CallOnFrontPool.getInstance() , GuideTaskPool.getInstance()};

		// 注意！GuideTaskPool在这里启动
		
		// 启动全部管理池
		return startAllPools(pools);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#loadTaskPool()
	 */
	@Override
	protected boolean loadTaskPool() {
		// 设置事件监听器
		PutTaskPool.getInstance().setTaskListener(this);
		EndTaskPool.getInstance().setTaskListener(this);
		NearTaskPool.getInstance().setTaskListener(this);
		
		// CONDUCT.PUT阶段资源代理
		PutTaskPool.getInstance().setPutTrustor(getStaffPool());
		// ESTABLISH.END阶段资源代理
		EndTaskPool.getInstance().setEndTrustor(getStaffPool());
		// CONTACT.NEAR阶段资源代理
		NearTaskPool.getInstance().setNearTrustor(getStaffPool());

		// 业务管理池
		VirtualPool[] pools = new VirtualPool[] { 
				PutTaskPool.getInstance(), EndTaskPool.getInstance(),
				NearTaskPool.getInstance() };

		// 启动全部管理池
		return startAllPools(pools);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#stopPool()
	 */
	@Override
	protected void stopPool() {
		// 管理池
		VirtualPool[] pools = new VirtualPool[] {
				// MEET命令管理池
				MeetCommandPool.getInstance(),
				// MEET调用器管理池
				MeetInvokerPool.getInstance(), CallOnFrontPool.getInstance(),
				AuthroizerGateOnFrontPool.getInstance(),
				StaffOnTerminalPool.getInstance(), TubPool.getInstance(),
				PutTaskPool.getInstance(), EndTaskPool.getInstance(), 
				NearTaskPool.getInstance(), GuideTaskPool.getInstance()};

		// 线程
		VirtualThread[] threads = new VirtualThread[] {
				SoundPlayer.getInstance(),
				SwingDispatcher.getInstance(),
				TerminalLogTrustor.getInstance() };

		// 先关闭线程，再关闭管理池
		stopThreads(threads);
		// 关闭全部管理池
		stopAllPools(pools);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#ticking()
	 */
	@Override
	public void ticking() {
		// 规定必须登录成功1分钟之后，才允许闪烁图标
		// 通过这个限制，判断GUI界面卡死的问题是不是SWING线程造成的？
		long time = getLoginTime();
		boolean success = (time > 0 && System.currentTimeMillis() >= time + 60000);
		if (!success) {
			return;
		}
		// 触发动画显示
		window.flash();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
	 */
	@Override
	protected void disableProcess() {
		// 设置图标为中断
		window.setOnlineIcon(false);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#getStaffPool()
	 */
	@Override
	public StaffOnFrontPool getStaffPool() {
		return StaffOnTerminalPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public MeetInvokerPool getInvokerPool() {
		return MeetInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public MeetCommandPool getCommandPool() {
		return MeetCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#showStatusText(java.lang.String)
	 */
	@Override
	public void showStatusText(String text) {
		window.setStatusText(text);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#showGrade(int)
	 */
	@Override
	public void showGrade(int grade) {
		if (GradeTag.isAdministrator(grade)) {
			String e = findContent("grade/administrator");
			showStatusText(e);
			window.setAdministratorIcon();
		} else if (GradeTag.isUser(grade)) {
			String e = findContent("grade/user");
			showStatusText(e);
			window.setUserIcon();
		} else {
			String e = findContent("grade/undefined");
			showStatusText(e);
			window.setNobodyIcon();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#forsake()
	 */
	@Override
	public void forsake() {
		TerminalForsakeThread e = new TerminalForsakeThread(this);
		e.start();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#setMaxLogs(int)
	 */
	@Override
	public int setMaxLogs(int n) {
		// 通知图形窗口
		int logs = window.setMaxLogs(n);
		// 记录最大日志数目
		TerminalProperties.writeLogElements(logs);
		// 返回结果!
		return logs;
	}

	/**
	 * 弹出确认窗口
	 * @param content 显示文本
	 * @return 接受返回真，否则假
	 */
	private boolean showConfirmDialog(String content) {		
		String title = window.getTitle();
		int who = MessageDialog.showMessageBox(window, title,
				JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION);
	}


	/**
	 * 弹出对话框提示信息
	 */
	public boolean confirm(int no) {
		String content = message(no);
		return showConfirmDialog(content);
	}

	/**
	 * 弹出对话窗口
	 */
	public boolean confirm(int no, Object... params) {
		String content = message(no, params);
		return showConfirmDialog(content);
	}
	
	/**
	 * 解析许可证签名
	 * @param element
	 */
	private void splitLicenceSignature(org.w3c.dom.Element element) {
		// 许可证签名
		String signature = XMLocal.getValue(element, LoginMark.SIGNATURE);
		if (signature != null && signature.trim().length() > 0) {
			setSignature(signature.trim());
		}
	}

	/**
	 * 解析登录服务器地址
	 * @param element 
	 * @return 返回服务器节点
	 */
	private Node splitHubSite(org.w3c.dom.Element element) {
		String input = XMLocal.getValue(element, LoginMark.HUB_SITE);
		try {
			if (input != null) {
				return new Node(input);
			}
		} catch (java.net.UnknownHostException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 解析账号
	 * @param root
	 * @return 成功返回账号，否则是空指针
	 */
	private User splitUser(org.w3c.dom.Element root) {
		org.w3c.dom.NodeList nodes = root.getElementsByTagName(LoginMark.MARK_ACCOUNT);
		if (nodes == null || nodes.getLength() != 1) {
			return null;
		}

		org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);
		String username = XMLocal.getValue(element, LoginMark.USERNAME);
		String password = XMLocal.getValue(element, LoginMark.PASSWORD);

		// 判断是SHA数字，或者是明文
		if (Siger.validate(username) && SHA512Hash.validate(password)) {
			// 生成签名
			return new User(new Siger(username), new SHA512Hash(password));
		} else {
			return new User(username, password);
		}
	}

	/**
	 * 解析自动注册登录参数
	 * @param filename
	 */
	private void splitLogin(String filename) {
		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			return ;
		}

		// 登录参数
		org.w3c.dom.NodeList nodes = document.getElementsByTagName(LoginMark.MARK_LOGIN);
		if (nodes.getLength() == 1) {
			org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);
			String yes = element.getAttribute(LoginMark.AUTO);
			boolean auto = yes.matches("^\\s*(?i)(?:YES|TRUE)\\s*$");

			// 在“YES”情况下，解析许可证
			if (auto) {
				splitLicenceSignature(element); // 解析许可证签名
			}
			Node hub = splitHubSite(element);
			User user = splitUser(element);

			// 判断有效且是ENTRANCE节点
			boolean success = (hub != null && user != null);
			if (success) {
				success = hub.isEntrance();
			}
			if (success) {
				LoginToken token = new LoginToken(auto, hub, user);
				window.setLoginToken(token);
			}
		}
	}

	/**
	 * 建立目录
	 * @return
	 */
	private File createDirectory() {
		String bin = System.getProperty("user.dir");
		bin += "/../conf";
		File file = new File(bin);
		boolean success = (file.exists() && file.isDirectory());
		if (!success) {
			success = file.mkdirs();
		}
		return (success ? file : null);
	}

	/**
	 * 从本地配置中读取皮肤配置名称
	 * @return 返回字符串或者空指针
	 */
	private String readSkinName() {
		// 建立配置目录
		File dir = createDirectory();

		File file = new File(dir, "terminal.conf");
		// 读磁盘文件
		UITacker tracker = new UITacker();
		int who = tracker.read(file);
		if (who < 1) {
			return null;
		}
		// 找到"skin.name"对应值
		return tracker.getString(TerminalProperties.skinName); // "skin.name");
	}

	/**
	 * 返回皮肤加载器
	 * @return SkinTokenLoader实例句柄
	 */
	public SkinTokenLoader getSkinLoader() {
		return skinLoader;
	}

	/**
	 * 加载颜色参数
	 */
	private void loadColors() {
		String xmlPath = "conf/front/terminal/color/color.txt";
		ColorTemplate.load(xmlPath);
	}

	/**
	 * 加载皮肤配置，同是更新外观界面
	 * @return 成功返回真，否则假
	 */
	private boolean loadSkins() {
		// 1. 从配置文件中加载皮肤参数，不成功退出
		boolean success = skinLoader.load("conf/front/terminal/skin/config.xml");
		if (!success) {
			return false;
		}
		// 2. 读取"terminal.conf"配置文件，把"skin.name"参数读取出来
		String name = readSkinName();
		// 如果没有找到皮肤名字，默认是"normal"，对应Nimbus外观
		if (name == null) {
			name = "gray"; // normal关键字在config.xml配置文件里定义
		}
		
		// 3. 找到匹配的皮肤方案
		success = false;
		SkinToken token = skinLoader.findSkinTokenByName(name);
		// 存在，加载皮肤界面
		if (token != null) {
			// 3. 切换到主题界面
			success = token.updateTheme(true);
			if (success) {
				// 选择中
				skinLoader.exchangeCheckedSkinToken(token.getName());
				// 定义外观
				Skins.setLookAndFeel(token.getLookAndFeel());
				Skins.setSkinName(token.getName());
			}
		} else {
			//			// 设置为默认的“Nimbus”外观，“Metal”为暗黑！
			//			success = UITools.updateLookAndFeel("Nimbus", null, null);
			//			if (success) {
			//				int count = SkinToken.loadSkins("conf/front/terminal/skin/nimbus_normal.txt");
			//				success = (count > 0);
			//			}
			//			if (success) {
			//				skinLoader.exchangeCheckedSkinToken("Nimbus");
			//				Skins.setLookAndFeel(Skins.Nimbus);
			//				Skins.setSkinName("normal"); // normal是脚本中的定义
			//			}
			
			// 设置为默认的“Metal”外观，白色
			String clazz = "com.laxcus.util.skin.GrayMetalTheme";
			SkinSheet sheet = new FlatSkinSheet();
			success = UITools.updateLookAndFeel("Metal", clazz, sheet);
			if (success) {
				int count = SkinToken.loadSkins("conf/front/terminal/skin/metal_gray.txt");
				success = (count > 0);
			}
			if (success) {
				skinLoader.exchangeCheckedSkinToken("gray");
				Skins.setLookAndFeel(Skins.Metal);
				Skins.setSkinName("gray"); // "gray"是脚本中的定义
			}
		}
		
		return success;
	}

	/**
	 * 写入FRONT.TERMINAL节点参数配置
	 */
	private void writeConfigure() {
		UITacker tracker = new UITacker();
		// 窗口范围
		tracker.put(TerminalProperties.boundFrame, TerminalProperties.readWindowBound());

		// 分割线
		tracker.put(TerminalProperties.dividerBrowserPane, TerminalProperties.readBrowserPaneDeviderLocation());
		tracker.put(TerminalProperties.dividerCenterPane, TerminalProperties.readCenterPaneDeviderLocation());
		// 云端浏览面板分割线
		tracker.put(TerminalProperties.dividerRemoteDataPane, TerminalProperties.readRemoteDataPaneDeviderLocation());
		// 云端应用软件面板分割线
		tracker.put(TerminalProperties.dividerRemoteSoftwarePane, TerminalProperties.readRemoteSoftwarePaneDeviderLocation());
		// 本地浏览面板分割线
		tracker.put(TerminalProperties.dividerLocalBrowserPane, TerminalProperties.readLocalBrowserPaneDeviderLocation());

		// 系统字体
		tracker.put(TerminalProperties.fontSystem, TerminalProperties.readSystemFont()); 

		// 云端数据字体
		tracker.put(TerminalProperties.fontRemoteData, TerminalProperties.readRemoteDataFont());
		// 云端应用软件字体
		tracker.put(TerminalProperties.fontRemoteSoftware, TerminalProperties.readRemoteSoftwareFont());
		// 本地浏览窗口
		tracker.put(TerminalProperties.fontLocalBrowser, TerminalProperties.readLocalBrowserFont());

		tracker.put(TerminalProperties.fontTabbed, TerminalProperties.readTabbedFont());
		tracker.put(TerminalProperties.fontCommand, TerminalProperties.readCommandPaneFont());
		tracker.put(TerminalProperties.fontMessage, TerminalProperties.readTabbedMessageFont());
		tracker.put(TerminalProperties.fontTable, TerminalProperties.readTabbedTableFont());
		tracker.put(TerminalProperties.fontGraph, TerminalProperties.readTabbedGraphFont());
		tracker.put(TerminalProperties.fontLog, TerminalProperties.readTabbedLogFont());
		// 主菜单字体
		tracker.put(TerminalProperties.fontMenu,TerminalProperties.readMainMenuFont());

		// 帮助字体类型
		String helpFamily = context.getTemplateFontName();
		tracker.put(TerminalProperties.fontHelp, helpFamily);
		// 播放声音
		boolean play = SoundPlayer.getInstance().isPlay();
		tracker.put(TerminalProperties.soundPlay, play);
		// 显示的日志数目
		tracker.put(TerminalProperties.logElements, TerminalProperties.readLogElements());
		// 拒绝显示日志
		tracker.put(TerminalProperties.logForbid, TerminalProperties.readLogForbid());
		
		// 界面皮肤颜色名称
		SkinToken token = skinLoader.findCheckedSkinToken();
		if (token != null) {
			tracker.put(TerminalProperties.skinName, token.getName());
		}

		// 配置参数写入指定的目录
		File dir = createDirectory();
		if (dir != null) {
			File file = new File(dir, "terminal.conf");
			// 写入磁盘
			tracker.write(file);
		}
	}

	/**
	 * 读写FRONT.TERMINAL节点参数配置
	 * @return 成功返回真，否则假
	 */
	private boolean readConfigure() {
		// 选择默认字体做为显示字体
		Font[] defaultFonts = readPlatformFont();
		if (defaultFonts != null) {
			context.setTemplateFontName(defaultFonts[0].getFamily());
		}

		// 读本地配置文件
		File dir = createDirectory();
		if (dir == null) {
			return false;
		}
		File file = new File(dir, "terminal.conf");

		// 读磁盘文件
		UITacker tracker = new UITacker();
		int who = tracker.read(file);
		if (who < 1) {
			return false;
		}

		// 窗口范围
		Rectangle rect = tracker.getRectangle(TerminalProperties.boundFrame); 
		if (rect != null) {
			TerminalProperties.writeWindowBound(rect);
		}
		// 中央面板分割线位置
		Integer pixel = tracker.getInteger(TerminalProperties.dividerCenterPane);
		if (pixel != null) {
			TerminalProperties.writeCenterPaneDeviderLocation(pixel.intValue());
		}
		// 浏览面板分割线位置
		pixel = tracker.getInteger(TerminalProperties.dividerBrowserPane);
		if (pixel != null) {
			TerminalProperties.writeBrowserPaneDeviderLocation(pixel.intValue());
		}
		
		// 云浏览器面板上下分割线
		pixel = tracker.getInteger(TerminalProperties.dividerRemoteDataPane);
		if (pixel != null) {
			TerminalProperties.writeRemoteDataPaneDeviderLocation(pixel.intValue());
		}
		// 云浏览器面板上下分割线
		pixel = tracker.getInteger(TerminalProperties.dividerRemoteSoftwarePane);
		if (pixel != null) {
			TerminalProperties.writeRemoteSoftwarePaneDeviderLocation(pixel.intValue());
		}
		// 本地浏览面板上下分割线
		pixel = tracker.getInteger(TerminalProperties.dividerLocalBrowserPane);
		if (pixel != null) {
			TerminalProperties.writeLocalBrowserPaneDeviderLocation(pixel.intValue());
		}
		
		// 系统环境字体
		Font font = tracker.getFont(TerminalProperties.fontSystem);
		if (font != null) {
			// 更新系统环境字体
			UITools.updateSystemFonts(font);
			TerminalProperties.writeSystemFont(font);
		}
		// 云端数据窗口字体
		font = tracker.getFont(TerminalProperties.fontRemoteData);
		if (font != null) {
			TerminalProperties.writeRemoteDataFont(font);
		}
		// 云端软件窗口字体
		font = tracker.getFont(TerminalProperties.fontRemoteSoftware);
		if (font != null) {
			TerminalProperties.writeRemoteSoftwareFont(font);
		}
		// 选项卡字体
		font = tracker.getFont(TerminalProperties.fontTabbed);
		if (font != null) {
			TerminalProperties.writeTabbedFont(font);
		}
		// 命令字体
		font = tracker.getFont(TerminalProperties.fontCommand); 
		if (font != null) {
			TerminalProperties.writeCommandPaneFont(font);
		}
		// 消息字体
		font = tracker.getFont(TerminalProperties.fontMessage);
		if (font != null) {
			TerminalProperties.writeTabbedMessageFont(font);
		}
		// 表格字体
		font = tracker.getFont(TerminalProperties.fontTable); 
		if (font != null) {
			TerminalProperties.writeTabbedTableFont(font);
		}
		// 图像状态字体
		font = tracker.getFont(TerminalProperties.fontGraph); 
		if (font != null) {
			TerminalProperties.writeTabbedGraphFont(font);
		}
		// 日志字体
		font = tracker.getFont(TerminalProperties.fontLog); 
		if (font != null) {
			TerminalProperties.writeTabbedLogFont(font);
		}
		// 主菜单字体
		font = tracker.getFont(TerminalProperties.fontMenu); 
		if (font != null) {
			TerminalProperties.writeMainMenuFont(font);
		}

		// 帮助字体类型
		String helpFamily = tracker.getString(TerminalProperties.fontHelp);
		if (helpFamily != null) {
			TerminalProperties.writeHelpMenuFontFamily(helpFamily);
			TerminalLauncher.getInstance().getCommentContext().setTemplateFontName(helpFamily);
		}
		// 声音
		Boolean play = tracker.getBoolean(TerminalProperties.soundPlay); 
		if (play != null) {
			boolean yes = play.booleanValue();
			TerminalProperties.writeSoundPlay(yes);
			SoundPlayer.getInstance().setPlay(yes);
		}
		// 日志显示数目
		Integer logs = tracker.getInteger(TerminalProperties.logElements);
		if (logs != null) {
			TerminalProperties.writeLogElements(logs.intValue());
		}
		// 拒绝显示日志
		Boolean forbid = tracker.getBoolean(TerminalProperties.logForbid);
		if (forbid != null) {
			boolean yes = forbid.booleanValue();
			TerminalProperties.writeLogForbid(yes);
		}
		
		return true;
	}

	/**
	 * 启动闪屏
	 */
	public void startSplash() {
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/splash/");
		ImageIcon icon = null;
		// 判断中文或者其它，显示！
		if (Laxkit.isSimplfiedChinese()) {
			icon = loader.findImage("zh_CN/splash.jpg");
		} else {
			icon = loader.findImage("en_US/splash.jpg");
		}

		// 设置界面
		splash.createWindow(icon);
		// 启动界面
		splash.start();
	}

	/**
	 * 启动闪屏
	 */
	public void stopSplash() {
		splash.stop();
	}

	/**
	 * FRONT.TERMINAL启动入口
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		TerminalLauncher launcher = TerminalLauncher.getInstance();
		
		// 启动闪屏
		launcher.startSplash();

		// 加载“bin”目录下面和“laxcus.library”目录下面的库文件
		JNILoader.init();

		// 将颜色加载到内存集合中
		launcher.loadColors();

		// 加载外观和皮肤
		boolean loaded = launcher.loadSkins();
		if (!loaded) {
			launcher.stopSplash();
			Logger.error("cannot be load skin theme!");
			Logger.gushing();
			System.exit(0);
			return;
		}
		
		// 读取本地资源配置
		launcher.readConfigure();

		String filename = args[0];

		// 解析登录参数，只在TERMINAL有用
		launcher.splitLogin(filename);
		// 解析
		boolean success = launcher.loadLocal(filename);
		Logger.note("TerminalLauncher.main, load local", success);
		// 启动线程
		if (success) {
			success = launcher.start();
			Logger.note("TerminalLauncher.main, start service", success);
		}
		
		// 启动限制参数
		if (success) {
			launcher.loadTimerTasks(filename);
		} else {
			launcher.stopSplash();
			Logger.gushing();
			// 关闭日志
			launcher.stopLog();
			System.exit(0);
		}
	}



}


//	/**
//	 * 显示登录提示对话框
//	 */
//	private void showForsakeDialog() {
//		// 弹出提示对话框
//		String title = TerminalLauncher.getInstance().findCaption(
//				"MessageBox/Forsake/Title/title");
//		String content = TerminalLauncher.getInstance().findCaption(
//				"MessageBox/Forsake/Message/title");
//
//		ResourceLoader loader = new ResourceLoader();
//		ImageIcon icon = loader.findImage("conf/front/terminal/image/message/failed.png", 32, 32);
//		// 显示对话杠
//		MessageDialog.showMessageBox(window, title,
//				JOptionPane.ERROR_MESSAGE, icon, 
//				content, JOptionPane.DEFAULT_OPTION);
//	}
//	
//	class ForsakeThread implements Runnable {
//		public void run() {
//			// 注销
//			window.logout();
//			// 显示登录提示对话框
//			showForsakeDialog();
//		}
//	}


//	/**
//	 * 确定线程
//	 *
//	 * @author scott.liang
//	 * @version 1.0 4/3/2020
//	 * @since laxcus 1.0
//	 */
//	class ConfirmThread extends SwingEvent {
//		/** 显示文本 **/
//		private String content;
//
//		/** 接受或者否 **/
//		private boolean accpeted;
//
//		/** 延时等待器 **/
//		private DynamicWaiter waiter;
//
//		/**
//		 * 构造线程
//		 * @param s
//		 */
//		public ConfirmThread(String s) {
//			super();
//			accpeted = false;
//			content = s;
//			waiter = new DynamicWaiter();
//		}
//
//		/**
//		 * 判断接受！
//		 * @return 返回真或者假
//		 */
//		public boolean isAccpeted() {
//			return accpeted;
//		}
//
//		/**
//		 * 等待
//		 */
//		public void await() {
//			waiter.await();
//		}
//
//		/* (non-Javadoc)
//		 * @see com.laxcus.util.display.SwingEvent#process()
//		 */
//		@Override
//		public void process() {
//			accpeted = __showConfirmDialog(content);
//			// 唤醒线程
//			waiter.done();
//		}
//	}

//	/**
//	 * 弹出窗口
//	 * @param content
//	 * @return
//	 */
//	private boolean showConfirmDialog(String content) {
//		ConfirmThread thread = new ConfirmThread(content);
//		SwingDispatcher.invokeLater(thread);
//		// 进入等待
//		thread.await();
//		// 返回结果
//		return thread.isAccpeted();
//		
//		return __showConfirmDialog(content);
//	}

//	/**
//	 * 弹出窗口
//	 * @param content
//	 * @return
//	 */
//	private boolean showConfirmDialog(String content) {
//		return __showConfirmDialog(content);
//	}


//	/**
//	 * 修改某个皮肤配色
//	 * @param name 名称
//	 * @return 成功返回真，否则假
//	 */
//	public boolean exchangeCheckedSkinToken(String name) {
//		boolean success = false;
//		java.util.List<SkinToken> tokens = skinLoader.getSkinTokens();
//		for (SkinToken e : tokens) {
//			e.setChecked(false);
//			if (e.getName().equalsIgnoreCase(name)) {
//				success = true;
//				e.setChecked(true);
//			}
//		}
//		return success;
//	}
//
//	/**
//	 * 根据名称，查找匹配的皮肤配置
//	 * @param name 名称，具有唯一性
//	 * @return 返回SkinToken实例，或者空指针
//	 */
//	public SkinToken findSkinToken(String name) {
//		return skinLoader.findSkinTokenByName(name);
//	}
//
//	/**
//	 * 输出全部皮肤配置
//	 * @return SkinToken集合
//	 */
//	public java.util.List<SkinToken> getSkinTokens() {
//		return skinLoader.getSkinTokens();
//	}
//
//	/**
//	 * 找到选中的皮肤方案名称
//	 * @return 字符串或者空指针
//	 */
//	public String getCheckedSkinName() {
//		SkinToken token = skinLoader.findCheckedSkinToken();
//		return (token != null ? token.getName() : null);
//	}


///**
// * 加载皮肤配置，同是更新外观界面
// * @return 成功返回真，否则假
// */
//private boolean loadSkins() {
//	// 1. 从配置文件中加载皮肤参数，不成功退出
//	boolean success = skinLoader.load("conf/front/terminal/skin/config.xml");
//	if (!success) {
//		return false;
//	}
//	// 2. 读取"terminal.conf"配置文件，把"skin.name"参数读取出来
//	String name = readSkinName();
//	// 3. 找到匹配的皮肤方案
//	SkinToken token = skinLoader.findSkinTokenByName(name);
//	// 没有找到，读取默认的"Nimbus"定义
//	if (token == null) {
//		// 设置为默认的“Nimbus”外观，“Metal”为暗黑！
//		success = UITools.updateLookAndFeel("Nimbus", null);
//		if (success) {
//			int count = SkinToken.loadSkins("conf/front/terminal/skin/nimbus_normal.txt");
//			success = (count > 0);
//		}
//		return success;
//	}
//
//	// 3. 切换到主题界面
//	success = token.switchTheme();
//	if (success) {
//		// 选择中
//		skinLoader.exchangeCheckedSkinToken(token.getName());
//		// 定义外观
//		Skins.setLookAndFeel(token.getLookAndFeel());
//	}
//
//	return success;
//}
