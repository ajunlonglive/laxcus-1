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
import java.util.*;

import javax.swing.*;

import com.laxcus.application.manage.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.ray.util.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.event.*;
import com.laxcus.util.lock.*;

/**
 * 桌面管理器 <br>
 * 管理桌面上的图标
 * 
 * @author scott.liang
 * @version 1.0 7/20/2021
 * @since laxcus 1.0
 */
final class RayController extends MutexHandler {

	/** 双击间隔 **/
	static long DOUBLECLICK_INTERVAL = 1200L; 
	
	/** 左上角初始坐标位置 **/
	private final static int LEFT_X = 6;
	private final static int LEFT_Y = 6;

	/**
	 * 键盘回车
	 *
	 * @author scott.liang
	 * @version 1.0 7/30/2021
	 * @since laxcus 1.0
	 */
	class EnterAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				Object o = e.getSource();
				if (Laxkit.isClassFrom(o, JComponent.class)) {
					doEnter((JComponent) o);
				}
			}
		}

		private void doEnter(JComponent o) {
			if (o.getClass() == RayButton.class) {
				addThread(new RunApplicationThread(o));
			} else {
				Component parent = o.getParent();
				if (parent != null && parent.getClass() == RayButton.class) {
					addThread(new RunApplicationThread(parent));
				}
			}
		}
	}

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
		private int clicks;

		/** 时间 **/
		private long preTime;

		/** 被点击选中的按纽 **/
		private Object select;

		/** 判断是拖放光标 **/
		private boolean draggedCursor;

		/** 前一个按纽 **/
		private RayButton preview;

		/** 当前触发的按纽 **/
		private RayButton now;

		/** 鼠标在屏幕上的坐标位置 **/
		private Point point = new Point(0, 0);

		/**
		 * 构造鼠标追踪器
		 */
		public MouseTracker() {
			super();
			clicks = 0;
			preTime = 0L;
			draggedCursor = false;
		}

		/**
		 * 按下左侧鼠标
		 * @param e
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			Object source = e.getSource();
			// 桌面
			if (source == desktop) {
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
			
			// 判断来源，获取数值
			RayButton button = null;
			JComponent other = null;
			if (Laxkit.isClassFrom(source, JComponent.class)) {
				JComponent component = (JComponent) source;
				if (component.getClass() == RayButton.class) {
					button = (RayButton) component;
				} else {
					Component parent = component.getParent();
					if (parent != null && parent.getClass() == RayButton.class) {
						other = component;
						button = (RayButton) parent;
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
				now = (RayButton) button;

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
					if (clicks == 2 && select != null && select == button && nowTime - preTime <= DOUBLECLICK_INTERVAL) {
						// 触发事件
						clicks = 0;
						select = null;
						// 触发事件
						addThread(new RunApplicationThread(button));
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
			}
		}
		
		/**
		 * 释放按纽
		 * @param source
		 * @param pnt
		 */
		private void releaseButton(Object source, Point pnt) {
			RayButton button = null;
			JComponent other = null;

			JComponent component = (JComponent) source;
			if (component.getClass() == RayButton.class) {
				button = (RayButton) component;
			} else {
				Object parent = component.getParent();
				if (parent != null && parent.getClass() == RayButton.class) {
					other = component;
					button = (RayButton) parent;
				}
			}

			// 没有找到时，忽略
			if (button == null) {
				return;
			}

			Point newPoint = null;
			if (other == null) {
				newPoint = SwingUtilities.convertPoint(button, pnt, button.getParent());
			} else {
				Point p = SwingUtilities.convertPoint(other, pnt, other.getParent());
				newPoint = SwingUtilities.convertPoint(button, p, button.getParent());
			}

			// 调整位置
			int x = button.getX() + (newPoint.x - point.x);
			int y = button.getY() + (newPoint.y - point.y);
			
			// 调整位置
			if (x < 0) x = 0;
			if (y < 0) y = 0;

			// 判断落在哪个区域，定位一个区域
			Point p = scalePoint(x, y);
			// 判断位置被占用
			p = affirmPoint(p, button);

			// 坐标有效，按纽定位到新位置；否则返回原来位置
			if (p != null) {
				// 设置位置
				button.getItem().getContour().setX(p.x);
				button.getItem().getContour().setY(p.y);
				// 设置新位置
				button.setLocation(p.x, p.y);
				point = p;
			} else {
				// 找最近的一个坐标
				p = scaleNearPoint(x, y, button);
				if (p != null) {
					// 设置位置
					button.getItem().getContour().setX(p.x);
					button.getItem().getContour().setY(p.y);
					// 设置新位置
					button.setLocation(p.x, p.y);
					point = p;
				} else {
					RayContour r = button.getItem().getContour();
					button.setLocation(r.x, r.y);
				}
			}
		}

		/**
		 * 释放左侧鼠标
		 * @param e
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			Object source = e.getSource();
			
			// 如果处于拖放状态，并且是按纽时
			if (draggedCursor) {
				releaseButton(source, e.getPoint());
				
				// 隐藏窗口
				hideMagneticWindow();
			}

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
				cancelFocusSubFrames();
				// 恢复成默认光标
				desktop.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				return;
			}
		}

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

			clicks = 0; // 清除
			RayButton button = null;
			JComponent other = null;
			Object source = e.getSource();

			if (Laxkit.isClassFrom(source, JComponent.class)) {
				JComponent component = (JComponent) source;
				if (component.getClass() == RayButton.class) {
					button = (RayButton) component;
				} else {
					Object parent = component.getParent();
					if (parent != null && parent.getClass() == RayButton.class) {
						other = component;
						button = (RayButton) parent;
					}
				}
			}

			if (button == null) {
				return;
			}

			// 改变鼠标光标
			if (!draggedCursor) {
				if (other == null) {
					button.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				} else {
					other.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				}
				draggedCursor = true;
			}
			
			// 判断位置
			Point newPoint = null;
			if (other == null) {
				newPoint = SwingUtilities.convertPoint(button, e.getPoint(), button.getParent());
			} else {
				Point p = SwingUtilities.convertPoint(other, e.getPoint(), other.getParent());
				newPoint = SwingUtilities.convertPoint(button, p, button.getParent());
			}
			
			// 调整位置
			int x = button.getX() + (newPoint.x - point.x);
			int y = button.getY() + (newPoint.y - point.y);
			
			// 定位左上角
			Point sp = scalePoint(x, y);
			if (sp != null) {
				// 没有覆盖，打开它；否则关闭它
				if (!isOverride(sp.x, sp.y, button)) {
					showMagneticWindow(sp.x, sp.y, true);
				} else {
					hideMagneticWindow();
				}
			} else {
				// 如果边框打开，让边框消失
				hideMagneticWindow();
			}

			// 设置新位置
			button.setLocation(x, y);
			// 记录它
			point = newPoint;
		}
	}
	
	/**
	 * 双击/回车启动应用程序
	 *
	 * @author scott.liang
	 * @version 1.0 7/30/2021
	 * @since laxcus 1.0
	 */
	class RunApplicationThread extends SwingEvent {
		Object source;
		
		public RunApplicationThread(Object e){
			super();
			source = e;
		}
		
		public void process() {
			click(source);
		}
	}
	
	/**
	 * 单点/回车启动应用程序
	 * @param source
	 */
	private void click(Object source) {
		boolean success = (source != null && Laxkit.isClassFrom(source, RayButton.class));
		if (!success) {
			return;
		}
		// 启动应用
		RayButton but = (RayButton) source;
		but.start();
	}

	private static RayController selfHandle = new RayController();
	
	/** 判断为可用 **/
	private volatile boolean usabled = false;

	/** 已经更新或者否 **/
	private volatile boolean updated = false;

	/** 桌面 **/
	private PlatformDesktop desktop;
	
	/** 坐标 **/
	private ArrayList<Axis> points = new ArrayList<Axis>();
	
	/** 桌面按纽 **/
	private ArrayList<RayButton> buttons = new ArrayList<RayButton>();

	/** 鼠标追踪 **/
	private MouseTracker mouseTracker = new MouseTracker();
	
	/** 磁吸窗口 **/
	private RayMagneticWindow magicWindow;
	
	/**
	 * 构造桌面管理器
	 */
	private RayController() {
		super();
		usabled = false;
		updated = false;
	}
	
	/**
	 * 返回桌面管理器实例
	 * @return
	 */
	public static RayController getInstance() {
		return RayController.selfHandle;
	}
	
	/**
	 * 取得在桌面的坐标
	 * @param x
	 * @param y
	 * @return
	 */
	private Point scalePoint(int x, int y) {
		Dimension dim = RayUtil.getDesktopButtonSize(); // 桌面按纽尺寸
		for (Axis a : points) {
			int leftX = a.point.x;
			int leftY = a.point.y;
			int rightX = leftX + dim.width;
			int rightY = leftY + dim.height;
			if ((leftX <= x && x < rightX) && (leftY <= y && y < rightY)) {
				return new Point(leftX, leftY);
			}
		}
		return null;
	}

	/**
	 * 判断这个坐标被其它的按纽占用
	 * @param point 坐标位置
	 * @param current 当前按纽
	 * @return 没有返回实例，占用是空指针
	 */
	private Point affirmPoint(Point point, RayButton current) {
		if (point == null) {
			return null;
		}

		for (RayButton but : buttons) {
			// 忽略当前的
			if (but == current) {
				continue;
			}
			RayContour r = but.getItem().getContour();
			// 如果被占用了，返回空指针
			if (r.x == point.x && r.y == point.y) {
				return null;
			}
		}
		// 返回这个坐标
		return point;
	}
	
	/**
	 * 根据按纽位置定位它的坐标
	 * @param x
	 * @param y
	 * @return
	 */
	private Axis scaleAxis(int x, int y) {
		if (x <= RayController.LEFT_X) x = RayController.LEFT_X;
		if (y <= RayController.LEFT_Y) y = RayController.LEFT_Y;

		Dimension dim = RayUtil.getDesktopButtonSize(); // 桌面按纽尺寸
		for (Axis a : points) {
			int leftX = a.point.x;
			int leftY = a.point.y;
			int rightX = leftX + dim.width;
			int rightY = leftY + dim.height;
			if ((leftX <= x && x < rightX) && (leftY <= y && y < rightY)) {
				return a;
			}
		}
		return null;
	}
	
	/**
	 * 根据排序顺序找到对就的坐标
	 * @param h
	 * @param v
	 * @return
	 */
	private Axis scaleAlignment(int h, int v) {
		for (Axis a : points) {
			if (h == a.h && v == a.v) {
				return a;
			}
		}
		return null;
	}
	
	/**
	 * 找最近的一个点
	 * 先找下方的，不成立找右侧的
	 * @param x
	 * @param y
	 * @param current
	 * @return
	 */
	private Point scaleNearPoint(int x, int y, RayButton current) {
		Axis a = scaleAxis(x, y);
		if (a == null) {
			return null;
		}

		// 下面的一个坐标
		Axis next = scaleAlignment(a.h, a.v + 1);
		if (next != null) {
			Point p = affirmPoint(next.point, current);
			if (p != null) {
				return p;
			}
		}

		// 右侧的一个坐标
		next = scaleAlignment(a.h + 1, a.v);
		if (next != null) {
			Point p = affirmPoint(next.point, current);
			if (p != null) {
				return p;
			}
		}
		
		// 右侧下方的一个坐标
		next = scaleAlignment(a.h + 1, a.v + 1);
		if (next != null) {
			Point p = affirmPoint(next.point, current);
			if (p != null) {
				return p;
			}
		}
		
		return null;
	}

	/**
	 * 判断已经覆盖
	 * @param x
	 * @param y
	 * @param that
	 * @return
	 */
	private boolean isOverride(int x, int y, RayButton that) {
		Dimension dim = RayUtil.getDesktopButtonSize(); // 桌面按纽尺寸
		for (RayButton but : buttons) {
			if (but == that) {
				continue;
			}
			RayContour p = but.getItem().getContour();
			int leftX = p.x;
			int leftY = p.y;
			int rightX = leftX + dim.width;
			int rightY = leftY + dim.height;
			if ((leftX <= x && x < rightX) && (leftY <= y && y < rightY)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断为可用
	 * @return
	 */
	public boolean isUsabled() {
		return usabled;
	}

	/**
	 * 设置为可用或者否
	 * @param b
	 */
	public void setUsabled(boolean b) {
		usabled = b;
	}

	/**
	 * 重置更新
	 */
	public void resetUpdate(){
		updated = false;
	}

	/**
	 * 判断更新
	 * @return
	 */
	public boolean isUpdated(){
		return updated;
	}
	
	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}
	
	/**
	 * 建立磁吸式窗口
	 * @param e
	 */
	private void createMagneticWindow(PlatformDesktop e) {
		magicWindow = new RayMagneticWindow();
		magicWindow.setInvoker(e);
	}

	/**
	 * 显示或者隐藏磁吸窗口
	 * @param x X轴坐标
	 * @param y Y轴坐标
	 * @param show 显示或者否
	 */
	private void showMagneticWindow(int x, int y, boolean show) {
		if (show) {
			magicWindow.setLocation(x, y);
			if (!magicWindow.isVisible()) {
				magicWindow.setVisible(true);
			}
		} else {
			if (magicWindow.isVisible()) {
				magicWindow.setVisible(false);
			}
		}
	}

	/**
	 * 隐藏磁吸窗口
	 */
	private void hideMagneticWindow() {
		showMagneticWindow(-1, -1, false);
	}

	/**
	 * 设置桌面实例
	 * @param e
	 */
	public void setDesktop(PlatformDesktop e) {
		desktop = e;
		desktop.addMouseListener(mouseTracker);

		// 建立磁吸式窗口
		if (magicWindow == null && e != null) {
			createMagneticWindow(e);
		}
	}
	
	/**
	 * 返回桌面实例
	 * @return
	 */
	public PlatformDesktop getDesktop() {
		return desktop;
	}
	
	/**
	 * 选中子窗口或者否
	 * @param frame 子窗口
	 * @param selected 选中它或者否
	 */
	private void setSelectSubFrame(JInternalFrame frame, boolean selected) {
		try {
			frame.setSelected(selected);
		} catch (java.beans.PropertyVetoException e) {
		
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
				setSelectSubFrame(frames[i], false);
				return;
			}
		}
	}
	
	/**
	 * 生成分布点
	 */
	private void doPoints(int statusBarHeight) {
		int width = desktop.getWidth();
		int height = desktop.getHeight();
		
		// 没有显示时...
		if (width < 1 || height < 1) {
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			width = d.width;
			height = d.height;

			// 高度减去底栏位置
			if (statusBarHeight < 1) {
				height -= 40; // 40 是假设的状态栏高度
			} else {
				height -= statusBarHeight;
			}
		}
		
		// 规定图标按纽的位置
		points.clear();
		Dimension dim = RayUtil.getDesktopButtonSize(); // 桌面按纽尺寸
		// 间隔是0
		int xGap = 0; int yGap = 0;
		// X坐标起点是10
		for (int x = RayController.LEFT_X, h = 0; x < width; x += xGap, h++) {
			if (x + dim.width > width) {
				break;
			}
			// Y坐标起点是10
			for (int y = RayController.LEFT_Y, v = 0; y < height; y += yGap, v++) {
				if (y + dim.height > height) {
					break;
				}
				Axis axis = new Axis(h, v, x, y);
				points.add(axis);
				
				// 下一个Y坐标
				y = y + dim.height;
			}
			// 下一个X坐标
			x = x + dim.width;
		}
	}
	
	/**
	 * 生成桌面图标
	 * @param text
	 * @param icon
	 * @param size
	 * @return
	 */
	private RayButton createDesktopButton(String text, ImageIcon icon, Dimension size) {
		RayButton cmd = new RayButton();
		cmd.setSize(size);
		cmd.setIcon(icon, PlatformButton.DESKTOP_BUTTON_BRIGHTER, PlatformButton.DESKTOP_BUTTON_DARK);
		cmd.setText(text);

		// 鼠标事件
		cmd.addMouseListener(mouseTracker);
		cmd.addMouseMotionListener(mouseTracker);
		// 键盘事件
		cmd.addKeyListener(new EnterAdapter());

		return cmd;
	}
	
	/**
	 * 获取一个空的坐标位置
	 * @return Point实例
	 */
	private Point fatchPoint() {
		Dimension dim = RayUtil.getDesktopButtonSize();
		// 逐个检查坐标
		for (Axis a : points) {
			int x = a.point.x;
			int y = a.point.y;
			// 检查按纽判断位置被占用
			int count = 0;
			for (RayButton but : buttons) {
				RayContour p = but.getItem().getContour();
				int leftX = p.x;
				int leftY = p.y;
				int rightX = leftX + dim.width;
				int rightY = leftY + dim.height;
				// 坐标位置已经占用
				if ((leftX <= x && x < rightX) && (leftY <= y && y < rightY)) {
					count++;
					break;
				}
			}
			// 没有找到，返回它
			if (count == 0) {
				return new Point(x, y);
			}
		}
		// 返回默认位置
		return new Point(2, 2);
	}
	
	/**
	 * 生成桌面按纽
	 * @param item
	 */
	public boolean doDesktopButton(RayButtonItem item) {
		// 如果桌面被移出了，不接受
		if (item.isShiftout()) {
			return false;
		}

		// 生成按纽
		Dimension dimension = RayUtil.getDesktopButtonSize();
		RayButton cmd = createDesktopButton(item.getTitle(), item.getIcon(), dimension);
		cmd.setToolTipText(item.getToolTip());
		// 保存单元
		cmd.setItem(item);
		
		// 按纽在桌面的位置
		Point point = fatchPoint();
		cmd.setLocation(point); // 在桌面的位置
		// 增加到桌面
		desktop.add(cmd, Integer.MIN_VALUE + 1);
		
		// 保存坐标
		RayContour c = new RayContour(point.x, point.y, dimension.width, dimension.height );
		item.setContour(c);
		
		// 保存一个按纽
		buttons.add(cmd);
		
		return (updated = true);
	}
	
	/**
	 * 判断有这个按纽
	 * @param key
	 * @return
	 */
	public boolean hasDesktopButton(WKey key) {
		// 删除按纽
		for (RayButton but : buttons) {
			RayButtonItem item = but.getItem();
			if (Laxkit.compareTo(item.getKey(), key) == 0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 删除桌面按纽
	 * @param key 
	 * @return 返回真或者假
	 */
	public boolean deleteDesktopButton(WKey key) {
		RayButton cmd = null;

		// 删除按纽
		for (RayButton but : buttons) {
			RayButtonItem item = but.getItem();
			if (Laxkit.compareTo(item.getKey(), key) == 0) {
				cmd = but;
				break;
			}
		}
		if (cmd == null) {
			return false;
		}

		// 找到下标
		int index = desktop.getIndexOf(cmd);
		if (index < 0) {
			return false;
		}
		// 删除桌面和内存记录
		desktop.remove(index);
		boolean success = buttons.remove(cmd);
		// 更新
		if (success) {
			updated = true;
		}
		// 重新绘制桌面
		desktop.repaint();
		// 结果
		return success;
	}
	
	/**
	 * 显示图标按纽
	 * @param statusBarHeight
	 */
	public void showAllButtons(int statusBarHeight) {
		doPoints(statusBarHeight);
		
		// 读取本地的保存...
		RayButtonItem[] temps =	readButtons();
		
		// 显示全部按纽
		for(RayButtonItem item : temps) {
			// 生成按纽
			Dimension dimension = RayUtil.getDesktopButtonSize();
			RayButton cmd = createDesktopButton(item.getTitle(), item.getIcon(), dimension);
			cmd.setToolTipText(item.getToolTip());
			// 保存单元
			cmd.setItem(item);

			RayContour c = item.getContour();
			
			// 如果覆盖，分配新的
			if (isOverride(c.x, c.y, cmd)) {
				Point point = fatchPoint();
				cmd.setLocation(point); // 在桌面的位置
				// 重置位置
				c.setX(point.x); 
				c.setY(point.y);
			} else {
				cmd.setLocation(c.x, c.y); // 在桌面的位置
			}

			//	cmd.setLocation(c.x, c.y); // 在桌面的位置

			// 增加到桌面
			desktop.add(cmd, Integer.MIN_VALUE + 1);
			
			// 保存一个按纽
			buttons.add(cmd);
		}
	}
	
	/**
	 * 读按纽单元
	 * @return RayButtonItem数组
	 */
	private RayButtonItem[] readButtons() {
		File dir = RaySystem.createRuntimeDirectory();
		if (dir == null) {
			return new RayButtonItem[0];
		}
		// 配置目录下的字体文件
		File file = new File(dir, "buttons.conf");
		// 没有这个文件，忽略它
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			return new RayButtonItem[0];
		}
		
		ArrayList<RayButtonItem> array = new ArrayList<RayButtonItem>();
		// 锁定
		super.lockSingle();
		try {
			byte[] b = new byte[(int) file.length()];
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();

			ClassReader reader = new ClassReader(b);
			int size = reader.readInt();
			for (int i = 0; i < size; i++) {
				RayButtonItem item = new RayButtonItem(reader);
				array.add(item);
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		// 输出
		int size = array.size();
		RayButtonItem[] a = new RayButtonItem[size];
		return array.toArray(a);
	}
	
	/**
	 * 写入参数
	 */
	public void writeButtons() {
		File dir = RaySystem.createRuntimeDirectory();
		if (dir == null) {
			return;
		}
		// 配置目录下的字体文件
		File file = new File(dir, "buttons.conf");
		// 没有这个文件，忽略它
		super.lockSingle();
		try {
			int size = buttons.size();
			// 可类化
			ClassWriter writer = new ClassWriter();
			writer.writeInt(size);
			for (int i = 0; i < size; i++) {
				RayButton but = buttons.get(i);
				RayButtonItem item = but.getItem();
				writer.writeObject(item);
			}

			// 写入磁盘
			byte[] b = writer.effuse();
			FileOutputStream out = new FileOutputStream(file);
			out.write(b);
			out.flush();
			out.close();
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}
	
	/**
	 * 刷新，取消焦点
	 */
	public void doRefresh() {
		for (RayButton but : buttons) {
			but.setFocus(false);
		}
	}
	
	/**
	 * 判断是锁定
	 * @return
	 */
	public boolean isLocked() {
		return mouseTracker.locked;
	}
	
	/**
	 * 锁定或者否
	 */
	public void doLock() {
		mouseTracker.locked = !mouseTracker.locked;
	}
	
	class NameSort implements Comparator<RayButton> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(RayButton db1, RayButton db2) {
			RayButtonItem d1 = db1.getItem();
			RayButtonItem d2 = db2.getItem();
			return Laxkit.compareTo(d1.getTitle(), d2.getTitle());
		}
		
	}
	
	class TimeSort implements Comparator<RayButton> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(RayButton db1, RayButton db2) {
			RayButtonItem d1 = db1.getItem();
			RayButtonItem d2 = db2.getItem();
			return Laxkit.compareTo(d1.getCreateTime(), d2.getCreateTime());
		}
	}

	/**
	 * 重新排列
	 */
	private void realignment() {
		Dimension dim = RayUtil.getDesktopButtonSize();

		int index = 0;
		for (RayButton cmd : buttons) {
			if (index >= points.size()) {
				break;
			}
			// 取消焦点
			cmd.setFocus(false);
			Axis a = points.get(index++);
			cmd.setLocation(a.point); // 在桌面新的位置
			// 保存坐标
			RayContour c = new RayContour(a.point.x, a.point.y, dim.width, dim.height);
			cmd.getItem().setContour(c);
		}
	}

	
	/**
	 * 按照名称排列
	 */
	public void doAlignmentByName() {
		Collections.sort(buttons, new NameSort());
		realignment();
	}

	/**
	 * 按照时间排列
	 */
	public void doAlignmentByTime() {
		Collections.sort(buttons, new TimeSort());
		realignment();
	}
	
	/**
	 * 撤销修改状态
	 */
	public void doCancelRename() {
		for (RayButton cmd : buttons) {
			cmd.doCancelRename();
		}
	}

}

///**
//* 判断已经覆盖
//* @param x
//* @param y
//* @param current
//* @return
//*/
//private boolean isOverride(int x, int y, RayButton current) {
//	Dimension dim = RayUtil.getDesktopButtonSize(); // 桌面按纽尺寸
//	for (RayButton but : buttons) {
////		// 忽略当前的
////		if (but == current) {
////			continue;
////		}
//		
//		RayContour p = but.getItem().getContour();
//		int leftX = p.x;
//		int leftY = p.y;
//		int rightX = leftX + dim.width;
//		int rightY = leftY + dim.height;
//		if ((leftX <= x && x < rightX) && (leftY <= y && y < rightY)) {
//			return true;
//		}
//	}
//	return false;
//}


///**
//* 获得位置
//* @return
//*/
//private Point fatchPoint() {
//	Dimension d = RayUtil.getDesktopButtonSize();
//
//	// 逐个判断
//	for (Point point : points) {
//		int count = 0;
//		for (RayButton but : buttons) {
//			Point p = but.getLocation();
//			Rectangle rect = new Rectangle(p.x, p.y, d.width, d.height);
//			// 如果包含，统计值加1
//			if (rect.contains(point)) {
//				count++;
//			}
//		}
//		// 没有重叠的...
//		if (count == 0) {
//			return point;
//		}
//	}
//
//	// 返回默认位置
//	return new Point(2, 2);
//}

///**
//* 获得位置
//* @return
//*/
//private Point fatchPoint() {
//	Dimension d = RayUtil.getDesktopButtonSize();
//
//	// 逐个判断
//	for (Axis a : points) {
//		int count = 0;
//		for (RayButton but : buttons) {
//			Point p = but.getLocation();
//			Rectangle rect = new Rectangle(p.x, p.y, d.width, d.height);
//			// 如果包含，统计值加1
//			if (rect.contains(a.point)) {
//				count++;
//			}
//		}
//		// 没有重叠的...
//		if (count == 0) {
//			return a.point;
//		}
//	}
//
//	// 返回默认位置
//	return new Point(2, 2);
//}


///**
//* 重新排列
//*/
//private void realignment() {
//	Dimension dim = RayUtil.getDesktopButtonSize();
//
//	int index = 0;
//	for (RayButton cmd : buttons) {
//		if (index >= points.size()) {
//			break;
//		}
//		// 取消焦点
//		cmd.setFocus(false);
//		Point point = points.get(index++);
//		cmd.setLocation(point); // 在桌面新的位置
//		// 保存坐标
//		RayContour c = new RayContour(point.x, point.y, dim.width, dim.height);
//		cmd.getItem().setContour(c);
//	}
//}

