/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.login;

import java.awt.*;

import javax.swing.text.*;

/**
 * 密码数字过滤器
 * 限制非数字输入
 * 
 * @author scott.liang
 * @version 1.0 9/5/2021
 * @since laxcus 1.0
 */
final class PwdDocumentFilter extends DocumentFilter {

	/** 正则表达式 **/
	private final String REGEX = "^(?i)([0-9]{1,8})$";

	/** 文档实例 **/
	private Document document;

	/** 范围值 **/
	private int begin, end;

	/**
	 * 构造文本过滤器，指定文档
	 */
	public PwdDocumentFilter(Document doc, int b, int e) {
		super();
		document = doc;
		begin = b;
		end = e;
	}

	/**
	 * 声音
	 */
	private final void beep() {
		Toolkit.getDefaultToolkit().beep();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.text.DocumentFilter#insertString(javax.swing.text.DocumentFilter.FilterBypass, int, java.lang.String, javax.swing.text.AttributeSet)
	 */
	@Override
	public void insertString(DocumentFilter.FilterBypass fb, int offset,
			String str, AttributeSet attr) throws BadLocationException {
		// 如果是字值，忽略
		if (str == null || str.length() < 1) {
			return;
		}

		StringBuilder buf = new StringBuilder();
		int len = document.getLength();
		if (len > 0) {
			buf.append(document.getText(0, len));
		}
		// 从指定下标插入
		buf.insert(offset, str);

		String text = buf.toString();
		// 判断是数字
		if (!text.matches(REGEX)) {
			// System.out.printf("[%s] 不匹配\n", text);
			beep();
			return;
		}
		int value = Integer.parseInt(text);
		if (value < begin || value > end) {
			// System.out.printf("%d < %d\n", value, end);
			beep();
			return;
		} 

		// 调用超类
		super.insertString(fb, offset, str, attr);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.text.DocumentFilter#remove(javax.swing.text.DocumentFilter.FilterBypass, int, int)
	 */
	@Override
	public void remove(DocumentFilter.FilterBypass fb, int offset, int length)
	throws BadLocationException {
		// 调用超类
		super.remove(fb, offset, length);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.text.DocumentFilter#replace(javax.swing.text.DocumentFilter.FilterBypass, int, int, java.lang.String, javax.swing.text.AttributeSet)
	 */
	@Override
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length,
			String str, AttributeSet attrs) throws BadLocationException {
		// 提取已经存在的字符串
		StringBuilder buf = new StringBuilder();
		int len = document.getLength();
		if (len > 0) {
			String s = document.getText(0, len);
			buf.append(s);
		}

		// 删除是0，从指定位置插入
		if (length == 0) {
			buf.insert(offset, str);
		}
		// 替换某段字符串
		else {
			buf.replace(offset, offset + length, str);
		}

		String text = buf.toString();
		if (!text.matches(REGEX)) {
			// System.out.printf("[%s] 不匹配\n", s);
			beep();
			return;
		}
		// 如果不在范围内，拒绝执行
		int value = Integer.parseInt(text);
		if (value < begin || value > end) {
			// System.out.printf("%d < %d\n", value, end);
			beep();
			return;
		}

		// 调用超类
		super.replace(fb, offset, length, str, attrs);
	}

}