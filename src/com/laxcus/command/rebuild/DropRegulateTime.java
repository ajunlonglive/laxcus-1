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
 * 撤销数据表优化命令。
 * 
 * @author scott.liang
 * @version 1.1 2/2/2014
 * @since laxcus 1.0
 */
public class DropRegulateTime extends Command {

	private static final long serialVersionUID = -1550440547605407587L;

	/** 数据表名  **/
	private Space space;

	/**
	 * 根据传入实例生成它的数据副本
	 * @param that DropRegulateTime实例
	 */
	private DropRegulateTime(DropRegulateTime that) {
		super(that);
		space = that.space;
	}

	/**
	 * 构造默认的撤销数据表优化命令
	 */
	public DropRegulateTime() {
		super();
	}

	/**
	 * 构造撤销数据表优化，指定启动时间
	 * @param space 数据表名
	 */
	public DropRegulateTime(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 设置撤销数据表优化
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);
		space = e;
	}

	/**
	 * 返回撤销数据表优化
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropRegulateTime duplicate() {
		return new DropRegulateTime(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
	}

}