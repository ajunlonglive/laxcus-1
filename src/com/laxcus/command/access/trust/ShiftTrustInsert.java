/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.trust;

import com.laxcus.command.*;

/**
 * INSERT代理转发命令
 * 
 * @author scott.liang
 * @version 1.0 9/14/2017
 * @since laxcus 1.0
 */
public class ShiftTrustInsert extends ShiftCommand {

	private static final long serialVersionUID = -2036558106422814825L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTrustInsert(ShiftTrustInsert that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTrustInsert duplicate() {
		return new ShiftTrustInsert(this);
	}
	
	/**
	 * 构造INSERT代理转发命令，指定参数
	 * @param cmd INSERT代理命令
	 * @param hook INSERT代理命令钩子
	 */
	public ShiftTrustInsert(TrustInsert cmd, TrustInsertHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TrustInsert getCommand() {
		return (TrustInsert) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TrustInsertHook getHook() {
		return (TrustInsertHook) super.getHook();
	}
}
