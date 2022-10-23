/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import java.util.*;

import com.laxcus.access.index.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * <code>SQL WHERE</code>检索条件。<br>
 * 
 * @author scott.liang
 * @version 1.2 8/28/2015
 * @since laxcus 1.0
 */
public final class Where extends Gradation {

	private static final long serialVersionUID = 8205913390821991883L;

	/** 被比较的索引参数 **/
	private WhereIndex index;

	/** 同级关联单元 **/
	private ArrayList<Where> partners = new ArrayList<Where>(3);

	/** 子级检索条件，与当前对象链接起来，形成单向链表关系 **/
	private Where next;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Gradation#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 1. 写入WHERE检索(带类名的方式写入)
		writer.writeDefault(index);
		// 2. 写入同级成员
		writer.writeInt(partners.size());
		for (Where e : partners) {
			writer.writeObject(e);
		}
		// 子级参数
		writer.writeInstance(next);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.Gradation#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 1. 解析WHERE检索
		index = (WhereIndex) reader.readDefault();
		// 2. 取同级成员
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Where e = new Where(reader);
			partners.add(e);
		}
		// 3. 子级成员
		next = reader.readInstance(Where.class);
	}

	/**
	 * 根据传入参数，建立一个检索条件的副本
	 * @param that Where实例
	 */
	private Where(Where that) {
		super(that);
		if (that.index != null) {
			setIndex(that.index.duplicate());
		}
		for (Where e : that.partners) {
			partners.add((Where) e.duplicate());
		}
		if (that.next != null) {
			next = (Where) that.next.duplicate();
		}
	}

	/**
	 * 建立一个空的检索条件
	 */
	public Where() {
		super();
	}

	/**
	 * 从可类化读取器中解析SQL WHERE检索条件
	 * @param reader 可类化读取器
	 * @since 1.2
	 */
	public Where(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造检索条件，指定比较符号、比较值
	 * @param compare 比较符号(等于、不等于、大于、小于、大于等于、小于等于、LIKE、IS NULL、IS NOT NULL、IS EMPTY、IS NOT EMPTY、IN)
	 * @param index 比较值
	 */
	public Where(byte compare, WhereIndex index) {
		this();
		setCompare(compare);
		setIndex(index);
	}

	/**
	 * 构造检索条件，指定同级逻辑联系关系、比较符、比较值
	 * @param related 同级逻辑联系关系
	 * @param compare 比较符
	 * @param index 比较值
	 */
	public Where(byte related, byte compare, WhereIndex index) {
		this();
		setRelation(related);
		setCompare(compare);
		setIndex(index);
	}

	/**
	 * 返回检索的列编号，0是无效
	 * @return short类型列编号
	 */
	public short getColumnId() {
		if (index != null) {
			return index.getColumnId();
		}
		// 无效是0
		return 0;
	}

	/**
	 * 设置检索值
	 * @param e WhereIndex实例
	 */
	public void setIndex(WhereIndex e) {
		index = e;
	}

	/**
	 * 返回检索值
	 * @return WhereIndex实例
	 */
	public WhereIndex getIndex() {
		return index;
	}

	/**
	 * 保存一个同级检索条件
	 * @param e Where实例
	 */
	public void addPartner(Where e) {
		Laxkit.nullabled(e);

		partners.add(e);
	}

	/**
	 * 输出全部同级检索条件
	 * @return 输出Where列表
	 */
	public List<Where> getPartners() {
		return new ArrayList<Where>(partners);
	}

	/**
	 * 关联一个检索条件。把它绑定到最后，形成单向链表
	 * @param e Where实例
	 */
	public void attach(Where e) {
		if (next == null) {
			next = e;
		} else {
			next.attach(e); //继续关联到它的下级
		}
	}

	/**
	 * 返回单向链表的子级检索对象。如果没有是空指针
	 * @return 下一个Where实例
	 */
	public Where next() {
		return next;
	}

	/**
	 * 返回单向链表中的最后一个有效检索，如果没有子级，就是对象自己。
	 * @return Where实例
	 */
	public Where lastEnabled() {
		if(next != null) {
			return next.lastEnabled();
		}
		// 返回自己的句柄
		return this;
	}
	
	/**
	 * 判断是嵌套查询
	 * @return 返回真或者假
	 */
	public boolean isNested() {
		// 1. 查找子级
		boolean nested = false;
		if (next != null) {
			nested = next.isNested();
		}
		// 2. 子级未到找情况下找同级从属条件
		if (!nested) {
			for (Where condi : partners) {
				nested = condi.isNested();
				if (nested) break;
			}
		}
		// 3. 前两项不成功，检查本例
		if (!nested) {
			nested = (index != null && index.isNestedIndex());
		}
		// 返回结果
		return nested;
	}

	/**
	 * 检测是否包含SELECT子检索
	 * @return 返回真或者假
	 */
	public boolean hasSubSelect() {
		//1. 查找子级
		boolean enabled = false;
		if (next != null) {
			enabled = next.hasSubSelect();
		}
		//2. 子级未到找情况下找同级从属条件
		if (!enabled) {
			for (Where condi : partners) {
				enabled = condi.hasSubSelect();
				if (enabled) break;
			}
		}
		//3. 前两项不成功，检查本例
		if (!enabled) {
			enabled = (index != null && index.isNestedIndex());
		}
		// 返回结果
		return enabled;
	}
	
	/**
	 * 判断有相关的索引
	 * @param family 索引类型
	 * @return 返回真或者假
	 */
	public boolean hasIndex(byte family) {
		// 判断子级有匹配索引
		if (next != null) {
			return next.hasIndex(family);
		}
		// 同级对象匹配
		for (Where where : partners) {
			if (where.hasIndex(family)) {
				return true;
			}
		}
		// 本例对象判断索引
		if (index != null) {
			if (index.getFamily() == family) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断支持底层的JNI检索
	 * @return 返回真或者假
	 */
	public boolean isSupportJNI() {
		// 下一级对象
		if (next != null) {
			if (!next.isSupportJNI()) {
				return false;
			}
		}

		// 同级对象
		for (Where condi : partners) {
			boolean success = condi.isSupportJNI();
			if (!success) return false;
		}

		// 当前索引对象
		if (index == null) {
			return false;
		}
		// 判断继承自ColumnIndex，如果不是，不支持底层比较
		if (!Laxkit.isClassFrom(index, ColumnIndex.class)) {
			return false;
		}
		// 判断比较符支持底层比较
		if (!CompareOperator.isSupportJNI(getCompare())) {
			return false;
		}

		return true;
	}

	/** 
	 * 递归查找最后一个索引类型匹配的条件 <br>
	 * 三步优先级检查: <br>
	 * <1> 子级检查 (最高) <br>
	 * <2> 伙伴级检查 (其次) <br>
	 * <3> 自参数检查 (再次) <br>
	 * 
	 * @param source Where实例
	 * @return Where实例
	 */
	public static Where findLastSelectCondition(Where source) {
		Where condi = null;
		//1. 首先找最后
		if (source.next != null) {
			condi = Where.findLastSelectCondition( source.next );
		}
		//2. 在未到情况下，找同级最后一个
		if (condi == null) {
			// 找到最后一个
			for (Where partner : source.partners) {
				Where sub = Where.findLastSelectCondition(partner);
				if (sub != null) condi = sub;
			}
		}
		// 前两项没找到，判断本例
		if (condi == null && source.index != null && source.index.isNestedIndex()) {
			Select select = ((NestedIndex) source.index).getSelect();
			Where slave = select.getWhere();

			// 两种情况: <1>有子条件 <2>如果没有子条件就是本例
			Where sub = Where.findLastSelectCondition(slave);
			if(sub != null) condi = sub;
			else condi = source;
		}

		return condi;
	}

	/**
	 * 根据当前检索条件，生成它的数据副本
	 * @see com.laxcus.command.access.Gradation#duplicate()
	 */
	@Override
	public Where duplicate() {
		return new Where(this);
	}
	
	/**
	 * 设置命令发起人签名<br>
	 * @param issuer Siger实例
	 */
	public void setIssuer(Siger issuer) {
		// 被比较参数
		if (index != null) {
			index.setIssuer(issuer);
		}
		// 同级比较参数
		for (Where sub : partners) {
			sub.setIssuer(issuer);
		}
		// 子级比较参数
		if (next != null) {
			next.setIssuer(issuer);
		}
	}

	
}