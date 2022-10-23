/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.transfer;

import com.laxcus.command.*;
import com.laxcus.site.Node;
import com.laxcus.util.*;

/**
 * 转发更新数据块命令。
 * 
 * @author scott.liang
 * @version 1.0 4/23/2013
 * @since laxcus 1.0
 */
public final class ShiftUpdateMass extends ShiftCommand {

	private static final long serialVersionUID = -7391276168403676075L;

	/** 目标地址 **/
	private Node site;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftUpdateMass(ShiftUpdateMass that){
		super(that);
		site = that.site;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftUpdateMass duplicate() {
		return new ShiftUpdateMass(this);
	}

	/**
	 * 构造转发更新数据块命令，指定发送命令和命令钩子
	 * @param site 目标站点地址
	 * @param cmd 设置映像数据命令
	 * @param hook 命令钩子
	 */
	public ShiftUpdateMass(Node site, UpdateMass cmd, UpdateMassHook hook) {
		super(cmd, hook);
		setSite(site);
	}

	/**
	 * 设置映像数据的目标地址
	 * @param e Node实例
	 * @throws NullPointerException - 如果地址是空值
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回映像数据的目标地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public UpdateMass getCommand() {
		return (UpdateMass) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public UpdateMassHook getHook() {
		return (UpdateMassHook) super.getHook();
	}

}