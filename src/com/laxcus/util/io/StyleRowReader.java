/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.io;

import java.io.*;

import com.laxcus.util.charset.*;

/**
 * 文本样式读取器，是CSV和TXT文档格式的父类
 * 
 * @author soctt.liang
 * @version 1.0 5/3/2019
 * @since laxcus 1.0
 */
public abstract class StyleRowReader {
	
	/** 磁盘文件 **/
	protected File file;
	
	/** 字符集，默认不定义  **/
	private int charset;
	
	/** 实例  **/
	protected BufferedReader reader;
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		close();
	}

	/**
	 * 关闭文件
	 */
	public void close() {
		// 关闭文件
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {

			}
			reader = null;
		}
	}
	
	/**
	 * 判断文件的开头匹配指定的BOM符号
	 * @param f
	 * @param bom
	 * @return
	 * @throws IOException
	 */
	private boolean matchs(byte[] bom) throws IOException {
		byte[] b = new byte[bom.length];
		if (file.length() < b.length) {
			return false;
		}

		FileInputStream in = new FileInputStream(file);
		in.read(b);
		in.close();

		for (int i = 0; i < b.length; i++) {
			if (b[i] != bom[i]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 打开文件，默认是UTF8编码
	 * @throws IOException
	 */
	public void open() throws IOException {
		// 先关闭
		close();
		
		// 判断文件的字符编码
		int skip = 0;
		int who = CharsetType.NONE;
		// 过滤文件开头的BOM字符
		if (matchs(BOM.UTF8)) {
			who = CharsetType.UTF8; // "UTF-8";
			skip = BOM.UTF8.length;
		} else if (matchs(BOM.UTF16_BE)) {
			who = CharsetType.UTF16_BE; // "UTF-16BE";
			skip = BOM.UTF16_BE.length;
		} else if (matchs(BOM.UTF16_LE)) {
			who = CharsetType.UTF16_LE;// "UTF-16LE";
			skip = BOM.UTF16_LE.length;
		} else if (matchs(BOM.UTF32_BE)) {
			who = CharsetType.UTF32_BE; // "UTF-32BE";
			skip = BOM.UTF32_BE.length;
		} else if (matchs(BOM.UTF32_LE)) {
			who = CharsetType.UTF32_LE; // "UTF-32LE";
			skip = BOM.UTF32_LE.length;
		} else if (matchs(BOM.GB18030)) {
			who = CharsetType.GB18030;
			skip = BOM.GB18030.length;
		}
		
		// 如果没有定义编码，但是检测到匹配的BOM时，以检测到的BOM为准。
		if (!CharsetType.isCharset(charset) && CharsetType.isCharset(who)) {
			setCharset(who);
		}
		
//		System.out.printf("charset is %s\n", charset);

		// 打开文件
		FileInputStream in = new FileInputStream(file);
		// 跨过前面的字节
		if (skip > 0) {
			in.skip(skip);
		}
		// 按照字符编码定义打开文件
		InputStreamReader is = null;
		if (CharsetType.isCharset(charset)) {
			String type = CharsetType.translate(charset);
			is = new InputStreamReader(in, type);
		} else {
			is = new InputStreamReader(in);
		}
		// 放进缓冲
		reader = new BufferedReader(is);
	}

	/**
	 * 检测打开
	 * @throws IOException
	 */
	protected void ensureOpen() throws IOException {
		if(file == null){
			throw new IOException("null file!");
		}
		if (reader == null) {
			throw new IOException("stream closed");
		}
	}

	/**
	 * 构造默认的文本样式读取器
	 */
	protected StyleRowReader() {
		super();
//		setCharset("UTF-8");
	}
	
	/**
	 * 构造文本样式读取器，指定文件名
	 * @param file 文件名
	 */
	protected StyleRowReader(File file) {
		this();
		setFile(file);
	}
	
	/**
	 * 构造文本样式读取器，指定文件名
	 * @param file 文件名
	 * @param charset 字符编码
	 */
	protected StyleRowReader(File file, int charset) {
		this(file);
		setCharset(charset);
	}
	
	
	/**
	 * 设置字符串编码
	 * @param who 字符串编码
	 */
	public void setCharset(int who) {
		charset = who;
	}

	/**
	 * 设置字符串编码
	 * @return 字符串编类型
	 */
	public int getCharset() {
		return charset;
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
	 * 读标题栏
	 * @return 列数组
	 * @throws IOException
	 */
	public abstract String[] readTitle() throws IOException;

	/**
	 * 读一行记录
	 * @return 字符串数组
	 * @throws IOException
	 */
	public abstract String[] readRow() throws IOException;

}
