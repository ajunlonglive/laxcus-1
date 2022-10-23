/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.traffic;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * SWARM反射命令。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 8/10/2018
 * @since laxcus 1.0
 */
public final class SwarmReflex extends Command {

	private static final long serialVersionUID = 8244168768341076150L;

	/**
	 * 构造默认的SWARM反射命令
	 */
	public SwarmReflex() {
		super();
	}

	/**
	 * 根据传入的SWARM反射命令，生成它的数据副本
	 * @param that SwarmReflex实例
	 */
	private SwarmReflex(SwarmReflex that) {
		super(that);
	}
	
	/**
	 * 从可类化数据读取器中解析SWARM反射命令。
	 * @param reader 可类化数据读取器
	 */
	public SwarmReflex(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SwarmReflex duplicate() {
		return new SwarmReflex(this);
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