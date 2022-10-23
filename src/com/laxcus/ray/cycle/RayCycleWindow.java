/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.cycle;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.frame.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 循环窗口
 * 用键盘切换，最后弹出应用
 * 
 * @author scott.liang
 * @version 1.0 2/11/2022
 * @since laxcus 1.0
 */
public class RayCycleWindow extends JPopupMenu {

	private static final long serialVersionUID = -1587889632306042295L;

	static final int V_GAP = 16;
	static final int H_GAP = 20;
	static final int ROW_MAX_ELEMENTS = 12;
	static final int ROW_MIN_ELEMENTS = 8;

	/** 绘制边框 **/
	private boolean paintBorder;

	/** 显示按纽面板 **/
	private JPanel buttonPane;
	
	private JPanel titlePane;
	
	/** 标题 **/
	private CycleLabel title;
	
	/** 按纽数组 **/
	private CycleButton[] buttons;
	
	/** 被选中的按纽 **/
	private CycleButton selectButton;

	/**
	 * 构造循环窗口
	 * @param invoker 关联调用者
	 */
	public RayCycleWindow(Component invoker) {
		super();
		init();
		setInvoker(invoker);
	}

	/**
	 * 初始化
	 */
	private void init() {
		// 窗口
		buttonPane = new JPanel();
		buttonPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		title = new CycleLabel();
		title.setPreferredSize(new Dimension(20, 24));
		title.setBorder(new EmptyBorder(0, 0, 0, 0));

		// 上方面板
		titlePane = new JPanel();
		titlePane.setLayout(new BorderLayout(0, 0));
		titlePane.add(title, BorderLayout.CENTER);
		titlePane.add(new JSeparator(), BorderLayout.SOUTH);

		// 设置窗口尺寸
		setLayout(new BorderLayout(0, 8));
		setBorder(new EmptyBorder(V_GAP / 2 - 3, H_GAP / 2, V_GAP / 2 + 3, H_GAP / 2));
		add(titlePane, BorderLayout.NORTH);
		add(buttonPane, BorderLayout.CENTER);

		// 窗口默认尺寸
		String value = UIManager.getString("CycleWindow.Width");
		int width = ConfigParser.splitInteger(value, 220);
		value = UIManager.getString("CycleWindow.Height");
		int height = ConfigParser.splitInteger(value, 98);

		// 弹出窗口尺寸
		setPopupSize(new Dimension(width, height));
		// 不透明是true
		setOpaque(true);
		
		// 默认是绘制边框
		paintBorder = true;
	}
	
	
	/**
	 * 显示运行中的窗口
	 * @param a
	 */
	public void showRunFrames(LightFrame[] a) {
		int size = (a != null ? a.length : 0);
		if (size < 1) {
			return;
		}
		
//		// 如果小于10，每行8个，否则是是10
//		int maxRowElements = (size <= 8 ? 8 : 10);
		
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
		
		// 生成按纽数组
		buttons = new CycleButton[size];
		for (int i = 0; i < size; i++) {
			LightFrame frame = a[i];
			buttons[i] = new CycleButton(i, frame);
		}
		
		// 窗口布局
		buttonPane.setLayout(new GridLayout(rows, columns, 0,0));
		// 第一个为选中
		selectButton = buttons[0];
		selectButton.setSelected(true);
		for (int i = 0; i < buttons.length; i++) {
			buttonPane.add(buttons[i]);
		}
		
		// 填加剩余的尺寸
		int all = rows  * maxRowElements;
		for(int i = buttons.length; i < all; i++) {
			buttonPane.add(new JLabel());
		}
		
//		System.out.printf("columns:%d, rows:%d, buttons:%d, all:%d\n", columns, rows, buttons.length, all);
		
		// 默认尺寸
		Dimension d = selectButton.getPreferredSize();
		int gridWidth = (d.width * maxRowElements);
		int gridHeight = (d.height * rows);
		d = new Dimension(gridWidth, gridHeight);
		buttonPane.setPreferredSize(d);
		buttonPane.setMaximumSize(d);
		buttonPane.setMinimumSize(d);
		
		// 重制尺寸
		int cwidth = gridWidth + H_GAP; // 20是裕度
		int cheight = titlePane.getPreferredSize().height + 8 + gridHeight + V_GAP;

		// 最小尺寸
		d = getPreferredSize();
		int width = (d.width > cwidth ? d.width : cwidth);
		int height = (d.height > cheight ? d.height : cheight);
		// 定义尺寸
		setPopupSize(width, height);
		invalidate();

		// 选中第一个是组件
		selectButton.repaint();
	}
	
	/**
	 * 取消选中状态的子窗口
	 */
	private void cancelSelectFrames() {
		int size = buttons.length;
		for (int i = 0; i < size; i++) {
			LightFrame frame = buttons[i].getLightFrame();
			if (frame.isSelected()) {
				setSelectFrame(frame, false);
			}
		}
	}
	
	/**
	 * 选中子窗口或者否
	 * @param frame 子窗口
	 * @param selected 选中它或者否
	 */
	private void setSelectFrame(JInternalFrame frame, boolean selected) {
		try {
			// 取得焦点
			if (selected) {
				// 不可视的时候，显示它
				if (!frame.isVisible()) {
					frame.setVisible(true);
				}
				frame.setSelected(true);
				frame.requestFocusInWindow();
			} else {
				frame.setSelected(false);
			}
		} catch (java.beans.PropertyVetoException e) {
			Logger.error(e);
		}
	}
	
	/**
	 * 取出造中的窗口
	 */
	public void showSelectFrame() {
		if (selectButton != null) {
			// 撤销选中的窗口
			cancelSelectFrames();
			// 设置新的选中窗口
			LightFrame frame = selectButton.getLightFrame();
			setSelectFrame(frame, true);
		}
	}
	
	/**
	 * 切换到下一个
	 */
	public void nextSelectFrame() {
		if (selectButton != null) {
			// 如果只有一个时，忽略
			if (buttons.length == 1) {
				return;
			}
			// 取消选中的
			int index = selectButton.getIndex();
			selectButton.setSelected(false);
			selectButton.repaint();

			// 下一个
			index = (index + 1 >= buttons.length ? 0 : index + 1);
			selectButton = buttons[index];
			selectButton.setSelected(true);
			selectButton.repaint();
			showTitle();
		}
	}
	
	/**
	 * 显示标题
	 */
	private void showTitle() {
		if(selectButton != null) {
			title.setText(selectButton.getTitle());
		} else {
			title.setText("");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPopupMenu#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean show) {
		if (show) {
			Dimension d = getPreferredSize();
			Dimension d2 = getInvoker().getSize();
			int x = (d2.width - d.width) / 2;
			int y = (d2.height - d.height) / 2; 
			y = y - (int) ((double) y * 0.382); // 上移
			if (x < 0) x = 0;
			if (y < 0) y = 0;

			// 转换位置
			Point p = SwingUtilities.convertPoint(getInvoker(), x, y, this);
			setLocation(p);
		}
		// 显示或者否
		super.setVisible(show);

		// 显示标题
		if (show) {
			showTitle();
		}
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

		Color c = Color.DARK_GRAY; // Color.DARK_GRAY;

		// 不是灰色，选择弹出菜单的背景色调整加深处理
		if (!Skins.isGraySkin()) {
			c = UIManager.getColor("PopupMenu.background");
			if (c == null) {
				c = getBackground();
			}
			int dark = 38;
			ESL esl = new ESL(c);
			esl.darker(dark);
			c = esl.toColor();
		}

		// 设置颜色
		g.setColor(c);

//		// 圆角
//		if (round) {
//			int thickness = 1;
//			int x = 0;
//			int y = 0;
//			for (int i = 0; i < thickness; i++) {
//				g.drawRoundRect(x + i, y + i, width - (i * 2) - 1, height
//						- (i * 2) - 1, roundARC, roundARC);
//			}
//		}
//		// 矩形
//		else {
//			g.drawRect(0, 0, width - 1, height - 1);
//		}

		g.drawRect(0, 0, width - 1, height - 1);
		
		// 还原颜色
		g.setColor(old);
	}
}
