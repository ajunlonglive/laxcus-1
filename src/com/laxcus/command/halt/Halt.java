/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.halt;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 终止连接命令。<br>
 * 
 * 这个命令是上级站点发给下级站点，通知下属站点关闭与自己的一切连接。
 * 
 * @author scott.liang
 * @version 1.1 5/19/2015
 * @since laxcus 1.0
 */
public final class Halt extends Command {

	private static final long serialVersionUID = 9003428672021132344L;

	/**
	 * 构造默认的中断连接命令
	 */
	public Halt() {
		super();
	}

	/**
	 * 根据传入的中断连接命令，生成它的浅层数据副本
	 * @param that 中断连接命令
	 */
	private Halt(Halt that) {
		super(that);
	}

	/**
	 * 从可类化数据读取器中解析中断连接命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Halt(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Halt duplicate() {
		return new Halt(this);
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