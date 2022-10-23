/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.fast;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * “显示数据块尺寸”命令。<br>
 * 命令格式: SHOW ENTITY SIZE 数据库名.表名 
 * 
 * @author scott.liang
 * @version 1.2 12/3/2013
 * @since laxcus 1.0
 */
public final class ShowEntitySize extends FastSpace {
	
	private static final long serialVersionUID = -7852887628025874624L;

	/**
	 * 构造“显示数据块尺寸”命令
	 */
	private ShowEntitySize() {
		super();
	}
	
	/**
	 * 根据传入的“显示数据块尺寸”命令，生成它的数据副本
	 * @param that PrintEntitySize实例
	 */
	private ShowEntitySize(ShowEntitySize that) {
		super(that);
	}

	/**
	 * 构造“显示数据块尺寸”命令，指定数据表名
	 * @param space 数据表名
	 */
	public ShowEntitySize(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析“显示数据块尺寸”命令参数
	 * @param reader 可类化读取器
	 * @since 1.2
	 */
	public ShowEntitySize(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShowEntitySize duplicate() {
		return new ShowEntitySize(this);
	}

}