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
 * 数据块卸载命令。
 * 
 * @author scott.liang
 * @version 1.1 6/30/2015
 * @since laxcus 1.0
 */
public class StopEntity extends FastMass {

	private static final long serialVersionUID = -817932643508745328L;

	/**
	 * 构造数据块卸载命令
	 */
	private StopEntity() {
		super();
	}

	/**
	 * 根据传入的数据块卸载命令实例，生成它的数据副本
	 * @param that StopEntity实例
	 */
	private StopEntity(StopEntity that) {
		super(that);
	}

	/**
	 * 构造数据块卸载命令，指定数据表名
	 * @param space 数据表名
	 */
	public StopEntity(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析数据块卸载命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public StopEntity(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public StopEntity duplicate() {
		return new StopEntity(this);
	}

}