/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.status;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.laxcus.gui.component.*;
import com.laxcus.util.color.*;

/**
 * 启动按纽
 * 
 * @author scott.liang
 * @version 1.0 6/2/2021
 * @since laxcus 1.0
 */
class StartButton extends CraftButton {

	private static final long serialVersionUID = 5481837865596354833L;

	private boolean press;
	
	/**
	 * 鼠标点击事件
	 *
	 * @author scott.liang
	 * @version 1.0 6/2/2021
	 * @since laxcus 1.0
	 */
	class StartButtonAdapter extends MouseAdapter {

		public StartButtonAdapter() {
			super();
		}

		public void mousePressed(MouseEvent e) {
			press = true;
			// super.mousePressed(e);
		}

		public void mouseReleased(MouseEvent e) {
			press = false;
			// super.mouseReleased(e);
		}
	}
	 
	/**
	 * 构造启动按纽
	 */
	public StartButton() {
		super();
		press = false;
		
		// 增加鼠标点击事件
		addMouseListener(new StartButtonAdapter());
	}
	
	/**
	 * 设置图标
	 * @param icon
	 * @param light 高亮增幅
	 */
	public void setIcon(ImageIcon icon, int light) {
		super.setIcon(icon);
		
		// ESL的L亮度增加
		if (light > 0) {
			ImageIcon image = brighter(icon, light);
			if (image != null) {
				super.setPressedIcon(image);
				super.setSelectedIcon(image);
				super.setRolloverIcon(image);
				super.setRolloverSelectedIcon(image);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// 判断是按下
		if (press) {
			Color back = g.getColor();
			
			// 背景色
			Color c = getBackground();
			RGB rgb = new RGB(c.getRed(), c.getGreen(), c.getBlue());
			ESL esl = ESLConverter.convert(rgb);
			
			// 反向切换颜色. <=80是暗颜色，选用白色形成对比；否则大于80是亮色，选用黑色形成对比
			Color color = (esl.getL() <= 80 ? Color.WHITE : Color.BLACK);

			int x1 = 0;
			int y1 = 0;
			int x2 = x1 + getWidth() - 1;
			int y2 = y1 + getHeight() - 1;

			// 虚线
			g.setColor(color); 
			for (int x = 0; x < x2; x += 2) {
				g.drawLine(x, y1, x, y1); // 上横线
				g.drawLine(x, y2, x, y2); // 下横线
			}
			for (int y = 0; y < y2; y += 2) {
				g.drawLine(x1, y, x1, y); // 左侧线
				g.drawLine(x2, y, x2, y); // 右侧线
			}
			
			// 恢复原来的颜色
			g.setColor(back);
		}
	}

}