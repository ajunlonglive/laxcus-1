/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 打印FRONT网关站点命令。<br><br>
 * 
 * 只在FRONT站点使用。
 * 
 * @author scott.liang
 * @version 1.0 02/15/2018
 * @since laxcus 1.0
 */
public final class PrintFrontGateway extends Command {

	private static final long serialVersionUID = -957141642008814789L;

	/**
	 * 构造默认的打印FRONT网关站点命令
	 */
	public PrintFrontGateway() {
		super();
	}

	/**
	 * 根据传入的命令实例，生成它的数据副本
	 * @param that PrintFrontGateway实例
	 */
	private PrintFrontGateway(PrintFrontGateway that) {
		super(that);
	}

	/**
	 * 从可类化数据读取器中解析打印FRONT网关站点命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public PrintFrontGateway(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PrintFrontGateway duplicate() {
		return new PrintFrontGateway(this);
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