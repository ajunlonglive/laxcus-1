/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查询账号所在的ACCOUNT站点。<br>
 * GATE站点发出，HASH站点接收。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2018
 * @since laxcus 1.0
 */
public class TakeAccountSite extends Command {

	private static final long serialVersionUID = -5181007192017780125L;

	/** 查询签名 **/
	private Siger siger;

	/**
	 * 构造默认的查询账号所在的ACCOUNT站点命令
	 */
	private TakeAccountSite() {
		super();
	}

	/**
	 * 构造查询账号所在的ACCOUNT站点，指定查询签名
	 * @param siger 查询签名
	 */
	public TakeAccountSite(Siger siger) {
		this();
		setSiger(siger);
	}

	/**
	 * 生成查询账号所在的ACCOUNT站点的数据副本
	 * @param that 查询账号所在的ACCOUNT站点
	 */
	private TakeAccountSite(TakeAccountSite that) {
		super(that);
		siger = that.siger;
	}

	/**
	 * 从可类化数据读取器中解析查询账号所在的ACCOUNT站点
	 * @param reader 可类化数据读取器
	 */
	public TakeAccountSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置来源查询签名
	 * @param e 查询签名
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);
		siger = e;
	}

	/**
	 * 返回来源查询签名
	 * @return 查询签名
	 */
	public Siger getSiger() {
		return siger;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeAccountSite duplicate() {
		return new TakeAccountSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(siger);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		siger = new Siger(reader);
	}

}
