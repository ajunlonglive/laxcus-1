/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.entrance;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 根据账号签名定位GATE站点模式 <br><br>
 * 
 * @author scott.liang
 * @version 1.0 7/18/2019
 * @since laxcus 1.0
 */
public class ShadowMode extends Command {

	private static final long serialVersionUID = -3346394240730093965L;

	/** HASH定位或者否 **/
	private boolean hash;

	/**
	 * 构造根据账号签名定位GATE站点模式
	 */
	public ShadowMode() {
		super();
	}

	/**
	 * 构造根据账号签名定位GATE站点模式
	 * @param hash 用户签名
	 */
	public ShadowMode(boolean hash) {
		super();
		setHash(hash);
	}
	
	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 */
	public ShadowMode(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 根据传入的根据账号签名定位GATE站点模式实例，生成它的数据副本
	 * @param that ShadowMode实例
	 */
	private ShadowMode(ShadowMode that) {
		super(that);
		hash = that.hash;
	}

	/**
	 * 设置HASH定位或者否
	 * @param e 真或者假
	 */
	public void setHash(boolean e) {
		hash = e;
	}

	/**
	 * 返回HASH定位或者否
	 * @return 真或者假
	 */
	public boolean isHash() {
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(hash);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		hash = reader.readBoolean();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShadowMode duplicate() {
		return new ShadowMode(this);
	}

}