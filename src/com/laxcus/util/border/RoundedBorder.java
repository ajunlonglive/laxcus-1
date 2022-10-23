/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.border;

import java.awt.*;

import javax.swing.border.*;

import com.laxcus.util.*;
import com.laxcus.util.color.*;

/**
 * 圆孤形的边框
 * 
 * @author scott.liang
 * @version 1.0 10/7/2021
 * @since laxcus 1.0
 */
public class RoundedBorder extends AbstractBorder {

	private static final long serialVersionUID = 7866639667395142918L;

	/** 颜色 **/
	protected Color lineColor;

	/** 厚度，最小是1 **/
	protected int thickness;

	/** 如果是圆角，这是孤度数，默认是6 **/
	protected int arc;

	/** 
	 * 说明
	 * ARC:6, THICKNESS: 3 具有明显的孤形，适用在窗口上
	 * ARC:4, THICKNESS: 2/1, 有孤形，但是不明显，适合用在窗口组件，如按纽上
	 * ARC:6, THICKNESS: 1, 孤形明显，适合用在窗口组件，如按纽上
	 * 
	 */

	/**
	 * 初始化参数
	 */
	private void init() {
		setArc(6);
		setThickness(1);
	}

	/**
	 * 构造默认的圆孤形的边框
	 */
	public RoundedBorder() {
		super();
		init();
	}

	/**
	 * 构造圆孤形的边框，指定颜色
	 * @param c 颜色
	 */
	public RoundedBorder(Color c) {
		super();
		init();
		setLineColor(c);
	}

	/**
	 * 构造圆孤形的边框
	 * @param c
	 * @param arc
	 * @param thickness
	 */
	public RoundedBorder(Color c, int arc, int thickness) {
		super();
		init();
		setLineColor(c);
		setArc(arc);
		setThickness(thickness);
	}

	/**
	 * 构造圆孤形的边框
	 * @param arc
	 * @param thickness
	 */
	public RoundedBorder(int arc, int thickness) {
		super();
		init();
		setArc(arc);
		setThickness(thickness);
	}

	/**
	 * 判断是NIMBUS界面
	 * 
	 * @return 返回真或者假
	 */
	public boolean isNimbusUI() {
		return GUIKit.isNimbusUI();
	}

	/**
	 * 判断是METAL界面
	 * 
	 * @return 真或者假
	 */
	public boolean isMetalUI() {
		return GUIKit.isMetalUI();
	}


	/**
	 * 返回孤度
	 * @return 真或者假
	 */
	public int getArc() {
		return arc;
	}

	/**
	 * 返回厚度
	 * @return 厚度，整数值
	 */
	public int getThickness() {
		return thickness;
	}

	/**
	 * 设置孤度，最小是1
	 * @param i
	 */
	public void setArc(int i) {
		if (i > 0) {
			arc = i;
		}
	}

	/**
	 * 设置厚度
	 * @param i 厚度
	 */
	public void setThickness(int i) {
		if (i > 0) {
			thickness = i;
		}
	}

	/**
	 * 连线的颜色
	 * @param c
	 */
	public void setLineColor(Color c) {
		lineColor = c;
	}

	/**
	 * 设置线条颜色
	 * @return Color对象
	 */
	public Color getLinkeColor() {
		return lineColor;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.border.AbstractBorder#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
	 */
	@Override
	public void paintBorder(Component component, Graphics g, int x, int y, int width, int height) {
		Color oldColor = g.getColor();

		Color c = lineColor;
		if (c == null) {
			if (isMetalUI()) {
				Color color = component.getBackground();
				ESL esl = new RGB(color).toESL();
				c = esl.toBrighter(50).toColor();
			} else if (isNimbusUI()) {
				c = Color.DARK_GRAY;
			} else {
				c = Color.DARK_GRAY;
			}
		}

		g.setColor(c);
		for (int i = 0; i < thickness; i++) {
			g.drawRoundRect(x + i, y + i, width - (i * 2) - 1, height - (i * 2) - 1, arc, arc);
		}

		//		/// PENDING(klobad) How/should do we support Roundtangles?
		////	        g.setColor(lineColor);
		//	        for(i = 0; i < thickness; i++)  {
		////		    if(!roundedCorners)
		////	                g.drawRect(x+i, y+i, width-i-i-1, height-i-i-1);
		////		    else
		//	                g.drawRoundRect(x+i, y+i, width-i-i-1, height-i-i-1, arc, arc);
		////	        }

		g.setColor(oldColor);
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(thickness, thickness, thickness, thickness);
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		insets.left = insets.top = insets.right = insets.bottom = thickness;
		return insets;
	}

}