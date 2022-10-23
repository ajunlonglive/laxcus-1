/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.notify;

/**
 * 图形文本单元
 * 
 * @author scott.liang
 * @version 1.0 12/07/2011
 * @since laxcus 1.0
 */
final class GraphTextCell {

	private String text;

	private String tooltip;

	public GraphTextCell(String text, String tooltip) {
		super();
		setText(text);
		setTooltip(tooltip);
	}

	public void setText(String e) {
		text = e;
	}

	public String getText() {
		return text;
	}

	public void setTooltip(String e) {
		tooltip = e;
	}

	public String getTooltip() {
		return tooltip;
	}


}
