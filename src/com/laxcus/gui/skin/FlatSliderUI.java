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

/**
 *
 * @author scott.liang
 * @version 1.0 2021-10-3
 * @since laxcus 1.0
 */
public class FlatSliderUI extends MetalSliderUI {

	private static Icon SAFE_HORIZ_THUMB_ICON;

	private static Icon SAFE_VERT_THUMB_ICON;

	public static ComponentUI createUI(JComponent c) {
		return new FlatSliderUI();
	}

	public FlatSliderUI() {
		super();
	}
    
	public void installUI(JComponent c) {
		super.installUI(c);

		horizThumbIcon = SAFE_HORIZ_THUMB_ICON = UIManager
				.getIcon("MetalHorizontalThumbIcon");
		vertThumbIcon = SAFE_VERT_THUMB_ICON = UIManager
				.getIcon("MetalVerticalThumbIcon");
	}
   
    private static Icon getHorizThumbIcon() {
        if (System.getSecurityManager() != null) {
            return SAFE_HORIZ_THUMB_ICON;
        } else {
            return horizThumbIcon;
        }
    }

    private static Icon getVertThumbIcon() {
        if (System.getSecurityManager() != null) {
            return SAFE_VERT_THUMB_ICON;
        } else {
            return vertThumbIcon;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see javax.swing.plaf.metal.MetalSliderUI#paintThumb(java.awt.Graphics)
     */
    @Override
	public void paintThumb(Graphics g) {
		Rectangle knobBounds = thumbRect;

		g.translate(knobBounds.x, knobBounds.y);

		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			getHorizThumbIcon().paintIcon(slider, g, 0, 0);
		} else {
			getVertThumbIcon().paintIcon(slider, g, 0, 0);
		}

		g.translate(-knobBounds.x, -knobBounds.y);
	}

}
