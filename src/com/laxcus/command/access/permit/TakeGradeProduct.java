/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.permit;



import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 注册账号操作权及反馈结果。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeGradeProduct extends EchoProduct {

	private static final long serialVersionUID = 6738948156054497065L;

	/** 账号操作权级 **/
	private int grade;

	/**
	 * 构造默认的申请主机序列号命令
	 */
	private TakeGradeProduct() {
		super();
		setGrade(-1);
	}

	/**
	 * 构造申请主机序列号，指定账号操作权级
	 * @param grade 账号操作权级
	 */
	public TakeGradeProduct(int grade) {
		this();
		setGrade(grade);
	}

	/**
	 * 生成申请主机序列号的数据副本
	 * @param that 申请主机序列号
	 */
	private TakeGradeProduct(TakeGradeProduct that) {
		super(that);
		grade = that.grade;
	}

	/**
	 * 从可类化数据读取器中解析申请主机序列号
	 * @param reader 可类化数据读取器
	 */
	public TakeGradeProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置账号操作权级
	 * @param who 账号操作权级
	 */
	public void setGrade(int who) {
		grade = who;
	}

	/**
	 * 返回账号操作权级
	 * @return 账号操作权级
	 */
	public int getGrade() {
		return grade;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeGradeProduct duplicate() {
		return new TakeGradeProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(grade);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		grade = reader.readInt();
	}

}