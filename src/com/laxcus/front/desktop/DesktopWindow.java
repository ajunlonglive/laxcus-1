/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

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
import com.laxcus.front.*;
import com.laxcus.front.desktop.cycle.*;
import com.laxcus.front.desktop.dialog.build.*;
import com.laxcus.front.desktop.dialog.install.*;
import com.laxcus.front.desktop.dialog.login.*;
import com.laxcus.front.desktop.dialog.notify.*;
import com.laxcus.front.desktop.dialog.properties.*;
import com.laxcus.front.desktop.dialog.run.*;
import com.laxcus.front.desktop.dialog.shutdown.*;
import com.laxcus.front.desktop.dialog.uninstall.*;
import com.laxcus.front.desktop.dispatcher.*;
import com.laxcus.front.desktop.dock.*;
import com.laxcus.front.desktop.frame.help.*;
import com.laxcus.front.desktop.frame.log.*;
import com.laxcus.front.desktop.invoker.*;
import com.laxcus.front.desktop.menu.*;
import com.laxcus.front.desktop.status.*;
import com.laxcus.front.desktop.util.*;
import com.laxcus.gui.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.gui.frame.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.platform.control.*;
import com.laxcus.platform.listener.*;
import com.laxcus.register.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
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
public class DesktopWindow extends JFrame implements ActionListener, DesktopUIUpdater, InstallFactory, UninstallFactory, DesktopSelectFrameListener {

	private static final long serialVersionUID = -9096105623362635798L;;

	/** 桌面面板 **/
	protected PlatformDesktop desktop;

	/** 状态栏 **/
	protected DesktopStatusBar status;

	/** 应用坞，显示应用软件 **/
	protected DesktopDock dock;

	/** 切换页窗口 **/
	private DesktopCycleWindow switchWindow;

	/** 启动菜单 **/
	private DesktopLaunchMenu desktopLaunchMenu;

	/** 桌面弹出菜单 **/
	private DesktopRockMenu desktopRockMenu;

	/** 登录标记 **/
	private LoginToken loginToken;

	/** 类型名称和启动KEY的关联 **/
	private TreeMap<String, StartKey> startKeys = new TreeMap<String, StartKey>();

	private DesktopFullScreenAdapter fullScreen;

	/**
	 * 桌面窗口
	 * @throws HeadlessException
	 */
	public DesktopWindow() throws HeadlessException {
		super();
		this.fullScreen = new DesktopFullScreenAdapter(this);
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
		if (Laxkit.isClassFrom(element, WProgram.class)) {
			WProgram wp = (WProgram) element;
			if (wp.isDesktop()) {
				WKey key = wp.getKey();
				boolean exists = DesktopController.getInstance().hasDesktopButton(key);
				if (!exists) {
					DesktopButtonItem bt = new DesktopButtonItem(wp);
					DesktopController.getInstance().doDesktopButton(bt);
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
					DesktopDockButtonItem bt = new DesktopDockButtonItem(wp, system);
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
	 * @see com.laxcus.front.desktop.InstallFactory#setup(com.laxcus.application.manage.WRoot)
	 */
	@Override
	public boolean setup(WRoot boot) {
		JMenuItem menuItem = BootMenuCreator.createMenu(boot);
		if (menuItem == null) {
			Logger.error(this, "setup", "cannot be create menu!");
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
			Logger.error(this, "setup", "cannot be associate!");
			return false;
		}

		JMenu menu = DesktopLaunchMenuCreator.findMenu(desktopLaunchMenu, associate);
		if (menu == null) {
			Logger.error(this, "setup", "cannot be find menu!!");
			return false;
		}

		// 保存参数
		boolean success = RTManager.getInstance().add(boot);
		if (!success) {
			Logger.error(this, "setup", "cannot be add!");
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
			boolean success = DesktopController.getInstance().hasDesktopButton(key);
			if (success) {
				success = DesktopController.getInstance().deleteDesktopButton(key);
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

		return count>=0;
	}

	//	/**
	//	 * 删除应用坞按纽
	//	 * @param element
	//	 * @return 返回真或者假
	//	 */
	//	private boolean deleteDockButton(WElement element) {
	//		int count = 0;
	//		// 删除应用
	//		if (Laxkit.isClassFrom(element, WProgram.class)) {
	//			WProgram wp = (WProgram) element;
	//			WKey key = wp.getKey();
	//			boolean success = dock.hasButton(key); 
	//			if (success) {
	//				success = dock.deleteButton(key); 
	//				if (success) {
	//					count++;
	//				} else {
	//					return false;
	//				}
	//			}
	//		}
	//
	//		// 删除子任务
	//		if (Laxkit.isClassFrom(element, WDirectory.class)) {
	//			WDirectory dir = (WDirectory) element;
	//			for (WElement sub : dir.getTokens()) {
	//				boolean success = deleteDockButton(sub);
	//				if (success) {
	//					count++;
	//				} else {
	//					return false;
	//				}
	//			}
	//		}
	//
	//		//		System.out.printf("删除 %s %s\n", element.getTitle(), 
	//		//				(count > 0 ? "Yes" : "No"));
	//
	//		return count > 0;
	//	}

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
	 * @see com.laxcus.front.desktop.UninstallFactory#deleteApplication(com.laxcus.application.manage.WRoot)
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
		JMenu menu = DesktopLaunchMenuCreator.findMenu(desktopLaunchMenu, associate);
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
		//		addThread(new ActionThread(event));
		click(event);
	}

	//	class ActionThread extends SwingEvent {
	//		ActionEvent event;
	//
	//		ActionThread(ActionEvent e) {
	//			super();
	//			event = e;
	//		}
	//
	//		public void process() {
	//			click(event);
	//		}
	//	}

	//	/**
	//	 * 从菜单上启动应用
	//	 * @param key
	//	 */
	//	private void startApplication(WKey key) {
	//		int ret = -1;
	//		try {
	//			ret = ApplicationStarter.start(key);
	//		} catch (SecurityException e) {
	//			Logger.error(e);
	//		} catch (IllegalArgumentException e) {
	//			Logger.error(e);
	//		} catch (IOException e) {
	//			Logger.error(e);
	//		} catch (InstantiationException e) {
	//			Logger.error(e);
	//		} catch (IllegalAccessException e) {
	//			Logger.error(e);
	//		} catch (NoSuchMethodException e) {
	//			Logger.error(e);
	//		} catch (InvocationTargetException e) {
	//			Logger.error(e);
	//		} catch(Throwable e) {
	//			Logger.fatal(e);
	//		}
	//		if (ret != 0) {
	//			String title = UIManager.getString("DesktopWindow.StartErrorTitle");
	//			String content = UIManager.getString("DesktopWindow.StartErrorContent");
	//			// 弹出错误
	//			getLauncher().playSound(SoundTag.ERROR);
	//			MessageBox.showFault(desktop, title, content); // "出错", "不能正确启动应用");
	//		}
	//	}

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
			DesktopStarter starter = new DesktopStarter(desktop, startKeys);
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
	 * 重置分布式数据库客户端
	 */
	private void resetDatabaseClient() {
		DatabaseClient[] as = PlatformKit.findListeners(DatabaseClient.class);
		int size = (as != null ? as.length : 0);
		for (int i = 0; i < size; i++) {
			DatabaseClient client = as[i];
			client.reset(); //重置
		}
	}

	/**
	 * 重置分布组件客户端
	 */
	private void resetWareClient() {
		WareClient[] as = PlatformKit.findListeners(WareClient.class);
		int size = (as != null ? as.length : 0);
		for (int i = 0; i < size; i++) {
			WareClient client = as[i];
			client.reset(); //重置
		}
	}
	
	/**
	 * 将窗口的显示单元置于无效状态
	 */
	private void __disableWindow() {
		setOfflineIcon();
		setInterruptIocn();
		setStatusText(null);

		// 图形界面显示注销状态。这时的登录菜单生效，退出登录/重新登录失效
		setLoginMenuEnabled(true);
		
		// 重置分布式数据库客户端
		resetDatabaseClient();
		// 重置分布式组件客户端
		resetWareClient();
	}

	//	/**
	//	 * 设置自动登录信息
	//	 * @param xmlPath XML标签路径
	//	 * @param hub 服务器地址
	//	 */
	//	private void setAutoText(String xmlPath) {
	//		String content = UIManager.getString(xmlPath);
	//		if (content != null) {
	//			setStatusText(content);
	//		}
	//	}

	/**
	 * 设置错误信息
	 * @param xmlPath
	 */
	private void setErrorText(String xmlPath) {
		String content = UIManager.getString(xmlPath);
		if (content != null) {
			DesktopNotifyDialog dlg = DesktopNotifyDialog.getInstance();
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
			DesktopNotifyDialog dlg = DesktopNotifyDialog.getInstance();
			if (dlg != null) {
				dlg.message(content, true);
			}
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
		setErrorText("DesktopWindow.AutoLogin.RetryText");

		// 登录ENTRANCE服务器，分配资源！
		int who = DesktopLauncher.getInstance().login(entrance.getHost(), true, null);
		// 判断成功
		boolean success = FrontEntryFlag.isSuccessful(who);
		// 不成功，保存初始HUB地址
		if (!success) {
			DesktopLauncher.getInstance().setInitHub(entrance);
			setErrorText("DesktopWindow.AutoLogin.FailedText");
			return false;
		}

		// 设置GATE服务器地址
		setHubTip(DesktopLauncher.getInstance().getHub().toString());
		// 登录成功后，登录菜单失效，注销和重新登录有效
		setLoginMenuEnabled(false);
		// 图标显示用户属性
		showCyberStatus();

		setMessageText("DesktopWindow.AutoLogin.SuccessText");

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
		DesktopLogoutThread thread = new DesktopLogoutThread(this);
		thread.start();
	}

	/**
	 * 重新注册。先注销，再注册
	 */
	protected void relogin() {
		DesktopReloginThread thread = new DesktopReloginThread(this);
		thread.start();
	}

	/**
	 * 运行应用
	 */
	protected void doRun() {
		DesktopRunDialog dlg = DesktopRunDialog.getInstance();
		if (dlg == null) {
			DesktopRunDialog dialog = new DesktopRunDialog();
			dialog.showDialog(desktop, false);
		} else {
			setSelectFrame(dlg, true);
		}
	}

	//	/**
	//	 * 关闭
	//	 */
	//	private void shutdown() {
	////		// 关闭子窗口
	////		closeSubForms();
	//		
	////		// 延时，直到全部清除
	////		waitForIdle();
	//
	////		// 停止和退出线程
	////		bottom.stopDaemonThread();
	//
	//		// 写入按纽单元
	//		DesktopController.getInstance().writeButtons();
	//
	//		// 关闭窗口
	//		setVisible(false);
	//
	//		// 通知进程退出
	//		DesktopLauncher.getInstance().stop();
	//	}



	class Shutdown1Thread extends SwingEvent {

		Shutdown1Thread() {
			super(true);
		}

		public void process() {
			// // 关闭状态条上的托盘窗口
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
			if (Laxkit.isClassFrom(frame, DesktopShutdownDialog.class)) {
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
			DesktopController.getInstance().writeButtons();

			sleep(800L);

			// 关闭窗口
			setVisible(false);

			// 通知进程退出
			DesktopLauncher.getInstance().stop();
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
		DesktopShutdownDialog dialog = new DesktopShutdownDialog();
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

	//	/**
	//	 * 关闭窗口
	//	 * 包括窗体和对话框
	//	 */
	//	private void closeSubForms() {
	//		JInternalFrame[] frames = desktop.getAllFrames();
	//		int size = (frames == null ? 0 : frames.length);
	//		for (int i = 0; i < size; i++) {
	//			JInternalFrame frame = frames[i];
	//			if (frame == null) {
	//				continue;
	//			}
	//			if (Laxkit.isClassFrom(frame, DesktopShutdownDialog.class)) {
	//				continue;
	//			}
	//			// 是子窗口，关闭它们
	//			if ( Laxkit.isClassFrom(frame, LightForm.class)) {
	//				System.out.printf("释放：%s\n", frame.getTitle());
	//				((LightForm) frame).closeWindow();
	//				
	//				delay(500L); // 延时，同时让VM释放内存中的记录
	//				desktop.revalidate();
	//				desktop.repaint();
	//			}
	//		}
	//	}



	//	/**
	//	 * 关闭窗口
	//	 * 包括窗体和对话框
	//	 */
	//	private void closeSubForms() {
	////		 ArrayList<SwingEvent> array = new ArrayList<SwingEvent>();
	//
	//		// long now = System.currentTimeMillis();
	//
	////		final int interval = 2000;
	////		long touchTime = System.currentTimeMillis();
	//
	//		JInternalFrame[] frames = desktop.getAllFrames();
	//		int size = (frames == null ? 0 : frames.length);
	//		for (int i = 0; i < size; i++) {
	//			JInternalFrame frame = frames[i];
	//			if (frame == null) {
	//				continue;
	//			}
	//			if (Laxkit.isClassFrom(frame, DesktopShutdownDialog.class)) {
	//				continue;
	//			}
	//			// 是子窗口，关闭它们
	//			if (Laxkit.isClassFrom(frame, LightForm.class)) {
	////												System.out.printf("释放：%s\n", frame.getTitle());
	//				//								((LightForm) frame).closeWindow();
	//				//				
	//				//								delay(500L); // 延时，同时让VM释放内存中的记录
	//				//								desktop.revalidate();
	//				//								desktop.repaint();
	//
	//				ShutdownWindowThread event = new ShutdownWindowThread((LightForm) frame);
	////				event.setIntervalTime(interval);
	////				event.setTouchTime(touchTime);
	////				array.add(event);
	//				
	//				SwingDispatcher.invokeThread(event);
	//				
	////				// 下一次触发时间
	////				touchTime += interval;
	//			}
	//		}
	//
	////		SwingDispatcher.invokeThreads(array);
	//	}

	//	/**
	//	 * 关闭窗口
	//	 * 包括窗体和对话框
	//	 */
	//	private void closeSubForms() {
	//		JInternalFrame[] frames = desktop.getAllFrames();
	//		int size = (frames == null ? 0 : frames.length);
	//		for (int i = 0; i < size; i++) {
	//			JInternalFrame frame = frames[i];
	//			if (frame == null) {
	//				continue;
	//			}
	//			if (Laxkit.isClassFrom(frame, DesktopShutdownDialog.class)) {
	//				continue;
	//			}
	//			// 是子窗口，关闭它们
	//			if (Laxkit.isClassFrom(frame, LightForm.class)) {
	//				ShutdownWindowThread event = new ShutdownWindowThread((LightForm) frame);
	//				SwingDispatcher.invokeThread(event);
	//			}
	//		}
	//	}

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
			if (Laxkit.isClassFrom(frame, DesktopLogFrame.class)) {
				logs = ((DesktopLogFrame) frame).setMaxLogs(logs);
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

	class DesktopWindowAdapter extends WindowAdapter {

		DesktopWindowAdapter() {
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
			DesktopStarter starter = new DesktopStarter(desktop, startKeys);
			starter.run(object);
		}
	}

	//	/**
	//	 * 找到注册表中的WKey定义，关联的写入在DesktopStartDialog
	//	 * @param type 类型
	//	 * @return 返回注册的WKey实例，没有是空指针
	//	 */
	//	private WKey findWKey(String type) {
	//		String path = String.format("StartTypes/%s", type);
	//		String value = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, path);
	//		// 有效，解析它
	//		if (WKey.validate(value)) {
	//			return WKey.translate(value);
	//		}
	//		return null;
	//	}

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



	//	private String[] getLinks(Object o) {
	//		String[] links = null;
	//		if (o.getClass() == SRL[].class) {
	//			SRL[] srls = (SRL[]) o;
	//			links = new String[srls.length];
	//			for (int i = 0; i < srls.length; i++) {
	//				links[i] = srls[i].toString();
	//			}
	//		} else if (o.getClass() == File[].class) {
	//			File[] files = (File[]) o;
	//			links = new String[files.length];
	//			for (int i = 0; i < files.length; i++) {
	//				links[i] = Laxkit.canonical(files[i]);
	//			}
	//		} else if (o.getClass() == SRL.class) {
	//			SRL srl = (SRL) o;
	//			links = new String[1];
	//			links[0] = srl.toString();
	//		} else if (o.getClass() == File.class) {
	//			File file = (File) o;
	//			links = new String[1];
	//			links[0] = Laxkit.canonical(file);
	//		}
	//		return links;
	//	}

	//	private WKey doWKey(String link) {
	//		// 取出关联类型
	//		int last = link.lastIndexOf(".");
	//		if (last == -1) {
	//			// 出错
	//			String title = UIManager.getString("DesktopWindow.StartFileUnknowTitle");
	//			String content = UIManager.getString("DesktopWindow.StartFileUnknowContent");
	//			content = String.format(content, link);
	//			MessageBox.showWarning(desktop, title, content); //"出错", "这不是一个合适的启动类型");
	//			return null;
	//		}
	//		String suffix = link.substring(last + 1);
	//		suffix = suffix.toLowerCase().trim();
	//		
	//		// 找到WKEY
	//		WKey key = findWKey(suffix);
	//
	//		// 找到匹配的
	//		if (key == null) {
	//			StartKey startKey = startKeys.get(suffix);
	//			if (startKey == null) {
	//				// 合并全部，输出!
	//				startKey = new StartKey(suffix);
	//				for (StartKey st : startKeys.values()) {
	//					startKey.add(st.get(0)); // 只取第一个，因为StartToken是一样的，只是type不同
	//				}
	//				DesktopStartDialog dlg = new DesktopStartDialog(startKey, link);
	//				StartToken token = dlg.showDialog(this);
	//				if (token != null) {
	//					key = token.getKey();
	//				}
	//			} else {
	//				if (startKey.size() > 1) {
	//					DesktopStartDialog dlg = new DesktopStartDialog(startKey, link);
	//					StartToken token = dlg.showDialog(this);
	//					if (token != null) {
	//						key = token.getKey();
	//					}
	//				} else {
	//					key = startKey.get(0).getKey();
	//				}
	//			}
	//		}
	//		// 解析没有定义，忽略退出
	//		return key;
	//	}

	//	/**
	//	 * 启动
	//	 * @param starter
	//	 */
	//	private void start(Starter starter) {
	//		WKey key = starter.getKey();
	//		String[] links = starter.toArray();
	//		int ret = -1;
	//		try {
	//			ret = ApplicationStarter.start(key, links);
	//		} catch (SecurityException e) {
	//			Logger.error(e);
	//		} catch (IllegalArgumentException e) {
	//			Logger.error(e);
	//		} catch (IOException e) {
	//			Logger.error(e);
	//		} catch (InstantiationException e) {
	//			Logger.error(e);
	//		} catch (IllegalAccessException e) {
	//			Logger.error(e);
	//		} catch (NoSuchMethodException e) {
	//			Logger.error(e);
	//		} catch (InvocationTargetException e) {
	//			Logger.error(e);
	//		} catch(Throwable e) {
	//			Logger.fatal(e);
	//		}
	//		if (ret != 0) {
	//			String title = UIManager.getString("DesktopWindow.StartErrorTitle");
	//			String content = UIManager.getString("DesktopWindow.StartErrorContent");
	//			// 弹出错误
	//			getLauncher().playSound(SoundTag.ERROR);
	//			MessageBox.showFault(desktop, title, content); // "出错", "不能正确启动应用");
	//		}
	//	}

	//	/**
	//	 * 根据文件名启动对应的应用软件，打开它
	//	 * @param o
	//	 */
	//	private void startApplication(Object o) {
	//		// 取出运行链
	//		String[] links = getLinks(o);
	//		if (links == null) {
	//			return;
	//		}
	//
	//		TreeMap<WKey, Starter> starts = new TreeMap<WKey, Starter>();
	//
	//		for (int i = 0; i < links.length; i++) {
	//			String link = links[i];
	//			WKey key = doWKey(link);
	//			if (key == null) {
	//				continue;
	//			}
	//			Starter element = starts.get(key);
	//			if (element == null) {
	//				element = new Starter(key);
	//				starts.put(element.getKey(), element);
	//			}
	//			element.add(link);
	//		}
	//
	//		Iterator<Map.Entry<WKey, Starter>> iterator  = starts.entrySet().iterator();
	//		while(iterator.hasNext()) {
	//			Map.Entry<WKey, Starter> entry = iterator.next();
	//			Starter starter = entry.getValue();
	//			start(starter);
	//		}
	//
	//		//		// 取出关联类型
	//		//		int last = link.lastIndexOf(".");
	//		//		if (last == -1) {
	//		//			// 出错
	//		//			String title = UIManager.getString("DesktopWindow.StartFileUnknowTitle");
	//		//			String content = UIManager.getString("DesktopWindow.StartFileUnknowContent");
	//		//			content = String.format(content, link);
	//		//			MessageBox.showWarning(desktop, title, content); //"出错", "这不是一个合适的启动类型");
	//		//			return;
	//		//		}
	//		//		String suffix = link.substring(last + 1);
	//		//		suffix = suffix.toLowerCase().trim();
	//		//		
	//		//		// 找到WKEY
	//		//		WKey key = findWKey(suffix);
	//		//
	//		//		// 找到匹配的
	//		//		if (key == null) {
	//		//			StartKey startKey = startKeys.get(suffix);
	//		//			if (startKey == null) {
	//		//				// 合并全部，输出!
	//		//				startKey = new StartKey(suffix);
	//		//				for (StartKey st : startKeys.values()) {
	//		//					startKey.add(st.get(0)); // 只取第一个，因为StartToken是一样的，只是type不同
	//		//				}
	//		//				DesktopStartDialog dlg = new DesktopStartDialog(startKey, link);
	//		//				StartToken token = dlg.showDialog(this);
	//		//				if (token != null) {
	//		//					key = token.getKey();
	//		//				}
	//		//			} else {
	//		//				if (startKey.size() > 1) {
	//		//					DesktopStartDialog dlg = new DesktopStartDialog(startKey, link);
	//		//					StartToken token = dlg.showDialog(this);
	//		//					if (token != null) {
	//		//						key = token.getKey();
	//		//					}
	//		//				} else {
	//		//					key = startKey.get(0).getKey();
	//		//				}
	//		//			}
	//		//		}
	//		//		// 解析没有定义，忽略退出
	//		//		if (key == null) {
	//		//			return;
	//		//		}
	//		//
	//		//		int ret = -1;
	//		//		try {
	//		//			ret = ApplicationStarter.start(key, links);
	//		//		} catch (SecurityException e) {
	//		//			Logger.error(e);
	//		//		} catch (IllegalArgumentException e) {
	//		//			Logger.error(e);
	//		//		} catch (IOException e) {
	//		//			Logger.error(e);
	//		//		} catch (InstantiationException e) {
	//		//			Logger.error(e);
	//		//		} catch (IllegalAccessException e) {
	//		//			Logger.error(e);
	//		//		} catch (NoSuchMethodException e) {
	//		//			Logger.error(e);
	//		//		} catch (InvocationTargetException e) {
	//		//			Logger.error(e);
	//		//		} catch(Throwable e) {
	//		//			Logger.fatal(e);
	//		//		}
	//		//		if (ret != 0) {
	//		//			String title = UIManager.getString("DesktopWindow.StartErrorTitle");
	//		//			String content = UIManager.getString("DesktopWindow.StartErrorContent");
	//		//			// 弹出错误
	//		//			getLauncher().playSound(SoundTag.ERROR);
	//		//			MessageBox.showFault(desktop, title, content); // "出错", "不能正确启动应用");
	//		//		}
	//	}

	//	/**
	//	 * 根据文件名启动对应的应用软件，打开它
	//	 * @param obj
	//	 */
	//	private void startApplicationX(Object obj) {
	//		String link = obj.toString();
	//		
	////		System.out.printf("这个启动对象是：%s\n", obj.getClass().getName());
	//
	//		// 取出关联类型
	//		int last = link.lastIndexOf(".");
	//		if (last == -1) {
	//			// 出错
	//			String title = UIManager.getString("DesktopWindow.StartFileUnknowTitle");
	//			String content = UIManager.getString("DesktopWindow.StartFileUnknowContent");
	//			content = String.format(content, link);
	//			MessageBox.showWarning(desktop, title, content); //"出错", "这不是一个合适的启动类型");
	//			return;
	//		}
	//		String suffix = link.substring(last + 1);
	//		suffix = suffix.toLowerCase().trim();
	//		
	//		// 找到WKEY
	//		WKey key = findWKey(suffix);
	//
	//		// 找到匹配的
	//		if (key == null) {
	//			StartKey startKey = startKeys.get(suffix);
	//
	//			//			if (startKey == null) {
	//			//				String title = UIManager.getString("DesktopWindow.StartFileNotfoundTitle");
	//			//				String content = UIManager.getString("DesktopWindow.StartFileNotfoundContent");
	//			//				content = String.format(content, link);
	//			//				// 弹出，没有找到启动应用
	//			//				MessageBox.showFault(desktop, title, content); 
	//			//				return;
	//			//			}
	//			//			if (startKey.size() > 1) {
	//			//				DesktopStartDialog dlg = new DesktopStartDialog(startKey, link);
	//			//				StartToken token = dlg.showDialog(this);
	//			//				if (token != null) {
	//			//					key = token.getKey();
	//			//				}
	//			//			} else {
	//			//				key = startKey.get(0).getKey();
	//			//			}
	//			
	//			if (startKey == null) {
	//				// 合并全部，输出!
	//				startKey = new StartKey(suffix);
	//				for (StartKey st : startKeys.values()) {
	//					startKey.add(st.get(0)); // 只取第一个，因为StartToken是一样的，只是type不同
	//				}
	//				DesktopStartDialog dlg = new DesktopStartDialog(startKey, link);
	//				StartToken token = dlg.showDialog(this);
	//				if (token != null) {
	//					key = token.getKey();
	//				}
	//			} else {
	//				if (startKey.size() > 1) {
	//					DesktopStartDialog dlg = new DesktopStartDialog(startKey, link);
	//					StartToken token = dlg.showDialog(this);
	//					if (token != null) {
	//						key = token.getKey();
	//					}
	//				} else {
	//					key = startKey.get(0).getKey();
	//				}
	//			}
	//		}
	//		// 解析没有定义，忽略退出
	//		if (key == null) {
	//			return;
	//		}
	//
	//		int ret = -1;
	//		try {
	//			ret = ApplicationStarter.start(key, new String[]{link});
	//		} catch (SecurityException e) {
	//			Logger.error(e);
	//		} catch (IllegalArgumentException e) {
	//			Logger.error(e);
	//		} catch (IOException e) {
	//			Logger.error(e);
	//		} catch (InstantiationException e) {
	//			Logger.error(e);
	//		} catch (IllegalAccessException e) {
	//			Logger.error(e);
	//		} catch (NoSuchMethodException e) {
	//			Logger.error(e);
	//		} catch (InvocationTargetException e) {
	//			Logger.error(e);
	//		} catch(Throwable e) {
	//			Logger.fatal(e);
	//		}
	//		if (ret != 0) {
	//			String title = UIManager.getString("DesktopWindow.StartErrorTitle");
	//			String content = UIManager.getString("DesktopWindow.StartErrorContent");
	//			// 弹出错误
	//			getLauncher().playSound(SoundTag.ERROR);
	//			MessageBox.showFault(desktop, title, content); // "出错", "不能正确启动应用");
	//		}
	//	}

	//	NimbusMinimizeIcon [ICON 16*16] conf/desktop/image/frame/desktop/nimbus_mini.png
	//	NimbusMaximizeIcon [ICON 16*16] conf/desktop/image/frame/desktop/nimbus_max.png
	//	NimbusIconifyIcon [ICON 16*16] conf/desktop/image/frame/desktop/nimbus_icon.png
	//	NimbusCloseIcon [ICON 16*16] conf/desktop/image/frame/desktop/nimbus_close.png

	//	 maxIcon = UIManager.getIcon("InternalFrame.maximizeIcon");
	//	minIcon = UIManager.getIcon("InternalFrame.minimizeIcon");
	//	iconIcon = UIManager.getIcon("InternalFrame.iconifyIcon");
	//	closeIcon = UIManager.getIcon("InternalFrame.closeIcon");

	//	private void reloadNimbusTitleIcon() {
	//		Icon icon = UIManager.getIcon("NimbusIconifyIcon");
	//		UIManager.put("InternalFrame.iconifyIcon", icon);
	//
	//		icon = UIManager.getIcon("NimbusCloseIcon");
	//		UIManager.put("InternalFrame.closeIcon", icon);
	//
	//		icon = UIManager.getIcon("NimbusMinimizeIcon");
	//		UIManager.put("InternalFrame.minimizeIcon", icon);
	//
	//		icon = UIManager.getIcon("NimbusMaximizeIcon");
	//		UIManager.put("InternalFrame.maximizeIcon", icon);
	//		
	//		System.out.println("系统更新NIMBUS UI");
	//	}

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
	 * 被DesktopLauncher调用，显示动态图标！
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
		DesktopLaunchMenuCreator creator = new DesktopLaunchMenuCreator();
		return creator.findMenuItemByMethod(desktopLaunchMenu, method);
	}
	
	/**
	 * 设置数据库应用软件客户端的级别，与当前一致
	 * @param grade
	 */
	private void setClientGrade(int grade) {
		// 找到注册的数据库客户端，定义他们
		DatabaseClient[] as = PlatformKit.findListeners(DatabaseClient.class);
		int size = (as != null ? as.length : 0);
		for (int i = 0; i < size; i++) {
			DatabaseClient client = as[i];
			client.setGrade(grade);
		}
	}

	/**
	 * 调整状态栏图标显示
	 */
	public void showCyberStatus() {
		DesktopLauncher launcher = DesktopLauncher.getInstance();
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
				setOfflineIcon();
			}
		} else {
			setInterruptIocn();
			setOfflineIcon();
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

	/**
	 * 设置管理员图标
	 */
	public void setAdministratorIcon() {
		status.setAdministratorIcon();
		setClientGrade(GradeTag.ADMINISTRATOR);
	}

	/**
	 * 设置普通注册用户图标
	 */
	public void setUserIcon() {
		status.setUserIcon();
		setClientGrade(GradeTag.USER);
	}

	/**
	 * 无定义的身份图标
	 */
	public void setOfflineIcon() {
		status.setOfflineIcon();
		setClientGrade(GradeTag.OFFLINE);
	}

	/**
	 * 设置状态栏文本提示
	 * @param text
	 */
	public void setStatusText(String text) {
		DesktopNotifyDialog dlg = DesktopNotifyDialog.getInstance();
		if (dlg != null) {
			dlg.setStatusText(text);
		}
	}

	class DesktopResetHandler implements ResetHandler {
		public boolean reset() {
			ResourceReleaser rs = new ResourceReleaser();
			rs.deleteResource();
			return true;
		}
	}

	/**
	 * 登录目标站点
	 * @param showReset 恢复初始状态，只在登录时显示
	 * @return 成功返回真，否则假
	 */
	private boolean __login(boolean showReset) {
		Logger.debug(this, "__login", "into ...");

		// 显示前端的注册窗口
		DesktopResetHandler handler = (showReset ? new DesktopResetHandler() : null);
		DesktopLoginDialog dialog = new DesktopLoginDialog(this, handler);

		dialog.setModal(true);
		dialog.showDialog(loginToken);
		dialog.dispose();
		// 注册窗口取消向GATE节点注册操作
		if (dialog.isCanceled()) {
			return false;
		}

		// 设置服务器地址
		Node hub = DesktopLauncher.getInstance().getHub();
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
		String title = UIManager.getString("DesktopWindow.Forsake.Title");
		String content = UIManager.getString("DesktopWindow.Forsake.Text");
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
	//	private DesktopLauncher getLauncher() {
	//		return DesktopLauncher.getInstance();
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

	//	/**
	//	 * 选择焦点窗口
	//	 * @param frame
	//	 */
	//	private void doCallSelect(LightFrame frame) {
	//		if (frame == null) {
	//			return;
	//		}
	//		
	//		// 如果已经图标化，恢复它
	//		if (frame.isDetached()) {
	//			desktop.getDesktopManager().deiconifyFrame(frame);
	//			return;
	//		}
	//
	//		// 全部窗口
	//		JInternalFrame[] frames = desktop.getAllFrames();
	//		int size = (frames == null ? 0 : frames.length);
	//		boolean exists = false;
	//		for (int i = 0; i < size; i++) {
	//			JInternalFrame other = frames[i];
	//			// 只处理LightFrame窗口
	//			if (!(other instanceof LightFrame)) {
	//				continue;
	//			}
	//			
	//			if (other == frame) {
	//				exists = true;
	//			} else if (other.isSelected()) {
	////				setSelectFrame(other, false); // 取消选中
	//			}
	//		}
	//
	//		// 存在这个窗口...
	//		if (exists) {
	//			// 不是显示状态，显示它
	//			boolean success = (frame.isVisible() && frame.isShowing());
	//			if (success) {
	//				if (frame.isSelected()) {
	//					frame.moveToFront();
	//					frame.restoreSubcomponentFocus();
	//				}
	//			} else {
	//				frame.setVisible(true);
	//				// 设置为选中
	//				setSelectFrame(frame, true);
	//			}
	//		}
	//
	////		// 如果图标化后，判断有这个窗口
	//////		exists = desktop.hasFrame(frame);
	////		if (frame.isIcon()) {
	////			
	////				desktop.getDesktopManager().deiconifyFrame(frame);
	////			
	////		}
	//		
	//		
	////		// 存在这个窗口...
	////		if (exists) {
	////			// 不是显示状态，显示它
	////			if (!frame.isVisible()) {
	//////				frame.deiconfied();
	////
	////				// frame.getDesktopIcon().setVisible(true);
	////				// frame.setBounds(10, 10, 400, 400);
	////				frame.setVisible(true);
	////				
	//////				// 调用
	//////				InternalFrameEvent event = new InternalFrameEvent(frame, InternalFrameEvent.INTERNAL_FRAME_DEICONIFIED);
	//////				InternalFrameListener[] s = frame.getInternalFrameListeners();
	//////				for (int i = 0; i < s.length; i++) {
	////////					s[i].internalFrameIconified(arg0);
	//////					s[i].internalFrameDeiconified(event);
	//////					// System.out.println(s[i].getClass().getName());
	////////					System.out.printf("remove %d %s!\n", i + 1, s[i].getClass().getName());
	////////					removeInternalFrameListener(s[i]);
	//////				}
	//////				frame.doResize();
	////				
	//////				frame.show();
	////			}
	////			
	//////			frame.getDesktopIcon().setVisible(false);
	//////			
	//////			frame.setVisible(false);
	//////			frame.show();
	////////			frame.restoreSubcomponentFocus();
	//////			frame.setBounds(10, 10, 400, 400);
	//////			frame.setVisible(true);
	////			
	////			// 设置为选中
	////			setSelectFrame(frame, true);
	//////			
	//////			frame.validate();
	////		}
	//	}

	/**
	 * 选择焦点窗口
	 * @param frame
	 */
	private void doCallSelect(LightFrame frame) {
		if (frame == null) {
			return;
		}

		//		// 如果已经隐藏，恢复它
		//		if (frame.isHidden()) {
		//			desktop.getDesktopManager().deiconifyFrame(frame);
		//
		//			//			setSelectFrame(frame, true); // 选中
		//			//			frame.moveToFront();
		//			//			frame.restoreSubcomponentFocus();
		//
		//			return;
		//		}

		boolean exists = false;
		LightFrame[] frames = desktop.getShowFrames();
		for (LightFrame other : frames) {
			if (other == frame) {
				exists = true;
			}
			// else if (other.isSelected()) {
			// // setSelectFrame(other, false); // 取消选中
			// }
		}

		if (exists) {
			if (!frame.isVisible()) {
				// frame.setVisible(true);
				frame.setIcon(false);
			}
			// frame.moveToFront();
			frame.toFront();
			setSelectFrame(frame, true); // 选中
			// frame.restoreSubcomponentFocus();
		}

		//		// 全部窗口
		//		JInternalFrame[] frames = desktop.getAllFrames();
		//		int size = (frames == null ? 0 : frames.length);
		//		boolean exists = false;
		//		for (int i = 0; i < size; i++) {
		//			JInternalFrame other = frames[i];
		//			// 只处理LightFrame窗口
		//			if (!(other instanceof LightFrame)) {
		//				continue;
		//			}
		//			
		//			if (other == frame) {
		//				exists = true;
		//			} else if (other.isSelected()) {
		////				setSelectFrame(other, false); // 取消选中
		//			}
		//		}

		//		// 存在这个窗口...
		//		if (exists) {
		//			// 不是显示状态，显示它
		//			boolean success = (frame.isVisible() && frame.isShowing());
		//			if (success) {
		//				if (frame.isSelected()) {
		//					frame.moveToFront();
		//					frame.restoreSubcomponentFocus();
		//				}
		//			} else {
		//				frame.setVisible(true);
		//				// 设置为选中
		//				setSelectFrame(frame, true);
		//			}
		//		}

		//		// 如果图标化后，判断有这个窗口
		////		exists = desktop.hasFrame(frame);
		//		if (frame.isIcon()) {
		//			
		//				desktop.getDesktopManager().deiconifyFrame(frame);
		//			
		//		}

		//		// 存在这个窗口...
		//		if (exists) {
		//			// 不是显示状态，显示它
		//			if (!frame.isVisible()) {
		////				frame.deiconfied();
		//
		//				// frame.getDesktopIcon().setVisible(true);
		//				// frame.setBounds(10, 10, 400, 400);
		//				frame.setVisible(true);
		//				
		////				// 调用
		////				InternalFrameEvent event = new InternalFrameEvent(frame, InternalFrameEvent.INTERNAL_FRAME_DEICONIFIED);
		////				InternalFrameListener[] s = frame.getInternalFrameListeners();
		////				for (int i = 0; i < s.length; i++) {
		//////					s[i].internalFrameIconified(arg0);
		////					s[i].internalFrameDeiconified(event);
		////					// System.out.println(s[i].getClass().getName());
		//////					System.out.printf("remove %d %s!\n", i + 1, s[i].getClass().getName());
		//////					removeInternalFrameListener(s[i]);
		////				}
		////				frame.doResize();
		//				
		////				frame.show();
		//			}
		//			
		////			frame.getDesktopIcon().setVisible(false);
		////			
		////			frame.setVisible(false);
		////			frame.show();
		//////			frame.restoreSubcomponentFocus();
		////			frame.setBounds(10, 10, 400, 400);
		////			frame.setVisible(true);
		//			
		//			// 设置为选中
		//			setSelectFrame(frame, true);
		////			
		////			frame.validate();
		//		}
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

	//	/**
	//	 * 打开一个控制台面板
	//	 */
	//	public void doOpenConsoleWindow() {
	//		DesktopConsoleFrame frame = new DesktopConsoleFrame();
	//		// 显示窗口
	//		frame.showWindow();
	//
	//		// frame.init();
	//		// frame.setVisible(true);
	//		// 加入子窗口
	//		// addSubFrame(frame);
	//	}

	/**
	 * 生成应用软件包
	 */
	public void doBuildSoftware() {
		DesktopBuildDialog dialog = new DesktopBuildDialog();
		dialog.showDialog(desktop, false);
	}

	/**
	 * 安装应用软件
	 */
	public void doInstallSoftware() {
		DesktopInstallDialog dialog = new DesktopInstallDialog(this);
		dialog.showDialog(desktop, false);
	}

	/**
	 * 删除应用软件
	 */
	public void doUninstallSoftware(){
		DesktopUninstallDialog dialog = new DesktopUninstallDialog(this);
		dialog.showDialog(desktop, false);
	}

	/**
	 * 打开日志窗口
	 */
	public void doOpenLogWindow() {
		// // 如果已经打开，忽略它
		// if (DesktopLogFrame.isLoaded()) {
		// return;
		// }

		// 句柄
		DesktopLogFrame dlg = DesktopLogFrame.getInstance();
		if (dlg == null) {
			DesktopLogFrame frame = new DesktopLogFrame();
			frame.showWindow();
		} else {
			setSelectFrame(dlg, true);
		}
	}

	//	/**
	//	 * 打开帮助文本
	//	 */
	//	public void doHelp() {
	//		// 如果已经打开，忽略！
	//		if (DesktopHelpFrame.isLoaded()) {
	//			JInternalFrame[] frames = desktop.getAllFrames();
	//			for (int i = 0; frames != null && i < frames.length; i++) {
	//				JInternalFrame frame = frames[i];
	//				if (Laxkit.isClassFrom(frame, DesktopHelpFrame.class)) {
	//					// 取消焦点状态窗口
	//					cancelFocusSubFrames();
	//					// 当前窗口是选中
	//					setSelectSubFrame((DesktopHelpFrame) frame, true);
	//				}
	//			}
	//			return;
	//		}
	//
	//		Icon close = UIManager.getIcon("HelpFrame.CloseIcon"); // 关闭图标
	//		Icon open = UIManager.getIcon("HelpFrame.OpenIcon");// 打开图标
	//		Icon command = UIManager.getIcon("HelpFrame.CommandIcon");// 命令图标
	//		Icon search = UIManager.getIcon("HelpFrame.SearchIcon");// 命令图标
	//		Icon go = UIManager.getIcon("HelpFrame.GoIcon");// 命令图标
	//
	//		DesktopHelpFrame frame = new DesktopHelpFrame();
	//		frame.setCommentContext(DesktopLauncher.getInstance().getCommentContext());
	//		frame.setHelpIcon(close, open, command, search, go);
	//		// 显示窗口
	//		frame.showWindow();
	//	}

	//	public void doHelpXX(String command) {
	////		// 如果已经显示在界面上时
	////		if (NotifyDialog.isLoaded()) {
	////			JInternalFrame[] frames = desktop.getAllFrames();
	////			for (int i = 0; frames != null && i < frames.length; i++) {
	////				JInternalFrame frame = frames[i];
	////				if (Laxkit.isClassFrom(frame, NotifyDialog.class)) {
	////					// 取消焦点状态窗口
	////					cancelFocusSubFrames();
	////					// 消息通知窗口选中，显示在前面
	////					setSelectSubFrame(frame, true);
	////				}
	////			}
	////		} else {
	////			// 打开窗口
	////			NotifyDialog.getInstance().showDialog(desktop);
	////		}
	////		
	////		// 显示状态信息
	////		NotifyDialog.getInstance().setStatusText(command);
	//		
	//		NotifyDialog.getInstance(desktop).doShow();
	//		NotifyDialog.getInstance(desktop).setStatusText("千里江山寒色远！");
	//	}

	/**
	 * 打开帮助文本
	 */
	public void doHelp(String command) {
		//		// 如果已经打开，忽略！
		//		if (DesktopHelpFrame.isLoaded()) {
		//			JInternalFrame[] frames = desktop.getAllFrames();
		//			for (int i = 0; frames != null && i < frames.length; i++) {
		//				JInternalFrame frame = frames[i];
		//				if (Laxkit.isClassFrom(frame, DesktopHelpFrame.class)) {
		//					// 取消焦点状态窗口
		//					cancelFocusSubFrames();
		//					// 当前窗口是选中
		//					setSelectFrame(frame, true);
		//					// 显示命令
		//					((DesktopHelpFrame) frame).showCommand(command);
		//				}
		//			}
		//			return;
		//		}

		// 句柄存在
		DesktopHelpFrame dlg = DesktopHelpFrame.getInstance();
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
			return;
		}

		//		Icon closeIcon = UIManager.getIcon("HelpFrame.CloseIcon"); // 关闭图标
		//		Icon openIcon = UIManager.getIcon("HelpFrame.OpenIcon");// 打开图标
		//		Icon commandIcon = UIManager.getIcon("HelpFrame.CommandIcon");// 命令图标
		//		ImageIcon searchIcon = (ImageIcon)UIManager.getIcon("HelpFrame.SearchIcon");// 搜索图标
		//		searchIcon = ImageUtil.brighter(searchIcon, 30); // 调亮图标
		//		Icon goIcon = UIManager.getIcon("HelpFrame.GoIcon");// 命令图标

		DesktopHelpFrame frame = new DesktopHelpFrame();
		frame.setCommentContext(DesktopLauncher.getInstance().getCommentContext());
		// 显示窗口
		frame.showWindow();

		// 如果有命令时...
		if (command != null) {
			frame.showCommand(command);
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

	//	private void doSystemButtons() {
	//		String[] xmlPaths = { "conf/desktop/image/window/system/cluster.png", 
	//				"conf/desktop/image/window/system/trash.png",
	//				"conf/desktop/image/window/system/tub.png",
	//				"conf/desktop/image/frame/console.png", "conf/desktop/image/frame/notepad.png",
	//				"conf/desktop/image/frame/car.png", "conf/desktop/image/frame/flish.png", 
	//				"conf/desktop/image/frame/xship.png", "conf/desktop/image/frame/weiqi.png", "conf/desktop/image/frame/mydb.png",
	//				"conf/desktop/image/frame/mozilla.png","conf/desktop/image/frame/eat.png", 
	//				"conf/desktop/image/frame/natigator.png", "conf/desktop/image/frame/calendar.png",
	//				"conf/desktop/image/frame/organize.png", 
	//				"conf/desktop/image/frame/tables.png", "conf/desktop/image/frame/3d.png", 
	//				"conf/desktop/image/frame/bi.png", "conf/desktop/image/frame/bug.png" };
	//		String[] texts = { "我的集群", "回收站", "我的容器", 
	//				"远程控制台", "笔记本", 
	//				"极速甲壳虫", "宝宝鱼",
	//				"星际迷航", "AI围棋" , "数据仓库", "侏罗纪公园","吃豆", "航海家",
	//				"备忘录","搬箱子","财务报表","高速空气流体","BI View","捉虫子" };
	//
	//
	//		//		DesktopManager dm = DesktopManager.getInstance();
	//		//		dm.setDesktop(desktop, bottom.getHeight());
	//		////		for (int i = 0; i < 4; i++) {
	//		//		for (int i = 0; i < xmlPaths.length; i++) {
	//		//			dm.doDesktopButton(xmlPaths[i], texts[i]);
	//		//		}
	//
	//		//		//		DesktopButton[] cmds = { software, this.tub, this.terminal };
	//		//		Point[] points = fatchPoints(20 * 3);
	//		//		// 加载图标
	//		//		ResourceLoader loader = new ResourceLoader();
	//		//		int n = 0;
	//		//		//		for(int j =0; j < 20; j++){
	//		////		for (int i = 0; i < xmlPaths.length; i++) {
	//		//		for (int i = 0; i < 4; i++) {
	//		//			ImageIcon icon = loader.findImage(xmlPaths[i], 32, 32);
	//		//			DesktopButton cmd = createDesktopButton(texts[i], icon, DesktopUtil.getDesktopButtonSize());
	//		//			cmd.setToolTipText(texts[i]);
	//		//			//			cmds[i].setStateIcon(xmlPaths[i]);
	//		//
	//		//			cmd.setLocation(points[n++]); // 在桌面的位置
	//		//			desktop.add(cmd, Integer.MIN_VALUE + 1);
	//		//		}
	//		//		//		}
	//	}

	//	/**
	//	 * 初始化背景界面
	//	 */
	//	private void initBackground() {
	//		//		desktop.setBackgroundColor(new Color(58, 110, 165));
	//
	//		//		ResourceLoader loader = new ResourceLoader();
	//		//		ImageIcon image = loader.findImage("conf/desktop/image/window/background/desktop.jpg");
	//		//		desktop.setBackgroundImage(image.getImage());
	//	}

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

	//	class LaunchButtonClickThread extends SwingEvent {
	//		public LaunchButtonClickThread(){
	//			super();
	//		}
	//		public void process() {
	//			doLaunchButton();
	//		}
	//	}

	class StartButtonAdapter implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			//			addThread(new LaunchButtonClickThread());

			doLaunchButton();
		}
	}


	//	/** 上一次选中的按纽 **/
	//	private JInternalFrame beforeSelectFrame;

	//	/**
	//	 * 最小全部窗口或者否
	//	 */
	//	private void doHideOrShowInternalFrames() {
	//		// 最小化桌面
	//		// System.out.println("最小化桌面");
	//
	//		JInternalFrame focus = null;
	//
	//		JInternalFrame[] frames = desktop.getAllFrames();
	//		int size = (frames == null ? 0 : frames.length);
	//		for (int i = 0; i < size; i++) {
	//			JInternalFrame frame = frames[i];
	//			// 只处理窗口
	//			if (!(frame instanceof LightFrame)) {
	//				continue;
	//			}
	//
	//			// 隐藏窗口
	//			if (hideInternalFrames) {
	//				// 如果是选中状态，忽略它
	//				if (frame.isSelected()) {
	//					setSelectFrame(frame, false);
	//					beforeSelectFrame = frame;
	//				}
	//				// 隐藏窗口
	//				frame.setVisible(false);
	//			}
	//			// 显示窗口
	//			else {
	//				// 如果不在显示状态，显示它
	//				if (!frame.isVisible()) {					
	//					frame.setVisible(true);
	//				}
	//				// 有匹配的...
	//				if (beforeSelectFrame != null && beforeSelectFrame == frame) {
	//					focus = frame;
	//				}
	//			}
	//		}
	//
	//		// 焦点
	//		if (focus != null) {
	//			setSelectFrame(focus, true);
	//		}
	//		// 交换状态
	//		hideInternalFrames = !hideInternalFrames;
	//	}

	//	/**
	//	 * 最小全部窗口或者否
	//	 */
	//	private void doHideOrShowInternalFrames() {
	//		// 最小化桌面
	//		// System.out.println("最小化桌面");
	//
	//		LightFrame focus = null;
	//
	//		LightFrame[] frames = desktop.getFrames();
	//		int size = (frames == null ? 0 : frames.length);
	//		for (int i = 0; i < size; i++) {
	//			LightFrame frame = frames[i];
	//			//			// 只处理窗口
	//			//			if (!(frame instanceof LightFrame)) {
	//			//				continue;
	//			//			}
	//
	//			// 隐藏窗口
	//			if (hideInternalFrames) {
	//				// 如果是选中状态，忽略它
	//				if (frame.isSelected()) {
	//					setSelectFrame(frame, false);
	//					beforeSelectFrame = frame;
	//				}
	//				// 隐藏窗口
	//				frame.setVisible(false);
	//			}
	//			// 显示窗口
	//			else {
	//				// 如果不在显示状态，显示它
	//				if (!frame.isVisible()) {					
	//					frame.setVisible(true);
	//				}
	//				// 有匹配的...
	//				if (beforeSelectFrame != null && beforeSelectFrame == frame) {
	//					focus = frame;
	//				}
	//			}
	//		}
	//
	//		// 焦点
	//		if (focus != null) {
	//			setSelectFrame(focus, true);
	//		}
	//		// 交换状态
	//		hideInternalFrames = !hideInternalFrames;
	//	}

	//	/** 隐藏全部窗口 **/
	//	private boolean hideInternalFrames = true;
	//
	//	/**
	//	 * 隐藏/显示窗口
	//	 */
	//	private void doHideOrShowInternalFrames() {
	//		// 显示/隐藏窗口
	//		desktop.doFlexFrames(!hideInternalFrames);
	//		// 交换状态
	//		hideInternalFrames = !hideInternalFrames;
	//	}

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

			SkinTokenLoader loader = DesktopLauncher.getInstance().getSkinLoader();
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
			//			System.out.println("UPDATE SYSTEM UI!");

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
	 * @param root 应用软件根集合
	 * @param system 系统应用
	 * @return 初始化成功返回真，否则假
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

		JMenu menu = DesktopLaunchMenuCreator.findMenu(desktopLaunchMenu, associate);

		// 保存参数在内存里
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
		LocalSelector selector = new LocalSelector("conf/desktop/menu/config.xml");
		return selector.findPath("resource");
	}

	/**
	 * 建立启动菜单
	 */
	private void createLaunchMenu() {
		// 建立菜单
		String path = getSurfacePath(); // DesktopLauncher.getInstance().getSurfacePath();
		DesktopLaunchMenuCreator creator = new DesktopLaunchMenuCreator();
		desktopLaunchMenu = creator.create(path);
		desktopLaunchMenu.setInvoker(desktop); // 调用者

		desktopLaunchMenu.setParameter(this, DesktopUtil.getMainMenuBorder(), 6,
				DesktopUtil.getSubMenuBorder());
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
		File dir = DesktopSystem.createRuntimeDirectory();
		File file = new File(dir, "applications.conf");
		WRoot[] roots = RTManager.getInstance().readRoots(file);

//		System.out.printf("全部应用数是：%d\n", (roots != null ? roots.length : -1));

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
		JMenuItem item = DesktopRockMenuCreator.findMenuItemByMethod(desktopRockMenu, "doShowOrHideDockBar");
		// 判断是选择菜单
		if (item != null && Laxkit.isClassFrom(item, JCheckBoxMenuItem.class)) {
			JCheckBoxMenuItem box = (JCheckBoxMenuItem) item;
			// 切换显示或者隐藏
			boolean on = dock.isVisible();
			box.setState(on);
		}
	}
	private void checkStatusBarMenuItem() {
		JMenuItem item = DesktopRockMenuCreator.findMenuItemByMethod(desktopRockMenu, "doShowOrHideStatusBar");
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
		DesktopController.getInstance().doAlignmentByName();
	}

	/**
	 * 按照日期排列
	 */
	public void doDesktopAlignmentByTime() {
		DesktopController.getInstance().doAlignmentByTime();
	}

	/**
	 * 锁定桌面图标
	 */
	public void doLockDesktop() {
		JMenuItem item = DesktopRockMenuCreator.findMenuItemByMethod(desktopRockMenu, "doLockDesktop");
		// 判断是选择菜单
		if (item != null && Laxkit.isClassFrom(item, JCheckBoxMenuItem.class)) {
			JCheckBoxMenuItem box = (JCheckBoxMenuItem) item;
			boolean locked = DesktopController.getInstance().isLocked();
			// 切换状态
			boolean on = !locked;
			box.setState(on);
			// 修改图标
			if (on) {
				Icon icon = UIManager.getIcon("DesktopWindow.PopupMenu.LockedIcon");
				box.setIcon(icon);
			} else {
				Icon icon = UIManager.getIcon("DesktopWindow.PopupMenu.UnlockedIcon");
				box.setIcon(icon);
			}
			// 切换状态
			DesktopController.getInstance().doLock();
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
		DesktopController.getInstance().doRefresh();
	}

	//	class SwitchStatusBarThread extends SwingEvent {
	//		private boolean on;
	//
	//		public SwitchStatusBarThread(boolean b) {
	//			super();
	//			on = b;
	//		}
	//
	//		public void process() {
	//			bottom.setVisible(on);
	//		}
	//	}

	class MoveDockToBottom extends SwingEvent {
		MoveDockToBottom() {
			super();
		}

		public void process() {
			dock.doMoveToBottom();
		}
	}

	public void doShowOrHideStatusBar() {
		JMenuItem item = DesktopRockMenuCreator.findMenuItemByMethod(desktopRockMenu, "doShowOrHideStatusBar");
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
		JMenuItem item = DesktopRockMenuCreator.findMenuItemByMethod(desktopRockMenu, "doShowOrHideDockBar");
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
		DesktopPropertiesDialog dlg = DesktopPropertiesDialog.getInstance();
		if (dlg == null) {
			DesktopPropertiesDialog dialog = new DesktopPropertiesDialog(desktop, this);
			// 进入非模态
			dialog.showDialog(desktop, false);
		} else {
			// 显示在前面
			setSelectFrame(dlg, true);
		}
	}

	//	/**
	//	 * 弹出菜单线程
	//	 *
	//	 * @author scott.liang
	//	 * @version 1.0 6/12/2021
	//	 * @since laxcus 1.0
	//	 */
	//	class PopupMenuThread extends SwingEvent {
	//		/** 事件 **/
	//		private ActionEvent event;
	//
	//		public PopupMenuThread(ActionEvent e) {
	//			super();
	//			event = e;
	//		}
	//
	//		public void process() {
	//			click(event);
	//		}
	//	}

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
		DesktopRockMenuCreator creator = new DesktopRockMenuCreator();
		desktopRockMenu = creator.create(path, "DesktopPopupMenu");
		desktopRockMenu.setInvoker(desktop);

		desktopRockMenu.setParameter(DesktopUtil.getSubMenuBorder(), 6);
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
				DesktopUtil.shotScreen(frames[i]);
				return;
			}
		}
		// 2. 第二种
		Container panel = getContentPane();
		if (panel != null && Laxkit.isClassFrom(panel, CenterPanel.class)) {
			DesktopUtil.shotScreen((CenterPanel) panel);
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
					DesktopUtil.shotScreen(sub);
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
			DesktopNotifyDialog dlg = DesktopNotifyDialog.getInstance();
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

			//			if (dock.isVisible()) {
			//				dock.setVisible(false);
			//				setSelectFrame(dock, false);
			//				dock.toBack();
			//			} else {
			//				dock.setVisible(true);
			//				setSelectFrame(dock, true);
			//				dock.toFront();
			//			}
		}
	}

	//	class UpSystemDockWidth extends SwingEvent {
	//		UpSystemDockWidth() {
	//			super();
	//		}
	//
	//		public void process() {
	//			if (dock.isVisible()) {
	//				dock.modifyWidth(true);
	//			}
	//		}
	//	}

	//	class DownSystemDockWidth extends SwingEvent {
	//		DownSystemDockWidth() {
	//			super();
	//		}
	//
	//		public void process() {
	//			if (dock.isVisible()) {
	//				dock.modifyWidth(false);
	//			}
	//		}
	//	}

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
	 * 判断是COPY屏幕上的窗口
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
		//		return e.isShiftDown()
		//				&& (e.getKeyCode() == KeyEvent.VK_KP_UP || e.getKeyCode() == KeyEvent.VK_UP);

		return e.isControlDown() && e.isShiftDown()
		&& (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS);
	}

	/**
	 * 缩小DOCKER宽度，SHIFT + DOWN
	 * @param e
	 * @return
	 */
	private boolean isShrinkSytemDockWidth(KeyEvent e) {
		//		return e.isShiftDown()
		//				&& (e.getKeyCode() == KeyEvent.VK_KP_DOWN || e.getKeyCode() == KeyEvent.VK_DOWN);

		return e.isControlDown() && e.isShiftDown()
		&& (e.getKeyCode() == KeyEvent.VK_UNDERSCORE || e.getKeyCode() == KeyEvent.VK_MINUS);
	}

	//	/**
	//	 * 显示DOCKER栏
	//	 * @param e
	//	 * @return
	//	 */
	//	private boolean isShowSystemDock(KeyEvent e) {
	//		return e.isControlDown() && e.isShiftDown() && (e.getKeyCode() == KeyEvent.VK_F);
	//	}
	//
	//	/**
	//	 * 隐藏DOCKER栏
	//	 * @param e
	//	 * @return
	//	 */
	//	private boolean isHideSystemDock(KeyEvent e) {
	//		return e.isControlDown() && e.isShiftDown() && (e.getKeyCode() == KeyEvent.VK_H);
	//	}

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
			switchWindow = new DesktopCycleWindow(this);
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

			//			// 显示/隐藏DOCKER
			//			else if (isShowSystemDock(e)) {
			//				if (!dock.isVisible()) {
			//					dock.setVisible(true);
			//					setSelectFrame(dock, true);
			//					dock.toFront();
			//				}
			//			} else if (isHideSystemDock(e)) {
			//				if (dock.isVisible()) {
			//					dock.setVisible(false);
			//					setSelectFrame(dock, false);
			//					dock.toBack();
			//				}
			//			}
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
		PlatformListener[] systemListeners = new PlatformListener[] {
				new DesktopCommandDispatcher(), new DesktopCommandParser(), new DesktopAdapter(),
				new DesktopSoundAdapter(), new HelpAdapter(),
				new DesktopGradeAdapter(), new DesktopDatabaseAdapter(),
				new FrontListenerImplementor(), new ComponentListenerImplementor() };

		// 状态条
		status = new DesktopStatusBar();
		// 桌面
		desktop = new FrontDesktop(systemListeners, status);
		// 弹出菜单
		desktop.addMouseListener(new PopupMenuAdapter());

		// 桌面窗口监听器
		status.setSelectFrameListener(this);

		// 应用坞
		dock = new DesktopDock();

		// 找到标题和图标
		String title = UIManager.getString("DesktopWindow.Title");
		ImageIcon icon = (ImageIcon) UIManager.getIcon("DesktopWindow.TitleIcon");

		// 标题和图标
		setTitle(title);
		setIconImage(icon.getImage());

		// 状态
		DesktopWindowAdapter adapter = new DesktopWindowAdapter();
		addWindowListener(adapter);
		addWindowStateListener(adapter);
		addWindowFocusListener(adapter);

		// 忽略关闭窗口操作，DesktopWindowAdapter.windowClosing方法做成空方法
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
		CenterPanel panel = new CenterPanel();
		panel.init(desktop, status);
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(panel);

		// 设置桌面和读取桌面图标参数
		DesktopController.getInstance().setDesktop(desktop);

		// 生成句柄，保存到静态显示环境
		DesktopNotifyDialog dialog = DesktopNotifyDialog.getInstance(desktop);
		DesktopInvoker.setDefaultDisplay(dialog);

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
	 * 参数见DesktopPropertiesDialog.BackgroundPane
	 */
	private void setDesktopBackground() {
		Color color = RTKit.readColor(RTEnvironment.ENVIRONMENT_SYSTEM, "DesktopWindow/Background/Color");
		String filename = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "DesktopWindow/Background/File");
		int layout = RTKit.readInteger(RTEnvironment.ENVIRONMENT_SYSTEM, "DesktopWindow/Background/Layout");

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

	class ShowDesktopWindow extends SwingEvent {
		public ShowDesktopWindow() {
			super(true);
		}

		public void process() {
			// 设置背景
			setDesktopBackground();
			// 显示窗口
			setVisible(true);
			// 延时...
			sleep(500);
		}
	}

	class ShowDesktopButtons extends SwingEvent {
		public ShowDesktopButtons(){
			super(true);
		}
		@Override
		public void process() {
			// 显示图标
			DesktopController.getInstance().showAllButtons(status.getHeight());
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

	class EdgeAdapter implements DesktopEdgeListener {

		/* (non-Javadoc)
		 * @see com.laxcus.front.desktop.dock.EdgeListener#doRunFromEdge()
		 */
		@Override
		public void doRunFromEdge() {
			doRun();
		}

		/* (non-Javadoc)
		 * @see com.laxcus.front.desktop.dock.EdgeListener#doShutdownFromEdge()
		 */
		@Override
		public void doShutdownFromEdge() {
			exit();
		}

	}

	class ShowDesktopDock extends SwingEvent {
		public ShowDesktopDock() {
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
			DesktopNotifyDialog dlg = DesktopNotifyDialog.getInstance();
			// 显示弹出窗口
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
			DesktopController.getInstance().setUsabled(true);
		}
	}

	//	/**
	//	 * 显示登录窗口和登录
	//	 * @return 登录成功返回真，否则假
	//	 */
	//	private boolean showLoginWindow2() {
	//		boolean success = false;
	//
	//		// 以线程模式处理注册登录到GATE
	//		LaunchThread thread = new LaunchThread(this);
	//		// 启动线程
	//		boolean launch = thread.start();
	//
	//		if (launch) {
	//			// 进入等待
	//			thread.await();
	//			// 判断登录成功
	//			success = thread.isSuccessful();
	//		}
	//
	//		if (success) {
	//			// 状态栏图标为连接状态
	//			bottom.setConnectIcon();
	//		} else {
	//			dispose();
	//		}
	//
	//		return success;
	//	}

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
			addThread(new ShowDesktopWindow()); // 显示桌面窗口
			addThread(new ShowDesktopDock()); // 显示应用坞
			addThread(new ShowDesktopButtons()); // 显示桌面上的按纽

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
			
			// 几个参数设置为可用
			addThread(new UsabledThread());
		}
		return success;
	}

}