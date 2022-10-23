/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.cast;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.Node;

/**
 * 投递命令。<br>
 * 
 * 投递命令的是把一个命令由一个站点发向另一个站点。在指定命令之外，还包含其它必须的参数。
 * 
 * @author scott.liang
 * @version 1.1 7/17/2015
 * @since laxcus 1.0
 */
public abstract class CastMethod extends Command {

	private static final long serialVersionUID = -2253195313065529678L;

	/** 服务器地址 **/
	private Node hub;

	/**
	 * 构造投递命令
	 */
	protected CastMethod() {
		super();
	}

	/**
	 * 根据传入的投递命令，生成它的数据副本
	 * @param that CastMethod实例
	 */
	protected CastMethod(CastMethod that) {
		super(that);
		hub = that.hub;
	}

	/**
	 * 设置服务器地址
	 * @param e Node实例
	 */
	public void setHub(Node e) {
		hub = e;
	}

	/**
	 * 返回服务器地址
	 * @return Node实例
	 */
	public Node getHub() {
		return hub;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(hub);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		hub = reader.readInstance(Node.class);
	}

}