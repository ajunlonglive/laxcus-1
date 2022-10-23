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
 * 命令优先级。<br>
 * 只允许WATCH节点使用。设置成功后，所有通过WATCH节点发出的命令都遵循这个定义！
 * 
 * @author scott.liang
 * @version 1.0 1/22/2020
 * @since laxcus 1.0
 */
public final class CommandRank extends Command {

	private static final long serialVersionUID = -6184666614341069406L;

	/** 命令优先级 **/
	private byte rank;

	/**
	 * 构造命令优先级
	 */
	public CommandRank() {
		super();
	}

	/**
	 * 构造命令优先级，指定时间间隔
	 * @param rank 时间间隔
	 */
	public CommandRank(byte rank) {
		this();
		setRank(rank);
	}
	
	/**
	 * 生成命令副本
	 * @param that CommandRank实例
	 */
	private CommandRank(CommandRank that) {
		super(that);
		rank = that.rank;
	}

	/**
	 * 设置命令优先级
	 * @param ms 优先级
	 */
	public void setRank(byte ms) {
		rank = ms;
	}

	/**
	 * 返回命令优先级
	 * @return 优先级
	 */
	public byte getRank() {
		return rank;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CommandRank duplicate() {
		return new CommandRank(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.write(rank);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		rank = reader.read();
	}

}