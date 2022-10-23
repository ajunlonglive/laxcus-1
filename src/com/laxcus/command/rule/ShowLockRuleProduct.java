/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rule;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 显示事务规则的返回结果
 * 
 * @author scott.liang
 * @version 1.0 4/2/2017
 * @since laxcus 1.0
 */
public class ShowLockRuleProduct extends EchoProduct {
	
	private static final long serialVersionUID = 5772327055350429408L;

	/** 处理状态的事务规则 **/
	private RuleSheet running = new RuleSheet();
	
	/** 等待状态的事务规则 **/
	private RuleSheet waiting = new RuleSheet();

	/**
	 * 构造默认的显示事务规则的返回结果
	 */
	public ShowLockRuleProduct() {
		super();
	}

	/**
	 * 生成显示事务规则的返回结果的数据副本
	 * @param that ShowRuleProduct实例
	 */
	private ShowLockRuleProduct(ShowLockRuleProduct that) {
		super(that);
		running.addAll(that.running);
		waiting.addAll(that.waiting);
	}

	/**
	 * 从可类化数据读取器中解析显示事务规则的返回结果
	 * @param reader 可类化数据读取器
	 */
	public ShowLockRuleProduct(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 保存运行状态的事务规则，不允许空指针
	 * @param e RuleItem实例
	 * @return 返回成功返回真，否则假
	 */
	public boolean addRunRule(RuleItem e) {
		Laxkit.nullabled(e);

		return running.add(e);
	}
	
	/**
	 * 保存一批运行状态的事务规则
	 * @param a RuleItem数组
	 * @return 返回新增成员数目
	 */
	public int addRunRules(Collection<RuleItem> a) {
		int size = running.size();
		for (RuleItem e : a) {
			addRunRule(e);
		}
		return running.size() - size;
	}

	/**
	 * 输出全部运行状态的事务规则
	 * @return RuleItem列表
	 */
	public List<RuleItem> getRunRules() {
		return running.list();
	}

	/**
	 * 保存等待状态的事务规则，不允许空指针
	 * @param e RuleItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addWaitRule(RuleItem e) {
		Laxkit.nullabled(e);

		return waiting.add(e);
	}

	/**
	 * 保存一批等待状态的事务规则
	 * @param a RuleItem数组
	 * @return 返回新增成员数目
	 */
	public int addWaitRules(Collection<RuleItem> a) {
		int size = waiting.size();
		for (RuleItem e : a) {
			addWaitRule(e);
		}
		return waiting.size() - size;
	}

	/**
	 * 返回全部等待状态的事务规则
	 * @return RuleItem列表
	 */
	public List<RuleItem> getWaitRules() {
		return waiting.list();
	}

	/**
	 * 统计全部成员数目
	 * @return 全部成员数目
	 */
	public int size() {
		return running.size() + waiting.size();
	}

	/**
	 * 判断是空值。运行状态和等待状态都没有
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return running.isEmpty() && waiting.isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ShowLockRuleProduct duplicate() {
		return new ShowLockRuleProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(running);
		writer.writeObject(waiting);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		running = new RuleSheet(reader);
		waiting = new RuleSheet(reader);
	}

}