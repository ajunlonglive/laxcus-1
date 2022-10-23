/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 授权建立用户账号 <br>
 * 
 * 这个命令由TOP站点投递给GATE站点。
 * 
 * @author scott.liang
 * @version 1.1 11/23/2015
 * @since laxcus 1.0
 */
public final class AwardCreateAccount extends Command {

	private static final long serialVersionUID = 8770993997442945794L;

	/** 用户账号 **/
	private Account account;

	/**
	 * 构造默认和私有的授权建立用户账号命令
	 */
	private AwardCreateAccount() {
		super();
	}

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that AwardCreateAccount实例
	 */
	private AwardCreateAccount(AwardCreateAccount that) {
		super(that);
		account = that.account;
	}

	/**
	 * 构造授权建立用户账号命令，指定参数
	 * @param account Account实例
	 */
	public AwardCreateAccount(Account account) {
		this();
		setAccount(account);
	}

	/**
	 * 构造授权建立用户账号命令，指定参数
	 * @param account Account实例
	 * @param swift 快速处理或者否
	 */
	public AwardCreateAccount(Account account, boolean swift) {
		this(account);
		setQuick(swift);
	}

	/**
	 * 从可类化数据读取器中解析授权建立用户账号
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AwardCreateAccount(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置账号，不允许空指针
	 * @param e Account实例
	 */
	public void setAccount(Account e) {
		Laxkit.nullabled(e);
		account = e;
	}

	/**
	 * 返回账号
	 * @return Account实例
	 */
	public Account getAccount() {
		return account;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AwardCreateAccount duplicate() {
		return new AwardCreateAccount(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(account);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		account = new Account(reader);
	}

}