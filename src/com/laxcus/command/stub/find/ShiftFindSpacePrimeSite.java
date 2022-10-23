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
 * “FindSpacePrimeSite”转发命令
 * 
 * @author scott.liang
 * @version 1.0 06/17/2013
 * @since laxcus 1.0
 */
public class ShiftFindSpacePrimeSite extends ShiftCommand {

	private static final long serialVersionUID = 6235958801947453348L;

	/** 目标站点地址 **/
	private Node hub;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftFindSpacePrimeSite(ShiftFindSpacePrimeSite that){
		super(that);
		hub = that.hub;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftFindSpacePrimeSite duplicate() {
		return new ShiftFindSpacePrimeSite(this);
	}

	/**
	 * 构造FindSpacePrimeSite转发命令，指定全部参数
	 * @param hub 目标站点地址
	 * @param cmd FindSpacePrimeSite命令实例
	 * @param hook FindSpacePrimeSite命令钩子
	 */
	public ShiftFindSpacePrimeSite(Node hub, FindSpacePrimeSite cmd, FindSpacePrimeSiteHook hook) {
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
	public FindSpacePrimeSite getCommand() {
		return (FindSpacePrimeSite) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public FindSpacePrimeSiteHook getHook() {
		return (FindSpacePrimeSiteHook) super.getHook();
	}
}