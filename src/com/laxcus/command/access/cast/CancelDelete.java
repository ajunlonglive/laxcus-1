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
 * 取消删除操作
 * 
 * @author scott.liang
 * @version 1.0 5/2/2013
 * @since laxcus 1.0
 */
public class CancelDelete extends Command {

	private static final long serialVersionUID = 2633961861686191422L;

	/**
	 * 构造默认的取消删除操作
	 */
	public CancelDelete() {
		super();
	}

	/**
	 * 根据传入的取消删除操作，生成它的数据副本
	 * @param that CancelDelete实例
	 */
	private CancelDelete(CancelDelete that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CancelDelete duplicate() {
		return new CancelDelete(this);
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