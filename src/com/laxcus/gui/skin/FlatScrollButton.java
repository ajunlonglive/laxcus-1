/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.skin;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.metal.*;

/**
 *
 * @author scott.liang
 * @version 1.0 6/18/2022
 * @since laxcus 1.0
 */
public class FlatScrollButton extends MetalScrollButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Color shadowColor;

//	private static Color highlightColor;

	private boolean isFreeStanding = false;

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public FlatScrollButton(int direction, int width, boolean freeStanding) {
		super(direction, width, freeStanding);
		setFreeStanding(freeStanding);
		
		shadowColor = UIManager.getColor("ScrollBar.darkShadow");
//	    highlightColor = UIManager.getColor("ScrollBar.highlight");

		// System.out.println("flat scroll button!");
	}
	
	public void setFreeStanding(boolean b) {
		super.setFreeStanding(b);
		isFreeStanding = b;
	}
	
//	boolean isLeftToRight(Component c) {
//		return c.getComponentOrientation().isLeftToRight();
//	}
	
//	void drawDisabledBorder(Graphics g, int x, int y, int w, int h) {
//		g.translate(x, y);
//		g.setColor(MetalLookAndFeel.getControlShadow());
//		g.drawRect(0, 0, w - 1, h - 1);
//		g.translate(-x, -y);
//	}
	
	void drawDisabledBorder(Graphics g, int x, int y, int w, int h) {
		g.translate(x, y);
		g.setColor(MetalLookAndFeel.getControlShadow());
		g.drawRect(0, 0, w, h);
		g.translate(-x, -y);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalScrollButton#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		boolean leftToRight = FlatUtil.isLeftToRight(this);
		boolean isEnabled = getParent().isEnabled();

		Color arrowColor = isEnabled ? MetalLookAndFeel.getControlInfo()
				: MetalLookAndFeel.getControlDisabled();
		boolean isPressed = getModel().isPressed();
		int width = getWidth();
		int height = getHeight();
		final int w = width;
		final int h = height;
		int arrowHeight = (height + 1) / 4;
//		int arrowWidth = (height + 1) / 2;

		if (isPressed) {
			g.setColor(MetalLookAndFeel.getControlShadow());
		} else {
			g.setColor(getBackground());
		}

//		g.fillRect(0, 0, width, height);
		
		g.fillRect(0, 0, w - 1, h - 1);

		if (getDirection() == NORTH) {
			if (!isFreeStanding) {
				height += 1;
				g.translate(0, -1);
				width += 2;
				if (!leftToRight) {
					g.translate(-1, 0);
				}
			}

			// Draw the arrow
			g.setColor(arrowColor);
//			int startY = ((h + 1) - arrowHeight) / 2;
//			int startX = (w / 2);
			
			int startY = (((h + 1) - arrowHeight) % 2 == 0 ? ((h + 1) - arrowHeight) / 2 : (((h + 1) - arrowHeight) / 2 -1));
			if(startY < 0) startY = 0;
			int startX = ((w % 2 == 0) ? (w / 2) : (w / 2) - 1);
			if(startX < 0) startX = 0;

			// System.out.println( "startX :" + startX + " startY :"+startY);
			for (int line = 0; line < arrowHeight; line++) {
				g.drawLine(startX - line, startY + line, startX + line + 1, startY + line);
			}

//			/*
//			 * g.drawLine( 7, 6, 8, 6 ); g.drawLine( 6, 7, 9, 7 ); g.drawLine(
//			 * 5, 8, 10, 8 ); g.drawLine( 4, 9, 11, 9 );
//			 */
//			if (isEnabled) {
//				g.setColor(highlightColor);
//
//				if (!isPressed) {
//					g.drawLine(1, 1, width - 3, 1);
//					g.drawLine(1, 1, 1, height - 1);
//				}
//
//				g.drawLine(width - 1, 1, width - 1, height - 1);
//
//				g.setColor(shadowColor);
//				g.drawLine(0, 0, width - 2, 0);
//				g.drawLine(0, 0, 0, height - 1);
//				g.drawLine(width - 2, 2, width - 2, height - 1);
//			} else {
//				drawDisabledBorder(g, 0, 0, width, height + 1);
//			}
			
			/*
			 * g.drawLine( 7, 6, 8, 6 ); g.drawLine( 6, 7, 9, 7 ); g.drawLine(
			 * 5, 8, 10, 8 ); g.drawLine( 4, 9, 11, 9 );
			 */
			if (isEnabled) {
//				g.setColor(highlightColor);

//				if (!isPressed) {
//					g.drawLine(1, 1, width - 3, 1);
//					g.drawLine(1, 1, 1, height - 1);
//				}
//
//				g.drawLine(width - 1, 1, width - 1, height - 1);
//
//				g.setColor(shadowColor);
//				g.drawLine(0, 0, width - 2, 0);
//				g.drawLine(0, 0, 0, height - 1);
//				g.drawLine(width - 2, 2, width - 2, height - 1);
				
				g.setColor(shadowColor);
				g.drawRect(0, 1, w - 2, h - 2);
//				g.drawRect(1, 1, w - 2, h - 1);
				
			} else {
//				drawDisabledBorder(g, 0, 0, width, height + 1);
				drawDisabledBorder(g, 0, 1, w - 2, h - 2);
			}
			
			if (!isFreeStanding) {
				height -= 1;
				g.translate(0, 1);
				width -= 2;
				if (!leftToRight) {
					g.translate(1, 0);
				}
			}
		} else if (getDirection() == SOUTH) {
			if (!isFreeStanding) {
				height += 1;
				width += 2;
				if (!leftToRight) {
					g.translate(-1, 0);
				}
			}

			// Draw the arrow
			g.setColor(arrowColor);

//			int startY = (((h + 1) - arrowHeight) / 2) + arrowHeight - 1;
//			int startX = (w / 2);
			
			int startY = (((h + 1) - arrowHeight) / 2) + arrowHeight - 1;
			int startX = ((w % 2 == 0) ? (w / 2) : (w / 2) - 1);
			if (startX < 0) startX = 0;

			// System.out.println( "startX2 :" + startX + " startY2 :"+startY);

			for (int line = 0; line < arrowHeight; line++) {
				g.drawLine(startX - line, startY - line, startX + line + 1,
						startY - line);
			}

			/*
			 * g.drawLine( 4, 5, 11, 5 ); g.drawLine( 5, 6, 10, 6 ); g.drawLine(
			 * 6, 7, 9, 7 ); g.drawLine( 7, 8, 8, 8 );
			 */

			if (isEnabled) {
//				g.setColor(highlightColor);
//
//				if (!isPressed) {
//					g.drawLine(1, 0, width - 3, 0);
//					g.drawLine(1, 0, 1, height - 3);
//				}
//
//				g.drawLine(1, height - 1, width - 1, height - 1);
//				g.drawLine(width - 1, 0, width - 1, height - 1);
//
//				g.setColor(shadowColor);
//				g.drawLine(0, 0, 0, height - 2);
//				g.drawLine(width - 2, 0, width - 2, height - 2);
//				g.drawLine(2, height - 2, width - 2, height - 2);
				
				g.setColor(shadowColor);
				g.drawRect(0,  0, w - 2, h - 2);
			} else {
//				drawDisabledBorder(g, 0, -1, width, height + 1);
				drawDisabledBorder(g, 0,  0, w - 2, h - 2);
			}

			if (!isFreeStanding) {
				height -= 1;
				width -= 2;
				if (!leftToRight) {
					g.translate(1, 0);
				}
			}
		} else if (getDirection() == EAST) {
			if (!isFreeStanding) {
				height += 2;
				width += 1;
			}

			// Draw the arrow
			g.setColor(arrowColor);

			int startX = (((w + 1) - arrowHeight) / 2) + arrowHeight - 1;
//			int startY = (h / 2);
			
			int startY = ((h % 2 == 0) ? (h / 2) : (h / 2) - 1);
			if(startY < 0) startY = 0;

			// System.out.println( "startX2 :" + startX + " startY2 :"+startY);

			for (int line = 0; line < arrowHeight; line++) {
				g.drawLine(startX - line, startY - line, startX - line, startY + line + 1);
			}

			/*
			 * g.drawLine( 5, 4, 5, 11 ); g.drawLine( 6, 5, 6, 10 ); g.drawLine(
			 * 7, 6, 7, 9 ); g.drawLine( 8, 7, 8, 8 );
			 */

			if (isEnabled) {
//				g.setColor(highlightColor);
//
//				if (!isPressed) {
//					g.drawLine(0, 1, width - 3, 1);
//					g.drawLine(0, 1, 0, height - 3);
//				}
//
//				g.drawLine(width - 1, 1, width - 1, height - 1);
//				g.drawLine(0, height - 1, width - 1, height - 1);
//
//				g.setColor(shadowColor);
//				g.drawLine(0, 0, width - 2, 0);
//				g.drawLine(width - 2, 2, width - 2, height - 2);
//				g.drawLine(0, height - 2, width - 2, height - 2);
				
				g.setColor(shadowColor);
//				g.drawRect(0, 0, w - 1, h - 2);
				g.drawRect(0, 0, w - 2, h - 2);
			} else {
//				drawDisabledBorder(g, -1, 0, width + 1, height);
				drawDisabledBorder(g, 0, 0, w - 2, h - 2);
			}
			if (!isFreeStanding) {
				height -= 2;
				width -= 1;
			}
		} else if (getDirection() == WEST) {
			if (!isFreeStanding) {
				height += 2;
				width += 1;
				g.translate(-1, 0);
			}

			// Draw the arrow
			g.setColor(arrowColor);

//			int startX = (((w + 1) - arrowHeight) / 2);
//			int startY = (h / 2);
			
			int startX = (((w + 1) - arrowHeight) % 2 == 0 ? ((w + 1) - arrowHeight) / 2 : (((w + 1) - arrowHeight) / 2 - 1));
			int startY = ((h % 2 == 0) ? (h / 2) : (h / 2) - 1);
			if (startY < 0) startY = 0;

			// 绘制箭头
			for (int line = 0; line < arrowHeight; line++) {
				g.drawLine(startX + line, startY - line, startX + line, startY + line + 1);
			}

			/*
			 * g.drawLine( 6, 7, 6, 8 ); g.drawLine( 7, 6, 7, 9 ); g.drawLine(
			 * 8, 5, 8, 10 ); g.drawLine( 9, 4, 9, 11 );
			 */

			if (isEnabled) {
//				g.setColor(highlightColor);
//
//				if (!isPressed) {
//					g.drawLine(1, 1, width - 1, 1);
//					g.drawLine(1, 1, 1, height - 3);
//				}
//
//				g.drawLine(1, height - 1, width - 1, height - 1);
//
//				g.setColor(shadowColor);
//				g.drawLine(0, 0, width - 1, 0);
//				g.drawLine(0, 0, 0, height - 2);
//				g.drawLine(2, height - 2, width - 1, height - 2);
				
				g.setColor(shadowColor);
				g.drawRect(1, 0, w - 2, h - 2); // x从1开始，左侧空一个
			} else {
//				drawDisabledBorder(g, 0, 0, width + 1, height);
				drawDisabledBorder(g, 1, 0, w - 2, h - 2);
			}

			if (!isFreeStanding) {
				height -= 2;
				width -= 1;
				g.translate(1, 0);
			}
		}
	}

//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.plaf.metal.MetalScrollButton#paint(java.awt.Graphics)
//	 */
//	@Override
//	public void paint(Graphics g) {
//		super.paint(g);
//		
//		Color old = g.getColor();
//		
//		boolean isPressed = getModel().isPressed();
//		
//		int width = getWidth();
//	    int height = getHeight();
//	    int w = width;
//	    int h = height;
//	    
//		if (isEnabled()) {
//			if (isPressed) {
//				g.setColor(MetalLookAndFeel.getControlDarkShadow());
//			} else {
//				g.setColor(MetalLookAndFeel.getControlShadow());
//			}
//		} else {
//			g.setColor(MetalLookAndFeel.getControlShadow());
//		}
//
////		g.fillRect(0, 0, width, height);
//		
//		g.drawRect(0, 0, w - 1, h - 1);
//
//		g.setColor(old);
//
//	}
	
 

}
