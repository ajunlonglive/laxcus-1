/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.field;

import java.util.*;

import com.laxcus.command.*;

import com.laxcus.site.Node;
import com.laxcus.util.*;

/**
 * 转发筛选元数据命令。<br><br>
 * 
 * 这个命令在HOME站点建立和使用，发送请求投递命令到DATA/WORK/BUILD三种站点。要求这三个站点将指定账号的元数据，以“PushXXXField”命令投递到CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 4/19/2013
 * @since laxcus 1.0
 */
public final class ShiftSelectFieldToCall extends ShiftCommand {

	private static final long serialVersionUID = 7635152131755017261L;

	/** 目标站点地址，是DATA/WORK/BUILD中的任意一种 **/
	private Node site;
	
	/** 命令成员数组 **/
	private ArrayList<SelectFieldToCall> array = new ArrayList<SelectFieldToCall>();

	/**
	 * 生成转发命令的数据副本
	 * @param that 转发命令
	 */
	private ShiftSelectFieldToCall(ShiftSelectFieldToCall that){
		super(that);
		site = that.site;
		array.addAll(that.array);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftSelectFieldToCall duplicate() {
		return new ShiftSelectFieldToCall(this);
	}
	
	/**
	 * 构造转发投递命令，指定目标地址和命令集合
	 * @param site Node实例
	 * @param cmds SelectFieldToCall命令数组
	 */
	public ShiftSelectFieldToCall(Node site, Collection<SelectFieldToCall> cmds) {
		super();
		setSite(site);
		addAll(cmds);
	}
	
	/**
	 * 构造转发投递命令，指定目标地址和命令
	 * @param site Node实例
	 * @param cmd SelectFieldToCall命令实例
	 */
	public ShiftSelectFieldToCall(Node site, SelectFieldToCall cmd) {
		super();
		setSite(site);
		add(cmd);
	}

	/**
	 * 设置目标站点地址。只能是DATA/WORK/BUILD中的任意一种
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		// 空指针
		Laxkit.nullabled(e);

		// 不是以下三种地址时，是错误
		if (!(e.isData() || e.isWork() || e.isBuild())) {
			throw new IllegalValueException("illegal site:%s", e);
		}
		site = e;
	}

	/**
	 * 返回目标站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}
	
	/**
	 * 保存待转发的命令
	 * @param e SelectFieldToCall实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(SelectFieldToCall e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批待转发的命令
	 * @param a SelectFieldToCall数组
	 * @return 返回新增加的命令数目
	 */
	public int addAll(Collection<SelectFieldToCall> a) {
		int size = array.size();
		for (SelectFieldToCall e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部待转发的命令列表
	 * @return SelectFieldToCall列表
	 */
	public List<SelectFieldToCall> list() {
		return new ArrayList<SelectFieldToCall>(array);
	}
	
	/**
	 * 统计全部命令数目
	 * @return 命令数目
	 */
	public int size() {
		return array.size();
	}
	
	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() ==0;
	}

}