/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.attend;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查找签到器命令 <br>
 * 
 * 通过这个命令，将判断所查找的调用器是否“活着”。
 * 
 * @author scott.liang
 * @version 1.0 3/18/2017
 * @since laxcus 1.0
 */
public final class SeekAttender extends Command {

	private static final long serialVersionUID = -9024064934586649615L;

	/** 调用器监听地址 **/
	private Cabin cabin;

	/**
	 * 构造默认和私有的查找签到器命令
	 */
	private SeekAttender() {
		super();
	}

	/**
	 * 生成查找签到器命令的数据副本
	 * @param that SeekAttender实例
	 */
	private SeekAttender(SeekAttender that) {
		super(that);
		cabin = that.cabin;
	}

	/**
	 * 构造查找签到器命令，指定调用器地址
	 * @param cabin 调用器地址
	 */
	public SeekAttender(Cabin cabin) {
		this();
		setCabin(cabin);
	}

	/**
	 * 从可类化数据读取器中解析查找签到器命令
	 * @param reader 可类化数据读取器
	 */
	public SeekAttender(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置目标调用器监听地址
	 * @param e Cabin实例
	 */
	public void setCabin(Cabin e) {
		Laxkit.nullabled(e);

		cabin = e;
	}

	/**
	 * 返回目标调用器监听地址
	 * @return Cabin实例
	 */
	public Cabin getCabin() {
		return cabin;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SeekAttender duplicate() {
		return new SeekAttender(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(cabin);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		cabin = new Cabin(reader);
	}

}