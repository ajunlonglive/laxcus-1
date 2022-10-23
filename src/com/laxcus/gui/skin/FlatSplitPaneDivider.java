///**
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// * 
// * Copyright 2009 laxcus.com. All rights reserved
// * 
// * @license Laxcus Public License (LPL)
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
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;

/**
 * 分隔符
 * 
 * @author scott.liang
 * @version 1.0 2021-10-2
 * @since laxcus 1.0
 */
public class FlatSplitPaneDivider extends BasicSplitPaneDivider {
	
	private static final long serialVersionUID = 1L;

	// private MetalBumps bumps = new MetalBumps(10, 10,
	// MetalLookAndFeel.getControlHighlight(),
	// MetalLookAndFeel.getControlDarkShadow(),
	// MetalLookAndFeel.getControl() );
	//
	// private MetalBumps focusBumps = new MetalBumps(10, 10,
	// MetalLookAndFeel.getPrimaryControlHighlight(),
	// MetalLookAndFeel.getPrimaryControlDarkShadow(),
	// UIManager.getColor("SplitPane.dividerFocusColor"));

//	private int inset = 2;
	
//	private boolean flatDivider = false; 

//	private Color controlColor = MetalLookAndFeel.getControl();
//
//	private Color primaryControlColor = UIManager.getColor("SplitPane.dividerFocusColor");

	public FlatSplitPaneDivider(BasicSplitPaneUI ui) {
		super(ui);
	}
	
//	public FlatSplitPaneDivider(BasicSplitPaneUI ui, boolean flatDivider) {
//		super(ui);
//		setFlatDivider(flatDivider);
//	}
	
//	public void setFlatDivider(boolean b) {
//		flatDivider = b;
//	}
//	
//	public boolean isFlatDivider() {
//		return flatDivider;
//	}
	
	/**
	 * 判断包括平面属性
	 */
	private boolean isFlatDivider() {
		if (splitPane != null) {
			return FlatUtil.isFlatDivider(splitPane);
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicSplitPaneDivider#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		// 控制颜色
		Color c = MetalLookAndFeel.getControl();
		if (c == null) {
			c = getBackground();
		}
		g.setColor(c);
		int w = getWidth();
		int h = getHeight();
		g.fillRect(0, 0, w - 1, h - 1);

		// 非平面（无边缘色），绘制边框
		if (!isFlatDivider()) {
			c = UIManager.getColor("SplitPane.dividerFocusColor");
			if (c == null) {
				c = MetalLookAndFeel.getControlDarkShadow();
			}
			g.setColor(c);
			
//			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			
			if (FlatUtil.isDrawRectangle(splitPane, false)) {
				g.drawRect(0, 0, w - 1, h - 1);
			} else {
				if (FlatUtil.isDrawTopLine(splitPane, false)) {
					g.drawLine(0, 0, w - 1, 0); // 顶部线
				}
				if (FlatUtil.isDrawBottomLine(splitPane, false)) {
					g.drawLine(0, h - 1, w - 1, h - 1); // 底线部
				}
				if (FlatUtil.isDrawLeftLine(splitPane, false)) {
					g.drawLine(0, 0, 0, h - 1); // 左侧
				}
				if (FlatUtil.isDrawRightLine(splitPane, false)) {
					g.drawLine(w - 1, 0, w - 1, h - 1); // 右侧
				}
			}

			// int who = splitPane.getOrientation();
			// if (who == JSplitPane.VERTICAL_SPLIT) {
			// g.drawRect(1, 0, width - 3, height - 1);
			// } else if (who == JSplitPane.HORIZONTAL_SPLIT) {
			// g.drawRect(0, 1, width - 1, height - 3);
			// }
		}
	}
	
//	public void paint(Graphics g) {
////		System.out.printf("SplitPaneDivider %s\n" , (this.flatDivider ? "平面" : "非平面"));
//
////		//		MetalBumps usedBumps;
////		if (splitPane.hasFocus()) {
////			//			usedBumps = focusBumps;
////			g.setColor(primaryControlColor);
////		} else {
////			//			usedBumps = bumps;
////			g.setColor(controlColor);
////		}
//
//		g.setColor(controlColor);
//		Rectangle clip = g.getClipBounds();
//		g.fillRect(clip.x, clip.y, clip.width, clip.height);
//		
////		Insets insets = getInsets();
////		Dimension size = getSize();
////		size.width -= inset * 2;
////		size.height -= inset * 2;
////		int drawX = inset;
////		int drawY = inset;
////		if (insets != null) {
////			size.width -= (insets.left + insets.right);
////			size.height -= (insets.top + insets.bottom);
////			drawX += insets.left;
////			drawY += insets.top;
////		}
//		
//		//		usedBumps.setBumpArea(size);
//		//		usedBumps.paintIcon(this, g, drawX, drawY);
//
//		// 非平面设置
//		if (!isFlatDivider()) {
////			super.paint(g);
//			
//			g.setColor( primaryControlColor); // Color.RED );
//			g.drawRect(clip.x, clip.y, clip.width-1, clip.height-1);
//		}
//	}

//	public void paint(Graphics g) {
////		System.out.printf("SplitPaneDivider %s\n" , (this.flatDivider ? "平面" : "非平面"));
//
//		//		MetalBumps usedBumps;
//		if (splitPane.hasFocus()) {
//			//			usedBumps = focusBumps;
//			g.setColor(primaryControlColor);
//		} else {
//			//			usedBumps = bumps;
//			g.setColor(controlColor);
//		}
//
//		Rectangle clip = g.getClipBounds();
//		g.fillRect(clip.x, clip.y, clip.width, clip.height);
//		
//		Insets insets = getInsets();
//		Dimension size = getSize();
//		size.width -= inset * 2;
//		size.height -= inset * 2;
//		int drawX = inset;
//		int drawY = inset;
//		if (insets != null) {
//			size.width -= (insets.left + insets.right);
//			size.height -= (insets.top + insets.bottom);
//			drawX += insets.left;
//			drawY += insets.top;
//		}
//		
//		//		usedBumps.setBumpArea(size);
//		//		usedBumps.paintIcon(this, g, drawX, drawY);
//
//		// 非平面设置
//		if (!isFlatDivider()) {
////			super.paint(g);
//			
//			g.setColor( Color.RED );
//			g.drawRect(clip.x, clip.y, clip.width-1, clip.height-1);
//		}
//	}

//	/**
//	 * Creates and return an instance of JButton that can be used to collapse
//	 * the left component in the metal split pane.
//	 */
//	protected JButton createLeftOneTouchButton() {
//		JButton b = new JButton() {
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 1L;
//			// Sprite buffer for the arrow image of the left button
//			int[][] buffer = { { 0, 0, 0, 2, 2, 0, 0, 0, 0 },
//					{ 0, 0, 2, 1, 1, 1, 0, 0, 0 },
//					{ 0, 2, 1, 1, 1, 1, 1, 0, 0 },
//					{ 2, 1, 1, 1, 1, 1, 1, 1, 0 },
//					{ 0, 3, 3, 3, 3, 3, 3, 3, 3 } };
//
//			public void setBorder(Border b) {
//			}
//
//			public void paint(Graphics g) {
//				JSplitPane splitPane = getSplitPaneFromSuper();
//				if (splitPane != null) {
//					int oneTouchSize = getOneTouchSizeFromSuper();
//					int orientation = getOrientationFromSuper();
//					int blockSize = Math.min(getDividerSize(), oneTouchSize);
//
//					// Initialize the color array
//					Color[] colors = { this.getBackground(),
//							MetalLookAndFeel.getPrimaryControlDarkShadow(),
//							MetalLookAndFeel.getPrimaryControlInfo(),
//							MetalLookAndFeel.getPrimaryControlHighlight() };
//
//					// Fill the background first ...
//					g.setColor(this.getBackground());
//					if (isOpaque()) {
//						g.fillRect(0, 0, this.getWidth(), this.getHeight());
//					}
//
//					// ... then draw the arrow.
//					if (getModel().isPressed()) {
//						// Adjust color mapping for pressed button state
//						colors[1] = colors[2];
//					}
//					if (orientation == JSplitPane.VERTICAL_SPLIT) {
//						// Draw the image for a vertical split
//						for (int i = 1; i <= buffer[0].length; i++) {
//							for (int j = 1; j < blockSize; j++) {
//								if (buffer[j - 1][i - 1] == 0) {
//									continue;
//								} else {
//									g.setColor(colors[buffer[j - 1][i - 1]]);
//								}
//								g.drawLine(i, j, i, j);
//							}
//						}
//					} else {
//						// Draw the image for a horizontal split
//						// by simply swaping the i and j axis.
//						// Except the drawLine() call this code is
//						// identical to the code block above. This was done
//						// in order to remove the additional orientation
//						// check for each pixel.
//						for (int i = 1; i <= buffer[0].length; i++) {
//							for (int j = 1; j < blockSize; j++) {
//								if (buffer[j - 1][i - 1] == 0) {
//									// Nothing needs
//									// to be drawn
//									continue;
//								} else {
//									// Set the color from the
//									// color map
//									g.setColor(colors[buffer[j - 1][i - 1]]);
//								}
//								// Draw a pixel
//								g.drawLine(j, i, j, i);
//							}
//						}
//					}
//				}
//			}
//
//			// Don't want the button to participate in focus traversable.
//			public boolean isFocusTraversable() {
//				return false;
//			}
//		};
//		b.setRequestFocusEnabled(false);
//		b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//		b.setFocusPainted(false);
//		b.setBorderPainted(false);
//		maybeMakeButtonOpaque(b);
//		return b;
//	}
//
//	/**
//	 * If necessary <code>c</code> is made opaque.
//	 */
//	private void maybeMakeButtonOpaque(JComponent c) {
//		Object opaque = UIManager.get("SplitPane.oneTouchButtonsOpaque");
//		if (opaque != null) {
//			c.setOpaque(((Boolean) opaque).booleanValue());
//		}
//	}
//
//	/**
//	 * Creates and return an instance of JButton that can be used to collapse
//	 * the right component in the metal split pane.
//	 */
//	protected JButton createRightOneTouchButton() {
//		JButton b = new JButton() {
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 1L;
//			// Sprite buffer for the arrow image of the right button
//			int[][] buffer = { { 2, 2, 2, 2, 2, 2, 2, 2 },
//					{ 0, 1, 1, 1, 1, 1, 1, 3 }, { 0, 0, 1, 1, 1, 1, 3, 0 },
//					{ 0, 0, 0, 1, 1, 3, 0, 0 }, { 0, 0, 0, 0, 3, 0, 0, 0 } };
//
//			public void setBorder(Border border) {
//			}
//
//			public void paint(Graphics g) {
//				JSplitPane splitPane = getSplitPaneFromSuper();
//				if (splitPane != null) {
//					int oneTouchSize = getOneTouchSizeFromSuper();
//					int orientation = getOrientationFromSuper();
//					int blockSize = Math.min(getDividerSize(), oneTouchSize);
//
//					// Initialize the color array
//					Color[] colors = { this.getBackground(),
//							MetalLookAndFeel.getPrimaryControlDarkShadow(),
//							MetalLookAndFeel.getPrimaryControlInfo(),
//							MetalLookAndFeel.getPrimaryControlHighlight() };
//
//					// Fill the background first ...
//					g.setColor(this.getBackground());
//					if (isOpaque()) {
//						g.fillRect(0, 0, this.getWidth(), this.getHeight());
//					}
//
//					// ... then draw the arrow.
//					if (getModel().isPressed()) {
//						// Adjust color mapping for pressed button state
//						colors[1] = colors[2];
//					}
//					if (orientation == JSplitPane.VERTICAL_SPLIT) {
//						// Draw the image for a vertical split
//						for (int i = 1; i <= buffer[0].length; i++) {
//							for (int j = 1; j < blockSize; j++) {
//								if (buffer[j - 1][i - 1] == 0) {
//									continue;
//								} else {
//									g.setColor(colors[buffer[j - 1][i - 1]]);
//								}
//								g.drawLine(i, j, i, j);
//							}
//						}
//					} else {
//						// Draw the image for a horizontal split
//						// by simply swaping the i and j axis.
//						// Except the drawLine() call this code is
//						// identical to the code block above. This was done
//						// in order to remove the additional orientation
//						// check for each pixel.
//						for (int i = 1; i <= buffer[0].length; i++) {
//							for (int j = 1; j < blockSize; j++) {
//								if (buffer[j - 1][i - 1] == 0) {
//									// Nothing needs
//									// to be drawn
//									continue;
//								} else {
//									// Set the color from the
//									// color map
//									g.setColor(colors[buffer[j - 1][i - 1]]);
//								}
//								// Draw a pixel
//								g.drawLine(j, i, j, i);
//							}
//						}
//					}
//				}
//			}
//
//			// Don't want the button to participate in focus traversable.
//			public boolean isFocusTraversable() {
//				return false;
//			}
//		};
//		b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//		b.setFocusPainted(false);
//		b.setBorderPainted(false);
//		b.setRequestFocusEnabled(false);
//		maybeMakeButtonOpaque(b);
//		return b;
//	}
//
//	/**
//	 * Used to layout a FlatSplitPaneDivider. Layout for the divider involves
//	 * appropriately moving the left/right buttons around.
//	 * <p>
//	 * This class should be treated as a &quot;protected&quot; inner class.
//	 * Instantiate it only within subclasses of FlatSplitPaneDivider.
//	 */
//	public class MetalDividerLayout implements LayoutManager {
//
//		// NOTE NOTE NOTE NOTE NOTE
//		// This class is no longer used, the functionality has
//		// been rolled into BasicSplitPaneDivider.DividerLayout as a
//		// defaults property
//
//		public void layoutContainer(Container c) {
//			JButton leftButton = getLeftButtonFromSuper();
//			JButton rightButton = getRightButtonFromSuper();
//			JSplitPane splitPane = getSplitPaneFromSuper();
//			int orientation = getOrientationFromSuper();
//			int oneTouchSize = getOneTouchSizeFromSuper();
//			int oneTouchOffset = getOneTouchOffsetFromSuper();
//			Insets insets = getInsets();
//
//			// This layout differs from the one used in BasicSplitPaneDivider.
//			// It does not center justify the oneTouchExpadable buttons.
//			// This was necessary in order to meet the spec of the Metal
//			// splitpane divider.
//			if (leftButton != null && rightButton != null
//					&& c == FlatSplitPaneDivider.this) {
//				if (splitPane.isOneTouchExpandable()) {
//					if (orientation == JSplitPane.VERTICAL_SPLIT) {
//						int extraY = (insets != null) ? insets.top : 0;
//						int blockSize = getDividerSize();
//
//						if (insets != null) {
//							blockSize -= (insets.top + insets.bottom);
//						}
//						blockSize = Math.min(blockSize, oneTouchSize);
//						leftButton.setBounds(oneTouchOffset, extraY,
//								blockSize * 2, blockSize);
//						rightButton.setBounds(
//								oneTouchOffset + oneTouchSize * 2, extraY,
//								blockSize * 2, blockSize);
//					} else {
//						int blockSize = getDividerSize();
//						int extraX = (insets != null) ? insets.left : 0;
//
//						if (insets != null) {
//							blockSize -= (insets.left + insets.right);
//						}
//						blockSize = Math.min(blockSize, oneTouchSize);
//						leftButton.setBounds(extraX, oneTouchOffset, blockSize,
//								blockSize * 2);
//						rightButton.setBounds(extraX, oneTouchOffset
//								+ oneTouchSize * 2, blockSize, blockSize * 2);
//					}
//				} else {
//					leftButton.setBounds(-5, -5, 1, 1);
//					rightButton.setBounds(-5, -5, 1, 1);
//				}
//			}
//		}
//
//		public Dimension minimumLayoutSize(Container c) {
//			return new Dimension(0, 0);
//		}
//
//		public Dimension preferredLayoutSize(Container c) {
//			return new Dimension(0, 0);
//		}
//
//		public void removeLayoutComponent(Component c) {
//		}
//
//		public void addLayoutComponent(String string, Component c) {
//		}
//	}
//
//	/*
//	 * The following methods only exist in order to be able to access protected
//	 * members in the superclass, because these are otherwise not available in
//	 * any inner class.
//	 */
//
//	@SuppressWarnings("static-access")
//	public int getOneTouchSizeFromSuper() {
//		return super.ONE_TOUCH_SIZE;
//	}
//
//	@SuppressWarnings("static-access")
//	public int getOneTouchOffsetFromSuper() {
//		return super.ONE_TOUCH_OFFSET;
//	}
//
//	int getOrientationFromSuper() {
//		return super.orientation;
//	}
//
//	JSplitPane getSplitPaneFromSuper() {
//		return super.splitPane;
//	}
//
//	JButton getLeftButtonFromSuper() {
//		return super.leftButton;
//	}
//
//	JButton getRightButtonFromSuper() {
//		return super.rightButton;
//	}
	
}


// */
//package com.laxcus.gui.skin;
//
//import java.awt.*;
//
//import javax.swing.*;
//import javax.swing.border.*;
//import javax.swing.plaf.basic.*;
//import javax.swing.plaf.metal.*;
//
///**
// * 分隔符
// * 
// * @author scott.liang
// * @version 1.0 2021-10-2
// * @since laxcus 1.0
// */
//public class FlatSplitPaneDivider extends BasicSplitPaneDivider {
//	
//	private static final long serialVersionUID = 1L;
//
//	// private MetalBumps bumps = new MetalBumps(10, 10,
//	// MetalLookAndFeel.getControlHighlight(),
//	// MetalLookAndFeel.getControlDarkShadow(),
//	// MetalLookAndFeel.getControl() );
//	//
//	// private MetalBumps focusBumps = new MetalBumps(10, 10,
//	// MetalLookAndFeel.getPrimaryControlHighlight(),
//	// MetalLookAndFeel.getPrimaryControlDarkShadow(),
//	// UIManager.getColor("SplitPane.dividerFocusColor"));
//
////	private int inset = 2;
//	
////	private boolean flatDivider = false; 
//
////	private Color controlColor = MetalLookAndFeel.getControl();
////
////	private Color primaryControlColor = UIManager.getColor("SplitPane.dividerFocusColor");
//
//	public FlatSplitPaneDivider(BasicSplitPaneUI ui) {
//		super(ui);
//	}
//	
////	public FlatSplitPaneDivider(BasicSplitPaneUI ui, boolean flatDivider) {
////		super(ui);
////		setFlatDivider(flatDivider);
////	}
//	
////	public void setFlatDivider(boolean b) {
////		flatDivider = b;
////	}
////	
////	public boolean isFlatDivider() {
////		return flatDivider;
////	}
//	
//	/**
//	 * 判断包括平面属性
//	 */
//	private boolean isFlatDivider() {
//		if (splitPane != null) {
//			return FlatUtil.isFlatDivider(splitPane);
//		}
//		return false;
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.plaf.basic.BasicSplitPaneDivider#paint(java.awt.Graphics)
//	 */
//	@Override
//	public void paint(Graphics g) {
//		super.paint(g);
//		
//		// 控制颜色
//		Color c = MetalLookAndFeel.getControl();
//		if (c == null) {
//			c = getBackground();
//		}
//		g.setColor(c);
//		int width = getWidth();
//		int height = getHeight();
//		g.fillRect(0, 0, width - 1, height - 1);
//
//		// 非平面（无边缘色），绘制边框
//		if (!isFlatDivider()) {
//			c = UIManager.getColor("SplitPane.dividerFocusColor");
//			if (c == null) {
//				c = MetalLookAndFeel.getControlDarkShadow();
//			}
//
//			g.setColor(c);
//			int who = splitPane.getOrientation();
//			if (who == JSplitPane.VERTICAL_SPLIT) {
//				g.drawRect(1, 0, width - 3, height - 1);
//			} else if (who == JSplitPane.HORIZONTAL_SPLIT) {
//				g.drawRect(0, 1, width - 1, height - 3);
//			}
//		}
//	}
//	
////	public void paint(Graphics g) {
//////		System.out.printf("SplitPaneDivider %s\n" , (this.flatDivider ? "平面" : "非平面"));
////
//////		//		MetalBumps usedBumps;
//////		if (splitPane.hasFocus()) {
//////			//			usedBumps = focusBumps;
//////			g.setColor(primaryControlColor);
//////		} else {
//////			//			usedBumps = bumps;
//////			g.setColor(controlColor);
//////		}
////
////		g.setColor(controlColor);
////		Rectangle clip = g.getClipBounds();
////		g.fillRect(clip.x, clip.y, clip.width, clip.height);
////		
//////		Insets insets = getInsets();
//////		Dimension size = getSize();
//////		size.width -= inset * 2;
//////		size.height -= inset * 2;
//////		int drawX = inset;
//////		int drawY = inset;
//////		if (insets != null) {
//////			size.width -= (insets.left + insets.right);
//////			size.height -= (insets.top + insets.bottom);
//////			drawX += insets.left;
//////			drawY += insets.top;
//////		}
////		
////		//		usedBumps.setBumpArea(size);
////		//		usedBumps.paintIcon(this, g, drawX, drawY);
////
////		// 非平面设置
////		if (!isFlatDivider()) {
//////			super.paint(g);
////			
////			g.setColor( primaryControlColor); // Color.RED );
////			g.drawRect(clip.x, clip.y, clip.width-1, clip.height-1);
////		}
////	}
//
////	public void paint(Graphics g) {
//////		System.out.printf("SplitPaneDivider %s\n" , (this.flatDivider ? "平面" : "非平面"));
////
////		//		MetalBumps usedBumps;
////		if (splitPane.hasFocus()) {
////			//			usedBumps = focusBumps;
////			g.setColor(primaryControlColor);
////		} else {
////			//			usedBumps = bumps;
////			g.setColor(controlColor);
////		}
////
////		Rectangle clip = g.getClipBounds();
////		g.fillRect(clip.x, clip.y, clip.width, clip.height);
////		
////		Insets insets = getInsets();
////		Dimension size = getSize();
////		size.width -= inset * 2;
////		size.height -= inset * 2;
////		int drawX = inset;
////		int drawY = inset;
////		if (insets != null) {
////			size.width -= (insets.left + insets.right);
////			size.height -= (insets.top + insets.bottom);
////			drawX += insets.left;
////			drawY += insets.top;
////		}
////		
////		//		usedBumps.setBumpArea(size);
////		//		usedBumps.paintIcon(this, g, drawX, drawY);
////
////		// 非平面设置
////		if (!isFlatDivider()) {
//////			super.paint(g);
////			
////			g.setColor( Color.RED );
////			g.drawRect(clip.x, clip.y, clip.width-1, clip.height-1);
////		}
////	}
//
//	/**
//	 * Creates and return an instance of JButton that can be used to collapse
//	 * the left component in the metal split pane.
//	 */
//	protected JButton createLeftOneTouchButton() {
//		JButton b = new JButton() {
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 1L;
//			// Sprite buffer for the arrow image of the left button
//			int[][] buffer = { { 0, 0, 0, 2, 2, 0, 0, 0, 0 },
//					{ 0, 0, 2, 1, 1, 1, 0, 0, 0 },
//					{ 0, 2, 1, 1, 1, 1, 1, 0, 0 },
//					{ 2, 1, 1, 1, 1, 1, 1, 1, 0 },
//					{ 0, 3, 3, 3, 3, 3, 3, 3, 3 } };
//
//			public void setBorder(Border b) {
//			}
//
//			public void paint(Graphics g) {
//				JSplitPane splitPane = getSplitPaneFromSuper();
//				if (splitPane != null) {
//					int oneTouchSize = getOneTouchSizeFromSuper();
//					int orientation = getOrientationFromSuper();
//					int blockSize = Math.min(getDividerSize(), oneTouchSize);
//
//					// Initialize the color array
//					Color[] colors = { this.getBackground(),
//							MetalLookAndFeel.getPrimaryControlDarkShadow(),
//							MetalLookAndFeel.getPrimaryControlInfo(),
//							MetalLookAndFeel.getPrimaryControlHighlight() };
//
//					// Fill the background first ...
//					g.setColor(this.getBackground());
//					if (isOpaque()) {
//						g.fillRect(0, 0, this.getWidth(), this.getHeight());
//					}
//
//					// ... then draw the arrow.
//					if (getModel().isPressed()) {
//						// Adjust color mapping for pressed button state
//						colors[1] = colors[2];
//					}
//					if (orientation == JSplitPane.VERTICAL_SPLIT) {
//						// Draw the image for a vertical split
//						for (int i = 1; i <= buffer[0].length; i++) {
//							for (int j = 1; j < blockSize; j++) {
//								if (buffer[j - 1][i - 1] == 0) {
//									continue;
//								} else {
//									g.setColor(colors[buffer[j - 1][i - 1]]);
//								}
//								g.drawLine(i, j, i, j);
//							}
//						}
//					} else {
//						// Draw the image for a horizontal split
//						// by simply swaping the i and j axis.
//						// Except the drawLine() call this code is
//						// identical to the code block above. This was done
//						// in order to remove the additional orientation
//						// check for each pixel.
//						for (int i = 1; i <= buffer[0].length; i++) {
//							for (int j = 1; j < blockSize; j++) {
//								if (buffer[j - 1][i - 1] == 0) {
//									// Nothing needs
//									// to be drawn
//									continue;
//								} else {
//									// Set the color from the
//									// color map
//									g.setColor(colors[buffer[j - 1][i - 1]]);
//								}
//								// Draw a pixel
//								g.drawLine(j, i, j, i);
//							}
//						}
//					}
//				}
//			}
//
//			// Don't want the button to participate in focus traversable.
//			public boolean isFocusTraversable() {
//				return false;
//			}
//		};
//		b.setRequestFocusEnabled(false);
//		b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//		b.setFocusPainted(false);
//		b.setBorderPainted(false);
//		maybeMakeButtonOpaque(b);
//		return b;
//	}
//
//	/**
//	 * If necessary <code>c</code> is made opaque.
//	 */
//	private void maybeMakeButtonOpaque(JComponent c) {
//		Object opaque = UIManager.get("SplitPane.oneTouchButtonsOpaque");
//		if (opaque != null) {
//			c.setOpaque(((Boolean) opaque).booleanValue());
//		}
//	}
//
//	/**
//	 * Creates and return an instance of JButton that can be used to collapse
//	 * the right component in the metal split pane.
//	 */
//	protected JButton createRightOneTouchButton() {
//		JButton b = new JButton() {
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 1L;
//			// Sprite buffer for the arrow image of the right button
//			int[][] buffer = { { 2, 2, 2, 2, 2, 2, 2, 2 },
//					{ 0, 1, 1, 1, 1, 1, 1, 3 }, { 0, 0, 1, 1, 1, 1, 3, 0 },
//					{ 0, 0, 0, 1, 1, 3, 0, 0 }, { 0, 0, 0, 0, 3, 0, 0, 0 } };
//
//			public void setBorder(Border border) {
//			}
//
//			public void paint(Graphics g) {
//				JSplitPane splitPane = getSplitPaneFromSuper();
//				if (splitPane != null) {
//					int oneTouchSize = getOneTouchSizeFromSuper();
//					int orientation = getOrientationFromSuper();
//					int blockSize = Math.min(getDividerSize(), oneTouchSize);
//
//					// Initialize the color array
//					Color[] colors = { this.getBackground(),
//							MetalLookAndFeel.getPrimaryControlDarkShadow(),
//							MetalLookAndFeel.getPrimaryControlInfo(),
//							MetalLookAndFeel.getPrimaryControlHighlight() };
//
//					// Fill the background first ...
//					g.setColor(this.getBackground());
//					if (isOpaque()) {
//						g.fillRect(0, 0, this.getWidth(), this.getHeight());
//					}
//
//					// ... then draw the arrow.
//					if (getModel().isPressed()) {
//						// Adjust color mapping for pressed button state
//						colors[1] = colors[2];
//					}
//					if (orientation == JSplitPane.VERTICAL_SPLIT) {
//						// Draw the image for a vertical split
//						for (int i = 1; i <= buffer[0].length; i++) {
//							for (int j = 1; j < blockSize; j++) {
//								if (buffer[j - 1][i - 1] == 0) {
//									continue;
//								} else {
//									g.setColor(colors[buffer[j - 1][i - 1]]);
//								}
//								g.drawLine(i, j, i, j);
//							}
//						}
//					} else {
//						// Draw the image for a horizontal split
//						// by simply swaping the i and j axis.
//						// Except the drawLine() call this code is
//						// identical to the code block above. This was done
//						// in order to remove the additional orientation
//						// check for each pixel.
//						for (int i = 1; i <= buffer[0].length; i++) {
//							for (int j = 1; j < blockSize; j++) {
//								if (buffer[j - 1][i - 1] == 0) {
//									// Nothing needs
//									// to be drawn
//									continue;
//								} else {
//									// Set the color from the
//									// color map
//									g.setColor(colors[buffer[j - 1][i - 1]]);
//								}
//								// Draw a pixel
//								g.drawLine(j, i, j, i);
//							}
//						}
//					}
//				}
//			}
//
//			// Don't want the button to participate in focus traversable.
//			public boolean isFocusTraversable() {
//				return false;
//			}
//		};
//		b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//		b.setFocusPainted(false);
//		b.setBorderPainted(false);
//		b.setRequestFocusEnabled(false);
//		maybeMakeButtonOpaque(b);
//		return b;
//	}
//
//	/**
//	 * Used to layout a FlatSplitPaneDivider. Layout for the divider involves
//	 * appropriately moving the left/right buttons around.
//	 * <p>
//	 * This class should be treated as a &quot;protected&quot; inner class.
//	 * Instantiate it only within subclasses of FlatSplitPaneDivider.
//	 */
//	public class MetalDividerLayout implements LayoutManager {
//
//		// NOTE NOTE NOTE NOTE NOTE
//		// This class is no longer used, the functionality has
//		// been rolled into BasicSplitPaneDivider.DividerLayout as a
//		// defaults property
//
//		public void layoutContainer(Container c) {
//			JButton leftButton = getLeftButtonFromSuper();
//			JButton rightButton = getRightButtonFromSuper();
//			JSplitPane splitPane = getSplitPaneFromSuper();
//			int orientation = getOrientationFromSuper();
//			int oneTouchSize = getOneTouchSizeFromSuper();
//			int oneTouchOffset = getOneTouchOffsetFromSuper();
//			Insets insets = getInsets();
//
//			// This layout differs from the one used in BasicSplitPaneDivider.
//			// It does not center justify the oneTouchExpadable buttons.
//			// This was necessary in order to meet the spec of the Metal
//			// splitpane divider.
//			if (leftButton != null && rightButton != null
//					&& c == FlatSplitPaneDivider.this) {
//				if (splitPane.isOneTouchExpandable()) {
//					if (orientation == JSplitPane.VERTICAL_SPLIT) {
//						int extraY = (insets != null) ? insets.top : 0;
//						int blockSize = getDividerSize();
//
//						if (insets != null) {
//							blockSize -= (insets.top + insets.bottom);
//						}
//						blockSize = Math.min(blockSize, oneTouchSize);
//						leftButton.setBounds(oneTouchOffset, extraY,
//								blockSize * 2, blockSize);
//						rightButton.setBounds(
//								oneTouchOffset + oneTouchSize * 2, extraY,
//								blockSize * 2, blockSize);
//					} else {
//						int blockSize = getDividerSize();
//						int extraX = (insets != null) ? insets.left : 0;
//
//						if (insets != null) {
//							blockSize -= (insets.left + insets.right);
//						}
//						blockSize = Math.min(blockSize, oneTouchSize);
//						leftButton.setBounds(extraX, oneTouchOffset, blockSize,
//								blockSize * 2);
//						rightButton.setBounds(extraX, oneTouchOffset
//								+ oneTouchSize * 2, blockSize, blockSize * 2);
//					}
//				} else {
//					leftButton.setBounds(-5, -5, 1, 1);
//					rightButton.setBounds(-5, -5, 1, 1);
//				}
//			}
//		}
//
//		public Dimension minimumLayoutSize(Container c) {
//			return new Dimension(0, 0);
//		}
//
//		public Dimension preferredLayoutSize(Container c) {
//			return new Dimension(0, 0);
//		}
//
//		public void removeLayoutComponent(Component c) {
//		}
//
//		public void addLayoutComponent(String string, Component c) {
//		}
//	}
//
//	/*
//	 * The following methods only exist in order to be able to access protected
//	 * members in the superclass, because these are otherwise not available in
//	 * any inner class.
//	 */
//
//	@SuppressWarnings("static-access")
//	public int getOneTouchSizeFromSuper() {
//		return super.ONE_TOUCH_SIZE;
//	}
//
//	@SuppressWarnings("static-access")
//	public int getOneTouchOffsetFromSuper() {
//		return super.ONE_TOUCH_OFFSET;
//	}
//
//	int getOrientationFromSuper() {
//		return super.orientation;
//	}
//
//	JSplitPane getSplitPaneFromSuper() {
//		return super.splitPane;
//	}
//
//	JButton getLeftButtonFromSuper() {
//		return super.leftButton;
//	}
//
//	JButton getRightButtonFromSuper() {
//		return super.rightButton;
//	}
//}
