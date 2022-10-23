/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.account;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 更新私有网络空间。
 * 这是FRONT节点专属命令，命令自己的账号，还有关联的CALL记录。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2019
 * @since laxcus 1.0
 */
public class RefreshCyber extends Command {

	private static final long serialVersionUID = -1497561910173228213L;

	/** 打印结果或者否  **/
	private boolean print;

	/**
	 * 构造更新私有网络空间
	 * @param print 打印结果
	 */
	public RefreshCyber(boolean print) {
		super();
		setPrint(print);
	}
	
	/**
	 * 构造默认的更新私有网络空间
	 */
	public RefreshCyber() {
		this(false);
	}

	/**
	 * 生成更新私有网络空间副本
	 * @param that
	 */
	private RefreshCyber(RefreshCyber that) {
		super(that);
		print = that.print;
	}

	/**
	 * 设置打印显示
	 * @param b 真或者假
	 */
	public void setPrint(boolean b) {
		print = b;
	}

	/**
	 * 判断打印显示
	 * @return 返回真或者假
	 */
	public boolean isPrint() {
		return print;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RefreshCyber duplicate() {
		return new RefreshCyber(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(print);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		print = reader.readBoolean();
	}

}
