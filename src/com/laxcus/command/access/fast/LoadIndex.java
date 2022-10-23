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
 * 数据索引加载命令。<br>
 * 这个命令由终端发起，通过TOP、HOME、DATA站点的传递，把磁盘数据块的索引加载到内存中，提高检索速度。
 * 
 * @author scott.liang
 * @version 1.1 6/30/2015
 * @since laxcus 1.0
 */
public class LoadIndex extends FastMass {

	private static final long serialVersionUID = 1994088733593464543L;

	/**
	 * 构造数据索引加载命令
	 */
	private LoadIndex() {
		super();
	}

	/**
	 * 构造构造数据索引加载命令，指定数据表名
	 * @param space 数据表名
	 */
	public LoadIndex(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析数据索引加载命令
	 * @param reader 可类化读取器
	 * @since laxcus 1.1
	 */
	public LoadIndex(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的数据索引加载命令实例，生成它的数据副本
	 * @param that LoadIndex实例
	 */
	private LoadIndex(LoadIndex that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public LoadIndex duplicate() {
		return new LoadIndex(this);
	}

}