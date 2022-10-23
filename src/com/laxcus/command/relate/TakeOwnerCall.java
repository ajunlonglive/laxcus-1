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
 * 获得账号所有人的CALL站点。<br>
 * 
 * 命令由FRONT站点发给GATE站点。GATE站点在本地检索匹配的参数，返回给FRONT站点。
 * 
 * @author scott.liang
 * @version 1.1 9/30/2015
 * @since laxcus 1.1
 */
public class TakeOwnerCall extends Command {

	private static final long serialVersionUID = 27318421672915670L;

	/** 用户签名 **/
	private Siger siger;

	/**
	 * 根据传入的获得账号所有人的CALL站点，生成它的浅层数据副本
	 * @param that 获得账号所有人的CALL站点实例
	 */
	private TakeOwnerCall(TakeOwnerCall that) {
		super(that);
		siger = that.siger;
	}

	/**
	 * 构造默认的获得账号所有人的CALL站点命令
	 */
	private TakeOwnerCall() {
		super();
	}

	/**
	 * 构造获得账号所有人的CALL站点，指定用户签名
	 * @param e Siger实例
	 */
	public TakeOwnerCall(Siger e) {
		this();
		setSiger(e);
	}

	/**
	 * 从可类化读取器中解析SCALING命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeOwnerCall(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户签名
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);

		siger = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeOwnerCall duplicate() {
		return new TakeOwnerCall(this);
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