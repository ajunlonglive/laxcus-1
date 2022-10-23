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
 * 平面复选框UI
 * 
 * @author scott.liang
 * @version 1.0 5/6/2022
 * @since laxcus 1.0
 */
public class FlatCheckBoxUI extends MetalCheckBoxUI {

	/**
	 * 复选框图标
	 * 根据桌面背景，绘制边框和勾选号
	 * 
	 * @author scott.liang
	 * @version 1.0 5/6/2022
	 * @since laxcus 1.0
	 */
	class FlatCheckBoxIcon implements Icon, UIResource, Serializable {

		private static final long serialVersionUID = -127065182366235532L;

		protected int getControlSize() {
			return 14;
		}

		public int getIconWidth() {
			return getControlSize();
		}

		public int getIconHeight() {
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

			ButtonModel model = ((JCheckBox) c).getModel();
			int w = getIconWidth();
			int h = getIconHeight();

			if (model.isEnabled()) {
				Color fillColor = null;
				// 边框是暗色
				Color borderColor = new Color(MetalLookAndFeel.getControlDarkShadow().getRGB());

				if (model.isPressed() && model.isArmed()) {
					ESL esl = new ESL(c.getBackground());
					if (Skins.isGraySkin()) {
						fillColor = esl.toDraker(20).toColor(); // 最暗
					} else {
						fillColor = esl.toBrighter(20).toColor(); // 最亮
					}
				} else if (model.isRollover()) {
					ESL esl = new ESL(c.getBackground());
					if (Skins.isGraySkin()) {
						fillColor = esl.toDraker(10).toColor();// 微暗
					} else {
						fillColor = esl.toBrighter(10).toColor(); // 微亮
					}
				} else {
					fillColor = c.getBackground();
				}
				// 颜色
				g.setColor(fillColor); 
				g.fillRect(x, y, w - 1, h - 1);
				g.setColor(borderColor); 
				g.drawRect(x, y, w - 1, h - 1);
			} else {
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

		protected void drawCheck(Component c, Graphics g, int x, int y) {
			ButtonModel model = ((JCheckBox) c).getModel();
			int controlSize = getControlSize();
			Color color = null;

			if (c.isEnabled()) {
				if (Skins.isGraySkin()) {
					ESL esl = new ESL(new Color(0, 255, 108)); // 绿色
					if (model.isPressed() && model.isArmed()) {
						color = esl.toDraker(68).toColor(); // 最暗
					} else if (model.isRollover()) {
						color = esl.toDraker(28).toColor(); // 最亮
					} else {
						color = esl.toDraker(48).toColor(); // 中间
					}
				} else {
					ESL esl = new ESL(c.getBackground());
					if (model.isPressed() && model.isArmed()) {
						color = esl.toBrighter(90).toColor();
					} else if (model.isRollover()) {
						color = esl.toBrighter(160).toColor();
					} else {
						color = esl.toBrighter(120).toColor();
					}
				}
			} else {
				if (Skins.isGraySkin()) {
					color = new Color(64, 64, 64);
				} else {
					ESL esl = new ESL(c.getBackground());
					color = esl.toBrighter(120).toColor();
				}
			}

			g.setColor(color);

			g.fillRect(x + 3, y + 5, 2, controlSize - 8);
			g.drawLine(x + (controlSize - 4), y + 3, x + 5, y + (controlSize - 6));
			g.drawLine(x + (controlSize - 4), y + 4, x + 5, y + (controlSize - 5));
		}
	}

	private final static FlatCheckBoxUI flatCheckboxUI = new FlatCheckBoxUI();

	/**
	 * 构造默认的平面复选框UI
	 */
	public FlatCheckBoxUI() {
		super();
	}

	/**
	 * 生成UI
	 * @param c
	 * @return 返回实例
	 */
	public static ComponentUI createUI(JComponent c) {
		if (c != null && c.getClass() == JCheckBox.class) {
			return flatCheckboxUI;
		} else {
			return MetalCheckBoxUI.createUI(c);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalCheckBoxUI#installDefaults(javax.swing.AbstractButton)
	 */
	@Override
	public void installDefaults(AbstractButton b) {
		super.installDefaults(b);
		// 重新定义图标
		icon = new FlatCheckBoxIcon();
	}

	//	public void installDefaults(AbstractButton b) {
	//		super.installDefaults(b);
	//		if(!defaults_initialized) {
	//			icon = UIManager.getIcon(getPropertyPrefix() + "icon");
	//			defaults_initialized = true;
	//		}
	//	}

}