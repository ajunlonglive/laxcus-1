/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cyber;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 检测内网/公网、网段之间的穿透信道。<br>
 * 
 * 这个命令只在FRONT节点执行，检查FRONT与ENTRANCE、GATE、CALL节点之间的通信信道，包括命令控制信道和数据传输信道。
 * 
 * @author scott.liang
 * @version 1.0 2/2/2021
 * @since laxcus 1.0
 */
public class CheckPockChannel extends Command {

	private static final long serialVersionUID = -7690762608712793679L;

	/**
	 * 根据传入的检测穿透信道命令，生成它的数据副本
	 * @param that 检测穿透信道命令
	 */
	private CheckPockChannel(CheckPockChannel that) {
		super(that);
	}

	/**
	 * 构造检测穿透信道命令
	 */
	public CheckPockChannel() {
		super();
	}
	

	/**
	 * 从可类化数据读取器中解析检测穿透信道命令
	 * @param reader 可类化数据读取器
	 */
	public CheckPockChannel(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CheckPockChannel duplicate() {
		return new CheckPockChannel(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// TODO Auto-generated method stub

	}

}