/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.cyber;

import com.laxcus.util.classable.*;

/**
 * FRONT在线用户虚拟空间。
 * 节点可以承载的在线FRONT用户数目，部署在GATE/CALL节点。
 * 
 * @author scott.liang
 * @version 1.0 10/26/2019
 * @since laxcus 1.0
 */
public final class FrontCyber extends SiteCyber {

	private static final long serialVersionUID = -4932664390001867529L;

	/**
	 * 构造默认的成员虚拟空间
	 */
	public FrontCyber() {
		super();
	}
	
	/**
	 * 生成成员虚拟空间副本
	 * @param that 成员虚拟空间
	 */
	private FrontCyber(FrontCyber that) {
		super(that);
	}
	
	/**
	 * 从可类化读取器解析成员虚拟空间
	 * @param reader 可类化数据读取器
	 */
	public FrontCyber(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造成员虚拟空间，指定参数
	 * @param maxPersons 最大用户数
	 * @param threshold 阀值
	 */
	public FrontCyber(int maxPersons, double threshold) {
		this();
		setPersons(maxPersons);
		setThreshold(threshold);
	}

	/**
	 * 生成副本
	 * @return FrontCyber副本
	 */
	public FrontCyber duplicate() {
		return new FrontCyber(this);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.util.cyber.VirtualCyber#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {

	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.cyber.VirtualCyber#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {

	}

}