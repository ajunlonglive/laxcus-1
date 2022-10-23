/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.user;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查询FRONT用户的登录站点 <br>
 * 命令由GATE站点发出，目标是TOP站点。
 * 
 * @author scott.liang
 * @version 1.0 2/15/2017
 * @since laxcus 1.0
 */
public class FindFrontLoginSite extends Command {

	private static final long serialVersionUID = -546516142833273446L;

	/** FRONT用户签名 **/
	private Siger username;
	
	/**
	 * 生成传入实例的数据副本
	 * @param that FindFrontLoginSite实例
	 */
	private FindFrontLoginSite(FindFrontLoginSite that) {
		this();
		username = that.username;
	}

	/**
	 * 查询查询FRONT用户的登录站点
	 */
	public FindFrontLoginSite() {
		super();
	}

	/**
	 * 查询FRONT用户的登录站点，指定FRONT用户签名
	 * @param username FRONT用户签名
	 */
	public FindFrontLoginSite(Siger username) {
		this();
		setUsername(username);
	}

	/**
	 * 从可类化读取器中解析查询FRONT用户的登录站点
	 * @param reader 可类化读取器
	 */
	public FindFrontLoginSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindFrontLoginSite duplicate() {
		return new FindFrontLoginSite(this);
	}

	/**
	 * 设置FRONT用户签名
	 * @param e Siger实例
	 */
	public void setUsername(Siger e) {
		Laxkit.nullabled(e);

		username = e;
	}

	/**
	 * 返回FRONT用户签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return username;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(username);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		username = new Siger(reader);
	}

}