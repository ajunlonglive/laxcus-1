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
 * CALL辅助连接器 <br><br>
 * 
 * 在FRONT站点上使用。
 * 
 * @author scott.liang
 * @version 1.1 05/03/2015
 * @since laxcus 1.0
 */
public final class CallRabbet extends Rabbet {

	private static final long serialVersionUID = 2529896560825117059L;

	/**
	 * 根据传入的CALL辅助连接器，生成它的数据副本
	 * @param that CallRabbet实例
	 */
	private CallRabbet(CallRabbet that) {
		super(that);
	}

	/**
	 * 构造默认的CALL辅助连接器
	 */
	public CallRabbet() {
		super(SiteTag.CALL_SITE);
	}
	
	/**
	 * 构造CALL辅助连接器，指定服务器地址
	 * @param hub 服务器地址
	 */
	public CallRabbet(Node hub) {
		this();
		setHub(hub);
	}

	/**
	 * 从可类化读取器中解析CALL辅助连接器
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CallRabbet(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.Rabbet#duplicate()
	 */
	@Override
	public CallRabbet duplicate() {
		return new CallRabbet(this);
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