/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.skin;

import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 无线按纽UI
 * 
 * @author scott.liang
 * @version 1.0 5/11/2022
 * @since laxcus 1.0
 */
public class FlatRadioButtonUI extends MetalRadioButtonUI {

	class FlatRadioButtonIcon implements Icon, UIResource, Serializable {

		private static final long serialVersionUID = 1L;

		protected int getControlSize() {
			return 14;
		}

		/* (non-Javadoc)
		 * @see javax.swing.Icon#getIconHeight()
		 */
		@Override
		public int getIconHeight() {
			return getControlSize();
		}

		/* (non-Javadoc)
		 * @see javax.swing.Icon#getIconWidth()
		 */
		@Override
		public int getIconWidth() {
			return getControlSize();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
		 */
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			// 保存原始颜色
			Color old = g.getColor();

			// 以基础色为准
			Color color = c.getBackground();
			ESL esl = new ESL(color);

			ButtonModel model = ((JRadioButton) c).getModel();
			int w = getIconWidth();
			int h = getIconHeight();

			// 有效
			if (model.isEnabled()) {
				Color fillColor = null;
				Color borderColor = null;

				borderColor = MetalLookAndFeel.getControlDarkShadow();
				if (borderColor != null) {
					borderColor = new Color(borderColor.getRGB());
				}

				if (model.isPressed() && model.isArmed()) {
					if (Skins.isGraySkin()) {
						fillColor = esl.toDraker(20).toColor(); // 最暗
					} else {
						fillColor = esl.toBrighter(20).toColor(); // 最亮
					}
				} else if (model.isRollover()) {
					if (Skins.isGraySkin()) {
						fillColor = esl.toDraker(10).toColor();// 微暗
					} else {
						fillColor = esl.toBrighter(10).toColor(); // 微亮
					}
				} else {
					if (Skins.isGraySkin()) {
						fillColor = color; // 不变 
					} else {
						fillColor = color; // 不变
					}
				}
				// 颜色
				g.setColor(fillColor); 
				g.fillRect(x, y, w - 1, h - 1);
				g.setColor(borderColor); 
				g.drawRect(x, y, w - 1, h - 1);
			}
			// 无效
			else {
				g.setColor(MetalLookAndFeel.getControlShadow());
				g.fillRect(x, y, w - 1, h - 1);
			}

			// 被选中，绘制勾号
			if (model.isSelected()) {
				drawCheck(c, g, x, y);
			}

			// 恢复
			g.setColor(old);
		}

		/**
		 * 恢复矩形，替换圆点
		 * @param c
		 * @param g
		 * @param x
		 * @param y
		 */
		protected void drawCheck(Component c, Graphics g, int x, int y) {
			ButtonModel model = ((JRadioButton) c).getModel();
			int controlSize = getControlSize();
			Color color = null;

			if (c.isEnabled()) {
				if (Skins.isGraySkin()) {
					ESL esl = new ESL(Color.GREEN);
					if (model.isPressed() && model.isArmed()) {
						color = esl.toDraker(68).toColor(); // 最暗
					} else if (model.isRollover()) {
						color = esl.toDraker(18).toColor(); // 最亮
					} else {
						color = esl.toDraker(48).toColor(); // 中间
					}
				} else {
					ESL esl = new ESL(c.getBackground());
					if (model.isPressed() && model.isArmed()) {
						color = esl.toBrighter(60).toColor();
					} else if (model.isRollover()) {
						color = esl.toBrighter(130).toColor();
					} else {
						color = esl.toBrighter(90).toColor();
					}
				}
			} else {
				if (Skins.isGraySkin()) {
					color = new Color(64, 64, 64);
				} else {
					ESL esl = new ESL(c.getBackground());
					color = esl.toBrighter(90).toColor();
				}
			}

			// 矩形
			g.setColor(color);
			g.fillRect(x + 4, y + 4, controlSize - 8, controlSize - 8);
		}
	}

	private final static FlatRadioButtonUI flatCheckboxUI = new FlatRadioButtonUI();

	/**
	 * 构造默认的平面无线按纽UI
	 */
	public FlatRadioButtonUI() {
		super();
	}

	/**
	 * 生成UI
	 * @param c
	 * @return 返回实例
	 */
	public static ComponentUI createUI(JComponent c) {
		if (c != null && c.getClass() == JRadioButton.class) {
			return flatCheckboxUI;
		} else {
			return MetalRadioButtonUI.createUI(c);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalRadioButtonUI#installDefaults(javax.swing.AbstractButton)
	 */
	@Override
	public void installDefaults(AbstractButton b) {
		super.installDefaults(b);
		// 重新定义图标
		icon = new FlatRadioButtonIcon();
	}

}