/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.cycle;

import java.awt.*;

import javax.swing.*;

/**
 * 自动调整位置的标签
 * 
 * @author scott.liang
 * @version 1.0 2/12/2022
 * @since laxcus 1.0
 */
final class CycleLabel extends JLabel {

	private static final long serialVersionUID = 1L;

	/**
	 * 构造默认的标签
	 */
	public CycleLabel() {
		super();
		// 居中
		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JLabel#setText(java.lang.String)
	 */
	@Override
	public void setText(String str) {
		if (str == null || str.length() == 0) {
			super.setText("");
			return;
		}

		// 字符串
		FontMetrics fm = getFontMetrics(getFont());
		int fontWidth = fm.stringWidth(str);

		// 标签的长度
		int width = getWidth();
		if (fontWidth + 8 >= width) {
			if (getHorizontalAlignment() != SwingConstants.LEFT) {
				setHorizontalAlignment(SwingConstants.LEFT);
			}
		} else {
			if (getHorizontalAlignment() != SwingConstants.CENTER) {
				setHorizontalAlignment(SwingConstants.CENTER);
			}
		}

		// 显示
		super.setText(str);
	}

	//	/**
	//	 * @param arg0
	//	 */
	//	public AutoLabel(String arg0) {
	//		super(arg0);
	//		// TODO Auto-generated constructor stub
	//	}
	//
	//	/**
	//	 * @param arg0
	//	 */
	//	public AutoLabel(Icon arg0) {
	//		super(arg0);
	//		// TODO Auto-generated constructor stub
	//	}
	//
	//	/**
	//	 * @param arg0
	//	 * @param arg1
	//	 */
	//	public AutoLabel(String arg0, int arg1) {
	//		super(arg0, arg1);
	//		// TODO Auto-generated constructor stub
	//	}
	//
	//	/**
	//	 * @param arg0
	//	 * @param arg1
	//	 */
	//	public AutoLabel(Icon arg0, int arg1) {
	//		super(arg0, arg1);
	//		// TODO Auto-generated constructor stub
	//	}
	//
	//	/**
	//	 * @param arg0
	//	 * @param arg1
	//	 * @param arg2
	//	 */
	//	public AutoLabel(String arg0, Icon arg1, int arg2) {
	//		super(arg0, arg1, arg2);
	//		// TODO Auto-generated constructor stub
	//	}


}
