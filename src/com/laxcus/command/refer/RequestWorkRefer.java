/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import com.laxcus.util.classable.*;

/**
 * 请求分配WORK站点资源引用。
 * 
 * @author scott.liang
 * @version 1.1 05/17/2015
 * @since laxcus 1.0
 */
public final class RequestWorkRefer extends RequestRefer {

	private static final long serialVersionUID = 5948425842950447192L;

	/**
	 * 根据传入的实例，生成它的数据副本
	 * @param that 请求分配WORK站点资源引用实例
	 */
	private RequestWorkRefer(RequestWorkRefer that) {
		super(that);
	}
	
	/**
	 * 构造默认的请求分配WORK站点资源引用
	 */
	public RequestWorkRefer() {
		super();
	}
	
	/**
	 * 构造请求分配WORK站点资源引用，指定内存空间
	 * @param size 内存空间
	 */
	public RequestWorkRefer(long size) {
		super();
		setSize(size);
	}

	/**
	 * 从可类化数据读取器中解析请求分配WORK站点资源引用命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RequestWorkRefer(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RequestWorkRefer duplicate() {
		return new RequestWorkRefer(this);
	}

}