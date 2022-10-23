/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 诊断节点运行时状态。<br><br>
 * 
 * 这个命令由WATCH站点发给被监视的所有节点，这些节点返回SiteRunetime命令
 * 
 * @author scott.liang
 * @version 1.0 4/13/2018
 * @since laxcus 1.0
 */
public final class SeekSiteRuntime extends Command {

	private static final long serialVersionUID = -3470015818874696924L;

	/**
	 * 构造默认的诊断节点运行时状态
	 */
	public SeekSiteRuntime() {
		super();
	}
	
	/**
	 * 诊断节点运行时状态
	 * @param fast 极速处理
	 */
	public SeekSiteRuntime(boolean fast) {
		this();
		setFast(fast);
	}

	/**
	 * 根据传入的命令实例，生成它的数据副本
	 * @param that SeekSiteRuntime实例
	 */
	private SeekSiteRuntime(SeekSiteRuntime that) {
		super(that);
	}

	/**
	 * 从可类化数据读取器中解析诊断节点运行时状态
	 * @param reader 可类化数据读取器
	 */
	public SeekSiteRuntime(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SeekSiteRuntime duplicate() {
		return new SeekSiteRuntime(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// TODO Auto-generated method stub

	}

}