/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * “SWITCH PARTNER”命令的本地转发命令。<br><br>
 * 
 * 这个命令由TOP/HOME监视器站点发出，通知本集群内的同级监视站点，切换到新的注册地址上。
 * 
 * @author scott.liang
 * @version 1.1 9/12/2015
 * @since laxcus 1.0
 */
public class ShiftSwitchPartner extends ShiftCommand {

	private static final long serialVersionUID = 3939832254156855084L;

	/** 目标地址 **/
	private Node remote;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftSwitchPartner(ShiftSwitchPartner that){
		super(that);
		remote = that.remote;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftSwitchPartner duplicate() {
		return new ShiftSwitchPartner(this);
	}
	
	/**
	 * 构造默认和私有的“SWITCH PARTNER”本地转发命令
	 */
	private ShiftSwitchPartner() {
		super();
	}

	/**
	 * 构造“SWITCH PARTNER”本地转发命令，指定参数
	 * @param cmd 目标地址
	 * @param remote 新的管理站点地址
	 */
	public ShiftSwitchPartner(SwitchPartner cmd, Node remote) {
		this();
		setCommand(cmd);
		setRemote(remote);
	}

	/**
	 * 设置目标站点地址
	 * @param e Node实例
	 */
	public void setRemote(Node e) {
		Laxkit.nullabled(e);

		remote = e.duplicate();
	}

	/**
	 * 返回目标站点地址
	 * @return Node实例
	 */
	public Node getRemote() {
		return remote;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public SwitchPartner getCommand() {
		return (SwitchPartner) super.getCommand();
	}

}