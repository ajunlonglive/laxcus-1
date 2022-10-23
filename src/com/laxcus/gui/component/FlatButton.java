/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.component;

import java.awt.*;

import javax.swing.*;

import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 平面按纽
 * 
 * @author scott.liang
 * @version 1.0 9/30/2021
 * @since laxcus 1.0
 */
public class FlatButton extends JButton {

	private static final long serialVersionUID = -5921544081423573196L;

	/** 暗色调整值 **/
	private final static int DRAK_VALUE = 70;

	/** 亮色调整值 **/
	private final static int LIGHT_VALUE = 50;
	
	/** 暗色调整值 **/
	private final static int GRAY_DRAK_VALUE = 100;

	/** 亮色调整值 **/
	private final static int GRAY_LIGHT_VALUE = 50;

	/** 平面状态 **/
	private boolean flat;

	/**
	 * 初始化参数
	 */
	private void init() {
		flat = false;
		setBorderPainted(true);
		setRolloverEnabled(true);
	}

	/**
	 * 构造平面按纽
	 */
	public FlatButton() {
		super();
		init();
	}

	/**
	 * 构造平面按纽
	 * @param icon
	 */
	public FlatButton(Icon icon) {
		super(icon);
		init();
	}

	/**
	 * 构造平面按纽
	 * @param text
	 */
	public FlatButton(String text) {
		super(text);
		init();
	}

	/**
	 * 构造平面按纽
	 * @param action
	 */
	public FlatButton(Action action) {
		super(action);
		init();
	}

	/**
	 * 构造平面按纽
	 * @param text
	 * @param icon
	 */
	public FlatButton(String text, Icon icon) {
		super(text, icon);
		init();
	}

	/**
	 * 设置为平面状态
	 * @param b
	 */
	public void setFlat(boolean b) {
		flat = b;
	}
	
	/**
	 * 判断是平面状态
	 * @return 返回真或者假
	 */
	public boolean isFlat() {
		return flat;
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
	 * @return
	 */
	public boolean isMetalUI() {
		return GUIKit.isMetalUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.AbstractButton#paintBorder(java.awt.Graphics)
	 */
	@Override
	protected void paintBorder(Graphics g) {
		//		if(g != null) {
		//			try {
		//				throw new IllegalArgumentException("PRINT STACK!");
		//			} catch (Exception e) {
		//				e.printStackTrace();
		//			}
		//		}
		
		// 有效状态
		if (!isMetalUI()) {
			super.paintBorder(g);
			return;
		}

		// METAL界面，判断...
		int width = getWidth();
		int height = getHeight();
		if (isEnabled()) {
			ButtonModel bm = getModel();
			boolean pressed = (bm.isPressed() && bm.isArmed());
			if (pressed) {
				paintMetalFlatBorder(this, g, 0, 0, width, height);
			} else {
				if (bm.isRollover()) {
					paintMetalRaisedBorder(this, g, 0, 0, width, height);
				} else {
					if (!flat) {
						paintMetalRaisedBorder(this, g, 0, 0, width, height); // 不是平面，浮起
					}
				}
			}
		} else {
			if (!flat) {
				paintMetalRaisedBorder(this, g, 0, 0, width, height);
			}
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
			dark = esl.toDraker(FlatButton.GRAY_DRAK_VALUE).toColor();
			light = esl.toBrighter(FlatButton.GRAY_LIGHT_VALUE).toColor();
		} else {
			dark = esl.toDraker(FlatButton.DRAK_VALUE).toColor();
			light = esl.toBrighter(FlatButton.LIGHT_VALUE).toColor();
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
			color = esl.toDraker(FlatButton.GRAY_DRAK_VALUE).toColor();
		} else {
			color = esl.toBrighter(FlatButton.LIGHT_VALUE).toColor();
		}

		// 设置颜色
		g.setColor(color);
		g.drawRect(x, y, width - 1, height - 1);

		// 恢复为旧颜色
		g.setColor(old);
	}

}

///**
// * 平面按纽
// * 
// * @author scott.liang
// * @version 1.0 9/30/2021
// * @since laxcus 1.0
// */
//public class FlatButton extends JButton {
//
//	private static final long serialVersionUID = -5921544081423573196L;
//	
////	/** 黑色调整值 **/
////	private final static int BLACK_VALUE = 180;
//
//	/** 暗色调整值 **/
//	private final static int DRAK_VALUE = 70;
//
//	/** 亮色调整值 **/
//	private final static int LIGHT_VALUE = 50;
//	
//	
//	/** 暗色调整值 **/
//	private final static int GRAY_DRAK_VALUE = 100;
//
//	/** 亮色调整值 **/
//	private final static int GRAY_LIGHT_VALUE = 50;
//
//
//	/** 厚度，最小是1 **/
//	private int thickness;
//	
//	/** 如果是圆角，这是孤度数，默认是6 **/
//	private int arc;
//
//	/** 圆角 **/
//	private boolean roundedCorners;
//	
//	/** 
//	 * 说明
//	 * ARC:6, THICKNESS: 3 具有明显的孤形，适用在窗口上
//	 * ARC:4, THICKNESS: 2/1, 有孤形，但是不明显，适合用在窗口组件，如按纽上
//	 * ARC:6, THICKNESS: 1, 孤形明显，适合用在窗口组件，如按纽上
//	 * 
//	 */
//	
//	/**
//	 * 初始化参数
//	 */
//	private void init() {
//		// thickness = 8;
//		// roundedCorners = false;
//
//		setRoundedCorners(true);
//		setArc(6);
//		setThickness(1);
//	}
//
//	/**
//	 * 构造平面按纽
//	 */
//	public FlatButton() {
//		super();
//		init();
//	}
//
//	/**
//	 * 构造平面按纽
//	 * @param icon
//	 */
//	public FlatButton(Icon icon) {
//		super(icon);
//		init();
//	}
//
//	/**
//	 * 构造平面按纽
//	 * @param text
//	 */
//	public FlatButton(String text) {
//		super(text);
//		init();
//	}
//
//	/**
//	 * 构造平面按纽
//	 * @param action
//	 */
//	public FlatButton(Action action) {
//		super(action);
//		init();
//	}
//
//	/**
//	 * 构造平面按纽
//	 * @param text
//	 * @param icon
//	 */
//	public FlatButton(String text, Icon icon) {
//		super(text, icon);
//		init();
//	}
//
//	/**
//	 * 返回孤度
//	 * @return
//	 */
//	public int getArc() {
//		return arc;
//	}
//	
//	/**
//	 * 返回厚度
//	 * @return
//	 */
//	public int getThickness() {
//		return thickness;
//	}
//
//	/**
//	 * 判断是圆角
//	 * @return
//	 */
//	public boolean isRoundedCorners() {
//		return roundedCorners;
//	}
//
//	/**
//	 * 设置孤度，最小是1
//	 * @param i
//	 */
//	public void setArc(int i) {
//		if (i > 0) {
//			arc = i;
//		}
//	}
//	
//	public void setThickness(int i) {
//		if(i>0){
//			thickness = i;
//		}
//	}
//
//	/**
//	 * 设置为圆角
//	 * @param b
//	 */
//	public void setRoundedCorners(boolean b) {
//		roundedCorners = b;
//	}
//
//	/**
//	 * 判断是NIMBUS界面
//	 * 
//	 * @return 返回真或者假
//	 */
//	public boolean isNimbusUI() {
//		return GUIKit.isNimbusUI();
//	}
//
//	/**
//	 * 判断是METAL界面
//	 * 
//	 * @return
//	 */
//	public boolean isMetalUI() {
//		return GUIKit.isMetalUI();
//	}
//
////	/*
////	 * (non-Javadoc)
////	 * 
////	 * @see javax.swing.AbstractButton#paintBorder(java.awt.Graphics)
////	 */
////	@Override
////	protected void paintBorder(Graphics g) {
////		// 有效状态
////		if (!isMetalUI()) {
////			super.paintBorder(g);
////			return;
////		}
////		
////		// METAL界面，判断...
////		int width = getWidth();
////		int height = getHeight();
////		if (isEnabled()) {
////			boolean pressed = getModel().isPressed();
////			if (pressed) {
////				if (Skins.isGraySkin()) {
////					paintMetalBlackBorder(this, g, 0, 0, width, height);
////				} else {
////					paintMetalDarkBorder(this, g, 0, 0, width, height);
////				}
////			} else {
////				if (Skins.isGraySkin()) {
////					paintMetalDarkBorder(this, g, 0, 0, width, height);
////				} else {
////					paintMetalLightBorder(this, g, 0, 0, width, height);
////				}
////			}
////		} else {
////			if (Skins.isGraySkin()) {
////				paintMetalDarkBorder(this, g, 0, 0, width, height);
////			} else {
////				paintMetalLightBorder(this, g, 0, 0, width, height);
////			}
////		}
////
////	}
//	
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see javax.swing.AbstractButton#paintBorder(java.awt.Graphics)
//	 */
//	@Override
//	protected void paintBorder(Graphics g) {
//		// 有效状态
//		if (!isMetalUI()) {
//			super.paintBorder(g);
//			return;
//		}
//
//		// METAL界面，判断...
//		int width = getWidth();
//		int height = getHeight();
//		if (isEnabled()) {
//			boolean pressed = getModel().isPressed();
//			if (pressed) {
//				//				if (Skins.isGraySkin()) {
//				//					paintMetalBlackBorder(this, g, 0, 0, width, height);
//				//				} else {
//				//					paintMetalDarkBorder(this, g, 0, 0, width, height);
//				//				}
//				paintMetalFlatBorder(this, g, 0, 0, width, height);
//			} else {
//				paintMetalRaisedBorder(this, g, 0, 0, width, height);
//				//				if (Skins.isGraySkin()) {
//				//					// paintMetalDarkBorder(this, g, 0, 0, width, height);
//				//					paintMetalRaisedBorder(this, g, 0, 0, width, height);
//				//				} else {
//				//					// paintMetalLightBorder(this, g, 0, 0, width, height);
//				//					paintMetalLowerdBorder(this, g, 0, 0, width, height);
//				//				}
//			}
//		} else {
//			paintMetalRaisedBorder(this, g, 0, 0, width, height);
//			//			if (Skins.isGraySkin()) {
//			//				paintMetalRaisedBorder(this, g, 0, 0, width, height);
//			//			} else {
//			//				paintMetalLowerdBorder(this, g, 0, 0, width, height);
//			//			}
//		}
//	}
//
////	/*
////	 * (non-Javadoc)
////	 * 
////	 * @see javax.swing.AbstractButton#paintBorder(java.awt.Graphics)
////	 */
////	@Override
////	protected void paintBorder(Graphics g) {
////		// 有效状态
////		if (isMetalUI()) {
////			int width = getWidth();
////			int height = getHeight();
////
////			if (isEnabled()) {
////				boolean pressed = getModel().isPressed();
////				if (pressed) {
////					paintMetalDarkBorder(this, g, 0, 0, width, height);
////				} else {
////					paintMetalLightBorder(this, g, 0, 0, width, height);
////				}
////			} else {
////				paintMetalLightBorder(this, g, 0, 0, width, height);
////			}
////		} else {
////			super.paintBorder(g);
////		}
////	}
//
////	/*
////	 * (non-Javadoc)
////	 * 
////	 * @see javax.swing.AbstractButton#paintBorder(java.awt.Graphics)
////	 */
////	@Override
////	protected void paintBorder(Graphics g) {
////		// 有效状态
////		if (isMetalUI()) {
////			int width = getWidth();
////			int height = getHeight();
////
////			if (isEnabled()) {
////				boolean pressed = getModel().isPressed();
////				if (pressed) {
////					if (Skins.isGraySkin()) {
////						paintMetalLightBorder(this, g, 0, 0, width, height);
////					} else {
////						paintMetalDarkBorder(this, g, 0, 0, width, height);
////					}
////				} else {
////					if (Skins.isGraySkin()) {
////						paintMetalDarkBorder(this, g, 0, 0, width, height);
////					} else {
////						paintMetalLightBorder(this, g, 0, 0, width, height);
////					}
////				}
////			} else {
////				if (Skins.isGraySkin()) {
////					paintMetalDarkBorder(this, g, 0, 0, width, height);
////				} else {
////					paintMetalLightBorder(this, g, 0, 0, width, height);
////				}
////			}
////		} else {
////			super.paintBorder(g);
////		}
////	}
//	
////	/**
////	 * 绘制边框
////	 * 
////	 * @param color
////	 * @param g
////	 * @param x
////	 * @param y
////	 * @param width
////	 * @param height
////	 */
////	private void paintBorder(Color color, Graphics g, int x, int y, int width, int height) {
////		Color oldColor = g.getColor();
////
////		g.setColor(color);
////		for (int i = 0; i < thickness; i++) {
////			if (!roundedCorners) {
////				g.drawRect(x + i, y + i, width - i - i - 1, height - i - i - 1);
////			} else {
////				g.drawRoundRect(x + i, y + i, width - i - i - 1, height - i - i
////						- 1, thickness, thickness);
////			}
////		}
////		g.setColor(oldColor);
////	}
//
////	/**
////	 * 绘制边框
////	 * 
////	 * @param color
////	 * @param g
////	 * @param x
////	 * @param y
////	 * @param width
////	 * @param height
////	 */
////	private void paintBorder(Color color, Graphics g, int x, int y, int width, int height) {
////		Color oldColor = g.getColor();
////
////		g.setColor(color);
////		
//////		for (int i = 0; i < thickness; i++) {
//////			if (!roundedCorners) {
//////				g.drawRect(x + i, y + i, width - i - i - 1, height - i - i - 1);
//////			} else {
//////				g.drawRoundRect(x + i, y + i, width - i - i - 1, height - i - i
//////						- 1, thickness, thickness);
//////			}
//////		}
////		
////		// 圆角
////		if (roundedCorners) {
//////			g.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
//////			g.drawRoundRect(x+1, y+1, width -2 -1, height -2 - 1, arc, arc);
////			for (int i = 0; i < thickness; i++) {
////				g.drawRoundRect(x + i, y + i, width - (i * 2) - 1, height - (i * 2) - 1, arc, arc);
////			}
//////			g.drawRoundRect(x+2, y+2, width-3, height-3, thickness, thickness);
////		} else {
////			g.drawRect(x, y, width - 1, height - 1);
////		}
////		
////		g.setColor(oldColor);
////	}
//	
////	/**
////	 * 绘制METAL界面的阳刻浮雕效果
////	 * 
////	 * @param c
////	 * @param g
////	 * @param x
////	 * @param y
////	 * @param width
////	 * @param height
////	 */
////	private void paintMetalLightBorder(Component c, Graphics g, int x, int y,
////			int width, int height) {
////
////		Color color = c.getBackground();
////		ESL esl = new RGB(color).toESL();
////		Color light = esl.toBrighter(FlatButton.LIGHT_VALUE).toColor();
////
////		paintBorder(light, g, x, y, width, height);
////	}
//
////	/**
////	 * 绘制METAL界面的阴刻浮雕效果
////	 * 
////	 * @param c
////	 * @param g
////	 * @param x
////	 * @param y
////	 * @param width
////	 * @param height
////	 */
////	private void paintMetalDarkBorder(Component c, Graphics g, int x, int y,
////			int width, int height) {
////		// 取组件的背景色
////		Color color = c.getBackground();
////		ESL esl = new RGB(color).toESL();
////		Color dark = esl.toDraker(FlatButton.DRAK_VALUE).toColor();
////
////		paintBorder(dark, g, x, y, width, height);
////	}
//	
////	/**
////	 * 绘制METAL界面的阴刻浮雕效果
////	 * 
////	 * @param c
////	 * @param g
////	 * @param x
////	 * @param y
////	 * @param width
////	 * @param height
////	 */
////	private void paintMetalBlackBorder(Component c, Graphics g, int x, int y, int width, int height) {
////		// 取组件的背景色
////		Color color = c.getBackground();
////		ESL esl = new RGB(color).toESL();
////		Color black = esl.toDraker(FlatButton.BLACK_VALUE).toColor();
////
////		paintBorder(black, g, x, y, width, height);
////	}
//
//	// /**
//	// * 绘制METAL界面的阳刻浮雕效果
//	// *
//	// * @param c
//	// * @param g
//	// * @param x
//	// * @param y
//	// * @param width
//	// * @param height
//	// */
//	// private void paintMetalRaisedBorder(Component c, Graphics g, int x, int
//	// y, int width, int height) {
//	//
//	// Color color = c.getBackground();
//	// ESL esl = new RGB(color).toESL();
//	// Color dark = esl.toDraker(FlatButton.DRAK_VALUE).toColor();
//	// Color light = esl.toBrighter(FlatButton.LIGHT_VALUE).toColor();
//	//
//	// int x2 = x + width - 1;
//	// int y2 = y + height - 1;
//	//
//	// // 原色
//	// Color oldColor = g.getColor();
//	//
//	// // 亮色
//	// g.setColor(light);
//	// g.drawLine(x, y, x2, y); // 上线
//	// g.drawLine(x, y, x, y2); // 左线
//	//
//	// // 暗色
//	// g.setColor(dark);
//	// g.drawLine(x, y2, x2, y2); // 下线
//	// g.drawLine(x2, y, x2, y2); // 右线
//	//
//	// // 设置颜色
//	// g.setColor(oldColor);
//	// }
//	//
//	// /**
//	// * 绘制METAL界面的阴刻浮雕效果
//	// *
//	// * @param c
//	// * @param g
//	// * @param x
//	// * @param y
//	// * @param width
//	// * @param height
//	// */
//	// private void paintMetalLowerdBorder(Component c, Graphics g, int x, int
//	// y, int width, int height) {
//	// // 取组件的背景色
//	// Color color = c.getBackground();
//	// ESL esl = new RGB(color).toESL();
//	// Color dark = esl.toDraker(FlatButton.DRAK_VALUE).toColor();
//	// Color light = esl.toBrighter(FlatButton.LIGHT_VALUE).toColor();
//	//
//	// int x2 = x + width - 1;
//	// int y2 = y + height - 1;
//	//
//	// // 原色
//	// Color oldColor = g.getColor();
//	//
//	// // 暗色
//	// g.setColor(dark);
//	// g.drawLine(x, y, x2, y); // 上线
//	// g.drawLine(x, y, x, y2); // 左线
//	//
//	// // 高亮色
//	// g.setColor(light);
//	// g.drawLine(x, y2, x2, y2); // 下线
//	// g.drawLine(x2, y, x2, y2); // 右线
//	//
//	// // 设置颜色
//	// g.setColor(oldColor);
//	// }
//
////	 /**
////	 * 绘制METAL界面的阳刻浮雕效果
////	 *
////	 * @param c
////	 * @param g
////	 * @param x
////	 * @param y
////	 * @param width
////	 * @param height
////	 */
////	private void paintMetalRaisedBorder(Component c, Graphics g, int x, int
////			y, int width, int height) {
////
////		Color color = c.getBackground();
////		
////		Color dark, light;
////		if (Skins.isGraySkin()) {
//////			dark = Color.DARK_GRAY;
//////			light = Color.LIGHT_GRAY;
////			ESL esl = new RGB(color).toESL();
////			dark = esl.toDraker(FlatButton.GRAY_DRAK_VALUE).toColor();
////			light = esl.toBrighter(FlatButton.GRAY_LIGHT_VALUE).toColor();
////		} else {
////			ESL esl = new RGB(color).toESL();
////			light = esl.toDraker(FlatButton.DRAK_VALUE).toColor();
////			dark = esl.toBrighter(FlatButton.LIGHT_VALUE).toColor();
////		}
////		
////		int x2 = x + width - 1;
////		int y2 = y + height - 1;
////
////		// 原色
////		Color oldColor = g.getColor();
////
////		// 亮色
////		g.setColor(light);
////		g.drawLine(x, y, x2, y); // 上线
////		g.drawLine(x, y, x, y2); // 左线
////
////		// 暗色
////		g.setColor(dark);
////		g.drawLine(x, y2, x2, y2); // 下线
////		g.drawLine(x2, y, x2, y2); // 右线
////
////		// 设置颜色
////		g.setColor(oldColor);
////	}
////	
////	 /**
////	 * 绘制METAL界面的阴刻浮雕效果
////	 *
////	 * @param c
////	 * @param g
////	 * @param x
////	 * @param y
////	 * @param width
////	 * @param height
////	 */
////	private void paintMetalLowerdBorder(Component c, Graphics g, int x, int
////			y, int width, int height) {
////		// 取组件的背景色
////		Color color = c.getBackground();
////		Color dark, light;
////		if (Skins.isGraySkin()) {
//////			dark = Color.DARK_GRAY;
//////			light = Color.LIGHT_GRAY;
////			ESL esl = new RGB(color).toESL();
////			dark = esl.toDraker(FlatButton.GRAY_DRAK_VALUE).toColor();
////			light = esl.toBrighter(FlatButton.GRAY_LIGHT_VALUE).toColor();
////		} else {
////			ESL esl = new RGB(color).toESL();
////			light = esl.toDraker(FlatButton.DRAK_VALUE).toColor();
////			dark = esl.toBrighter(FlatButton.LIGHT_VALUE).toColor();
////		}
////		
////		int x2 = x + width - 1;
////		int y2 = y + height - 1;
////
////		// 原色
////		Color oldColor = g.getColor();
////
////		// 暗色
////		g.setColor(dark);
////		g.drawLine(x, y, x2, y); // 上线
////		g.drawLine(x, y, x, y2); // 左线
////
////		// 高亮色
////		g.setColor(light);
////		g.drawLine(x, y2, x2, y2); // 下线
////		g.drawLine(x2, y, x2, y2); // 右线
////
////		// 设置颜色
////		g.setColor(oldColor);
////	}
//	
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
//
//		Color color = c.getBackground();
//		ESL esl = new RGB(color).toESL();
//
//		// 区分颜色
//		Color dark, light;
//		if (Skins.isGraySkin()) {
//			dark = esl.toDraker(FlatButton.GRAY_DRAK_VALUE).toColor();
//			light = esl.toBrighter(FlatButton.GRAY_LIGHT_VALUE).toColor();
//		} else {
//			dark = esl.toDraker(FlatButton.DRAK_VALUE).toColor();
//			light = esl.toBrighter(FlatButton.LIGHT_VALUE).toColor();
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
//	
//	private void paintMetalFlatBorder(Component c, Graphics g, int x, int y, int width, int height) {
//		Color old = g.getColor();
//		Color color = c.getBackground();
//		// 取组件的背景色
//		if (color == null) {
//			color = c.getBackground();
//		}
//		// 颜色
//		ESL esl = new RGB(color).toESL();
//		if (Skins.isGraySkin()) {
//			color = esl.toDraker(FlatButton.GRAY_DRAK_VALUE).toColor();
//		} else {
//			color = esl.toBrighter(FlatButton.LIGHT_VALUE).toColor();
//		}
//
//		// 设置颜色
//		g.setColor(color);
//		g.drawRect(x, y, width - 1, height - 1);
//
//		// 恢复为旧颜色
//		g.setColor(old);
//	}
//
//}