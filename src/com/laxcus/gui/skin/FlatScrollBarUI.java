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
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 滚动条外观
 * 
 * @author scott.liang
 * @version 1.0 10/2/2021
 * @since laxcus 1.0
 */
public class FlatScrollBarUI extends MetalScrollBarUI {

//	private static Color shadowColor;
	private static Color highlightColor;
	private static Color darkShadowColor;

	// private static Color shadowColor;
	// private static Color highlightColor;
	// private static Color darkShadowColor;

	private static Color thumbColor;

//	private static Color thumbShadow;

//	private static Color thumbHighlightColor;

	/**
	 * 
	 */
	public FlatScrollBarUI() {
		super();
	}

	public static ComponentUI createUI(JComponent c) {
		return new FlatScrollBarUI();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicScrollBarUI#installUI(javax.swing.JComponent)
	 */
	@Override
	public void installUI(JComponent c)   {
		super.installUI(c);
	}
	
	/**
	 * Returns the view that represents the decrease view.
	 */
	protected JButton createDecreaseButton(int orientation) {
		decreaseButton = new FlatScrollButton(orientation, scrollBarWidth,
				isFreeStanding);
		return decreaseButton;
	}

	/** Returns the view that represents the increase view. */
	protected JButton createIncreaseButton(int orientation) {
		increaseButton = new FlatScrollButton(orientation, scrollBarWidth,
				isFreeStanding);
		return increaseButton;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalScrollBarUI#configureScrollBarColors()
	 */
	@Override
	protected void configureScrollBarColors() {
		super.configureScrollBarColors();
		// shadowColor = UIManager.getColor("ScrollBar.shadow");
		highlightColor = UIManager.getColor("ScrollBar.highlight");
		darkShadowColor = UIManager.getColor("ScrollBar.darkShadow");

		thumbColor = UIManager.getColor("ScrollBar.thumb");
		// thumbShadow = UIManager.getColor("ScrollBar.thumbShadow");
		// thumbHighlightColor = UIManager.getColor("ScrollBar.thumbHighlight");

		// 重新定义把手的颜色值
		if (Skins.isGraySkin()) {
			// ESL esl = new ESL(thumbColor);
			// thumbColor = esl.toBrighter(10).toColor();
		} else {
			int value = 32;
			if (Skins.isBronzSkin()) {
				value = 52;
			} else if (Skins.isCyanoSkin()) {
				value = 26;
			} else if (Skins.isDarkSkin()) {
				value = 26;
			}
			ESL esl = new ESL(thumbColor);
			thumbColor = esl.toBrighter(value).toColor();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalScrollBarUI#paintThumb(java.awt.Graphics, javax.swing.JComponent, java.awt.Rectangle)
	 */
	@Override
	protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
		if (!c.isEnabled()) {
			return;
		}

		// if (MetalLookAndFeel.usingOcean()) {
		// oceanPaintThumb(g, c, thumbBounds);
		// return;
		// }

		boolean leftToRight = FlatUtil.isLeftToRight(c);

		g.translate(thumbBounds.x, thumbBounds.y);

		if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
			if (!isFreeStanding) {
				thumbBounds.width += 2;
				if (!leftToRight) {
					g.translate(-1, 0);
				}
			}

			g.setColor(thumbColor);
			g.fillRect(0, 0, thumbBounds.width - 2, thumbBounds.height);
//			g.fillRect(0, 0, thumbBounds.width - 2, thumbBounds.height - 1);

//			g.setColor(thumbShadow);
//			g.drawRect(0, 0, thumbBounds.width - 2, thumbBounds.height - 1);

//			g.setColor(thumbHighlightColor);
//			g.drawLine(1, 1, thumbBounds.width - 3, 1);
//			g.drawLine(1, 1, 1, thumbBounds.height - 2);

//			// 不要绘制把手的图标
//			 bumps.setBumpArea( thumbBounds.width - 6, thumbBounds.height - 7);
//			 bumps.paintIcon( c, g, 3, 4 );

			if (!isFreeStanding) {
				thumbBounds.width -= 2;
				if (!leftToRight) {
					g.translate(1, 0);
				}
			}
		} 
		else // HORIZONTAL
		{
			if (!isFreeStanding) {
				thumbBounds.height += 2;
			}

			g.setColor(thumbColor);
//			g.fillRect(0, 0, thumbBounds.width - 1, thumbBounds.height - 2);
			g.fillRect(0, 0, thumbBounds.width, thumbBounds.height - 2);

//			g.setColor(thumbShadow);
//			g.drawRect(0, 0, thumbBounds.width - 1, thumbBounds.height - 2);

//			g.setColor(thumbHighlightColor);
//			g.drawLine(1, 1, thumbBounds.width - 3, 1);
//			g.drawLine(1, 1, 1, thumbBounds.height - 3);

			// bumps.setBumpArea( thumbBounds.width - 7, thumbBounds.height - 6
			// );
			// bumps.paintIcon( c, g, 4, 3 );

			if (!isFreeStanding) {
				thumbBounds.height -= 2;
			}
		}

		g.translate(-thumbBounds.x, -thumbBounds.y);
	}
	
//	boolean isLeftToRight(Component c) {
//		return c.getComponentOrientation().isLeftToRight();
//	}

	void drawDisabledBorder(Graphics g, int x, int y, int w, int h) {
		g.translate(x, y);
		g.setColor(MetalLookAndFeel.getControlShadow());
		g.drawRect(0, 0, w - 1, h - 1);
		g.translate(-x, -y);
	}
	
	protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
		g.translate(trackBounds.x, trackBounds.y);

		boolean leftToRight = FlatUtil.isLeftToRight(c);

		if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
			if (!isFreeStanding) {
				trackBounds.width += 2;
				if (!leftToRight) {
					g.translate(-1, 0);
				}
			}

			if (c.isEnabled()) {
				g.setColor(darkShadowColor);
				g.drawLine( 0, 0, 0, trackBounds.height - 1 ); // left
				g.drawLine( trackBounds.width - 2, 0, trackBounds.width - 2, trackBounds.height - 1 ); // right
				
//				g.drawLine( 2, trackBounds.height - 1, trackBounds.width - 1, trackBounds.height - 1);
//				g.drawLine( 2, 0, trackBounds.width - 2, 0 );

//				g.setColor( shadowColor );
//				//	g.setColor( Color.red);
//				g.drawLine( 1, 1, 1, trackBounds.height - 2 );
//				g.drawLine( 1, 1, trackBounds.width - 3, 1 );
//				if (scrollbar.getValue() != scrollbar.getMaximum()) {  // thumb shadow
//					int y = thumbRect.y + thumbRect.height - trackBounds.y;
//					g.drawLine( 1, y, trackBounds.width-1, y);
//				}
				g.setColor(highlightColor);
				g.drawLine( trackBounds.width - 1, 0, trackBounds.width - 1, trackBounds.height - 1 );
			} else {
				drawDisabledBorder(g, 0, 0, trackBounds.width, trackBounds.height );
			}

			if ( !isFreeStanding ) {
				trackBounds.width -= 2;
				if ( !leftToRight ) {
					g.translate( 1, 0 );
				}
			}
		}
		else  // HORIZONTAL
		{
			if ( !isFreeStanding ) {
				trackBounds.height += 2;
			}

			if (c.isEnabled()) {
				g.setColor(darkShadowColor);
				g.drawLine( 0, 0, trackBounds.width - 1, 0 );  // top
				g.drawLine( 0, trackBounds.height - 2, trackBounds.width - 1, trackBounds.height - 2 ); // bottom
				
//				g.drawLine( 0, 2, 0, trackBounds.height - 2 ); // left
//				g.drawLine( trackBounds.width - 1, 2, trackBounds.width - 1, trackBounds.height - 1 ); // right

//				g.setColor( shadowColor );
//				//	g.setColor( Color.red);
//				g.drawLine( 1, 1, trackBounds.width - 2, 1 );  // top
//				g.drawLine( 1, 1, 1, trackBounds.height - 3 ); // left
//				g.drawLine( 0, trackBounds.height - 1, trackBounds.width - 1, trackBounds.height - 1 ); // bottom
//				if (scrollbar.getValue() != scrollbar.getMaximum()) {  // thumb shadow
//					int x = thumbRect.x + thumbRect.width - trackBounds.x;
//					g.drawLine( x, 1, x, trackBounds.height-1);
//				}
			} else {
				drawDisabledBorder(g, 0, 0, trackBounds.width, trackBounds.height );
			}

			if ( !isFreeStanding ) {
				trackBounds.height -= 2;
			}
		}

        g.translate( -trackBounds.x, -trackBounds.y );
    }
	
	   /**
     * This is overridden only to increase the invalid area.  This
     * ensures that the "Shadow" below the thumb is invalidated
     */
	protected void setThumbBounds(int x, int y, int width, int height) {
		/*
		 * If the thumbs bounds haven't changed, we're done.
		 */
		if ((thumbRect.x == x) && (thumbRect.y == y)
				&& (thumbRect.width == width) && (thumbRect.height == height)) {
			return;
		}

		/*
		 * Update thumbRect, and repaint the union of x,y,w,h and the old
		 * thumbRect.
		 */
		int minX = Math.min(x, thumbRect.x);
		int minY = Math.min(y, thumbRect.y);
		int maxX = Math.max(x + width, thumbRect.x + thumbRect.width);
		int maxY = Math.max(y + height, thumbRect.y + thumbRect.height);

		thumbRect.setBounds(x, y, width , height );
		scrollbar.repaint(minX, minY, (maxX - minX) + 1, (maxY - minY) + 1);
	}

	
}