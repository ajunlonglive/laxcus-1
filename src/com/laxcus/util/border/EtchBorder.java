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

import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 蚀刻边框。<br>
 * 区分：阳刻（突起）、阴刻（凹陷）。颜色是以Nimbus界面为基准，如果不是，以ESL亮色的“加减50”进行调整。
 * 
 * @author scott.liang
 * @version 1.0 6/16/2021
 * @since laxcus 1.0
 */
public class EtchBorder extends AbstractBorder {
	
	private static final long serialVersionUID = -7041110027841590213L;

	/** 如果是METAL界面，是不是绘制浮雕效果，默认是假 **/
	private boolean metalPaint;

	/** 突起 **/
	private boolean raised;
	
	/** Nimbus阳刻  **/
	private Color shadowColor;
	
	/** Nimbus阳刻 **/
	private Color lightColor;

	/**
	 * 构造默认的蚀刻边框，采用Nimbus阴刻浮雕效果
	 */
	public EtchBorder() {
		super();
		setMetalPaint(false);
		setRaised(false);
		shadowColor = Color.GRAY;
		lightColor = Color.WHITE;
	}

	/**
	 * 构造蚀刻边框，指定浮雕效果
	 * @param raised
	 */
	public EtchBorder(boolean raised) {
		this();
		setRaised(raised);
	}
	
	/**
	 * 构造蚀刻边框，指定浮雕效果
	 * @param raised
	 */
	public EtchBorder(boolean raised, boolean metalPaint) {
		this(raised);
		setMetalPaint(metalPaint);
	}

	/**
	 * 构造蚀刻边框，指定参数
	 * @param raised Nimbus阳刻浮雕
	 * @param dark 暗色
	 * @param light 亮色
	 */
	public EtchBorder(boolean raised, Color dark, Color light) {
		this(raised);
		setShadowColor(dark);
		setLightColor(light);
	}

	/**
	 * 构造蚀刻边框
	 * @param raised
	 * @param dark
	 * @param light
	 * @param metalPaint
	 */
	public EtchBorder(boolean raised, Color dark, Color light, boolean metalPaint) {
		this(raised, dark, light);
		setMetalPaint(metalPaint);
	}
	
	/**
	 * 设置绘制METAL界面
	 * @param b
	 */
	public void setMetalPaint(boolean b) {
		metalPaint = b;
	}
	
	/**
	 * 判断绘制METAL界面
	 * @return 真或者假
	 */
	public boolean isMetalPaint() {
		return metalPaint;
	}

	/**
	 * 设置Nimbus阳刻浮雕效果
	 * @param b 真或者假
	 */
	public void setRaised(boolean b) {
		raised = b;
	}

	/**
	 * 判断是Nimbus阳刻浮雕效果
	 * @return 真或者假
	 */
	public boolean isRaised() {
		return raised;
	}

	/**
	 * 设置Nimbus阴刻浮雕效果颜色
	 * @param c 颜色值
	 */
	public void setShadowColor(Color c) {
		shadowColor = c;
	}

	/**
	 * 返回Nimbus阴刻浮雕效果颜色
	 * @return 颜色值
	 */
	public Color getShadowColor() {
		return shadowColor;
	}

	/**
	 * 设置Nimbus阳刻浮雕效果
	 * @param c 颜色值
	 */
	public void setLightColor(Color c) {
		lightColor = c;
	}

	/**
	 * 返回Nimbus阳刻浮雕效果
	 * @return 颜色值
	 */
	public Color getLightColor() {
		return lightColor;
	}

	/**
	 * 判断是啥界面
	 */
	private boolean isNimbus() {
		return Skins.isNimbus();
	}
	
	/**
	 * 绘制阴刻浮雕界面
	 * @param c
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void paintLowerdBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Color shadow = null;
		Color light = null;

		// 如果是“Nimbus”界面，生成蚀刻效果；如果不是，平面显示
		if (isNimbus()) {
			shadow = shadowColor;
			light = lightColor;
		} else {
			// 取组件的背景色
			if (metalPaint) {
				Color color = c.getBackground();
				ESL esl = new RGB(color).toESL();
				shadow = esl.toDraker(50).toColor();
				light = esl.toBrighter(50).toColor();
			} else {
				shadow = light = c.getBackground();
			}
		}

		int x2 = x + width - 1;
		int y2 = y + height - 1;

		// 原色
		Color oldColor = g.getColor();

		// 暗色
		g.setColor(shadow);
		g.drawLine(x, y, x2, y); // 上线
		g.drawLine(x, y, x, y2); // 左线

		// 高亮色
		g.setColor(light);
		g.drawLine(x, y2, x2, y2); // 下线
		g.drawLine(x2, y, x2, y2); // 右线

		// 设置颜色
		g.setColor(oldColor);
	}
	
	/**
	 * 绘制阳刻浮雕界面
	 * @param c
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void paintRaisedBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Color dark = null;
		Color light = null;

		// 如果是“Nimbus”界面，生成蚀刻效果；如果不是，平面显示
		if (isNimbus()) {
			dark = shadowColor;
			light = lightColor;
		} else {
			if (metalPaint) {
				Color color = c.getBackground();
				ESL esl = new RGB(color).toESL();
				dark = esl.toDraker(50).toColor();
				light = esl.toBrighter(50).toColor();
			} else {
				 // 取组件的背景色
				 light = dark = c.getBackground();
			}
		}

		int x2 = x + width - 1;
		int y2 = y + height - 1;

		// 原色
		Color oldColor = g.getColor();

		// 亮色
		g.setColor(light);
		g.drawLine(x, y, x2, y); // 上线
		g.drawLine(x, y, x, y2); // 左线

		// 暗色
		g.setColor(dark);
		g.drawLine(x, y2, x2, y2); // 下线
		g.drawLine(x2, y, x2, y2); // 右线

		// 设置颜色
		g.setColor(oldColor);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.border.LineBorder#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
	 */
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		// 突起
		if (raised) {
			paintRaisedBorder(c, g, x, y, width, height);
		} else {
			paintLowerdBorder(c, g, x, y, width, height);
		}
	}
}
