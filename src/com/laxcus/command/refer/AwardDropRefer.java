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
 * 删除用户资源引用 <br>
 * 
 * 当HOME站点删除账户时，分发这个命令给下属的CALL/DATA/WORK/BUILD站点。
 * 这个命令与“AwardCreateRefer”相对。
 * 
 * @author scott.liang
 * @version 1.1 11/23/2015
 * @since laxcus 1.0
 */
public final class AwardDropRefer extends Command {

	private static final long serialVersionUID = 7032804416188874514L;

	/** 账号签名 **/
	private Siger username;

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that AwardDropRefer实例
	 */
	private AwardDropRefer(AwardDropRefer that) {
		super(that);
		username = that.username;
	}

	/**
	 * 构造默认和私有的删除用户资源引用命令。
	 */
	private AwardDropRefer() {
		super();
	}

	/**
	 * 构造删除用户资源引用命令，指定全部参数
	 * @param siger 账号签名
	 * @param direct 数据直达，不需要反馈
	 */
	public AwardDropRefer(Siger siger, boolean direct) {
		this();
		setUsername(siger);
		setDirect(direct);
	}

	/**
	 * 构造删除用户资源引用命令，指定账号签名。
	 * @param siger 账号签名
	 */
	public AwardDropRefer(Siger siger) {
		this(siger, false);
	}
	
	/**
	 * 从可类化数据读取器中解析删除用户资源引用命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AwardDropRefer(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置账号签名，不允许空指针。
	 * @param e 账号签名实例
	 * @throws NullPointerException
	 */
	public void setUsername(Siger e) {
		Laxkit.nullabled(e);
		// 赋值
		username = e;
	}

	/**
	 * 返回账号签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return username;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AwardDropRefer duplicate() {
		return new AwardDropRefer(this);
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