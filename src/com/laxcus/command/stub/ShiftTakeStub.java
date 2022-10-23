/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub;

import com.laxcus.command.*;

/**
 * 转发“TAKE STUB”命令
 * 
 * @author scott.liang
 * @version 1.0 03/04/2012
 * @since laxcus 1.0
 */
public final class ShiftTakeStub extends ShiftCommand {

	private static final long serialVersionUID = -3970277713864946296L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeStub(ShiftTakeStub that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeStub duplicate() {
		return new ShiftTakeStub(this);
	}

	/**
	 * 构造“TAKE STUB”转发命令，指定“TAKE STUB”命令
	 * @param cmd TakeStub实例
	 */
	public ShiftTakeStub(TakeStub cmd) {
		super(cmd);
	}

	/**
	 * 构造“TAKE STUB”转发命令，指定全部参数
	 * @param cmd TakeStub实例
	 * @param hook TakeStubHook实例
	 */
	public ShiftTakeStub(TakeStub cmd, TakeStubHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeStub getCommand() {
		return (TakeStub) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeStubHook getHook() {
		return (TakeStubHook) super.getHook();
	}

}