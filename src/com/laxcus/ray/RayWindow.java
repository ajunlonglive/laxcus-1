/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.application.factory.*;
import com.laxcus.application.manage.*;
import com.laxcus.application.menu.*;
import com.laxcus.ray.cycle.*;
//import com.laxcus.ray.dialog.build.*;
import com.laxcus.ray.dialog.build.*;
import com.laxcus.ray.dialog.install.*;
import com.laxcus.ray.dialog.login.*;
import com.laxcus.ray.dialog.notify.*;
import com.laxcus.ray.dialog.properties.*;
import com.laxcus.ray.dialog.run.*;
import com.laxcus.ray.dialog.script.*;
import com.laxcus.ray.dialog.shutdown.*;
import com.laxcus.ray.dialog.uninstall.*;
import com.laxcus.ray.dispatcher.*;
import com.laxcus.ray.dock.*;
import com.laxcus.ray.frame.help.*;
import com.laxcus.ray.frame.log.*;
import com.laxcus.ray.invoker.*;
import com.laxcus.ray.menu.*;
import com.laxcus.ray.status.*;
import com.laxcus.ray.util.*;
import com.laxcus.gui.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.gui.frame.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.platform.control.*;
import com.laxcus.platform.listener.*;
import com.laxcus.register.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.local.*;
import com.laxcus.util.login.*;
import com.laxcus.util.skin.*;
import com.laxcus.util.sound.*;

/**
 * 桌面窗口
 * 
 * @author scott.liang
 * @version 1.0 5/18/2021
 * @since laxcus 1.0
 */
public class RayWindow extends JFrame implements ActionListener, RayUIUpdater, InstallFactory, UninstallFactory, RaySelectFrameListener {

	private static final long serialVersionUID = -9096105623362635798L;;

	/** 桌面面板 **/
	protected PlatformDesktop desktop;

	/** 状态栏 **/
	protected RayStatusBar status;

	/** 应用坞，显示应用软件 **/
	protected RayDock dock;

	/** 切换页窗口 **/
	private RayCycleWindow switchWindow;

	/** 启动菜单 **/
	private RayLaunchMenu desktopLaunchMenu;

	/** 桌面弹出菜单 **/
	private RayRockMenu desktopRockMenu;

	/** 登录标记 **/
	private LoginToken loginToken;

	/** 类型名称和启动KEY的关联 **/
	private TreeMap<String, StartKey> startKeys = new TreeMap<String, StartKey>();

	/** 全屏适配器 **/
	private RayFullScreenAdapter fullScreen;

	/**
	 * 桌面窗口
	 * @throws HeadlessException
	 */
	public RayWindow() throws HeadlessException {
		super();
		fullScreen = new RayFullScreenAdapter(this);
	}

	/**
	 * 判断有应用
	 * @param item
	 * @return
	 */
	public boolean hasApplication(WRoot boot) {
		return RTManager.getInstance().hasRoot(boot.getHash());
	}

	/**
	 * 安装桌面图标
	 * @param element
	 */
	private void addDesktopButton(WElement element) {
		//		try {
		//			if (element != null) {
		//				throw new RuntimeException("fuck!");
		//			}
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//			return;
		//		}

		if (Laxkit.isClassFrom(element, WProgram.class)) {
			WProgram wp = (WProgram) element;
			if (wp.isDesktop()) {
				WKey key = wp.getKey();
				boolean exists = RayController.getInstance().hasDesktopButton(key);
				if (!exists) {
					RayButtonItem bt = new RayButtonItem(wp);
					RayController.getInstance().doDesktopButton(bt);
				}
			}
			// 注册启动关联
			addStartTypes(wp);
		}

		if (Laxkit.isClassFrom(element, WDirectory.class)) {
			WDirectory dir = (WDirectory) element;
			for (WElement sub : dir.getTokens()) {
				addDesktopButton(sub);
			}
		}
	}
	
//	private void testTockButton() {
//		TockButton cmd = new TockButton();
//		
//		Dimension size = RayUtil.getDesktopButtonSize();
//		cmd.setSize(size); // 120, 120);
//		
//		Point point = new Point(300, 300);
//		cmd.setLocation(point);
//		this.desktop.add(cmd, Integer.MIN_VALUE + 1);
//		
////		cmd.setText("大江东去，浪淘尽千古风流人物。故垒西边，人道是、三国周郎赤壁。");
//		cmd.setText("我的计算机");
//		cmd.setToolTipText("大江东去，浪淘尽千古风流人物。故垒西边，人道是、三国周郎赤壁");
//		
//		String s = "DockBar.RunIcon";
//		ImageIcon icon = (ImageIcon) UIManager.getIcon(s); // "DockBar.ShutdownIcon"); // "RayWindow.TitleIcon");
//		cmd.setIcon(icon, PlatformButton.DESKTOP_BUTTON_BRIGHTER, PlatformButton.DESKTOP_BUTTON_DARK);
//	}

	/**
	 * 安装应用坞图标
	 * @param element
	 */
	private void addDockButton(WElement element, boolean system) {
		if (Laxkit.isClassFrom(element, WProgram.class)) {
			WProgram wp = (WProgram) element;
			if (wp.isDock()) {
				WKey key = wp.getKey();
				boolean exists = dock.hasButton(key); 
				if (!exists) {
					RayDockButtonItem bt = new RayDockButtonItem(wp, system);
					dock.addButton(bt);
				}
			}
		}

		if (Laxkit.isClassFrom(element, WDirectory.class)) {
			WDirectory dir = (WDirectory) element;
			for (WElement sub : dir.getTokens()) {
				addDockButton(sub, system);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ray.InstallFactory#setup(com.laxcus.application.manage.WRoot)
	 */
	@Override
	public boolean setup(WRoot boot) {
		JMenuItem menuItem = BootMenuCreator.createMenu(boot);
		if (menuItem == null) {
			return false;
		}

		// 找到关联
		String associate = null;
		if (Laxkit.isClassFrom(menuItem, BootMenu.class)) {
			associate = ((BootMenu) menuItem).getAttachMenu();
		} else if (Laxkit.isClassFrom(menuItem, BootMenuItem.class)) {
			associate = ((BootMenuItem) menuItem).getAttachMenu();
		}
		if (associate == null) {
			return false;
		}

		JMenu menu = RayLaunchMenuCreator.findMenu(desktopLaunchMenu, associate);
		if (menu == null) {
			return false;
		}

		// 保存参数
		boolean success = RTManager.getInstance().add(boot);
		if (!success) {
			return false;
		}

		// 加入到菜单队列中
		menu.add(menuItem);
		// 更新字体和边框
		desktopLaunchMenu.updateFontAndBorder(false);
		// 更新监听事件
		desktopLaunchMenu.updateActionListener();

		// 安装桌面图标
		addDesktopButton(boot.getElement());

		// 安装到应用坞
		addDockButton(boot.getElement(), false);
		// 应用坞的用户按纽参数写入磁盘
		dock.writeUserButtons();

		return true;
	}

	/**
	 * 删除桌面按纽
	 * @param element
	 * @return
	 */
	private boolean deleteDesktopButton(WElement element) {
		int count = 0;
		// 删除应用
		if (Laxkit.isClassFrom(element, WProgram.class)) {
			WProgram wp = (WProgram) element;
			WKey key = wp.getKey();
			boolean success = RayController.getInstance().hasDesktopButton(key);
			if (success) {
				success = RayController.getInstance().deleteDesktopButton(key);
				if (success) {
					removeStartTypes(wp);
					count++;
				} else {
					return false;
				}
			}
		}

		// 删除子任务
		if (Laxkit.isClassFrom(element, WDirectory.class)) {
			WDirectory dir = (WDirectory) element;
			for (WElement sub : dir.getTokens()) {
				boolean success = deleteDesktopButton(sub);
				if (success) {
					count++;
				} else {
					return false;
				}
			}
		}

		return count >= 0;
	}

	/**
	 * 删除应用坞按纽
	 * 被删除的应用坞按纽必须存在，才可以删除
	 * 
	 * @param element
	 * @return 返回真或者假
	 */
	private boolean deleteDockButton(WElement element) {
		int count = 0;
		// 删除应用
		if (Laxkit.isClassFrom(element, WProgram.class)) {
			WProgram wp = (WProgram) element;
			WKey key = wp.getKey();
			// 判断组件存在，再做删除
			if (dock.hasButton(key)) {
				boolean success = dock.deleteButton(key); 
				if (success) {
					count++;
				} else {
					return false;
				}
			}
		}

		// 删除子任务
		if (Laxkit.isClassFrom(element, WDirectory.class)) {
			WDirectory dir = (WDirectory) element;
			for (WElement sub : dir.getTokens()) {
				boolean success = deleteDockButton(sub);
				if (success) {
					count++;
				} else {
					return false;
				}
			}
		}

		// 允许是等于大于0
		return count >= 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ray.UninstallFactory#deleteApplication(com.laxcus.application.manage.WRoot)
	 */
	@Override
	public boolean deleteApplication(WRoot root) {
		WElement element = root.getElement();
		// 1. 删除桌面按纽
		boolean success = deleteDesktopButton(element);
		if (!success) {
			return false;
		}
		// 2. 删除应用坞上应用
		success = deleteDockButton(element);
		if (!success) {
			return false;
		}

		// 3. 删除菜单上的应用
		String associate = root.getAttachMenu();
		JMenu menu = RayLaunchMenuCreator.findMenu(desktopLaunchMenu, associate);
		if (menu != null) {
			Component[] components = menu.getMenuComponents();
			int size = (components == null ? 0 : components.length);
			for (int i = 0; i < size; i++) {
				Component sub = components[i];
				// 删除菜单
				if (Laxkit.isClassFrom(sub, BootMenu.class)) {
					BootMenu bm = (BootMenu) sub;
					if (Laxkit.compareTo(bm.getHash(), root.getHash()) == 0) {
						menu.remove(sub);
						break;
					}
				}
				// 删除菜单项
				else if (Laxkit.isClassFrom(sub, BootMenuItem.class)) {
					if (!Laxkit.isClassFrom(element, WProgram.class)) {
						continue;
					}
					BootMenuItem bmi = (BootMenuItem) sub;
					WProgram wp = (WProgram) element;
					if (Laxkit.compareTo(bmi.getKey(), wp.getKey()) == 0) {
						menu.remove(sub);
						break;
					}
				}
			}
		}
		// 4. 删除
		success = RTManager.getInstance().remove(root);
		if (!success) {
			return false;
		}
		// 5. 删除磁盘上的文件
		File file = root.getPath();
		if (file != null) {
			success = (file.exists() && file.isFile());
			if (success) {
				success = file.delete();
				if (!success) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * 设置自动登录标记
	 * @param e 自动登录标记
	 */
	public void setLoginToken(LoginToken e) {
		loginToken = e;
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		click(event);
	}

	/**
	 * 单击操作
	 * @param e
	 */
	private void click(ActionEvent e) {
		Object source = e.getSource();

		// 单元
		if(Laxkit.isClassFrom(source, BootMenuItem.class) ) {
			BootMenuItem item = (BootMenuItem) source;
			WKey key = item.getKey();
			// 打开应用软件
			RayStarter starter = new RayStarter(desktop, startKeys);
			starter.open(key);
		}
		// 菜单
		else if (source instanceof JMenuItem) {
			JMenuItem item = (JMenuItem) source;
			String methodName = item.getName();
			invoke(methodName);
		} else if (source.getClass() == JButton.class) {
			JButton button = (JButton) source;
			String methodName = button.getName();
			invoke(methodName);
		}
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
	 * 将窗口的显示单元置于无效状态
	 */
	private void __disableWindow() {
		setInterruptIocn();
		setStatusText(null);

		// 图形界面显示注销状态。这时的登录菜单生效，退出登录/重新登录失效
		setLoginMenuEnabled(true);
	}

	/**
	 * 设置错误信息
	 * @param xmlPath
	 */
	private void setErrorText(String xmlPath) {
		String content = UIManager.getString(xmlPath);
		if (content != null) {
			RayNotifyDialog dlg = RayNotifyDialog.getInstance();
			if (dlg != null) {
				dlg.fault(content, true);
			}
		}
	}

	/**
	 * 追加普通信息
	 * @param xmlPath
	 */
	private void setMessageText(String xmlPath) {
		String content = UIManager.getString(xmlPath);
		if (content != null) {
			RayNotifyDialog dlg = RayNotifyDialog.getInstance();
			if (dlg != null) {
				dlg.message(content, true);
			}
		}
	}

	/**
	 * 更新状态栏的节点数目
	 */
	public void updateStatusSites() {
		//		int sites = SiteOnRayPool.getInstance().size();
		//		bottom.setSites(sites);
	}

	/**
	 * 在线程驱动下自动登录
	 * @param hub 服务器主机
	 * @return 成功返回真，否则假
	 */
	public boolean __auto_login(Node hub) {
		// 让窗口信息失效
		__disableWindow();

		// 显示自动登录
		setErrorText("RayWindow.AutoLogin.RetryText");

		// 调用线程完成登录
		int who = RayLauncher.getInstance().login(hub.getHost(), true, null);
		// 判断成功
		boolean success = RayEntryFlag.isSuccessful(who);
		if (!success) {
			setErrorText("RayWindow.AutoLogin.FailedText");
			return false;
		}

		// 登录成功后，登录菜单失效，注销和重新登录有效
		setLoginMenuEnabled(false);

		// 更新状态
		updateStatusSites();

		setMessageText("RayWindow.AutoLogin.SuccessText");

		Logger.debug(this, "__auto_login", "hub is %s", hub);

		return true;
	}

	/**
	 * 登录目标站点
	 */
	protected void login() {
		addThread(new LoginThread());
	}

	/**
	 * 从服务器注销
	 */
	protected void logout() {
		RayLogoutThread thread = new RayLogoutThread(this);
		thread.start();
	}

	/**
	 * 重新注册。先注销，再注册
	 */
	protected void relogin() {
		RayReloginThread thread = new RayReloginThread(this);
		thread.start();
	}

	/**
	 * 运行应用
	 */
	protected void doRun() {
		RayRunDialog dlg = RayRunDialog.getInstance();
		if (dlg == null) {
			RayRunDialog dialog = new RayRunDialog();
			dialog.showDialog(desktop, false);
		} else {
			setSelectFrame(dlg, true);
		}
	}

	class Shutdown1Thread extends SwingEvent {

		Shutdown1Thread() {
			super(true);
		}

		public void process() {
			// 关闭状态条上的托盘窗口
			// status.closeTrayWindow();

			// 停止状态条线程
			status.stopDaemonThread();
		}
	}

	class ShutdownWindowThread extends SwingEvent {
		LightForm form;

		ShutdownWindowThread(LightForm e) {
			super(true);
			form = e;
		}

		public void process() {
			form.closeWindow();
			sleep(380);
		}
	}

	/**
	 * 关闭窗口
	 * 包括窗体和对话框
	 */
	private void shutdown2() {
		JInternalFrame[] frames = desktop.getAllFrames();
		int size = (frames == null ? 0 : frames.length);
		for (int i = 0; i < size; i++) {
			JInternalFrame frame = frames[i];
			if (frame == null) {
				continue;
			}
			// 忽略停止窗口
			if (Laxkit.isClassFrom(frame, RayShutdownDialog.class)) {
				continue;
			}
			// 忽略应用坞
			if (dock != null && frame == dock) {
				continue;
			}
			// 是子窗口，关闭它们
			if (Laxkit.isClassFrom(frame, LightForm.class)) {
				ShutdownWindowThread event = new ShutdownWindowThread((LightForm) frame);
				SwingDispatcher.invokeThread(event);
			}
		}
	}

	class ShutdownDockThread extends SwingEvent {
		ShutdownDockThread() {
			super(true);
		}

		public void process() {
			if (dock == null) {
				return;
			}
			// 关闭和消毁窗口
			dock.closeWindow();
			sleep(380);
		}
	}

	class Shutdown3Thread extends SwingEvent {
		Shutdown3Thread() {
			super(true);
		}

		public void process() {
			// 写入DOCK按纽单元
			dock.writeUserButtons();
			// 写入桌面按纽单元
			RayController.getInstance().writeButtons();

			sleep(800L);

			// 关闭窗口
			setVisible(false);

			// 通知进程退出
			RayLauncher.getInstance().stop();
		}
	}

	class ShutdownThread extends SwingEvent {
		ShutdownThread() {
			super(true);
		}

		public void process() {
			// 1. 停止状态条线程
			addThread(new Shutdown1Thread());

			// 2. 关闭窗口
			shutdown2();

			// 3. 关闭应用坞
			addThread(new ShutdownDockThread());

			// 4. 释放其它...
			addThread(new Shutdown3Thread());

			// 拒绝事件...
			SwingDispatcher.getInstance().setRefuseEvent(true);
		}
	}

	/**
	 * 退出运行
	 */
	private void exit() {
		boolean success = false;
		RayShutdownDialog dialog = new RayShutdownDialog();
		// 输出结果
		Object value = dialog.showDialog(desktop); // 必须是模态
		if (value != null) {
			success = ((Boolean) value).booleanValue();
		}

		// 启动关闭线程，退出全部服务
		if (success) {
			addThread(new ShutdownThread());
		}
	}

	/**
	 * 设置最大日志数目
	 * @param logs 日志数目
	 * @return 返回修改的日志数目
	 */
	public int setMaxLogs(int logs) {
		// 查找显示的窗口，显示日志
		JInternalFrame[] frames = desktop.getAllFrames();
		for (int i = 0; frames != null && i < frames.length; i++) {
			JInternalFrame frame = frames[i];
			if (Laxkit.isClassFrom(frame, RayLogFrame.class)) {
				logs = ((RayLogFrame) frame).setMaxLogs(logs);
			}
		}
		return logs;
	}

	//	/**
	//	 * 退出线程
	//	 *
	//	 * @author scott.liang
	//	 * @version 1.0 4/3/2020
	//	 * @since laxcus 1.0
	//	 */
	//	class ExitThread extends SwingEvent {
	//
	//		public ExitThread() {
	//			super();
	//		}
	//
	//		@Override
	//		public void process() {
	//			exit();
	//		}
	//	}	

	class RayWindowAdapter extends WindowAdapter {

		RayWindowAdapter() {
			super();
		}

		public void windowClosing(WindowEvent e) {
			// addThread(new ExitThread());
			// exit();
		}
	}

	class PopupMenuAdapter extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			// 弹出菜单（由方法决定）
			showPopupMenu(e);
		}

		public void mouseReleased(MouseEvent e) {
			// 弹出菜单（由方法决定）
			showPopupMenu(e);
		}
	}

	/**
	 * 桌面监听实现
	 *
	 * @author scott.liang
	 * @version 1.0 3/6/2022
	 * @since laxcus 1.0
	 */
	class DesktopAdapter implements DesktopListener {
		@Override
		public void open(Object o) {
			addThread(new StartApplicationThread(o));
		}

		@Override
		public boolean setWallPaper(Object o, int layout) {
			WallPaperLoader loader = new WallPaperLoader(desktop);
			return loader.load(o, layout);
		}

		@Override
		public boolean setFullScreen(LightForm form, boolean full) {
			if (full) {
				return fullScreen.full(form);
			} else {
				return fullScreen.cancel(form);
			}
		}
	}

	class StartApplicationThread extends SwingEvent {
		Object object;

		StartApplicationThread(Object e) {
			super();
			object = e;
		}

		public void process() {
			RayStarter starter = new RayStarter(desktop, startKeys);
			starter.run(object);
		}
	}

	/**
	 * 删除注册的WKEY
	 * @param type
	 * @param key
	 * @return
	 */
	private boolean removeWKey(String type, WKey key) {
		String path = String.format("StartTypes/%s", type);
		String value = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, path);
		if (!WKey.validate(value)) {
			return false;
		}
		WKey wk = WKey.translate(value);
		if (key.compareTo(wk) == 0) {
			return RTKit.removeString(RTEnvironment.ENVIRONMENT_SYSTEM, path);
		}
		return false;
	}


	/**
	 * 更新窗口外观
	 */
	private void __reloadUI() {

		//		reloadNimbusTitleIcon();

		SwingUtilities.updateComponentTreeUI(this);
	}

	/**
	 * 用线程更新全部UI界面
	 * @author scott.liang
	 * @version 1.0 6/5/2021
	 * @since laxcus 1.0
	 */
	class ReloadUIThread extends SwingEvent {

		public ReloadUIThread(){
			super(true);
		}

		public void process() {
			__reloadUI();
		}
	}

	/**
	 * 更新窗口外观
	 */
	private void reloadUI() {
		addThread(new ReloadUIThread());
	}

	/**
	 * 被RayLauncher调用，显示动态图标！
	 */
	public void flash() {
		// 显示中
		if (isShowing() && status.isVisible()) {
			status.flash();
		}
	}

	/**
	 * 增加发送和接收的流量
	 * @param receiveFlows 接收流量
	 * @param sendFlows 发送流量
	 */
	public void addFlows(long receiveFlows, long sendFlows) {
		status.addFlows(receiveFlows, sendFlows);
	}

	/**
	 * 收到信息后，电灯图标闪烁
	 */
	public void rolling(boolean start) {
		// 增加任务数字
		if (start) {
			status.addInvokerRolling();
		}

		// 如果处于闪烁状态，忽略它
		if (status.isInvokerRolling()) {
			return;
		}
		// 可视状态下，更新
		if (isShowing()) {
			status.rolling();
		}
	}

	/**
	 * 根据方法名称查找菜单项
	 * @param method 调用的方法名称
	 * @return 返回菜单项实例，没有找到是空指针
	 */
	private JMenuItem findMenuItem(String method) {
		RayLaunchMenuCreator creator = new RayLaunchMenuCreator();
		return creator.findMenuItemByMethod(desktopLaunchMenu, method);
	}

	/**
	 * 调整状态栏图标显示
	 */
	public void showCyberStatus() {
		RayLauncher launcher = RayLauncher.getInstance();
		boolean logined = launcher.isLogined();

		if (logined) {
			// 连接图标
			setConnectIcon();
			//			// 选择管理员/用户图标
			//			if (launcher.isAdministrator()) {
			//				setAdministratorIcon();
			//			} else if (launcher.isUser()) {
			//				setUserIcon();
			//			} 
			//			// 特别情况！此时尚没有定义用户状态!
			//			else {
			//				setNobodyIcon();
			//			}
		} else {
			setInterruptIocn();
			//			setNobodyIcon();
		}
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
		status.setDisconnectIcon();
	}

	/**
	 * 设置已经连接图标
	 */
	private void setConnectIcon() {
		status.setConnectIcon();
	}

	/**
	 * 设置HUB图标提示
	 * @param text
	 */
	public void setHubTip(String text) {
		status.setHubTip(text);
	}

	//	/**
	//	 * 设置管理员图标
	//	 */
	//	public void setAdministratorIcon() {
	//		status.setAdministratorIcon();
	//	}
	//
	//	/**
	//	 * 设置普通注册用户图标
	//	 */
	//	public void setUserIcon() {
	//		status.setUserIcon();
	//	}

	//	/**
	//	 * 无定义的身份图标
	//	 */
	//	public void setNobodyIcon() {
	//		status.setNobodyIcon();
	//	}

	/**
	 * 设置状态栏文本提示
	 * @param text
	 */
	public void setStatusText(String text) {
		RayNotifyDialog dlg = RayNotifyDialog.getInstance();
		if (dlg != null) {
			dlg.setStatusText(text);
		}
	}

	class RayResetHandler implements ResetHandler {
		public boolean reset() {
			ResourceReleaser rs = new ResourceReleaser();
			rs.deleteResource();
			return true;
		}
	}

	/**
	 * 登录目标站点
	 * @param showReset 显示左下角的清除运行参数的按纽
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean __login(boolean showReset) {
		Logger.debug(this, "__login", "into ...");

		// 显示前端的注册窗口
		RayResetHandler handler = (showReset ? new RayResetHandler(): null);
		RayLoginDialog dialog = new RayLoginDialog(this, handler);
		dialog.setModal(true);
		dialog.showDialog(loginToken);
		dialog.dispose();
		// 注册窗口取消向GATE节点注册操作
		if (dialog.isCanceled()) {
			return false;
		}

		// 设置服务器地址
		Node hub = RayLauncher.getInstance().getHub();
		setHubTip(hub.toString());
		// 登录菜单失效！
		setLoginMenuEnabled(false);
		// 图标显示用户属性
		showCyberStatus();
		// 重置流量参数...
		status.resetRolling();

		Logger.debug(this, "__login", "hub is %s", hub);

		return true;
	}

	class LoginThread extends SwingEvent {
		public LoginThread() {
			super();
		}
		public void process() {
			__login(false);
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
		// 显示对话框
		String title = UIManager.getString("RayWindow.Forsake.Title");
		String content = UIManager.getString("RayWindow.Forsake.Text");
		SoundPlayer.getInstance().play(SoundTag.ERROR);
		MessageBox.showFault(desktop, title, content);
	}

	/**
	 * 显示或者隐藏状态栏
	 */
	public void doShowOrHideStatusbar() {
		JMenuItem item = findMenuItem("doShowOrHideStatusbar");
		if (item != null) {
			boolean on = status.isVisible();
			status.setVisible(!on);
			((JCheckBoxMenuItem) item).setState(!on);
		}
	}

	//	/**
	//	 * 返回句柄
	//	 */
	//	private RayLauncher getLauncher() {
	//		return RayLauncher.getInstance();
	//	}

	/**
	 * 选中子窗口或者否
	 * @param frame 子窗口
	 * @param selected 选中它或者否
	 */
	private void setSelectFrame(JInternalFrame frame, boolean selected) {
		try {
			frame.setSelected(selected);
		} catch (java.beans.PropertyVetoException e) {
			Logger.error(e);
		}
	}

	/**
	 * 取消选中状态的子窗口
	 */
	private void cancelFocusSubFrames() {
		JInternalFrame[] frames = desktop.getAllFrames();
		int size = (frames == null ? 0 : frames.length);
		for (int i = 0; i < size; i++) {
			// 判断当前子窗口被选中
			if (frames[i].isSelected()) {
				setSelectFrame(frames[i], false);
				return;
			}
		}
	}

	/**
	 * 选择焦点窗口
	 * @param frame
	 */
	private void doCallSelect(LightFrame frame) {
		if (frame == null) {
			return;
		}

		boolean exists = false;
		LightFrame[] frames = desktop.getShowFrames();
		for (LightFrame other : frames) {
			if (other == frame) {
				exists = true;
			}
		}

		if (exists) {
			if (!frame.isVisible()) {
				// frame.setVisible(true);
				frame.setIcon(false);
			}
			frame.toFront();
			setSelectFrame(frame, true); // 选中
		}
	}

	class CallSelectThread extends SwingEvent {

		LightFrame frame;

		CallSelectThread(LightFrame e) {
			super(true); // 使用同步
			frame = e;
		}

		public void process() {
			doCallSelect(frame);
		}
	}

	/**
	 * 来自桌面状态条的鼠标点击，反向选中焦点窗口
	 * 通知桌面，拿到选中的焦点窗口
	 * @param frame
	 */
	@Override
	public void callSelectFrame(LightFrame frame) {
		addThread(new CallSelectThread(frame));
	}

	/**
	 * 生成应用软件包
	 */
	public void doBuildSoftware() {
		RayBuildDialog dialog = new RayBuildDialog();
		dialog.showDialog(desktop, false);
	}

	/**
	 * 安装应用软件
	 */
	public void doInstallSoftware() {
		RayInstallDialog dialog = new RayInstallDialog(this);
		dialog.showDialog(desktop, false);
	}

	/**
	 * 删除应用软件
	 */
	public void doUninstallSoftware(){
		RayUninstallDialog dialog = new RayUninstallDialog(this);
		dialog.showDialog(desktop, false);
	}

	/**
	 * 修改、新建runos.sh stopos.sh脚本
	 */
	void doControlScript() {
		RayControlScriptDialog dlg = RayControlScriptDialog.getInstance();
		if (dlg == null) {
			RayControlScriptDialog dialog = new RayControlScriptDialog();
			// 进入非模态
			dialog.showDialog(desktop, false);
		} else {
			// 显示在前面
			setSelectFrame(dlg, true);
		}
	}

	/**
	 * 新建、修改address.sh脚本
	 */
	void doAddressScript() {
		RayAddressScriptDialog dlg = RayAddressScriptDialog.getInstance();
		if (dlg == null) {
			RayAddressScriptDialog dialog = new RayAddressScriptDialog();
			// 进入非模态
			dialog.showDialog(desktop, false);
		} else {
			// 显示在前面
			setSelectFrame(dlg, true);
		}
	}
	
	/**
	 * 新建管理员账号脚本
	 */
	void doAdminScript () {
		RayDBAScriptDialog dlg = RayDBAScriptDialog.getInstance();
		if (dlg == null) {
			RayDBAScriptDialog dialog = new RayDBAScriptDialog();
			// 进入非模态
			dialog.showDialog(desktop, false);
		} else {
			// 显示在前面
			setSelectFrame(dlg, true);
		}		
	}

	/**
	 * 打开日志窗口
	 */
	public void doOpenLogWindow() {
		// 句柄
		RayLogFrame dlg = RayLogFrame.getInstance();
		if (dlg == null) {
			RayLogFrame frame = new RayLogFrame();
			frame.showWindow();
		} else {
			setSelectFrame(dlg, true);
		}
	}

	/**
	 * 打开帮助文本
	 */
	public void doHelp(String command) {
		// 句柄存在
		RayHelpFrame dlg = RayHelpFrame.getInstance();
		if (dlg != null) {
			// 取消焦点状态窗口
			cancelFocusSubFrames();
			// 非图标状态
			if (!dlg.isVisible()) {
				dlg.setIcon(false);
			}
			// 置于前端
			dlg.toFront();
			// 当前窗口是选中
			setSelectFrame(dlg, true);
			// 显示命令
			dlg.showCommand(command);
		} else {
			RayHelpFrame frame = new RayHelpFrame();
			frame.setCommentContext(RayLauncher.getInstance().getCommentContext());
			// 显示窗口
			frame.showWindow();
			// 如果有命令时...
			if (command != null) {
				frame.showCommand(command);
			}
		}
	}

	/**
	 * 打开帮助窗口
	 */
	public void doHelp() {
		doHelp(null);
	}

	class HelpThread extends SwingEvent {
		String command;

		public HelpThread(String cmd) {
			super();
			command = cmd;
		}

		@Override
		public void process() {
			doHelp(command);
		}
	}

	/**
	 * 打开帮助接口
	 *
	 * @author scott.liang
	 * @version 1.0 3/6/2022
	 * @since laxcus 1.0
	 */
	class HelpAdapter implements HelpListener {

		@Override
		public void showHelp(String command) {
			addThread(new HelpThread(command));
		}
	}

	/**
	 * 单击启动菜单
	 */
	private void doLaunchButton() {
		// 先可视化显示，确定菜单尺寸
		desktopLaunchMenu.setVisible(true);
		// 计算是不是要重新绘制弹出菜单左侧的图像
		if (desktopLaunchMenu.isRepaint()) {
			// 更新整理图片
			desktopLaunchMenu.doFlagImage();
		}  
		// 在指定位置显示
		desktopLaunchMenu.show(desktop, 0, desktop.getHeight() - desktopLaunchMenu.getHeight());
	}

	class StartButtonAdapter implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			doLaunchButton();
		}
	}



	/**
	 * 隐藏/显示窗口
	 */
	private void doHideOrShowInternalFrames() {
		desktop.doFlexFrames();
	}

	class MiniButtonClickThread extends SwingEvent {
		public MiniButtonClickThread() {
			super(true); // 单一执行
		}

		public void process() {
			doHideOrShowInternalFrames();
		}
	}

	class MiniButtonAdapter implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			addThread(new MiniButtonClickThread());
		}
	}

	class UpdateLookAndFeelThread extends SwingEvent {
		SkinToken token;

		UpdateLookAndFeelThread(SkinToken e) {
			super(true); // 以"SwingUtilities.invokeAndWait"同步方式处理
			token = e;
		}

		public void process() {
			// 修改UI配置，同时更新组件UI
			boolean success = token.updateTheme(true);
			if (!success) {
				return;
			}

			SkinTokenLoader loader = RayLauncher.getInstance().getSkinLoader();
			// 重置选中的的皮肤方案
			String skinName = token.getName();
			loader.exchangeCheckedSkinToken(skinName);
			// 记录界面外观
			Skins.setLookAndFeel(token.getLookAndFeel());
			Skins.setSkinName(skinName);

			// 更新字体
			Font font = RTKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "FONT/SYSTEM");
			if (font != null) {
				UITools.updateSystemFonts(font);
				if (Skins.isNimbus()) {
					UITools.updateNimbusSystemFonts(font);
				} else {
					UITools.updateMetalSystemFonts(font);
				}
			}

			// 调用UI更新
			__reloadUI();

			// 重绘启动菜单的UI界面
			if (desktopLaunchMenu != null) {
				desktopLaunchMenu.updateFontAndBorder(true);
				desktopLaunchMenu.updateUI();
			}
			// 重绘弹出菜单的UI界面
			if (desktopRockMenu != null) {
				desktopRockMenu.updateFontAndBorder(true);
				desktopRockMenu.updateUI();
			}
		}
	}

	class UpdateSystemFontThread extends SwingEvent {
		Font font;

		UpdateSystemFontThread(Font e) {
			super(true); // 以"SwingUtilities.invokeAndWait"同步方式处理
			font = e;
		}

		public void process() {
			// 更新字体
			UITools.updateSystemFonts(font);
			if (Skins.isNimbus()) {
				UITools.updateNimbusSystemFonts(font);
			} else {
				UITools.updateMetalSystemFonts(font);
			}

			// 更新UI界面
			__reloadUI();

			// 重绘启动菜单的UI界面
			if (desktopLaunchMenu != null) {
				desktopLaunchMenu.updateFontAndBorder(false);
				desktopLaunchMenu.updateUI();
			}
			// 重绘弹出菜单的UI界面
			if (desktopRockMenu != null) {
				desktopRockMenu.updateFontAndBorder(false);
			}
		}
	}

	@Override
	public void updateLookAndFeel(SkinToken token) {
		addThread(new UpdateLookAndFeelThread(token));
	}

	@Override
	public void updateSystemFont(Font font) {
		addThread(new UpdateSystemFontThread(font));
	}

	/**
	 * 增加启动类型
	 * 
	 * @param wp
	 */
	private void addStartTypes(WProgram wp) {
		String text = wp.getSupportTypes();
		if (text == null) {
			return;
		}
		String[] types = text.split(",");
		for (int i = 0; i < types.length; i++) {
			String type = types[i].trim().toLowerCase();
			if (type.length() > 0) {
				StartKey key = startKeys.get(type);
				if (key == null) {
					key = new StartKey(type);
					startKeys.put(type, key);
				}
				// 保存实例
				StartToken st = new StartToken(wp.getKey(), wp.getTitle(), wp.getIcon(), wp.getToolTip());
				key.add(st);
			}
		}
	}

	/**
	 * 删除关联的启动类型
	 * @param wp
	 */
	private void removeStartTypes(WProgram wp) {
		String text = wp.getSupportTypes();
		if (text == null) {
			return;
		}
		String[] types = text.split(",");
		for (int i = 0; i < types.length; i++) {
			text = types[i].trim().toLowerCase();
			if (text.length() > 0) {
				// 删除注册表的关联记录
				removeWKey(text, wp.getKey());

				// 删除内存中的记录
				StartKey key = startKeys.get(text);
				if (key == null) {
					continue;
				}

				// 删除
				StartToken st = new StartToken(wp.getKey(), wp.getTitle(), wp.getIcon(), wp.getToolTip());
				key.remove(st);
				// 空集合，删除它
				if (key.isEmpty()) {
					startKeys.remove(text);
				}
			}
		}
	}

	/**
	 * 初始化应用...
	 * @param root
	 * @return
	 */
	private boolean initApplication(WRoot root, boolean system) {
		if (RTManager.getInstance().hasRoot(root.getHash())) {
			Logger.error(this, "initApplication", "%s existed!", root.getHash());
			return false;
		}

		JMenuItem menuItem = BootMenuCreator.createMenu(root);
		if (menuItem == null) {
			return false;
		}

		// 找到关联
		String associate = null;
		if (Laxkit.isClassFrom(menuItem, BootMenu.class)) {
			associate = ((BootMenu) menuItem).getAttachMenu();
		} else if (Laxkit.isClassFrom(menuItem, BootMenuItem.class)) {
			associate = ((BootMenuItem) menuItem).getAttachMenu();
		}

		JMenu menu = RayLaunchMenuCreator.findMenu(desktopLaunchMenu, associate);
		// 保存参数
		RTManager.getInstance().add(root);

		// 加入到菜单队列中
		if (menu != null) {
			menu.add(menuItem);
		}

		// 加载到桌面按纽
		WElement element = root.getElement();
		// 设置系统标记
		if (Laxkit.isClassFrom(element, WProgram.class)) {
			WProgram wp = (WProgram) element;
			wp.setSystem(system);
			addStartTypes(wp);
		}

		// 加载到桌面上的应用
		addDesktopButton(element);
		// 如果是系统应用，初始化时加载到DOCK上
		if (system) {
			addDockButton(element, true);
		}

		return true;
	}

	/**
	 * 初始化系统应用
	 * @param content 字节流
	 * @return 成功返回真，失败返回假
	 */
	private boolean initSystemApplication(byte[] content) {
		try {
			if (content != null && content.length > 0) {
				WRoot root = WTokenChanger.split(content, true);
				return initApplication(root, true);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return false;
	}

	/**
	 * 从JAR包中加载系统应用
	 */
	private int initSystemApplications() {
		String path = getSurfacePath(); 

		InnerApplicationSplitter n = new InnerApplicationSplitter();
		String[] links = n.split(path);

		int count = 0;
		ResourceLoader rs = new ResourceLoader();
		for (String link : links) {
			byte[] b = rs.findAbsoluteStream(link);
			// 加载应用
			boolean success = initSystemApplication(b);
			if (success) {
				count++;
			}
		}

		Logger.info(this, "initSystemApplications" , "system applications %d", count);

		return count;
	}

	/**
	 * 配置资源
	 * @return
	 */
	private String getSurfacePath() {
		LocalSelector selector = new LocalSelector("conf/ray/menu/config.xml");
		return selector.findPath("resource");
	}

	/**
	 * 建立启动菜单
	 */
	private void createLaunchMenu() {
		// 建立菜单
		String path = getSurfacePath(); // RayLauncher.getInstance().getSurfacePath();
		RayLaunchMenuCreator creator = new RayLaunchMenuCreator();
		desktopLaunchMenu = creator.create(path);
		desktopLaunchMenu.setInvoker(desktop); // 调用者

		desktopLaunchMenu.setParameter(this, RayUtil.getMainMenuBorder(), 6,
				RayUtil.getSubMenuBorder());
		// 更新字体和边框，还有监听事件
		desktopLaunchMenu.updateFontAndBorder(false);
		desktopLaunchMenu.updateActionListener();
	}

	/**
	 * 加载保存在指定目录下的用户应用
	 * @return 返回加载成功数目
	 */
	private int initUserApplications() {
		// 导入资源
		File dir = RaySystem.createRuntimeDirectory();
		File file = new File(dir, "applications.conf");
		WRoot[] roots = RTManager.getInstance().readRoots(file);

		// 加载到菜单和内存记录
		int count = 0;
		if (roots != null) {
			for (WRoot root : roots) {
				boolean success = initApplication(root, false);
				if (success) {
					count++;
				}
			}
		}

		Logger.info(this, "initUserApplications", "user applications %d", count);

		return count;
	}

	private void checkDockMenuItem() {
		JMenuItem item = RayRockMenuCreator.findMenuItemByMethod(desktopRockMenu, "doShowOrHideDockBar");
		// 判断是选择菜单
		if (item != null && Laxkit.isClassFrom(item, JCheckBoxMenuItem.class)) {
			JCheckBoxMenuItem box = (JCheckBoxMenuItem) item;
			// 切换显示或者隐藏
			boolean on = dock.isVisible();
			box.setState(on);
		}
	}
	private void checkStatusBarMenuItem() {
		JMenuItem item = RayRockMenuCreator.findMenuItemByMethod(desktopRockMenu, "doShowOrHideStatusBar");
		// 判断是选择菜单
		if (item != null && Laxkit.isClassFrom(item, JCheckBoxMenuItem.class)) {
			JCheckBoxMenuItem menu = (JCheckBoxMenuItem) item;
			// 切换显示或者隐藏
			boolean on = status.isVisible();
			menu.setState(on);
		}
	}

	/**
	 * 显示弹出菜单
	 * @param e
	 */
	private void showPopupMenu(MouseEvent e) {
		// 不满足SWING条件的POPUP触发，不处理
		if (!e.isPopupTrigger()) {
			return;
		}

		// 来源自桌面，显示弹出菜单
		if (e.getSource() == desktop) {
			checkStatusBarMenuItem();
			checkDockMenuItem();

			Point p = SwingUtilities.convertPoint(desktop, e.getPoint(), desktop.getParent());
			desktopRockMenu.show(desktop, p.x, p.y);
		}
	}

	/**
	 * 按照名字排序
	 */
	public void doDesktopAlignmentByName() {
		RayController.getInstance().doAlignmentByName();
	}

	/**
	 * 按照日期排列
	 */
	public void doDesktopAlignmentByTime() {
		RayController.getInstance().doAlignmentByTime();
	}

	/**
	 * 锁定桌面图标
	 */
	public void doLockDesktop() {
		JMenuItem item = RayRockMenuCreator.findMenuItemByMethod(desktopRockMenu, "doLockDesktop");
		// 判断是选择菜单
		if (item != null && Laxkit.isClassFrom(item, JCheckBoxMenuItem.class)) {
			JCheckBoxMenuItem box = (JCheckBoxMenuItem) item;
			boolean locked = RayController.getInstance().isLocked();
			// 切换状态
			boolean on = !locked;
			box.setState(on);
			// 修改图标
			if (on) {
				Icon icon = UIManager.getIcon("RayWindow.PopupMenu.LockedIcon");
				box.setIcon(icon);
			} else {
				Icon icon = UIManager.getIcon("RayWindow.PopupMenu.UnlockedIcon");
				box.setIcon(icon);
			}
			// 切换状态
			RayController.getInstance().doLock();
		}
	}

	/**
	 * 刷新桌面
	 */
	public void doRefreshDesktop() {
		// 重绘...
		desktop.revalidate();
		desktop.repaint();

		dock.revalidate();
		dock.repaint();

		status.revalidate();
		status.repaint();
		// 更新
		RayController.getInstance().doRefresh();
	}

	class MoveDockToBottom extends SwingEvent {
		MoveDockToBottom() {
			super();
		}

		public void process() {
			dock.doMoveToBottom();
		}
	}

	public void doShowOrHideStatusBar() {
		JMenuItem item = RayRockMenuCreator.findMenuItemByMethod(desktopRockMenu, "doShowOrHideStatusBar");
		// 判断是选择菜单
		if (item != null && Laxkit.isClassFrom(item, JCheckBoxMenuItem.class)) {
			boolean bottom = dock.isBottom();

			// 找到菜单
			JCheckBoxMenuItem box = (JCheckBoxMenuItem) item;
			// 切换显示或者隐藏
			boolean on = box.getState();

			// 任务条不在显示状态，这里要变成显示
			if (!bottom && on) {
				Rectangle r = dock.getBounds();
				Rectangle d = desktop.getBounds();
				int statusHeight = status.getHeight();
				// dock最低位置，在状态栏显示区域内...
				bottom = (r.y + r.height >= d.height - statusHeight);
			}

			// 显示或者否
			status.setVisible(on);

			// 移到新的底部位置
			if (bottom) {
				addThread(new MoveDockToBottom());
			}
		}
	}

	public void doShowOrHideDockBar() {
		JMenuItem item = RayRockMenuCreator.findMenuItemByMethod(desktopRockMenu, "doShowOrHideDockBar");
		// 判断是选择菜单
		if (item != null && Laxkit.isClassFrom(item, JCheckBoxMenuItem.class)) {
			JCheckBoxMenuItem box = (JCheckBoxMenuItem) item;
			// 切换显示或者隐藏
			boolean on = box.getState();
			dock.setVisible(on);
		}
	}

	/**
	 * 打开桌面属性
	 */
	public void doDesktopProperties() {
		RayPropertiesDialog dlg = RayPropertiesDialog.getInstance();
		if (dlg == null) {
			RayPropertiesDialog dialog = new RayPropertiesDialog(desktop, this);
			// 进入非模态
			dialog.showDialog(desktop, false);
		} else {
			// 显示在前面
			setSelectFrame(dlg, true);
		}
	}

	/**
	 * 弹出菜单监听器
	 *
	 * @author scott.liang
	 * @version 1.0 6/12/2021
	 * @since laxcus 1.0
	 */
	class PopupMenuListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			click(event);
		}
	}

	private void createPopupMenu() {
		String path = getSurfacePath();
		RayRockMenuCreator creator = new RayRockMenuCreator();
		desktopRockMenu = creator.create(path, "DesktopPopupMenu");
		desktopRockMenu.setInvoker(desktop);

		desktopRockMenu.setParameter(RayUtil.getSubMenuBorder(), 6);
		desktopRockMenu.setActionListener(new PopupMenuListener());
	}

	/**
	 * 执行截屏，顺序：
	 * 1. 找到获得焦点的子窗口，把子窗口截屏幕
	 * 2. 以上不成立，截取整个屏幕
	 */
	private void fullShotScreen() {
		JInternalFrame[] frames = desktop.getAllFrames();
		int size = (frames == null ? 0 : frames.length);
		for (int i = 0; i < size; i++) {
			// 判断当前子窗口被选中
			if (frames[i].isSelected()) {
				RayUtil.shotScreen(frames[i]);
				return;
			}
		}
		// 2. 第二种
		Container panel = getContentPane();
		if (panel != null && Laxkit.isClassFrom(panel, ScreenPanel.class)) {
			RayUtil.shotScreen((ScreenPanel) panel);
		}
	}

	/**
	 * 找到那个获得焦点的组件
	 * @param parent 父组件
	 * @return 返回焦点组件，如果没有返回空指针
	 */
	private JComponent findFocusComponent(JComponent parent) {
		// 判断获得焦点
		if (parent.hasFocus()) {
			return parent;
		}
		// 判断得到结果
		Component[] subs = parent.getComponents();
		for (int i = 0; subs != null && i < subs.length; i++) {
			Component sub = subs[i];
			if (Laxkit.isClassFrom(sub, JComponent.class)) {
				JComponent real = findFocusComponent((JComponent) sub);
				if (real != null) {
					return real;
				}
			}
		}
		return null;
	}

	/**
	 * 截取焦点组件的屏幕
	 */
	private void scrollShotScreen() {
		JInternalFrame[] frames = desktop.getAllFrames();
		int size = (frames == null ? 0 : frames.length);
		for (int i = 0; i < size; i++) {
			// 判断当前子窗口被选中
			if (frames[i].isSelected()) {
				JComponent sub = findFocusComponent(frames[i]);
				if (sub != null) {
					RayUtil.shotScreen(sub);
				}
				return;
			}
		}
	}

	class ShotScreen extends SwingEvent {
		boolean full = false;

		public ShotScreen(boolean b) {
			super();
			full = b;
		}

		public void process() {
			if (full) {
				fullShotScreen();
			} else {
				scrollShotScreen();
			}
		}
	}

	class ShowSystemMessage extends SwingEvent {

		public ShowSystemMessage() {
			super();
		}

		public void process() {
			RayNotifyDialog dlg = RayNotifyDialog.getInstance();
			// 如果在可视状态，变成不可视；否则为可视状态
			if (dlg != null) {
				if (dlg.isVisible() && dlg.isShowing()) {
					dlg.setVisible(false);
				} else {
					dlg.doShow();
				}
			}
		}
	}

	class HideOrShowSystemDock extends SwingEvent {
		public HideOrShowSystemDock() {
			super();
		}

		public void process() {
			boolean on = dock.isVisible();
			dock.setVisible(!on);
		}
	}

	class HideOrShowSystemStatusBar extends SwingEvent {
		public HideOrShowSystemStatusBar() {
			super();
		}

		public void process() {
			boolean on = status.isVisible();
			// 如果在可视状态，关闭托盘窗口
			if (on) {
				status.closeTrayWindow();
			}
			status.setVisible(!on);
		}
	}

	/**
	 * 判断是复制屏幕上的窗口
	 * @param e
	 * @return
	 */
	private boolean isShotFullScreen(KeyEvent e) {
		return e.isControlDown() && e.isShiftDown()
		&& e.getKeyCode() == KeyEvent.VK_INSERT;
	}

	/**
	 * 判断是COPY屏幕的全部显示部分
	 * @param e
	 * @return
	 */
	private boolean isShotScreen(KeyEvent e) {
		return e.isControlDown() && e.isAltDown()
		&& e.getKeyCode() == KeyEvent.VK_INSERT;
	}

	/**
	 * 显示系统消息窗口
	 * @param e
	 * @return
	 */
	private boolean isShowOrHideSystemMessage(KeyEvent e) {
		return e.isControlDown() && e.isShiftDown()
		&& e.getKeyCode() == KeyEvent.VK_M;
	}

	/**
	 * 隐藏应用坞
	 * @param e
	 * @return
	 */
	private boolean isShowOrHideSystemDock(KeyEvent e) {
		return e.isControlDown() && e.isShiftDown()
		&& e.getKeyCode() == KeyEvent.VK_D;
	}

	/**
	 * 如果按下F1，是显示帮助窗口
	 * @param e
	 * @return
	 */
	private final boolean isShowHelp(KeyEvent e) {
		return e.getKeyCode() == KeyEvent.VK_F1;
	}

	/**
	 * 如果按下F5，是刷新
	 * @param e
	 * @return
	 */
	private final boolean isRefreshDesktop(KeyEvent e) {
		return e.getKeyCode() == KeyEvent.VK_F5;
	}

	/**
	 * 隐藏任务栏
	 * @param e
	 * @return
	 */
	private boolean isShowOrHideSystemTray(KeyEvent e) {
		return e.isControlDown() && e.isShiftDown()
		&& e.getKeyCode() == KeyEvent.VK_Y;
	}

	/**
	 * 在桌面和当前打开窗口之间切换
	 * @param e 
	 * @return
	 */
	private boolean isShowOrHideDesktop(KeyEvent e) {
		return e.isControlDown() && e.isShiftDown()
				&& e.getKeyCode() == KeyEvent.VK_F;
	}

	/**
	 * 隐藏任务栏
	 * @param e
	 * @return
	 */
	private boolean isShowOrHideSystemStatusBar(KeyEvent e) {
		return e.isControlDown() && e.isShiftDown()
		&& e.getKeyCode() == KeyEvent.VK_T;
	}

	/**
	 * 增加DOCK宽度， SHIFT + UP
	 * @param e
	 * @return
	 */
	private boolean isExpandSytemDockWidth(KeyEvent e) {
		return e.isControlDown() && e.isShiftDown()
		&& (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS);
	}

	/**
	 * 缩小DOCKER宽度，SHIFT + DOWN
	 * @param e
	 * @return
	 */
	private boolean isShrinkSytemDockWidth(KeyEvent e) {
		return e.isControlDown() && e.isShiftDown()
		&& (e.getKeyCode() == KeyEvent.VK_UNDERSCORE || e.getKeyCode() == KeyEvent.VK_MINUS);
	}

	/**
	 * 判断CTRL+TAB按下，弹出窗口
	 * @param e 键盘实例
	 * @return 返回真或者假
	 */
	private boolean isPressSwitchWindow(KeyEvent e) {
		// 窗口有效时，忽略，返回假
		if (switchWindow != null) {
			return false;
		}

		boolean success = (e.isControlDown() && (e.getID() == KeyEvent.KEY_PRESSED && 
				e.getKeyCode() == KeyEvent.VK_TAB));
		if (success) {
			switchWindow = new RayCycleWindow(this);
			// 显示切换页窗口
			switchWindow.showRunFrames(status.getLightFrames());
			switchWindow.setVisible(true);
		}
		return success;
	}

	/**
	 * 释放切换窗口
	 * @param e
	 */
	private void releaseSwitchWindow(KeyEvent e) {
		if (switchWindow != null) {
			if (!e.isControlDown() && e.getKeyCode() != KeyEvent.VK_TAB) {
				// 显示选中的窗口
				switchWindow.showSelectFrame();
				// 关闭窗口
				switchWindow.setVisible(false);
				switchWindow = null;
			}
		}
	}

	/**
	 * 判断切换窗口执行中
	 * @param e
	 * @return
	 */
	private boolean isSwitchWindow(KeyEvent e) {
		if (switchWindow != null && e.isControlDown()) {
			if (e.getKeyCode() == KeyEvent.VK_TAB) {
				// 按下时，切换到下一个
				if (e.getID() == KeyEvent.KEY_PRESSED) {
					switchWindow.nextSelectFrame();
					return true;
				} else if (e.getID() == KeyEvent.KEY_RELEASED) {
					return true;
				}
			}
		}
		return false;
	}

	class SystemKeyboard implements KeyEventPostProcessor {

		/*
		 * (non-Javadoc)
		 * @see java.awt.KeyEventPostProcessor#postProcessKeyEvent(java.awt.event.KeyEvent)
		 */
		@Override
		public boolean postProcessKeyEvent(KeyEvent e) {
			// 如果处于全屏幕状态时
			if (fullScreen.hasFull()) {
				return false;
			}

			// 弹出/关闭切换窗口，最高优先级
			if (isPressSwitchWindow(e)) {
				return true;
			} else if (isSwitchWindow(e)) {
				return true;
			} else {
				releaseSwitchWindow(e);
			}

			// 判断是按下，否则忽略
			if (e.getID() != KeyEvent.KEY_RELEASED) {
				return false;
			}

			if (isShowHelp(e)) {
				doHelp();
			} else if (isRefreshDesktop(e)) {
				doRefreshDesktop();
			} else if (isShotFullScreen(e)) {
				addThread(new ShotScreen(true));
			} else if (isShotScreen(e)) {
				addThread(new ShotScreen(false));
			} else if (isShowOrHideSystemMessage(e)) {
				addThread(new ShowSystemMessage());
			} else if (isShowOrHideSystemDock(e)) {
				addThread(new HideOrShowSystemDock());
			} else if (isShowOrHideSystemStatusBar(e)) {
				addThread(new HideOrShowSystemStatusBar());
			} 
			// 动态伸缩DOCKER
			else if (isExpandSytemDockWidth(e)) {
				if (dock.isVisible()) {
					dock.modifyWidth(true);
				}
			} else if (isShrinkSytemDockWidth(e)) {
				if (dock.isVisible()) {
					dock.modifyWidth(false);
				}
			}
			// 显示/关闭托盘
			else if (isShowOrHideSystemTray(e)) {
				status.switchTrayWindow();
			}
			// 显示或者隐藏桌面
			else if(isShowOrHideDesktop(e)) {
				addThread(new MiniButtonClickThread());
			}
			// 其它
			else {
				return false;
			}
			return true;
		}
	}

	/**
	 * 注册键盘事件
	 */
	private void registerKeyboard() {
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventPostProcessor(new SystemKeyboard());
	}

	/**
	 * 屏幕最大尺寸
	 * @return
	 */
	private Rectangle getFullScreen() {
		// 默认是全屏
		Dimension d = getToolkit().getScreenSize();
		return new Rectangle(0, 0, d.width, d.height);
	}

	/**
	 * 设置窗口参数
	 */
	private void initDesktop() {
		// 系统监听器
		PlatformListener[] systemListeners = new PlatformListener[] {
				new RayCommandDispatcher(), new DesktopAdapter(),
				new RaySoundAdapter(), new RayResourceAdapter(),
				new HelpAdapter(), new RayGradeAdapter() };

		// 状态条
		status = new RayStatusBar();
		// 桌面
		desktop = new RayDesktop(systemListeners, status);
		// 弹出菜单
		desktop.addMouseListener(new PopupMenuAdapter());

		// 桌面窗口监听器
		status.setSelectFrameListener(this);

		// 应用坞
		dock = new RayDock();

		// 找到标题和图标
		String title = UIManager.getString("RayWindow.Title");
		ImageIcon icon = (ImageIcon) UIManager.getIcon("RayWindow.TitleIcon");

		// 标题和图标
		setTitle(title);
		setIconImage(icon.getImage());

		// 状态
		RayWindowAdapter adapter = new RayWindowAdapter();
		addWindowListener(adapter);
		addWindowStateListener(adapter);
		addWindowFocusListener(adapter);

		// 忽略关闭窗口操作，RayWindowAdapter.windowClosing方法做成空方法
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	/**
	 * 初始化窗口组件
	 */
	private void initControls() {
		// 建立启动菜单
		createLaunchMenu();

		// 建立弹出菜单
		createPopupMenu();

		// 底部状态栏
		status.init();
		status.addStartButtonActionListener(new StartButtonAdapter());
		status.addMiniButtonActionListener(new MiniButtonAdapter());
		status.startDaemonThread();

		// 中心面板
		ScreenPanel panel = new ScreenPanel();
		panel.init(desktop, status);
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(panel);

		// 设置桌面和读取桌面图标参数
		RayController.getInstance().setDesktop(desktop);

		// 生成句柄，保存到静态显示环境
		RayNotifyDialog dialog = RayNotifyDialog.getInstance(desktop);
		RayInvoker.setDefaultDisplay(dialog);

		// 从磁盘上加载桌面上的应用，但是显示这些应用

		// 无边框
		try {
			setUndecorated(true);
		} catch (IllegalComponentStateException e) {

		}
		// 调整窗口尺寸，适应控件
		pack();
		// 设置默认的窗口范围
		setBounds(getFullScreen());

		// 注册键盘事件
		registerKeyboard();
	}

	private void initApplications() {
		// 加载系统应用
		int sys = initSystemApplications();
		// 加载应用到菜单
		int users = initUserApplications();
		// 有加载，更新字体和边框和监听事件
		if (sys > 0 || users > 0) {
			desktopLaunchMenu.updateFontAndBorder(false);
			desktopLaunchMenu.updateActionListener();
		}
	}

	/**
	 * 生成桌面
	 *
	 * @author scott.liang
	 * @version 1.0 7/25/2021
	 * @since laxcus 1.0
	 */
	class CreateDesktopThread extends SwingEvent {
		CreateDesktopThread(){
			super();
		}

		public void process() {
			initDesktop();
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

		// 本窗口不需要位于其它窗口上方
		setAlwaysOnTop(false);
		// 置于后端
		toBack();
	}

	class SuffixProcessThread extends SwingEvent {
		public SuffixProcessThread(){
			super(true);
		}
		public void process(){
			suffixProcess();
		}
	}

	/**
	 * 设置桌面背景
	 * 参数见RayPropertiesDialog.BackgroundPane
	 */
	private void setRayBackground() {
		Color color = RTKit.readColor(RTEnvironment.ENVIRONMENT_SYSTEM, "RayWindow/Background/Color");
		String filename = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "RayWindow/Background/File");
		int layout = RTKit.readInteger(RTEnvironment.ENVIRONMENT_SYSTEM, "RayWindow/Background/Layout");

		DesktopWall wall = new DesktopWall();
		wall.setColor(color);
		// 有效时
		if (filename != null && layout > 0) {
			File file = new File(filename);
			// 判断文件存在...
			boolean exists = (file.exists() && file.isFile());
			if (exists) {
				wall.setFile(file);
				wall.setLayout(layout);
				try {
					// 读取图片
					Image image = ImageIO.read(file);
					wall.setImage(image);
				} catch (IOException e) {
					Logger.error(e);
				}
			}
		}
		// 设置背景
		if (wall.getColor() != null || wall.getImage() != null) {
			desktop.setDesktopWall(wall);
		}
	}

	class ShowRayWindow extends SwingEvent {
		public ShowRayWindow() {
			super(true);
		}

		public void process() {
			// 设置背景
			setRayBackground();
			// 显示窗口
			setVisible(true);
			// 延时...
			sleep(500);
		}
	}

	class ShowRayButtons extends SwingEvent {
		public ShowRayButtons(){
			super(true);
		}
		@Override
		public void process() {
			// 显示图标
			RayController.getInstance().showAllButtons(status.getHeight());
			
			// 显示测试用按纽
//			testTockButton();
		}
	}

	class ShowEdgeSystemButtons extends SwingEvent {
		ShowEdgeSystemButtons(){
			super(true);
		}
		public void process() {
			dock.loadDefaultEdgeButtons();
		}
	}

	class ShowDockUserButtons extends SwingEvent {
		public ShowDockUserButtons() {
			super(true);
		}

		@Override
		public void process() {
			// 加载配置环境中的用户级应用按纽
			dock.loadDefaultDockButtons();
		}
	}

	class EdgeAdapter implements RayEdgeListener {

		/* (non-Javadoc)
		 * @see com.laxcus.ray.dock.EdgeListener#doRunFromEdge()
		 */
		@Override
		public void doRunFromEdge() {
			doRun();
		}

		/* (non-Javadoc)
		 * @see com.laxcus.ray.dock.EdgeListener#doShutdownFromEdge()
		 */
		@Override
		public void doShutdownFromEdge() {
			exit();
		}
	}

	class ShowRayDock extends SwingEvent {
		public ShowRayDock() {
			super(true);
		}

		public void process() {
			dock.showWindow(new EdgeAdapter());
		}
	}

	class InitApplications extends SwingEvent {
		InitApplications() {
			super(true);
		}

		public void process(){
			initApplications();
		}
	}

	class ShowNotifyDialog extends SwingEvent {
		ShowNotifyDialog() {
			super(true);
		}

		@Override
		public void process() {
			// 显示弹出窗口
			RayNotifyDialog dlg = RayNotifyDialog.getInstance();
			if (dlg != null) {
				boolean show = dlg.isAlwaysShow();
				if (show) {
					dlg.doShow();
				}
			}
		}
	}
	
	class UsabledThread extends SwingEvent {
		public UsabledThread() {
			super();
		}

		public void process() {
			RTEnvironment.getInstance().setUsabled(true);
			RTManager.getInstance().setUsabled(true);
			RayController.getInstance().setUsabled(true);
		}
	}

	/**
	 * 显示登录窗口和执行登录动作
	 * @return 成功返回真，失败返回假
	 */
	private boolean showLoginWindow() {
		boolean success = __login(true);
		if (!success) {
			dispose();
		}
		return success;
	}

	/**
	 * 延时
	 * @param timeout
	 */
	private synchronized void delay(long timeout) {
		try {
			if (timeout > 0L) {
				wait(timeout);
			}
		} catch (InterruptedException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}

	/**
	 * 等待，直到空闲后释放...
	 */
	private void waitForIdle() {
		// 如果是忙状态，等待
		do {
			delay(1000L);
			if (SwingDispatcher.getInstance().isIdle()) {
				break;
			}
		} while (true);
	}

	/**
	 * 显示窗口并且注册到服务器
	 * @return 返回真或者假
	 */
	public boolean showWindow() {
		// 生成桌面
		addThread(new CreateDesktopThread());
		// 用线程更新UI界面
		reloadUI();

		// 等待AWT事件派发线程进入空闲状态
		waitForIdle();

		// 执行登录
		boolean success = showLoginWindow();
		// 最后显示界面
		if (success) {
			// 加入线程，注意！顺序不能乱
			addThread(new SuffixProcessThread()); // 后续处理
			addThread(new ShowRayWindow()); // 显示桌面窗口
			addThread(new ShowRayDock()); // 显示应用坞
			addThread(new ShowRayButtons()); // 显示桌面上的按纽

			addThread(new ShowEdgeSystemButtons()); // 边缘侧的系统按纽
			addThread(new InitApplications()); // 初始化应用
			addThread(new ShowDockUserButtons()); // 显示DOCK上用户按纽，必须放在InitApplications之后，因为InitApplications在加载系统应用
			addThread(new ShowNotifyDialog());

			// 等待AWT事件派发线程进入空闲状态
			waitForIdle();

			// 在等待结束后，强制延时10秒钟，这个时间不处理任何事件
			// 原因：在SWING启动之后显示时，会出现UI卡死现象，让SwingDispatcher程延时，不处理任何SWING事件，
			// 能够使VM有充足的空闲时间来处理已经执行的SWING事件
			SwingDispatcher.getInstance().setSlack(12 * 1000);
			
			// 环境参数为可用
			addThread(new UsabledThread());
		}
		return success;
	}

}