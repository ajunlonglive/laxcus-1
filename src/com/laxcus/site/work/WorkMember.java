/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.work;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * WORK站点成员<br><br>
 * 
 * @author scott.liang
 * @version 1.1 6/12/2015
 * @since laxcus 1.0
 */
public final class WorkMember extends TableMember { 

	private static final long serialVersionUID = -5008655295170638393L;

	/**
	 * 根据传入WORK站点成员参数，生成它的数据副本
	 * @param that WorkMember实例
	 */
	private WorkMember(WorkMember that) {
		super(that);
	}

	/**
	 * 构造默认的WORK站点成员
	 */
	private WorkMember() {
		super();
	}

	/**
	 * 构造WORK站点成员，指定持有人
	 * @param siger 持有人
	 */
	public WorkMember(Siger siger) {
		this();
		setSiger(siger);
	}

	/**
	 * 从可类化数据读取器中解析WORK站点成员参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public WorkMember(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.SiteMember#duplicate()
	 */
	@Override
	public WorkMember duplicate() {
		return new WorkMember(this);
	}

}