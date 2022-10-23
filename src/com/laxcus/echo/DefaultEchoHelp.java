/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo;

import com.laxcus.util.classable.*;

/**
 * 系统默认的回显辅助信息<br>
 * 
 * 在这里，提供一组提示信息。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public class DefaultEchoHelp extends EchoHelp {
	
	private static final long serialVersionUID = 4000879032566799219L;
	
	/** 消息提示 **/
	private String message;

	/**
	 * 构造默认回显辅助信息
	 * @param that
	 */
	private DefaultEchoHelp(DefaultEchoHelp that) {
		super(that);
		message = that.message;
	}

	/**
	 * 构造默认回显辅助信息
	 */
	public DefaultEchoHelp() {
		super();
	}

	/**
	 * 构造默认回显辅助信息，设置提示信息
	 * @param message 提示信息
	 */
	public DefaultEchoHelp(String message) {
		this();
		setMessage(message);
	}

	/**
	 * 从可类化数据读取器中解析默认回显辅助信息
	 * @param reader - 可类化数据读取器
	 * @since 1.1
	 */
	public DefaultEchoHelp(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置提示信息
	 * @param e  信息字符串
	 */
	public void setMessage(String e) {
		message = e;
	}
	
	/**
	 * 返回提示信息
	 * @return 信息字符串
	 */
	public String getMessage() {
		return message;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return message;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.EchoHelp#duplicate()
	 */
	@Override
	public DefaultEchoHelp duplicate() {
		return new DefaultEchoHelp(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.EchoHelp#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeString(message);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.EchoHelp#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		message = reader.readString();
	}

}