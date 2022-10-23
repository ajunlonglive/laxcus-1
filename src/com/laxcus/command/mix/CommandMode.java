/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 命令处理模式
 * 
 * @author scott.liang
 * @version 1.1 09/09/2015
 * @since laxcus 1.0
 */
public final class CommandMode extends Command {

	private static final long serialVersionUID = 8542133458587700361L;

	/** 处理模式选项：内存/磁盘 **/
	public static final int DISK = 1;

	public static final int MEMORY = 2;

	/** 处理模式 **/
	private int mode;

	/**
	 * 构造命令处理模式
	 */
	public CommandMode() {
		super();
	}

	/**
	 * 构造命令处理模式，指定内存
	 * @param memory 内存或者否
	 */
	public CommandMode(boolean memory) {
		this();
		setMode(memory ? CommandMode.MEMORY : CommandMode.DISK);
	}

	/**
	 * 生成当前实例的数据副本
	 * @param that CommandMode实例
	 */
	private CommandMode(CommandMode that) {
		super(that);
		mode = that.mode;
	}

	/**
	 * 设置处理模式
	 * @param who 处理模式
	 */
	public void setMode(int who) {
		switch (who) {
		case CommandMode.DISK:
		case CommandMode.MEMORY:
			mode = who;
			break;
		default:
			throw new IllegalValueException("illegal %d", who);
		}
	}

	/**
	 * 返回处理模式
	 * @return 处理模式
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * 判断是内存处理
	 * @return 返回真或者假
	 */
	public boolean isMemoryMode() {
		return mode == CommandMode.MEMORY;
	}

	/**
	 * 判断是磁盘处理
	 * @return 返回真或者假
	 */
	public boolean isDiskMode() {
		return mode == CommandMode.DISK;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CommandMode duplicate() {
		return new CommandMode(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(mode);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		mode = reader.readInt();
	}

}