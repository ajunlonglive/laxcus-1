/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 计算半截码命令。<br>
 * 
 * 文本内容编码首先进行UTF8编码，然后将每个字节高4位和低4位切开，加'a'字符，转成两个字节保存。全部字节合并输出就是结果值。
 * 
 * @author scott.liang
 * @version 1.1 11/25/2017
 * @since laxcus 1.0
 */
public class BuildHalf extends Command {
	
	private static final long serialVersionUID = -4254601187248208180L;
	
	/** 编码模式 **/
	private boolean encode;

	/** 忽略大小写，只在编码时有效 **/
	private boolean ignore;

	/** 原始文本 **/
	private String text;

	/**
	 * 根据传入的计算半截码命令，生成它的数据副本
	 * @param that 计算散列码实例
	 */
	private BuildHalf(BuildHalf that) {
		super(that);
		encode = that.encode;
		ignore = that.ignore;
		text = that.text;
	}
	
	/**
	 * 建立计算半截码命令
	 */
	public BuildHalf() {
		super();
		setEncode(true);
		setIgnore(false);
	}

	/**
	 * 建立计算半截码命令，指定参数
	 * @param encode 编码模式
	 * @param ignore 忽略大小写
	 * @param text 原始文本
	 */
	public BuildHalf(boolean encode, boolean ignore, String text) {
		super();
		setEncode(encode);
		setIgnore(ignore);
		setText(text);
	}

	/**
	 * 设置原始文本
	 * @param e 原始文本
	 */
	public void setText(String e) {
		Laxkit.nullabled(e);
		text = e;
	}

	/**
	 * 返回原始文本
	 * @return 原始文本
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * 设置是编码模式
	 * @param b 是或者否
	 */
	public void setEncode(boolean b) {
		encode = b;
	}
	
	/**
	 * 判断是编码模式
	 * @return 返回真或者假
	 */
	public boolean isEncode() {
		return encode;
	}

	/**
	 * 设置忽略大小写。只在编码时有效
	 * @param b 忽略大小写或者否
	 */
	public void setIgnore(boolean b) {
		ignore = b;
	}

	/**
	 * 判断忽略大小写
	 * @return 返回真或者假
	 */
	public boolean isIgnore() {
		return ignore;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public BuildHalf duplicate() {
		return new BuildHalf(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(encode);
		writer.writeBoolean(ignore);
		writer.writeString(text);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		encode = reader.readBoolean();
		ignore = reader.readBoolean();
		text = reader.readString();
	}

}