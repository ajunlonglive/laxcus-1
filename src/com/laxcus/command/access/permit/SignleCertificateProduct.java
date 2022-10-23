/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.permit;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 单个权限处理报告
 * 
 * @author scott.liang
 * @version 1.1 05/09/2015
 * @since laxcus 1.0
 */
public final class SignleCertificateProduct extends ConfirmProduct {

	private static final long serialVersionUID = 431944997152175900L;

	/** 账号位置 */
	private Seat seat;

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that 单个权限处理报告实例
	 */
	private SignleCertificateProduct(SignleCertificateProduct that) {
		super(that);
		seat = that.seat;
	}

	/**
	 * 构造默认的单个权限处理报告
	 */
	private SignleCertificateProduct() {
		super();
	}
	
	/**
	 * 构造单个权限处理报告，指定参数
	 * @param siger 账号位置
	 * @param successful 成功或者否
	 */
	public SignleCertificateProduct(Seat siger, boolean successful) {
		this();
		setSeat(siger);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析单个权限处理报告
	 * @param reader 可类化数据读取器
	 */
	public SignleCertificateProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置账号位置
	 * @param e Siger实例
	 */
	public void setSeat(Seat e) {
		Laxkit.nullabled(e);

		seat = e;
	}

	/**
	 * 返回账号位置
	 * @return Seat实例
	 */
	public Seat getSeat() {
		return seat;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SignleCertificateProduct duplicate() {
		return new SignleCertificateProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 签名
		writer.writeObject(seat);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		seat = new Seat(reader);
	}

}