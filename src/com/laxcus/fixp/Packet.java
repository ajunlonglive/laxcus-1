/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * FIXP数据包(UDP模式)。<br>
 * 
 * @author scott.liang
 * @version 1.1 3/13/2012
 * @since laxcus 1.0
 */
public class Packet extends Entity {

	/**
	 * 根据传入的FIXP数据包实例，生成它的数据副本。
	 * @param that Packet实例
	 */
	protected Packet(Packet that) {
		super(that);
	}

	/**
	 * 构造一个默认的FIXP UDP数据包
	 */
	public Packet() {
		super(SocketTag.UDP);
	}

	/**
	 * 构造一个FIXP数据包，同时指定它的操作行为
	 * @param mark FIXP协议标头
	 */
	public Packet(Mark mark) {
		this();
		setMark(mark);
	}

	/**
	 * 构造一个FIXP数据包，同时指定它的连接地址
	 * @param endpoint 目标地址
	 */
	public Packet(SocketHost endpoint) {
		this();
		setRemote(endpoint);
	}

	/**
	 * 构造一个FIXP数据包，同时指定它的请求标记
	 * @param major FIXP主码
	 * @param minor FIXP从码
	 */
	public Packet(byte major, byte minor) throws FixpParameterException {
		this(new Mark(major, minor));
	}

	/**
	 * 构造一个FIXP数据包，同时指定它的应答码
	 * @param reply FIXP应答码
	 */
	public Packet(short reply) throws FixpParameterException {
		this(new Mark(reply));
	}

	/**
	 * 构造一个FIXP数据包，同时指定它命令和连接地址
	 * @param endpoint 目标地址
	 * @param mark FIXP协议标头
	 */
	public Packet(SocketHost endpoint, Mark mark) {
		this(endpoint);
		setMark(mark);
	}

	/**
	 * 构造一个FIXP数据包，同时指定它的连接地址和请求命令
	 * @param endpoint 目标地址
	 * @param major 请求主码
	 * @param minor 请求从码
	 */
	public Packet(SocketHost endpoint, byte major, byte minor) throws FixpParameterException {
		this(endpoint, new Mark(major, minor));
	}

	/**
	 * 构造一个FIXP数据包，同时指定它的连接地址和应答码
	 * @param endpoint 目标地址
	 * @param reply 应答编码
	 */
	public Packet(SocketHost endpoint, short reply) throws FixpParameterException {
		this(endpoint, new Mark(reply));
	}

	/**
	 * 构造一个FIXP数据包，解析包数据
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 */
	public Packet(byte[] b, int off, int len) throws FixpProtocolException {
		this();
		// 解析数据包
		resolve(b, off, len);
	}

	/**
	 * 构造一个FIXP数据包，指定目标地址和解析包数据
	 * @param remote 目标地址
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @throws FixpProtocolException
	 */
	public Packet(SocketHost remote, byte[] b, int off, int len) throws FixpProtocolException {
		this(remote);
		// 解析数据包
		resolve(b, off, len);
	}

	/**
	 * 判断是FIXP UDP子包。通过子包统计数和子包编号两个参数确定。
	 * @return 返回真或者假
	 */
	public boolean isSubPacket() {
		Message e1 = findMessage(MessageKey.SUBPACKET_COUNT);
		Message e2 = findMessage(MessageKey.SUBPACKET_SERIAL);
		return e1 != null && e2 != null;
	}

	/**
	 * 从可类化读取器中解析数据包的参数
	 * @param reader 可类化读取器
	 * @return 返回读取的字节长度
	 * @throws FixpProtocolExcpetion
	 */
	public int resolve(ClassReader reader) throws FixpProtocolException {
		final int seek = reader.getSeek();
		// 解析命令
		mark = new Mark(reader);
		// 解析消息
		int items = mark.getMessages();
		for (int i = 0; i < items; i++) {
			Message e = new Message(reader);
			addMessage(e);
		}
		// 读数据域
		int len = getContentLength();
		if (len > 0) {
			data = reader.read(len);
		}
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 从字节数组中解析数据包，返回解析的字节长度
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 返回解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) throws FixpProtocolException {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}

}