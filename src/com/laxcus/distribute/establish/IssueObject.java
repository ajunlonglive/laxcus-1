/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish;

import com.laxcus.distribute.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 数据构建的“ISSUE”阶段对象。<br>
 * 
 * 本阶段主要的工作是判断SCAN阶段中的数据表名，和SIFT阶段中的列空间有效和合法。
 * 
 * @author scott.liang
 * @version 1.3 4/2/2015
 * @since laxcus 1.0
 */
public final class IssueObject extends AccessObject {

	private static final long serialVersionUID = 6202552258220311448L;

	/**
	 * 根据传入的“ISSUE”阶段对象实例，生成它的数据副本
	 * @param that IssueObject实例
	 */
	private IssueObject(IssueObject that) {
		super(that);
	}

	/**
	 * 构造一个默认的“ISSUE”阶段对象
	 */
	public IssueObject() {
		super();
	}

	/**
	 * 构造一个“ISSUE”阶段对象，和指定它的阶段命名。
	 * @param phase 阶段命名
	 */
	public IssueObject(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 使用可类化读取器解析“ISSUE”对象参数
	 * @param reader 可类化读取器
	 */
	public IssueObject(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据当前实例，生成“ISSUE”阶段对象的数据副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public IssueObject duplicate() {
		return new IssueObject(this);
	}

}