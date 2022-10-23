/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

import java.io.*;

import com.laxcus.util.net.*;

/**
 * FIXP数据流(TCP模式)。相对于FIXP数据包。<br>
 * 
 * @author scott.liang
 * @version 1.1 3/13/2012
 * @since laxcus 1.0
 */
public class Stream extends Entity {

	/** FIXP协议数据读取器 **/
	private FixpInputStream input;

	/** FIXP协议数据输出器 **/
	private FixpOutputStream output;
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.Entity#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		input = null;
		output = null;
	}

	/**
	 * 根据传入的FIXP数据流实例，生成它的副本
	 * @param that
	 */
	private Stream(Stream that) {
		super(that);
		input = that.input;
		output = that.output;
	}

	/**
	 * 构造一个默认的FIXP数据流。
	 */
	public Stream() {
		super(SocketTag.TCP);
	}

	/**
	 * 构造FIXP数据流，指定它的连接地址
	 * @param endpoint 连接地址
	 */
	public Stream(SocketHost endpoint) {
		this();
		setRemote(endpoint);
	}

	/**
	 * 构造FIXP数据流，指定它的连接地址、输入/输出读句柄
	 * @param endpoint 连接地址
	 * @param in FIXP读取器
	 * @param out FIXP输出器
	 */
	public Stream(SocketHost endpoint, FixpInputStream in, FixpOutputStream out) {
		this(endpoint);
		setInput(in);
		setOutput(out);
	}
	
	/**
	 * 构造FIXP数据流，指定它的命令
	 * @param mark FIXP协议标头
	 */
	public Stream(Mark mark) {
		this();
		setMark(mark);
	}

	/**
	 * 构造FIXP数据流，指定它的连接地址和命令
	 * @param endpoint 连接地址
	 * @param mark FIXP协议标头
	 */
	public Stream(SocketHost endpoint, Mark mark) {
		this(endpoint);
		setMark(mark);
	}

	/**
	 * 构造一个FIXP数据流，同时指定它的请求标记
	 * @param major 请求主码
	 * @param minor 请求从码
	 */
	public Stream(byte major, byte minor) throws FixpParameterException {
		this(new Mark(major, minor));
	}

	/**
	 * 构造一个FIXP数据流，同时指定它的应答码
	 * @param reply FIXP应答码
	 */
	public Stream(short reply) throws FixpParameterException {
		this(new Mark(reply));
	}

	/**
	 * 构造一个FIXP数据流，同时指定它的连接地址和请求命令
	 * @param endpoint 目标地址
	 * @param major 请求主码
	 * @param minor 请求从码
	 */
	public Stream(SocketHost endpoint, byte major, byte minor) throws FixpParameterException {
		this(endpoint, new Mark(major, minor));
	}

	/**
	 * 构造一个FIXP数据流，同时指定它的连接地址和应答码
	 * @param endpoint 目标地址
	 * @param reply FIXP应答码
	 */
	public Stream(SocketHost endpoint, short reply) throws FixpParameterException {
		this(endpoint, new Mark(reply));
	}
	
	/**
	 * 设置FIXP通信输入流句柄
	 * @param e FIXP通信输入流句柄
	 */
	public void setInput(FixpInputStream e) {
		input = e;
	}

	/**
	 * 返回FIXP通信输入流句柄
	 * @return FixpInputStream实例
	 */
	public InputStream getInput() {
		return input;
	}

	/**
	 * 设置FIXP通信输出流句柄
	 * @param e FixpOutputStream实例
	 */
	public void setOutput(FixpOutputStream e) {
		output = e;
	}

	/**
	 * 返回FIXP通信输出流句柄
	 * @return FixpOutputStream实例
	 */
	public OutputStream getOutput() {
		return output;
	}

	/**
	 * 从FIXP SOCKET读取流中读出数据
	 * @param in 读取流
	 * @param out 写入流
	 * @param readBody 判断读数据块
	 * @return 返回读取的数据流长度
	 * @throws IOException
	 */
	public long read(FixpInputStream in, FixpOutputStream out, boolean readBody) throws IOException {
		input = in;
		output = out;
		
		// 数据流量
		long flows = 0L;

		// 读FIXP协议标头
		byte[] b = input.readFully(MarkIdentity.SIZE);
		// 读并且解析全部消息
		try {
			mark = new Mark(b, 0, b.length);
			
			// 统计接收的数据流量
			flows += b.length;
			
			int count = mark.getMessages();
			for (int i = 0; i < count; i++) {
				Message msg = new Message();
				int len = msg.resolve(in);
				addMessage(msg);
				// 统计接收的数据流量
				if (len > 0) flows += len;
			}
		} catch (FixpProtocolException exp) {
			throw new IOException(exp);
		}

		// 读数据域
		if (readBody) {
			int len = readBody();
			// 统计接收的数据流量
			if (len > 0) flows += len;
		}
		// 返回接收的数据流量
		return flows;
	}

	/**
	 * 从FIXP SOCKET读取流中读出数据
	 * @param readBody 判断读数据块
	 * @return 返回读取的数据流长度
	 * @throws IOException
	 */
	public long read(boolean readBody) throws IOException {
		// 数据流量
		long flows = 0L;

		// 读操作行为
		byte[] b = input.readFully(MarkIdentity.SIZE);
		// 读并且解析全部消息
		try {
			mark = new Mark(b, 0, b.length);
			// 统计接收的数据流量
			flows += b.length;
			
			int count = mark.getMessages();
			for (int i = 0; i < count; i++) {
				Message message = new Message();
				int len = message.resolve(input);
				addMessage(message);
				// 统计接收的数据流量
				if (len > 0) flows += len;
			}
		} catch (FixpProtocolException e) {
			throw new IOException(e);
		}

		// 读数据域
		if (readBody) {
			int len = readBody();
			// 统计接收的数据流量
			if (len > 0) flows += len;
		}
		// 返回接收的数据流量
		return flows;
	}

	/**
	 * 判断数据域长度，然后从输入流中读出数据域，并且返回数据域尺寸
	 * @return 返回读取的数据块长度
	 * @throws IOException
	 */
	public int readBody() throws IOException {
		int len = getContentLength();
		if(len > 0) {
			data = input.readFully(len);
		}
		return len;
	}

	/**
	 * 判断数据域长度，读出本次数据域字节数组并且输出
	 * @return 数据域的字节数组
	 * @throws IOException
	 */
	public byte[] readContent() throws IOException {
		int len = readBody();
		if(len < 1) return null;
		return data;
	}
	
	/**
	 * 指定字节数据缓存，把FIXP SOCKET中的数据读取出来，并保存的缓存中
	 * @param b  输出的缓存字节数组
	 * @param off 指定下标
	 * @param len 长度
	 * @return 返回读取的长度
	 * @throws IOException
	 */
	public int readFully(byte[] b, int off, int len) throws IOException {
		return input.readFully(b, off, len);
	}

	/**
	 * 指定要读取的数据长度，把FIXP SOCKET中的数据读取出来，并且输出
	 * @param len 指定长度
	 * @return 返回读取的字节数组
	 * @throws IOException
	 */
	public byte[] readFully(int len) throws IOException {
		return input.readFully(len);
	}

}