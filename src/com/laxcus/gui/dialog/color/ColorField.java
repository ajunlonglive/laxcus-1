/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.color;

import java.awt.*;

import javax.swing.*;

import com.laxcus.util.color.*;

/**
 * 颜色框
 * 
 * @author scott.liang
 * @version 1.0 8/27/2021
 * @since laxcus 1.0
 */
public class ColorField extends JButton { // JLabel { // JTextField {

	private static final long serialVersionUID = 9211174399979532186L;

	/** 选中的颜色 **/
	private Color selectColor;

	/**
	 * 构造默认的颜色框
	 */
	public ColorField() {
		super();
	}

	/**
	 * 设置选中的颜色
	 * @param c
	 */
	public void setSelectColor(Color c) {
		selectColor = c;
	}

	/**
	 * 返回选中的颜色
	 * @return
	 */
	public Color getSelectColor(){
		return selectColor;
	}

	//	/**
	//	 * 判断是NIMBUS界面
	//	 * 
	//	 * @return 返回真或者假
	//	 */
	//	private boolean isNimbus() {
	//		LookAndFeel laf = UIManager.getLookAndFeel();
	//		if (laf == null) {
	//			return false;
	//		}
	//		String name = laf.getID();
	//		return name != null && name.indexOf("Nimbus") > -1;
	//	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		boolean select = (selectColor != null);
		Color color = (select ? selectColor : Color.WHITE);
		// 背景色
		Color back = g.getColor();

		int width = getWidth();
		int height = getHeight();

		// 背景颜色
		g.setColor(color);
		g.fillRect(0, 0, width - 1, height - 1);

		// 判断在焦点状态
		boolean focus = hasFocus(); 
		if (focus && select) {
			int x = width / 2;
			int y = height / 2;

			int leftXEnd = x - 3;
			int leftXStart = leftXEnd - 4;

			int rightXStart = x + 3;
			int rightXEnd = rightXStart + 4;

			int upYStart = y - 3;
			int upYEnd = upYStart - 4;

			int downYStart = y + 3;
			int downYEnd = downYStart + 4;

			ESL esl = new RGB(color).toESL();
			double l =	esl.getL(); // 亮度
			Color cursor = (l < 120 ? Color.white : Color.black);

			// 十字瞄准线
			g.setColor(cursor); 
			g.drawLine(leftXStart, y, leftXEnd, y);
			g.drawLine(rightXStart, y, rightXEnd, y);
			g.drawLine(x, upYStart, x, upYEnd);
			g.drawLine(x, downYStart, x, downYEnd);
		}

		g.setColor(back);
	}
	
	

	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	//	 */
	//	@Override
	//	protected void paintComponent(Graphics g) {
	//		// 颜色有效，绘制它
	//		if (selectColor != null) {
	//			// 背景色
	//			Color back = g.getColor();
	//
	//			int width = getWidth();
	//			int height = getHeight();
	//
	//			// 选中颜色
	//			g.setColor(selectColor);
	////			g.fillRect(0, 0, width - 1, height - 1);
	//			g.fillRect(0, 0, width, height);
	//			
	//			boolean focus = hasFocus();
	//			if (focus) {
	//				int x = width / 2;
	//				int y = height / 2;
	//				
	//				int leftXEnd = x - 3;
	//				int leftXStart = leftXEnd - 4;
	//
	//				int rightXStart = x + 3;
	//				int rightXEnd = rightXStart + 4;
	//				
	//				int upYStart = y - 3;
	//				int upYEnd = upYStart - 4;
	//				
	//				int downYStart = y + 3;
	//				int downYEnd = downYStart + 4;
	//
	//				g.setColor(Color.WHITE);
	//				g.drawLine(leftXStart, y, leftXEnd, y);
	//				g.drawLine(rightXStart, y, rightXEnd, y);
	//				
	//				g.drawLine(x, upYStart, x, upYEnd);
	//				g.drawLine(x, downYStart, x, downYEnd);
	//
	////				g.drawLine(x, y, x + 6, y);
	//			}
	//
	//			g.setColor(back);
	//		} else {
	//			setBackground(Color.WHITE);
	//			super.paintComponent(g);
	//		}
	//	}



}
