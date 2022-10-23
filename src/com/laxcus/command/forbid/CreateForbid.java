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
 * 提交禁止操作命令。<br>
 * 
 * 禁止操作发生后，相关操作的读写相关申请都被禁止。
 * 
 * @author scott.liang
 * @version 1.0 3/31/2017
 * @since laxcus 1.0
 */
public class CreateForbid extends PostForbid {

	private static final long serialVersionUID = 428627556735005373L;

	/**
	 * 构造默认的提交禁止操作命令
	 */
	public CreateForbid() {
		super();
	}

	/**
	 * 构造提交禁止操作命令，指定一个禁止操作单元
	 * @param item 禁止操作单元
	 */
	public CreateForbid(ForbidItem item) {
		this();
		add(item);
	}

	/**
	 * 构造提交禁止操作命令，指定一批禁止操作单元
	 * @param array 禁止操作单元数组
	 */
	public CreateForbid(Collection<ForbidItem> array) {
		this();
		addAll(array);
	}
	
	/**
	 * 生成提交禁止操作命令的数据副本
	 * @param that CreateForbid实例
	 */
	private CreateForbid(CreateForbid that) {
		super(that);
	}
	
	/**
	 * 从可类化数据读取器中解析提交禁止操作命令
	 * @param reader 可类化数据读取器
	 */
	public CreateForbid(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CreateForbid duplicate() {
		return new CreateForbid(this);
	}

}