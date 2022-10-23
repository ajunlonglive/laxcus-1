/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.command.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 回滚历史数据。<br>
 * 此操作由管理员通过WATCH站点发出，目标是DATA主节点。这个操作是当系统发生数据处理故障后，管理员恢复数据的一个操作。
 * 另一种情况是DATA主节点自己检测到错误，自己发命令自己处理。
 * 两种情况都将DATA主节点的“回滚目录”下的数据，回退到存储层。
 * 
 * 
 * @author scott.liang
 * @version 1.1 11/23/2016
 * @since laxcus 1.0
 */
public class RollbackHistory extends Command {
	
	private static final long serialVersionUID = 8588550279900909236L;
	
	/** DATA主节点 **/
	private Node site;

	/**
	 * 构造默认和私有的回滚历史数据
	 */
	private RollbackHistory() {
		super();
	}

	/**
	 * 构造回滚历史数据，指定DATA站点地址
	 * @param site DATA站点地址
	 */
	public RollbackHistory(Node site) {
		this();
		setSite(site);
	}

	/**
	 * 生成回滚历史数据的数据副本
	 * @param that RollbackHistory实例
	 */
	private RollbackHistory(RollbackHistory that) {
		super(that);
		site = that.site;
	}

	/**
	 * 从可类化数据读取器中解析回滚历史数据
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RollbackHistory(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置目标站点，必须是DATA站点
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		// 如果不是DATA站点，弹出异常
		if (!e.isData()) {
			throw new IllegalValueException("illegal site:%s", e);
		}
		site = e;
	}

	/**
	 * 返回目标站点
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RollbackHistory duplicate() {
		return new RollbackHistory(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = new Node(reader);
	}

}