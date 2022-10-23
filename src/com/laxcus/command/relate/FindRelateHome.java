/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.relate;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查找与账号关联的HOME站点。
 * 这个命令由CALL站点发出，通过HOME站点转发给TOP站点。检查当前集群下匹配账号的HOME站点。
 * 
 * @author scott.liang
 * @version 1.1 5/21/2015
 * @since laxcus 1.0
 */
public class FindRelateHome extends Command {
	
	private static final long serialVersionUID = 5468732069373182671L;
	
	/** 用户签名 **/
	private Siger username;
	
	/**
	 * 使用传入类，生成它的浅层数据副本
	 * @param that
	 */
	private FindRelateHome(FindRelateHome that) {
		super(that);
		username = that.username;
	}
	
	/**
	 * 构造默认查找HOME站点命令
	 */
	private FindRelateHome() {
		super();
	}
	
	/**
	 * 构造查找HOME站点命令，指定用户签名
	 * @param e 用户签名
	 */
	public FindRelateHome(Siger e) {
		this();
		setUsername(e);
	}

	/**
	 * 从可类化读取器中解析SCALING命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FindRelateHome(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户名，不允许空值
	 * @param e Siger实例
	 * @throws NullPointerException
	 */
	public void setUsername(Siger e) {
		Laxkit.nullabled(e);

		username = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return username;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindRelateHome duplicate() {
		return new FindRelateHome(this);
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