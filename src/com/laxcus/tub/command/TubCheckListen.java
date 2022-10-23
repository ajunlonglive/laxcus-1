/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.command;

import com.laxcus.util.classable.*;

/**
 * 检测边缘服务监听，包括IP地址和端口。
 * 
 * 这是提供给终端的类。以“X”开头。
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public class TubCheckListen extends TubCommand {
	
	/**
	 * 构造默认的检测边缘服务监听
	 */
	public TubCheckListen() {
		super();
	}

	/**
	 * 生成检测边缘服务监听的数据副本
	 * @param that 检测边缘服务监听实例
	 */
	private TubCheckListen(TubCheckListen that) {
		super(that);
	}

	/**
	 * 从可类化数据读取器中解析检测边缘服务监听命令
	 * @param reader 可类化数据读取器
	 */
	public TubCheckListen(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TubCheckListen duplicate() {
		return new TubCheckListen(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {

	}

}