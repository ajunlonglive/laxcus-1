/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.rabbet;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * HOME辅助连接器。<br><br>
 * 
 * 在CALL站点上使用。
 * 
 * @author scott.liang
 * @version 1.1 05/03/2015
 * @since laxcus 1.0
 */
public final class HomeRabbet extends Rabbet {

	private static final long serialVersionUID = 5330103986884082298L;

	/**
	 * 根据传入的HOME辅助连接器，生成它的数据副本
	 * @param that HomeRabbet实例
	 */
	private HomeRabbet(HomeRabbet that) {
		super(that);
	}

	/**
	 * 构造默认的HOME辅助连接器
	 */
	public HomeRabbet() {
		super(SiteTag.HOME_SITE);
	}
	
	/**
	 * 构造HOME辅助连接器，指定服务器地址
	 * @param hub 服务器地址
	 */
	public HomeRabbet(Node hub) {
		this();
		setHub(hub);
	}

	/**
	 * 从可类化读取器中解析HOME辅助连接器
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public HomeRabbet(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.Rabbet#duplicate()
	 */
	@Override
	public HomeRabbet duplicate() {
		return new HomeRabbet(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Rabbet#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Rabbet#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// TODO Auto-generated method stub
		
	}
}