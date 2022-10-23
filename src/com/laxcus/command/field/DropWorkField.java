/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.field;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 释放WORK站点元数据命令。
 * 
 * @author scott.liang
 * @version 1.1 10/12/2015
 * @since laxcus 1.0
 */
public final class DropWorkField extends DropField {

	private static final long serialVersionUID = -5980712577586556969L;

	/**
	 * 构造默认的释放WORK站点元数据命令
	 */
	private DropWorkField() {
		super();
	}

	/**
	 * 根据传入的释放WORK站点元数据命令实例，生成它的数据副本
	 * @param that DropWorkField实例
	 */
	private DropWorkField(DropWorkField that) {
		super(that);
	}
	
	/**
	 * 构造释放WORK站点元数据命令，指定源站点地址
	 * @param node 源站点地址
	 */
	public DropWorkField(Node node) {
		this();
		setNode(node);
	}
	
	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public DropWorkField(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropWorkField duplicate() {
		return new DropWorkField(this);
	}

}