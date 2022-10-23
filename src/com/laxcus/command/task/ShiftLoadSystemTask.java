/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.naming.*;

/**
 * 转发获取保存系统组件ACCOUNT站点命令
 * 
 * @author scott.liang
 * @version 1.0 10/11/2019
 * @since laxcus 1.0
 */
public class ShiftLoadSystemTask extends ShiftCommand {

	private static final long serialVersionUID = 3450276857109190025L;

	/** 阶段类型 **/
	private TreeSet<Integer> families = new TreeSet<Integer>();

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftLoadSystemTask(ShiftLoadSystemTask that){
		super(that);
		families.addAll(that.families);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftLoadSystemTask duplicate() {
		return new ShiftLoadSystemTask(this);
	}

	/**
	 * 构造转发获取保存系统组件ACCOUNT站点命令，指定所需参数
	 * @param cmd 上传命令
	 * @param hook 异步钩子
	 */
	public ShiftLoadSystemTask(TakeSystemTaskSite cmd) {
		super(cmd);
	}


	/**
	 * 保存投递的阶段位置
	 * @param who 阶段位置
	 */
	public boolean addFamily(int who) {
		// 如果不是阶段类型，忽略它！
		if (!PhaseTag.isPhase(who)){
			return false;
		}
		// 保存
		return families.add(who);
	}

	/**
	 * 返回全部投递的阶段位置
	 * @return Integer列表
	 */
	public List<Integer> getFamilies() {
		return new ArrayList<Integer>(families);
	}

	/**
	 * 有阶段类型
	 * @return 真或者假
	 */
	public boolean hasFamily() {
		return families.size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public TakeSystemTaskSite getCommand() {
		return (TakeSystemTaskSite) super.getCommand();
	}

}