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
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;

/**
 * 分隔面板UI
 * 
 * @author scott.liang
 * @version 1.0 10/2/2021
 * @since laxcus 1.0
 */
public class FlatSplitPaneUI extends MetalSplitPaneUI {
	
	/**
	 * 空的边框，绘制由子类实现
	 *
	 * @author scott.liang
	 * @version 1.0 7/3/2022
	 * @since laxcus 1.0
	 */
	public static class EmptySplitPaneBorder implements Border, UIResource {

		public EmptySplitPaneBorder() {
			super();
		}
		
//		private JComponent getChild(Component child) {
//			if(child != null && Laxkit.isClassFrom(child, JComponent.class)) {
//				return (JComponent)child;
//			}
//			return null;
//		}
		
//		private void paintChild(Component c, Graphics g) {
//			JComponent child = getChild(c);
//			if (child != null) {
////				child.updateUI();
////				child.repaint();
//				
//				SwingUtilities.updateComponentTreeUI(child);
//				
////				Rectangle bound = child.getBounds();
////				Border b = child.getBorder();
////				if (b != null) {
////					b.paintBorder(child, g, 0, 0, bound.width, bound.height);
//////					System.out.println("paint child border");
////				}
//			}
//		}

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			//			JComponent          child;
			//			Rectangle          bound;

//			JSplitPane jsp = (JSplitPane) c;
//			paintChild(jsp.getLeftComponent(), g);
//			paintChild(jsp.getRightComponent(), g);

			////			child = getChild( jsp.getLeftComponent());
			//			if (jsp.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
			//				paintChild(jsp.getLeftComponent(), g);
			//				paintChild(jsp.getRightComponent(), g);
			//				
			////				if (child != null) {
			////					bound = child.getBounds();
			////					Border b = child.getBorder();
			////					if (b != null) {
			////						b.paintBorder(child, g, 0, 0, bound.width, bound.height);
			////					}
			////				}
			////				child = getChild( jsp.getRightComponent());
			////				if (child != null) {
			////
			////				}
			//			} else {
			//				if (child != null) {
			//
			//				}
			//				child = jsp.getRightComponent();
			//				if (child != null) {
			//
			//				}
			//			}
		}

		public Insets getBorderInsets(Component c) {
			return new Insets(0, 0, 0, 0);
		}

		public boolean isBorderOpaque() {
			return true;
		}
	}

//    /**
//     * Draws the border around the splitpane. To work correctly you shoudl
//     * also install a border on the divider (property SplitPaneDivider.border).
//     */
//	public static class FlatSplitPaneBorder implements Border, UIResource {
//		protected Color highlight;
//
//		protected Color shadow;
//
//		public FlatSplitPaneBorder(Color highlight, Color shadow) {
//			this.highlight = highlight;
//			this.shadow = shadow;
//		}
//
//		public FlatSplitPaneBorder() {
//			super();
//			highlight = Color.red; // MetalLookAndFeel.getControlHighlight();
//			shadow = Color.GREEN; // MetalLookAndFeel.getControlDarkShadow();
//		}
//
//		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
//			// The only tricky part with this border is that the divider is
//			// not positioned at the top (for horizontal) or left (for vert),
//			// so this border draws to where the divider is:
//			// -----------------
//			// |xxxxxxx xxxxxxx|
//			// |x     ---     x|
//			// |x     |	|     x|
//			// |x     |D|     x|
//			// |x     | |     x|
//			// |x     ---     x|
//			// |xxxxxxx xxxxxxx|
//			// -----------------
//			// The above shows (rather excessively) what this looks like for
//			// a horizontal orientation. This border then draws the x's, with
//			// the SplitPaneDividerBorder drawing its own border.
//
//			Component          child;
//			Rectangle          cBounds;
//
//			JSplitPane splitPane = (JSplitPane)c;
//
//			child = splitPane.getLeftComponent();
//			// This is needed for the space between the divider and end of splitpane.
//			g.setColor(c.getBackground());
//			
////			g.setColor(Color.BLACK);
//			g.drawRect(x, y, width - 1, height - 1);
//			
//			if(splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
//				if(child != null) {
//					cBounds = child.getBounds();
//					g.setColor(shadow);
//					g.drawLine(0, 0, cBounds.width + 1, 0);
//					g.drawLine(0, 1, 0, cBounds.height + 1);
//
//					g.setColor(highlight);
//					g.drawLine(0, cBounds.height + 1, cBounds.width + 1, cBounds.height + 1);
//				}
//				child = splitPane.getRightComponent();
//				if(child != null) {
//					cBounds = child.getBounds();
//
//					int             maxX = cBounds.x + cBounds.width;
//					int             maxY = cBounds.y + cBounds.height;
//
//					g.setColor(shadow);
//					g.drawLine(cBounds.x - 1, 0, maxX, 0);
//					g.setColor(highlight);
//					g.drawLine(cBounds.x - 1, maxY, maxX, maxY);
//					g.drawLine(maxX, 0, maxX, maxY + 1);
//				}
//			} else {
//				if(child != null) {
//					cBounds = child.getBounds();
//					g.setColor(shadow);
//					g.drawLine(0, 0, cBounds.width + 1, 0);
//					g.drawLine(0, 1, 0, cBounds.height);
//					g.setColor(highlight);
//					g.drawLine(1 + cBounds.width, 0, 1 + cBounds.width,
//							cBounds.height + 1);
//					g.drawLine(0, cBounds.height + 1, 0, cBounds.height + 1);
//				}
//				child = splitPane.getRightComponent();
//				if(child != null) {
//					cBounds = child.getBounds();
//
//					int             maxX = cBounds.x + cBounds.width;
//					int             maxY = cBounds.y + cBounds.height;
//
//					g.setColor(shadow);
//					g.drawLine(0, cBounds.y - 1, 0, maxY);
//					g.drawLine(maxX, cBounds.y - 1, maxX, cBounds.y - 1);
//					g.setColor(highlight);
//					g.drawLine(0, maxY, cBounds.width + 1, maxY);
//					g.drawLine(maxX, cBounds.y, maxX, maxY);
//				}
//			}
//		}
//
//		public Insets getBorderInsets(Component c) {
////			return new Insets(1, 1, 1, 1);
//			
//			return new Insets(0,0,0,0);
//		}
//
//		public boolean isBorderOpaque() {
//			return true;
//		}
//	}
    
//	/**
//	 * Draws the border around the splitpane. To work correctly you shoudl
//	 * also install a border on the divider (property SplitPaneDivider.border).
//	 */
//	public class SplitPaneBorder implements Border, UIResource {
//		protected Color highlight;
//		protected Color shadow;
//
//		public SplitPaneBorder(Color h, Color s) {
//			super();
//			highlight = h;
//			shadow = s;
//		}
//
//		public SplitPaneBorder() {
//			super();
//			highlight = MetalLookAndFeel.getControlHighlight();
//			shadow = MetalLookAndFeel.getControlDarkShadow();
//		}
//
//		//		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
//		//			// The only tricky part with this border is that the divider is
//		//			// not positioned at the top (for horizontal) or left (for vert),
//		//			// so this border draws to where the divider is:
//		//			// -----------------
//		//			// |xxxxxxx xxxxxxx|
//		//			// |x     ---     x|
//		//			// |x     |	|     x|
//		//			// |x     |D|     x|
//		//			// |x     | |     x|
//		//			// |x     ---     x|
//		//			// |xxxxxxx xxxxxxx|
//		//			// -----------------
//		//			// The above shows (rather excessively) what this looks like for
//		//			// a horizontal orientation. This border then draws the x's, with
//		//			// the SplitPaneDividerBorder drawing its own border.
//		//
//		//			Component          child;
//		//			Rectangle          cBounds;
//		//
//		//			JSplitPane splitPane = (JSplitPane)c;
//		//
//		//			child = splitPane.getLeftComponent();
//		//			// This is needed for the space between the divider and end of splitpane.
//		//			g.setColor(c.getBackground());
//		//			
//		//			// 无边框
//		//			if (!FlatUtil.isNotBorder(splitPane)) {
//		//				g.drawRect(x, y, width - 1, height - 1);
//		//			}
//		//			
//		//			if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
//		////				if (child != null) {
//		////					cBounds = child.getBounds();
//		////					g.setColor(shadow);
//		////					g.drawLine(0, 0, cBounds.width + 1, 0);
//		////					g.drawLine(0, 1, 0, cBounds.height + 1);
//		////
//		////					g.setColor(highlight);
//		////					g.drawLine(0, cBounds.height + 1, cBounds.width + 1, cBounds.height + 1);
//		////				}
//		////				child = splitPane.getRightComponent();
//		////				if (child != null) {
//		////					cBounds = child.getBounds();
//		////
//		////					int maxX = cBounds.x + cBounds.width;
//		////					int maxY = cBounds.y + cBounds.height;
//		////
//		////					g.setColor(shadow);
//		////					g.drawLine(cBounds.x - 1, 0, maxX, 0);
//		////					g.setColor(highlight);
//		////					g.drawLine(cBounds.x - 1, maxY, maxX, maxY);
//		////					g.drawLine(maxX, 0, maxX, maxY + 1);
//		////				}
//		//			} else {
//		////				if (child != null) {
//		////					cBounds = child.getBounds();
//		////					g.setColor(shadow);
//		////					g.drawLine(0, 0, cBounds.width + 1, 0);
//		////					g.drawLine(0, 1, 0, cBounds.height);
//		////					g.setColor(highlight);
//		////					g.drawLine(1 + cBounds.width, 0, 1 + cBounds.width, cBounds.height + 1);
//		////					g.drawLine(0, cBounds.height + 1, 0, cBounds.height + 1);
//		////				}
//		////				child = splitPane.getRightComponent();
//		////				if (child != null) {
//		////					cBounds = child.getBounds();
//		////
//		////					int maxX = cBounds.x + cBounds.width;
//		////					int maxY = cBounds.y + cBounds.height;
//		////
//		////					g.setColor(shadow);
//		////					g.drawLine(0, cBounds.y - 1, 0, maxY);
//		////					g.drawLine(maxX, cBounds.y - 1, maxX, cBounds.y - 1);
//		////					g.setColor(highlight);
//		////					g.drawLine(0, maxY, cBounds.width + 1, maxY);
//		////					g.drawLine(maxX, cBounds.y, maxX, maxY);
//		////				}
//		//			}
//		//		}
//
//		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
//			if (FlatUtil.isNotBorder(c)) {
//				return;
//			}
//
//			// The only tricky part with this border is that the divider is
//			// not positioned at the top (for horizontal) or left (for vert),
//			// so this border draws to where the divider is:
//			// -----------------
//			// |xxxxxxx xxxxxxx|
//			// |x     ---     x|
//			// |x     |	|     x|
//			// |x     |D|     x|
//			// |x     | |     x|
//			// |x     ---     x|
//			// |xxxxxxx xxxxxxx|
//			// -----------------
//			// The above shows (rather excessively) what this looks like for
//			// a horizontal orientation. This border then draws the x's, with
//			// the SplitPaneDividerBorder drawing its own border.
//
//			Component          child;
//			Rectangle          cBounds;
//
//			JSplitPane splitPane = (JSplitPane) c;
//
//			child = splitPane.getLeftComponent();
//			// This is needed for the space between the divider and end of splitpane.
//			g.setColor(c.getBackground());
//
//			g.drawRect(x, y, width - 1, height - 1);
//
//			if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
//				if (child != null) {
//					cBounds = child.getBounds();
//					g.setColor(shadow);
//					g.drawLine(0, 0, cBounds.width + 1, 0);
//					g.drawLine(0, 1, 0, cBounds.height + 1);
//
//					g.setColor(highlight);
//					g.drawLine(0, cBounds.height + 1, cBounds.width + 1, cBounds.height + 1);
//				}
//				child = splitPane.getRightComponent();
//				if (child != null) {
//					cBounds = child.getBounds();
//
//					int maxX = cBounds.x + cBounds.width;
//					int maxY = cBounds.y + cBounds.height;
//
//					g.setColor(shadow);
//					g.drawLine(cBounds.x - 1, 0, maxX, 0);
//					g.setColor(highlight);
//					g.drawLine(cBounds.x - 1, maxY, maxX, maxY);
//					g.drawLine(maxX, 0, maxX, maxY + 1);
//				}
//			} else {
//				if (child != null) {
//					cBounds = child.getBounds();
//					g.setColor(shadow);
//					g.drawLine(0, 0, cBounds.width + 1, 0);
//					g.drawLine(0, 1, 0, cBounds.height);
//					g.setColor(highlight);
//					g.drawLine(1 + cBounds.width, 0, 1 + cBounds.width, cBounds.height + 1);
//					g.drawLine(0, cBounds.height + 1, 0, cBounds.height + 1);
//				}
//				child = splitPane.getRightComponent();
//				if (child != null) {
//					cBounds = child.getBounds();
//
//					int maxX = cBounds.x + cBounds.width;
//					int maxY = cBounds.y + cBounds.height;
//
//					g.setColor(shadow);
//					g.drawLine(0, cBounds.y - 1, 0, maxY);
//					g.drawLine(maxX, cBounds.y - 1, maxX, cBounds.y - 1);
//					g.setColor(highlight);
//					g.drawLine(0, maxY, cBounds.width + 1, maxY);
//					g.drawLine(maxX, cBounds.y, maxX, maxY);
//				}
//			}
//		}
//		
//		public void paintBorderFUCK(Component c, Graphics g, int x, int y, int width, int height) {
//			
//
//			// The only tricky part with this border is that the divider is
//			// not positioned at the top (for horizontal) or left (for vert),
//			// so this border draws to where the divider is:
//			// -----------------
//			// |xxxxxxx xxxxxxx|
//			// |x     ---     x|
//			// |x     |	|     x|
//			// |x     |D|     x|
//			// |x     | |     x|
//			// |x     ---     x|
//			// |xxxxxxx xxxxxxx|
//			// -----------------
//			// The above shows (rather excessively) what this looks like for
//			// a horizontal orientation. This border then draws the x's, with
//			// the SplitPaneDividerBorder drawing its own border.
//
//			Component          child;
//			Rectangle          cBounds;
//
//			JSplitPane splitPane = (JSplitPane) c;
//
//			child = splitPane.getLeftComponent();
//			// This is needed for the space between the divider and end of splitpane.
//			
//			// 允许出现边框时...
////			if (!FlatUtil.isNotBorder(c)) {
//				g.setColor(c.getBackground());
//				g.setColor(Color.BLUE);
////				g.drawRect(x, y, width - 1, height - 1);
////			}
//
//			if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
//				if (child != null) {
////					((JComponent)child).updateUI();
//					
////					cBounds = child.getBounds();
////					g.setColor(Color.RED);
////					g.drawRect(1 , 1, cBounds.width + 1, cBounds.height + 1);
//////					System.out.println("FUCK 1");
//					
////					g.setColor(shadow);
////					g.drawLine(0, 0, cBounds.width + 1, 0);
////					g.drawLine(0, 1, 0, cBounds.height + 1);
////
////					g.setColor(highlight);
////					g.drawLine(0, cBounds.height + 1, cBounds.width + 1, cBounds.height + 1);
//				}
//				child = splitPane.getRightComponent();
//				if (child != null) {
////					((JComponent)child).updateUI();
//					
////					cBounds = child.getBounds();
////
////					int maxX = cBounds.x + cBounds.width;
////					int maxY = cBounds.y + cBounds.height;
////
////					g.setColor(Color.RED);
////					g.drawRect(cBounds.x - 1, 0, maxX, maxY);
//					
////					System.out.println("FUCK 2");
//					
////					g.setColor(shadow);
////					g.drawLine(cBounds.x - 1, 0, maxX, 0);
////					g.setColor(highlight);
////					g.drawLine(cBounds.x - 1, maxY, maxX, maxY);
////					g.drawLine(maxX, 0, maxX, maxY + 1);
//				}
//			} else {
//				if (child != null) {
////					((JComponent)child).updateUI();
//					
////					cBounds = child.getBounds();
////					
////					g.setColor(Color.RED);
////					g.drawRect(0, 0, cBounds.width + 1, cBounds.height + 1);
//					
////					g.setColor(shadow);
////					g.drawLine(0, 0, cBounds.width + 1, 0);
////					g.drawLine(0, 1, 0, cBounds.height);
////					g.setColor(highlight);
////					g.drawLine(1 + cBounds.width, 0, 1 + cBounds.width, cBounds.height + 1);
////					g.drawLine(0, cBounds.height + 1, 0, cBounds.height + 1);
//				}
//				child = splitPane.getRightComponent();
//				if (child != null) {
////					((JComponent)child).updateUI();
//					
////					cBounds = child.getBounds();
////
////					int maxX = cBounds.x + cBounds.width;
////					int maxY = cBounds.y + cBounds.height;
////					
////					g.setColor(Color.RED);
////					
////					g.drawRect(0, cBounds.y - 1, maxX, maxY);
//
////					g.setColor(shadow);
////					g.drawLine(0, cBounds.y - 1, 0, maxY);
////					g.drawLine(maxX, cBounds.y - 1, maxX, cBounds.y - 1);
////					g.setColor(highlight);
////					g.drawLine(0, maxY, cBounds.width + 1, maxY);
////					g.drawLine(maxX, cBounds.y, maxX, maxY);
//				}
//			}
//		}
//
//		@Override
//		public Insets getBorderInsets(Component c) {
////			// 边框
////			if (FlatUtil.isNotBorder(c)) {
////				return new Insets(0, 0, 0, 0);
////			} else {
////				return new Insets(1, 1, 1, 1);
////			}
//			
////			return new Insets(6,6,6,6);
//				
//				return new Insets(0, 0, 0, 0);
//		}
//
//		public boolean isBorderOpaque() {
//			return true;
//		}
//	}

	/**
	 * 
	 */
	public FlatSplitPaneUI() {
		super();
	}

	/**
	 * Creates a new MetalSplitPaneUI instance
	 */
	public static ComponentUI createUI(JComponent x) {
		return new FlatSplitPaneUI();
	}

	/**
	 * Creates the default divider.
	 */
	public BasicSplitPaneDivider createDefaultDivider() {
		return new FlatSplitPaneDivider(this);
	}

	protected void installDefaults(){ 
		super.installDefaults();

		// 重新设置边框
		splitPane.setBorder(new EmptySplitPaneBorder());
		
		divider.setBorder(new EmptyBorder(0, 0, 0, 0));
	}
	
//	protected void installDefaults(){ 
//		super.installDefaults();
//
//		// 重新设置边框
//		splitPane.setBorder(new SplitPaneBorder());
//		
////		Border b = divider.getBorder();
////		System.out.printf("divider class border:%s\n", (b != null ? b
////				.getClass().getName() : "Null"));
//
//		// 重置设置分隔符边框，默认的边框是BasicBorder.SplitPaneDividerBorder
//		divider.setBorder(new EmptyBorder(0, 0, 0, 0));
//
//		//		System.out.println("THIS IS SPLIT PANE!");
//
//		//		Border b = splitPane.getBorder();
//		//		System.out.printf("SplitPane Border is %s\n" , (b != null ? b.getClass().getName() : "Null"));
//	}

}