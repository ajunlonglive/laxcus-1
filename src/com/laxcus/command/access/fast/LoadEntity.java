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
 * 数据块加载命令。<br>
 * 这个命令由终端发起，通过TOP、HOME、DATA站点的传递，把磁盘中的数据块加载到内存中，提高检索速度。<br>
 * 实现的结果是相当于一个内存数据库的作用。
 * 
 * @author scott.liang
 * @version 1.1 6/30/2015
 * @since laxcus 1.0
 */
public class LoadEntity extends FastMass {

	private static final long serialVersionUID = -2880900766014862114L;

	/**
	 * 构造数据块加载命令
	 */
	private LoadEntity() {
		super();
	}

	/**
	 * 根据传入的数据块加载命令实例，生成它的数据副本
	 * @param that LoadEntity实例
	 */
	private LoadEntity(LoadEntity that) {
		super(that);
	}

	/**
	 * 构造数据块加载命令，指定数据表名
	 * @param space 数据表名
	 */
	public LoadEntity(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析数据块加载命令
	 * @param reader 可类化读取器
	 * @since laxcus 1.1
	 */
	public LoadEntity(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public LoadEntity duplicate() {
		return new LoadEntity(this);
	}

}