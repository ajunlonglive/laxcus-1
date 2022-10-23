/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.util.net.*;

/**
 * 原生数据包。<br>
 * 保存UDP数据包来源地址和字节数据。
 * 
 * 用在FixpPacketHelper、ReplyHelper、ReplyWorker。
 * 
 * @author scott.liang
 * @version 1.0 8/2/2009
 * @since laxcus 1.0
 */
public final class PrimitivePacket {

	/** 数据来源地址 **/
	private SocketHost remote;

	/** 数据包原始数据 **/
	private byte[] data;
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		if (remote != null) {
			remote = null;
		}
		if (data != null) {
			data = null;
		}
	}

	/**
	 * 构造原生数据包
	 * @param endpoint 目标站点
	 * @param b 数据报字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 */
	public PrimitivePacket(SocketHost endpoint, byte[] b, int off, int len) {
		remote = endpoint;
		data = Arrays.copyOfRange(b, off, off + len);
	}

	/**
	 * 返回来源地址
	 * @return SocketHost地址
	 */
	public SocketHost getRemote() {
		return remote;
	}

	/**
	 * 返回数据缓存
	 * @return 字节数组
	 */
	public byte[] getBuffer() {
		return data;
	}
	
	/**
	 * 返回数据缓存长度
	 * @return 整数
	 */
	public int length() {
		return data.length;
	}

	/**
	 * 还原成FIXP数据包
	 * @return 返回FIXP包实例，或者空指针
	 */
	public Packet regress() {
		try {
			return new Packet(remote, data, 0, data.length);
		} catch (FixpProtocolException e) {
			Logger.error(e);
		}
		return null;
	}
}