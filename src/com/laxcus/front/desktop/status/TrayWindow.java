/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.status;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.tray.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 托盘面板
 * 
 * @author scott.liang
 * @version 1.0 2/25/2022
 * @since laxcus 1.0
 */
class TrayWindow extends JPopupMenu {
	
	private static final long serialVersionUID = -9154270185215510480L;

	/** 一行的图标数量 **/
	static final int ROW_MAX_ELEMENTS = 18;
	static final int ROW_MIN_ELEMENTS = 4;
	
	/** 绘制边框 **/
	private boolean paintBorder;

	/** 圆弧 **/
	private boolean round;

	/** 圆弧角度 **/
	private int roundARC;
	
	/**
	 * 
	 */
	public TrayWindow() {
		super();
		init();
	}

	/**
	 * 绘制边框
	 * @param b
	 */
	public void setPaintBorder(boolean b) {
		paintBorder = b;
	}

	/**
	 * 判断绘制边框
	 * @return
	 */
	public boolean isPaintBorder() {
		return paintBorder;
	}

	/**
	 * 初始化
	 */
	private void init() {
		String value = UIManager.getString("StatusBar.FrameButton.SnapshotWindow.Round");
		round = (value != null && value.equalsIgnoreCase("YES"));
		if (round) {
			value = UIManager.getString("StatusBar.FrameButton.SnapshotWindow.RoundARC");
			roundARC = ConfigParser.splitInteger(value, 20);
		}

		// 窗口尺寸
		value = UIManager.getString("StatusBar.FrameButton.SnapshotWindow.Width");
		int width = ConfigParser.splitInteger(value, 280);
		value = UIManager.getString("StatusBar.FrameButton.SnapshotWindow.Height");
		int height = ConfigParser.splitInteger(value, 173);

		setPopupSize(new Dimension(width, height));
		// 透明
		setOpaque(true);
		
		// 默认是绘制边框
		paintBorder = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintBorder(java.awt.Graphics)
	 */
	@Override
	protected void paintBorder(Graphics g) {
		// 如果不绘制，退出
		if (!paintBorder) {
			return;
		}

		Color old = g.getColor();
		int width = getWidth();
		int height = getHeight();

		Color c = Color.DARK_GRAY;
		// 不是灰色，选择弹出菜单的背景色调整加深处理
		if (!Skins.isGraySkin()) {
			c = UIManager.getColor("PopupMenu.background");
			if (c == null) {
				c = getBackground();
			}
			ESL esl = new ESL(c);
			esl.darker(22); // 调暗
			c = esl.toColor();
		}

		// 设置颜色
		g.setColor(c);

		// 圆角
		if (round) {
			int thickness = 1;
			int x = 0;
			int y = 0;
			for (int i = 0; i < thickness; i++) {
				g.drawRoundRect(x + i, y + i, width - (i * 2) - 1, height
						- (i * 2) - 1, roundARC, roundARC);
			}
		}
		// 矩形
		else {
			g.drawRect(0, 0, width - 1, height - 1);
		}

		// 还原颜色
		g.setColor(old);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPopupMenu#menuSelectionChanged(boolean)
	 */
	@Override
	public void menuSelectionChanged(boolean isIncluded){
		// 空方法，在这里截胡，不让它调用父类生效
		// 这个很重要！很关键！
	}

	class ActionAdapter implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			TrayButton button = (TrayButton) e.getSource();
			Tray tray = button.getTray();
			ActionListener[] as  = tray.getActionListeners();
			int size = (as != null ? as.length: 0);
			for (int i = 0; i < size; i++) {
				as[i].actionPerformed(e);
			}
		}
	}
	
	class ButtonMouseAdapter extends MouseAdapter {
		
		public void mouseClicked(MouseEvent e) {
			TrayButton button = (TrayButton) e.getSource();
			Tray tray = button.getTray();
			MouseListener[] as = tray.getMouseListeners();
			int size = (as != null ? as.length: 0);
			for(int i =0; i < size; i++) {
				as[i].mouseClicked(e);
			}
		}

		public void mousePressed(MouseEvent e) {
			TrayButton button = (TrayButton) e.getSource();
			Tray tray = button.getTray();
			MouseListener[] as = tray.getMouseListeners();
			int size = (as != null ? as.length: 0);
			for(int i =0; i < size; i++) {
				as[i].mousePressed(e);
			}
		}

		public void mouseReleased(MouseEvent e) {
			TrayButton button = (TrayButton) e.getSource();
			Tray tray = button.getTray();
			MouseListener[] as = tray.getMouseListeners();
			int size = (as != null ? as.length: 0);
			for(int i =0; i < size; i++) {
				as[i].mouseReleased(e);
			}
		}
	}

	/**
	 * 
	 * @param invoker
	 * @param trays
	 */
	public void show(Component invoker, Tray[] trays) {
		int size = (trays != null ? trays.length : 0);
		if (size < 1) {
			return;
		}
		
		int maxRowElements = size;
		// 小于规定值或者大于规定值时
		if (size <= ROW_MIN_ELEMENTS) {
			maxRowElements = ROW_MIN_ELEMENTS;
		} else if (size >= ROW_MAX_ELEMENTS) {
			maxRowElements = ROW_MAX_ELEMENTS;
		}
		
		// 计算行列
		int columns = (size >= maxRowElements ? maxRowElements : size);
		int rows = (size / columns) + ((size % columns != 0) ? 1 : 0);
		
		TrayButton[] buttons = new TrayButton[size];
		for (int index = 0; index < trays.length; index++) {
			Tray tray = trays[index];
			buttons[index] = new TrayButton(index, tray);
			buttons[index].addMouseListener(new ButtonMouseAdapter());
			buttons[index].addMouseMotionListener(new ButtonMouseAdapter());
			buttons[index].addActionListener(new ActionAdapter());
		}
		// 把按纽放入面板
		for (int i = 0; i < buttons.length; i++) {
			add(buttons[i]);
		}
		// 填加剩余的组件
		int all = rows * maxRowElements;
		for (int i = buttons.length; i < all; i++) {
			add(new JLabel());
		}
		
		// 布局
		setLayout(new GridLayout(rows, columns, 0, 0));
		setBorder(new EmptyBorder(6,6,6,6));

		// 默认尺寸
		Dimension d = buttons[0].getPreferredSize();
		int gridWidth = (d.width * maxRowElements);
		int gridHeight = (d.height * rows);
		d = new Dimension(gridWidth, gridHeight);
		setPreferredSize(d);
		setPopupSize(d);
		setMaximumSize(d);
		setMinimumSize(d);
		
		// 无效重制
		invalidate();
		
		show(invoker);
	}
	
	/**
	 * 显示
	 * @param invoker
	 */
	private void show(Component invoker) {
		setInvoker(invoker);

		Point p = invoker.getLocationOnScreen();
		Dimension d1 = invoker.getSize();
		// 弹出菜单尺寸
		Dimension d = getPreferredSize();
		// Y轴显示位置，向上
		int newX = p.x - (d.width - d1.width);
		int newY = p.y - (d.height + 4); // 间隔四个像素，这是StatusBar.top像素值

		// 坐标
		if (newX < 0) newX = 0;
		if (newY < 0) newY = 0;

		// 新位置
		setLocation(newX, newY);
		// 可视...
		setVisible(true);
	}

}


///**
// * 删除一个托盘按纽
// * @param tray
// */
//public void remove(Tray tray) {
//	int size = getComponentCount();
//	for (int i = 0; i < size; i++) {
//		Component element = getComponent(i);
//		if (element.getClass() != TrayButton.class) {
//			continue;
//		}
//		TrayButton but = (TrayButton) element;
//		Tray that = but.getTray();
//		if (tray == that) {
//			remove(element); // 删除
//			add(new JLabel()); // 增加新的补充
//			validate();
//			break;
//		}
//	}
//}

///*
// * (non-Javadoc)
// * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
// */
//@Override
//public void show(Component invoker, int x, int y) {
//	System.out.printf("show %d - %d\n", x, y);
//
////	super.show(invoker, x, y);
//	
//	setInvoker(invoker);
//	Frame newFrame = getFrame(invoker);
//	if (newFrame !=  frame) {
//		// Use the invoker's frame so that events 
//		// are propagated properly
//		if (newFrame!=null) {
//			frame = newFrame;
//			//			if(popup != null) {
//			//				setVisible(false);
//			//			}
//			setVisible(false);
//		}
//	}
//	
////	setSize(280, 198);
//	
//	Point p = invoker.getLocationOnScreen();
//	
//	Dimension d1 = invoker.getSize(); // invoker.getSize();
////	int newX = p.x  - 10;
////	int newY = p.y - 10;
//	
//	// 弹出菜单尺寸
//	Dimension d = getPreferredSize();
//	int width = d.width;
//	int height = d.height;
//	// Y轴显示位置，向上
//	p.y -= (height + 4); // 间隔四个像素
//	p.x -= (width - d1.width);
//	
//	// 新位置
//	setLocation(p.x, p.y);
//	
//	setVisible(true);
//
////	// 调整位置
////	if (invoker != null) {
////		Point screen = invoker.getLocationOnScreen();
////		Dimension d1 = invoker.getSize();
////		Dimension d2 = getSize();
////
////		// To avoid integer overflow
////		int newX = screen.x + x;
////		int newY = screen.y + y;
////		// 上移位置
////		newY -= (d1.height + 4);
////		// 左移
////		newX -= (x + d2.width);
////
////		if (newX > Integer.MAX_VALUE) newX = Integer.MAX_VALUE;
////		if (newX < Integer.MIN_VALUE) newX = Integer.MIN_VALUE;
////		if (newY > Integer.MAX_VALUE) newY = Integer.MAX_VALUE;
////		if (newY < Integer.MIN_VALUE) newY = Integer.MIN_VALUE;
////
////		// 新位置
////		setLocation(newX, newY);
////
////		//			System.out.printf("invoker is %s, mouse [%d %d], new [%d %d]\n",
////		//					(invoker != null ? invoker.getClass().getName() : "Not!"),
////		//					x, y, newX, newY);
////
////	}
//	
//} 



// private static Frame getFrame(Component c) {
//        Component w = c;
//
//        while(!(w instanceof Frame) && (w!=null)) {
//            w = w.getParent();
//        }
//        return (Frame)w;
//    }

//public void show(Component invoker, int x, int y) {
//	//			if (DEBUG) {
//	//			    System.out.println("in JPopupMenu.show " );
//	//			}
//	
//	System.out.printf("invoker is %s, x:%d, y:%d\n", (invoker != null? invoker.getClass().getName() :"Not!"),x,y);
//	super.show(invoker, x, y);
//	
////	setInvoker(invoker);
////	Frame newFrame = getFrame(invoker);
////	if (newFrame !=  frame) {
////		// Use the invoker's frame so that events 
////		// are propagated properly
////		if (newFrame!=null) {
////			frame = newFrame;
////			if(popup != null) {
////				setVisible(false);
////			}
////		}
////	}
////	
////	Point invokerOrigin;
////	if (invoker != null) {
////		invokerOrigin = invoker.getLocationOnScreen();
////
////		// To avoid integer overflow
////		long lx, ly;
////		lx = ((long) invokerOrigin.x) +
////		((long) x);
////		ly = ((long) invokerOrigin.y) +
////		((long) y);
////		if(lx > Integer.MAX_VALUE) lx = Integer.MAX_VALUE;
////		if(lx < Integer.MIN_VALUE) lx = Integer.MIN_VALUE;
////		if(ly > Integer.MAX_VALUE) ly = Integer.MAX_VALUE;
////		if(ly < Integer.MIN_VALUE) ly = Integer.MIN_VALUE;
////
////		setLocation((int) lx, (int) ly);
////	} else {
////		setLocation(x, y);
////	}
////	setVisible(true);       
//}

//	setInvoker(invoker);
//	Frame newFrame = getFrame(invoker);
//	if (newFrame !=  frame) {
//		// Use the invoker's frame so that events 
//		// are propagated properly
//		if (newFrame!=null) {
//			frame = newFrame;
//			if(popup != null) {
//				setVisible(false);
//			}
//		}
//	}
//	
//	Point invokerOrigin;
//	if (invoker != null) {
//		invokerOrigin = invoker.getLocationOnScreen();
//
//		// To avoid integer overflow
//		long lx, ly;
//		lx = ((long) invokerOrigin.x) +
//		((long) x);
//		ly = ((long) invokerOrigin.y) +
//		((long) y);
//		if(lx > Integer.MAX_VALUE) lx = Integer.MAX_VALUE;
//		if(lx < Integer.MIN_VALUE) lx = Integer.MIN_VALUE;
//		if(ly > Integer.MAX_VALUE) ly = Integer.MAX_VALUE;
//		if(ly < Integer.MIN_VALUE) ly = Integer.MIN_VALUE;
//
//		setLocation((int) lx, (int) ly);
//	} else {
//		setLocation(x, y);
//	}
//	setVisible(true);       