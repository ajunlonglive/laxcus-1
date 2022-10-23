/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import java.io.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 上传文件。
 * 上传位置是CALL节点，做为云存储服务。
 * 
 * @author scott.liang
 * @version 1.0 10/14/2021
 * @since laxcus 1.0
 */
public class DownloadCloudFile extends Command {

	private static final long serialVersionUID = 5549255109102279274L;

	/** 资源路径 **/
	private SRL srl;

	/** 如果存在，覆盖或者否 **/
	private boolean override;

	/** 磁盘文件，覆盖 **/
	private File file;

	/**
	 * 构造默认的上传文件命令
	 */
	public DownloadCloudFile() {
		super();
		override = false;
	}
	
	/**
	 * 构造默认的上传文件命令
	 */
	public DownloadCloudFile(SRL srl) {
		this();
		setSRL(srl);
	}

	/**
	 * 从可类化读取器中解析上传文件命令
	 * @param reader 可类化读取器
	 */
	public DownloadCloudFile(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成副本
	 * @param that
	 */
	private DownloadCloudFile(DownloadCloudFile that) {
		super(that);
		override = that.override;
		srl = that.srl;
		file = that.file;
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

	/**
	 * 设置为覆盖或者否
	 * @param b
	 */
	public void setOverride(boolean b) {
		override = b;
	}

	/**
	 * 判断为覆盖或者否
	 * @return
	 */
	public boolean isOverride() {
		return override;
	}

	/**
	 * 设置文件
	 * @param e
	 */
	public void setFile(File e) {
		file = e;
	}

	/**
	 * 返回文件
	 * @return
	 */
	public File getFile() {
		return file;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DownloadCloudFile duplicate() {
		return new DownloadCloudFile(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(srl);
		writer.writeBoolean(override);
		writer.writeFile(file);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		srl = new SRL(reader);
		override = reader.readBoolean();
		file = reader.readFile();
	}

}