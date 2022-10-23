/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rebuild;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 启动数据优化命令。<br>
 * 
 * 这个命令由GATE站点提交给CALL站点。CALL站点驱动全部DATA主站点，执行数据优化命令（Regulate）。
 * 
 * @author scott.liang
 * @version 1.1 6/30/2015
 * @since laxcus 1.0
 */
public final class PressRegulate extends Command {

	private static final long serialVersionUID = -3889399253279531871L;

	/** 列空间 **/
	private Dock dock;

	/**
	 * 根据传入的数据表优化命令，生成它的数据副本
	 * @param that PressSwitchTime实例
	 */
	private PressRegulate(PressRegulate that) {
		super(that);	
		dock = that.dock;
	}

	/**
	 * 构造默认的数据表优化命令。
	 */
	private PressRegulate() {
		super();
	}

	/**
	 * 构造数据表优化命令，指定列空间
	 * @param dock Dock实例
	 */
	public PressRegulate(Dock dock) {
		this();
		setDock(dock);
	}

	/**
	 * 从可类化数据读取器中解析启动数据优化命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public PressRegulate(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置列空间
	 * @param e Dock实例
	 */
	public void setDock(Dock e) {
		Laxkit.nullabled(e);

		dock = e;
	}
	
	/**
	 * 返回列空间
	 * @return Dock实例
	 */
	public Dock getDock() {
		return dock;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return dock.getSpace();
	}

	/**
	 * 返回索引键
	 * @return 索引键
	 */
	public short getColumnId() {
		return dock.getColumnId();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PressRegulate duplicate() {
		return new PressRegulate(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(dock);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		dock = new Dock(reader);
	}

}