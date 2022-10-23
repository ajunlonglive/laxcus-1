/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 更改文件的处理结果
 * 
 * @author scott.liang
 * @version 1.0 1/26/2022
 * @since laxcus 1.0
 */
public class RenameCloudFileProduct extends EchoProduct {

	private static final long serialVersionUID = -4603914090666726605L;

	/** 存储文件 **/
	private SFile file;
	
	/** 状态 **/
	private int state;

	/**
	 * 构造更改文件的处理结果
	 */
	public RenameCloudFileProduct() {
		super();
		state = StoreState.NONE;
	}
	
	/**
	 * 生成更改文件的处理结果副本
	 * @param that
	 */
	private RenameCloudFileProduct(RenameCloudFileProduct that) {
		this();
		file = that.file;
		state = that.state;
	}
	
	/**
	 * 从可类化读取器中解析更改文件的处理结果
	 * @param that 可类化读取器
	 */
	public RenameCloudFileProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置文件
	 * @param e
	 */
	public void setFile(SFile e) {
		file = e;
	}

	/**
	 * 返回文件
	 * @return
	 */
	public SFile getFile() {
		return file;
	}

	/**
	 * 设置处理状态
	 * @param who
	 */
	public void setState(int who) {
		state = who;
	}

	/**
	 * 返回处理状态
	 * @return
	 */
	public int getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public RenameCloudFileProduct duplicate() {
		return new RenameCloudFileProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(file);
		writer.writeInt(state);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		file = reader.readInstance(SFile.class);
		state = reader.readInt();
	}

}