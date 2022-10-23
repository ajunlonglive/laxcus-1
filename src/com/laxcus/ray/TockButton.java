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
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.application.manage.*;
import com.laxcus.container.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;
import com.laxcus.util.sound.*;

/**
 *
 * @author scott.liang
 * @version 1.0 2022-5-1
 * @since laxcus 1.0
 */
public class TockButton extends JPanel {

	private static final long serialVersionUID = 1L;

	/** 双击间隔 **/
	static long DOUBLECLICK_INTERVAL = 2000L; 

	/**
	 * 鼠标追踪器
	 *
	 * @author scott.liang
	 * @version 1.0 5/27/2021
	 * @since laxcus 1.0
	 */
	class MouseTracker extends MouseAdapter {

		/** 锁定图标 **/
		volatile boolean locked;

		/** 点击次数 **/
		private int clicks = 0;

		/** 时间 **/
		private long preTime = 0;

		/** 被点击选中的按纽 **/
		private Object select;

		/** 判断是拖放光标 **/
		private boolean draggedCursor ;

		/** 前一个按纽 **/
		private TockButton preview;

		/** 当前触发的按纽 **/
		private TockButton now;

		/** 鼠标在屏幕上的坐标位置 **/
		private Point point = new Point(0, 0);

		/**
		 * 构造鼠标追踪器
		 */
		public MouseTracker() {
			super();
			draggedCursor = false;
		}

		//		/**
		//		 * 按下左侧鼠标
		//		 * @param e
		//		 */
		//		@Override
		//		public void mousePressed(MouseEvent e) {
		//			Object source = e.getSource();
		//			// 桌面
		//			if (source == desktop) {
		//				if (preview != null) {
		//					preview.setFocus(false);
		//					preview = null;
		//				}
		//				if (now != null) {
		//					now.setFocus(false);
		//					now = null;
		//				}
		//				clicks = 0; // 重置
		//				return;
		//			}
		//			// 不是按纽，忽略
		//			if (source.getClass() != TockButton.class) {
		//				clicks = 0; // 重置
		//				return;
		//			}
		//
		//			// 左侧按纽
		//			if (e.getButton() == MouseEvent.BUTTON1) {
		//				// 前一个按纽
		//				preview = now;
		//				// 当前按纽
		//				now = (TockButton) source;
		//
		//				// 前一个按纽取消焦点，恢复状态
		//				if (preview != null && preview != now) {
		//					preview.setFocus(false); //  取消焦点
		//				}
		//				// 生成焦点重新绘制
		//				now.setFocus(true); 
		//				
		//				// 准备点击...
		//				
		//				// 当前时间
		//				long nowTime = System.currentTimeMillis();
		//				// 统计点击次数
		//				clicks++;
		//
		//				if (clicks > 1) {
		//					if (clicks == 2 && select != null && select == source
		//							&& nowTime - preTime <= DOUBLECLICK_INTERVAL) {
		//						// 触发事件
		//						clicks = 0;
		//						select = null;
		//						// 触发事件
		////						addThread(new RunApplicationThread(source));
		//						return;
		//					}
		//					// 重置
		//					clicks = 0;
		//					select = null;
		//				} else {
		//					preTime = nowTime;
		//					select = source;
		//				}
		//			}
		//
		//			// 取鼠标的坐标
		//			point = SwingUtilities.convertPoint((TockButton) source, e.getPoint(), ((TockButton) source).getParent());
		//		}

		/**
		 * 按下左侧鼠标
		 * @param e
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			Object rt = e.getSource();
			// 桌面
			if (rt == desktop) {
				if (preview != null) {
					preview.setFocus(false);
					preview = null;
				}
				if (now != null) {
					now.setFocus(false);
					now = null;
				}
				clicks = 0; // 重置
				return;
			}
			TockButton button = null;
			JComponent other = null;

			//			// 不是按纽，忽略
			//			if (rt.getClass() != TockButton.class) {
			//				clicks = 0; // 重置
			//				return;
			//			} else {
			//				source = (TockButton)rt;
			//			}

			if (Laxkit.isClassFrom(rt, JComponent.class)) {
				JComponent sub = (JComponent) rt;
				if (sub.getClass() == TockButton.class) {
					button = (TockButton) sub;
				} else {
					//					System.out.printf("class is %s -> %s\n",
					//							sub.getClass().getName(), sub.getParent().getClass().getName());

					if (sub.getParent() != null && sub.getParent().getClass() == TockButton.class) {
						other = sub;
						button = (TockButton) sub.getParent();
					}
				}
			}

			if (button == null) {
				clicks = 0;
				return;
			}

			// 左侧按纽
			if (e.getButton() == MouseEvent.BUTTON1) {
				// 前一个按纽
				preview = now;
				// 当前按纽
				now = (TockButton) button;

				// 前一个按纽取消焦点，恢复状态
				if (preview != null && preview != now) {
					preview.setFocus(false); //  取消焦点
				}
				// 生成焦点重新绘制
				now.setFocus(true); 

				// 准备点击...

				// 当前时间
				long nowTime = System.currentTimeMillis();
				// 统计点击次数
				clicks++;

				if (clicks > 1) {
					if (clicks == 2 && select != null && select == button
							&& nowTime - preTime <= DOUBLECLICK_INTERVAL) {
						// 触发事件
						clicks = 0;
						select = null;
						// 触发事件
						//						addThread(new RunApplicationThread(source));
						return;
					}
					// 重置
					clicks = 0;
					select = null;
				} else {
					preTime = nowTime;
					select = button;
				}
			}

			// 取鼠标的坐标
			if (other == null) {
				point = SwingUtilities.convertPoint(button, e.getPoint(), button.getParent());
			} else {
				// 两层转换
				Point p = SwingUtilities.convertPoint(other, e.getPoint(), other.getParent());
				point = SwingUtilities.convertPoint(button, p, button.getParent());
				//				System.out.printf("TRANSLATE %d:%d\n", point.x, point.y);
			}
		}

		/**
		 * 释放左侧鼠标
		 * @param e
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			Object source = e.getSource();

			// 恢复鼠标状态
			draggedCursor = false;
			if (Laxkit.isClassFrom(source, java.awt.Component.class)) {
				((java.awt.Component) source).setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			// 是桌面，处理它
			if (source == desktop) {
				if (preview != null) {
					preview.setFocus(false);
					preview = null;
				}
				if (now != null) {
					now.setFocus(false);
					now = null;
				}
				// 取消焦点状态的子窗口
				//				cancelFocusSubFrames();
				// 恢复成默认光标
				desktop.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				return;
			}
		}

		//		/*
		//		 * (non-Javadoc)
		//		 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
		//		 */
		//		@Override
		//		public void mouseDragged(MouseEvent e) {
		//			// 如果锁定，不允许移动
		//			if (locked) {
		//				return;
		//			}
		//			
		//			Object source = e.getSource();
		//			if (source.getClass() != TockButton.class) {
		//				return;
		//			}
		//			TockButton but = (TockButton) source;
		//
		//			// 改变鼠标光标
		//			if (!draggedCursor) {
		//				but.setCursor(new Cursor(Cursor.MOVE_CURSOR));
		//				draggedCursor = true;
		//			}
		//
		//			Point newPoint = SwingUtilities.convertPoint(but, e.getPoint(), but.getParent());
		//			// 调整位置
		//			// but.setLocation(but.getX() + (newPoint.x - point.x), but.getY() + (newPoint.y - point.y));
		//			int x = but.getX() + (newPoint.x - point.x);
		//			int y = but.getY() + (newPoint.y - point.y);
		//
		//			// 设置新位置
		//			but.setLocation(x, y);
		////			// 设置位置
		////			but.getItem().getContour().setX(x);
		////			but.getItem().getContour().setY(y);
		//			
		//			point = newPoint;
		//		}

		/*
		 * (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			// 如果锁定，不允许移动
			if (locked) {
				return;
			}

			TockButton button = null;
			JComponent other = null;
			Object rt = e.getSource();

			if (Laxkit.isClassFrom(rt, JComponent.class)) {
				JComponent sub = (JComponent) rt;
				if (sub.getClass() == TockButton.class) {
					button = (TockButton) sub;
				} else {
					//					System.out.printf("class is %s -> %s\n",
					//							sub.getClass().getName(), sub.getParent().getClass().getName());
					Object parent = sub.getParent();
					if (parent != null && parent.getClass() == TockButton.class) {
						other = sub;
						button = (TockButton) parent;
					}
				}
			}

			if (button == null) {
				return;
			}

			//			if (source.getClass() = TockButton.class) {
			//				return;
			//			}
			//			
			//			
			//			TockButton but =  (TockButton) source;

			// 改变鼠标光标
			if (!draggedCursor) {
				if (other == null) {
					button.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				} else {
					other.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				}
				draggedCursor = true;
			}

			if (other == null) {
				Point newPoint = SwingUtilities.convertPoint(button, e.getPoint(), button.getParent());
				// 调整位置
				// but.setLocation(but.getX() + (newPoint.x - point.x), but.getY() + (newPoint.y - point.y));
				int x = button.getX() + (newPoint.x - point.x);
				int y = button.getY() + (newPoint.y - point.y);

				// 设置新位置
				button.setLocation(x, y);
				//			// 设置位置
				//			but.getItem().getContour().setX(x);
				//			but.getItem().getContour().setY(y);

				point = newPoint;
			} else {
				Point p = SwingUtilities.convertPoint(other, e.getPoint(), other.getParent());
				Point newPoint = SwingUtilities.convertPoint(button, p, button.getParent());
				// but.setLocation(but.getX() + (newPoint.x - point.x), but.getY() + (newPoint.y - point.y));
				int x = button.getX() + (newPoint.x - point.x);
				int y = button.getY() + (newPoint.y - point.y);

				// 设置新位置
				button.setLocation(x, y);
				//				// 设置位置
				//				but.getItem().getContour().setX(x);
				//				but.getItem().getContour().setY(y);

				point = newPoint;
			}
		}
	}

	class TextMouseAdapter extends MouseAdapter {
		boolean press;

		public TextMouseAdapter() {
			super();
			press = false;
		}

		public void mouseEntered(MouseEvent e) {
			if (!press) {
				cmdIcon.setIcon(cmdIcon.getRolloverIcon());
				lblName.setForeground(Color.WHITE);
			}
		}

		public void mouseExited(MouseEvent e) {
			if (!press) {
				cmdIcon.setIcon(cmdIcon.getDisabledIcon());
				lblName.setForeground(textForeground);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			press = true;
			cmdIcon.setIcon(cmdIcon.getPressedIcon());
			lblName.setForeground(Color.WHITE);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			press = false;
			cmdIcon.setIcon(cmdIcon.getDisabledIcon());
			cmdIcon.setForeground(textForeground);
		}
	}

//	/** 边框颜色 **/
//	private static Color borderColor = new Color(197, 145, 90);

//	private static Color darkText = new Color(224, 224, 224);
	
//	private static Color normalForeground = new Color(218, 218, 218);
	
	private Color textForeground;

	/** 焦点 **/
	private boolean focus;

	/** 图标按纽 **/
	private JButton cmdIcon;

	/** 显示文本 **/
	private JLabel lblName;

	/** 文本 **/
	private JTextArea txtName;
	
	/** 桌面 **/
	private PlatformDesktop desktop;

	/** 鼠标追踪 **/
	private MouseTracker mouseTracker = new MouseTracker();
	
	/** 文本追踪 **/
	private TextMouseAdapter textTracker = new TextMouseAdapter();

	private RayButtonItem item;
	
	/** 弹出菜单 **/
	private JPopupMenu rockMenu;

	class ButtonMouseAdapter extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			showPopupMenu(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showPopupMenu(e);
		}
	}
	


	/**
	 * 
	 */
	public TockButton() {
		super();
		
	
		
		// 初始化
		init();

		desktop = PlatformKit.getPlatformDesktop();
		desktop.addMouseListener(mouseTracker);

		//		focus = true;
	}

	/**
	 * 设置单元参数
	 * @param e DesktopButtonItem实例
	 */
	public void setItem(RayButtonItem e) {
		item = e;
	}

	/**
	 * 返回单元参数
	 * @return DesktopButtonItem实例
	 */
	public RayButtonItem getItem() {
		return item;
	}

	/**
	 * 初始化参数
	 */
	private void init() {
		// 文本前景颜色
		Color c = UIManager.getColor("DesktopButton.TextForeground");
		if (c != null) {
			textForeground = new Color(c.getRGB());
		} else {
			textForeground = new Color(198, 198, 198);
		}
		
		setFocus(false);

		// 要求透明
		setOpaque(false);

		cmdIcon = new JButton();
		cmdIcon.setContentAreaFilled(false); // 平面
		cmdIcon.setBorderPainted(false); // 不绘制边框
		cmdIcon.setFocusPainted(false); // 不绘制焦点边框
		cmdIcon.setHorizontalAlignment(SwingConstants.CENTER);
		cmdIcon.setVerticalAlignment(SwingConstants.CENTER);
		
//		ImageIcon icon = (ImageIcon) UIManager.getIcon("DockBar.ShutdownIcon"); // "RayWindow.TitleIcon");
//		setIcon(icon, PlatformButton.DESKTOP_BUTTON_BRIGHTER, PlatformButton.DESKTOP_BUTTON_DARK);

		lblName = new JLabel();
		lblName.setHorizontalAlignment(SwingConstants.CENTER);
		lblName.setVerticalAlignment(SwingConstants.TOP);
		lblName.setBorder(new EmptyBorder(0, 2, 0, 2));
//		lblName.setForeground(Color.WHITE);
		lblName.addMouseListener(textTracker);
		super.addMouseListener(textTracker); // 本次处理 
		
		//		setText("我的计算机");
		//		setText("千里江山寒色远，芦花深处泊孤舟，笛在月明楼。");

		//		lblContent.setBorder(new LineBorder(Color.white));
		//		cmdIcon.setBorder(new LineBorder(Color.white));

		txtName = new JTextArea();
		txtName.setBorder(new EmptyBorder(0, 0, 0, 0));
//		txtContent.setBorder(new LineBorder(Color.white));
//		txtContent.setText("我的计算机，千里江山寒色远，芦花深处泊孤舟，笛在月明楼。");
		txtName.setLineWrap(true);
//		txtContent.setOpaque(false); // 透明
		txtName.addKeyListener(new TextKeyAdapter());

		// 鼠标事件
		addMouseListener(mouseTracker);
		addMouseMotionListener(mouseTracker);
		
//		// 鼠标事件
//		cmdIcon.addMouseListener(mouseTracker);
//		cmdIcon.addMouseMotionListener(mouseTracker);
//		// 鼠标事件
//		lblContent.addMouseListener(mouseTracker);
//		lblContent.addMouseMotionListener(mouseTracker);

		setLayout(new BorderLayout(0, 0));
		setBorder(new EmptyBorder(0, 2, 2, 2));

		add(cmdIcon, BorderLayout.NORTH);
		//	add(txtContent, BorderLayout.CENTER);
		add(lblName, BorderLayout.CENTER);
		
		// 绑定弹出菜单
		initPopupMenu();
	}
	
	/**
	 * 键盘输入适配器
	 *
	 * @author scott.liang
	 * @version 1.0 6/9/2021
	 * @since laxcus 1.0
	 */
	class TextKeyAdapter extends KeyAdapter {

		/**
		 * 构造默认的键盘输入适配器
		 */
		public TextKeyAdapter(){
			super();
		}

//		/*
//		 * (non-Javadoc)
//		 * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
//		 */
//		@Override
//		public void keyPressed(KeyEvent e) {
//			switch(e.getKeyCode()) {
//			case KeyEvent.VK_ENTER:
//				if (e.isAltDown()) {
//					doFullScreen(); // 全屏切换
//				} else if(!e.isControlDown() && !e.isShiftDown()) {
//					doEnter();
//				}
//				break;
//			case KeyEvent.VK_PAGE_DOWN:
//			case KeyEvent.VK_DOWN:
//				keyPressNext(); // 下一个
//				break;
//			case KeyEvent.VK_PAGE_UP:
//			case KeyEvent.VK_UP:
//				keyPressPrevious(); // 前一个
//				break;
//			default:
//				// 调用超类
//				super.keyPressed(e);
//				break;
//			}
//		}

		/*
		 * (non-Javadoc)
		 * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
		 */
		@Override
		public void keyReleased(KeyEvent e) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
			case KeyEvent.VK_ENTER:
				cancelRename();
				break;
			}
			
//			switch(e.getKeyCode()) {
//			//			case KeyEvent.VK_ENTER:
//			////				if (e.isAltDown()) {
//			////					doFullScreen();
//			////				} else if(!e.isControlDown() && !e.isShiftDown()) {
//			////					doEnter();
//			////				}
//			//				
//			////				// 没有换ALT，做回车判断
//			////				if (!e.isAltDown()) {
//			////					doEnter();
//			////				}
//			//				break;
//			case KeyEvent.VK_PAGE_DOWN:
//			case KeyEvent.VK_DOWN:
//				doNext();
//				break;
//			case KeyEvent.VK_PAGE_UP:
//			case KeyEvent.VK_UP:
//				doPrevious();
//				break;
//			case KeyEvent.VK_ESCAPE:
//				addThread(new CancelFullScreen());; // 用线程取消全屏显示
//				break;
//			default:
//				doDefault();
//				break;
//			}
		}
	}


	/**
	 * 初始化弹出菜单
	 */
	private void initPopupMenu() {
		// 菜单
		rockMenu = new JPopupMenu();
		rockMenu.add(MenuBuilder.createMenuItem(null, "DesktopButton.PopupMenu.RunText", "DesktopButton.PopupMenu.RunMWord",
				null, "DesktopButton.PopupMenu.RunMethodName", new MenuItemClick()));
		rockMenu.addSeparator();
		rockMenu.add(MenuBuilder.createMenuItem(null, "DesktopButton.PopupMenu.DeleteText", "DesktopButton.PopupMenu.DeleteMWord",
				null, "DesktopButton.PopupMenu.DeleteMethodName", new MenuItemClick()));
		rockMenu.addSeparator();
		rockMenu.add(MenuBuilder.createMenuItem(null, "DesktopButton.PopupMenu.RenameText", "DesktopButton.PopupMenu.RenameMWord",
				null, "DesktopButton.PopupMenu.RenameMethodName", new MenuItemClick()));

		// 调用
		rockMenu.setInvoker(this);
		
		super.addMouseListener(new ButtonMouseAdapter());
		cmdIcon.addMouseListener(new ButtonMouseAdapter());
		lblName.addMouseListener(new ButtonMouseAdapter());
	}
	
	class MenuItemClick implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			click(e);
		}
	}

	/**
	 * 菜单事件
	 * @param event
	 */
	private void click(ActionEvent event) {
		Object object = event.getSource();
		// 必须是继承自“JMenuItem”
		if (Laxkit.isClassFrom(object, JMenuItem.class)) {
			JMenuItem source = (JMenuItem) object;
			String methodName = source.getName();
			invoke(methodName);
		}
	}

	/**
	 * 调用实例
	 * @param methodName
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
	 * 运行
	 */
	void doRun() {
//		start();
	}

	class RunApplicationThread extends SwingEvent {
		WKey key;

		public RunApplicationThread(WKey e) {
			super();
			key = e;
		}

		@Override
		public void process() {
			int ret = -1;
			try {
				ret = ApplicationStarter.start(key);
			} catch (SecurityException e) {
				Logger.error(e);
			} catch (IllegalArgumentException e) {
				Logger.error(e);
			} catch (IOException e) {
				Logger.error(e);
			} catch (InstantiationException e) {
				Logger.error(e);
			} catch (IllegalAccessException e) {
				Logger.error(e);
			} catch (NoSuchMethodException e) {
				Logger.error(e);
			} catch (InvocationTargetException e) {
				Logger.error(e);
			} catch (Throwable e) {
				Logger.fatal(e);
			}
			// 弹出错误
			if (ret != 0) {
				String title = UIManager.getString("ApplicationStart.startFaultTitle");
				String content = UIManager.getString("ApplicationStart.startFaultContent");
				PlatformKit.getPlatformDesktop().playSound(SoundTag.ERROR);
				MessageBox.showFault(PlatformKit.getPlatformDesktop(), title, content);
			}
		}
	}

	/**
	 * 弹出错误
	 * @param title
	 * @param content
	 */
	private void showFault(String title, String content) {
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		desktop.playSound(SoundTag.ERROR);
		MessageBox.showFault(desktop, title, content);
	}

	/**
	 * 启动
	 */
	public void start() {
		if (item == null) {
			String title = UIManager.getString("ApplicationStart.notfoundTitle");
			String content = UIManager.getString("ApplicationStart.notfoundContent");
			showFault(title, content);
			return;
		}

		WKey key = item.getKey();

		// 不存在时
		if (!RTManager.getInstance().hasProgram(key)) {
			String title = UIManager.getString("ApplicationStart.notfoundTitle");
			String content = UIManager.getString("ApplicationStart.notfoundContent");
			showFault(title, content);
			return;
		}

		// 放入线程
		SwingDispatcher.invokeThread(new RunApplicationThread(key));

		//		RunApplicationThread rt = new RunApplicationThread(item.getKey());
		//		rt.execute();

		//		int ret = startApplication(item.getKey());
		//		if (ret != 0) {
		//			String title = UIManager.getString("ApplicationStart.startFaultTitle");
		//			String content = UIManager.getString("ApplicationStart.startFaultContent");
		//			showFault(title, content);
		//		}
	}

	void doDelete() {
//		String title = UIManager.getString("DesktopButton.DeleteTitle"); // "删除按纽";
//		String content = UIManager.getString("DesktopButton.DeleteConfirmText");
//		content = String.format(content, item.getTitle());
//		// 确认
//		boolean allow = MessageBox.showYesNoDialog(PlatformKit.getPlatformDesktop(), title, content);
//		if (!allow) {
//			return;
//		}
//
//		WKey key = item.getKey();
//		boolean success = RayController.getInstance().hasDesktopButton(key);
//		if (success) {
//			success = RayController.getInstance().deleteDesktopButton(key);
//			if (success) {
//				RTManager.getInstance().setShiftout(key, true);
//			} else {
//				title = UIManager.getString("DesktopButton.DeleteErrorTitle"); 
//				content = UIManager.getString("DesktopButton.DeleteErrorContent");
//				content = String.format(content, item.getTitle());
//				MessageBox.showFault(PlatformKit.getPlatformDesktop(), title, content);
//			}
//		}
		
		System.out.println("FUCK, DELETE!");
	}

	void doRename() {
//		NameInputDialog dlg = new NameInputDialog(UIManager.getString("DesktopButton.InputDialog.RenameTitle"));
//		dlg.setApproveText(UIManager.getString("DesktopButton.InputDialog.RenameText"));
//		dlg.setInitInputText(item.getTitle());
//
//		String text = (String) dlg.showDialog(PlatformKit.getPlatformDesktop());
//		if (text == null) {
//			return;
//		}
//
//		// 设置新的名称
//		item.setTitle(text);
//		setText(text);
		
		String text = lblName.getName();
		txtName.setText(text);
		
		// 删除
		remove(lblName);
		// 保存
		add(txtName, BorderLayout.CENTER);
		
		validate();
		repaint();

//		// 设置选中区域
//		if (text.length() > 0) {
//			txtContent.selectAll();
//			txtContent.setCaretPosition(text.length());
////			txtContent.select(0, text.length());
//		}
		
		SwingDispatcher.invokeThread(new SelectAllThread());
		
//		txtContent.setSelectionStart(0);
//		txtContent.setSelectionEnd( text.length() );
		
//		System.out.println("FUCK, RENAME!!!");
	}
	
	class SelectAllThread extends SwingEvent {
		SelectAllThread() {
			super();
		}

		@Override
		public void process() {
			// 设置选中区域
			int len = txtName.getDocument().getLength();
			if (len > 0) {
				txtName.requestFocus();
				txtName.setCaretPosition(len);
			}
		}
	}
	
	void cancelRename() {
		String text = txtName.getText();
		if (text == null) {
			text = "";
		}
		
		// 过滤全部回车换行符
		text = text.replace('\n', (char) 0x20);
		text = text.replace('\r', (char) 0x20);

		// 过滤回车符
		String regex = "^\\s*([\\w\\W]+?)\\s*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		if (matcher.matches()) {
			text = matcher.group(1);
		}

		// 删除
		remove(txtName);
		// 保存
		add(lblName, BorderLayout.CENTER);
		// 撤销
		validate();
		repaint();

		// 设置文本
		setText(text);
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

		// 如果是系统组件，不允许删除
		JMenuItem mi = MenuBuilder.findMenuItemByMethod(rockMenu, "doDelete");
		if (mi != null) {
//			mi.setEnabled(item.isSystem() ? false : true);
		}

		int newX = e.getX();
		int newY = e.getY();
		rockMenu.show(rockMenu.getInvoker(), newX, newY);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#addMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public void addMouseListener(MouseListener l) {
		super.addMouseListener(l);
		if (cmdIcon != null) {
			cmdIcon.addMouseListener(l);
		}
		if (lblName != null) {
			lblName.addMouseListener(l);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#addMouseMotionListener(java.awt.event.MouseMotionListener)
	 */
	@Override
	public void addMouseMotionListener(MouseMotionListener l) {
		super.addMouseMotionListener(l);
		if (cmdIcon != null) {
			cmdIcon.addMouseMotionListener(l);
		}
		if (lblName != null) {
			lblName.addMouseMotionListener(l);
		}
	}

	/**
	 * 设置图标
	 * @param icon
	 * @param brighter
	 * @param dark
	 */
	public void setIcon(ImageIcon icon, int brighter, int dark) {
		cmdIcon.setIcon(icon);
		
		cmdIcon.setDisabledIcon(icon);

		// ESL的亮度增加
		if (brighter > 0) {
			// 亮
			ImageIcon image = ImageUtil.brighter(icon, brighter);
			if (image != null) {
				cmdIcon.setRolloverIcon(image);
			}
		}
		// 调暗
		if (dark < 0) {
			cmdIcon.setRolloverEnabled(true); // 浮入
			// 暗
			ImageIcon image = ImageUtil.dark(icon, dark);
			if (image != null) {
				cmdIcon.setPressedIcon(image);
			}
		}
	}

	/**
	 * 设置按纽为焦点
	 * @param b
	 */
	public void setFocus(boolean b) {
		focus = b;
		repaint();
		
//		if (focus) {
//			setForeground(Color.WHITE);
//		} else {
//			setForeground(darkText);
//		}
	}

	/**
	 * 判断是焦点
	 * @return
	 */
	public boolean isFocus() {
		return focus;
	}

//	/**
//	 * 生成自动换行的HTML格式文本提示
//	 * @param text 提示文本
//	 * @param label 按纽
//	 * @return 返回经过格式化处理的提示文本
//	 */
//	private String createText(String text, JLabel label) {
//		FontMetrics fm = label.getFontMetrics(label.getFont());
//		int fontHeight = fm.getHeight();
//
//		char[] words = text.toCharArray();
//		int len = fm.charsWidth(words, 0, words.length);
//		int width = label.getWidth();
//		int height = label.getHeight();
//		
////		System.out.printf("FK: %d %d -> %d\n", width, height, len);
//		
//		// 小于指定宽度
//		if (len < width) {
//			return text;
//		}
//		
//		// 两边空格
//		width -= 8;
//
//		String title = "";
//		int start = 0;
//		int last = 0;
//		int count = 0;
//		boolean bottom = false;
//		
//		for (last++; last < words.length; last++) {
//			len = fm.charsWidth(words, start, last - start);
//			if (len > width) {
//				String str = text.substring(start, last - 1);
//				// 压缩
//				bottom = (count + fontHeight * 2 > height);
//				if (bottom) {
////					str = (str.length() > 2 ? str.substring(0, str.length() - 2) : str.substring(0, str.length() - 1));
//					
//					if (str.length() > 1) {
//						str = str.substring(0, str.length() - 1);
//					}
//					str = str + "...";
//				}
//				
//				if (title.isEmpty()) {
//					title = str;
//				} else {
////					title = String.format("%s<br>%s", title, str);
//					title = String.format("%s%s", title, str);
//				}
//
//				start = last - 1;
//				count += fontHeight;
//				
//				if (bottom) {
//					break;
//				}
//			}
//		}
//
//		// 最后的
//		if (!bottom) {
//			if (start < last) {
//				String str = text.substring(start, last);
//				if (title.isEmpty()) {
//					title = str;
//				} else {
////					title = String.format("%s<br>%s", title, str);
//					title = String.format("%s%s", title, str);
//				}
//			}
//		}
//
//		return String.format("<html><center>%s</center></html>", title);
//	}

	/**
	 * 生成自动换行的HTML格式文本提示
	 * @param text 提示文本
	 * @param label 按纽
	 * @return 返回经过格式化处理的提示文本
	 */
	private String createText(String text, JLabel label) {
		FontMetrics fm = label.getFontMetrics(label.getFont());
		int fontHeight = fm.getHeight();

		char[] words = text.toCharArray();
		int len = fm.charsWidth(words, 0, words.length);
		int width = label.getWidth();
		int height = label.getHeight();

		// 小于指定宽度
		if (len < width) {
			return text;
		}
		
		// 两边空格
		width -= 10;
		
		// 最少一行
		int rows = height / fontHeight;
		if (rows < 1) rows = 1;
		
		// 总长度
		int length = width * rows;
		
		String title = null;
		int start = 0;
		int last = 0;
		
		for (last++; last < words.length; last++) {
			len = fm.charsWidth(words, start, last - start);
			if (len >= length) {
				title = text.substring(start, (last > 2 ? last - 2 : last - 1));
				title = title + "...";
				break;
			}
		}

		if (title == null) {
			title = text;
		}

//		String title = "";
//		int start = 0;
//		int last = 0;
//		int count = 0;
//		boolean bottom = false;
//		
//		for (last++; last < words.length; last++) {
//			len = fm.charsWidth(words, start, last - start);
//			if (len > width) {
//				String str = text.substring(start, last - 1);
//				// 压缩
//				bottom = (count + fontHeight * 2 > height);
//				if (bottom) {
//					if (str.length() > 1) {
//						str = str.substring(0, str.length() - 1);
//					}
//					str = str + "...";
//				}
//				
//				if (title.isEmpty()) {
//					title = str;
//				} else {
//					title = String.format("%s%s", title, str);
//				}
//
//				start = last - 1;
//				count += fontHeight;
//				
//				if (bottom) {
//					break;
//				}
//			}
//		}
//
//		// 最后的
//		if (!bottom) {
//			if (start < last) {
//				String str = text.substring(start, last);
//				if (title.isEmpty()) {
//					title = str;
//				} else {
////					title = String.format("%s<br>%s", title, str);
//					title = String.format("%s%s", title, str);
//				}
//			}
//		}

		return String.format("<html><center>%s</center></html>", title);
	}
	
	/**
	 * 设置文本
	 * @param text
	 */
	public void setText(String text) {
		String html = createText(text, lblName);

		// String html = String.format("<html>%s</html>", text);
		// System.out.println(html);

		lblName.setText(html);
		lblName.setName(text);
		// 默认是白色前景
		lblName.setForeground(textForeground);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setToolTipText(java.lang.String)
	 */
	@Override
	public void setToolTipText(String text) {
		super.setToolTipText(text);
		if (cmdIcon != null) {
			cmdIcon.setToolTipText(text);
		}
		if (lblName != null) {
			lblName.setToolTipText(text);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (focus) {
			Color old = g.getColor();

			int x1 = 0;
			int y1 = 0;
			int x2 = x1 + getWidth() - 1;
			int y2 = y1 + getHeight() - 1;

			//			// 边框颜色
			//			if (isNimbusUI()) {
			//				g.setColor(borderColor);
			//			} else {
			//				g.setColor(Color.WHITE);
			//			}

			//			// 边框颜色
			//			Color c = UIManager.getColor("TextField.foreground");
			//			if (c != null) {
			//				g.setColor(new Color(c.getRGB()));
			//			} else {
			//				g.setColor(Color.WHITE);
			//			}

			g.setColor(Color.WHITE);

			for (int x = 0; x < x2; x += 2) {
				g.drawLine(x, y1, x, y1); // 上横线点
				g.drawLine(x, y2, x, y2); // 下横线点
			}
			for (int y = 0; y < y2; y += 2) {
				g.drawLine(x1, y, x1, y); // 左侧线点
				g.drawLine(x2, y, x2, y); // 右侧线点
			}

			//			// 测试..
			//			int w = getWidth();
			//			int h = getHeight();
			//			g.drawRoundRect(0, 0, w-1, h-1, 12, 12);

			// 恢复原来的颜色
			g.setColor(old);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.desktop.status.CraftButton#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		if (rockMenu != null) {
			FontKit.updateDefaultFonts(rockMenu, true);
			rockMenu.updateUI();
		}
		
		if (lblName != null) {
			lblName.updateUI();
			String text = lblName.getName();
			setText(text);
		}
		if (txtName != null) {
			txtName.updateUI();
		}
	}

}