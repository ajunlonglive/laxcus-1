/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cyber;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 上传文件的处理结果
 * 
 * @author scott.liang
 * @version 1.0 10/29/2021
 * @since laxcus 1.0
 */
public class CheckMassiveMimoProduct extends ConfirmProduct {

	private static final long serialVersionUID = -2036016239270832678L;

	/** 网关接收器数目 **/
	private int suckers;

	/**
	 * 构造上传文件的处理结果
	 */
	public CheckMassiveMimoProduct() {
		super();
		suckers = 0;
	}
	
	/**
	 * 上传文件的处理结果
	 * @param suckers 网关接收器数目
	 */
	public CheckMassiveMimoProduct(int suckers) {
		this();
		setMISuckers(suckers);
	}
	
	/**
	 * 生成上传文件的处理结果副本
	 * @param that
	 */
	private CheckMassiveMimoProduct(CheckMassiveMimoProduct that) {
		this();
		suckers = that.suckers;
	}
	
	/**
	 * 从可类化读取器中解析上传文件的处理结果
	 * @param that 可类化读取器
	 */
	public CheckMassiveMimoProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置网关接收器数目。<br>
	 * 接收器是网关MISucker数量
	 * 
	 * @param i
	 */
	public void setMISuckers(int i) {
		suckers = i;
	}

	/**
	 * 返回网关接收器数目
	 * @return
	 */
	public int getMISuckers() {
		return suckers;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CheckMassiveMimoProduct duplicate() {
		return new CheckMassiveMimoProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(suckers);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		suckers = reader.readInt();
	}

}