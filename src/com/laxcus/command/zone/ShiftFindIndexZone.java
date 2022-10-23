/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.zone;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.Node;

/**
 * 查询索引分区的转移命令。
 * 
 * @author scott.liang
 * @version 1.1 11/26/2015
 * @since laxcus 1.0
 */
public class ShiftFindIndexZone extends ShiftCommand {

	private static final long serialVersionUID = 6296949919744366646L;

	/** 目标地址命令 **/
	private TreeSet<Node> hubs = new TreeSet<Node>();

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftFindIndexZone(ShiftFindIndexZone that){
		super(that);
		hubs.addAll(that.hubs);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftFindIndexZone duplicate() {
		return new ShiftFindIndexZone(this);
	}
	
	/**
	 * 构造默认的查询索引分区的转移命令
	 */
	public ShiftFindIndexZone() {
		super();
	}

	/**
	 * 构造查询索引分区的转移命令，指定命令
	 * @param cmd 查询索引分区命令
	 */
	public ShiftFindIndexZone(FindIndexZone cmd) {
		super(cmd);
	}

	/**
	 * 构造查询索引分区的转移命令，指定命令和钩子
	 * @param cmd 查询索引分区命令
	 * @param hook 命令钩子
	 */
	public ShiftFindIndexZone(FindIndexZone cmd, IndexZoneHook hook) {
		super(cmd, hook);
	}

	/**
	 * 设置投递的服务器地址
	 * @param a Node数组
	 */
	public void setHubs(Collection<Node> a) {
		hubs.addAll(a);
	}

	/**
	 * 返回投递的服务器地址。
	 * @return 返回Node列表
	 */
	public List<Node> getHubs() {
		return new ArrayList<Node>(hubs);
	}

	/*
	 * 返回查找列索引值区域命令
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public FindIndexZone getCommand() {
		return (FindIndexZone)super.getCommand();
	}

	/**
	 * 返回列索引值钩子
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public IndexZoneHook getHook() {
		return (IndexZoneHook) super.getHook();
	}

}