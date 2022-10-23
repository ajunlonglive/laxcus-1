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
 * 时间日期检测接口。截获和检测文本框的输入字符，只接受数字类型，不包括正负符号，同时限制输入长度。
 * 
 * @author scott.liang
 * @version 1.0 7/31/2020
 * @since laxcus 1.0
 */
public class DateTimeDocument extends PlainDocument {

	private static final long serialVersionUID = -9200795714050536682L;

	/** 文本组件 **/
	private JTextComponent textComponent;
	
	/** 长度限制，默认是0，不限度 **/
	private int limit = 24;

	/**
	 * 构造默认的时间日期检测接口
	 */
	public DateTimeDocument() {
		super();
	}


	/**
	 * 构造时间日期检测接口，指定参数
	 * @param component
	 * @param limit
	 */
	public DateTimeDocument(JTextComponent component, int size) {
		this();
		textComponent = component;
		this.limit = size;
	}
	
//	public static int getLimitSize() {
//		return LIMIT_SIZE;
//	}

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
		if (!(0 <= limit && len <= limit)) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		// 判断字符，包括数字，横线，空格，冒号
		for (int i = 0; i < len; i++) {
			char word = buf.charAt(i);
			if (word >= '0' && word <= '9') {
				continue;
			} else if (word == '-' || word == 0x20 || word == ':') {
				continue;
			} else {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
		}

		// 调用超类
		super.insertString(offs, str, aset);
	}
	
}