/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.io.*;

import com.laxcus.echo.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 在线命令单元
 * 
 * @author scott.liang
 * @version 1.0 4/16/2018
 * @since laxcus 1.0
 */
public class SeekOnlineCommandItem implements Classable, Serializable, Cloneable, Comparable<SeekOnlineCommandItem> {

	private static final long serialVersionUID = -2916950430387106061L;

	/** FRONT站点登录地址 **/
	private String command;

	/** 在线用户名 **/
	private Siger siger;

	/** 命令来源回显地址。说明命令来源，由客户机设置，被服务器使用。服务器根据这个地址向客户机反馈结果。 **/
	private Cabin source;

	/** 运行状态 **/
	private boolean running;

	/** 命令优先级（权重）。大于0时，系统需要优先处理。默认优先级是0。**/
	private byte priority;

	/** 单向处理命令。单向处理命令被客户端投递给服务端后，不需要服务端反馈应答，默认是“假”。 **/
	private boolean direct;

	/** 内存处理模式，默认是“假”。此参数为“真”后，分布数据处理工作将以内存为介质实施数据存取。**/
	private boolean memory;
	
	/** 命令在线存在时间，以毫微秒为单位 **/
	private long onlineTime;
	
	/** 异步调用器线程编号 **/
	private long threadId;

	/**
	 * 构造默认和私有的在线命令单元
	 */
	private SeekOnlineCommandItem() {
		super();
		threadId = -1;
	}

	/**
	 * 生成在线命令单元的数据副本
	 * @param that SeekSiteCommandItem实例
	 */
	private SeekOnlineCommandItem(SeekOnlineCommandItem that) {
		this();
		command = that.command;
		siger = that.siger;
		source = that.source;
		running = that.running;
		priority = that.priority;
		direct = that.direct;
		memory = that.memory;
		onlineTime = that.onlineTime;
		threadId = that.threadId;
	}

	/**
	 * 构造在线命令单元，指定参数
	 * @param command 命令名称
	 * @param siger 用户签名
	 */
	public SeekOnlineCommandItem(String command, Siger siger) {
		this();
		setCommand(command);
		setSiger(siger);
	}

	/**
	 * 从可类化数据读取器中解析在线命令单元
	 * @param reader 可类化数据读取器
	 */
	public SeekOnlineCommandItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户签名，允许空指针
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		siger = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 判断是系统命令
	 * @return 返回真或者假
	 */
	public boolean isSystem() {
		return siger == null;
	}

	/**
	 * 判断是用户命令
	 * @return 返回真或者假
	 */
	public boolean isUser() {
		return siger != null;
	}

	/**
	 * 设置命令名称
	 * @param e String实例
	 */
	public void setCommand(String e) {
		Laxkit.nullabled(e);

		command = e;
	}

	/**
	 * 返回命令名称
	 * @return String实例
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * 设置运行状态
	 * @param b
	 */
	public void setRunning(boolean b) {
		running = b;
	}

	/**
	 * 判断是运行状态
	 * @return 返回真或者假
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * 设置命令优先级
	 * @param no 优先级编号
	 */
	public void setPriority(byte no) {
		priority = no;
	}

	/**
	 * 返回命令优先级。见CommandPriority定义
	 * @return 优先级编号
	 */
	public byte getPriority(){
		return priority;
	}

	/**
	 * 设置为单向处理命令。<br>
	 * 单向处理命令不需要服务反馈应答，通常的命令都要求服务器返回处理结果。
	 * 
	 * @param b 单向处理
	 */
	public void setDirect(boolean b) {
		direct = b;
	}

	/**
	 * 要求命令反馈结果
	 * 
	 * @param b 是或者否
	 */
	public void setReply(boolean b) {
		setDirect(!b);
	}

	/**
	 * 判断是单向处理命令
	 * @return 返回真或者假。
	 */
	public boolean isDirect() {
		return direct;
	}

	/**
	 * 设置命令为内存存取模式。<br><br>
	 * 
	 * 用户设置命令为内存存取模式后，在实施过程中，仍然需要根据当时系统资源状况来判断和决定。也就是说，虽然用户命令是内存存取模式，也不一定全部保证分布处理都是内存存取模式。<br><br>
	 * 
	 * 2.0版本的内存分布计算取消了预约操作，统一采用直接请求方式。主要原因有3点：1.当系统总体资源达不到用户请求要求时，内存计算将永远无法实施，最后达到超时后撤销，若不设置超时，则永远滞留在集群中。2.当系统资源不足时，预约要长时间等待，可能比直接请求效率还低。3.简化设计。<br><br>
	 * 
	 * <br>
	 * @param b 内存存取模式
	 */
	public void setMemory(boolean b) {
		// 保存参数
		memory = b;
	}

	/**
	 * 设置命令为硬盘模式
	 * @param b 硬盘存取模式
	 */
	public void setDisk(boolean b){
		setMemory(!b);
	}

	/**
	 * 判断命令选用内存做为中间存取介质。<br>
	 * @return 返回真或者假。
	 */
	public boolean isMemory() {
		return memory;
	}

	/**
	 * 设置命令来源地址。<br>
	 * 命令来源地址用于异步命令中，指示命令发起的源头。命令来源地址由命令的请求端设置，被服务端调用。服务器根据这个地址，将异步应答数据返回给请求端。<br>
	 * 命令来源地址被设置后，表示将执行异步处理操作。
	 * @param e 命令来源地址
	 */
	public void setSource(Cabin e) {
		source = e;
	}

	/**
	 * 返回命令来源地址
	 * @return 命令来源地址实例
	 */
	public final Cabin getSource() {
		return source;
	}
	
	/**
	 * 设置命令在线存在时间
	 * @param e 命令在线存在时间
	 */
	public void setOnlineTime(long e){
		onlineTime = e;
	}

	/**
	 * 返回以毫微秒为单位的命令在线存在时间
	 * @return 命令在线存在时间
	 */
	public long getOnlineTime() {
		return onlineTime;
	}
	
	/**
	 * 设置关联线程编号
	 * @param who 关联线程编号
	 */
	public void setThreadId(long who){
		threadId = who;
	}

	/**
	 * 返回以毫微秒为单位的关联线程编号
	 * @return 关联线程编号
	 */
	public long getThreadId() {
		return threadId;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return SeekSiteCommandItem实例
	 */
	public SeekOnlineCommandItem duplicate() {
		return new SeekOnlineCommandItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SeekOnlineCommandItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SeekOnlineCommandItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return siger.hashCode() ^ command.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SeekOnlineCommandItem that) {
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(siger, that.siger);
		if (ret == 0) {
			ret = Laxkit.compareTo(command, that.command);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();

		writer.writeString(command);
		writer.writeInstance(siger);
		writer.writeInstance(source);
		writer.writeBoolean(running);
		writer.write(priority);
		writer.writeBoolean(direct);
		writer.writeBoolean(memory);
		writer.writeLong(onlineTime);
		writer.writeLong(threadId);

		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();

		command = reader.readString();
		siger = reader.readInstance(Siger.class);
		source = reader.readInstance(Cabin.class);
		running = reader.readBoolean();
		priority = reader.read();
		direct = reader.readBoolean();
		memory = reader.readBoolean();
		onlineTime = reader.readLong();
		threadId = reader.readLong();

		return reader.getSeek() - seek;
	}

}