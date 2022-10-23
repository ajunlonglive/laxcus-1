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
 * 获取CALL站点成员命令。<br><br>
 * 
 * 命令由GATE站点发出，分发处理流程：GATE -> BANK -> TOP -> HOME(MULTI) -> TOP -> BANK -> GATE。
 * 
 * @author scott.liang
 * @version 1.2 7/1/2018
 * @since laxcus 1.0
 */
public final class TakeCallItem extends Command {

	private static final long serialVersionUID = 5907398147540308530L;
	
	/** 用户签名 **/
	private Siger username;

	/**
	 * 根据传入的获取CALL站点成员命令命令，生成它的数据副本
	 * @param that TakeCallItem实例
	 */
	private TakeCallItem(TakeCallItem that) {
		super(that);
		setUsername(that.username);
	}

	/**
	 * 构造默认和私有的获取CALL站点成员命令命令。
	 */
	private TakeCallItem() {
		super();
	}

	/**
	 * 构造获取CALL站点成员命令命令，指定用户签名。
	 * @param siger 用户签名
	 */
	public TakeCallItem(Siger siger) {
		this();
		setUsername(siger);
	}

	/**
	 * 从可类化数据读取器中解析获取CALL站点成员命令命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeCallItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户签名，不允许空指针。
	 * @param e 用户签名实例
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
	public TakeCallItem duplicate() {
		return new TakeCallItem(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(username);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		username = reader.readInstance(Siger.class);
	}

}