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
 * 数字检测接口。截获和检测文本框的输入字符，只接受数字类型，不包括正负符号，同时限制输入长度。
 * 
 * @author scott.liang
 * @version 1.0 12/02/2009
 * @since laxcus 1.0
 */
public class DigitDocument extends PlainDocument {

	private static final long serialVersionUID = 1L;

	/** 文本组件 **/
	private JTextComponent textComponent;
	
	/** 长度限制，默认是0，不限度 **/
	private int limitSize = 0;

	/**
	 * 构造默认的数字检测接口
	 */
	public DigitDocument() {
		super();
	}

	/**
	 * 构造数字检测接口，指定参数
	 * @param component
	 * @param limit
	 */
	public DigitDocument(JTextComponent component, int limit) {
		this();
		textComponent = component;
		limitSize = limit;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.text.PlainDocument#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
	 */
	@Override
	public void insertString(int offs, String str, AttributeSet aset) throws BadLocationException {
		if (str == null || str.length() < 1) {
			return;
		}

		StringBuilder buf = new StringBuilder();
		if (textComponent != null) {
			String line = textComponent.getText();
			if (line.length() > 0) {
				buf.append(line);
			}
		}
		// 插入到指定位置
		if (offs >= buf.length()) {
			buf.append(str);
		} else {
			buf.insert(offs, str);
		}

		int len = buf.length();
		// 判断长度在规定范围内
		if (!(0 <= limitSize && len <= limitSize)) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		// 计算每个字符
		for (int i = 0; i < len; i++) {
			char word = buf.charAt(i);
			if (word >= '0' && word <= '9') {
				continue;
			} else {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
		}

		// 调用超类
		super.insertString(offs, str, aset);
	}
	
//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.text.PlainDocument#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
//	 */
//	@Override
//	public void insertString(int offs, String str, AttributeSet aset)
//			throws BadLocationException {
//		if (str == null || str.length() < 1) {
//			return;
//		}
//
//		int buffsize = 0;
//		if (textComponent != null) {
//			String line = textComponent.getText();
//			if (line.length() > 0) {
//				buffsize = line.getBytes().length;
//			}
//		}
//
//		for (int i = 0; i < str.length(); i++) {
//			char word = str.charAt(i);
//			if (word >= '0' && word <= '9') {
//				continue;
//			} else {
//				if (word == '-' && i == 0 && buffsize == 0) {
//					continue;
//				}
//				Toolkit.getDefaultToolkit().beep();
//				return;
//			}
//		}
//
//		int size = str.getBytes().length;
//		if (limitSize > 0 && buffsize + size > limitSize) {
//			Toolkit.getDefaultToolkit().beep();
//			return;
//		}
//
//		super.insertString(offs, str, aset);
//	}

}