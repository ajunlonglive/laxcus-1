/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * 获得表数据块编号命令。<br><br>
 * 
 * 命令格式：GET ALL STUBS 数据库.表 <br><br>
 * 
 * 此操作由FRONT节点发出，通过CALL站点，分发到DATA主站点。<br>
 * FRONT -> CALL -> MASTER DATA (ALL SITES)<br>
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class GitStubs extends ProcessTable {

	private static final long serialVersionUID = 2859749796033965718L;

	/**
	 * 构造默认和私有获得表数据块编号命令
	 */
	private GitStubs() {
		super();
	}

	/**
	 * 从传入的获得表数据块编号命令，生成它的数据副本
	 * @param that GitStubs实例
	 */
	private GitStubs(GitStubs that) {
		super(that);
	}

	/**
	 * 构造获得表数据块编号命令，指定数据表名
	 * @param space 数据表名
	 */
	public GitStubs(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析获得表数据块编号命令
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public GitStubs(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public GitStubs duplicate() {
		return new GitStubs(this);
	}

}