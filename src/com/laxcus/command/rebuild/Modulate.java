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
 * 数据调整命令。<br>
 * 命令格式：modulate [schema.table|schema.table/column]
 * 
 * MODULATE命令是REGULATE命令在BUILD站点的实现。它们执行相同的工作，只是站点位置不同。
 * 
 * @author scott.liang
 * @version 1.1 6/30/2015
 * @since laxcus 1.0
 */
public final class Modulate extends Command {

	private static final long serialVersionUID = 4784589888970733998L;

	/** 列空间 **/
	private Dock dock;

	/**
	 * 根据传入的数据调整命令，生成它的数据副本
	 * @param that Modulate实例
	 */
	private Modulate(Modulate that) {
		super(that);	
		dock = that.dock;
	}

	/**
	 * 构造默认的数据调整命令。
	 */
	private Modulate() {
		super();
	}

	/**
	 * 构造数据调整命令，指定列空间
	 * @param dock 列空间
	 */
	public Modulate(Dock dock) {
		this();
		setDock(dock);
	}

	/**
	 * 从可类化数据读取器中解析数据优化命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Modulate(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回列空间
	 * @return Dock实例
	 */
	public Dock getDock() {
		return dock;
	}

	/**
	 * 设置列空间
	 * @param e Dock实例
	 */
	public void setDock(Dock e) {
		Laxkit.nullabled(e);

		dock = e;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Modulate duplicate() {
		return new Modulate(this);
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
		// 列空间
		dock = new Dock(reader);
	}

}