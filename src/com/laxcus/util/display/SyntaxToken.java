/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

/**
 * 分布描述语言的命令检查点。
 * 
 * @author scott.liang
 * @version 1.0 10/08/2009
 * @since laxcus 1.0
 */
public class SyntaxToken {

	/** 标记类型 **/
	public final static byte COMMAND = 1;
	public final static byte KEYWORD = 2;
	public final static byte TYPE = 3;

	/** 字符 **/
	private String word;

	/** 字符长度 */
	private int size;

	/** 字符串在文本的下标位置 **/
	private int index;

	/** 字符类型 **/
	private byte family;

	/**
	 * 构造检查点，指定全部参数
	 * @param word
	 * @param index
	 * @param size
	 * @param family
	 */
	public SyntaxToken(String word, int index, int size, byte family) {
		super();
		this.word = word;
		this.index = index;
		this.size = size;
		this.family = family;
	}
	
	/**
	 * 返回字符
	 * @return
	 */
	public String getWord() {
		return word;
	}

	/**
	 * 返回字符下标位置
	 * @return
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * 返回字符长度
	 * @return
	 */
	public int getSize() {
		return this.size;
	}
	
	/**
	 * 判断是命令
	 * @return
	 */
	public boolean isCommand() {
		return family == SyntaxToken.COMMAND;
	}

	/**
	 * 判断字符是关键字
	 * @return
	 */
	public boolean isKeyword() {
		return family == SyntaxToken.KEYWORD;
	}

	/**
	 * 判断字符是数据类型
	 * @return
	 */
	public boolean isType() {
		return family == SyntaxToken.TYPE;
	}
	
	/**
	 * 返回类型定义
	 * @return 字节标记
	 */
	public byte getFamily() {
		return family;
	}

	/**
	 * 翻译
	 * @return
	 */
	private String translate() {
		if(isCommand()) {
			return "Command";
		} else if(isKeyword()) {
			return "Keyword";
		} else if(isType()) {
			return "Type";
		}
		return "none";
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s,%d,%d,%s", word, index, size, translate());
		// (family == Token.KEYWORD ? "Keyword" : "Type"));
	}
}
