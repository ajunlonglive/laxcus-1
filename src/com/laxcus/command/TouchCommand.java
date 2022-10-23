/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 定时触发命令 <br>
 * 
 * TouchCommand是一个固定类，包含一个被准备发送的命令，但是它没有子类。
 * TouchCommand放在“CommandPool”管理池中，当达到超时时间后被CommandPool触发，将包含的命令发送出去。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class TouchCommand extends Command {

	private static final long serialVersionUID = -1436713534897603058L;

	/** 实际传递的命令 **/
	private Command command;

	/** 触发时间（最终抵达时间） **/
	private long touchTime;

	/**
	 * 构造默认和私有的定时触发命令
	 */
	private TouchCommand() {
		super();
	}

	/**
	 * 生成一个定时触发命令副本
	 * @param that TouchCommand实例
	 */
	private TouchCommand(TouchCommand that) {
		super(that);
		command = that.command;
		touchTime = that.touchTime;
	}

	/**
	 * 建立一个定时触发命令，指定这个全部参数
	 * @param cmd 被传递的命令实例
	 * @param touchTime 定时触发时间（达到这个时间后，命令被启用）
	 */
	public TouchCommand(Command cmd, long touchTime) {
		this();
		setCommand(cmd);
		setTouchTime(touchTime);
	}

	/**
	 * 建立一个定时触发命令，指定全部参数
	 * @param timeout 超时时间（触发时间是当前时间加上传入的超时时间，单位：毫秒）
	 * @param cmd 被传递的命令实例
	 */
	public TouchCommand(long timeout, Command cmd) {
		this(cmd, System.currentTimeMillis() + timeout);
	}

	/**
	 * 从可类化数据读取器中解析定时触发命令实例
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TouchCommand(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置真实命令
	 * @param e Command命令实例
	 */
	public void setCommand(Command e) {
		Laxkit.nullabled(e);

		command = e;
	}

	/**
	 * 返回真实命令
	 * @return Command命令实例
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * 设置触发超时时间
	 * @param time 超时时间
	 */
	public void setTouchTime(long time) {
		if (time > 0) touchTime = time;
	}

	/**
	 * 返回触发超时时间，单位：毫秒
	 * @return 超时时间
	 */
	public long getTouchTime() {
		return touchTime;
	}

	/**
	 * 判断触发超时
	 * @return 返回真或者假
	 */
	public boolean isTouchTimeout() {
		return System.currentTimeMillis() >= touchTime;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setMemory(boolean)
	 */
	@Override
	public void setMemory(boolean b) {
		super.setMemory(b);
		if (command != null) {
			command.setMemory(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setDisk(boolean)
	 */
	@Override
	public void setDisk(boolean b) {
		super.setDisk(b);
		if (command != null) {
			command.setDisk(b);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TouchCommand duplicate() {
		return new TouchCommand(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeDefault(command);
		writer.writeLong(touchTime);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		command = (Command) reader.readDefault();
		touchTime = reader.readLong();
	}

}