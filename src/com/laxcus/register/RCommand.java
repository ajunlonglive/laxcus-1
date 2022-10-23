/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

import com.laxcus.command.*;

/**
 * 命令对象实例参数。<br>
 * 
 * @author scott.liang
 * @version 1.1 7/15/2021
 * @since laxcus 1.0
 */
public final class RCommand extends RParameter {

	/** 命令对象 **/
	private Command value;

	/**
	 * 根据传入的对象，生成它的副本
	 * @param that
	 */
	private RCommand(RCommand that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的命令对象实例
	 */
	public RCommand() {
		super(RParameterType.COMMAND);
	}
	
	/**
	 * 建立一个命令对象实例，同时指定它的标题和参数
	 * @param name 参数名称
	 * @param cmd 命令
	 */
	public RCommand(String name, Command cmd) {
		this();
		setName(name);
		setValue(cmd);
	}
	
	/**
	 * 建立一个命令对象实例，同时指定它的标题和参数
	 * @param name 参数名称
	 * @param cmd 命令
	 */
	public RCommand(Naming name, Command cmd) {
		this();
		setName(name);
		setValue(cmd);
	}
	
	/**
	 * 从命令读取器中解析数据
	 * @param reader 命令数据读取器
	 * @since 1.1
	 */
	public RCommand(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置命令实例对象
	 * @param e Command实例
	 */
	public void setValue(Command e) {
		Laxkit.nullabled(e);

		value = e;
	}
	
	/**
	 * 返回命令实例对象
	 * @return Command实例
	 */
	public Command getValue() {
		return value;
	}

	/**
	 * 将命令对象写入命令写入器
	 * @see com.laxcus.distribute.parameter.RParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeDefault(value);
	}

	/**
	 * 从命令读取器中解析一个命令对象
	 * @see com.laxcus.distribute.parameter.RParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		value = (Command) reader.readDefault();
	}

	/**
	 * 生成当前命令对象的实例副本
	 * @see com.laxcus.distribute.parameter.RParameter#duplicate()
	 */
	@Override
	public RCommand duplicate() {
		return new RCommand(this);
	}

}