/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.find;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * “FindSpaceSite”转发命令
 * 
 * @author scott.liang
 * @version 1.0 5/28/2012
 * @since laxcus 1.0
 */
public class ShiftFindSpaceSite extends ShiftCommand {

	private static final long serialVersionUID = 6235958801947453348L;

	/** 目标站点地址 **/
	private Node hub;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftFindSpaceSite(ShiftFindSpaceSite that){
		super(that);
		hub = that.hub;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftFindSpaceSite duplicate() {
		return new ShiftFindSpaceSite(this);
	}

	/**
	 * 构造FindSpaceSite转发命令，指定全部参数
	 * @param hub 目标站点地址
	 * @param cmd FindSpaceSite命令
	 * @param hook FindSpaceSite命令钩子
	 */
	public ShiftFindSpaceSite(Node hub, FindTableSite cmd, FindTableSiteHook hook) {
		super(cmd, hook);
		setHub(hub);
	}

	/**
	 * 设置目标站点地址
	 * @param e Node实例
	 */
	public void setHub(Node e) {
		Laxkit.nullabled(e);

		hub = e;
	}

	/**
	 * 返回目标站点地址
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
	public FindTableSite getCommand() {
		return (FindTableSite) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public FindTableSiteHook getHook() {
		return (FindTableSiteHook) super.getHook();
	}
}