/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.find;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查询表空间关联的DATA主站点命令。<br>
 * 
 * 此命令由WORK站点发出，目标是CALL站点。根据数据表名，查找关联的DATA主站点（非从站点！）。
 * 
 * @author scott.liang
 * @version 1.1 07/12/2015
 * @since laxcus 1.0
 */
public final class FindSpacePrimeSite extends Command {

	private static final long serialVersionUID = 6901017967665856182L;

	/** 数据表名 **/
	private Space space;

	/**
	 * 构造FindSpacePrimeSite实例
	 */
	private FindSpacePrimeSite() {
		super();
	}

	/**
	 * 根据传入的FindSpacePrimeSite实例，生成它的数据副本
	 * @param that FindSpacePrimeSite实例
	 */
	private FindSpacePrimeSite(FindSpacePrimeSite that) {
		super(that);
		space = that.space.duplicate();
	}

	/**
	 * 构造FindSpacePrimeSite实例，设置数据表名
	 * @param space 数据表名
	 */
	public FindSpacePrimeSite(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析FindSpacePrimeSite实例
	 * @param reader 可类化数据读取器
	 * @since 1.0
	 */
	public FindSpacePrimeSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置数据表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindSpacePrimeSite duplicate() {
		return new FindSpacePrimeSite(this);
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
