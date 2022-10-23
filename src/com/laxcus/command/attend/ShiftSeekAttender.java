/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.attend;

import java.util.*;

import com.laxcus.echo.*;
import com.laxcus.util.*;
import com.laxcus.command.*;

/**
 * 转发查找签到器命令
 * 
 * @author scott.liang
 * @version 1.0 3/20/2017
 * @since laxcus 1.0
 */
public class ShiftSeekAttender extends ShiftCommand {

	private static final long serialVersionUID = -6383287364436649961L;

	/** 全部调用器地址集合 **/
	private Set<Cabin> hubs = new TreeSet<Cabin>();
	
	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftSeekAttender(ShiftSeekAttender that){
		super(that);
		hubs.addAll(that.hubs);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftSeekAttender duplicate() {
		return new ShiftSeekAttender(this);
	}
	
	/**
	 * 构造默认的转发查找签到器命令
	 */
	private ShiftSeekAttender() {
		super();
	}

	/**
	 * 构造转发查找签到器命令，指定参数
	 * @param hubs 调用器地址
	 * @param hook 命令钩子
	 */
	public ShiftSeekAttender(Cabin[] hubs, SeekAttenderHook hook) {
		this();
		setHook(hook);
		addHubs(hubs);
	}

	/**
	 * 构造转发查找签到器命令，指定参数
	 * @param hubs 调用器地址
	 * @param hook 命令钩子
	 */
	public ShiftSeekAttender(Collection<Cabin> hubs, SeekAttenderHook hook) {
		this();
		setHook(hook);
		addHubs(hubs);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getHook()
	 */
	@Override
	public SeekAttenderHook getHook() {
		return (SeekAttenderHook) super.getHook();
	}

	/**
	 * 保存一个调用器地址
	 * @param e Cabin实例
	 * @return 返回真或者假
	 */
	public boolean addHub(Cabin e) {
		Laxkit.nullabled(e);

		return hubs.add(e);
	}

	/**
	 * 保存一批调用器地址
	 * @param a Cabin列表
	 * @return 返回新增成员数目
	 */
	public int addHubs(Collection<Cabin> a) {
		int size = hubs.size();
		for (Cabin e : a) {
			addHub(e);
		}
		return hubs.size() - size;
	}

	/**
	 * 保存一批调用器地址
	 * @param a Cabin数组
	 * @return 返回新增成员数目
	 */
	public int addHubs(Cabin[] a) {
		int size = hubs.size();
		for (int i = 0; a != null && i < a.length; i++) {
			addHub(a[i]);
		}
		return hubs.size() - size;
	}

	/**
	 * 输出全部调用器地址
	 * @return 返回Cabin列表
	 */
	public List<Cabin> getHubs() {
		return new ArrayList<Cabin>(hubs);
	}

	/**
	 * 返回调用器地址成员数目
	 * @return 调用器地址成员数目
	 */
	public int getHubSize() {
		return hubs.size();
	}
}