/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import com.laxcus.util.classable.*;

/**
 * 云文件
 * 
 * @author scott.liang
 * @version 1.0 10/23/2021
 * @since laxcus 1.0
 */
public class SFile extends SElement {
	
	private static final long serialVersionUID = -1835757600531084300L;
	
	/** 文件长度 **/
	private long length;

	/**
	 * 构造云文件
	 */
	public SFile() {
		super();
	}
	
	/**
	 * 构造云文件，指定路径
	 * @param path 路径
	 */
	public SFile(String path) {
		this();
		setPath(path);
	}
	
	/**
	 * 从可类化读取器中解析云文件
	 * @param reader 可类化读取器
	 */
	public SFile (ClassReader reader){
		this();
		resolve(reader);
	}

	/**
	 * @param that
	 */
	public SFile(SFile that) {
		super(that);
		length = that.length;
	}
	
	/**
	 * 设置文件长度
	 * @param l
	 */
	public void setLength(long l){
		length = l;
	}
	
	/**
	 * 返回文件长度
	 * @return
	 */
	public long getLength(){
		return length;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.cloud.store.SElement#duplicate()
	 */
	@Override
	public SFile duplicate() {
		return new SFile(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.cloud.store.SElement#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(length);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.cloud.store.SElement#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		length = reader.readLong();
	}

}
