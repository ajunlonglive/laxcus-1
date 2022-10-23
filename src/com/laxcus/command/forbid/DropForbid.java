/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.forbid;

import java.util.*;

import com.laxcus.law.forbid.*;
import com.laxcus.util.classable.*;

/**
 * 撤销禁止操作命令。<br>
 * 
 * 
 * @author scott.liang
 * @version 1.0 3/31/2017
 * @since laxcus 1.0
 */
public class DropForbid extends PostForbid {

	private static final long serialVersionUID = 3499098624348821785L;

	/**
	 * 构造默认的撤销禁止操作命令
	 */
	public DropForbid() {
		super();
	}

	/**
	 * 生成撤销禁止操作命令的数据副本
	 * @param that DropForbid实例
	 */
	private DropForbid(DropForbid that) {
		super(that);
	}
	
	/**
	 * 构造撤销禁止操作命令，指定一批禁止操作单元
	 * @param array 禁止操作单元数组
	 */
	public DropForbid(Collection<ForbidItem> array) {
		this();
		addAll(array);
	}
	
	/**
	 * 从可类化数据读取器中解析撤销禁止操作命令
	 * @param reader 可类化数据读取器
	 */
	public DropForbid(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropForbid duplicate() {
		return new DropForbid(this);
	}

}