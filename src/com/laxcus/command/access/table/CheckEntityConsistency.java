/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * 检查分布环境下数据块内容的一致性
 * 
 * @author scott.liang
 * @version 1.0 9/21/2015
 * @since laxcus 1.0
 */
public final class CheckEntityConsistency extends ProcessTable {

	private static final long serialVersionUID = 6302026770836804466L;

	/** 详细记录  **/
	private boolean detail;

	/**
	 * 构造检测表数据一致性
	 */
	public CheckEntityConsistency() {
		super();
		detail = false;
	}

	/**
	 * 根据传入检测表数据一致性，生成它的数据副本
	 * @param that CheckEntityConsistency实例
	 */
	private CheckEntityConsistency(CheckEntityConsistency that) {
		super(that);
		detail = that.detail;
	}

	/**
	 * 构造检测表数据一致性，指定数据表名
	 * @param space 数据表名
	 */
	public CheckEntityConsistency(Space space) {
		this();
		setSpace(space);
	}
	
	/**
	 * 从可类化读取器中解析检测表数据一致性
	 * @param reader 可类化数据读取器
	 */
	public CheckEntityConsistency(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置为要求详细记录
	 * @param b 真或者假
	 */
	public void setDetail(boolean b) {
		detail = b;
	}

	/**
	 * 判断要求详细记录
	 * @return 真或者假
	 */
	public boolean isDetail() {
		return detail;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CheckEntityConsistency duplicate() {
		return new CheckEntityConsistency(this);
	}

	/**
	 * 将被处理的表名写入可类化存储器
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeBoolean(detail);
	}

	/**
	 * 从可类化读取器中解析被处理的表名
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		detail = reader.readBoolean();
	}

}