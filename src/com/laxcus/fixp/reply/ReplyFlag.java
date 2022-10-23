/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import java.io.*;

import com.laxcus.echo.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * 异步快速传输标识 <br><br>
 * 
 * 参数组成：<br>
 * 1. 通信源地址 <br>
 * 2. 异步通信码 <br><br>
 * 
 * 两个参数组合，标识在ReplySucker/ReplyWorker中的每一个通信的唯一性。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/21/2018
 * @since laxcus 1.0
 */
public final class ReplyFlag implements Serializable, Cloneable, Classable, Comparable<ReplyFlag> {

	private static final long serialVersionUID = 5050612737641252218L;

	/** 通信源地址 **/
	private SocketHost remote;

	/** 异步通信码 **/
	private CastCode code;

	/**
	 * 根据传入的异步快速传输标识实例，生成它的浅层数据副本
	 * @param that
	 */
	private ReplyFlag(ReplyFlag that) {
		this();
		remote = that.remote;
		code = that.code;
	}

	/**
	 * 构造异步快速传输标识
	 */
	public ReplyFlag() {
		super();
	}

	/**
	 * 构造异步快速传输标识，指定全部参数
	 * @param remote 通信源地址
	 * @param code 异步通信码
	 */
	public ReplyFlag(SocketHost remote, CastCode code) {
		this();
		setRemote(remote);
		setCode(code);
	}

	/**
	 * 从可类化数据读取器中解析异步快速传输标识
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ReplyFlag(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置通信源地址
	 * @param e 通信源地址
	 */
	public void setRemote(SocketHost e) {
		Laxkit.nullabled(e);

		remote = e;
	}

	/**
	 * 返回通信源地址
	 * @return 通信源地址
	 */
	public SocketHost getRemote() {
		return remote;
	}

	/**
	 * 设置异步通信码
	 * @param e 异步通信码
	 */
	public void setCode(CastCode e) {
		Laxkit.nullabled(e);
		code = e;
	}

	/**
	 * 返回异步通信码
	 * @return 异步通信码
	 */
	public CastCode getCode() {
		return code;
	}
	
	/**
	 * 生成异步快速传输标识的浅层数据副本
	 * @return 异步快速传输标识实例
	 */
	public ReplyFlag duplicate() {
		return new ReplyFlag(this);
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
		return String.format("%s#%s", remote.getAddress(), code);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ReplyFlag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((ReplyFlag) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (remote.getAddress().hashCode() ^ code.hashCode());
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ReplyFlag that) {
		if (that == null) {
			return 1;
		}
		// 注意！因为采用MASSIVE MIMO多址发送数据，会出现从多个MIDipatcher和ReplyDispatcher发送数据包的现象，每个MISucker和ReplyDipathcer，
		// 它们的端口是不一样的。所以，必须忽略端口的比较，只比较IP地址！
		// 同时，我们还要保存端口，因为存在ReplyReceiver向MODispatcher/ReplyDipathcer发送应答，所以端口仍然有效，保证一个即可！
		int ret = Laxkit.compareTo(remote.getAddress(), that.remote.getAddress());
		if (ret == 0) {
			ret = Laxkit.compareTo(code, that.code);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(remote);
		writer.writeObject(code);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		remote = new SocketHost(reader);
		code = new CastCode(reader);
		return reader.getSeek() - seek;
	}

}