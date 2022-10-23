/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.reflex;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 转发查询映像数据块站点命令。
 * 
 * @author scott.liang
 * @version 1.0 11/2/2013
 * @since laxcus 1.0
 */
public final class ShiftFindReflexStubSite extends ShiftCommand {

	private static final long serialVersionUID = 4190679844223050906L;
	
	/** 目标站点地址 **/
	private Node hub;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftFindReflexStubSite(ShiftFindReflexStubSite that){
		super(that);
		hub = that.hub;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftFindReflexStubSite duplicate() {
		return new ShiftFindReflexStubSite(this);
	}

	/**
	 * 构造查询映像数据块站点，指定发送命令和钩子
	 * @param hub 目标站点地址
	 * @param cmd 查询映像数据块站点命令
	 * @param hook 查询映像数据块站点命令钩子
	 */
	public ShiftFindReflexStubSite(Node hub, FindReflexStubSite cmd, FindReflexStubSiteHook hook) {
		super(cmd, hook);
		setHub(hub);
	}

	/**
	 * 设置被查询站点地址
	 * @param e Node实例
	 */
	public void setHub(Node e) {
		Laxkit.nullabled(e);

		hub = e;
	}

	/**
	 * 返回被查询站点地址
	 * @return Node实例
	 */
	public Node getHub() {
		return hub;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public FindReflexStubSite getCommand() {
		return (FindReflexStubSite) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public FindReflexStubSiteHook getHook() {
		return (FindReflexStubSiteHook) super.getHook();
	}

}
