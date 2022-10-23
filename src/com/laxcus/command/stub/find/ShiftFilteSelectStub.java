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
 * “FilteSelectStub”转发命令
 * 
 * @author scott.liang
 * @version 1.0 06/17/2013
 * @since laxcus 1.0
 */
public class ShiftFilteSelectStub extends ShiftCommand {

	private static final long serialVersionUID = 6235958801947453348L;

	/** DATA站点地址 **/
	private Node hub;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftFilteSelectStub(ShiftFilteSelectStub that){
		super(that);
		hub = that.hub;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftFilteSelectStub duplicate() {
		return new ShiftFilteSelectStub(this);
	}

	/**
	 * 构造FilteSelectStub转发命令，指定全部参数
	 * @param hub DATA站点地址
	 * @param cmd FilteSelectStub命令
	 * @param hook FilteSelectStubHook命令钩子
	 */
	public ShiftFilteSelectStub(Node hub, FilteSelectStub cmd, FilteSelectStubHook hook) {
		super(cmd, hook);
		setHub(hub);
	}

	/**
	 * 设置DATA站点地址
	 * @param e Node实例
	 */
	public void setHub(Node e) {
		Laxkit.nullabled(e);

		hub = e;
	}

	/**
	 * 返回DATA站点地址
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
	public FilteSelectStub getCommand() {
		return (FilteSelectStub) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public FilteSelectStubHook getHook() {
		return (FilteSelectStubHook) super.getHook();
	}
}