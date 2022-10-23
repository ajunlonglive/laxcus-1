/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.awt.*;

import javax.swing.*;

/**
 * 表头高度修正类。<br>
 * 
 * 继承自JViewport，配合JScrollPane.setColumnHeader方法，修正表头的高度。<br><br>
 * 
 * 如果采用一般设置表头高度的方法：<br>
 * table.getTableHeader().setPreferredSize(new Dimension(width, height)); <br>
 * 表在显示和拖动的时候会出错！！！<br><br>
 * 
 * @author scott.liang
 * @version 1.0 3/4/2020
 * @since laxcus 1.0
 */
public class HeightViewport extends JViewport {
	
	private static final long serialVersionUID = 1308763687008134328L;

	/** 默认高度 **/
	private int height = 28;

	/**
	 * 表头高度修正类
	 * @param height 指定高度
	 */
	public HeightViewport(int height) {
		super();
		setHeight(height);
	}
	
	/**
	 * 设置表头高度，必须大于0
	 * @param h
	 */
	private void setHeight(int h) {
		if (h >= 0) {
			height = h;
		} else {
			height = 28;
		}
	}

	/*
	 * 注意！关键是这个方法，修正表头的高度
	 * 
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.height = height;
		return d;
	}
	
//	/*
//	 * (non-Javadoc)
//	 * @see java.awt.Component#getFont()
//	 */
//	@Override
//	public Font getFont() {
//		Font font = FontKit.findFont(this.getClass());
//		if(font != null){
//			return font;
//		}
//		return super.getFont();
//	}

}