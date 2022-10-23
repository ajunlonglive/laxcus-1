/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;

import com.laxcus.command.cloud.*;
import com.laxcus.front.*;
import com.laxcus.front.terminal.component.*;
import com.laxcus.front.terminal.dialog.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.help.*;
import com.laxcus.util.help.dialog.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.login.*;
import com.laxcus.util.skin.*;

/**
 * FRONT站点终端显示窗口
 * 
 * @author scott.liang
 * @version 1.25 12/10/2014
 * @since laxcus 1.0
 */
public class TerminalWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = -9036179239567939906L;

	/** 菜单条 **/
	private JMenuBar menubar;

	/** 工具栏 **/
	private JToolBar toolbar;

	/** 浏览窗口 **/
	private TerminalLeftPanel left = new TerminalLeftPanel();

	/** 操作窗口 **/
	private TerminalRightPanel right = new TerminalRightPanel();

	/** 状态栏 **/
	private TerminalStatusPanel bottom = new TerminalStatusPanel();

	/** 登录标记 **/
	private LoginToken regToken;

	/**
	 * 从指定的JAR配置文档中增加命令标记
	 * @param name JAR档案文件
	 */
	public void addCommandTokens(String name) {
		right.addCommandTokens(name);
	}

	/**
	 * 构造默认的FRONT终端窗口
	 */
	public TerminalWindow() {
		super();
	}

	/**
	 * 设置自动登录标记
	 * @param e 自动登录标记
	 */
	public void setLoginToken(LoginToken e) {
		regToken = e;
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}
	
	/**
	 * 更新窗口外观
	 */
	private void __reloadUI() {
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	/**
	 * 更新窗口外观线程
	 *
	 * @author scott.liang
	 * @version 1.0 6/5/2021
	 * @since laxcus 1.0
	 */
	class ReloadUIThread extends SwingEvent {
		public ReloadUIThread() {
			super();
		}

		public void process() {
			__reloadUI();
		}
	}

	/**
	 * 更新UI界面
	 */
	private void reloadUI() {
		addThread(new ReloadUIThread());
	}

	/**
	 * 更新系统环境字体
	 */
	private void updateSystemFonts() {
		Font font = TerminalProperties.readSystemFont();
		if (font != null) {
			UITools.updateSystemFonts(font);
		}
	}

//	class DesktopRollingThread extends SwingEvent {
//		boolean rolling;
//
//		DesktopRollingThread(boolean b) {
//			super();
//			rolling = b;
//		}
//		public void process() {
//			bottom.doInvokerRolling(rolling);
//		}
//	}

	/**
	 * 增加发送和接收的流量
	 * @param receiveFlows 接收流量
	 * @param sendFlows 发送流量
	 */
	public void addFlows(long receiveFlows, long sendFlows) {
		bottom.addFlows(receiveFlows, sendFlows);
	}

//	/**
//	 * 收到信息后，电灯图标闪烁
//	 */
//	public void rolling(boolean start) {
//		// 增加任务数字
//		if (start) {
//			bottom.addInvokerRolling();
//		}
//
//		// 如果处于闪烁状态，忽略它
//		if (bottom.isInvokerRolling()) {
//			return;
//		}
//		// 非可视状态，忽略！
//		if (!isVisible()) {
//			return;
//		}
//
//		ArrayList<SwingEvent> array = new ArrayList<SwingEvent>();
//
//		// 线程之间的间隔时间是800毫秒
//		final int interval = 800;
//		long touchTime = System.currentTimeMillis();
//
//		// 播放动画效果
//		DesktopRollingThread light = new DesktopRollingThread(true);
//		light.setIntervalTime(interval);
//		light.setTouchTime(touchTime);
//		array.add(light);
//
//		// 下一次触发时间
//		touchTime += interval;
//
//		// 多延时200毫秒，效果更好！
//		DesktopRollingThread dark = new DesktopRollingThread(false);
//		dark.setIntervalTime(interval + 200);
//		dark.setTouchTime(touchTime + 200);
//		array.add(dark);
//
//		// 一批线程放入队列
//		SwingDispatcher.invokeThreads(array);
//	}
	
	/**
	 * 收到信息后，电灯图标闪烁
	 */
	public void rolling(boolean start) {
		// 增加任务数字
		if (start) {
			bottom.addInvokerRolling();
		}

		// 如果处于闪烁状态，忽略它
		if (bottom.isInvokerRolling()) {
			return;
		}
		// 非可视状态，忽略！
		if (!isVisible()) {
			return;
		}
		// 调用状态栏
		bottom.rolling();
	}

	/**
	 * 返回左侧云端浏览面板
	 * @return TerminalBrowserListPanel实例
	 */
	public TerminalRemoteDataListPanel getRemoteDataListPanel() {
		return left.getRemoteDataPanel().getListPanel();
	}

	/**
	 * 返回左侧云端浏览面板
	 * @return TerminalRemoteSoftwareListPanel实例
	 */
	public TerminalRemoteSoftwareListPanel getRemoteSoftwareListPanel() {
		return left.getRemoteSoftwarePanel().getListPanel();
	}

	/**
	 * 返回左侧本地浏览面板
	 * @return TerminalTubSoftwareListPanel实例
	 */
	public TerminalTubSoftwareListPanel getLocalTubListPanel() {
		return left.getLocalTubPanel().getListPanel();
	}

	/**
	 * 返回右侧的操作面板
	 * @return TerminalRightPanel实例
	 */
	public TerminalRightPanel getImplementPanel() {
		return right;
	}

	/**
	 * 设置最大日志数目
	 * @param n 日志数目
	 * @return 返回修改的日志数目
	 */
	public int setMaxLogs(int n) {
		return getLogPanel().setMaxItems(n);
	}

	/**
	 * 返回可以显示的最大日志数目
	 * @return 日志数目
	 */
	public int getMaxLogs() {
		return getLogPanel().getMaxItems();
	}

	/**
	 * 日志面板
	 * @return TerminalLogPanel实例
	 */
	public TerminalLogPanel getLogPanel() {
		return right.getLogPanel();
	}

	class TerminalWindowAdapter extends WindowAdapter {

		TerminalWindowAdapter() {
			super();
		}

		public void windowClosing(WindowEvent e) {
			Logger.debug(this, "windowClosing","Hi...");
			addThread(new ExitThread());
		}

//	    public void windowOpened(WindowEvent e) {
//	    	Logger.debug(this, "windowOpened","Hi...");
//	    }
//
//	    public void windowClosed(WindowEvent e) {
//	    	Logger.debug(this, "windowClosed","Hi...");
//	    }
//
//	    public void windowIconified(WindowEvent e) {
//	    	Logger.debug(this, "windowIconified","Hi...");
//	    }
//
//	    public void windowDeiconified(WindowEvent e) {
//	    	Logger.debug(this, "windowDeiconified","Hi...");
//	    }
//
//		public void windowActivated(WindowEvent e) {
//			Logger.debug(this, "windowActivated","Hi...");
//		}
//
//	    public void windowDeactivated(WindowEvent e) {
//	    	Logger.debug(this, "windowDeactivated","Hi...");
//	    }
//
//	    public void windowStateChanged(WindowEvent e) {
//	    	Logger.debug(this, "windowStateChanged","Hi...");
//	    }
//
//	    public void windowGainedFocus(WindowEvent e) {
//	    	Logger.debug(this, "windowGainedFocus","Hi...");
//	    }
//
//	    public void windowLostFocus(WindowEvent e) {
//	    	Logger.debug(this, "windowLostFocus","Hi...");
//	    }
	}

//	/**
//	 * 状态栏图标闪烁线程
//	 *
//	 * @author scott.liang
//	 * @version 1.0 3/2/2020
//	 * @since laxcus 1.0
//	 */
//	class TerminalFlashThread extends SwingEvent {
//		/** 图标ID **/
//		int id;
//
//		public TerminalFlashThread(int who) {
//			super();
//			id = who;
//		}
//
//		/* (non-Javadoc)
//		 * @see com.laxcus.util.display.SwingEvent#process()
//		 */
//		@Override
//		public void process() {
//			bottom.doThreadFlash(id);
//		}
//	}
//
//	/**
//	 * 被TerminalLauncher调用，显示动态图标！
//	 */
//	public void flash() {
//		// 非可视状态，忽略！
//		if (!isVisible()) {
//			return;
//		}
//
//		ArrayList<SwingEvent> array = new ArrayList<SwingEvent>();
//
//		// 图标编号
//		int[] icons = new int[] { TerminalFlashTag.FLASH_START, TerminalFlashTag.FLASH_NEXT };
//
//		// 线程之间的间隔时间是800毫秒
//		final int interval = 800;
//		long touchTime = System.currentTimeMillis();
//
//		// 播放动画效果
//		for (int skip = 0; skip < 1; skip++) {
//			for (int i = 0; i < icons.length; i++) {
//				TerminalFlashThread event = new TerminalFlashThread(icons[i]);
//				event.setIntervalTime(interval);
//				event.setTouchTime(touchTime);
//				array.add(event);
//				// 下一次触发时间
//				touchTime += interval;
//			}
//		}
//
//		// 多延时200毫秒，效果更好！
//		TerminalFlashThread last = new TerminalFlashThread(TerminalFlashTag.FLASH_STOP);
//		last.setIntervalTime(interval + 200);
//		last.setTouchTime(touchTime + 200);
//		array.add(last);
//
//		// 一批线程放入队列
//		SwingDispatcher.invokeThreads(array);
//	}
	
	/**
	 * 被TerminalLauncher调用，显示动态图标！
	 */
	public void flash() {
		// 非可视状态，忽略！
		if (!isVisible()) {
			return;
		}
		bottom.flash();
	}

	/**
	 * 清空浏览面板上的内容
	 */
	public void clearRemoteBrowser() {
		left.clearRemoteBrowser();
	}

	/**
	 * 让登录菜单生效
	 * @param enabled 生效
	 */
	public void setLoginMenuEnabled(boolean enabled) {
		// 登录菜单
		JMenuItem item = findMenuItem("login");
		item.setEnabled(enabled);
		// 注销菜单
		item = findMenuItem("logout");
		item.setEnabled(!enabled);
		// 重新登录菜单
		item = findMenuItem("relogin");
		item.setEnabled(!enabled);
	}

	/**
	 * 设置在线状态图标或者否
	 * @param online 在线状态
	 */
	public void setOnlineIcon(boolean online) {
		if (online) {
			setConnectIcon();
		} else {
			setInterruptIocn();
		}
	}

	/**
	 * 设置中断图标
	 */
	private void setInterruptIocn() {
		bottom.setDisconnectIcon();
	}

	/**
	 * 设置已经连接图标
	 */
	private void setConnectIcon() {
		bottom.setConnectIcon();
	}

	/**
	 * 设置HUB图标提示
	 * @param text
	 */
	public void setHubTip(String text) {
		bottom.setHubTip(text);
	}

	/**
	 * 设置管理员图标
	 */
	public void setAdministratorIcon() {
		bottom.setAdministratorIcon();
	}

	/**
	 * 设置普通注册用户图标
	 */
	public void setUserIcon() {
		bottom.setUserIcon();
	}

	/**
	 * 无定义的身份图标
	 */
	public void setNobodyIcon() {
		bottom.setNobodyIcon();
	}

	/**
	 * 设置状态栏文本提示
	 * @param text
	 */
	public void setStatusText(String text) {
		bottom.setTip(text);
	}

	/**
	 * 登录目标站点
	 * @return 成功返回真，否则假
	 */
	protected boolean __login() {
		Logger.debug(this, "__login", "into ...");

		// 显示前端的注册窗口
		TerminalLoginDialog dialog = new TerminalLoginDialog(this);
		dialog.setModal(true);
		dialog.showDialog(regToken);
		dialog.dispose();
		// 注册窗口取消向GATE节点注册操作
		if (dialog.isCanceled()) {
			return false;
		}

		// 设置服务器地址
		Node hub = TerminalLauncher.getInstance().getHub();
		setHubTip(hub.toString());
		// 登录菜单失效！
		setLoginMenuEnabled(false);
		// 图标显示用户属性
		showCyberStatus();

		Logger.debug(this, "__login", "hub is %s", hub);

		return true;
	}

	class LoginThread extends SwingEvent {
		public LoginThread(){
			super();
		}
		public void process() {
			__login();
		}
	}

	class LogoutThread extends SwingEvent {
		public LogoutThread(){
			super();
		}
		public void process() {
			__disableWindow();
		}
	}

	class ForsakeThread extends SwingEvent {
		public ForsakeThread() {
			super();
		}
		@Override
		public void process() {
			// 清除窗口
			__disableWindow();
			// 显示对话框
			showPrivateForsakeDialog();
		}
	}

	/**
	 * 注销线程
	 */
	protected void doLogoutThread() {
		addThread(new LogoutThread());
	}

	/**
	 * 重新启动注册线程
	 */
	protected void doReloginThread() {
		addThread(new LogoutThread());
		addThread(new LoginThread());
	}

	/**
	 * 故障显示
	 */
	public void doForsakeThread() {
		addThread(new ForsakeThread());
	}

	/**
	 * 显示登录提示对话框
	 */
	private void showPrivateForsakeDialog() {
		// 弹出提示对话框
		String title = TerminalLauncher.getInstance().findCaption(
		"MessageBox/Forsake/Title/title");
		String content = TerminalLauncher.getInstance().findCaption(
		"MessageBox/Forsake/Message/title");

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/front/terminal/image/message/failed.png", 32, 32);
		// 显示对话杠
		MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, 
				icon, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 将窗口的显示单元置于无效状态
	 */
	private void __disableWindow() {
		// 清除左侧的云端面板，本地面板不变
		clearRemoteBrowser();
		// 清除右侧的面板
		right.clear();

		setNobodyIcon();
		setInterruptIocn();
		setStatusText(null);

		// 图形界面显示注销状态。这时的登录菜单生效，退出登录/重新登录失效
		setLoginMenuEnabled(true);
	}
	
	/**
	 * 设置自动登录信息
	 * @param xmlPath XML标签路径
	 * @param hub 服务器地址
	 */
	private void setAutoText(String xmlPath) {
		String content = TerminalLauncher.getInstance().findContent(xmlPath);
		if (content != null) {
			setStatusText(content);
		}
	}

	/**
	 * 自动登录，流程 <br><br>
	 * 1. 清除UI界面 <br>
	 * 2. 启动登录 <br>
	 * 3. 如果成功，重新设置基础参数<br><br>
	 * 
	 * @param entrance ENTRANCE服务器地址
	 * @return 成功返回真，失败返回假
	 */
	public boolean __auto_login(Node entrance) {
		// 清除图形界面
		__disableWindow();
		
		// 显示自动登录
		setAutoText("Window/auto-login/retry");

		// 登录ENTRANCE服务器，分配资源！
		int who = TerminalLauncher.getInstance().login(entrance.getHost(), true, null);
		// 判断成功
		boolean success = FrontEntryFlag.isSuccessful(who);
		// 不成功，保存初始HUB地址
		if (!success) {
			TerminalLauncher.getInstance().setInitHub(entrance);
			setAutoText("Window/auto-login/failed");
			return false;
		}
		
		// 设置GATE服务器地址
		setHubTip(TerminalLauncher.getInstance().getHub().toString());
		// 登录成功后，登录菜单失效，注销和重新登录有效
		setLoginMenuEnabled(false);
		// 图标显示用户属性
		showCyberStatus();

		setAutoText("Window/auto-login/successful");

		Logger.debug(this, "__auto_login", "login successful! entrance hub is %s", entrance);

		return true;
	}

	/**
	 * 登录目标站点
	 */
	protected void login() {
		addThread(new LoginThread());
	}

	/**
	 * 注销。这个方法是菜单驱动
	 * 顺序：先关闭网络和资源，再清除界面。
	 */
	protected void logout() {
		// 关闭与所有的连接，包括：GAET、被授权GATE、CALL站点
		TerminalLogoutThread thread = new TerminalLogoutThread(this);
		thread.start();
	}

	/**
	 * 重新注册。先注销，再注册
	 */
	protected void relogin() {
		TerminalReloginThread thread = new TerminalReloginThread(this);
		thread.start();
	}

	/**
	 * 退出线程
	 *
	 * @author scott.liang
	 * @version 1.0 4/3/2020
	 * @since laxcus 1.0
	 */
	class ExitThread extends SwingEvent {

		public ExitThread() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.laxcus.util.display.SwingEvent#process()
		 */
		@Override
		public void process() {
			exit();
		}
	}

	/**
	 * 退出运行
	 */
	private void exit() {
		String title = TerminalLauncher.getInstance().findCaption(
		"MessageBox/Exit/Title/title");
		String content = TerminalLauncher.getInstance().findCaption(
		"MessageBox/Exit/Message/title");

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/front/terminal/image/message/exit.png", 32, 32);

		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, icon, content,
				JOptionPane.YES_NO_OPTION);
		if (who != JOptionPane.YES_OPTION) {
			return;
		}

		// 停止线程
		bottom.stopDaemonThread();
		// 更新界面参数
		updateConfigure();

		// 关闭窗口
		setVisible(false);
		// 通知进程退出
		TerminalLauncher.getInstance().stop();
	}

	/**
	 * 返回面板的分割位置
	 * @return 返回大于0的正整数，否则是-1
	 */
	private int getDividerLocation() {
		// 对应"initControls"方法
		Container container = getContentPane();

		// 逐个检测
		Component[] elements = container.getComponents();
		int size = (elements != null && elements.length > 0 ? elements.length : 0);
		for (int i = 0; i < size; i++) {
			Component element = elements[i];
			if (element.getClass() == JSplitPane.class) {
				JSplitPane pane = (JSplitPane) element;
				int pixel = pane.getDividerLocation();
				return pixel;
			}
		}
		return -1;
	}

	/**
	 * 读取界面参数，导入到系统的内存配置集合里
	 */
	private void updateConfigure() {
		// 定义范围
		Rectangle rect = new Rectangle(getLocationOnScreen(), getSize());
		TerminalProperties.writeWindowBound(rect);

		// 中央窗口分割线位置，在像素为单位！
		int pixel = right.getDividerLocation();
		TerminalProperties.writeCenterPaneDeviderLocation(pixel);

		// 当前浏览窗口分割线位置
		pixel = getDividerLocation();
		TerminalProperties.writeBrowserPaneDeviderLocation(pixel);

		// 站点浏览面板
		pixel = left.getRemoteDataDividerLocation();
		TerminalProperties.writeRemoteDataPaneDeviderLocation(pixel);

		// 成员浏览面板
		pixel = left.getLocalBrowserDividerLocation();
		TerminalProperties.writeLocalBrowserPaneDeviderLocation(pixel);
	}

	/**
	 * 检查命令语法
	 */
	public void doCheckCommand() {
		right.check();
	}

	/**
	 * 执行命令
	 */
	public void doLaunchCommand() {
		right.execute();
	}

	class DisableBrowerThread extends SwingEvent {
		// 窗口
		TerminalWindow me;

		public DisableBrowerThread(TerminalWindow e) {
			super();
			me = e;
		}

		public void process() {
			// 弹出提示对话框
			String title = TerminalLauncher.getInstance().findCaption("MessageBox/Brower/Title/title");
			String content = TerminalLauncher.getInstance().findCaption("MessageBox/Brower/Message/title");

			ResourceLoader loader = new ResourceLoader();
			ImageIcon icon = loader.findImage("conf/front/terminal/image/message/warning.png", 32, 32);

			// 显示对话框
			MessageDialog.showMessageBox(me, title, JOptionPane.ERROR_MESSAGE, icon, content,
					JOptionPane.DEFAULT_OPTION);
		}
	}

	/**
	 * 切换菜单
	 * @param method 焦点方法
	 */
	private void exchangeSkin(final String method) {
		SkinTokenLoader loader = TerminalLauncher.getInstance().getSkinLoader();
		SkinToken token = loader.findSkinTokenByMethod(method);
		// 没有找到，忽略不处理
		if (token == null) {
			return;
		}
		// 皮肤名称
		String skinName = token.getName();
		// 全部方法名
		String[] methods = loader.getSkinMethods();

		// 调整菜单
		JMenuItem[] items = findMenuItems(methods);
		JMenuItem item = findMenuItem(method);
		// 全部选择是假
		for (int i = 0; i < items.length; i++) {
			items[i].setSelected(false);
		}
		if (item != null) {
			item.setSelected(true);
		}

		// 切换主题界面！
		boolean success = token.updateTheme(true);
		// 保存焦点
		if (success) {
			// 重置选中的的皮肤方案
			loader.exchangeCheckedSkinToken(skinName);
			// 记录界面外观
			Skins.setLookAndFeel(token.getLookAndFeel());
			Skins.setSkinName(token.getName());

			// 更新系统环境字体！
			updateSystemFonts();
			// 重置UI界面
			reloadUI();
		}
	}

	/**
	 * 此方法匹配"conf/font/terminal/skin/config.xml"中的"method"定义
	 */
	public void doSkinNormal() {
		exchangeSkin("doSkinNormal");
	}

	/**
	 * 暗黑色Metal界面
	 * 此方法匹配"conf/watch/skin/config.xml"中的"method"定义
	 */
	public void doSkinGray() {
		exchangeSkin("doSkinGray");
	}

	/**
	 * 此方法匹配"conf/font/terminal/skin/config.xml"中的"method"定义
	 */
	public void doSkinDark() {
		exchangeSkin("doSkinDark");
	}

	/**
	 * 青铜色Metal界面
	 * 此方法匹配"conf/front/terminal/skin/config.xml"中的"method"定义
	 */
	public void doSkinBronze() {
		exchangeSkin("doSkinBronze");
	}

	/**
	 * 深兰Metal界面
	 * 此方法匹配"conf/front/terminal/skin/config.xml"中的"method"定义
	 */
	public void doSkinCyano() {
		exchangeSkin("doSkinCyano");
	}

	/**
	 * 打开网站
	 */
	public void doHome() {
		// 判断支持桌面
		boolean success = Desktop.isDesktopSupported();
		// 获得桌面实例
		if (success) {
			String url = TerminalLauncher.getInstance().findCaption("MessageBox/Brower/URI/title");
			DesktopBrowerThread e = new DesktopBrowerThread(url);
			e.start();
		} else {
			addThread(new DisableBrowerThread(this));
		}
	}

	/**
	 * 显示错误消息
	 * @param content
	 * @param title
	 */
	private void showWarming(String content, String title) {
		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/front/terminal/image/message/warning.png", 32, 32);

		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, icon, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 应用软件商店
	 */
	public void doStore() {
		showWarming("抱歉！因为版本原因，LAXCUS应用软件商店暂不开放！", getTitle());
	}

	/**
	 * 修改窗口字体
	 * @param font
	 * @return 返回新的字体
	 */
	private Font doFont(Font font) {
		// 构造字体窗口
		FontSelectDialog dialog = new FontSelectDialog(this, true, TerminalLauncher.getInstance(), font);
		// 显示窗口
		dialog.setVisible(true);
		// 选择新字体，也可能是空指针
		Font select = dialog.getSelectFont();
		// 关闭窗口
		dialog.dispose();
		// 返回结果
		return select;
	}

	/**
	 * 修改TERMINAL节点环境字体
	 */
	public void doSystemMenuFont() {
		Font font = TerminalProperties.readSystemFont();
		Font select = doFont(font);
		if (select != null) {
			// 字体在12 - 18磅之间
			if (select.getSize() < 12) {
				select = new Font(select.getFamily(), select.getStyle(), 12);
			} else if (select.getSize() > 18) {
				select = new Font(select.getFamily(), select.getStyle(), 18);
			}
			TerminalProperties.writeSystemFont(select);
		}
	}

	/**
	 * 修改主菜单字体
	 */
	public void doMainMenuFont() {
		Font font = menubar.getFont();
		Font select = doFont(font);
		if (select != null) {
			TerminalProperties.writeMainMenuFont(select);
			updateMainMenuFont(select); // 更新字体
		}
	}

	/**
	 * 设置云端数据浏览窗口字体
	 */
	public void doRemoteDataWindowFont() {
		TerminalRemoteDataPanel panel = left.getRemoteDataPanel();
		Font font = panel.getListPanel().getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			// 保存字体
			TerminalProperties.writeRemoteDataFont(select);
			// 界面修改成新的字体
			panel.setSelectFont(select);
		}
	}

	/**
	 * 设置云端应用软件浏览窗口字体
	 */
	public void doRemoteSoftwareWindowFont() {
		TerminalRemoteSoftwarePanel panel = left.getRemoteSoftwarePanel();
		Font font = panel.getListPanel().getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			// 保存字体
			TerminalProperties.writeRemoteSoftwareFont(select);
			// 界面修改成新的字体
			panel.setSelectFont(select);
		}
	}

	/**
	 * 设置本地浏览窗口字体
	 */
	public void doLocalBrowserWindowFont() {
		TerminalLocalTubPanel panel = left.getLocalTubPanel();
		Font font = panel.getListPanel().getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			// 保存字体
			TerminalProperties.writeLocalBrowserFont(select);
			// 界面修改成新的字体
			panel.setSelectFont(select);
		}
	}

	/**
	 * TAB选项卡字体！
	 */
	public void doTabbedWindowFont() {
		Font font = left.getTabbedSelectFont();
		Font select = doFont(font);
		if(select != null) {
			// 保存字体
			TerminalProperties.writeTabbedFont(select);
			// 字体
			left.setTabbedSelectFont(select);
			right.getMixedPanel().setTabbedSelectFont(select);
		}
	}

	/**
	 * 修改命令窗口字体
	 */
	public void doCommandWindowFont() {
		TerminalCommandPane panel = right.getCommandPanel();
		Font font = panel.getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			TerminalProperties.writeCommandPaneFont(select);
			panel.setSelectFont(select);
		}
	}

	/**
	 * 修改提示面板字体
	 */
	public void doPromptWindowFont() {
		TerminalMessagePanel panel = right.getMessagePanel();
		Font font = panel.getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			TerminalProperties.writeTabbedMessageFont(select);
			panel.setSelectFont(select);
		}
	}

	/**
	 * 修改表格面板字体
	 */
	public void doTableWindowFont() {
		TerminalTablePanel panel = right.getTablePanel();
		Font font = panel.getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			TerminalProperties.writeTabbedTableFont(select);
			panel.setSelectFont(select);
		}
	}

	/**
	 * 修改图形面板字体
	 */
	public void doGraphWindowFont() {
		TerminalGraphPanel panel = right.getGraphPanel();
		Font font = panel.getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			TerminalProperties.writeTabbedGraphFont(select);
			panel.setSelectFont(select);
		}
	}

	/**
	 * 修改日志面板字体
	 */
	public void doLogWindowFont() {
		TerminalLogPanel panel = right.getLogPanel();
		Font font = panel.getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			TerminalProperties.writeTabbedLogFont(select);
			panel.setSelectFont(select);
		}
	}

	/**
	 * 修改帮助字体
	 */
	public void doHelpFont() {
		CommentContext context = TerminalLauncher.getInstance().getCommentContext();
		Font font = new Font(context.getTemplate().getFontName(), Font.PLAIN, 14);
		Font select = doFont(font);
		if (select != null) {
			TerminalProperties.writeHelpMenuFontFamily(select.getFamily());
			context.getTemplate().setFontName(select.getFamily());
		}
	}

	/**
	 * 更新数据库的显示资源
	 * 这个方法名在配置中设置，不能随便修改。如果修改，两处都要改动。
	 */
	public void doRefresh() {
		// 判断已经注册
		boolean success = TerminalLauncher.getInstance().isLogined();

		// 如果在登录状态，更新显示内容
		if (success) {
			success = right.doRefreshCyber();
			if (success) {
				// 清除云端界面显示的资源配置
				clearRemoteBrowser();
			}
		}
	}

	/**
	 * 调整状态栏图标显示
	 */
	public void showCyberStatus() {
		TerminalLauncher launcher = TerminalLauncher.getInstance();
		boolean logined = launcher.isLogined();

		if (logined) {
			// 连接图标
			setConnectIcon();
			// 选择管理员/用户图标
			if (launcher.isAdministrator()) {
				setAdministratorIcon();
			} else if (launcher.isUser()) {
				setUserIcon();
			} 
			// 特别情况！此时尚没有定义用户状态!
			else {
				setNobodyIcon();
			}
		} else {
			setNobodyIcon();
			setInterruptIocn();
		}
	}

	class ActionThread extends SwingEvent {
		ActionEvent event;

		ActionThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			click(event);
		}
	}

	/**
	 * 单击操作
	 * @param e
	 */
	private void click(ActionEvent e) {
		Object source = e.getSource();
		if (source instanceof JMenuItem) {
			JMenuItem item = (JMenuItem) source;
			String methodName = item.getName();
			invoke(methodName);
		} else if (source.getClass() == JButton.class) {
			JButton button = (JButton) source;
			String methodName = button.getName();
			invoke(methodName);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new ActionThread(e));
	}

	/**
	 * 根据方法名称，调用对应的方法。这些方法必须已经在当前类中定义
	 * @param methodName 方法名称
	 */
	private void invoke(String methodName) {
		if (methodName == null || methodName.isEmpty()) {
			return;
		}

		try {
			Method method = (getClass()).getDeclaredMethod(methodName, new Class<?>[0]);
			method.invoke(this, new Object[0]);
		} catch (NoSuchMethodException e) {
			Logger.error(e);
		} catch (IllegalArgumentException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		} catch (InvocationTargetException e) {
			Logger.error(e);
		}
	}

	/**
	 * 根据方法名称查找菜单项
	 * @param method 调用的方法名称
	 * @return 返回菜单项实例，没有找到是空指针
	 */
	private JMenuItem findMenuItem(String method) {
		MenuBarCreator creator = new MenuBarCreator();
		return creator.findMenuItem(menubar, method);
	}

	/**
	 * 根据方法名，查找一组菜单项 
	 * @param methods 调用的方法名称数组
	 * @return 返回菜单数组
	 */
	private JMenuItem[] findMenuItems(String[] methods) {
		JMenuItem[] items = new JMenuItem[methods.length];
		for (int i = 0; i < methods.length; i++) {
			items[i] = findMenuItem(methods[i]);
		}
		return items;
	}
	
	/** 菜单栏高度 **/
	private int menubarHeight = 0;

	/** 显示菜单栏 **/
	private boolean showMenubar = true;
	
	/**
	 * 判断是显示菜单栏
	 * @return
	 */
	public boolean isShowMenubar() {
		return showMenubar;
	}

	/**
	 * 显示或者隐藏菜单栏
	 */
	public void doShowOrHideMenubar() {
		if (!menubar.isVisible()) {
			return;
		}

		// 隐藏菜单栏
		JMenuItem item = findMenuItem("doShowOrHideMenubar");
		// 菜单项有效！
		if (item == null) {
			return;
		}

		// 菜单正常显示，把它隐藏起来
		if (showMenubar) {
			menubarHeight = menubar.getHeight();
			showMenubar = false;
			// 隐藏菜单
			((JCheckBoxMenuItem) item).setState(false);
			menubar.setVisible(false);
		}
		// 已经手动隐藏菜单，恢复显示它
		else {
			// 显示菜单
			menubarHeight = 0;
			showMenubar = true;
			// 正确显示菜单
			((JCheckBoxMenuItem) item).setState(true);
		}
	}

	/**
	 * 检测追踪鼠标，通过鼠标位置判断菜单条的显示
	 */
	private void addMouseMotion() {
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				// 如果是正常显示，忽略它
				if (showMenubar) {
					return;
				}
				// 取它的Y坐标
				int y = e.getY();
				// 显示边框
				boolean border = (!fullScreen && !attachScreen);
				// 有边框的时候...
				if (border) {
					// 标题栏高度
					Dimension d1 = getSize();
					Dimension d2 = getContentPane().getSize();
					int titleHeight = d1.height - d2.height;
					// 边栏减少N个像素，这个好象没有被JAVA API统计?
					titleHeight -= 4;

					// 在范围内...
					if (titleHeight <= y && y <= titleHeight + 20) {
						boolean on = menubar.isVisible();
						if (!on) {
							menubar.setVisible(true); // 显示菜单栏
						}
					}
					// 不在范围内...
					else {
						boolean on = menubar.isVisible();
						if (on) {
							menubar.setVisible(false); // 隐藏菜单栏
						}
					}
				}
				// 没有边框的时候
				else {
					// 在范围内...
					if (y >= 0 && y < menubarHeight) {
						boolean on = menubar.isVisible();
						if (!on) {
							menubar.setVisible(true); // 显示菜单栏
						}
					}
					// 不在范围内...
					else {
						boolean on = menubar.isVisible();
						if (on) {
							menubar.setVisible(false); // 隐藏菜单栏
						}
					}
				}
			}
		});
	}

//	/**
//	 * 检测追踪鼠标，通过鼠标位置判断菜单条的显示
//	 */
//	private void addMouseMotion() {
//		addMouseMotionListener(new MouseMotionAdapter() {
//			public void mouseMoved(MouseEvent e) {
//				// 如果是正常显示，忽略它
//				if (showMenubar) {
//					return;
//				}
//
//				// 取它的Y坐标
//				int y = e.getY();
//				// 在范围内...
//				if (y < menubarHeight) {
//					boolean on = menubar.isVisible();
//					if (!on) {
//						menubar.setVisible(true); // 显示菜单栏
//					}
//				}
//				// 不在范围内...
//				else {
//					boolean on = menubar.isVisible();
//					if (on) {
//						menubar.setVisible(false); // 隐藏菜单栏
//					}
//				}
//			}
//		});
//	}

	/**
	 * 显示或者隐藏工具栏
	 */
	public void doShowOrHideToolbar() {
		JMenuItem item = findMenuItem("doShowOrHideToolbar");
		if (item != null) {
			boolean on = toolbar.isVisible();
			toolbar.setVisible(!on);
			((JCheckBoxMenuItem) item).setState(!on);
		}
	}

	/**
	 * 显示或者隐藏状态栏
	 */
	public void doShowOrHideStatusbar() {
		JMenuItem item = findMenuItem("doShowOrHideStatusbar");
		if (item != null) {
			boolean on = bottom.isVisible();
			bottom.setVisible(!on);
			((JCheckBoxMenuItem) item).setState(!on);
		}
	}

	/**
	 * 调整日志显示状态
	 * @param select
	 * @param unselects
	 */
	private void doChangeLogState(String select) {
		String[] all = new String[] { "doDebug", "doWarning", "doInfo",
				"doError", "doFatal" };
		JMenuItem[] items = findMenuItems(all);
		JMenuItem item = findMenuItem(select);

		for (int i = 0; i < items.length; i++) {
			items[i].setSelected(false);
		}

		item.setSelected(true);
	}

	/**
	 * 调整日志到DEBUG状态
	 */
	public void doDebug() {
		doChangeLogState("doDebug");
		Logger.setLevel(LogLevel.DEBUG);
	}

	/**
	 * 调整日志到WARNING状态
	 */
	public void doWarning() {
		doChangeLogState("doWarning");
		Logger.setLevel(LogLevel.WARNING);
	}

	/**
	 * 调整日志到INFORMATION状态
	 */
	public void doInfo() {
		doChangeLogState("doInfo");
		Logger.setLevel(LogLevel.INFO);
	}

	/**
	 * 调整日志到ERROR状态
	 */
	public void doError() {
		doChangeLogState("doError");
		Logger.setLevel(LogLevel.ERROR);
	}

	/**
	 * 调整日志到FATAL状态
	 */
	public void doFatal() {
		doChangeLogState("doFatal");
		Logger.setLevel(LogLevel.FATAL);
	}

	/**
	 * 清除提示信息
	 */
	public void doClearNote() {
		right.doClearNote();
	}

	/**
	 * 清除执行结果记录
	 */
	public void doClearDetail() {
		right.doClearTable();
	}

	/**
	 * 清除窗口界面的图形
	 */
	public void doClearGraph() {
		right.doClearGraph();
	}

	/**
	 * 清除日志记录
	 */
	public void doClearLog() {
		right.doClearLog();
	}

	/** 全屏标记 **/
	private boolean fullScreen = false;

	/** 窗口在屏幕的局部范围 **/
	private Rectangle brush = null;

	/**
	 * 调整屏幕范围，全屏或者否
	 * @param full 全屏
	 */
	private void adjustScreen(boolean full) {
		// 调整此窗口的大小，以适合其子组件的首选大小和布局
		pack();
		// 全屏或者否
		if (full) {
			Dimension d = getToolkit().getScreenSize();
			Rectangle rect = new Rectangle(0, 0, d.width, d.height);
			setBounds(rect);
		} else {
			// 局部屏幕有效，否则取默认值
			if (brush != null) {
				setBounds(brush);
			} else {
				Rectangle rect = getDefaultBounds();
				setBounds(rect);
			}
		}
	}

	/**
	 * 全屏显示
	 */
	private void yesFullScrren() {
		// 关闭显示
		setVisible(false);
		// 必须先销毁窗口，这是必须的，否则调用下一句出错
		dispose();
		// 无边框
		try {
			setUndecorated(true);
		} catch (IllegalComponentStateException e) {
			setVisible(true);
			return;
		}

		// 调整屏幕到全屏状态，且无边框
		adjustScreen(true);
		// 恢复显示
		setVisible(true);
	}

	/**
	 * 局部屏幕显示
	 */
	private void noFullScrren() {
		// 关闭显示
		setVisible(false);
		// 必须先销毁窗口，这是必须！否则调用下一句出错
		dispose();
		// 恢复边框
		try {
			setUndecorated(false);
		} catch (IllegalComponentStateException e) {
			setVisible(true);
			return;
		}

		// 调整屏幕到局部状态，且有边框
		adjustScreen(false);
		// 恢复显示
		setVisible(true);
	}

	/**
	 * 全屏幕显示或者恢复
	 */
	public void doFullScreen() {
		JMenuItem item = findMenuItem("doFullScreen");
		// 设置到相反状态
		if (item != null) {
			((JCheckBoxMenuItem) item).setState(!fullScreen);
		}

		// 不是全屏，设置到全屏模式
		if (!fullScreen) {
			brush = getBounds(); // 取系统中的屏幕尺寸
			yesFullScrren(); // 调整到全屏
		}
		// 是全屏幕模式，恢复到原来的窗口尺寸
		else {
			noFullScrren();
		}

		// 设置状态
		fullScreen = !fullScreen;

		// 磁贴屏幕假
		item = findMenuItem("doAttachScreen");
		if (item != null) {
			((JCheckBoxMenuItem) item).setState(attachScreen = false);
		}
	}

	/** 磁贴屏幕 **/
	private boolean attachScreen = false;
	
	/**
	 * 磁贴屏幕
	 */
	private void yesAttachScrren() {
		// 关闭显示
		setVisible(false);
		// 必须先销毁窗口，这是必须的，否则调用下一句出错
		dispose();
		// 无边框
		try {
			setUndecorated(true);
		} catch (IllegalComponentStateException e) {
			setVisible(true);
			return;
		}

		// 恢复显示
		setVisible(true);
	}

	/**
	 * 非磁贴屏幕
	 */
	private void noAttachScrren() {
		// 关闭显示
		setVisible(false);
		// 必须先销毁窗口，这是必须！否则调用下一句出错
		dispose();
		// 恢复边框
		try {
			setUndecorated(false);
		} catch (IllegalComponentStateException e) {
			setVisible(true);
			return;
		}

		// 恢复显示
		setVisible(true);
	}
	
	/**
	 * 磁巾屏幕显示或者恢复
	 */
	public void doAttachScreen() {
		JMenuItem item = findMenuItem("doAttachScreen");
		// 设置到相反状态
		if (item != null) {
			((JCheckBoxMenuItem) item).setState(!attachScreen);
		}

		// 不是磁贴屏幕，调整到磁贴屏幕状态
		if (!attachScreen) {
			brush = getBounds(); // 取当前系统的屏幕尺寸
			yesAttachScrren(); // 调整到全屏
		}
		// 是磁贴屏幕模式，恢复到原来的窗口尺寸
		else {
			noAttachScrren();
		}

		// 设置状态
		attachScreen = !attachScreen;
		
		// 全屏幕“假”状态
		item = findMenuItem("doFullScreen");
		if (item != null) {
			((JCheckBoxMenuItem) item).setState(fullScreen = false);
		}
	}

	/**
	 * 解析标签
	 * @param xmlPath
	 * @return
	 */
	private String getCaption(String xmlPath) {
		return TerminalLauncher.getInstance().findCaption(xmlPath);
	}

	/**
	 * 选择一个文件
	 * @param description 描述文字
	 * @param extensions 选项参数
	 * @return
	 */
	private File choiceSingleFile(String title, String buttonText,
			String description, String... extensions) {
		JFileChooser chooser = new JFileChooser();
		if (description != null && extensions != null) {
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					description, extensions);
			chooser.setFileFilter(filter);
		}

		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);

		chooser.setApproveButtonText(buttonText); 
		int val = chooser.showOpenDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		// 返回选择的文件
		return chooser.getSelectedFile();
	}

	/**
	 * 上传数据文件到集群
	 */
	public void doImportEntity() {
		TerminalImportEntityDialog dialog = new TerminalImportEntityDialog(this, true);
		dialog.showDialog();
		// 处理结果
		String syntax = dialog.getResult(); // "IMPORT ENTITY ";
		if (syntax != null) {
			right.doImportEntity(syntax);
		}
	}

	/***
	 * 从集群下载数据块文件
	 */
	public void doExportEntity() {
		TerminalExportEntityDialog dialog = new TerminalExportEntityDialog(this, true);
		dialog.showDialog();
		// 处理结果
		String syntax = dialog.getResult();
		if (syntax != null) {
			right.doExportEntity(syntax);
		}
	}

	/**
	 * 检测文件字符集
	 */
	public void doCheckEntityCharset() {
		TerminalCheckEntityCharsetDialog dialog = new TerminalCheckEntityCharsetDialog(this, true);
		dialog.showDialog();
		// 处理结果
		String syntax = dialog.getResult(); 
		if (syntax != null) {
			right.doCheckEntityCharset(syntax);
		}
	}

	/**
	 * 检测文件内容
	 */
	public void doCheckEntityContent() {
		TerminalCheckEntityContentDialog dialog = new TerminalCheckEntityContentDialog(this, true);
		dialog.showDialog();
		// 处理结果
		String syntax = dialog.getResult(); 
		if (syntax != null) {
			right.doCheckEntityContent(syntax);
		}
	}

	/**
	 * 生成分布计算云应用包
	 */
	public void doBuildConductPackage() {
		TerminalBuildCloudPackageDialog dialog = new TerminalBuildCloudPackageDialog(
				this, true, BuildCloudPackageTag.CONDUCT);
		// 打开对话框
		boolean accepted = dialog.showDialog();
		if (accepted) {
			String reader = dialog.getReader();
			String writer = dialog.getWriter();
			// 生成命令来执行
			String syntax = String.format("BUILD CONDUCT PACKAGE %s IMPORT BY %s", writer, reader);
			right.doBuildConductPackage(syntax);
		}
	}

	/**
	 * 发布分布计算云应用软件包
	 */
	public void doDeployConductPackage() {
		TerminalDeployCloudPackageDialog dialog = new TerminalDeployCloudPackageDialog(
				this, true, BuildCloudPackageTag.CONDUCT);
		// 打开对话框
		boolean accepted = dialog.showDialog();
		if (accepted) {
			String writer = dialog.getReader();
			boolean local = dialog.isLocal();
			// 生成命令来执行
			String syntax = String.format("DEPLOY CONDUCT PACKAGE %s %s", writer, (local ? "TO LOCAL" : ""));
			right.doDeployConductPackage(syntax);
		}
	}

	/**
	 * 生成数据构建应用软件包
	 */
	public void doBuildEstablishPackage() {
		TerminalBuildCloudPackageDialog dialog = new TerminalBuildCloudPackageDialog(
				this, true, BuildCloudPackageTag.ESTABLISH);
		// 打开对话框
		boolean accepted = dialog.showDialog();
		if (accepted) {
			String reader = dialog.getReader();
			String writer = dialog.getWriter();
			// 生成命令来执行
			String syntax = String.format("BUILD ESTABLISH PACKAGE %s IMPORT BY %s", writer, reader);
			right.doBuildEstablishPackage(syntax);
		}
	}

	/**
	 * 生成CONDUCT应用软件包脚本文件
	 */
	public void doCreateConductScript() {
		TerminalCreateConductScriptDialog dialog = new TerminalCreateConductScriptDialog(this, true);
		dialog.showDialog();
	}

	/**
	 * 生成ESTABLISH应用软件包脚本文件
	 */
	public void doCreateEstablishScript() {
		TerminalCreateEstablishScriptDialog dialog = new TerminalCreateEstablishScriptDialog(this, true);
		dialog.showDialog();
	}

	/**
	 * 生成CONTACT应用软件包脚本文件
	 */
	public void doCreateContactScript() {
		TerminalCreateContactScriptDialog dialog = new TerminalCreateContactScriptDialog(this, true);
		dialog.showDialog();
	}

	/**
	 * 部署数据构建应用包
	 */
	public void doDeployEstablishPackage() {
		TerminalDeployCloudPackageDialog dialog = new TerminalDeployCloudPackageDialog(
				this, true, BuildCloudPackageTag.ESTABLISH);
		// 打开对话框
		boolean accepted = dialog.showDialog();
		if (accepted) {
			String writer = dialog.getReader();
			boolean local = dialog.isLocal();
			// 生成命令来执行
			String syntax = String.format("DEPLOY ESTABLISH PACKAGE %s %s", writer, (local ? "TO LOCAL" : ""));
			right.doDeployEstablishPackage(syntax);
		}		
	}

	/**
	 * 生成迭代计算应用包
	 */
	public void doBuildContactPackage() {
		TerminalBuildCloudPackageDialog dialog = new TerminalBuildCloudPackageDialog(
				this, true, BuildCloudPackageTag.CONTACT);
		// 打开对话框
		boolean accepted = dialog.showDialog();
		if (accepted) {
			String reader = dialog.getReader();
			String writer = dialog.getWriter();
			// 生成命令来执行
			String syntax = String.format("BUILD CONTACT PACKAGE %s IMPORT BY %s", writer, reader);
			right.doBuildContactPackage(syntax);
		}
	}

	/**
	 * 部署迭代计算应用软件包
	 */
	public void doDeployContactPackage() {
		TerminalDeployCloudPackageDialog dialog = new TerminalDeployCloudPackageDialog(
				this, true, BuildCloudPackageTag.CONTACT);
		// 打开对话框
		boolean accepted = dialog.showDialog();
		if (accepted) {
			String writer = dialog.getReader();
			boolean local = dialog.isLocal();
			// 生成命令来执行
			String syntax = String.format("DEPLOY CONTACT PACKAGE %s %s", writer, (local ? "TO LOCAL" : ""));
			right.doDeployContactPackage(syntax);
		}
	}

	/**
	 * 生成\修改GUIDE-INF/guides.xml文件
	 */
	public void doCreateGuide() {
		TerminalCreateGuideDialog dialog = new TerminalCreateGuideDialog(this, true);
		dialog.showDialog();
	}

	/**
	 * 生成/修改TASK-INF/tasks.xml文件
	 */
	public void doCreateTask(){
		TerminalCreateTaskDialog dialog = new TerminalCreateTaskDialog(this, true);
		dialog.showDialog();
	}

	/**
	 * 生成文件的Each散列码
	 */
	public void doEachFile() {
		String description = null;
		String extensions = null;
		String title = getCaption("Dialog/BuildHash/Title/title");
		String buttonText = getCaption("Dialog/BuildEach/OK/title");
		File file = choiceSingleFile(title, buttonText, description, extensions);
		if (file != null) {
			right.doEachFile(file);
		}
	}

	/**
	 * 生成文件的MD5散列码
	 */
	public void doMD5File() {
		String description = null;
		String extensions = null;
		String title = getCaption("Dialog/BuildHash/Title/title");
		String buttonText = getCaption("Dialog/BuildHash/OK/title");
		File file = choiceSingleFile(title, buttonText, description, extensions);
		if (file != null) {
			right.doMD5File(file);
		}
	}

	/**
	 * 生成文件的SHA1散列码
	 */
	public void doSHA1File() {
		String description = null;
		String extensions = null;
		String title = getCaption("Dialog/BuildHash/Title/title");
		String buttonText = getCaption("Dialog/BuildHash/OK/title");
		File file = choiceSingleFile(title, buttonText, description, extensions);
		if (file != null) {
			right.doSHA1File(file);
		}
	}

	/**
	 * 生成文件的SHA256散列码
	 */
	public void doSHA256File() {
		String description = null;
		String extensions = null;
		String title = getCaption("Dialog/BuildHash/Title/title");
		String buttonText = getCaption("Dialog/BuildHash/OK/title");
		File file = choiceSingleFile(title, buttonText, description, extensions);
		if (file != null) {
			right.doSHA256File(file);
		}
	}

	/**
	 * 生成文件的SHA512散列码
	 */
	public void doSHA512File() {
		String description = null;
		String extensions = null;
		String title = getCaption("Dialog/BuildHash/Title/title");
		String buttonText = getCaption("Dialog/BuildHash/OK/title");
		File file = choiceSingleFile(title, buttonText, description, extensions);
		if (file != null) {
			right.doSHA512File(file);
		}
	}

	/**
	 * 打开帮助文本
	 */
	public void doHelp() {
		// 如果已经打开，忽略！
		if (CommonHelpDialog.isLoaded()) {
			return;
		}

		String title = TerminalLauncher.getInstance().findCaption("Dialog/Help/title");

		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/help/");
		int iconWidth = 16, iconHeight = 16;
		Icon close = loader.findImage("close.png", iconWidth, iconHeight); // 关闭图标
		Icon open = loader.findImage("open.png", iconWidth, iconHeight);// 打开图标
		Icon command = loader.findImage("command.png", iconWidth, iconHeight);// 命令图标
		Icon search = loader.findImage("search.png", iconWidth, iconHeight);// 命令图标
		Icon go = loader.findImage("go.png", iconWidth, iconHeight);// 命令图标

		CommonHelpDialog dialog = new CommonHelpDialog();
		dialog.setCommentContext(TerminalLauncher.getInstance().getCommentContext());
		dialog.setHelpIcon(close, open, command, search, go);
		dialog.showDialog(title, getIconImage());
	}

	/**
	 * 显示版本窗口
	 */
	public void doAbout() {
		TerminalAboutDialog about = new TerminalAboutDialog(this, true);
		about.showDialog();
	}

	/**
	 * 命令处理模式
	 */
	public void doCommandMode() {
		TerminalCommandModeDialog dialog = new TerminalCommandModeDialog(this, false);
		dialog.showDialog();
	}

	/**
	 * 命令超时间隔时间
	 */
	public void doCommandTimeout() {
		TerminalCommandTimeoutDialog dialog = new TerminalCommandTimeoutDialog(this, true);
		dialog.showDialog();
	}

	/**
	 * 测试正则表达式
	 */
	public void doRegex() {
		TerminalRegexDialog dialog = new TerminalRegexDialog(this, true);
		// 显示窗口
		dialog.showDialog();
	}

	/**
	 * 转换图像格式和尺寸
	 */
	public void doImageTransform() {
		TerminalTransformImageDialog dialog = new TerminalTransformImageDialog(this, false);
		// 显示窗口
		dialog.showDialog();
	}

	/**
	 * 更新主菜单字体
	 * @param font 字体实例
	 */
	private void updateMainMenuFont(Font font) {
		menubar.setFont(font);
		// 更新菜单字体
		int count = menubar.getMenuCount();
		for (int index = 0; index < count; index++) {
			JMenu menu = menubar.getMenu(index);
			updateMenuItemFont(menu, font);
		}
	}

	/**
	 * 更新菜单栏字体
	 * @param menu 菜单
	 * @param font 字体实例
	 */
	private void updateMenuItemFont(JMenu menu, Font font) {
		menu.setFont(font);

		int count = menu.getItemCount();
		for (int index = 0; index < count; index++) {
			JMenuItem item = menu.getItem(index);
			// 如果是分隔符，是空指针！存在这个可能！
			if (item == null) {
				continue;
			}

			// 是JMenu，递归！
			if (item.getClass() == JMenu.class) {
				JMenu sub = (JMenu) item;
				updateMenuItemFont(sub, font);
			} else {
				// 修改字体
				item.setFont(font);
			}
		}
	}

	/**
	 * 建立皮肤菜单项目
	 */
	private void createSkinMenu(JMenu menu) {
		// 找到全部皮肤
		SkinTokenLoader loader = TerminalLauncher.getInstance().getSkinLoader();
		java.util.List<SkinToken> tokens = loader.getSkinTokens(); // TerminalLauncher.getInstance().getSkinTokens();
		// 逐一生成菜单
		for (SkinToken e : tokens) {
			JMenuItem item = new JRadioButtonMenuItem();
			// 添加菜单项
			item = menu.add(item);
			// 设置显示文本、工具提示文本
			FontKit.setButtonText(item, e.getTitle());
			FontKit.setToolTipText(item, e.getTitle());
			// 菜单点击触发的方法
			item.setName(e.getMethod());

			// 被选中或者否
			((JRadioButtonMenuItem) item).setSelected(e.isChecked());
		}
	}

	/**
	 * 根据当前语言环境，选择资源配置文件，建立菜单条
	 */
	private void createMenuBar() {
		String path = TerminalLauncher.getInstance().getSurfacePath();
		MenuBarCreator creator = new MenuBarCreator();
		menubar = creator.create(path);

		// 建立皮肤菜单项目, 关键字“SkinMenu”在resource.xml配置文件中定义
		final String associate = "SkinMenu";
		JMenu menu = creator.findMenu(menubar, associate);
		if (menu != null) {
			createSkinMenu(menu);
		}

		creator.setActionListener(menubar, this);

		// 设置菜单字体
		Font font = TerminalProperties.readMainMenuFont();
		if (font != null) {
			updateMainMenuFont(font);
		}

		menubar.setBorder(new EmptyBorder(2, 2, 0, 2));
		setJMenuBar(menubar);
	}

	/**
	 * 根据当前语言环境，选择资源配置文件，建立工具条。
	 * 工具条下面有一条线！
	 */
	private void createToolBar() {
		String path = TerminalLauncher.getInstance().getSurfacePath();
		ToolBarCreator creator = new ToolBarCreator();
		toolbar = creator.create(path);
		creator.setActionListener(toolbar, this);
		toolbar.setFloatable(false);
		// 不要有边框
		toolbar.setBorder(new EmptyBorder(0, 0, 0, 0));
	}

	/**
	 * 初始化窗口组件
	 */
	private void initControls() {
		// 窗口范围
		Rectangle rect = getDefaultBounds();

		// 左侧面板
		left.init();
		// 右侧栏
		right.init(rect);
		// 底部状态栏
		bottom.init();
		// 建立工具栏
		createToolBar();
		// 建立菜单栏
		createMenuBar();

		// 启动线程
		bottom.startDaemonThread();
		
		// 中心面板
		TerminalPanel panel = new TerminalPanel();
		panel.init(rect, toolbar, left, right, bottom);
		panel.setBorder(new EmptyBorder(4, 4, 4, 4));
		setContentPane(panel);
		
		// 窗口注册鼠标追踪
		addMouseMotion();

		// 调整窗口尺寸，适应控件
		pack();
		// 设置默认的窗口范围
		setBounds(rect);

		
	}

//	/**
//	 * 初始化控件
//	 *
//	 * @author scott.liang
//	 * @version 1.0 1/22/2020
//	 * @since laxcus 1.0
//	 */
//	class InitControlsThread extends SwingEvent {
//		InitControlsThread() {
//			super(true); // 要求SwingDispatcher等待线程工作完成才退出！
//		}
//
//		public void process() {
//			// 初始化控件
//			initControls();
//		}
//	}

	/**
	 * 设置窗口参数
	 */
	private void initWindow() {
		// 设置标题
		String title = TerminalLauncher.getInstance().findCaption("Window/Frame/title");
		// 图标资源，在JAR文档中
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/frame/");
		ImageIcon icon = loader.findImage("logo.png");
		// 标题和图标
		setTitle(title);
		setIconImage(icon.getImage());
		
		// 注册窗口事件
		TerminalWindowAdapter adapter = new TerminalWindowAdapter();
		addWindowListener(adapter);
		addWindowFocusListener(adapter);
		addWindowStateListener(adapter);
		
		// 忽略关闭窗口操作
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

//	/**
//	 * 初始化窗口
//	 *
//	 * @author scott.liang
//	 * @version 1.0 1/22/2020
//	 * @since laxcus 1.0
//	 */
//	class InitWindowThread extends SwingEvent {
//		InitWindowThread(){
//			super(true); // 要求SwingDispatcher等待线程工作完成才退出！
//		}
//		public void process() {
//			initWindow();
//		}
//	}
	
	class CreateTerminalThread extends SwingEvent {
		CreateTerminalThread(){
			super(); // 同步处理
		}
		public void process() {
			// 初始化窗口
			initWindow();
			// 初始化控件
			initControls();
		}
	}

	/**
	 * 显示主窗口
	 */
	private void suffixProcess() {
		// 设置为无效状态
		JMenuItem item = findMenuItem("login");
		item.setEnabled(false);

		// 本窗口不需要们于其它窗口上方
		setAlwaysOnTop(false);
		// 置于后端
		toBack();
	}

	class SuffixProcessThread extends SwingEvent {
		public SuffixProcessThread(){
			super();
		}
		public void process(){
			suffixProcess();
		}
	}

	/**
	 * 显示登录窗口和登录
	 * @return 登录成功返回真，否则假
	 */
	private boolean showLoginWindow() {
		boolean success = false;

		// 以线程模式处理注册登录到GATE
		LaunchThread thread = new LaunchThread(this);
		// 启动线程
		boolean launch = thread.start();

		if (launch) {
			// 进入等待
			thread.await();
			// 判断登录成功
			success = thread.isSuccessful();
		}

		if (success) {
			//			addThread(new VisibleThread());
			right.focusCommand();
			// 状态栏图标为连接状态
			bottom.setConnectIcon();
			// 线程
			addThread(new SuffixProcessThread());
		} else {
			dispose();
		}

		return success;
	}
	
	class ShowThread extends SwingEvent {
		public ShowThread(){
			super();
		}
		public void process(){
			// 显示窗口
			setVisible(true);
		}
	}

	/**
	 * 显示窗口并且注册到服务器
	 * @return 返回真或者假
	 */
	public boolean showWindow() {
		// 选择平台字体
		TerminalLauncher.getInstance().doPlatformFont();

//		// 初始化窗口
//		addThread(new InitWindowThread());
//		// 初始化控件
//		addThread(new InitControlsThread());
		
		// 建立窗口
		addThread(new CreateTerminalThread());
		// 更新窗口外观（放在最后，更新全部UI界面）
		reloadUI();
		
		// 如果是繁忙状态，延时...
		while (SwingDispatcher.getInstance().isBusy()) {
			SwingDispatcher.getInstance().delay(1000L);
		}

		// 显示登录窗口
		boolean success = showLoginWindow();
		
		// 可视化
		if (success) {
//			// 如果是繁忙状态，延时...
//			while (SwingDispatcher.getInstance().isBusy()) {
//				SwingDispatcher.getInstance().delay(200L);
//			}
			
			// 进入线程显示
			addThread(new ShowThread());
			// 如果是繁忙状态，延时...
			while (SwingDispatcher.getInstance().isBusy()) {
				SwingDispatcher.getInstance().delay(200L);
			}
		}
		return success;
	}

	/**
	 * 读窗口在屏幕的默认位置
	 * 1. 从配置中文件中取得
	 * 2. 以上不成立，建立一个默认范围
	 * @return Rectangle对象
	 */
	private Rectangle getDefaultBounds() {
		// 从配置中读取范围
		Rectangle rect = TerminalProperties.readWindowBound();
		if (rect != null) {
			return rect;
		}

		// 默认是全屏
		Dimension d = getToolkit().getScreenSize();
		return new Rectangle(0, 0, d.width, d.height);
	}

}