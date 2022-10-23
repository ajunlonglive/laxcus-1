/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.awt.*;

import javax.swing.table.*;

/**
 * 表头高度修正类。<br>
 * 
 * 继承自JTableHeader，配合JTable.setTableHeader(new JTableHeader())方法，修正表头的高度。<br><br>
 * 
 * 如果采用一般设置表头高度的方法：<br>
 * table.getTableHeader().setPreferredSize(new Dimension(width, height)); <br>
 * 表在显示和拖动的时候会出错！！！<br><br>
 * 
 * @author scott.liang
 * @version 1.0 3/4/2020
 * @since laxcus 1.0
 */
public class HeightTableHeader extends JTableHeader {

	private static final long serialVersionUID = 489784793331470550L;
	
	/** 默认高度 **/
	private int height = 28;

	/**
	 * 表头高度修正类，指定模型和表头高度
	 * @param model 列模型
	 * @param height 表头高度
	 */
	public HeightTableHeader(TableColumnModel model, int height) {
		super(model);
		setHeight(height);
	}

	/**
	 * 设置表头高度，必须大于0
	 * @param h 高度
	 */
	private void setHeight(int h) {
		if (h > 0) {
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
}
