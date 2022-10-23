/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.command.*;

/**
 * 向BANK站点申请主机序列号转发命令。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class ShiftTakeSiteSerial extends ShiftCommand {

	private static final long serialVersionUID = 3170447349897552621L;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftTakeSiteSerial(ShiftTakeSiteSerial that){
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftTakeSiteSerial duplicate() {
		return new ShiftTakeSiteSerial(this);
	}

	/**
	 * 构造向BANK站点申请主机序列号转发命令，指定全部参数
	 * @param cmd 向BANK站点申请主机序列号命令
	 * @param hook 向BANK站点申请主机序列号命令钩子
	 */
	public ShiftTakeSiteSerial(TakeSiteSerial cmd, TakeSiteSerialHook hook) {
		super(cmd, hook);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeSiteSerial getCommand() {
		return (TakeSiteSerial) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public TakeSiteSerialHook getHook() {
		return (TakeSiteSerialHook) super.getHook();
	}
}