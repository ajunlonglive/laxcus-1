/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.graph;

import javax.swing.*;

/**
 * 图文数据信息<br>
 * 
 * 以图形的方式，显示数据计算或者数据构造，以及其它处理产生的图形，包括对图形的注解、提示。
 * 
 * @author scott.liang
 * @version 1.0 12/07/2011
 * @since laxcus 1.0
 */
public class GraphItem {
	
	/** 图像 **/
	private Icon icon;

	/** 显示文本 **/
	private String text;

	/** 提示文本 **/
	private String tooltip;

	/**
	 * 构造图文数据信息
	 * @param icon 图像
	 * @param text 显示文本
	 * @param tooltip 提示文本
	 */
	public GraphItem(Icon icon, String text, String tooltip) {
		super();
		setIcon(icon);
		setText(text);
		setTooltip(tooltip);
	}

	/**
	 * 设置图像
	 * @param e Icon实例
	 */
	public void setIcon(Icon e) {
		icon = e;
	}

	/**
	 * 返回图像
	 * @return Icon实例
	 */
	public Icon getIcon() {
		return icon;
	}

	/**
	 * 设置提示文本
	 * @param e 提示文本
	 */
	public void setTooltip(String e) {
		tooltip = e;
	}

	/**
	 * 返回提示文本
	 * @return 提示文本
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * 设置显示文本
	 * @param e 显示文本
	 */
	public void setText(String e) {
		text = e;
	}

	/**
	 * 返回显示文本
	 * @return 显示文本
	 */
	public String getText() {
		return text;
	}

}