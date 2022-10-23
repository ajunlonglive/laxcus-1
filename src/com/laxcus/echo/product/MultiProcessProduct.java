/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.product;

import com.laxcus.util.classable.*;

/**
 * 多处理报告。
 * 
 * @author scott.liang
 * @version 1.0 7/8/2018
 * @since laxcus 1.0
 */
public class MultiProcessProduct extends EchoProduct {

	private static final long serialVersionUID = -4229057215166276879L;
	
	/** 成功数目 **/
	private int rights;
	
	/** 错误数目 **/
	private int faults;

	/**
	 * 构造默认的多处理报告
	 */
	protected MultiProcessProduct() {
		super();
		rights = 0;
		faults = 0;
	}

	/**
	 * 建立多处理报告的数据副本
	 * @param that MultiProcessProduct实例
	 */
	protected MultiProcessProduct(MultiProcessProduct that) {
		super(that);
		rights = that.rights;
		faults =that.faults;
	}

	/**
	 * 构造多处理报告，指定它
	 * @param successful 成功或者失败
	 */
	public MultiProcessProduct(boolean successful) {
		this();
		setSuccessful(successful);
	}
	
	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 */
	public MultiProcessProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回成功数目
	 * @return
	 */
	public int getRights() {
		return rights;
	}

	/**
	 * 返回失败数目
	 * @return
	 */
	public int getFaults() {
		return faults;
	}

	/**
	 * 统计多处理报告 
	 * @param e
	 */
	public void add(MultiProcessProduct e) {
		rights += e.rights;
		faults += e.faults;
	}

	/**
	 * 增加正确统计数
	 * @param count
	 */
	public void addRights(int count) {
		rights += count;
	}

	/**
	 * 增加失败的统计数
	 * @param count
	 */
	public void addFaults(int count) {
		faults += count;
	}

	/**
	 * 设置成功标识
	 * @param b 成功标识
	 */
	public void setSuccessful(boolean b) {
		if (b) {
			rights++;
		} else {
			faults++;
		}
	}

	/**
	 * 判断有成功记录
	 * @return 返回真或者假
	 */
	public boolean hasSuccessful() {
		return rights > 0;
	}

	/**
	 * 判断完全成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return rights > 0 && faults == 0;
	}

	/**
	 * 判断是失败
	 * @return 返回真或者假
	 */
	public boolean isFailed() {
		return !isSuccessful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(rights);
		writer.writeInt(faults);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		rights = reader.readInt();
		faults = reader.readInt();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return isSuccessful() ? "successful" : "failed";
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public MultiProcessProduct duplicate() {
		return new MultiProcessProduct(this);
	}

}