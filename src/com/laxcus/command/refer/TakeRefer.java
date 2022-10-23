/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获得账号的资源引用。<br>
 * 这个命令最终作用到ACCOUNT站点，拿出账号的资源引用
 * 
 * @author scott.liang
 * @version 1.0 7/27/2017
 * @since laxcus 1.0
 */
public final class TakeRefer extends Command {

	private static final long serialVersionUID = 1985854389938506360L;
	
	/** 账号签名 **/
	private Siger siger;

	/**
	 * 构造默认的获得账号的资源引用
	 */
	private TakeRefer() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析获得账号的资源引用
	 * @param reader 可类化数据读取器
	 */
	public TakeRefer(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造获得账号的资源引用，指定用户签名
	 * @param siger 用户签名
	 */
	public TakeRefer(Siger siger) {
		this();
		setSiger(siger);
	}

	/**
	 * 生成获得账号的资源引用的数据副本
	 * @param that 获得账号的资源引用实例
	 */
	private TakeRefer(TakeRefer that) {
		super(that);
		siger = that.siger;
	}

	/**
	 * 设置账号签名
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);

		siger = e;
	}

	/**
	 * 返回账号签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeRefer duplicate() {
		return new TakeRefer(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(siger);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		siger = new Siger(reader);
	}

}