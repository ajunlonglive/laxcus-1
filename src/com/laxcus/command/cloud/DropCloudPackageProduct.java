/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 删除云端应用软件包反馈结果
 * 
 * @author scott.liang
 * @version 1.0 6/20/2020
 * @since laxcus 1.0
 */
public class DropCloudPackageProduct extends MultiProcessProduct {

	private static final long serialVersionUID = -6147831211009706575L;

	//	/** 删除的单元数目 **/
	//	private int elements;

	/**
	 * 构造投递云端应用软件包反馈结果
	 */
	public DropCloudPackageProduct() {
		super();
		//		elements = 0;
	}

	/**
	 * 生成投递云端应用软件包反馈结果数据副本
	 * @param that 投递云端应用软件包反馈结果
	 */
	private DropCloudPackageProduct(DropCloudPackageProduct that) {
		super(that);
		//		elements = that.elements;
	}

	/**
	 * 构造投递云端应用软件包反馈结果
	 * @param successful 成功或者否
	 */
	public DropCloudPackageProduct(int rights, int faluts) {
		this();
		this.addRights(rights);
		this.addRights(faluts);
	}
	//
	//	/**
	//	 * 构造投递云端应用软件包反馈结果
	//	 * @param successful 成功或者否
	//	 * @param elements 成员数目
	//	 */
	//	public DropCloudPackageProduct(boolean successful, int elements) {
	//		this(successful);
	//		setElements(elements);
	//	}

	/**
	 * 从可类化读取器中解析投递云端应用软件包反馈结果
	 * @param reader 可类化数据读取器
	 */
	public DropCloudPackageProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	//	/**
	//	 * 设置成员数目
	//	 * @param i 成员数目
	//	 */
	//	public void setElements(int i) {
	//		elements = i;
	//	}
	//
	//	/**
	//	 * 返回成员数目
	//	 * @return 成员数目
	//	 */
	//	public int getElements() {
	//		return elements;
	//	}

	//	/**
	//	 * 增加成员数目
	//	 * @param i 成员数目
	//	 */
	//	public void addElements(int i) {
	//		elements += i;
	//	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.MultiProcessProduct#duplicate()
	 */
	@Override
	public DropCloudPackageProduct duplicate() {
		return new DropCloudPackageProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		//		writer.writeInt(elements);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		//		elements = reader.readInt();
	}

}