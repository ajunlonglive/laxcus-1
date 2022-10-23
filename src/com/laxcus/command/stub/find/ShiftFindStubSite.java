/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.find;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 转发“FindStubSite”命令
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public final class ShiftFindStubSite extends ShiftCommand {

	private static final long serialVersionUID = -164933733906217162L;

	/** 目标站点地址 **/
	private Node hub;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftFindStubSite(ShiftFindStubSite that){
		super(that);
		hub = that.hub;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftFindStubSite duplicate() {
		return new ShiftFindStubSite(this);
	}

	/**
	 * 构造“FindStubSite”转发命令，指定全部参数
	 * @param hub 目标站点地址（CALL站点）
	 * @param cmd 查询命令
	 * @param hook 命令钩子
	 */
	public ShiftFindStubSite(Node hub, FindStubSite cmd, FindStubSiteHook hook) {
		super(cmd, hook);
		setHub(hub);
	}

	/**
	 * 设置服务器地址
	 * @param e Node实例
	 */
	public void setHub(Node e) {
		Laxkit.nullabled(e);

		hub = e;
	}

	/**
	 * 返回服务器地址
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
	public FindStubSite getCommand() {
		return (FindStubSite) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public FindStubSiteHook getHook() {
		return (FindStubSiteHook) super.getHook();
	}

}