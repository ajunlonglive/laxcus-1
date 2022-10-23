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
 * 请求分配BUILDK站点资源引用。
 * 
 * @author scott.liang
 * @version 1.1 05/17/2015
 * @since laxcus 1.0
 */
public final class RequestBuildRefer extends RequestRefer {

	private static final long serialVersionUID = 7844835762028794787L;

	/**
	 * 根据传入的实例，生成它的数据副本
	 * @param that 请求分配BUILDK站点资源引用实例
	 */
	private RequestBuildRefer(RequestBuildRefer that) {
		super(that);
	}
	
	/**
	 * 请求分配BUILDK站点资源引用命令，指定内存空间
	 * @param size 内存空间
	 */
	public RequestBuildRefer(long size) {
		super();
		setSize(size);
	}
	
	/**
	 * 构造默认的请求分配BUILDK站点资源引用命令
	 */
	public RequestBuildRefer() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析请求分配BUILDK站点资源引用命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RequestBuildRefer(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RequestBuildRefer duplicate() {
		return new RequestBuildRefer(this);
	}

}