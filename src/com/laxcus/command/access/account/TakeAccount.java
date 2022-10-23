/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.account;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获取账号配置命令。<br>
 * 
 * 命令从GATE站点发出，目标是ACCOUNT站点。
 * 
 * 这个命令由FRONT/Gate站点发出，对应Gate/ACCOUNT站点，返回Account实例。
 * 
 * @author scott.liang
 * @version 1.1 4/9/2015
 * @since laxcus 1.0
 */
public final class TakeAccount extends Command {

	private static final long serialVersionUID = -2618755795791023669L;

	/** 用户签名 **/
	private Siger siger;
	
	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that TakeAccount实例
	 */
	private TakeAccount(TakeAccount that) {
		super(that);
		siger = that.siger;
	}

	/**
	 * 构造默认的获取账号配置命令
	 */
	public TakeAccount() {
		super();
	}
	
	/**
	 * 构造默认的获取账号配置，指定用户签名
	 * @param siger 申请账号用户签名
	 */
	public TakeAccount(Siger siger) {
		this();
		setSiger(siger);
	}
	
	/**
	 * 从可类化数据读取器中解析获取账号配置命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeAccount(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置用户签名，不允许空指针
	 * @param e 用户签名实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);
		siger = e;
	}
	
	/**
	 * 返回用户签名
	 * @return 用户签名实例
	 */
	public Siger getSiger() {
		return siger;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeAccount duplicate() {
		return new TakeAccount(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(siger);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		siger = new Siger(reader);
	}

}