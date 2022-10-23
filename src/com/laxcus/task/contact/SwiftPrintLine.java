/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 快捷组件记录单元。<br>
 * 
 * 保存快捷组件中执行的打印结果。
 * 
 * @author scott.liang
 * @version 1.0 11/23/2015
 * @since laxcus 1.0
 */
public final class SwiftPrintLine implements Serializable, Cloneable, Classable {

	private static final long serialVersionUID = -2003376337524675690L;

	/** 快捷组件运行记录 **/
	private StringBuilder buff = new StringBuilder();
	
	/** 错误记录标记 **/
	private boolean error;

	/** 完成标记 **/
	private boolean close;

	/**
	 * 构造默认的快捷组件记录单元
	 */
	private SwiftPrintLine() {
		super();
	}

	/**
	 * 构造快捷组件记录单元，确定是错误或者否
	 * @param error 错误
	 */
	public SwiftPrintLine(boolean error) {
		this();
		setError(error);
	}
	
	/**
	 * 生成快捷组件记录单元数据副本
	 * @param that SwiftPrintLine实例
	 */
	private SwiftPrintLine(SwiftPrintLine that) {
		buff.append(that.buff);
		error = that.error;
		close = that.close;
	}

	/**
	 * 构造快捷组件记录单元，保存文本记录
	 * @param error 错误记录
	 * @param text 记录
	 */
	public SwiftPrintLine(boolean error, String text) {
		this();
		append(text);
		setError(error);
	}

	/**
	 * 从可类化数据读取器解析快捷组件记录单元
	 * @param reader 可类化数据读取器
	 */
	public SwiftPrintLine(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 输出一行参数
	 * @return 字符串
	 */
	public String line() {
		return buff.toString();
	}
	
	/**
	 * 设置快捷组件运行记录
	 * @param e String实例
	 */
	public void append(String e) {
		Laxkit.nullabled(e);

		buff.append(e);
	}

	/**
	 * 返回快捷组件运行记录
	 * @return String实例
	 */
	public String flush() {
		return buff.toString();
	}
	
	/**
	 * 设置为错误
	 * @param b
	 */
	public void setError(boolean b){
		error = b;
	}
	
	/**
	 * 判断是错误
	 * @return 真或者假
	 */
	public boolean isError(){
		return error;
	}

	/**
	 * 设置结束标记
	 * @param b 结束标记
	 */
	public void setClose(boolean b) {
		close = b;
	}

	/**
	 * 判断是成功
	 * @return 返回真或者假
	 */
	public boolean isClose() {
		return close;
	}

	/**
	 * 产生数据副本
	 * @return SwiftPrintLine实例
	 */
	public SwiftPrintLine duplicate() {
		return new SwiftPrintLine(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return buff.hashCode();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeString(buff.toString());
		writer.writeBoolean(error);
		writer.writeBoolean(close);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		append(reader.readString());
		error = reader.readBoolean();
		close = reader.readBoolean();
		return reader.getSeek() - seek;
	}

}