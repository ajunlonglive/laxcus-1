/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import java.io.*;

import com.laxcus.util.classable.*;

/**
 * 云目录
 * 
 * @author scott.liang
 * @version 1.0 10/23/2021
 * @since laxcus 1.0
 */
public class SDirectory extends SElement {
	
	private static final long serialVersionUID = -8763070033445324410L;

	/**
	 * 构造默认的云目录
	 */
	public SDirectory() {
		super();
	}

	/**
	 * 设置云目录
	 * @param root
	 */
	public SDirectory(File root) {
		this();
		setRoot(root);
	}

	/**
	 * 构造云目录
	 * @param path
	 */
	public SDirectory(String path) {
		this();
		setPath(path);
	}

	/**
	 * 设置云目录
	 * @param root
	 */
	public SDirectory(File root, String path) {
		this();
		this.setRoot(root);
		this.setPath(path);
	}
	
	/**
	 * 从可类化读取器中解析云目录
	 * @param reader 可类化读取器
	 */
	public SDirectory(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造云目录副本
	 * @param that 云目录
	 */
	protected SDirectory(SDirectory that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.cloud.store.SElement#duplicate()
	 */
	@Override
	public SDirectory duplicate() {
		return new SDirectory(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.cloud.store.SElement#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.cloud.store.SElement#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		
	}

}