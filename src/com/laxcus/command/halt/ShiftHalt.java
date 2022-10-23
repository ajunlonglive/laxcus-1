/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.halt;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.Node;

/**
 * 转发中断连接命令。
 * 
 * @author scott.liang
 * @version 1.0 7/12/2012
 * @since laxcus 1.0
 */
public final class ShiftHalt extends ShiftCommand {
	
	private static final long serialVersionUID = -2140632474951270755L;
	
	/** 目标站点 **/
	private Node[] sites;

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftHalt(ShiftHalt that){
		super(that);
		sites = that.sites;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftHalt duplicate() {
		return new ShiftHalt(this);
	}
	
	/**
	 * 构造默认和私有的转发中断连接命令
	 */
	private ShiftHalt() {
		super();
	}

	/**
	 * 构造转发中断连接命令，指定全部目标站点地址和中断命令
	 * @param sites 目标站点地址
	 * @param halt 中断命令
	 */
	public ShiftHalt(Node[] sites, Halt halt) {
		this();
		setSites(sites);
		setCommand(halt);
	}

	/**
	 * 构造转发中断连接命令，指定全部目标站点地址和中断命令
	 * @param sites 全部目标站点地址
	 * @param halt 中断命令
	 */
	public ShiftHalt(Collection<Node> sites, Halt halt) {
		this();
		setSites(sites);
		setCommand(halt);
	}

	/**
	 * 构造转发中断连接命令，指定目标站点地址和中断命令
	 * @param site 一个目标地址
	 * @param halt 中断命令
	 */
	public ShiftHalt(Node site, Halt halt) {
		this(new Node[] { site }, halt);
	}

	/**
	 * 设置目标站点地址
	 * @param e Node数组
	 */
	public void setSites(Node[] e) {
		if (e == null || e.length == 0) {
			throw new NullPointerException();
		}
		sites = e;
	}

	/**
	 * 设置目标站点地址
	 * @param a 目标站点
	 */
	public void setSites(Collection<Node> a) {
		sites = new Node[a.size()];
		sites = a.toArray(sites);
	}

	/**
	 * 返回目标站点地址
	 * @return Node数组
	 */
	public Node[] getSites() {
		return sites;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public Halt getCommand() {
		return (Halt) super.getCommand();
	}
}
