/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.io.*;
import java.util.regex.*;

/**
 * 显示日志。<br>
 * 用在FRONT终端和WATCH站点上。
 * 
 * @author scott.liang
 * @version 1.0 09/06/2009
 * @since laxcus 1.0
 */
public class LogItem implements Serializable {

	private static final long serialVersionUID = 6228738008223913689L;

	private final static String PREFIX = "^\\s*([DEBUG|INFO|WRANING|ERROR|FATAL]+)\\:\\s*([\\p{ASCII}\\W]+?)\\s*$";

	/** 日志类型 **/
	public final static byte DEBUG = 1;
	public final static byte INFO = 2;
	public final static byte WARNING = 3;
	public final static byte ERROR = 4;
	public final static byte FATAL = 5;

	/** 子类型 **/
	public final static byte SUBDEBUG = 11;
	public final static byte SUBINFO = 12;
	public final static byte SUBWARNING = 13;
	public final static byte SUBERROR = 14;
	public final static byte SUBFATAL = 15;

	/** 日志类型定义 **/
	private byte family;

	/** 原始参数 **/
	private String primitive;

	/** 显示文本 **/
	private String text;

	/**
	 * 构造前端显示日志
	 */
	public LogItem(byte family, String primitive) {
		super();
		setFamily(family);
		setPrimitive(primitive);
		filte();
	}

	/**
	 * 设置类型
	 * @param who
	 */
	public void setFamily(byte who) {
		family = who;
	}

	/**
	 * 返回类型
	 * @return
	 */
	public byte getFamily() {
		return family;
	}

	/**
	 * 判断是子类型
	 * @return
	 */
	public boolean isSub() {
		return family > 10;
	}

	/**
	 * 设置原始值
	 * @param s
	 */
	public void setPrimitive(String s) {
		primitive = s;
	}
	
	/**
	 * 返回原始值
	 * @return
	 */
	public String getPrimitive(){
		return primitive;
	}

	//	/**
	//	 * 返回过滤值
	//	 * @return
	//	 */
	//	public String getFilteValue() {
	//		Pattern pattern = Pattern.compile(PREFIX);
	//		Matcher matcher = pattern.matcher(primitive);
	//		if (matcher.matches()) {
	//			return matcher.group(2);
	//		}
	//		return primitive;
	//	}

	/**
	 * 返回显示文本
	 * @return
	 */
	public String getText() {
		//		if(text == null) {
		//			return "null log";
		//		}
		return text;
	}

	//	/**
	//	 * 筛选参数
	//	 */
	//	private void filte() {
	//		Pattern pattern = Pattern.compile(PREFIX);
	//		Matcher matcher = pattern.matcher(primitive);
	//		if (matcher.matches()) {
	//			text = matcher.group(2);
	//			text = text.replaceAll("([\\t]{1})", "&nbsp;&nbsp;&nbsp;&nbsp;");
	//			text = text.replaceAll("([\\x20]{1})", "&nbsp;");
	//			text = String.format("<HTML><BODY>%s</BODY></HTML>", text);
	//		} else {
	//			text = primitive;
	//			text = text.replaceAll("([\\t]{1})", "&nbsp;&nbsp;&nbsp;&nbsp;");
	//			text = text.replaceAll("([\\x20]{1})", "&nbsp;");
	//			text = String.format("<HTML><BODY>%s</BODY></HTML>", text);
	//		}
	//	}

	/**
	 * 筛选参数
	 */
	private void filte() {
		Pattern pattern = Pattern.compile(PREFIX);
		Matcher matcher = pattern.matcher(primitive);
		if (matcher.matches()) {
			text = matcher.group(2);
		} else {
			text = primitive;
		}
		text = text.replaceAll("([\\t]{1})", "&nbsp;&nbsp;&nbsp;&nbsp;");
		text = text.replaceAll("([\\x20]{1})", "&nbsp;");
		text = String.format("<HTML><BODY>%s</BODY></HTML>", text);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return primitive;
	}

	//	public static void main(String[] args) {
	//		String log = "	at java.net.PlainSocketImpl.doConnect(PlainSocketImpl.java:333)";
	//		LogItem item = new LogItem(LogItem.SUBERROR, log);
	//		System.out.printf("\n%s\n", item.getText());
	//		System.out.printf("%s\n", item.toString());
	//	}
}