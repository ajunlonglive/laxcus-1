/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact;

import java.util.*;

import com.laxcus.util.*;

/**
 * 快捷组件数据栈
 * 
 * @author scott.liang
 * @version 1.0 3/25/2018
 * @since laxcus 1.0
 */
public class SwiftPrintStack {

	/** 保存存储单元记录 **/
	private ArrayList<SwiftPrintLine> array = new ArrayList<SwiftPrintLine>();

	/**
	 * 构造默认的快捷组件数据栈
	 */
	public SwiftPrintStack() {
		super();
	}
	
	/**
	 * 成员数目
	 * @return 整数
	 */
	public int size() {
		return array.size();
	}
	
//	/**
//	 * 关闭当前行
//	 * @return 成功返回真，否则假
//	 */
//	public boolean close() {
//		int size = array.size();
//		SwiftPrintLine line = (size > 0 ? array.get(size - 1) : null);
//
//		if(line != null) {
//			line.setClose(true);
//			return true;
//		} 
//		return false;
//	}

	/**
	 * 保存打印信息
	 * @param str
	 */
	public void print(String str, boolean close) {
		Laxkit.nullabled(str);

		int size = array.size();
		SwiftPrintLine line = (size > 0 ? array.get(size - 1) : null);

		// 空、上一行是错误、上一行已经关闭，三种条件生成新行
		if (line == null || line.isError() || line.isClose()) {
			line = new SwiftPrintLine(false);
			array.add(line);
		}

		// 保存打印记录
		line.append(str);
		line.setClose(close);
	}

	/**
	 * 保存打印信息
	 * @param str
	 */
	public void print(String str) {
		boolean close = (str.endsWith("\r\n") || str.endsWith("\n"));
		print(str, close);
	}

	/**
	 * 保存格式化打印信息
	 * @param format
	 * @param args
	 */
	public void printf(String format, Object... args) {
		String str = String.format(format, args);
		print(str);
	}

	/**
	 * 保存一个空行
	 */
	public void println() {
		print("\r\n");
	}

	/**
	 * 保存一行打印信息
	 * @param str
	 */
	public void println(String str) {
		Laxkit.nullabled(str);
		str = str + "\r\n";
		print(str);
//		println();
	}

	/**
	 * 保存格式化打印信息，结尾换行
	 * @param format
	 * @param args
	 */
	public void println(String format, Object... args) {
		String str = String.format(format, args);
		println(str);
//		println();
	}

	/**
	 * 保存错误
	 * @param str
	 * @param close
	 */
	private void error(String str, boolean close) {
		int size = array.size();
		SwiftPrintLine line = (size > 0 ? array.get(size - 1) : null);
		// 空、上一行不是错误、上一行已经关闭，三种条件生成新行
		if (line == null || !line.isError() || line.isClose()) {
			line = new SwiftPrintLine(true);
			array.add(line);
		}
		line.append(str);
		line.setClose(close);
	}

	/**
	 * 保存错误信息
	 * @param str
	 */
	public void error(String str) {
		error(str, true);
	}

	/**
	 * 格式化错误信息
	 * @param format
	 * @param args
	 */
	public void error(String format, Object... args) {
		String str = String.format(format, args);
		error(str);
	}

	/**
	 * 格式化错误信息，结尾换行
	 * @param format
	 * @param args
	 */
	public void errorln(String format, Object... args) {
		String str = String.format(format, args);
		error(str);
	}

	/**
	 * 保存故障信息
	 * @param e
	 */
	public void error(Throwable e) {
		String str = Laxkit.printThrowable(e);
		error(str);
	}

	/**
	 * 输出全部打印信息
	 * @return 打印信息
	 */
	public List<SwiftPrintLine> flush() {
		return new ArrayList<SwiftPrintLine>(array);
	}

}