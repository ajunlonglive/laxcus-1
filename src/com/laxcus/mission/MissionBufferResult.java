/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.mission;

import java.io.*;

import com.laxcus.util.*;

/**
 * 缓存结果。<br>
 * 返回的数据，保存在内存里
 * 
 * @author scott.liang
 * @version 1.0 5/2/2013
 * @since laxcus 1.0
 */
public class MissionBufferResult extends MissionResult {

	/** 数据在缓存 **/
	private byte[] buff;

	/**
	 * 构造默认的缓存结果
	 */
	public MissionBufferResult() {
		super(MissionResultTag.BUFFER);
	}

	/**
	 * 构造缓存结果，指定缓存数据
	 * @param b 缓存数据
	 */
	public MissionBufferResult(byte[] b) {
		this();
		setBuffer(b);
	}

	/**
	 * 保存内存数据
	 * @param e 字节数组
	 */
	public void setBuffer(byte[] e) {
		buff = e;
		
		// 字节数组类
		setThumb(byte[].class);
	}

	/**
	 * 输出内存数据
	 * @return 字节数组
	 */
	public byte[] getBuffer() {
		return buff;
	}

	/**
	 * 返回数据读取接口
	 * @return InputStream子类实例
	 */
	public InputStream getInputStream() throws IOException {
		if (buff != null) {
			return new ByteArrayInputStream(buff);
		}
		throw new IOException("empty data!");
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.driver.mission.TubResult#destroy()
	 */
	@Override
	protected void destroy() {
		super.destroy();
		// 释放内存数据
		if (buff != null) {
			buff = null;
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.driver.mission.TubResult#getProduct(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Class<T> clazz) {
		if (buff == null) {
			return null;
		} else if (!Laxkit.isClassFrom(buff, clazz)) {
			String e = String.format("%s != %s", buff.getClass().getName(), clazz.getName());
			throw new ClassCastException(e);
		}
		// 返回类实例
		return (T) buff;
	}

}