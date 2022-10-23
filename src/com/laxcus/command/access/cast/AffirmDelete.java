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

/**
 * 确认删除操作。
 * 
 * @author scott.liang
 * @version 1.0 5/2/2013
 * @since laxcus 1.0
 */
public class AffirmDelete extends Command {

	private static final long serialVersionUID = 2025377821931349755L;

	/**
	 * 构造默认的确认删除操作
	 */
	public AffirmDelete() {
		super();
	}

	/**
	 * 根据传入的确认删除操作实例，生成它的数据副本
	 * @param that AffirmDelete实例
	 */
	private AffirmDelete(AffirmDelete that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AffirmDelete duplicate() {
		return new AffirmDelete(this);
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