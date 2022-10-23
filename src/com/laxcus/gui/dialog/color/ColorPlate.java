/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.color;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.color.*;

/**
 * 颜色面板
 * 
 * 有一个240 * 241 的范围值
 * 
 * @author scott.liang
 * @version 1.0 8/31/2021
 * @since laxcus 1.0
 */
public class ColorPlate extends JButton {
	
	private static final long serialVersionUID = 7622286653239165484L;

	/** 范围 **/
	private Dimension dim = new Dimension(240, 241);

	/** 颜色图片 **/
	private BufferedImage image;
	
	/** 选中的位置 **/
	private int selectX, selectY;
	
	/** 设置选中颜色 **/
	private Color focusColor;
	
	class MouseTracker extends MouseAdapter {

		public void mouseClicked(MouseEvent e) {
			selectX = e.getX();
			selectY = e.getY();
		}
	}
	
	/**
	 * 构造默认的颜色面板
	 */
	public ColorPlate() {
		super();
		init();
	}
	
	public void setFocusColor(Color c) {
		focusColor = c;
	}
	
	/**
	 * 初始化
	 */
	private void init() {
		selectX = selectY = -1;

		createImage();
		setPreferredSize(dim);
		setMaximumSize(dim);
		setMinimumSize(dim);

		setContentAreaFilled(false); // 平面
		setBorderPainted(false); // 不绘制边框
		// 鼠标事件 
		addMouseListener(new MouseTracker());

		setBorder(new EmptyBorder(0, 0, 0, 0));
	}
	
	/**
	 * 生成“FLAG”图像
	 * @return 返回对象实例
	 */
	private void createImage() {
		// 创建缓冲图片类变量
		image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		// 获得缓冲图片的2D绘图类变量
		Graphics2D g = image.createGraphics();
		
		int E = 0; 
		// 绘制图像. 亮度默认是120，在0-240的中间值
		for (int x = 0; x < dim.width ; x++) {
			int S = dim.height - 1;
			for (int y = 0; y < dim.height; y++) {
				ESL esl = new ESL((double) E, (double) S, (double) 120);
				g.setColor(esl.toColor()); 
				g.drawLine(x, y, x, y);
				S--;
			}
			E++;
		}

		// 销毁
		g.dispose();
	}
	
	/**
	 * 返回选中的颜色
	 * @return
	 */
	public Color getSelectColor() {
		if (!isInside()) {
			return Color.BLACK;
		}

		int E = selectX;
		int S = (dim.height - 1) - selectY;
		ESL esl = new ESL((double) E, (double) S, (double) 120);
		return esl.toColor();
	}
	
	/**
	 * 设置十字光标的颜色
	 * @return 颜色对象
	 */
	private Color getCursorColor() {
		int E = selectX;
		int S = (dim.height - 1) - selectY;
		return ((E >= 120 || S <= 120) ? Color.WHITE : Color.BLACK);
	}

	/**
	 * 判断在范围内
	 * @return
	 */
	private final boolean isInside() {
		return (selectX >= 0 && selectX < dim.width && selectY >= 0 && selectY < dim.height);
	}
	
	/**
	 * 绘制光标
	 * @param g
	 * @param x
	 * @param y
	 */
	private void printCursor(Graphics g, int x, int y) {
		Color back = g.getColor();
		
		int leftXEnd = x - 3;
		int leftXStart = leftXEnd - 7;

		int rightXStart = x + 3;
		int rightXEnd = rightXStart + 7;

		int upYStart = y - 3;
		int upYEnd = upYStart - 7;

		int downYStart = y + 3;
		int downYEnd = downYStart + 7;
		
		// 十字瞄准线
		g.setColor(getCursorColor()); 
		
		g.drawLine(leftXStart, y - 1, leftXEnd, y - 1);
		g.drawLine(leftXStart, y, leftXEnd, y);
		g.drawLine(leftXStart, y + 1, leftXEnd, y + 1);
		
		
		g.drawLine(rightXStart, y-1, rightXEnd, y-1);
		g.drawLine(rightXStart, y, rightXEnd, y);
		g.drawLine(rightXStart, y+1, rightXEnd, y+1);
		
		g.drawLine(x-1, upYStart, x-1, upYEnd);
		g.drawLine(x, upYStart, x, upYEnd);
		g.drawLine(x+1, upYStart, x+1, upYEnd);
		
		g.drawLine(x - 1, downYStart, x - 1, downYEnd);
		g.drawLine(x, downYStart, x, downYEnd);
		g.drawLine(x + 1, downYStart, x + 1, downYEnd);
		
		// 还原
		g.setColor(back);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, null);
		
		boolean focus = hasFocus();
		if (focus && isInside()) {
//			Color back = g.getColor();

//			Color color = Color.WHITE;
//			g.setColor(color);

			int x = selectX; // width / 2;
			int y = selectY; // height / 2;
			
			printCursor(g, x, y);

//			int leftXEnd = x - 3;
//			int leftXStart = leftXEnd - 4;
//
//			int rightXStart = x + 3;
//			int rightXEnd = rightXStart + 4;
//
//			int upYStart = y - 3;
//			int upYEnd = upYStart - 4;
//
//			int downYStart = y + 3;
//			int downYEnd = downYStart + 4;

//			// 十字瞄准线
//			g.setColor(color); 
//			g.drawLine(leftXStart, y, leftXEnd, y);
//			g.drawLine(rightXStart, y, rightXEnd, y);
//			g.drawLine(x, upYStart, x, upYEnd);
//			g.drawLine(x, downYStart, x, downYEnd);
			
//			int leftXEnd = x - 3;
//			int leftXStart = leftXEnd - 7;
//
//			int rightXStart = x + 3;
//			int rightXEnd = rightXStart + 7;
//
//			int upYStart = y - 3;
//			int upYEnd = upYStart - 7;
//
//			int downYStart = y + 3;
//			int downYEnd = downYStart + 7;
//			
//			// 十字瞄准线
//			g.setColor(getCursorColor()); 
//			
//			g.drawLine(leftXStart, y - 1, leftXEnd, y - 1);
//			g.drawLine(leftXStart, y, leftXEnd, y);
//			g.drawLine(leftXStart, y + 1, leftXEnd, y + 1);
//			
//			
//			g.drawLine(rightXStart, y-1, rightXEnd, y-1);
//			g.drawLine(rightXStart, y, rightXEnd, y);
//			g.drawLine(rightXStart, y+1, rightXEnd, y+1);
//			
//			g.drawLine(x-1, upYStart, x-1, upYEnd);
//			g.drawLine(x, upYStart, x, upYEnd);
//			g.drawLine(x+1, upYStart, x+1, upYEnd);
//			
//			g.drawLine(x - 1, downYStart, x - 1, downYEnd);
//			g.drawLine(x, downYStart, x, downYEnd);
//			g.drawLine(x + 1, downYStart, x + 1, downYEnd);
			
//			g.setColor(back);
		} else if(focusColor != null) {
			
			ESL esl = new RGB(focusColor).toESL();
			
			int x = (int) Math.round(esl.getH()); // width / 2;
			int y = (int) Math.round(esl.getS()); // height / 2;

			printCursor(g, x, y);
			
//			this.focusColor = null;
		}
		
		
//		boolean select = (selectColor != null);
//		Color color = (select ? selectColor : Color.WHITE);
//		// 背景色
//		Color back = g.getColor();
//
//		int width = getWidth();
//		int height = getHeight();
//
//		// 背景颜色
//		g.setColor(color);
//		g.fillRect(0, 0, width, height);
//
//		boolean focus = hasFocus(); // ||isFocusOwner();
//		if (focus && select) {
//			int x = width / 2;
//			int y = height / 2;
//
//			int leftXEnd = x - 3;
//			int leftXStart = leftXEnd - 4;
//
//			int rightXStart = x + 3;
//			int rightXEnd = rightXStart + 4;
//
//			int upYStart = y - 3;
//			int upYEnd = upYStart - 4;
//
//			int downYStart = y + 3;
//			int downYEnd = downYStart + 4;
//
//			ESL esl = new RGB(color).toESL();
//			double l =	esl.getL(); // 亮度
//			Color cursor = (l <= 100 ? Color.white : Color.black);
//
//			// 十字瞄准线
//			g.setColor(cursor); 
//			g.drawLine(leftXStart, y, leftXEnd, y);
//			g.drawLine(rightXStart, y, rightXEnd, y);
//			g.drawLine(x, upYStart, x, upYEnd);
//			g.drawLine(x, downYStart, x, downYEnd);
//		}
//
//		g.setColor(back);
	}
}
