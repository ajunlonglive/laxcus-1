/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.border;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * 标题边框
 * 
 * @author scott.liang
 * @version 1.0 2022-7-17
 * @since laxcus 1.0
 */
public class FlatTitledBorder extends TitledBorder {

	private static final long serialVersionUID = 1312049796365916279L;

	/**
	 * @param arg0
	 */
	public FlatTitledBorder(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public FlatTitledBorder(Border arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public FlatTitledBorder(Border arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public FlatTitledBorder(Border arg0, String arg1, int arg2, int arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 */
	public FlatTitledBorder(Border arg0, String arg1, int arg2, int arg3,
			Font arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public FlatTitledBorder(Border arg0, String arg1, int arg2, int arg3,
			Font arg4, Color arg5) {
		super(arg0, arg1, arg2, arg3, arg4, arg5);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.border.TitledBorder#getTitleColor()
	 */
	@Override
	public Color getTitleColor() {
		Color c = UIManager.getColor("Label.foreground");
		if (c == null) {
			c = super.getTitleColor();
		}
		return c;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.border.TitledBorder#getTitleFont()
	 */
	@Override
	public Font getTitleFont() {
		Font font = UIManager.getFont("Label.font");
		if (font == null) {
			font = super.getTitleFont();
		}
		return font;
	}

}
