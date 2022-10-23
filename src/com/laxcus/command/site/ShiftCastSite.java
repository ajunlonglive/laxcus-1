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

/**
 * 广播注册站点的转发命令。<br><br>
 * 
 * 这个命令由TOP/HOME/BANK的管理站点发出，转发一个广播注册站点给下属的监视站点，增加或者删除一个站点地址。
 * 这是内处理命令，只发生在发出TOP/HOME/BANK管理站点和监视器站点、WATCH站点之间。
 * 
 * @author scott.liang
 * @version 1.1 11/15/2015
 * @since laxcus 1.0
 */
public final class ShiftCastSite extends ShiftCommand {

	private static final long serialVersionUID = -2895705433410708769L;

	/** 被发送到的目标站点地址 **/
	private Node[] remotes;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftCastSite(ShiftCastSite that){
		super(that);
		remotes = that.remotes;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftCastSite duplicate() {
		return new ShiftCastSite(this);
	}
	
	/**
	 * 构造广播注册站点转发命令，指定发送命令和一批目标站点地址
	 * @param cmd 被发送的命令
	 * @param endpoints 目标站点地址
	 */
	public ShiftCastSite(CastSite cmd, Node[] endpoints) {
		super(cmd);
		setRemotes(endpoints);
	}

	/**
	 * 构造广播注册站点转发实例，指定发送命令和目标地址
	 * @param cmd 被发送的命令
	 * @param endpoint 目标站点地址
	 */
	public ShiftCastSite(CastSite cmd, Node endpoint) {
		this(cmd, new Node[] { endpoint });
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public CastSite getCommand() {
		return (CastSite) super.getCommand();
	}

	/**
	 * 设置目标站点站点地址
	 * @param e Node数组
	 */
	public void setRemotes(Node[] e) {
		remotes = e;
	}

	/**
	 * 返回目标站点地址
	 * @return Node数组
	 */
	public Node[] getRemotes() {
		return remotes;
	}

}