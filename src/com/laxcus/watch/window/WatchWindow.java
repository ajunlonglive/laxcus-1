/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.window;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;

import com.laxcus.command.cloud.*;
import com.laxcus.command.site.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.help.*;
import com.laxcus.util.help.dialog.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.login.*;
import com.laxcus.util.skin.*;
import com.laxcus.watch.*;
import com.laxcus.watch.component.*;
import com.laxcus.watch.dialog.*;
import com.laxcus.watch.pool.*;

/**
 * WATCH站点图形界面。
 * 
 * WATCH图形界面的刷新操作，防止类似死锁现象，要求减少线程的产生和调用频率，一个线程尽可能批量处理。
 * 
 * @author scott.liang
 * @version 1.2 7/28/2013
 * @since laxcus 1.0
 */
public class WatchWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = 5620367272657557372L;

	/** 菜单条 **/
	private JMenuBar menubar;

	/** 工具条 **/
	private JToolBar toolbar;

	/** 左侧浏览面板 **/
	private WatchLeftPanel left = new WatchLeftPanel();

	/** 右侧主操作面板 **/
	private WatchRightPanel right = new WatchRightPanel();

	/** 状态栏面板 **/
	private WatchStatusPanel bottom = new WatchStatusPanel();

	/** 自动注册标记 **/
	private LoginToken regToken;

	/**
	 * 构造默认的WATCH窗口
	 */
	public WatchWindow() {
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
			super(true); // 同步处理
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
		Font font = WatchProperties.readSystemFont();
		if (font != null) {
			UITools.updateSystemFonts(font);
		}
	}

	/**
	 * 加入线程
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 销毁图形资源
	 */
	public void destroy() {
		dispose();
	}

	/**
	 * 从指定的JAR配置文档中增加命令标记
	 * @param name JAR档案文件
	 */
	public void addCommandTokens(String name) {
		right.addCommandTokens(name);
	}

	/**
	 * 返回主架构面板
	 * @return
	 */
	public WatchRightPanel getSkeletonPanel() {
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
	public int getMaxLogs1() {
		return getLogPanel().getMaxItems();
	}

	/**
	 * 判断是拒绝显示日志
	 * @return 真或者假
	 */
	public boolean isLogForbid() {
		return getLogPanel().isForbid();
	}

	//	class WatchRollingThread extends SwingEvent {
	//		boolean rolling;
	//
	//		WatchRollingThread(boolean b) {
	//			super();
	//			rolling = b;
	//		}
	//		public void process() {
	//			bottom.doInvokerRolling(rolling);
	//		}
	//	}

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
	//		WatchRollingThread light = new WatchRollingThread(true);
	//		light.setIntervalTime(interval);
	//		light.setTouchTime(touchTime);
	//		array.add(light);
	//
	//		// 下一次触发时间
	//		touchTime += interval;
	//
	//		// 多延时200毫秒，效果更好！
	//		WatchRollingThread dark = new WatchRollingThread(false);
	//		dark.setIntervalTime(interval + 200);
	//		dark.setTouchTime(touchTime + 200);
	//		array.add(dark);
	//
	//		// 一批线程放入队列
	//		SwingDispatcher.invokeThreads(array);
	//	}

	/**
	 * 增加发送和接收的流量
	 * @param receiveFlows 接收流量
	 * @param sendFlows 发送流量
	 */
	public void addFlows(long receiveFlows, long sendFlows) {
		bottom.addFlows(receiveFlows, sendFlows);
	}

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
		// 调用，闪烁
		bottom.rolling();
	}

	/**
	 * 日志面板
	 * @return
	 */
	public WatchMixedLogPanel getLogPanel() {
		return right.getLogPanel();
	}

	class WatchWindowAdapter extends WindowAdapter {

		WatchWindowAdapter() {
			super();
		}

		public void windowClosing(WindowEvent e) {
//			System.out.printf("from source [%s]\n", e.getSource().getClass().getName());
			addThread(new ExitThread());
		}

//		public void windowOpened(WindowEvent e) {
//			Logger.debug(this, "windowOpened","Hi...");
//		}
//
//		public void windowClosed(WindowEvent e) {
//			Logger.debug(this, "windowClosed","Hi...");
//		}
//
//		public void windowIconified(WindowEvent e) {
//			Logger.debug(this, "windowIconified","Hi...");
//		}
//
//		public void windowDeiconified(WindowEvent e) {
//			Logger.debug(this, "windowDeiconified","Hi...");
//		}
//
//		public void windowActivated(WindowEvent e) {
//			Logger.debug(this, "windowActivated","Hi...");
//		}
//
//		public void windowDeactivated(WindowEvent e) {
//			Logger.debug(this, "windowDeactivated","Hi...");
//		}
//
//		public void windowStateChanged(WindowEvent e) {
//			Logger.debug(this, "windowStateChanged","Hi...");
//		}
//
//		public void windowGainedFocus(WindowEvent e) {
//			Logger.debug(this, "windowGainedFocus","Hi...");
//		}
//
//		public void windowLostFocus(WindowEvent e) {
//			Logger.debug(this, "windowLostFocus","Hi...");
//		}
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
	 * 接受线程操作
	 * @param event 激活事件
	 */
	private void click(ActionEvent event) {
		Object source = event.getSource();
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
	 * 返回一组菜单项
	 * @param methods
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
	 * 判断显示菜单栏
	 * @return 返回真或者假
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
		// 菜单项无效，忽略！
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

	//	/**
	//	 * 显示菜单项
	//	 * @param menu
	//	 */
	//	private void hideMenu(JMenu menu) {
	//		int count = menu.getItemCount();
	//		for (int i = 0; i < count; i++) {
	//			JMenuItem item = menu.getItem(i);
	//			if (item == null) {
	//				continue;
	//			}
	//
	//			if (item.getClass() == JMenu.class) {
	//				JMenu sub = (JMenu) item;
	//				hideMenu(sub);
	//			} else {
	//				if (item.isVisible()) {
	//					item.setVisible(false);
	//				}
	//			}
	//		}
	//
	//		if (menu.isVisible()) {
	//			menu.setVisible(false);
	//		}
	//	}

	//	/**
	//	 * 显示菜单栏
	//	 */
	//	private void hideMenubar() {
	//		int count = menubar.getMenuCount();
	//		for (int i = 0; i < count; i++) {
	//			JMenu menu = menubar.getMenu(i);
	//			if (menu == null) {
	//				continue;
	//			}
	//			// 显示菜单
	//			hideMenu(menu);
	//			//			if (menu.isVisible()) {
	//			//				menu.setVisible(false);
	//			//			}
	//		}
	//	}

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
	//	 * 显示或者隐藏菜单栏
	//	 */
	//	public void doShowOrHideMenubar() {
	//		if (!menubar.isVisible()) {
	//			return;
	//		}
	//
	//		// 隐藏菜单栏
	//		JMenuItem item = findMenuItem("doShowOrHideMenubar");
	//
	//		//			if (item != null) {
	//		//				menubarHeight = menubar.getHeight();
	//		//				hideMenubar = true;
	//		//				
	//		//				// 隐藏菜单
	//		//				menubar.setVisible(false);
	//		//				((JCheckBoxMenuItem) item).setState(false);
	//		//			}
	//
	//		// 菜单项有效！
	//		if (item != null) {
	//			// 菜单正常显示，把它隐藏起来
	//			if (showMenubar) {
	//				menubarHeight = menubar.getHeight();
	//				showMenubar = false;
	//				// 隐藏菜单
	//				menubar.setVisible(false);
	//				((JCheckBoxMenuItem) item).setState(false);
	//			}
	//			// 已经手动隐藏菜单，恢复显示它
	//			else {
	//				// 显示菜单
	//				menubarHeight = 0;
	//				showMenubar = true;
	//				// 正确显示菜单
	//				((JCheckBoxMenuItem) item).setState(true);
	//			}
	//		}
	//
	//
	//		//		else {
	//		//			// 显示菜单
	//		//			menubarHeight = 0;
	//		//			showMenubar = true;
	//		//			
	//		//			// 显示菜单
	//		//			menubar.setVisible(true);
	//		//			JMenuItem item = findMenuItem("doShowOrHideMenubar");
	//		//			if (item != null) {
	//		//				((JCheckBoxMenuItem) item).setState(true);
	//		//			}
	//		//		}
	//
	//		// JMenuItem item = findMenuItem("doShowOrHideMenubar");
	//		// if (item != null) {
	//		// boolean on = menubar.isVisible();
	//		// menubar.setVisible(!on);
	//		// ((JCheckBoxMenuItem) item).setState(!on);
	//		// } else {
	//		// menubar.setVisible(true);
	//		// }
	//	}

	//	private void trackMouse(MouseEvent e) {
	//		//		// 非全屏幕状态
	//		//		if (!fullScreen)
	//		//			return;
	//
	////		// 菜单没有隐藏，忽略它
	////		if (!hideMenubar) {
	////			return;
	////		}
	//
	////		int y = e.getY();
	////		int screenY = e.getYOnScreen();
	////		System.out.printf("Y:%d - Screen Y:%d, %d, Full Screen:%s \n", y,
	////				screenY, menubarHeight, (fullScreen ? "Yes" : "No"));
	//		
	//		// 如果是正常显示，忽略它
	//		if (showMenubar) {
	//			return;
	//		}
	//		
	//		// 取它的Y坐标
	//		int y = e.getY();
	//		// 在范围内...
	//		if (y < menubarHeight) {
	//			boolean on = menubar.isVisible();
	//			if (on) {
	//
	//			} else {
	//				// 显示菜单栏
	//				menubar.setVisible(true);
	//			}
	//		}
	//		// 不在范围内...
	//		else {
	//			boolean on = menubar.isVisible();
	//			if (on) {
	//				menubar.setVisible(false);
	//			}
	//		}
	//	}

	//	/**
	//	 * 检测追踪鼠标，通过鼠标位置判断菜单条的显示
	//	 */
	//	private void addMouseMotion() {
	//		addMouseMotionListener(new MouseMotionAdapter() {
	//			public void mouseMoved(MouseEvent e) {
	////				trackMouse(e);
	//				
	////				int y = e.getY();
	////				int screenY = e.getYOnScreen();
	////				System.out.printf("Y:%d - Screen Y:%d, Full Screen:%s \n", y, screenY, (fullScreen ? "Yes" : "No"));
	//				
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


	//	private void addMouse() {
	//		addMouseMotionListener(new MouseMotionAdapter() {
	//			public void mouseMoved(MouseEvent e) {
	//				int y = e.getY();
	//				int screenY = e.getYOnScreen();
	//				System.out.printf("Y:%d - Screen Y:%d, Full Screen:%s \n", y, screenY, (fullScreen ? "Yes" : "No"));
	//			}
	//		});
	//	}

	//	private void addMouse(JFrame panel) {
	//		panel.addMouseMotionListener(new MouseMotionAdapter() {
	//			public void mouseMoved(MouseEvent e) {
	//				int y = e.getY();
	//				int screenY = e.getYOnScreen();
	//				System.out.printf("Y:%d - Screen Y:%d, Full Screen:%s \n", y, screenY, (fullScreen ? "Yes" : "No"));
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
	 * 检查命令语法
	 */
	public void doCheckCommand() {
		right.check();
	}

	/**
	 * 启动命令
	 */
	public void doLaunchCommand() {
		right.execute();
	}

	/**
	 * 更新节点和用户记录
	 */
	public void doRefresh() {
		//	printUIElements2();
		//	reloadUI();

		boolean online = WatchLauncher.getInstance().isLogined();
		Node hub = null;
		if (online) {
			hub = WatchLauncher.getInstance().getHub();
		}
		// 清除节点内存记录
		SiteOnWatchPool.getInstance().clear();
		if (hub != null) {
			//			SiteOnWatchPool.getInstance().add(hub);
		}

		// 清除其它
		SiteRuntimeBasket.getInstance().clear();
		RegisterMemberBasket.getInstance().clear();
		FrontMemberBasket.getInstance().clear();

		// 清除界面
		//		siteBrowser.clear();
		//		memberBrowser.clear();
		left.clear();
		right.clear();
		bottom.clear();

		// 设置为连接状态
		if (online && hub != null) {
			bottom.setConnectIcon();
			bottom.setHub(hub);
		}

		// 快速提交命令由TOP/HOME/BANK站点（跳过检查本地资源过程）
		WatchCommandPool.getInstance().press(new AskSite());
		WatchCommandPool.getInstance().press(new AskClusterMember());
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
		if (item != null) {
			item.setSelected(true);
		}
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
	 * 修改窗口字体
	 * @param font
	 * @return 返回新的字体
	 */
	private Font doFont(Font font) {
		// 构造窗口字体
		FontSelectDialog dialog = new FontSelectDialog(this, true, WatchLauncher.getInstance(), font);
		// 显示窗口
		dialog.setVisible(true);
		// 取选择的字体
		Font select = dialog.getSelectFont();
		// 销毁窗口
		dialog.dispose();

		// 返回新字体
		return select;
	}

	/**
	 * 更新主菜单字体
	 * @param font 字体实例
	 */
	private void updateMainMenuFont(Font font) {
		menubar.setFont(font);

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
	 * 修改WATCH节点环境字体
	 */
	public void doSystemMenuFont() {
		Font font = WatchProperties.readSystemFont();
		Font select = doFont(font);
		if (select != null) {
			// 字体在12 - 18磅之间
			if (select.getSize() < 12) {
				select = new Font(select.getFamily(), select.getStyle(), 12);
			} else if (select.getSize() > 18) {
				select = new Font(select.getFamily(), select.getStyle(), 18);
			}
			WatchProperties.writeSystemFont(select);
		}
	}

	/**
	 * 修改主菜单字体
	 */
	public void doMainMenuFont() {
		Font font = menubar.getFont();
		Font select = doFont(font);
		if (select != null) {
			WatchProperties.writeMainMenuFont(select);
			updateMainMenuFont(select); // 更新字体
		}
	}

	/**
	 * 修改站点浏览窗口字体
	 */
	public void doSiteWindowFont() {
		WatchSiteBrowserPanel siteBrowser = left.getSiteBrowserPanel();
		Font font = siteBrowser.getListPanel().getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			WatchProperties.writeBrowserSiteFont(select);
			// 修改两个窗口的字体
			siteBrowser.getListPanel().setSelectFont(select);
			siteBrowser.getDetailPanel().setSelectFont(select);
		}
	}

	/**
	 * 修改成员浏览窗口字体
	 */
	public void doMemberWindowFont() {
		WatchMemberBrowserPanel memberBrowser = left.getMemberBrowserPanel();
		Font font = memberBrowser.getListPanel().getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			WatchProperties.writeBrowserMemberFont(select);
			// 修改两个窗口的字体
			memberBrowser.getListPanel().setSelectFont(select);
			memberBrowser.getDetailPanel().setSelectFont(select);
		}
	}

	/**
	 * 修改TABLE选项窗口字体
	 */
	public void doTabbedWindowFont() {
		Font font = right.getMixPanel().getTabbedSelectFont();
		Font select = doFont(font);
		if (select != null) {
			// 保存！
			WatchProperties.writeTabbedFont(select);
			// 重新设置
			left.setTabbedSelectFont(select);
			right.getMixPanel().setTabbedSelectFont(select);
		}
	}

	/**
	 * 修改命令窗口字体
	 */
	public void doCommandWindowFont() {
		WatchCommandPane panel = right.getCommandPanel();

		Font font = panel.getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			WatchProperties.writeCommandPaneFont(select);

			panel.setSelectFont(select);
		}
	}

	/**
	 * 修改提示窗口字体
	 */
	public void doNoticeWindowFont() {
		WatchMixedMessagePanel panel = right.getMessagePanel();
		Font font = panel.getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			WatchProperties.writeTabbedMessageFont(select);
			panel.setSelectFont(select);
		}
	}

	/**
	 * 修改表格窗口字体
	 */
	public void doTableWindowFont() {
		WatchMixedTablePanel panel = right.getTablePanel();
		Font font = panel.getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			WatchProperties.writeTabbedTableFont(select);
			panel.setSelectFont(select);
		}
	}

	/**
	 * 修改节点状态窗口字体
	 */
	public void doRuntimeWindowFont() {
		WatchMixedRuntimePanel panel = right.getRuntimePanel();
		Font font = panel.getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			WatchProperties.writeTabbedRuntimeFont(select);
			panel.setSelectFont(select);
		}
	}

	/**
	 * 修改日志窗口字体
	 */
	public void doLogWindowFont() {
		WatchMixedLogPanel panel = right.getLogPanel();
		Font font = panel.getSelectFont();
		Font select = doFont(font);
		if (select != null) {
			WatchProperties.writeTabbedLogFont(select);
			panel.setSelectFont(select);
		}
	}

	/**
	 * 修改帮助字体
	 */
	public void doHelpFont() {
		CommentContext context = WatchLauncher.getInstance().getCommentContext();
		Font font = new Font(context.getTemplate().getFontName(), Font.PLAIN, 14);
		Font select = doFont(font);
		if (select != null) {
			WatchProperties.writeHelpMenuFontFamily(select.getName());
			context.getTemplate().setFontName(select.getName());
		}
	}

	/**
	 * 清除通知文本
	 */
	public void doClearNote() {
		right.getMessagePanel().clear();
	}

	/**
	 * 清除表格数据
	 */
	public void doClearTable() {
		right.getTablePanel().clear();
	}

	/**
	 * 清除日志文本
	 */
	public void doClearLog() {
		right.getLogPanel().clear();
	}

	/**
	 * 选择组件文件
	 * @param description
	 * @param extensions
	 * @return
	 */
	private File choiceSingleFile(String title, String buttonText, String description, String... extensions) {
		JFileChooser chooser = new JFileChooser();
		// 不是空值，设置选择参数
		if (description != null && extensions != null) {
			FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensions);
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


	public void doDeployConductPackage() {
		WatchDeployCloudPackageDialog dialog = new WatchDeployCloudPackageDialog(
				this, true, BuildCloudPackageTag.CONDUCT);
		// 打开对话框
		boolean accepted = dialog.showDialog();
		if (accepted) {
			String writer = dialog.getReader();
			// 生成命令来执行
			String syntax = String.format("DEPLOY CONDUCT PACKAGE %s", writer);
			right.doDeployConductPackage(syntax);
		}
	}

	public void doDeployEstablishPackage() {
		WatchDeployCloudPackageDialog dialog = new WatchDeployCloudPackageDialog(
				this, true, BuildCloudPackageTag.ESTABLISH);
		// 打开对话框
		boolean accepted = dialog.showDialog();
		if (accepted) {
			String writer = dialog.getReader();
			// 生成命令来执行
			String syntax = String.format("DEPLOY ESTABLISH PACKAGE %s", writer);
			right.doDeployEstablishPackage(syntax);
		}		
	}

	public void doDeployContactPackage() {
		WatchDeployCloudPackageDialog dialog = new WatchDeployCloudPackageDialog(
				this, true, BuildCloudPackageTag.CONTACT);
		// 打开对话框
		boolean accepted = dialog.showDialog();
		if (accepted) {
			String writer = dialog.getReader();
			// 生成命令来执行
			String syntax = String.format("DEPLOY CONTACT PACKAGE %s", writer);
			right.doDeployContactPackage(syntax);
		}
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

	/** 全屏标记 **/
	private boolean fullScreen = false;

	/** 窗口在屏幕的局部范围 **/
	private Rectangle brush = null;

	/**
	 * 调整屏幕范围，全屏或者否
	 * @param full 全屏
	 */
	private void adjustFullScreen(boolean full) {
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
		adjustFullScreen(true);
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
		adjustFullScreen(false);
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
			brush = getBounds(); // 取当前系统的屏幕尺寸
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
	 * 生成集群管理员账号文件
	 */
	public void doDBAKey() {
		WatchDBADialog dialog = new WatchDBADialog(this, false);
		dialog.showDialog();
	}

	/**
	 * 生成FIXP安全通信文件
	 */
	public void doRSAKey() {
		WatchRSADialog dialog = new WatchRSADialog(this, false);
		dialog.showDialog();
	}

	/**
	 * 生成基于LINUX平台的LAXCUS批量启动/停止脚本
	 */
	public void doCreateRunStopScript() {
		WatchCreateRunStopScriptDialog dialog = new WatchCreateRunStopScriptDialog(this, false);
		dialog.showDialog();
	}

	/**
	 * 生成基于LINUX平台的LAXCUS地址脚本文件
	 */
	public void doCreateAddressScript() {
		WatchCreateAddressScriptDialog dialog = new WatchCreateAddressScriptDialog(this, false);
		dialog.showDialog();
	}

	/**
	 * 测试正则表达式
	 */
	public void doRegex() {
		WatchRegexDialog dialog = new WatchRegexDialog(this, false);
		dialog.showDialog(); // 显示窗口
	}

	/**
	 * 打开帮助文本
	 */
	public void doHelp() {
		// 如果已经打开，忽略！
		if (CommonHelpDialog.isLoaded()) {
			return;
		}

		String title = WatchLauncher.getInstance().findCaption("Dialog/Help/title");

		ResourceLoader loader = new ResourceLoader("conf/watch/image/help/");
		int iconWidth = 16, iconHeight = 16;
		Icon close = loader.findImage("close.png", iconWidth, iconHeight); // 关闭图标
		Icon open = loader.findImage("open.png", iconWidth, iconHeight);// 打开图标
		Icon command = loader.findImage("command.png", iconWidth, iconHeight);// 命令图标
		Icon search = loader.findImage("search.png", iconWidth, iconHeight);// 命令图标
		Icon go = loader.findImage("go.png", iconWidth, iconHeight);// 命令图标

		CommonHelpDialog dialog = new CommonHelpDialog();
		dialog.setCommentContext(WatchLauncher.getInstance().getCommentContext());
		dialog.setHelpIcon(close, open, command, search, go);
		dialog.showDialog(title, getIconImage());
	}

	/**
	 * 打开ABOUT对话框
	 */
	public void doAbout() {
		WatchAboutDialog dialog = new WatchAboutDialog(this, true);
		dialog.showDialog();
	}

	/**
	 * 命令处理模式
	 */
	public void doCommandMode() {
		WatchCommandModeDialog dialog = new WatchCommandModeDialog(this, true);
		dialog.showDialog();
	}

	/**
	 * 命令超时间隔时间
	 */
	public void doCommandTimeout() {
		WatchCommandTimeoutDialog dialog = new WatchCommandTimeoutDialog(this, true);
		dialog.showDialog();
	}

	/**
	 * 节点资源检索间隔时间
	 */
	public void doOutlookInterval() {
		WatchOutlookIntervalDialog dialog = new WatchOutlookIntervalDialog(this, true);
		dialog.showDialog();
	}


	/**
	 * 解析标签
	 * @param xmlPath
	 * @return
	 */
	private String getCaption(String xmlPath) {
		return WatchLauncher.getInstance().findCaption(xmlPath);
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

		@Override
		public void process() {
			exit();
		}
	}

	/**
	 * 退出运行
	 * 注意："exit"这个方法名对应resource.xml的按纽方法，不能修改！
	 */
	private void exit() {
		String title = getCaption("MessageBox/Exit/Title/title");
		String content = getCaption("MessageBox/Exit/Message/title");

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/watch/image/message/exit.png", 32, 32);

		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, icon, content, JOptionPane.YES_NO_OPTION);
		if (who != JOptionPane.YES_OPTION) {
			return;
		}

		// 停止线程
		bottom.stopDaemonThread();
		// 更新界面配置导入到内存
		updateConfigure();
		
		// 关闭窗口
		setVisible(false);
		// 通知进程退出
		WatchLauncher.getInstance().stop();
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
		WatchProperties.writeWindowBound(rect);

		// 中央窗口分割线位置，在像素为单位！
		int pixel = right.getDividerLocation();
		WatchProperties.writeCenterPaneDeviderLocation(pixel);

		// 当前浏览窗口分割线位置
		pixel = getDividerLocation();
		WatchProperties.writeBrowserPaneDeviderLocation(pixel);

		// 站点浏览面板
		pixel = left.getSiteBrowserDividerLocation();
		WatchProperties.writeSiteBrowserPaneDeviderLocation(pixel);

		// 成员浏览面板
		pixel = left.getMemberBrowserDividerLocation();
		WatchProperties.writeMemberBrowserPaneDeviderLocation(pixel);
	}

	/**
	 * 返回边框范围
	 * @return
	 */
	private Rectangle getDefaultBounds() {
		// 从配置中读取范围
		Rectangle rect = WatchProperties.readWindowBound();
		if (rect != null) {
			return rect;
		}

		// 默认是全屏
		Dimension d = getToolkit().getScreenSize();
		return new Rectangle(0, 0, d.width, d.height);
	}

	/**
	 * 设置状态栏文本提示
	 * @param text
	 */
	public void setStatusText(String text) {
		bottom.setTip(text);
	}

	/**
	 * 设置登录菜单有效
	 * @param enabled 有效
	 */
	private void setLoginMenuEnabled(boolean enabled) {
		// 重置菜单栏
		JMenuItem item = findMenuItem("login");
		item.setEnabled(enabled);
		item = findMenuItem("logout");
		item.setEnabled(!enabled);
		item = findMenuItem("relogin");
		item.setEnabled(!enabled);
	}

	/**
	 * 登录目标站点
	 * @return 成功返回真，否则假
	 */
	protected boolean __login() {
		Logger.debug(this, "__login", "into ...");

		// 显示前端的注册窗口
		WatchLoginDialog dialog = new WatchLoginDialog(this);
		dialog.setModal(true);
		dialog.showDialog(regToken);
		dialog.dispose();
		// 注册窗口取消向管理节点注册操作
		if (dialog.isCanceled()) {
			return false;
		}

		Node hub = WatchLauncher.getInstance().getHub();
		setHub(hub);

		// 登录成功后，登录菜单失效，注销和重新登录有效
		setLoginMenuEnabled(false);

		// 保存注册站点地址，定时检查
		//		SiteOnWatchPool.getInstance().add(hub);
		updateStatusSites();

		Logger.debug(this, "__login", "hub is %s", hub);

		return true;
	}


	/**
	 * 显示切到新的HUB节点
	 * @param hub 新管理节点
	 */
	private void showSwitchHubTip(Node hub) {
		// 弹出提示对话框
		String title = getCaption("MessageBox/SwitchHub/Title/title");
		String content = getCaption("MessageBox/SwitchHub/Message/title") + hub.toString();

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/watch/image/message/warning.png", 32, 32);
		// 显示对话杠
		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, 
				icon, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 执行窗口切换处理
	 * @param hub 管理节点
	 */
	private void doSwitchHub(Node hub) {
		// 清除全部参数
		clearAll();
		// 注销登录后，登录菜单有效，注销/重新登录失效！
		setLoginMenuEnabled(true);
		// 下次自动显示...
		if (regToken != null) {
			regToken.setShow(true);
			regToken.setHub(hub);
		}
		// 最后显示对话框
		showSwitchHubTip(hub);
	}

	/**
	 * 切换管理节点的图形处理部分
	 *
	 * @author scott.liang
	 * @version 1.0 11/6/2019
	 * @since laxcus 1.0
	 */
	class WindowSwitchHubThread extends SwingEvent {
		Node hub;

		WindowSwitchHubThread (Node remote) {
			super();
			hub = remote;
		}

		public void process() {
			doSwitchHub(hub);
		}
	}

	/**
	 * 调用线程，切换到另一个管理站点
	 * @param hub 新的管理站点
	 */
	public void switchHubTo(Node hub) {
		addThread(new WindowSwitchHubThread(hub));
	}

	/**
	 * 注销线程
	 *
	 * @author scott.liang
	 * @version 1.0 11/7/2019
	 * @since laxcus 1.0
	 */
	class LogutThread extends SwingEvent {
		public LogutThread() {
			super();
		}
		public void process() {
			disableWindow();
		}
	}

	/**
	 * 登录线程
	 *
	 * @author scott.liang
	 * @version 1.0 11/7/2019
	 * @since laxcus 1.0
	 */
	class LoginThread extends SwingEvent {
		public LoginThread(){
			super();
		}
		public void process() {
			__login();
		}
	}

	/**
	 * 清除窗口操作
	 */
	protected void doLogoutThread() {
		addThread(new LogutThread());
	}

	/**
	 * 重新登录
	 */
	protected void doReloginThread() {
		addThread(new LogutThread());
		addThread(new LoginThread());
	}

	private void setAutoText(String xmlPath) {
		String content = WatchLauncher.getInstance().findContent(xmlPath);
		if (content != null) {
			setStatusText(content);
		}
	}

	/**
	 * 在线程驱动下自动登录
	 * @param hub 服务器主机
	 * @return 成功返回真，否则假
	 */
	public boolean __auto_login(Node hub) {
		// 让窗口信息失效
		disableWindow();

		// 显示自动登录
		setAutoText("Window/auto-login/retry");

		// 调用线程完成登录
		int who = WatchLauncher.getInstance().login(hub.getHost(), true, null);
		// 判断成功
		boolean success = WatchEntryFlag.isSuccessful(who);
		if (!success) {
			setAutoText("Window/auto-login/failed");
			return false;
		}

		// 登录成功后，登录菜单失效，注销和重新登录有效
		setLoginMenuEnabled(false);

		// 更新状态
		updateStatusSites();

		setAutoText("Window/auto-login/successful");

		Logger.debug(this, "__auto_login", "hub is %s", hub);

		return true;
	}

	/**
	 * 登录
	 */
	protected void login() {
		addThread(new LoginThread());
	}

	/**
	 * 从服务器注销
	 */
	protected void logout() {
		WatchLogoutThread e = new WatchLogoutThread(this);
		e.start();
	}

	/**
	 * 重新注册到服务器
	 */
	protected void relogin() {
		WatchReloginThread e = new WatchReloginThread(this);
		e.start();
	}

	/**
	 * 清除窗口全部参数
	 */
	private void clearAll() {
		// 清除全部
		left.clear();
		right.clear();
		bottom.clear();
		setStatusText(null);
	}

	/**
	 * 清除窗口全部显示
	 */
	private void disableWindow() {
		// 清除全部参数
		clearAll();
		// 注销登录后，登录菜单有效，注销/重新登录失效！
		setLoginMenuEnabled(true);
	}

	/**
	 * 根据当前语言环境，选择资源配置文件，建立菜单条
	 */
	private void createMenuBar() {
		String path = WatchLauncher.getInstance().getSurfacePath();
		MenuBarCreator creator = new MenuBarCreator();
		menubar = creator.create(path);
		// 建立皮肤菜单项目, 关键字“SkinMenu”在resource.xml配置文件中定义
		final String associate = "SkinMenu";
		JMenu menu = creator.findMenu(menubar, associate);
		if (menu != null) {
			createSkinMenu(menu);
		}

		// 生成实例
		creator.setActionListener(menubar, this);

		// 设置菜单字体
		Font font = WatchProperties.readMainMenuFont();
		if (font != null) {
			updateMainMenuFont(font);
		}

		menubar.setBorder(new EmptyBorder(2, 2, 0, 2));
		setJMenuBar(menubar);
	}

	/**
	 * 建立皮肤菜单项目
	 */
	private void createSkinMenu(JMenu menu) {
		SkinTokenLoader loader = WatchLauncher.getInstance().getSkinLoader();
		// 找到全部皮肤
		java.util.List<SkinToken> tokens = loader.getSkinTokens(); // WatchLauncher.getInstance().getSkinTokens();
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
	 * 切换菜单
	 * @param method 焦点方法
	 */
	private void exchangeSkin(final String method) {
		SkinTokenLoader loader = WatchLauncher.getInstance().getSkinLoader();
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

		// 切换主题界面，不更新组件UI
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
	 * 普通明白色Nimbus样式界面
	 * 此方法匹配"conf/watch/skin/config.xml"中的"method"定义
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
	 * 暗黑色Metal界面
	 * 此方法匹配"conf/watch/skin/config.xml"中的"method"定义
	 */
	public void doSkinDark() {
		exchangeSkin("doSkinDark");
	}

	/**
	 * 青铜色Metal界面
	 * 此方法匹配"conf/watch/skin/config.xml"中的"method"定义
	 */
	public void doSkinBronze() {
		exchangeSkin("doSkinBronze");
	}

	/**
	 * 深兰Metal界面
	 * 此方法匹配"conf/watch/skin/config.xml"中的"method"定义
	 */
	public void doSkinCyano() {
		exchangeSkin("doSkinCyano");
	}

	/**
	 * 根据当前语言环境，选择资源配置文件，建立工具条
	 */
	private void createToolBar() {
		String path = WatchLauncher.getInstance().getSurfacePath();
		ToolBarCreator creator = new ToolBarCreator();
		toolbar = creator.create(path);
		creator.setActionListener(toolbar, this);
		toolbar.setFloatable(false);
		toolbar.setBorderPainted(true);
		// 不要有边框
		toolbar.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		// 显示浮动按纽
		toolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
	}

	/**
	 * 初始化控件
	 */
	private void initControls() {
		// 窗口宽度
		Rectangle rect = getDefaultBounds();

		// 初始化面板、工具条、菜单！
		left.init();
		right.init(rect);
		bottom.init();
		createToolBar();
		createMenuBar();
		
		// 启动线程
		bottom.startDaemonThread();

		// 中心面板
		WatchPanel panel = new WatchPanel();
		panel.init(rect, toolbar, left, right, bottom);
		panel.setBorder(new EmptyBorder(4, 4, 4, 4));
		setContentPane(panel);

		// 窗口注册鼠标追踪
		addMouseMotion();

		// 调整此窗口的大小，以适合其子组件的首选大小和布局
		pack();
		// 设置窗口范围
		setBounds(rect);

//		// 最后，更新窗口外观
//		reloadUI();
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
	//		public void process(){			
	//			// 初始化控件
	//			initControls();
	//		}
	//	}


	/**
	 * 建立窗口和组件
	 */
	class CreateWatchThread extends SwingEvent {
		CreateWatchThread() {
			// 同步处理，要求SwingDispatcher等待线程工作完成才退出！
			super(true); 
		}

		public void process() {
			// 初始化窗口
			initWindow();
			// 初始化控件
			initControls();
		}
	}

	/**
	 * 最后显示窗口
	 */
	private void suffixProcess() {
		// 本窗口不需要们于其它窗口上方
		setAlwaysOnTop(false);
		// 置于后端
		toBack();
		//		// 显示窗口
		//		setVisible(true);
	}

	/**
	 * 后续处理
	 *
	 * @author scott.liang
	 * @version 1.0 6/11/2021
	 * @since laxcus 1.0
	 */
	class SuffixProcessThread extends SwingEvent {
		public SuffixProcessThread() {
			super(true); // 同步处理
		}

		@Override
		public void process() {
			suffixProcess();
		}
	}

	/**
	 * 初始化窗口
	 */
	private void initWindow() {
		String title = getCaption("Window/Frame/title");
		// 图标资源，在JAR文档中
		ResourceLoader loader = new ResourceLoader("conf/watch/image/window/");
		ImageIcon icon = loader.findImage("logo.png");
		// 标题
		setTitle(title);
		setIconImage(icon.getImage());

		WatchWindowAdapter adapter = new WatchWindowAdapter();
		addWindowFocusListener(adapter);
		addWindowListener(adapter);
		addWindowStateListener(adapter);

		// 忽略关闭窗口操作
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}


	//	private void addKey() {
	//		// 取得键盘事件
	//		addKeyListener(new KeyAdapter() {
	//			public void keyReleased(KeyEvent e) {
	//				if (e.getKeyCode() == KeyEvent.VK_F2) {
	//					if (!menubar.isVisible()) {
	//						menubar.setVisible(true);
	//					}
	//				}
	//			}
	//		});
	//	}

	//	private void addKey(JPanel panel) {
	//		// Container c = getContentPane();
	//
	//		panel.getInputMap(JComponent.WHEN_FOCUSED).put(
	//				KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK, true), "CTRL X");
	//		panel.getActionMap().put("CTRL X", new AbstractAction() {
	//			private static final long serialVersionUID = 1L;
	//
	//			public void actionPerformed(ActionEvent e) {
	////				if (!menubar.isVisible()) {
	////					menubar.setVisible(true);
	////				}
	//				
	//				System.out.println("UNIX SYSTEM");
	//			}
	//		});
	//	}

	//	/**
	//	 * 初始化窗口
	//	 *
	//	 * @author scott.liang
	//	 * @version 1.0 1/22/2020
	//	 * @since laxcus 1.0
	//	 */
	//	class InitWindowThread extends SwingEvent {
	//		InitWindowThread() {
	//			super(true); // 要求SwingDispatcher等待线程工作完成才退出！
	//		}
	//		public void process() {
	//			initWindow();
	//		}
	//	}

	/**
	 * 显示登录窗口
	 * @return 成功返回真，否则假
	 */
	private boolean showLoginWindow() {
		boolean success = false;

		// 以线程模式处理注册登录到TOP/BANK/HOME
		LaunchThread thread = new LaunchThread(this);
		// 启动线程
		boolean launch = thread.start();

		if (launch) {
			// 进入等待
			thread.await();
			// 判断登录成功
			success = thread.isSuccessful();
		}

		// 登录成功，显示窗口；不成功，释放它退出
		if (success) {
			//			addThread(new VisibleThread());
			right.focusCommand();
			// 图标设置为连接状态
			bottom.setConnectIcon();
			// 后续处理
			addThread(new SuffixProcessThread());
		} else {
			dispose();
		}

		return success;
	}

	class ShowThread extends SwingEvent {
		public ShowThread(){
			super(true); // 同步处理
		}
		public void process() {
			// 显示窗口
			setVisible(true);
		}
	}

	/**
	 * 显示窗口并且注册到服务器
	 * @return  成功返回真，否则假
	 */
	public boolean showWindow() {
		// 处理平台字体
		WatchLauncher.getInstance().doPlatformFont();

		//		// 初始化窗口
		//		addThread(new InitWindowThread());
		//		// 初始化控件
		//		addThread(new InitControlsThread());

		// 初始化窗口和组件
		addThread(new CreateWatchThread());
		// 最后，更新窗口外观
		reloadUI();

		// 如果是繁忙状态，延时...
		while (SwingDispatcher.getInstance().isBusy()) {
			SwingDispatcher.getInstance().delay(1000L);
		}

		// 显示登录窗口
		boolean success = showLoginWindow();

		// 最后显示
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
	 * 推送一个新的登录站点
	 * @param node LAXCUS站点地址
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean pushSite(Node node) {
		// 在左侧栏显示登录站点
		return left.pushSite(node);
	}

	/**
	 * 正常退出一个登录站点
	 * @param node LAXCUS站点地址
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean dropSite(Node node) {
		// 从左侧栏撤销登录站点
		return left.dropSite(node);
	}

	/**
	 * 以故障状态销毁一个登录站点
	 * @param node LAXCUS站点地址
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean destroySite(Node node) {
		// 从左侧栏销毁登录站点
		return dropSite(node);
	}

	/**
	 * 调整运行时
	 * @param runtime
	 */
	public void modify(SiteRuntime runtime) {
		left.modify(runtime);
	}

	/**
	 * 推送新的注册成员
	 * @param siger 用户签名
	 */
	public void pushRegisterMember(Siger siger) {
		left.pushRegisterMember(siger);
	}

	/**
	 * 删除注册成员
	 * @param siger 用户签名
	 */
	public void dropRegisterMember(Siger siger) {
		left.dropRegisterMember(siger);
	}

	/**
	 * 推送在线成员
	 * @param siger 用户签名
	 */
	public void pushOnlineMember(Siger siger) {
		left.pushOnlineMember(siger);
	}

	/**
	 * 删除在线成员
	 * @param siger 用户签名
	 */
	public void dropOnlineMember(Siger siger) {
		left.dropOnlineMember(siger);
	}

	/**
	 * 更新在线成员的状态
	 * @param siger 用户签名
	 */
	public void updateOnlineMember(Siger siger) {
		left.updateOnlineMember(siger);
	}

	/**
	 * 设置服务器地址
	 * @param node
	 */
	public void setHub(Node node) {
		bottom.setHub(node);
	}

	/**
	 * 更新状态栏的节点数目
	 */
	public void updateStatusSites() {
		int sites = SiteOnWatchPool.getInstance().size();
		bottom.setSites(sites);
	}

	/**
	 * 更新状态栏的注册成员数目
	 */
	public void updateStatusMembers() {
		// 分别是：注册账号、在线账号、在线人数。
		int registers = RegisterMemberBasket.getInstance().countMember();
		int onlines = FrontMemberBasket.getInstance().countMember();
		int users = FrontMemberBasket.getInstance().countUsers();
		bottom.setMembers(registers, onlines, users);
	}

	//	/**
	//	 * 状态栏图标闪烁线程
	//	 *
	//	 * @author scott.liang
	//	 * @version 1.0 3/2/2020
	//	 * @since laxcus 1.0
	//	 */
	//	class FlashThread extends SwingEvent {
	//		/** 图标ID **/
	//		int id;
	//
	//		public FlashThread(int who) {
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
	//	 * 闪烁图标
	//	 */
	//	public void flash() {
	//		// 非可视状态不处理
	//		if (!isVisible()) {
	//			return;
	//		}
	//
	//		ArrayList<SwingEvent> array = new ArrayList<SwingEvent>();
	//
	//		// 图标编号
	//		int[] icons = new int[] { WatchFlashTag.FLASH_START, WatchFlashTag.FLASH_NEXT };
	//
	//		// 线程之间的间隔时间是800毫秒
	//		final int interval = 800;
	//		long touchTime = System.currentTimeMillis();
	//
	//		// 播放动画效果
	//		for (int skip = 0; skip < 1; skip++) {
	//			for (int i = 0; i < icons.length; i++) {
	//				FlashThread event = new FlashThread(icons[i]);
	//				event.setIntervalTime(interval);
	//				event.setTouchTime(touchTime);
	//				array.add(event);
	//				// 下一次触发时间
	//				touchTime += interval;
	//			}
	//		}
	//
	//		// 结束时，多延长200毫秒，效果更好！
	//		FlashThread last = new FlashThread(WatchFlashTag.FLASH_STOP);
	//		last.setIntervalTime(interval + 200);
	//		last.setTouchTime(touchTime + 200);
	//		array.add(last);
	//
	//		// 一批线程放入队列
	//		SwingDispatcher.invokeThreads(array);
	//	}

	/**
	 * 闪烁图标
	 */
	public void flash() {
		bottom.flash();
	}

	/**
	 * 设置图标为在线状态或者否
	 * @param online 在线状态
	 */
	public void setOnlineIcon(boolean online) {
		if (online) {
			bottom.setConnectIcon();
		} else {
			bottom.setDisconnectIcon();
		}
	}

}


//	/**
//	 * 自动登录线程
//	 *
//	 * @author scott.liang
//	 * @version 1.0 1/22/2021
//	 * @since laxcus 1.0
//	 */
//	class AutoLoginThread extends SwingEvent {
//		/** 服务器节点地址 **/
//		Node hub;
//		
//		/**
//		 * 服务器地址
//		 * @param e
//		 */
//		AutoLoginThread(Node e) {
//			super();
//			hub = e;
//		}
//		
//		/*
//		 * (non-Javadoc)
//		 * @see com.laxcus.util.display.SwingEvent#process()
//		 */
//		public void process() {
//			__auto_login(hub);
//		}
//	}


//	/**
//	 * 启动由线程驱动的注册
//	 * @param hub 服务器地址
//	 * @return 成功返回真，否则假
//	 */
//	protected boolean doAutoLogin(Node hub) {		
//		// 让窗口信息失效
//		disableWindow();
//		// 自动注册
//		return __auto_login(hub);
//	}