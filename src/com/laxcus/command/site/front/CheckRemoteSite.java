/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 显示连接的目标节点 <br><br>
 * 
 * 这个命令只在FRONT节点使用，显示它连接的GATE/CALL节点。
 * 
 * @author scott.liang
 * @version 1.0 6/5/2020
 * @since laxcus 1.0
 */
public class CheckRemoteSite extends Command {
	
	private static final long serialVersionUID = 8679196984154043206L;

	/**
	 * 构造默认的显示连接的目标节点
	 */
	public CheckRemoteSite() {
		super();
	}

	/**
	 * 生成显示连接的目标节点的数据副本
	 * @param that 显示连接的目标节点
	 */
	private CheckRemoteSite(CheckRemoteSite that) {
		super(that);
	}

	/**
	 * 从可类化数据读取器中解析显示连接的目标节点
	 * @param reader 可类化数据读取器
	 */
	public CheckRemoteSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CheckRemoteSite duplicate() {
		return new CheckRemoteSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		
	}

}