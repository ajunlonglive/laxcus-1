/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.zone;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查找索引分布区域命令。
 * 
 * @author scott.liang
 * @version 1.1 11/26/2015
 * @since laxcus 1.0
 */
public class FindIndexZone extends Command {

	private static final long serialVersionUID = -5112014614008470254L;

	/** 列空间 **/
	private Dock dock;
	
	/**
	 * 构造默认的查找索引分布区域命令
	 */
	private FindIndexZone() {
		super();
	}

	/**
	 * 根据传入的查找索引分布区域命令，生成它的数据副本
	 * @param that FindIndexZone实例
	 */
	private FindIndexZone(FindIndexZone that) {
		super(that);
		dock = that.dock;
	}

	/**
	 * 构造查找索引分布区域命令，指定列空间
	 * @param dock Dock实例
	 */
	public FindIndexZone(Dock dock) {
		this();
		setDock(dock);
	}

	/**
	 * 从可类化数据读取器解析查找索引分布区域命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FindIndexZone(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置列空间，不允许空指针
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

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindIndexZone duplicate() {
		return new FindIndexZone(this);
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
