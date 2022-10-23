/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.border;

import java.awt.*;

import javax.swing.border.AbstractBorder;
import javax.swing.plaf.metal.*;

/**
 * 具有高亮的边框。
 * 颜色根据关联组件的背景颜色自动调整，如果是METAL灰色，降低亮度，其它METAL颜色，调高亮度
 * 
 * @author scott.liang
 * @version 1.0 9/11/2021
 * @since laxcus 1.0
 */
public class HighlightBorder extends AbstractBorder {

	private static final long serialVersionUID = 9053638341317544136L;

	/** 边框范围 **/
	private Insets borderInsets;

	/** 厚度 **/
	private int thickness;

	/** 弧形 **/
	private boolean roundedCorners;

	/**
	 * 构造高亮的边框
	 * @param thickness 厚度
	 * @param roundedCorners 孤度
	 */
	public HighlightBorder(int thickness, boolean roundedCorners) {
		super();
		createInsets(thickness);
		setRoundedCorners(roundedCorners);
	}

	/**
	 * 构造具有高亮的边框
	 * @param thickness 厚度
	 */
	public HighlightBorder(int thickness) {
		this(thickness, false);
	}

	/**
	 * 构造默认的具有高亮的边框
	 */
	public HighlightBorder() {
		this(2);
	}

	/**
	 * 设置
	 * @param v
	 */
	private void createInsets(int v) {
		if (v < 1) v = 0;
		borderInsets = new Insets(v, v, v, v);
		thickness = v;
	}

	/**
	 * 设置弧形
	 * @param b
	 */
	public void setRoundedCorners(boolean b) {
		roundedCorners = b;
	}

	/**
	 * 判断是弧形
	 * @return 真或者假
	 */
	public boolean isRoundedCorners() {
		return roundedCorners;
	}

	//	/**
	//	 * 重绘边框
	//	 * @param c
	//	 * @param g
	//	 * @param x
	//	 * @param y
	//	 * @param width
	//	 * @param height
	//	 */
	//	@Override
	//	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
	//		Color color = null;
	//		if (GUIKit.isNimbusUI()) {
	//			ESL light = new ESL(150, 30, 135);
	//			color = light.toColor();
	//		} 
	//		// 如果是METAL灰色，降低
	//		else if(GUIKit.isMetalUI() && Skins.isGraySkin()) {
	//			color = c.getBackground();
	//			if (color == null) {
	//				color = Color.DARK_GRAY;
	//			}
	//			ESL e = new RGB(color).toESL();
	//			color = e.toDraker(50).toColor();
	//		} 
	//		// 其它暗色，调高亮度
	//		else {
	//			color = c.getBackground();
	//			if (color == null) {
	//				color = Color.DARK_GRAY;
	//			}
	//			ESL e = new RGB(color).toESL();
	//			color = e.toBrighter(50).toColor();
	//		}
	//		
	//		
	//		Color old = g.getColor();
	//		g.setColor(color);
	//		for (int i = 0; i < thickness; i++) {
	//			if (!roundedCorners) {
	//				g.drawRect(x + i, y + i, width - i - i - 1, height - i - i - 1);
	//			} else {
	//				g.drawRoundRect(x + i, y + i, width - i - i - 1, height - i - i
	//						- 1, thickness, thickness);
	//				
	////				g.drawRoundRect(x + i, y + i, width - i - i - 1, height - i - i
	////						- 1, 1, 1);
	//			}
	//		}
	//		// 恢复
	//		g.setColor(old);
	//	}

	/**
	 * 重绘边框
	 * @param c
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Color color = MetalLookAndFeel.getControlDarkShadow();
		if (color == null) {
			color = c.getBackground();
		}

		Color old = g.getColor();
		g.setColor(color);
		for (int i = 0; i < thickness; i++) {
			if (roundedCorners) {
				g.drawRoundRect(x + i, y + i, width - i - i - 1, height - i - i - 1, thickness, thickness);
			} else {
				g.drawRect(x + i, y + i, width - i - i - 1, height - i - i - 1);
			}
		}
		// 恢复
		g.setColor(old);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component)
	 */
	@Override
	public Insets getBorderInsets(Component c) {
		return borderInsets;
	}

}