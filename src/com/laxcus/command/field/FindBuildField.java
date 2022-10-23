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
 * 查找BUILD站点元数据命令。<br>
 * 
 * @author scott.liang
 * @version 1.1 10/12/2015
 * @since laxcus 1.0
 */
public final class FindBuildField extends FindField {

	private static final long serialVersionUID = -5877964658394420594L;

	/**
	 * 根据传入的查找BUILD站点元数据命令，生成它的数据副本
	 * @param that FindBuildField实例
	 */
	private FindBuildField(FindBuildField that) {
		super(that);
	}
	
	/**
	 * 构造默认的查找BUILD站点元数据命令
	 */
	public FindBuildField() {
		super();
	}

	/**
	 * 构造查找BUILD站点元数据命令，指定命令发起方地址
	 * @param from 发起方站点地址
	 */
	public FindBuildField(Node from) {
		this();
		setNode(from);
	}

	/**
	 * 从可类化数据读取器中解析查找BUILD站点元数据命令
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public FindBuildField(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindBuildField duplicate() {
		return new FindBuildField(this);
	}

}
