/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rule;

import com.laxcus.law.rule.*;
import com.laxcus.util.classable.*;

/**
 * 撤销事务命令。<br>
 * 
 * 这个命令由FRONT站点发送给GATE站点。要求回收之前通过“AttachRule”命令锁定的资源
 * 
 * @author scott.liang
 * @version 1.1 6/2/2015
 * @since laxcus 1.0
 */
public final class DetachRule extends ProcessRule {

	private static final long serialVersionUID = 4385774752318837108L;

	/**
	 * 根据传入的撤销事务命令，生成它的数据副本
	 * @param that DetachRule实例
	 */
	private DetachRule(DetachRule that) {
		super(that);
	}

	/**
	 * 生产撤销事务命令
	 * @param that AttachRule实例
	 */
	protected DetachRule(AttachRule that) {
		super(that);
	}

	/**
	 * 构造默认和私有的撤销事务命令
	 */
	private DetachRule() {
		super();
	}

	/**
	 * 构造撤销事务命令，指定标识
	 * @param tag 事务处理标识
	 */
	public DetachRule(ProcessRuleTag tag) {
		this();
		setTag(tag);
	}
	
	/**
	 * 构造撤销事务命令，指定参数
	 * @param tag 事务处理标识
	 * @param sheet 事务规则表
	 */
	public DetachRule(ProcessRuleTag tag, RuleSheet sheet) {
		this(tag);
		addAll(sheet);
	}

	/**
	 * 从可类化数据读取器中解析撤销事务命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public DetachRule(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DetachRule duplicate() {
		return new DetachRule(this);
	}

}