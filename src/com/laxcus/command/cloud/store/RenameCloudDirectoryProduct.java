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
 * 更改目录的处理结果
 * 
 * @author scott.liang
 * @version 1.0 1/26/2022
 * @since laxcus 1.0
 */
public class RenameCloudDirectoryProduct extends EchoProduct {

	private static final long serialVersionUID = -4603914090666726605L;

	/** 存储目录 **/
	private SDirectory dir;
	
	/** 状态 **/
	private int state;

	/**
	 * 构造更改目录的处理结果
	 */
	public RenameCloudDirectoryProduct() {
		super();
		state = StoreState.NONE;
	}
	
	/**
	 * 生成更改目录的处理结果副本
	 * @param that
	 */
	private RenameCloudDirectoryProduct(RenameCloudDirectoryProduct that) {
		this();
		dir = that.dir;
		state = that.state;
	}
	
	/**
	 * 从可类化读取器中解析更改目录的处理结果
	 * @param that 可类化读取器
	 */
	public RenameCloudDirectoryProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置目录
	 * @param e
	 */
	public void setDirectory(SDirectory e) {
		dir = e;
	}

	/**
	 * 返回目录
	 * @return
	 */
	public SDirectory getDirectory() {
		return dir;
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
	public RenameCloudDirectoryProduct duplicate() {
		return new RenameCloudDirectoryProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(dir);
		writer.writeInt(state);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		dir = reader.readInstance(SDirectory.class);
		state = reader.readInt();
	}

}