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
 * 浮点数检测接口。截获和检测文本框的输入字符，只接受数字类型，同时限制输入长度。
 * 
 * @author scott.liang
 * @version 1.0 7/30/2020
 * @since laxcus 1.0
 */
public class FloatDocument extends PlainDocument {

	private static final long serialVersionUID = 1L;

	/** 文本组件 **/
	private JTextComponent textComponent;
	
	/** 长度限制，默认是0，不限度 **/
	private int limitSize = 0;

	/**
	 * 构造默认的浮点数检测接口
	 */
	public FloatDocument() {
		super();
	}

	/**
	 * 构造浮点数检测接口，指定文本
	 * @param arg0
	 */
	public FloatDocument(Content arg0) {
		super(arg0);
	}

	/**
	 * 构造浮点数检测接口，指定参数
	 * @param component
	 * @param limit
	 */
	public FloatDocument(JTextComponent component, int limit) {
		this();
		textComponent = component;
		limitSize = limit;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.text.PlainDocument#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
	 */
	@Override
	public void insertString(int offs, String str, AttributeSet aset)
			throws BadLocationException {
		if (str == null || str.length() < 1) {
			return;
		}
		
		StringBuilder buf = new StringBuilder();
		// 保存参数
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
		// 判断长度
		if (!(0 <= limitSize && len <= limitSize)) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		int point = 0; // 小数点
		for (int i = 0; i < len; i++) {
			char word = buf.charAt(i);
			
			if (i == 0 && (word == '-' || word == '+')) {
				continue;
			}
			// 符号
			if (word >= '0' && word <= '9') {
				continue;
			}
			
			if (i > 0 && word == '.') {
				point++;
				// 大于1是错误，忽略!
				if (point > 1) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				continue;
			}
			
			
			else {
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
//		int point = 0;
//
//		for (int i = 0; i < str.length(); i++) {
//			char word = str.charAt(i);
//			
//			if (i == 0 && (word == '-' || word == '+')) {
//				continue;
//			}
//			// 符号
//			if (word >= '0' && word <= '9') {
//				continue;
//			} 
//			
//			if (i > 0 && word == '.') {
//				point++;
//				// 大于1是错误，忽略!
//				if (point > 1) {
//					Toolkit.getDefaultToolkit().beep();
//					return;
//				}
//				continue;
//			} else {
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