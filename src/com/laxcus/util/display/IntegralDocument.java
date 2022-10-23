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
 * 整数检测接口。截获和检测文本框的输入字符，只接受数字字符、正负符呈，限制输入长度。
 * 
 * @author scott.liang
 * @version 1.0 7/30/2020
 * @since laxcus 1.0
 */
public class IntegralDocument extends PlainDocument {

	private static final long serialVersionUID = -4969689593256041794L;

	/** 文本组件 **/
	private JTextComponent textComponent;
	
	/** 长度限制，默认是0，不限度 **/
	private int limitSize = 0;

	/**
	 * 构造默认的整数检测接口
	 */
	public IntegralDocument() {
		super();
	}

	/**
	 * 构造整数检测接口，指定参数
	 * @param component
	 * @param limit
	 */
	public IntegralDocument(JTextComponent component, int limit) {
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
		// 判断长度没有超出
		if (!(0 <= limitSize && len <= limitSize)) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		// 计算每个字符
		for (int i = 0; i < len; i++) {
			char word = buf.charAt(i);
			if (i == 0 && (word == '-' || word == '+')) {
				continue;
			}
			
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

}