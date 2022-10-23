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
 * “SWITCH HUB”命令的本地转发命令。
 * 这个命令由TOP/HOME的监视站点发出，目标是本集群内的全部下级站点，通知它们切换到新的注册地址。这个注册地址的站点已经成为新的值守站点。
 * 
 * @author scott.liang
 * @version 1.1 9/12/2015
 * @since laxcus 1.0
 */
public class ShiftSwitchHub extends ShiftCommand {

	private static final long serialVersionUID = 3939832254156855084L;

	/** 目标地址 **/
	private Node remote;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftSwitchHub(ShiftSwitchHub that){
		super(that);
		remote = that.remote;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftSwitchHub duplicate() {
		return new ShiftSwitchHub(this);
	}
	
	/**
	 * 构造默认和私有的“SWITCH HUB”本地转发命令
	 */
	private ShiftSwitchHub() {
		super();
	}

	/**
	 * 构造“SWITCH HUB”本地转发命令，指定参数
	 * @param cmd SwitchHub实例
	 * @param remote Node实例
	 */
	public ShiftSwitchHub(SwitchHub cmd, Node remote) {
		this();
		setCommand(cmd);
		this.setRemote(remote);
	}
	
	/**
	 * 设置目标站点地址
	 * @param e Node实例
	 */
	public void setRemote(Node e) {
		Laxkit.nullabled(e);

		this.remote = (Node) e.clone();
	}

	/**
	 * 返回目标站点地址
	 * @return Node实例
	 */
	public Node getRemote() {
		return this.remote;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public SwitchHub getCommand() {
		return (SwitchHub) super.getCommand();
	}

}