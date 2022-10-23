/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.build;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * BUILD站点成员<br><br>
 * 
 * @author scott.liang
 * @version 1.1 6/12/2015
 * @since laxcus 1.0
 */
public final class BuildMember extends TableMember {
	
	private static final long serialVersionUID = 2465076092465500616L;

	/**
	 * 根据传入BUILD站点成员参数，生成它的数据副本
	 * @param that BuildMember实例
	 */
	private BuildMember(BuildMember that) {
		super(that);
	}

	/**
	 * 构造默认的BUILD站点成员
	 */
	private BuildMember() {
		super();
	}

	/**
	 * 构造BUILD站点成员，指定持有人
	 * @param siger 持有人
	 */
	public BuildMember(Siger siger) {
		this();
		setSiger(siger);
	}

	/**
	 * 从可类化数据读取器中解析BUILD站点成员参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public BuildMember(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.SiteMember#duplicate()
	 */
	@Override
	public BuildMember duplicate() {
		return new BuildMember(this);
	}

}