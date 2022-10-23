/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.io.*;

import com.laxcus.util.*;

/**
 * 图形窗口提示信息。<br>
 * 用在FRONT终端和WATCH站点上使用。
 * 
 * @author scott.liang
 * @version 1.0 8/23/2009
 * @since laxcus 1.0
 */
public class NoteItem implements Serializable {

	private static final long serialVersionUID = 5747174933814454030L;

	/** 提示消息 **/
	public final static int MESSAGE = 1;

	/** 提示警告 **/
	public final static int WARNING = 2;

	/** 提示故障 **/
	public final static int FAULT = 3;

	/** 状态 **/
	private int status;
	
	/** 原始文本 **/
	private String primitive;

	/** 显示文本 **/
	private String text;
	
	/** 播放声音 **/
	private boolean sound;

	/**
	 * 构造显示消息，指定参数
	 * @param status 状态码
	 * @param text 显示文本
	 * @param sound 播放声音
	 */
	public NoteItem(int status, String text, boolean sound) {
		super();
		setStatus(status);
		setPrimitive(text);
		setText(text);
		setSound(sound);
	}

	/**
	 * 构造显示消息，指定参数
	 * @param status 状态码
	 * @param text 显示文本
	 */
	public NoteItem(int status, String text) {
		this(status, text, true);
	}
	
	/**
	 * 设置状态
	 * @param who
	 */
	public void setStatus(int who) {
		switch (who) {
		case NoteItem.MESSAGE:
		case NoteItem.WARNING:
		case NoteItem.FAULT:
			status = who;
			break;
		default:
			throw new IllegalValueException("illega id:%d", who);
		}
	}

	/**
	 * 返回状态
	 * @return
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * 判断是普通提示消息
	 * @return 返回真或者假
	 */
	public boolean isMessage() {
		return status == NoteItem.MESSAGE;
	}

	/**
	 * 判断是警告
	 * @return 返回真或者假
	 */
	public boolean isWarning() {
		return status == NoteItem.WARNING;
	}

	/**
	 * 是故障
	 * @return 返回真或者假
	 */
	public boolean isFault() {
		return status == NoteItem.FAULT;
	}
	
	/**
	 * 设置原始值
	 * @param e
	 */
	public void setPrimitive(String e) {
		primitive = e;
	}

	/**
	 * 设置文本信息，首先进行格式化
	 * @param str 字符串
	 */
	public void setText(String str) {
		text = fromat(str);
	}
	
	/**
	 * 格式化当前参数
	 * @param text
	 * @return
	 */
	private String fromat(String text) {
		if(text == null) {
			return "";
		}
		text = text.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;");
		text = text.replaceAll("\\x20", "&nbsp;");
		text = text.replaceAll("\\s+", "<br>");
		
		return String.format("<html><body>%s</body></html>", text);
	}

	/**
	 * 返回文本信息
	 * @return
	 */
	public String getText() {
		return text;
	}

	/**
	 * 播放声音
	 * @param b
	 */
	public void setSound(boolean b) {
		sound = b;
	}

	/**
	 * 播放声音
	 * @return 返回真或者假
	 */
	public boolean isSound() {
		return sound;
	}

	/**
	 * 消息的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return primitive;
	}
}