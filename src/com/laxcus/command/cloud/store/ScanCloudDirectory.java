/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 扫描云端目录。
 * 位置是CALL节点，某个账号下面的全部记录。
 * 
 * @author scott.liang
 * @version 1.0 10/30/2021
 * @since laxcus 1.0
 */
public class ScanCloudDirectory extends Command {
	
	private static final long serialVersionUID = 5549255109102279274L;
	
	/** 资源路径 **/
	private SRL srl;
	
	/** 全路径 **/
	private boolean fullPath;

	/**
	 * 构造默认的扫描云端目录命令
	 */
	public ScanCloudDirectory() {
		super();
		fullPath = true;
	}
	
	/**
	 * 构造默认的扫描云端目录命令
	 */
	public ScanCloudDirectory(SRL srl) {
		this();
		setSRL(srl);
	}
	
	/**
	 * 从可类化读取器中解析扫描云端目录命令
	 * @param reader 可类化读取器
	 */
	public ScanCloudDirectory(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成副本
	 * @param that
	 */
	private ScanCloudDirectory(ScanCloudDirectory that) {
		super(that);
		fullPath = that.fullPath;
		srl = that.srl;
	}
	
	/**
	 * 设置为全路径
	 * @param b
	 */
	public void setFullPath(boolean b) {
		fullPath = b;
	}

	/**
	 * 判断是全路径
	 * @return
	 */
	public boolean isFullPath() {
		return fullPath;
	}
	
	/**
	 * 设置存储资源定位器
	 * @param l
	 */
	public void setSRL(SRL l) {
		srl = l;
	}
	
	/**
	 * 返回存储资源定位器
	 * @return
	 */
	public SRL getSRL(){
		return srl;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ScanCloudDirectory duplicate() {
		return new ScanCloudDirectory(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(fullPath);
		writer.writeObject(srl);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		fullPath = reader.readBoolean();
		srl = new SRL(reader);
	}

}