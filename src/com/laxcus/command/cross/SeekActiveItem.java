/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查询授权单元 <br>
 * 这个命令是从DATA站点发出，目标是HOME站点，HOME站点按照要求返回关联的授权单元。
 * 
 * @author scott.liang
 * @version 1.0 8/15/2017
 * @since laxcus 1.0
 */
public final class SeekActiveItem extends Command {

	private static final long serialVersionUID = 9219082818216946951L;
	
	/** 授权人签名 **/
	private Siger authorizer;

	/**
	 * 构造默认的查询授权单元
	 */
	private SeekActiveItem() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析查询授权单元
	 * @param reader 可类化数据读取器
	 */
	public SeekActiveItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造查询授权单元，指定授权人签名
	 * @param authorizer 授权人签名
	 */
	public SeekActiveItem(Siger authorizer) {
		this();
		setAuthorizer(authorizer);
	}

	/**
	 * 生成查询授权单元的数据副本
	 * @param that SeekActiveItem实例
	 */
	private SeekActiveItem(SeekActiveItem that) {
		super(that);
		authorizer = that.authorizer;
	}

	/**
	 * 设置授权人签名
	 * @param e Siger实例
	 */
	public void setAuthorizer(Siger e) {
		Laxkit.nullabled(e);

		authorizer = e;
	}

	/**
	 * 返回授权人签名
	 * @return Siger实例
	 */
	public Siger getAuthorizer() {
		return authorizer;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SeekActiveItem duplicate() {
		return new SeekActiveItem(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(authorizer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		authorizer = new Siger(reader);
	}

}