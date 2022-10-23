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
 * 下载文件向导
 * 
 * @author scott.liang
 * @version 1.0 11/07/2021
 * @since laxcus 1.0
 */
public class DownloadCloudFileGuide extends Command {

	private static final long serialVersionUID = -5314472035377407305L;

	/** 资源路径 **/
	private SRL srl;

	/**
	 * 构造上传文件向导
	 */
	public DownloadCloudFileGuide() {
		super();
	}

	/**
	 * 生成上传文件向导
	 * @param reader 可类化读取器
	 */
	public DownloadCloudFileGuide(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造上传文件向导副本
	 * @param that 上传文件向导
	 */
	private DownloadCloudFileGuide(DownloadCloudFileGuide that) {
		super(that);
		srl = that.srl;
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
	public DownloadCloudFileGuide duplicate() {
		return new DownloadCloudFileGuide(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(srl);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		srl = new SRL(reader);
	}

}