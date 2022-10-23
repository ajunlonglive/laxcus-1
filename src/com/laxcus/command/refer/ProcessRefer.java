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
 * 执行用户资源引用 <br>
 * 
 * @author scott.liang
 * @version 1.0 07/18/2012
 * @since laxcus 1.0
 */
public abstract class ProcessRefer extends Command {

	private static final long serialVersionUID = 1069783817297358814L;
	
	/** 用户资源引用参数 **/
	private Refer refer;

	/**
	 * 构造默认的执行用户资源引用
	 */
	protected ProcessRefer() {
		super();
	}

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that 执行用户资源引用实例
	 */
	protected ProcessRefer(ProcessRefer that) {
		super(that);
		refer = that.refer;
	}

	/**
	 * 设置用户资源引用
	 * @param e Refer实例
	 */
	public void setRefer(Refer e) {
		Laxkit.nullabled(e);
		// 赋值
		refer = e;
	}

	/**
	 * 返回用户资源引用
	 * @return Refer实例
	 */
	public Refer getRefer() {
		return refer;
	}

	/**
	 * 返回用户账号
	 * @return User实例
	 */
	public User getUser() {
		return refer.getUser();
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return refer.getUsername();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(refer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		refer = new Refer(reader);
	}

}