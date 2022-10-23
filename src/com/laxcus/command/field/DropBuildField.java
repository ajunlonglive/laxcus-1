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
 * 释放BUILD站点元数据命令
 * 
 * @author scott.liang
 * @version 1.1 10/12/2015
 * @since laxcus 1.0
 */
public final class DropBuildField extends DropField {

	private static final long serialVersionUID = -8255661350294825415L;

	/**
	 * 构造默认的释放BUILD站点元数据命令
	 */
	private DropBuildField() {
		super();
	}

	/**
	 * 根据传入的命令实例，生成它的数据副本
	 * @param that DropBuildField实例
	 */
	private DropBuildField(DropBuildField that) {
		super(that);
	}

	/**
	 * 构造释放BUILD站点元数据命令，指定源站点地址
	 * @param node 源站点地址
	 */
	public DropBuildField(Node node) {
		this();
		setNode(node);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public DropBuildField(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropBuildField duplicate() {
		return new DropBuildField(this);
	}

}