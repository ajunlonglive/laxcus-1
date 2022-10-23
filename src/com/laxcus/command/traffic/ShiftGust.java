/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.traffic;

import com.laxcus.command.*;

/**
 * 检测数据传输流量转发命令。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 8/21/2018
 * @since laxcus 1.0
 */
public class ShiftGust extends ShiftCommand {

	private static final long serialVersionUID = 3170447349897552621L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftGust(ShiftGust that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftGust duplicate() {
		return new ShiftGust(this);
	}

	/**
	 * 构造检测数据传输流量转发命令，指定全部参数
	 * @param cmd 检测数据传输流量命令
	 * @param hook 检测数据传输流量命令钩子
	 */
	public ShiftGust(Gust cmd, GustHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public Gust getCommand() {
		return (Gust) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public GustHook getHook() {
		return (GustHook) super.getHook();
	}
}