/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.fast;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 快速表处理命令。<br>
 * 所有涉及表操作的命令都从此派生。
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public abstract class FastSpace extends Command {

	private static final long serialVersionUID = 4911018525703302610L;

	/** 数据表名 **/
	private Space space;

	/**
	 * 构造默认的快速表处理命令
	 */
	protected FastSpace() {
		super();
	}

	/**
	 * 根据传入的数据块操作命令，生成它的数据副本
	 * @param that FastSpace实例
	 */
	protected FastSpace(FastSpace that) {
		super(that);
		space = that.space;
	}

	/**
	 * 设置数据表名，不允许空指针
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);
		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 写数据表名
		writer.writeObject(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 数据表名
		space = new Space(reader);
	}

}