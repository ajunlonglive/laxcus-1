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
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 平面按纽UI <br><br>
 * 
 * 仅用来处理JButton按纽的UI视觉效果，子类也不可以！<br>
 * 目标是当第三方的图形应用中使用JButton时，拥有和FlatButton一样的视觉效果。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/5/2022
 * @since laxcus 1.0
 */
public class FlatButtonUI extends MetalButtonUI {

	class FlatButtonOutsideBorder extends MetalBorders.ButtonBorder implements Serializable {

		private static final long serialVersionUID = 1L;

		/** 暗色调整值 **/
		private final static int DRAK_VALUE = 70;

		/** 亮色调整值 **/
		private final static int LIGHT_VALUE = 50;

		/** 暗色调整值 **/
		private final static int GRAY_DRAK_VALUE = 100;

		/** 亮色调整值 **/
		private final static int GRAY_LIGHT_VALUE = 50;

		/**
		 * 按纽外侧边框
		 */
		public FlatButtonOutsideBorder() {
			super();
		}

		/* (non-Javadoc)
		 * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
		 */
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			AbstractButton button = (AbstractButton) c;
			width = button.getWidth();
			height = button.getHeight();
			if (button.isEnabled()) {
				ButtonModel bm = button.getModel();
				boolean pressed = (bm.isPressed() && bm.isArmed());
				if (pressed) {
					paintMetalFlatBorder(button, g, 0, 0, width, height);
				} else {
					paintMetalRaisedBorder(button, g, 0, 0, width, height); // 不是平面，浮起
				}
			} else {
				paintMetalRaisedBorder(button, g, 0, 0, width, height); // 不是平面，浮起
			}
		}

		/**
		 * 绘制METAL界面的阳刻浮雕效果
		 *
		 * @param c
		 * @param g
		 * @param x
		 * @param y
		 * @param width
		 * @param height
		 */
		private void paintMetalRaisedBorder(Component c, Graphics g, int x, 
				int y, int width, int height) {

			Color color = c.getBackground();
			ESL esl = new RGB(color).toESL();

			// 区分颜色
			Color dark, light;
			if (Skins.isGraySkin()) {
				dark = esl.toDraker(FlatButtonOutsideBorder.GRAY_DRAK_VALUE).toColor();
				light = esl.toBrighter(FlatButtonOutsideBorder.GRAY_LIGHT_VALUE).toColor();
			} else {
				dark = esl.toDraker(FlatButtonOutsideBorder.DRAK_VALUE).toColor();
				light = esl.toBrighter(FlatButtonOutsideBorder.LIGHT_VALUE).toColor();
			}

			int x2 = x + width - 1;
			int y2 = y + height - 1;

			// 原色
			Color old = g.getColor();

			// 亮色
			g.setColor(light);
			g.drawLine(x, y, x2, y); // 上线
			g.drawLine(x, y, x, y2); // 左线

			// 暗色
			g.setColor(dark);
			g.drawLine(x, y2, x2, y2); // 下线
			g.drawLine(x2, y, x2, y2); // 右线

			// 设置颜色
			g.setColor(old);
		}

		private void paintMetalFlatBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Color old = g.getColor();
			Color color = c.getBackground();
			// 取组件的背景色
			if (color == null) {
				color = c.getBackground();
			}
			// 颜色
			ESL esl = new RGB(color).toESL();
			if (Skins.isGraySkin()) {
				color = esl.toDraker(FlatButtonOutsideBorder.GRAY_DRAK_VALUE).toColor();
			} else {
				color = esl.toBrighter(FlatButtonOutsideBorder.LIGHT_VALUE).toColor();
			}

			// 设置颜色
			g.setColor(color);
			g.drawRect(x, y, width - 1, height - 1);

			// 恢复为旧颜色
			g.setColor(old);
		}
	}

	public class CompoundBorderUIResource extends CompoundBorder implements UIResource {

		private static final long serialVersionUID = 1L;

		public CompoundBorderUIResource(Border outsideBorder, Border insideBorder) {
			super(outsideBorder, insideBorder);
		}
	}

	private final static FlatButtonUI flatButtonUI = new FlatButtonUI();

	/**
	 * 构造平面按纽UI
	 */
	public FlatButtonUI() {
		super();
	}

	/**
	 * 返回UI实例
	 * 注意！只能是JButton，子类也不可以！
	 * @param c 句柄
	 * @return
	 */
	public static ComponentUI createUI(JComponent c) {
		if (c != null && c.getClass() == JButton.class) {
			return FlatButtonUI.flatButtonUI;
		} else {
			return MetalButtonUI.createUI(c);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalButtonUI#installDefaults(javax.swing.AbstractButton)
	 */
	@Override
	public void installDefaults(AbstractButton button) {
		super.installDefaults(button);

		Border border = button.getBorder();
		boolean success = (border != null && Laxkit.isClassFrom(border, CompoundBorder.class));
		if (success) {
			CompoundBorder bd = (CompoundBorder) border;
			button.setBorder(new CompoundBorderUIResource(new FlatButtonOutsideBorder(), bd.getInsideBorder()));
		}
	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.plaf.metal.MetalButtonUI#installDefaults(javax.swing.AbstractButton)
	//	 */
	//	@Override
	//	public void installDefaults(AbstractButton button) {
	//		super.installDefaults(button);
	//
	//		 System.out.printf("button class: %s\n", button.getClass().getName());
	//
	//		// 只处理JButton实例
	//		boolean match = (button.getClass() == JButton.class);
	//		if (match) {
	//			Border border = button.getBorder();
	//			match = (border != null && Laxkit.isClassFrom(border, CompoundBorder.class));
	//			if (match) {
	//				CompoundBorder bd = (CompoundBorder) border;
	//				button.setBorder(new CompoundBorderUIResource(new FlatButtonOutsideBorder(), bd.getInsideBorder()));
	//			}
	//		}
	//	}

	//	JButton.setAlignmentX(LEFT_ALIGNMENT);
	//    JButton.setAlignmentY(CENTER_ALIGNMENT);

	/*
	 * 
	 * border is javax.swing.plaf.BorderUIResource$CompoundBorderUIResource
outside javax.swing.plaf.metal.MetalBorders$ButtonBorder
inside javax.swing.plaf.basic.BasicBorders$MarginBorder
java.lang.IllegalArgumentException: FUCK!
        at com.laxcus.gui.skin.FlatButtonUI.installDefaults(FlatButtonUI.java:78)
        at javax.swing.plaf.basic.BasicButtonUI.installUI(BasicButtonUI.java:63)
        at javax.swing.JComponent.setUI(JComponent.java:673)
        at javax.swing.AbstractButton.setUI(AbstractButton.java:1782)
        at javax.swing.JButton.updateUI(JButton.java:128)
        at javax.swing.AbstractButton.init(AbstractButton.java:2149)
        at javax.swing.JButton.<init>(JButton.java:118)
        at javax.swing.JButton.<init>(JButton.java:91)
        at com.laxcus.watch.dialog.WatchLoginDialog.<init>(WatchLoginDialog.java:54)
        at com.laxcus.watch.window.WatchWindow.__login(WatchWindow.java:1655)
        at com.laxcus.watch.window.LaunchThread.process(LaunchThread.java:54)
        at com.laxcus.thread.VirtualThread.run(VirtualThread.java:301)
        at java.lang.Thread.run(Thread.java:619)
	 */

	//	@Override
	//	public void installDefaults(AbstractButton b) {
	//		super.installDefaults(b);
	//		Border border = b.getBorder();
	//		if (border != null && Laxkit.isClassFrom(border, CompoundBorder.class) ) {
	//			
	////			System.out.printf("border is %s\n", border.getClass().getName());
	//			CompoundBorder cb = (CompoundBorder)border;
	////			System.out.printf("outside %s\n", cb.getOutsideBorder().getClass().getName());
	////			System.out.printf("inside %s\n", cb.getInsideBorder().getClass().getName());
	////			
	////			if (b != null) {
	////				try {
	////					throw new IllegalArgumentException("FUCK!");
	////				} catch (Exception e) {
	////					e.printStackTrace();
	////				}
	////			}
	//			
	//			b.setBorder(new CompoundBorderUIResource(new FlatButtonOutsideBorder(), cb.getInsideBorder() ));
	//		}
	//
	////		b.setBorder(new FlatButtonBorder());
	//	}

	//    protected String getPropertyPrefix() {
	//        return propertyPrefix;
	//    }

	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.plaf.metal.MetalButtonUI#paintButtonPressed(java.awt.Graphics, javax.swing.AbstractButton)
	//	 */
	//	@Override
	//	protected void paintButtonPressed(Graphics g, AbstractButton c) {
	//		Dimension size = c.getSize();
	//		
	//		if (c.isContentAreaFilled()) {
	//			g.setColor(getSelectColor());
	//			g.fillRect(0, 0, size.width, size.height);
	//		} else {
	//			Color old = g.getColor();
	//			Color color = c.getBackground();
	//			// 取组件的背景色
	//			if (color == null) {
	//				color = c.getBackground();
	//			}
	//			// 颜色
	//			ESL esl = new RGB(color).toESL();
	//			if (Skins.isGraySkin()) {
	//				color = esl.toDraker(GRAY_DRAK_VALUE).toColor();
	//			} else {
	//				color = esl.toBrighter(LIGHT_VALUE).toColor();
	//			}
	//
	//			// 设置颜色
	//			g.setColor(color);
	//			g.fillRect(0, 0, size.width, size.height);
	//			//	g.drawRect(x, y, width - 1, height - 1);
	//
	//			// 恢复为旧颜色
	//			g.setColor(old);
	//			
	////			paintMetalRaisedBorder(c, g, 0, 0, size.width, size.height);
	//		}
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.plaf.metal.MetalButtonUI#paintButtonPressed(java.awt.Graphics, javax.swing.AbstractButton)
	//	 */
	//	@Override
	//	protected void paintButtonPressed(Graphics g, AbstractButton c) {
	//		if (c.isContentAreaFilled()) {
	//			Dimension size = c.getSize();
	//			Color old = g.getColor();
	//			
	////			Color color = c.getBackground();
	////			// 取组件的背景色
	////			if (color == null) {
	////				color = c.getBackground();
	////			}
	////			// 颜色
	////			ESL esl = new RGB(color).toESL();
	////			if (Skins.isGraySkin()) {
	////				color = esl.toDraker(GRAY_DRAK_VALUE).toColor();
	////			} else {
	////				color = esl.toBrighter(LIGHT_VALUE).toColor();
	////			}
	//			
	//			Color color = this.getSelectColor();
	//
	//			// 设置颜色
	//			g.setColor(color);
	//			g.fillRect(0, 0, size.width, size.height);
	//			//	g.drawRect(x, y, width - 1, height - 1);
	//
	//			// 恢复为旧颜色
	//			g.setColor(old);
	//			
	////			paintMetalRaisedBorder(c, g, 0, 0, size.width, size.height);
	//		}
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.plaf.basic.BasicButtonUI#paint(java.awt.Graphics, javax.swing.JComponent)
	//	 */
	//	@Override
	//	public void paint(Graphics g, JComponent c) {
	//		super.paint(g, c);
	//
	//		AbstractButton b = (AbstractButton) c;
	//		ButtonModel model = b.getModel();
	//		// 绘制突起的边框
	//		if (!b.isContentAreaFilled() && !model.isPressed()) {
	//			Dimension size = c.getSize();
	//			paintMetalRaisedBorder(c, g, 0, 0, size.width, size.height);
	//		}
	//	}

	//	public void update(Graphics g, JComponent c) {
	//		super.update(g, c);
	//		
	//		AbstractButton b = (AbstractButton) c;
	//		ButtonModel model = b.getModel();
	//		// 绘制突起的边框
	////		if (b.isContentAreaFilled() && !model.isPressed()) {
	//			Dimension size = c.getSize();
	//			paintMetalRaisedBorder(c, g, 0, 0, size.width, size.height);
	////		}
	//	}

	//	/**
	//	 * 绘制METAL界面的阳刻浮雕效果
	//	 *
	//	 * @param c
	//	 * @param g
	//	 * @param x
	//	 * @param y
	//	 * @param width
	//	 * @param height
	//	 */
	//	private void paintMetalRaisedBorder(Component c, Graphics g, int x, 
	//			int y, int width, int height) {
	////		System.out.println("draw raised");
	//		
	//		Color color = c.getBackground();
	//		ESL esl = new RGB(color).toESL();
	//
	//		// 区分颜色
	//		Color dark, light;
	//		if (Skins.isGraySkin()) {
	//			dark = esl.toDraker(GRAY_DRAK_VALUE).toColor();
	//			light = esl.toBrighter(GRAY_LIGHT_VALUE).toColor();
	//		} else {
	//			dark = esl.toDraker(DRAK_VALUE).toColor();
	//			light = esl.toBrighter(LIGHT_VALUE).toColor();
	//		}
	//		
	//		int x2 = x + width - 1;
	//		int y2 = y + height - 1;
	//
	//		// 原色
	//		Color old = g.getColor();
	//
	//		// 亮色
	//		g.setColor(light);
	//		g.drawLine(x, y, x2, y); // 上线
	//		g.drawLine(x, y, x, y2); // 左线
	//
	//		// 暗色
	//		g.setColor(dark);
	//		g.drawLine(x, y2, x2, y2); // 下线
	//		g.drawLine(x2, y, x2, y2); // 右线
	//
	//		// 设置颜色
	//		g.setColor(old);
	//	}

}