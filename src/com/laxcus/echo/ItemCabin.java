/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 异步缓存单元监听地址 <br>
 * 是EchoHelp的子类，在服务端使用，用来判断数据来源。通常在EchoInvoker.replyTo方法中使用，放在EchoHead中。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2016
 * @since laxcus 1.0
 */
public final class ItemCabin extends EchoHelp {

	private static final long serialVersionUID = 6058001739826875856L;

	/** 数据来源监听地址。由客户端设置，被服务端使用。与Command.listener作用相同，通常是在传输非Command时使用 **/
	private Cabin listener;

	/**
	 * 构造默认和私有的单元监听地址
	 */
	private ItemCabin() {
		super();
	}

	/**
	 * 构造单元监听地址，指定回显地址
	 * @param cabin Cabin实例
	 */
	public ItemCabin(Cabin cabin) {
		this();
		setListener(cabin);
	}

	/**
	 * 从可类化数据读取器中解析单元监听地址
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ItemCabin(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成单元监听地址的数据副本
	 * @param that ItemCabin实例
	 */
	private ItemCabin(ItemCabin that) {
		super(that);
		listener = that.listener.duplicate();
	}

	/**
	 * 设置回显地址
	 * @param e Cabin实例
	 */
	public void setListener(Cabin e) {
		Laxkit.nullabled(e);

		listener = e;
	}

	/**
	 * 返回回显地址
	 * @return Cabin实例
	 */
	public Cabin getListener() {
		return listener;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.EchoHelp#duplicate()
	 */
	@Override
	public ItemCabin duplicate() {
		return new ItemCabin(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.EchoHelp#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(listener);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.EchoHelp#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		listener = new Cabin(reader);
	}

}