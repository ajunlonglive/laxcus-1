/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.util.classable.*;
import com.laxcus.util.datetime.*;

/**
 * 异步调用日志。<br>
 * 记录回显调用器操作中产生的数据
 * 
 * @author scott.liang
 * @version 1.1 01/03/2017
 * @since laxcus 1.0
 */
public class EchoLog implements Classable, Cloneable, Serializable {

	private static final long serialVersionUID = -6216821823073496813L;

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*([\\w\\W]+?)\\|(S|F)\\|(M|D)\\|([0-9]+)\\|([0-9]+)\\|([0-9]+)\\|([0-9]+)\\s*$";

	/** 命令 **/
	private String command;

	/** 成功或者失败 **/
	private boolean perfectly;

	/** 数据处理模式（内存或者硬盘） **/
	private boolean memory;

	/** 启动时间。JRE格式 **/
	private long launchTime;

	/** 使用时间 **/
	private long runTime;

	/** 接收的数据流量 **/
	private long receiveFlowSize;

	/** 发送的数据流量 **/
	private long sendFlowSize;

	/**
	 * 构造默认的异步调用日志
	 */
	public EchoLog() {
		super();
		perfectly = false;
		memory = false;
		launchTime = 0;
		runTime = 0;
		receiveFlowSize = 0;
		sendFlowSize = 0;
	}

	/**
	 * 构造异步调用日志的数据副本
	 * @param that 异步调用日志原本
	 */
	protected EchoLog(EchoLog that) {
		this();
		command = that.command;
		perfectly = that.perfectly;
		memory = that.memory;
		launchTime = that.launchTime;
		runTime = that.runTime;
		receiveFlowSize = that.receiveFlowSize;
		sendFlowSize = that.sendFlowSize;
	}

	/**
	 * 从可类化数据读取器中解析异步调用日志
	 * @param reader 可类化数据读取器
	 */
	public EchoLog(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 解析异步调用日志
	 * @param input 字符串语句
	 */
	public EchoLog(String input) {
		this();
		split(input);
	}

	/**
	 * 设置命令
	 * @param e 语句命令
	 */
	public void setCommand(String e) {
		command = e;
	}

	/**
	 * 返回语句命令
	 * @return 字符串命令
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * 设置成功
	 * @param b 成功标记
	 */
	public void setPerfectly(boolean b) {
		perfectly = b;
	}

	/**
	 * 判断成功
	 * @return 返回真或者假
	 */
	public boolean isPerfectly() {
		return perfectly;
	}

	/**
	 * 设置为内存处理模式
	 * @param b 内存标记
	 */
	public void setMemory(boolean b) {
		memory = b;
	}

	/**
	 * 判断是内存处理模式
	 * @return 返回真或者假。
	 */
	public boolean isMemory() {
		return memory;
	}

	/**
	 * 判断是磁盘处理模式
	 * @return 返回真或者假
	 */
	public boolean isDisk() {
		return !memory;
	}

	/**
	 * 返回任务启动时间。单位：毫秒
	 * @return 启动时间
	 */
	public long getLaunchTime() {
		return getLaunchTimestamp().getTime();
	}
	
	/**
	 * 设置任务启动时间
	 * @param e 任务启动时间
	 */
	public void setLaunchTimestamp(Date e) {
		launchTime = SimpleTimestamp.format(e);
	}
	
	/**
	 * 返回任务启动时间
	 * @return 任务启动时间
	 */
	public Date getLaunchTimestamp() {
		return SimpleTimestamp.format(launchTime);
	}

	/**
	 * 设置任务运行时间。单位：毫秒
	 * @param e 运行时间
	 */
	public void setRunTime(long e) {
		runTime = e;
	}

	/**
	 * 返回任务运行时间
	 * @return 运行时间
	 */
	public long getRunTime() {
		return runTime;
	}

	/**
	 * 设置接收的数据流量，以字节计量
	 * @param e 当前接收的数据流量
	 */
	public void setReceiveFlowSize(long e) {
		receiveFlowSize = e;
	}

	/**
	 * 返回接收的数据流量，以字节计算
	 * @return 接收的数据流量
	 */
	public long getReceiveFlowSize() {
		return receiveFlowSize;
	}

	/**
	 * 设置发送的数据流量，以字节计算
	 * @param e 当前发送的数据流量
	 */
	public void setSendFlowSize(long e) {
		sendFlowSize = e;
	}

	/**
	 * 返回发送的数据流量，以字节计算
	 * @return 发送的数据流量
	 */
	public long getSendFlowSize() {
		return sendFlowSize;
	}

	/**
	 * 生成数据副本
	 * @return 当前EchoLog的数据副本
	 */
	public EchoLog duplicate() {
		return new EchoLog(this);
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s|%s|%s|%d|%d|%d|%d", command, (perfectly ? "S"
				: "F"), (memory ? "M" : "D"), launchTime, runTime, receiveFlowSize, sendFlowSize);
	}

	/**
	 * 使用正则表达式解析数据表空间。
	 * @param input EchoLog格式结构。
	 * @throws PatternSyntaxException
	 */
	public void split(String input) {
		Pattern pattern = Pattern.compile(EchoLog.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throw new PatternSyntaxException(input, EchoLog.REGEX, 0);
		}

		command = matcher.group(1);
		perfectly = "S".equals(matcher.group(2));
		memory = "M".equals(matcher.group(3));
		launchTime = Long.parseLong(matcher.group(4));
		runTime = Long.parseLong(matcher.group(5));
		receiveFlowSize = Long.parseLong(matcher.group(6));
		sendFlowSize = Long.parseLong(matcher.group(7));
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeString(command);
		writer.writeBoolean(perfectly);
		writer.writeBoolean(memory);
		writer.writeLong(launchTime);
		writer.writeLong(runTime);
		writer.writeLong(receiveFlowSize);
		writer.writeLong(sendFlowSize);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		command = reader.readString();
		perfectly = reader.readBoolean();
		memory = reader.readBoolean();
		launchTime = reader.readLong();
		runTime = reader.readLong();
		receiveFlowSize = reader.readLong();
		sendFlowSize = reader.readLong();
		return reader.getSeek() - seek;
	}

}
