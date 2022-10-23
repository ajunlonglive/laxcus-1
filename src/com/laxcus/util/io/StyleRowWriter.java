/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.io;

import java.io.*;

import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.util.charset.*;

/**
 * 行记录样式写入器，是CSV和TXT文档格式的父类
 * 
 * @author soctt.liang
 * @version 1.0 5/3/2019
 * @since laxcus 1.0
 */
public abstract class StyleRowWriter {
	
	/** 磁盘文件 **/
	protected File file;
	
	/** 字符串编码  **/
	private int charset;

	/**
	 * 构造默认的行记录样式写入器
	 */
	protected StyleRowWriter() {
		super();
		setCharset(CharsetType.UTF8);
	}
	
	/**
	 * 构造行记录样式写入器，指定文件名
	 * @param file 文件名
	 */
	protected StyleRowWriter(File file) {
		this();
		setFile(file);
	}

	/**
	 * 设置字符串编码
	 * @param e 字符串编码
	 */
	public void setCharset(int e) {
		charset = e;
	}

	/**
	 * 设置字符串编码
	 * @return 字符串编码
	 */
	public int getCharset() {
		return charset;
	}
	
	/**
	 * 对内容进行编码
	 * @param content 内容
	 * @return 输出经过编码的字节数组
	 * @throws UnsupportedEncodingException
	 */
	public byte[] encode(String content) throws UnsupportedEncodingException {
		if (!CharsetType.isCharset(charset)) {
			return content.getBytes();
		}
		String who = CharsetType.translate(charset);
		return content.getBytes(who);
	}
	
	/**
	 * 设置文件
	 * @param e
	 */
	public void setFile(File e) {
		file = e;
	}
	
	/**
	 * 返回文件
	 * @return
	 */
	public File getFile() {
		return file;
	}

	/**
	 * 写入标题
	 * @param table
	 * @throws IOException
	 */
	public abstract void writeTitle(Sheet sheet) throws IOException;

	/**
	 * 写入记录
	 * @param sheet
	 * @param rows
	 * @throws IOException
	 */
	public abstract void writeContent(Sheet sheet, Row[] rows) throws IOException;
}
