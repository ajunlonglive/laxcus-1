/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.io.*;
import java.util.regex.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 用户调用日志。<br>
 * 记录用户执行的每一个操作产生的数据
 * 
 * @author scott.liang
 * @version 1.1 01/03/2017
 * @since laxcus 1.0
 */
public class UserLog implements Classable, Cloneable, Serializable {

	private static final long serialVersionUID = -1023514710626431224L;

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*([0-9a-fA-F]{64})\\|([\\w\\W]+?)\\s*$";

	/** 用户签名 **/
	private Siger issuer;

	/** 异步日志 **/
	private EchoLog log;

	/**
	 * 构造默认的用户调用日志
	 */
	public UserLog() {
		super();
	}

	/**
	 * 生成用户调用日志的数据副本
	 * @param that UserLog实例
	 */
	protected UserLog(UserLog that) {
		this();
		issuer = that.issuer;
		log = that.log;
	}

	/**
	 * 从可类化数据读取器中解析用户调用日志
	 * @param reader 可类化数据读取器
	 */
	public UserLog(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 解析用户日志
	 * @param input 日志文本
	 */
	public UserLog(String input) {
		this();
		split(input);
	}

	/**
	 * 设置异步调用日志
	 * @param e EchoLog实例
	 */
	public void setLog(EchoLog e) {
		Laxkit.nullabled(e);

		log = e;
	}

	/**
	 * 返回异步调用日志
	 * @return EchoLog实例
	 */
	public EchoLog getLog() {
		return log;
	}

	/**
	 * 设置用户签名
	 * @param e Siger实例
	 */
	public void setIssuer(Siger e) {
		Laxkit.nullabled(e);

		issuer = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return issuer;
	}
	
	/**
	 * 生成数据副本
	 * @return 当前UserLog的数据副本
	 */
	public UserLog duplicate() {
		return new UserLog(this);
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
		return issuer.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s|%s", issuer, log.toString());
	}
	
	/**
	 * 解析日志信息
	 * @param input 一行日志文本
	 */
	public void split(String input) {
		Pattern pattern = Pattern.compile(UserLog.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throw new PatternSyntaxException(input, UserLog.REGEX, 0);
		}

		issuer = new Siger(matcher.group(1));
		log = new EchoLog(matcher.group(2));
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoLog#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(issuer);
		writer.writeObject(log);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoLog#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		issuer = new Siger(reader);
		log = new EchoLog(reader);
		return reader.getSeek() - seek;
	}
}
