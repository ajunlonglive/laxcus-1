/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rule;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 处理事务操作。<br>
 * 
 * 事务处理集中在GATE站点，GATE站点管理命令之间的读写、互斥、并行关系。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2014
 * @since laxcus 1.0
 */
public abstract class ProcessRule extends Command {

	private static final long serialVersionUID = -6407429084874847882L;

	/** 处理标识 **/
	private ProcessRuleTag tag;

	/** 事务规则集合 **/
	private TreeSet<RuleItem> rules = new TreeSet<RuleItem>();

	/**
	 * 构造默认的处理事务操作命令
	 */
	protected ProcessRule() {
		super();
	}

	/**
	 * 根据传入的处理事务操作命令实例，生成它的数据副本
	 * @param that ProcessRule实例
	 */
	protected ProcessRule(ProcessRule that) {
		super(that);
		tag = that.tag;
		rules.addAll(that.rules);
	}

	/**
	 * 设置事务处理标识
	 * @param e ProcessRuleTag实例
	 */
	public void setTag(ProcessRuleTag e) {
		Laxkit.nullabled(e);

		tag = e;
	}

	/**
	 * 返回事务处理标识
	 * @return ProcessRuleTag实例
	 */
	public ProcessRuleTag getTag() {
		return tag;
	}

	/**
	 * 建立事务规则表
	 * @return RuleSheet实例
	 */
	public RuleSheet createSheet() {
		return new RuleSheet(rules);
	}

	/**
	 * 保存一个事务规则
	 * @param e RuleItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(RuleItem e) {
		Laxkit.nullabled(e);

		return rules.add(e);
	}

	/**
	 * 保存一批事务规则 
	 * @param a 锁定单元数组
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<RuleItem> a) {
		int size = rules.size();
		for (RuleItem e : a) {
			add(e);
		}
		return rules.size() - size;
	}

	/**
	 * 保存一批事务规则 
	 * @param e 事务规则表
	 * @return 返回新增加的成员数目
	 */
	public int addAll(RuleSheet e) {
		Laxkit.nullabled(e);

		return addAll(e.list());
	}

	/**
	 * 删除一个事务规则
	 * @param e RuleItem实例
	 * @return 返回真或者假
	 */
	public boolean remove(RuleItem e) {
		if (e != null) {
			return rules.remove(e);
		}
		return false;
	}

	/**
	 * 删除全部事务规则
	 * @param a 事务规则数组
	 * @return 返回被删除的成员数目
	 */
	public int removeAll(Collection<RuleItem> a) {
		int size = rules.size();
		for (RuleItem e : a) {
			remove(e);
		}
		return size - rules.size();
	}

	/**
	 * 输出全部事务规则
	 * @return RuleItem列表
	 */
	public List<RuleItem> list() {
		return new ArrayList<RuleItem>(rules);
	}

	/**
	 * 判断包含一个事务规则
	 * @param e RuleItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(RuleItem e) {
		return rules.contains(e);
	}

	/**
	 * 返回事务规则成员数目
	 * @return 成员数目
	 */
	public int size() {
		return rules.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/**
	 * 比较两个对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		return compareTo((Command) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#hashCode()
	 */
	@Override
	public int hashCode() {
		return tag.hashCode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#toString()
	 */
	@Override
	public String toString() {
		return tag.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#compareTo(com.laxcus.command.Command)
	 */
	@Override
	public int compareTo(Command cmd) {
		// 空值在前
		if (cmd == null) {
			return 1;
		}
		// 不一致，交给上层处理
		if (!Laxkit.isClassFrom(cmd, ProcessRule.class)) {
			return super.compareTo(cmd);
		}

		ProcessRule that = (ProcessRule) cmd;

		// 比较参数
		int ret = Laxkit.compareTo(tag, that.tag);
		if (ret == 0) {
			ret = Laxkit.compareTo(rules.size(), that.rules.size());
		}
		// 逐一比较
		if (ret == 0) {
			int count = 0;
			for (RuleItem s : rules) {
				for (RuleItem e : that.rules) {
					if (Laxkit.compareTo(s, e) == 0) {
						count++;
						break;
					}
				}
			}
			ret = Laxkit.compareTo(rules.size(), count);
		}

		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 事务处理标识
		writer.writeObject(tag);
		// 事务规则单元
		writer.writeInt(rules.size());
		for (RuleItem e : rules) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 事务处理标识
		tag = new ProcessRuleTag(reader);
		// 事务规则单元
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			RuleItem e = RuleItemCreator.resolve(reader);
			rules.add(e);
		}
	}

}