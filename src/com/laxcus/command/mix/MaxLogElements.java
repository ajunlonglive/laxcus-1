/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 图形界面日志显示数目。<br><br>
 * 
 * WATCH/FRONT.TERMINAL节点上执行。<br>
 * 
 * 语法格式：SET LOG ELEMENTS 数字 <br>
 * 
 * @author scott.liang
 * @version 1.0 11/7/2019
 * @since laxcus 1.0
 */
public final class MaxLogElements extends Command {

	private static final long serialVersionUID = -6753468496296116112L;

	/** 日志单元数 **/
	private int elements;

	/**
	 * 根据传入的图形界面日志显示数目，生成它的数据副本
	 * @param that MaxLogElements实例
	 */
	private MaxLogElements(MaxLogElements that) {
		super(that);	
		elements = that.elements;
	}

	/**
	 * 构造默认的图形界面日志显示数目。
	 */
	public MaxLogElements() {
		super();
		elements = 0;
	}

	/**
	 * 构造图形界面日志显示数目，指定日志单元数
	 * @param much 日志单元数
	 */
	public MaxLogElements(int much) {
		this();
		setElements(much);
	}

	/**
	 * 从可类化数据读取器中解析数据优化命令
	 * @param reader 可类化数据读取器
	 */
	public MaxLogElements(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置日志单元数，必须大于0
	 * @param n 日志单元数
	 */
	public void setElements(int n) {
		if (n >= 0) elements = n;
	}

	/**
	 * 返回日志单元数
	 * @return 日志单元数
	 */
	public int getElements() {
		return elements;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public MaxLogElements duplicate() {
		return new MaxLogElements(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 日志单元数
		writer.writeInt(elements);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 日志单元数
		elements = reader.readInt();
	}

}