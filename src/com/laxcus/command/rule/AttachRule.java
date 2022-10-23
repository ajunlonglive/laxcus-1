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
 * 绑定事务操作。<br><br>
 * 
 * 命令由FRONT站点发出，目标是GATE站点。GATE站点将根据“AttachRule”命令中的参数锁定事务资源。<br>
 * 
 * 绑定事务操作（AttachRule）和撤销事务操作（DetachRule）互相关联，都作用到GATE站点上。
 * 确认两个命令存在关联的回显地址，必须保证回显地址是一致的。
 * 
 * @author scott.liang
 * @version 1.1 6/2/2015
 * @since laxcus 1.0
 */
public final class AttachRule extends ProcessRule {

	private static final long serialVersionUID = 2339594002138032616L;

	/**
	 * 根据传入的绑定事务操作，生成它的数据副本
	 * @param that AttachRule实例
	 */
	private AttachRule(AttachRule that) {
		super(that);
	}
	
	/**
	 * 构造默认和私有的绑定事务操作
	 */
	private AttachRule() {
		super();
	}
	
	/**
	 * 构造绑定事务操作，指定标识
	 * @param tag 事务处理标识
	 */
	public AttachRule(ProcessRuleTag tag) {
		this();
		setTag(tag);
	}

	/**
	 * 构造绑定事务操作，指定参数
	 * @param tag 事务处理标识
	 * @param sheet 事务规则表
	 */
	public AttachRule(ProcessRuleTag tag, RuleSheet sheet) {
		this(tag);
		addAll(sheet);
	}
	
	/**
	 * 从可类化数据读取器中解析绑定事务操作
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AttachRule(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成反向命令
	 * @return DetachRule实例
	 */
	public DetachRule reverse() {
		return new DetachRule(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AttachRule duplicate() {
		return new AttachRule(this);
	}

}