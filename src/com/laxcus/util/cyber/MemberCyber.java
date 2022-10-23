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
 * 成员虚拟空间。
 * 一个节点的承载量，以人数为基点。
 * 
 * 节点可以支持的最多成员数，即这个成员的资源，支持在线调整。部署在GATE/CALL/DATA/WORK/BUILD节点。
 * 
 * @author scott.liang
 * @version 1.0 10/20/2019
 * @since laxcus 1.0
 */
public final class MemberCyber extends SiteCyber {

	private static final long serialVersionUID = 855155199950328909L;

	/**
	 * 构造默认的成员虚拟空间
	 */
	public MemberCyber() {
		super();
	}
	
	/**
	 * 生成成员虚拟空间副本
	 * @param that 成员虚拟空间
	 */
	private MemberCyber(MemberCyber that) {
		super(that);
	}
	
	/**
	 * 从可类化读取器解析成员虚拟空间
	 * @param reader 可类化数据读取器
	 */
	public MemberCyber(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造成员虚拟空间，指定参数
	 * @param maxPersons 最大用户数
	 * @param threshold 阀值
	 */
	public MemberCyber(int maxPersons, double threshold) {
		this();
		setPersons(maxPersons);
		setThreshold(threshold);
	}

	/**
	 * 生成副本
	 * @return MemberCyber副本
	 */
	public MemberCyber duplicate() {
		return new MemberCyber(this);
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