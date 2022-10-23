/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.awt.Toolkit;

import javax.swing.text.*;

/**
 * 图形字符检测接口。截获和检测文本框的输入字符，只接受“>32 && <127”的图形字符，同时限制输入长度。
 * 
 * @author scott.liang
 * @version 1.0 12/02/2009
 * @see laxcus 1.0
 */
public class GraphDocument extends PlainDocument {

	private static final long serialVersionUID = 1L;

	/** 文本框句柄 **/
	private JTextComponent textComponent;

	/** 输入长度限制 **/
	private int limitSize = 0;

	/**
	 * 构造默认的图形字符检测接口
	 */
	public GraphDocument() {
		super();
	}

	/**
	 * 构造图形字符检测接口
	 * @param arg0
	 */
	public GraphDocument(Content arg0) {
		super(arg0);
	}

	/**
	 * 构造图形字符检测接口，指定全部参数
	 * @param component
	 * @param limit
	 */
	public GraphDocument(JTextComponent component, int limit) {
		this();
		this.textComponent = component;
		this.limitSize = limit;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.text.PlainDocument#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
	 */
	@Override
	public void insertString(int offs, String str, AttributeSet aset) throws BadLocationException {
		if (str == null || str.length() < 1) return;

		for (int i = 0; i < str.length(); i++) {
			char word = str.charAt(i);
			if (word > 32 && word < 127) {
				continue;
			} else {
				Toolkit.getDefaultToolkit().beep(); //发声
				return;
			}
		}

		int size = str.getBytes().length;
		int buffsize = 0;
		if (textComponent != null) {
			String line = textComponent.getText();
			if (line.length() > 0) {
				buffsize = line.getBytes().length;
			}
		}

		// 如果设置了限制长度,那么检查总长度
		if (limitSize > 0 && buffsize + size > limitSize) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		super.insertString(offs, str, aset);
	}

}
