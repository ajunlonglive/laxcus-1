/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.attend;

import com.laxcus.echo.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查找签到器处理结果
 * 
 * @author scott.liang
 * @version 1.0 3/18/2017
 * @since laxcus 1.0
 */
public final class SeekAttenderProduct extends ConfirmProduct implements Comparable<SeekAttenderProduct>{
	
	private static final long serialVersionUID = 8685082632428921606L;
	
	/** 调用器监听地址 **/
	private Cabin cabin;

	/**
	 * 构造默认和私有的调用器相互证明结果
	 */
	private SeekAttenderProduct() {
		super();
	}

	/**
	 * 生成一个调用器相互证明结果数据副本
	 * @param that SeekAttenderProduct实例
	 */
	private SeekAttenderProduct(SeekAttenderProduct that) {
		super(that);
		cabin = that.cabin;
	}

	/**
	 * 建立调用器相互证明结果，指定调用器监听地址
	 * @param cabin 调用器监听地址
	 * @param successful 成功标识
	 */
	public SeekAttenderProduct(Cabin cabin, boolean successful) {
		this();
		setCabin(cabin);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析调用器相互证明结果
	 * @param reader 可类化数据读取器
	 */
	public SeekAttenderProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置调用器监听地址
	 * @param e Cabin实例
	 */
	public void setCabin(Cabin e) {
		Laxkit.nullabled(e);

		cabin = e;
	}

	/**
	 * 返回调用器监听地址
	 * @return Cabin实例
	 */
	public Cabin getCabin() {
		return cabin;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SeekAttenderProduct duplicate() {
		return new SeekAttenderProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", cabin, (isSuccessful() ? "successful" : "failed"));
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(cabin);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		cabin = new Cabin(reader);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SeekAttenderProduct that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(cabin, that.cabin);
	}

}