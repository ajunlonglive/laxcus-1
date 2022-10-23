/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.color;

import java.awt.*;

import javax.swing.text.*;

/**
 * 颜色数字过滤器
 * 限制非数字输入
 * 
 * @author scott.liang
 * @version 1.0 9/1/2021
 * @since laxcus 1.0
 */
public class ColorDocumentFilter extends DocumentFilter {

	/** 正则表达式 **/
	private final String REGEX = "^(?i)([0-9]{1,8})$";

	/** 文档实例 **/
	private Document document;

	//	/** 对话框 **/
	//	private ColorDialog dialog;
	//	
	//	/** 输入框 **/
	//	private JTextField field;

	/** 范围值 **/
	private int begin, end;

	//	/**
	//	 * 构造文本过滤器，指定文档
	//	 */
	//	public ColorDocumentFilter(Document doc, ColorDialog d, JTextField f, int b, int e) {
	//		super();
	//		document = doc;
	//		dialog = d;
	//		field = f;
	//		begin = b;
	//		end = e;
	//	}

	/**
	 * 构造文本过滤器，指定文档
	 */
	public ColorDocumentFilter(Document doc, int b, int e) {
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

	//	private void touch() {
	//		//		ActionListener[] listeners = field.getActionListeners();
	//		//		System.out.printf("ActionListener size is %d\n", (listeners == null ? -1 : listeners.length));
	//
	////		ActionEvent event = new ActionEvent(field, ActionEvent.ACTION_PERFORMED, "");
	////		dialog.processInputEvent(event);
	//
	//		//		for (int i = 0; i < listeners.length; i++) {
	//		//			listeners[i].actionPerformed(event);
	//		//		}
	//	}

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

		//		String text = "";
		//		int len = document.getLength();
		//		if (len > 0) {
		//			text = document.getText(0, len);
		//		}
		//		text = text + str;

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

		//		System.out.printf("PASS [%s]\n", text);

		// 调用超类
		super.insertString(fb, offset, str, attr);

		//		// 去触发事件
		//		touch();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.text.DocumentFilter#remove(javax.swing.text.DocumentFilter.FilterBypass, int, int)
	 */
	@Override
	public void remove(DocumentFilter.FilterBypass fb, int offset, int length)
	throws BadLocationException {
		//		if (offset < document.getLastPosition()) {
		//			beep();
		//			return;
		//		}

		// 调用超类
		super.remove(fb, offset, length);

		//		// 去触发事件
		//		touch();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.text.DocumentFilter#replace(javax.swing.text.DocumentFilter.FilterBypass, int, int, java.lang.String, javax.swing.text.AttributeSet)
	 */
	@Override
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length,
			String str, AttributeSet attrs) throws BadLocationException {
		//		if (offset < document.getLastPosition()) {
		//			beep();
		//			return;
		//		}
		//		beep();

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

		//		System.out.printf("off:%d, len: %d, [%s] [%s]\n", offset, length, text, buf.toString());

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

		//		// 去触发事件
		//		touch();
	}

}
