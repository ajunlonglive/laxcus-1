/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command;

import com.laxcus.util.classable.*;

/**
 * “知道”命令。<br><br>
 * 
 * 这个命令通常是“接收端”发送给“请求端”，表示接收端已经收到请求端发来的命令或者数据。
 * 
 * @author scott.liang
 * @version 1.1 3/12/2015
 * @since laxcus 1.0
 */
public final class Well extends Command {

	private static final long serialVersionUID = 2146603077996587447L;

	/**
	 * 构造默认的“知道”命令
	 */
	public Well() {
		super();
	}

	/**
	 * 根据传入的命令实例，生成它的数据副本
	 * @param that Well实例
	 */
	private Well(Well that) {
		super(that);
	}

	/**
	 * 从可类化数据读取器中解析“知道”命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Well(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Well duplicate() {
		return new Well(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// TODO Auto-generated method stub

	}

}
