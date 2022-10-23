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
 * 上传文件向导
 * 
 * @author scott.liang
 * @version 1.0 10/29/2021
 * @since laxcus 1.0
 */
public class UploadCloudFileGuide extends Command {

	private static final long serialVersionUID = -6894127331194050368L;

	/** 如果存在，覆盖或者否 **/
	private boolean override;
	
	/** 文件长度 **/
	private long length;
	
	/** 资源路径 **/
	private SRL srl;

	/**
	 * 构造上传文件向导
	 */
	public UploadCloudFileGuide() {
		super();
	}

	/**
	 * 构造上传文件向导副本
	 * @param that 上传文件向导
	 */
	private UploadCloudFileGuide(UploadCloudFileGuide that) {
		super(that);
		override = that.override;
		length = that.length;
		srl = that.srl;
	}

	/**
	 * 从可类化读取器中解析
	 * @param reader 可类化读取器
	 */
	public UploadCloudFileGuide(ClassReader reader) {
		super();
		resolve(reader);
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
	 * 设置文件长度
	 * @param l
	 */
	public void setLength(long l) {
		length = l;
	}

	/**
	 * 返回文件长度
	 * @return
	 */
	public long getLength() {
		return length;
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
	public UploadCloudFileGuide duplicate() {
		return new UploadCloudFileGuide(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(override);
		writer.writeLong(length);
		writer.writeObject(srl);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		override = reader.readBoolean();
		length = reader.readLong();
		srl = new SRL(reader);
	}

}