/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.notify;

import javax.swing.*;

/**
 * 图形图标单元
 * 
 * @author scott.liang
 * @version 1.0 12/07/2011
 * @since laxcus 1.0
 */
final class GraphIconCell {

	private Icon icon;

	private String tooltip;

	public GraphIconCell(Icon icon, String tooltip) {
		super();
		this.setIcon(icon);
		this.setTooltip(tooltip);
	}

	public void setIcon(Icon e) {
		icon = e;
	}

	public Icon getIcon() {
		return icon;
	}

	public void setTooltip(String e) {
		tooltip = e;
	}

	public String getTooltip() {
		return tooltip;
	}

}
